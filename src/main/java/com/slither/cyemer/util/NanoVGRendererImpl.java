package com.slither.cyemer.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.slither.cyemer.Faith;
import java.awt.Color;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_276;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import org.lwjgl.BufferUtils;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class NanoVGRendererImpl implements IFaithRenderer {
   private static final Logger LOGGER = LoggerFactory.getLogger(NanoVGRendererImpl.class);
   private long nvg = 0L;
   private int regularFont = -1;
   private boolean initialized = false;
   private boolean frameActive = false;
   private boolean savedDepthTest;
   private boolean savedCullFace;
   private boolean savedBlend;
   private int savedBlendSrcRgb;
   private int savedBlendDstRgb;
   private int savedBlendSrcAlpha;
   private int savedBlendDstAlpha;
   private int savedBlendEquation;
   private boolean savedScissorTest;
   private int[] savedViewport = new int[4];
   private int savedActiveTexture;
   private int savedTextureBinding;
   private int savedProgram;
   private int savedArrayBuffer;
   private int savedElementArrayBuffer;
   private int savedVertexArray;
   private int savedDrawFbo = -1;
   private int savedReadFbo = -1;
   private int[] savedScissorBox = new int[4];
   private boolean savedStencilTest;
   private int stencilRbo = 0;
   private int stencilRboWidth = -1;
   private int stencilRboHeight = -1;
   private int stencilRboFbo = -1;
   private class_276 blurFramebuffer = null;
   private static final class_2960 BLUR_SHADER = class_2960.method_60656("blur");
   private int blurFBO = -1;
   private int blurTexture = -1;
   private int blurWidth = -1;
   private int blurHeight = -1;
   private boolean usingOffscreenFbo = false;
   private int offscreenPrevFbo = 0;
   private int offscreenFbo = 0;
   private int offscreenColorTex = 0;
   private int offscreenStencilRbo = 0;
   private int offscreenWidthPx = -1;
   private int offscreenHeightPx = -1;
   private int blitProgram = 0;
   private int blitVao = 0;
   private int blitVbo = 0;
   private int blitUniformTex = -1;

   private boolean isSalt(char c) {
      return c == '$' || c == '^' || c == '~' || c == '`';
   }

   public void init() {
      if (!this.initialized) {
         try {
            if (!RenderSystem.isOnRenderThread()) {
               return;
            }

            this.nvg = NanoVGGL3.nvgCreate(3);
            if (this.nvg == 0L) {
               throw new RuntimeException("Could not initialize NanoVG");
            }

            this.loadFonts();
            this.ensureBlitResources();
            this.initialized = true;
         } catch (Exception var2) {
            var2.printStackTrace();
            System.err.println("NanoVG init failed, forcing Vanilla renderer");
            Renderer.forceVanillaRenderer();
            Renderer.get().init();
         }

      }
   }

   private void ensureBlitResources() {
      if (this.blitProgram == 0 || this.blitVao == 0 || this.blitVbo == 0) {
         String vsSrc = "#version 150 core\nin vec2 aPos;\nin vec2 aTex;\nout vec2 vTex;\nvoid main() {\n    vTex = aTex;\n    gl_Position = vec4(aPos, 0.0, 1.0);\n}\n";
         String fsSrc = "#version 150 core\nuniform sampler2D uTex;\nin vec2 vTex;\nout vec4 fragColor;\nvoid main() {\n    fragColor = texture(uTex, vTex);\n}\n";
         int vs = this.compileShader(35633, vsSrc);
         int fs = this.compileShader(35632, fsSrc);
         int program = GL20.glCreateProgram();
         GL20.glAttachShader(program, vs);
         GL20.glAttachShader(program, fs);
         GL20.glBindAttribLocation(program, 0, "aPos");
         GL20.glBindAttribLocation(program, 1, "aTex");
         GL20.glLinkProgram(program);
         int linked = GL20.glGetProgrami(program, 35714);
         if (linked == 0) {
            String log = GL20.glGetProgramInfoLog(program, 4096);
            GL20.glDeleteProgram(program);
            GL20.glDeleteShader(vs);
            GL20.glDeleteShader(fs);
            throw new IllegalStateException("NanoVG blit shader link failed: " + log);
         } else {
            GL20.glDeleteShader(vs);
            GL20.glDeleteShader(fs);
            this.blitProgram = program;
            this.blitUniformTex = GL20.glGetUniformLocation(this.blitProgram, "uTex");
            float[] vertices = new float[]{-1.0F, -1.0F, 0.0F, 0.0F, 1.0F, -1.0F, 1.0F, 0.0F, -1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F};
            this.blitVao = GL30.glGenVertexArrays();
            this.blitVbo = GL15.glGenBuffers();
            GL30.glBindVertexArray(this.blitVao);
            GL15.glBindBuffer(34962, this.blitVbo);
            FloatBuffer buf = BufferUtils.createFloatBuffer(vertices.length);
            buf.put(vertices).flip();
            GL15.glBufferData(34962, buf, 35044);
            int stride = 16;
            GL20.glEnableVertexAttribArray(0);
            GL20.glVertexAttribPointer(0, 2, 5126, false, stride, 0L);
            GL20.glEnableVertexAttribArray(1);
            GL20.glVertexAttribPointer(1, 2, 5126, false, stride, 8L);
            GL15.glBindBuffer(34962, 0);
            GL30.glBindVertexArray(0);
         }
      }
   }

   private int compileShader(int type, String source) {
      int shader = GL20.glCreateShader(type);
      GL20.glShaderSource(shader, source);
      GL20.glCompileShader(shader);
      int compiled = GL20.glGetShaderi(shader, 35713);
      if (compiled == 0) {
         String log = GL20.glGetShaderInfoLog(shader, 4096);
         GL20.glDeleteShader(shader);
         throw new IllegalStateException("NanoVG blit shader compile failed: " + log);
      } else {
         return shader;
      }
   }

   private void ensureOffscreenFbo(int widthPx, int heightPx) {
      if (widthPx > 0 && heightPx > 0) {
         if (this.offscreenFbo == 0 || this.offscreenWidthPx != widthPx || this.offscreenHeightPx != heightPx) {
            this.deleteOffscreenFbo();
            int prevTex = 0;
            int prevFbo = 0;
            int prevRbo = 0;

            try {
               prevTex = GL11.glGetInteger(32873);
               prevFbo = GL11.glGetInteger(36006);
               prevRbo = GL11.glGetInteger(36007);
            } catch (Throwable var20) {
            }

            try {
               this.offscreenWidthPx = widthPx;
               this.offscreenHeightPx = heightPx;
               this.offscreenColorTex = GL11.glGenTextures();
               GL11.glBindTexture(3553, this.offscreenColorTex);
               GL11.glTexParameteri(3553, 10241, 9729);
               GL11.glTexParameteri(3553, 10240, 9729);
               GL11.glTexParameteri(3553, 10242, 33071);
               GL11.glTexParameteri(3553, 10243, 33071);
               GL11.glTexImage2D(3553, 0, 32856, widthPx, heightPx, 0, 6408, 5121, (ByteBuffer)null);
               this.offscreenStencilRbo = GL30.glGenRenderbuffers();
               GL30.glBindRenderbuffer(36161, this.offscreenStencilRbo);
               GL30.glRenderbufferStorage(36161, 35056, widthPx, heightPx);
               this.offscreenFbo = GL30.glGenFramebuffers();
               GL30.glBindFramebuffer(36160, this.offscreenFbo);
               GL30.glFramebufferTexture2D(36160, 36064, 3553, this.offscreenColorTex, 0);
               GL30.glFramebufferRenderbuffer(36160, 33306, 36161, this.offscreenStencilRbo);
               int status = GL30.glCheckFramebufferStatus(36160);
               if (status != 36053) {
                  throw new IllegalStateException("Offscreen FBO incomplete: 0x" + Integer.toHexString(status));
               }
            } finally {
               try {
                  GL30.glBindRenderbuffer(36161, prevRbo);
               } catch (Throwable var19) {
               }

               try {
                  GL30.glBindFramebuffer(36160, prevFbo);
               } catch (Throwable var18) {
               }

               try {
                  GL11.glBindTexture(3553, prevTex);
               } catch (Throwable var17) {
               }

            }

         }
      } else {
         throw new IllegalArgumentException("Invalid offscreen size: " + widthPx + "x" + heightPx);
      }
   }

   private void deleteOffscreenFbo() {
      if (this.offscreenFbo != 0) {
         try {
            GL30.glDeleteFramebuffers(this.offscreenFbo);
         } catch (Throwable var4) {
         }

         this.offscreenFbo = 0;
      }

      if (this.offscreenStencilRbo != 0) {
         try {
            GL30.glDeleteRenderbuffers(this.offscreenStencilRbo);
         } catch (Throwable var3) {
         }

         this.offscreenStencilRbo = 0;
      }

      if (this.offscreenColorTex != 0) {
         try {
            GL11.glDeleteTextures(this.offscreenColorTex);
         } catch (Throwable var2) {
         }

         this.offscreenColorTex = 0;
      }

      this.offscreenWidthPx = -1;
      this.offscreenHeightPx = -1;
   }

   private void compositeOffscreenToGameFramebuffer() {
      if (this.usingOffscreenFbo) {
         if (this.offscreenColorTex != 0 && this.blitProgram != 0 && this.blitVao != 0) {
            try {
               GL30.glBindFramebuffer(36009, this.offscreenPrevFbo);
               if (this.savedReadFbo >= 0) {
                  GL30.glBindFramebuffer(36008, this.savedReadFbo);
               }
            } catch (Throwable var3) {
            }

            GL11.glViewport(this.savedViewport[0], this.savedViewport[1], this.savedViewport[2], this.savedViewport[3]);
            GL11.glDisable(2929);
            GL11.glDisable(2884);
            GL11.glDisable(3089);
            GL11.glDisable(2960);
            GL11.glEnable(3042);
            GL15.glBlendEquation(32774);
            GL15.glBlendFuncSeparate(770, 771, 1, 771);
            GL13.glActiveTexture(33984);
            GL11.glBindTexture(3553, this.offscreenColorTex);
            GL20.glUseProgram(this.blitProgram);
            int uTexLoc = GL20.glGetUniformLocation(this.blitProgram, "uTex");
            if (uTexLoc >= 0) {
               GL20.glUniform1i(uTexLoc, 0);
            }

            if (this.blitVao == 0) {
               LOGGER.warn("Blit VAO is 0, skipping composite!");
            } else {
               GL30.glBindVertexArray(this.blitVao);
               GL11.glDrawArrays(5, 0, 4);
               int error = GL11.glGetError();
               if (error != 0) {
                  LOGGER.error("GL Error during offscreen composite: " + error);
               }

               GL30.glBindVertexArray(0);
               GL20.glUseProgram(0);
               GL11.glBindTexture(3553, 0);
            }
         }
      }
   }

   private void loadFonts() {
      LOGGER.info("Loading NanoVG fonts...");

      try {
         ByteBuffer fontData = this.loadResource("/assets/dynamic_fps/font/font.ttf");
         if (fontData != null) {
            this.regularFont = NanoVG.nvgCreateFontMem(this.nvg, "regular", fontData, false);
         }
      } catch (Exception var2) {
      }

   }

   private ByteBuffer loadResource(String path) {
      try {
         InputStream is = NanoVGRendererImpl.class.getResourceAsStream(path);
         if (is == null) {
            return null;
         } else {
            ReadableByteChannel rbc = Channels.newChannel(is);
            ByteBuffer buffer = MemoryUtil.memAlloc(8192);

            while(rbc.read(buffer) != -1) {
               if (buffer.remaining() == 0) {
                  ByteBuffer newBuffer = MemoryUtil.memAlloc(buffer.capacity() * 2);
                  buffer.flip();
                  newBuffer.put(buffer);
                  MemoryUtil.memFree(buffer);
                  buffer = newBuffer;
               }
            }

            buffer.flip();
            return buffer;
         }
      } catch (Exception var6) {
         return null;
      }
   }

   private void ensureStencilAttachment(int width, int height) {
      if (width > 0 && height > 0) {
         int drawFbo;
         int prevDrawFbo;
         int prevReadFbo;
         int prevRbo;
         try {
            prevDrawFbo = GL11.glGetInteger(36006);
            prevReadFbo = GL11.glGetInteger(36010);
            drawFbo = prevDrawFbo;
            prevRbo = GL11.glGetInteger(36007);
         } catch (Throwable var26) {
            return;
         }

         if (drawFbo != 0) {
            int stencilBits = 0;

            try {
               stencilBits = GL11.glGetInteger(3415);
            } catch (Throwable var25) {
            }

            if (stencilBits <= 0) {
               try {
                  if (this.stencilRbo != 0 && this.stencilRboWidth == width && this.stencilRboHeight == height && this.stencilRboFbo == drawFbo) {
                     GL30.glBindRenderbuffer(36161, this.stencilRbo);
                  } else {
                     if (this.stencilRbo != 0) {
                        try {
                           GL30.glDeleteRenderbuffers(this.stencilRbo);
                        } catch (Throwable var24) {
                        }
                     }

                     this.stencilRbo = GL30.glGenRenderbuffers();
                     this.stencilRboWidth = width;
                     this.stencilRboHeight = height;
                     this.stencilRboFbo = drawFbo;
                     GL30.glBindRenderbuffer(36161, this.stencilRbo);
                     GL30.glRenderbufferStorage(36161, 36168, width, height);
                  }

                  GL30.glBindFramebuffer(36160, drawFbo);
                  GL30.glFramebufferRenderbuffer(36160, 36128, 36161, this.stencilRbo);
               } catch (Throwable var27) {
               } finally {
                  try {
                     GL30.glBindRenderbuffer(36161, prevRbo);
                  } catch (Throwable var23) {
                  }

                  try {
                     GL30.glBindFramebuffer(36009, prevDrawFbo);
                     GL30.glBindFramebuffer(36008, prevReadFbo);
                  } catch (Throwable var22) {
                  }

               }

            }
         }
      }
   }

   public boolean beginFrame(float width, float height, float pixelRatio) {
      RenderSystem.assertOnRenderThread();
      if (!this.initialized) {
         this.init();
      }

      if (!this.initialized) {
         return Renderer.get().beginFrame(width, height, pixelRatio);
      } else {
         try {
            if (Faith.getInstance() != null && Faith.getInstance().getModuleManager() != null && Faith.getInstance().getModuleManager().shouldBlockNanoVG()) {
               this.frameActive = false;
               return false;
            }
         } catch (Exception var12) {
         }

         try {
            GL11.glGetIntegerv(2978, this.savedViewport);
            GL11.glGetIntegerv(3088, this.savedScissorBox);
         } catch (Throwable var11) {
         }

         try {
            this.savedDrawFbo = GL11.glGetInteger(36006);
            this.savedReadFbo = GL11.glGetInteger(36010);
         } catch (Throwable var10) {
            this.savedDrawFbo = -1;
            this.savedReadFbo = -1;
         }

         this.savedDepthTest = GL11.glIsEnabled(2929);
         this.savedCullFace = GL11.glIsEnabled(2884);
         this.savedBlend = GL11.glIsEnabled(3042);
         this.savedScissorTest = GL11.glIsEnabled(3089);
         this.savedStencilTest = GL11.glIsEnabled(2960);
         this.savedBlendSrcRgb = GL11.glGetInteger(32969);
         this.savedBlendDstRgb = GL11.glGetInteger(32968);
         this.savedBlendSrcAlpha = GL11.glGetInteger(32971);
         this.savedBlendDstAlpha = GL11.glGetInteger(32970);
         this.savedBlendEquation = GL11.glGetInteger(32777);
         this.savedActiveTexture = GL11.glGetInteger(34016);
         this.savedTextureBinding = GL11.glGetInteger(32873);
         this.savedProgram = GL11.glGetInteger(35725);
         this.savedArrayBuffer = GL11.glGetInteger(34964);
         this.savedElementArrayBuffer = GL11.glGetInteger(34965);
         this.savedVertexArray = GL11.glGetInteger(34229);
         this.usingOffscreenFbo = false;
         this.offscreenPrevFbo = 0;
         int stencilBits = 0;

         try {
            stencilBits = GL11.glGetInteger(3415);
         } catch (Throwable var9) {
         }

         if (stencilBits <= 0) {
            try {
               this.ensureStencilAttachment(this.savedViewport[2], this.savedViewport[3]);
            } catch (Throwable var8) {
            }

            try {
               stencilBits = GL11.glGetInteger(3415);
            } catch (Throwable var7) {
            }
         }

         if (stencilBits <= 0) {
            try {
               this.ensureBlitResources();
               this.usingOffscreenFbo = true;
               this.offscreenPrevFbo = this.savedDrawFbo;
               this.ensureOffscreenFbo((int)width, (int)height);
               GL30.glBindFramebuffer(36160, this.offscreenFbo);
               GL11.glViewport(0, 0, (int)width, (int)height);
               GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
               GL11.glClear(17664);
            } catch (Exception var13) {
               if (LOGGER != null) {
                  LOGGER.warn("Failed to create offscreen FBO, falling back to Vanilla: " + var13.getMessage());
               }

               Renderer.forceVanillaRenderer();
               return Renderer.get().beginFrame(width, height, pixelRatio);
            }
         }

         try {
            GL11.glDisable(3089);
            GL11.glDisable(2960);
            GL11.glColorMask(true, true, true, true);
            GL15.glBlendEquation(32774);
            GL11.glEnable(3042);
            GL15.glBlendFuncSeparate(770, 771, 1, 771);
            GL11.glDisable(2884);
            GL11.glDisable(2929);
            NanoVG.nvgBeginFrame(this.nvg, width, height, pixelRatio);
         } catch (Exception var6) {
            var6.printStackTrace();
            Renderer.forceVanillaRenderer();
            return Renderer.get().beginFrame(width, height, pixelRatio);
         }

         this.frameActive = true;
         return true;
      }
   }

   public void endFrame() {
      if (this.frameActive) {
         NanoVG.nvgEndFrame(this.nvg);
         this.frameActive = false;
         RenderSystem.assertOnRenderThread();
         GL15.glStencilMask(255);
         GL15.glClear(1024);
         GL15.glStencilFunc(519, 0, 255);
         GL15.glStencilOp(7680, 7680, 7680);
         GL15.glPixelStorei(3317, 4);
         GL15.glPixelStorei(3314, 0);
         GL15.glPixelStorei(3316, 0);
         GL15.glPixelStorei(3315, 0);

         try {
            this.compositeOffscreenToGameFramebuffer();
         } catch (Throwable var7) {
            var7.printStackTrace();
         } finally {
            this.usingOffscreenFbo = false;
            this.offscreenPrevFbo = 0;
         }

         try {
            if (this.savedDrawFbo >= 0) {
               GL30.glBindFramebuffer(36009, this.savedDrawFbo);
            }

            if (this.savedReadFbo >= 0) {
               GL30.glBindFramebuffer(36008, this.savedReadFbo);
            }
         } catch (Throwable var6) {
         }

         GL13.glActiveTexture(this.savedActiveTexture);
         GL15.glBindTexture(3553, this.savedTextureBinding);
         GL30.glBindVertexArray(this.savedVertexArray);
         GL15.glBindBuffer(34962, this.savedArrayBuffer);
         GL15.glBindBuffer(34963, this.savedElementArrayBuffer);
         GL20.glUseProgram(this.savedProgram);
         GL11.glViewport(this.savedViewport[0], this.savedViewport[1], this.savedViewport[2], this.savedViewport[3]);
         if (this.savedScissorTest) {
            GL11.glEnable(3089);
            GL11.glScissor(this.savedScissorBox[0], this.savedScissorBox[1], this.savedScissorBox[2], this.savedScissorBox[3]);
         } else {
            GL11.glDisable(3089);
         }

         if (this.savedBlend) {
            GL11.glEnable(3042);
         } else {
            GL11.glDisable(3042);
         }

         GL15.glBlendFuncSeparate(this.savedBlendSrcRgb, this.savedBlendDstRgb, this.savedBlendSrcAlpha, this.savedBlendDstAlpha);
         GL15.glBlendEquation(this.savedBlendEquation);
         if (this.savedDepthTest) {
            GL11.glEnable(2929);
         } else {
            GL11.glDisable(2929);
         }

         GL15.glDepthMask(true);
         if (this.savedCullFace) {
            GL11.glEnable(2884);
         } else {
            GL11.glDisable(2884);
         }

         GL15.glColorMask(true, true, true, true);
         if (this.savedStencilTest) {
            GL11.glEnable(2960);
         } else {
            GL11.glDisable(2960);
         }

      }
   }

   public void drawGlowingRect(class_332 context, float x, float y, float width, float height, float radius, Color innerColor, Color outerColor, float glowSize) {
      if (this.frameActive) {
         MemoryStack stack = MemoryStack.stackPush();

         try {
            NVGColor inner = NVGColor.mallocStack(stack);
            NVGColor outer = NVGColor.mallocStack(stack);
            NanoVG.nvgRGBA((byte)innerColor.getRed(), (byte)innerColor.getGreen(), (byte)innerColor.getBlue(), (byte)innerColor.getAlpha(), inner);
            NanoVG.nvgRGBA((byte)outerColor.getRed(), (byte)outerColor.getGreen(), (byte)outerColor.getBlue(), (byte)outerColor.getAlpha(), outer);
            float centerX = x + width / 2.0F;
            float centerY = y + height / 2.0F;
            NVGPaint paint = NVGPaint.mallocStack(stack);
            NanoVG.nvgRadialGradient(this.nvg, centerX, centerY, 0.0F, glowSize, inner, outer, paint);
            NanoVG.nvgBeginPath(this.nvg);
            NanoVG.nvgRoundedRect(this.nvg, x - glowSize, y - glowSize, width + glowSize * 2.0F, height + glowSize * 2.0F, radius + glowSize);
            NanoVG.nvgFillPaint(this.nvg, paint);
            NanoVG.nvgFill(this.nvg);
         } catch (Throwable var17) {
            if (stack != null) {
               try {
                  stack.close();
               } catch (Throwable var16) {
                  var17.addSuppressed(var16);
               }
            }

            throw var17;
         }

         if (stack != null) {
            stack.close();
         }

      }
   }

   public void drawTexture(class_332 context, int nvgImageId, float x, float y, float width, float height, float u, float v, float regionWidth, float regionHeight, float textureWidth, float textureHeight) {
      if (this.frameActive && nvgImageId > 0) {
         MemoryStack stack = MemoryStack.stackPush();

         try {
            float u1 = u / textureWidth;
            float v1 = v / textureHeight;
            float u2 = (u + regionWidth) / textureWidth;
            float v2 = (v + regionHeight) / textureHeight;
            NVGPaint paint = NVGPaint.mallocStack(stack);
            float scaleX = width / (u2 - u1);
            float scaleY = height / (v2 - v1);
            float offsetX = -u1 * scaleX;
            float offsetY = -v1 * scaleY;
            NanoVG.nvgImagePattern(this.nvg, x + offsetX, y + offsetY, scaleX, scaleY, 0.0F, nvgImageId, 1.0F, paint);
            NanoVG.nvgBeginPath(this.nvg);
            NanoVG.nvgRect(this.nvg, x, y, width, height);
            NanoVG.nvgFillPaint(this.nvg, paint);
            NanoVG.nvgFill(this.nvg);
         } catch (Throwable var24) {
            if (stack != null) {
               try {
                  stack.close();
               } catch (Throwable var23) {
                  var24.addSuppressed(var23);
               }
            }

            throw var24;
         }

         if (stack != null) {
            stack.close();
         }

      }
   }

   public void drawRoundedRect(class_332 context, float x, float y, float width, float height, float radius, Color color) {
      if (this.frameActive) {
         MemoryStack stack = MemoryStack.stackPush();

         try {
            NVGColor nvgColor = NVGColor.mallocStack(stack);
            NanoVG.nvgRGBA((byte)color.getRed(), (byte)color.getGreen(), (byte)color.getBlue(), (byte)color.getAlpha(), nvgColor);
            NanoVG.nvgBeginPath(this.nvg);
            NanoVG.nvgRoundedRect(this.nvg, x, y, width, height, radius);
            NanoVG.nvgFillColor(this.nvg, nvgColor);
            NanoVG.nvgFill(this.nvg);
         } catch (Throwable var12) {
            if (stack != null) {
               try {
                  stack.close();
               } catch (Throwable var11) {
                  var12.addSuppressed(var11);
               }
            }

            throw var12;
         }

         if (stack != null) {
            stack.close();
         }

      }
   }

   public void drawRect(class_332 context, float x, float y, float width, float height, Color color) {
      this.drawRoundedRect(context, x, y, width, height, 0.0F, color);
   }

   public void drawRectOutline(class_332 context, float x, float y, float width, float height, float strokeWidth, Color color) {
      if (this.frameActive) {
         MemoryStack stack = MemoryStack.stackPush();

         try {
            NVGColor nvgColor = NVGColor.mallocStack(stack);
            NanoVG.nvgRGBA((byte)color.getRed(), (byte)color.getGreen(), (byte)color.getBlue(), (byte)color.getAlpha(), nvgColor);
            NanoVG.nvgBeginPath(this.nvg);
            NanoVG.nvgRect(this.nvg, x, y, width, height);
            NanoVG.nvgStrokeColor(this.nvg, nvgColor);
            NanoVG.nvgStrokeWidth(this.nvg, strokeWidth);
            NanoVG.nvgStroke(this.nvg);
         } catch (Throwable var12) {
            if (stack != null) {
               try {
                  stack.close();
               } catch (Throwable var11) {
                  var12.addSuppressed(var11);
               }
            }

            throw var12;
         }

         if (stack != null) {
            stack.close();
         }

      }
   }

   public void drawRoundedRectGradient(class_332 context, float x, float y, float width, float height, float radius, Color color1, Color color2, boolean vertical) {
      if (this.frameActive) {
         MemoryStack stack = MemoryStack.stackPush();

         try {
            NVGColor c1 = NVGColor.mallocStack(stack);
            NVGColor c2 = NVGColor.mallocStack(stack);
            NanoVG.nvgRGBA((byte)color1.getRed(), (byte)color1.getGreen(), (byte)color1.getBlue(), (byte)color1.getAlpha(), c1);
            NanoVG.nvgRGBA((byte)color2.getRed(), (byte)color2.getGreen(), (byte)color2.getBlue(), (byte)color2.getAlpha(), c2);
            NVGPaint paint = NVGPaint.mallocStack(stack);
            if (vertical) {
               NanoVG.nvgLinearGradient(this.nvg, x, y, x, y + height, c1, c2, paint);
            } else {
               NanoVG.nvgLinearGradient(this.nvg, x, y, x + width, y, c1, c2, paint);
            }

            NanoVG.nvgBeginPath(this.nvg);
            NanoVG.nvgRoundedRect(this.nvg, x, y, width, height, radius);
            NanoVG.nvgFillPaint(this.nvg, paint);
            NanoVG.nvgFill(this.nvg);
         } catch (Throwable var15) {
            if (stack != null) {
               try {
                  stack.close();
               } catch (Throwable var14) {
                  var15.addSuppressed(var14);
               }
            }

            throw var15;
         }

         if (stack != null) {
            stack.close();
         }

      }
   }

   public void drawCircle(class_332 context, float x, float y, float radius, Color color) {
      if (this.frameActive) {
         MemoryStack stack = MemoryStack.stackPush();

         try {
            NVGColor nvgColor = NVGColor.mallocStack(stack);
            NanoVG.nvgRGBA((byte)color.getRed(), (byte)color.getGreen(), (byte)color.getBlue(), (byte)color.getAlpha(), nvgColor);
            NanoVG.nvgBeginPath(this.nvg);
            NanoVG.nvgCircle(this.nvg, x, y, radius);
            NanoVG.nvgFillColor(this.nvg, nvgColor);
            NanoVG.nvgFill(this.nvg);
         } catch (Throwable var10) {
            if (stack != null) {
               try {
                  stack.close();
               } catch (Throwable var9) {
                  var10.addSuppressed(var9);
               }
            }

            throw var10;
         }

         if (stack != null) {
            stack.close();
         }

      }
   }

   public void drawArc(class_332 context, float cx, float cy, float radius, float startAngle, float sweepAngle, float strokeWidth, Color color) {
      if (this.frameActive) {
         MemoryStack stack = MemoryStack.stackPush();

         try {
            NVGColor nvgColor = NVGColor.mallocStack(stack);
            NanoVG.nvgRGBA((byte)color.getRed(), (byte)color.getGreen(), (byte)color.getBlue(), (byte)color.getAlpha(), nvgColor);
            float startRad = (float)Math.toRadians((double)(startAngle - 90.0F));
            float endRad = (float)Math.toRadians((double)(startAngle + sweepAngle - 90.0F));
            NanoVG.nvgBeginPath(this.nvg);
            NanoVG.nvgArc(this.nvg, cx, cy, radius, startRad, endRad, 2);
            NanoVG.nvgStrokeColor(this.nvg, nvgColor);
            NanoVG.nvgStrokeWidth(this.nvg, strokeWidth);
            NanoVG.nvgStroke(this.nvg);
         } catch (Throwable var14) {
            if (stack != null) {
               try {
                  stack.close();
               } catch (Throwable var13) {
                  var14.addSuppressed(var13);
               }
            }

            throw var14;
         }

         if (stack != null) {
            stack.close();
         }

      }
   }

   public void drawBlur(class_332 context, float x, float y, float width, float height, float blurRadius) {
   }

   public void drawRoundedRectOutline(class_332 context, float x, float y, float width, float height, float radius, float strokeWidth, Color color) {
      if (this.frameActive) {
         MemoryStack stack = MemoryStack.stackPush();

         try {
            NVGColor nvgColor = NVGColor.mallocStack(stack);
            NanoVG.nvgRGBA((byte)color.getRed(), (byte)color.getGreen(), (byte)color.getBlue(), (byte)color.getAlpha(), nvgColor);
            NanoVG.nvgBeginPath(this.nvg);
            NanoVG.nvgRoundedRect(this.nvg, x, y, width, height, radius);
            NanoVG.nvgStrokeColor(this.nvg, nvgColor);
            NanoVG.nvgStrokeWidth(this.nvg, strokeWidth);
            NanoVG.nvgStroke(this.nvg);
         } catch (Throwable var13) {
            if (stack != null) {
               try {
                  stack.close();
               } catch (Throwable var12) {
                  var13.addSuppressed(var12);
               }
            }

            throw var13;
         }

         if (stack != null) {
            stack.close();
         }

      }
   }

   private void renderObfuscatedString(float x, float y, String text, boolean shadow, NVGColor color, NVGColor shadowColor) {
      float cursorX = x;
      float[] bounds = new float[4];

      for(int i = 0; i < text.length(); ++i) {
         char c = text.charAt(i);
         if (!this.isSalt(c)) {
            String charStr = String.valueOf(c);
            if (shadow && shadowColor != null) {
               NanoVG.nvgFillColor(this.nvg, shadowColor);
               NanoVG.nvgText(this.nvg, cursorX + 1.0F, y + 1.0F, charStr);
            }

            NanoVG.nvgFillColor(this.nvg, color);
            NanoVG.nvgText(this.nvg, cursorX, y, charStr);
            float advance = NanoVG.nvgTextBounds(this.nvg, 0.0F, 0.0F, charStr, bounds);
            cursorX += advance;
         }
      }

   }

   public void drawText(class_332 context, String text, float x, float y, float fontSize, Color color, boolean shadow) {
      if (this.frameActive) {
         if (this.regularFont != -1) {
            NanoVG.nvgFontFaceId(this.nvg, this.regularFont);
         }

         NanoVG.nvgFontSize(this.nvg, fontSize);
         NanoVG.nvgTextAlign(this.nvg, 9);
         MemoryStack stack = MemoryStack.stackPush();

         try {
            NVGColor nvgColor;
            if (shadow) {
               nvgColor = NVGColor.mallocStack(stack);
               NanoVG.nvgRGBA((byte)0, (byte)0, (byte)0, (byte)-106, nvgColor);
               NanoVG.nvgFillColor(this.nvg, nvgColor);
               NanoVG.nvgText(this.nvg, x + 1.0F, y + 1.0F, text);
            }

            nvgColor = NVGColor.mallocStack(stack);
            NanoVG.nvgRGBA((byte)color.getRed(), (byte)color.getGreen(), (byte)color.getBlue(), (byte)color.getAlpha(), nvgColor);
            NanoVG.nvgFillColor(this.nvg, nvgColor);
            NanoVG.nvgText(this.nvg, x, y, text);
         } catch (Throwable var12) {
            if (stack != null) {
               try {
                  stack.close();
               } catch (Throwable var11) {
                  var12.addSuppressed(var11);
               }
            }

            throw var12;
         }

         if (stack != null) {
            stack.close();
         }

      }
   }

   public void drawCenteredText(class_332 context, String text, float x, float y, float fontSize, Color color, boolean shadow) {
      if (this.frameActive) {
         if (this.regularFont != -1) {
            NanoVG.nvgFontFaceId(this.nvg, this.regularFont);
         }

         NanoVG.nvgFontSize(this.nvg, fontSize);
         NanoVG.nvgTextAlign(this.nvg, 17);
         float textWidth = this.getTextWidth(text, fontSize);
         float startX = x - textWidth / 2.0F;
         MemoryStack stack = MemoryStack.stackPush();

         try {
            NVGColor nvgColor = NVGColor.mallocStack(stack);
            NanoVG.nvgRGBA((byte)color.getRed(), (byte)color.getGreen(), (byte)color.getBlue(), (byte)color.getAlpha(), nvgColor);
            NVGColor shadowColor = null;
            if (shadow) {
               shadowColor = NVGColor.mallocStack(stack);
               NanoVG.nvgRGBA((byte)0, (byte)0, (byte)0, (byte)-106, shadowColor);
            }

            this.renderObfuscatedString(startX, y, text, shadow, nvgColor, shadowColor);
         } catch (Throwable var14) {
            if (stack != null) {
               try {
                  stack.close();
               } catch (Throwable var13) {
                  var14.addSuppressed(var13);
               }
            }

            throw var14;
         }

         if (stack != null) {
            stack.close();
         }

      }
   }

   public float getTextWidth(String text, float fontSize) {
      if (!this.initialized) {
         this.init();
      }

      if (this.initialized && this.nvg != 0L) {
         if (text != null && !text.isEmpty()) {
            if (this.regularFont != -1) {
               NanoVG.nvgFontFaceId(this.nvg, this.regularFont);
            }

            NanoVG.nvgFontSize(this.nvg, fontSize);
            float totalWidth = 0.0F;
            float[] bounds = new float[4];

            for(int i = 0; i < text.length(); ++i) {
               char c = text.charAt(i);
               if (!this.isSalt(c)) {
                  String charStr = String.valueOf(c);
                  totalWidth += NanoVG.nvgTextBounds(this.nvg, 0.0F, 0.0F, charStr, bounds);
               }
            }

            return totalWidth;
         } else {
            return 0.0F;
         }
      } else {
         return 0.0F;
      }
   }

   public float getTextHeight(float fontSize) {
      return fontSize;
   }

   public void scissor(class_332 context, float x, float y, float width, float height) {
      if (this.frameActive) {
         NanoVG.nvgScissor(this.nvg, x, y, width, height);
      }

   }

   public void setFontBlur(float blur) {
      if (this.frameActive) {
         NanoVG.nvgFontBlur(this.nvg, blur);
      }

   }

   public void scissor(float x, float y, float width, float height) {
      this.scissor((class_332)null, x, y, width, height);
   }

   public int createImageFromTexture(int textureId, int width, int height) {
      if (!this.initialized) {
         this.init();
      }

      if (!this.initialized) {
         return -1;
      } else {
         int flags = 7;
         return NanoVGGL3.nvglCreateImageFromHandle(this.nvg, textureId, width, height, flags);
      }
   }

   public void deleteImage(int imageId) {
      if (this.initialized && imageId > 0) {
         NanoVG.nvgDeleteImage(this.nvg, imageId);
      }

   }

   public void drawTextureRounded(class_332 context, int nvgImageId, float x, float y, float width, float height, float u, float v, float rW, float rH, float tW, float tH, float radius) {
      if (this.frameActive && nvgImageId > 0) {
         try {
            MemoryStack stack = MemoryStack.stackPush();

            try {
               float u1 = u / tW;
               float v1 = v / tH;
               float u2 = (u + rW) / tW;
               float v2 = (v + rH) / tH;
               NVGPaint paint = NVGPaint.mallocStack(stack);
               float scaleX = width / (u2 - u1);
               float scaleY = height / (v2 - v1);
               float offsetX = -u1 * scaleX;
               float offsetY = -v1 * scaleY;
               NanoVG.nvgImagePattern(this.nvg, x + offsetX, y + offsetY, scaleX, scaleY, 0.0F, nvgImageId, 1.0F, paint);
               NanoVG.nvgBeginPath(this.nvg);
               NanoVG.nvgRoundedRect(this.nvg, x, y, width, height, radius);
               NanoVG.nvgFillPaint(this.nvg, paint);
               NanoVG.nvgFill(this.nvg);
            } catch (Throwable var25) {
               if (stack != null) {
                  try {
                     stack.close();
                  } catch (Throwable var24) {
                     var25.addSuppressed(var24);
                  }
               }

               throw var25;
            }

            if (stack != null) {
               stack.close();
            }
         } catch (Exception var26) {
            LOGGER.warn("NanoVG drawSubImage failed", var26);
         }

      }
   }

   public int createImageFromFile(String resourcePath) {
      if (!this.initialized) {
         this.init();
      }

      if (!this.initialized) {
         return -1;
      } else {
         try {
            InputStream is = NanoVGRendererImpl.class.getResourceAsStream(resourcePath);
            if (is == null) {
               return -1;
            } else {
               int flags = 1;
               byte[] buffer = is.readAllBytes();
               ByteBuffer imageBuffer = MemoryUtil.memAlloc(buffer.length);
               imageBuffer.put(buffer);
               imageBuffer.flip();
               int imageId = NanoVG.nvgCreateImageMem(this.nvg, flags, imageBuffer);
               is.close();
               return imageId;
            }
         } catch (Exception var7) {
            var7.printStackTrace();
            return -1;
         }
      }
   }

   public void resetScissor() {
      if (this.frameActive) {
         NanoVG.nvgResetScissor(this.nvg);
      }

   }

   public void emergencyCleanup() {
      if (this.frameActive) {
         try {
            NanoVG.nvgEndFrame(this.nvg);
         } catch (Exception var2) {
            LOGGER.warn("NanoVG emergencyCleanup nvgEndFrame failed", var2);
         }

         this.frameActive = false;
      }

      GL11.glEnable(2929);
      GL11.glEnable(2884);
      GL11.glEnable(3042);
      GL15.glBlendFuncSeparate(770, 771, 1, 771);
      GL13.glActiveTexture(33984);
      GL15.glBindTexture(3553, 0);
   }

   public void save() {
      if (this.frameActive) {
         NanoVG.nvgSave(this.nvg);
      }

   }

   public void restore() {
      if (this.frameActive) {
         NanoVG.nvgRestore(this.nvg);
      }

   }

   public void translate(float x, float y) {
      if (this.frameActive) {
         NanoVG.nvgTranslate(this.nvg, x, y);
      }

   }

   public void rotate(float angle) {
      if (this.frameActive) {
         NanoVG.nvgRotate(this.nvg, angle);
      }

   }

   public void scale(float x, float y) {
      if (this.frameActive) {
         NanoVG.nvgScale(this.nvg, x, y);
      }

   }

   public void cleanup() {
      if (this.stencilRbo != 0) {
         try {
            GL30.glDeleteRenderbuffers(this.stencilRbo);
         } catch (Throwable var5) {
         }

         this.stencilRbo = 0;
         this.stencilRboWidth = -1;
         this.stencilRboHeight = -1;
         this.stencilRboFbo = -1;
      }

      this.deleteOffscreenFbo();
      this.usingOffscreenFbo = false;
      this.offscreenPrevFbo = 0;
      if (this.blitVbo != 0) {
         try {
            GL15.glDeleteBuffers(this.blitVbo);
         } catch (Throwable var4) {
         }

         this.blitVbo = 0;
      }

      if (this.blitVao != 0) {
         try {
            GL30.glDeleteVertexArrays(this.blitVao);
         } catch (Throwable var3) {
         }

         this.blitVao = 0;
      }

      if (this.blitProgram != 0) {
         try {
            GL20.glDeleteProgram(this.blitProgram);
         } catch (Throwable var2) {
         }

         this.blitProgram = 0;
         this.blitUniformTex = -1;
      }

      if (this.blurFramebuffer != null) {
         this.blurFramebuffer.method_1238();
         this.blurFramebuffer = null;
      }

      if (this.initialized) {
         NanoVGGL3.nvgDelete(this.nvg);
         this.initialized = false;
      }

   }

   public long getContext() {
      return this.nvg;
   }

   public boolean isInitialized() {
      return this.initialized;
   }
}
