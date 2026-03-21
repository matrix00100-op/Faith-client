package com.slither.cyemer.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.slither.cyemer.module.implementation.ClickGUIModule;
import com.slither.cyemer.module.implementation.CustomFont;
import com.slither.cyemer.rendering.font.MSDFFontRenderer;
import com.slither.cyemer.shader.CoreShaderManager;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_11231;
import net.minecraft.class_11241;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class VanillaRendererImpl implements IFaithRenderer {
   private final Matrix4f currentMatrix = new Matrix4f();
   private final Stack<Matrix4f> matrixStack = new Stack();
   private float windowHeight;
   private float globalPixelRatio;
   private class_332 activeContext;
   private final Map<Integer, class_2960> textureMap = new HashMap();
   private int nextTextureId = 1;

   public void init() {
   }

   public boolean beginFrame(float width, float height, float pixelRatio) {
      this.windowHeight = height;
      this.globalPixelRatio = pixelRatio;
      this.currentMatrix.identity();
      this.matrixStack.clear();
      this.activeContext = null;
      return true;
   }

   public void endFrame() {
      this.activeContext = null;
   }

   public void save() {
      this.matrixStack.push(new Matrix4f(this.currentMatrix));
   }

   public void restore() {
      if (!this.matrixStack.isEmpty()) {
         this.currentMatrix.set((Matrix4fc)this.matrixStack.pop());
      }

   }

   public void translate(float x, float y) {
      this.currentMatrix.translate(x, y, 0.0F);
   }

   public void scale(float x, float y) {
      this.currentMatrix.scale(x, y, 1.0F);
   }

   public void cleanup() {
      this.matrixStack.clear();
      this.textureMap.clear();
   }

   private Vector4f transform(float x, float y, float w, float h) {
      Vector4f pos = new Vector4f(x, y, 0.0F, 1.0F);
      pos.mul(this.currentMatrix);
      return pos;
   }

   public void drawRect(class_332 context, float x, float y, float width, float height, Color color) {
      if (context != null) {
         GL11.glEnable(3042);
         GL11.glBlendFunc(770, 771);
         Vector4f p1 = (new Vector4f(x, y, 0.0F, 1.0F)).mul(this.currentMatrix);
         Vector4f p2 = (new Vector4f(x + width, y + height, 0.0F, 1.0F)).mul(this.currentMatrix);
         context.method_25294((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y, color.getRGB());
      }
   }

   public void drawRoundedRect(class_332 context, float x, float y, float width, float height, float radius, Color color) {
      if (context != null) {
         if (radius <= 0.5F) {
            this.drawRect(context, x, y, width, height, color);
         } else {
            RenderPipeline curvePipeline = CoreShaderManager.getInstance().getPipeline(class_2960.method_60655("dynamic_fps", "curve"));
            if (curvePipeline == null) {
               this.drawRect(context, x, y, width, height, color);
            } else {
               Vector4f p1 = (new Vector4f(x, y, 0.0F, 1.0F)).mul(this.currentMatrix);
               Vector4f p2 = (new Vector4f(x + width, y + height, 0.0F, 1.0F)).mul(this.currentMatrix);
               context.field_59826.method_70919(new class_11241(curvePipeline, class_11231.method_70899(), new Matrix3x2f(context.method_51448()), (int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y, 0.0F, 1.0F, 0.0F, 1.0F, color.getRGB(), context.field_44659.method_70863()));
            }
         }
      }
   }

   public void drawGlowingRect(class_332 context, float x, float y, float width, float height, float radius, Color innerColor, Color outerColor, float glowSize) {
      if (context != null) {
         for(int i = (int)glowSize; i >= 1; --i) {
            float expand = (float)i * 1.2F;
            int a = (int)((float)innerColor.getAlpha() * (1.0F - (float)i / glowSize) * 0.4F);
            Color layerColor = new Color(innerColor.getRed(), innerColor.getGreen(), innerColor.getBlue(), Math.max(0, Math.min(255, a)));
            this.drawRoundedRect(context, x - expand, y - expand, width + expand * 2.0F, height + expand * 2.0F, radius + expand, layerColor);
         }

      }
   }

   public void drawRectOutline(class_332 context, float x, float y, float width, float height, float strokeWidth, Color color) {
      if (context != null) {
         Vector4f p1 = (new Vector4f(x, y, 0.0F, 1.0F)).mul(this.currentMatrix);
         float sX = this.currentMatrix.m00();
         float sY = this.currentMatrix.m11();
         int x0 = (int)p1.x;
         int y0 = (int)p1.y;
         int w0 = (int)(width * sX);
         int h0 = (int)(height * sY);
         if (w0 > 0 && h0 > 0) {
            int x1 = x0 + w0 - 1;
            int y1 = y0 + h0 - 1;
            int rgb = color.getRGB();
            context.method_51738(x0, x1, y0, rgb);
            context.method_51738(x0, x1, y1, rgb);
            context.method_51742(x0, y0, y1, rgb);
            context.method_51742(x1, y0, y1, rgb);
         }
      }
   }

   public void drawRoundedRectOutline(class_332 context, float x, float y, float width, float height, float radius, float strokeWidth, Color color) {
      if (context != null) {
         if (radius <= 0.5F) {
            this.drawRectOutline(context, x, y, width, height, strokeWidth, color);
         } else {
            this.drawRoundedRect(context, x - strokeWidth, y - strokeWidth, width + strokeWidth * 2.0F, height + strokeWidth * 2.0F, radius + strokeWidth, color);
         }
      }
   }

   public void drawRoundedRectGradient(class_332 context, float x, float y, float width, float height, float radius, Color c1, Color c2, boolean vertical) {
      if (context != null && !(width <= 0.0F) && !(height <= 0.0F)) {
         if (c1.getRGB() == c2.getRGB()) {
            this.drawRoundedRect(context, x, y, width, height, radius, c1);
         } else if (radius <= 0.5F) {
            Vector4f p1 = (new Vector4f(x, y, 0.0F, 1.0F)).mul(this.currentMatrix);
            Vector4f p2 = (new Vector4f(x + width, y + height, 0.0F, 1.0F)).mul(this.currentMatrix);
            context.method_25296((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y, c1.getRGB(), c2.getRGB());
         } else {
            RenderPipeline curvePipeline = CoreShaderManager.getInstance().getPipeline(class_2960.method_60655("dynamic_fps", "curve"));
            boolean useRoundedPipeline = radius > 0.5F && curvePipeline != null;
            float scaledSpan = vertical ? Math.abs(height * this.currentMatrix.m11()) : Math.abs(width * this.currentMatrix.m00());
            float quality = Math.max(0.25F, Math.min(2.0F, ClickGUIModule.getGradientQualityFactor()));
            int slices = Math.max(2, Math.min(2048, (int)Math.ceil((double)(scaledSpan * Math.max(1.0F, this.globalPixelRatio) * quality))));
            Vector4f fullP1 = (new Vector4f(x, y, 0.0F, 1.0F)).mul(this.currentMatrix);
            Vector4f fullP2 = (new Vector4f(x + width, y + height, 0.0F, 1.0F)).mul(this.currentMatrix);
            int fullX0 = Math.round(Math.min(fullP1.x, fullP2.x));
            int fullY0 = Math.round(Math.min(fullP1.y, fullP2.y));
            int fullX1 = Math.round(Math.max(fullP1.x, fullP2.x));
            int fullY1 = Math.round(Math.max(fullP1.y, fullP2.y));

            for(int i = 0; i < slices; ++i) {
               float t0 = (float)i / (float)slices;
               float t1 = (float)(i + 1) / (float)slices;
               float mid = (t0 + t1) * 0.5F;
               Color sliceColor = this.interpolate(c1, c2, mid);
               float sliceX0 = vertical ? x : x + width * t0;
               float sliceY0 = vertical ? y + height * t0 : y;
               float sliceX1 = vertical ? x + width : x + width * t1;
               float sliceY1 = vertical ? y + height * t1 : y + height;
               Vector4f p1 = (new Vector4f(sliceX0, sliceY0, 0.0F, 1.0F)).mul(this.currentMatrix);
               Vector4f p2 = (new Vector4f(sliceX1, sliceY1, 0.0F, 1.0F)).mul(this.currentMatrix);
               int drawX0 = Math.round(Math.min(p1.x, p2.x));
               int drawY0 = Math.round(Math.min(p1.y, p2.y));
               int drawX1 = Math.round(Math.max(p1.x, p2.x));
               int drawY1 = Math.round(Math.max(p1.y, p2.y));
               if (drawX1 > drawX0 && drawY1 > drawY0) {
                  if (useRoundedPipeline) {
                     context.method_44379(drawX0, drawY0, drawX1, drawY1);
                     context.field_59826.method_70919(new class_11241(curvePipeline, class_11231.method_70899(), new Matrix3x2f(context.method_51448()), fullX0, fullY0, fullX1, fullY1, 0.0F, 1.0F, 0.0F, 1.0F, sliceColor.getRGB(), context.field_44659.method_70863()));
                     context.method_44380();
                  } else {
                     context.method_25294(drawX0, drawY0, drawX1, drawY1, sliceColor.getRGB());
                  }
               }
            }

         }
      }
   }

   public void drawText(class_332 context, String text, float x, float y, float fontSize, Color color, boolean shadow) {
      if (context != null) {
         Vector4f pos = (new Vector4f(x, y, 0.0F, 1.0F)).mul(this.currentMatrix);
         float currentScaleX = this.currentMatrix.m00();
         float fontScale = this.normalizeFontScale(this.getTextScale(fontSize) * CustomFont.getClientFontScale() * currentScaleX);
         float snappedX = this.snapToPixel(pos.x, fontScale);
         float snappedY = this.snapToPixel(pos.y, fontScale);
         boolean renderShadow = CustomFont.useClientShadow(shadow) && CustomFont.useGlobalShadow(shadow);
         class_2960 selectedFont = CustomFont.getSelectedFontId();
         if (selectedFont != null && CustomFont.isMsdfEnabled()) {
            boolean rendered = MSDFFontRenderer.drawText(context, selectedFont, text, snappedX, snappedY, fontScale, color.getRGB(), renderShadow);
            if (rendered) {
               return;
            }
         }

         context.method_51448().pushMatrix();
         context.method_51448().translate(snappedX, snappedY);
         context.method_51448().scale(fontScale, fontScale);
         CustomFont.drawText(context, class_310.method_1551().field_1772, text, 0, 0, color.getRGB(), renderShadow);
         context.method_51448().popMatrix();
      }
   }

   public void drawBlur(class_332 context, float x, float y, float width, float height, float blurRadius) {
   }

   public void drawBlurSimple(class_332 context, float x, float y, float width, float height, float blurRadius) {
   }

   public void rotate(float angle) {
      this.currentMatrix.rotate(angle, 0.0F, 0.0F, 1.0F);
   }

   public void drawCircle(class_332 context, float x, float y, float radius, Color color) {
      if (context != null) {
         Vector4f center = (new Vector4f(x, y, 0.0F, 1.0F)).mul(this.currentMatrix);
         float scaleX = this.currentMatrix.m00();
         float scaleY = this.currentMatrix.m11();
         float scaledRadius = radius * Math.max(scaleX, scaleY);
         int segments = Math.max(16, (int)(scaledRadius * 2.0F));
         double angleStep = 6.283185307179586D / (double)segments;
         GL11.glEnable(3042);
         GL11.glBlendFunc(770, 771);

         for(int i = 0; i < segments; ++i) {
            double angle1 = (double)i * angleStep;
            double angle2 = (double)(i + 1) * angleStep;
            float x1 = center.x + (float)(Math.cos(angle1) * (double)scaledRadius);
            float y1 = center.y + (float)(Math.sin(angle1) * (double)scaledRadius);
            float x2 = center.x + (float)(Math.cos(angle2) * (double)scaledRadius);
            float y2 = center.y + (float)(Math.sin(angle2) * (double)scaledRadius);
            context.method_25294((int)center.x, (int)center.y, (int)x1, (int)y1, color.getRGB());
            context.method_25294((int)x1, (int)y1, (int)x2, (int)y2, color.getRGB());
         }

      }
   }

   public void drawArc(class_332 context, float cx, float cy, float radius, float startAngle, float sweepAngle, float strokeWidth, Color color) {
      if (context != null) {
         Vector4f center = (new Vector4f(cx, cy, 0.0F, 1.0F)).mul(this.currentMatrix);
         float scaleX = this.currentMatrix.m00();
         float scaleY = this.currentMatrix.m11();
         float scaledRadius = radius * Math.max(scaleX, scaleY);
         float scaledStroke = strokeWidth * Math.max(scaleX, scaleY);
         double startRad = Math.toRadians((double)(startAngle - 90.0F));
         double sweepRad = Math.toRadians((double)sweepAngle);
         int segments = Math.max(8, (int)(Math.abs(sweepAngle) / 5.0F));
         double angleStep = sweepRad / (double)segments;
         GL11.glEnable(3042);
         GL11.glBlendFunc(770, 771);

         for(int i = 0; i < segments; ++i) {
            double angle1 = startRad + (double)i * angleStep;
            double angle2 = startRad + (double)(i + 1) * angleStep;
            float x1 = center.x + (float)(Math.cos(angle1) * (double)scaledRadius);
            float y1 = center.y + (float)(Math.sin(angle1) * (double)scaledRadius);
            float x2 = center.x + (float)(Math.cos(angle2) * (double)scaledRadius);
            float y2 = center.y + (float)(Math.sin(angle2) * (double)scaledRadius);
            this.drawThickLine(context, x1, y1, x2, y2, scaledStroke, color);
         }

      }
   }

   private void drawThickLine(class_332 context, float x1, float y1, float x2, float y2, float thickness, Color color) {
      float dx = x2 - x1;
      float dy = y2 - y1;
      float len = (float)Math.sqrt((double)(dx * dx + dy * dy));
      if (!(len < 0.001F)) {
         float perpX = -dy / len * (thickness / 2.0F);
         float perpY = dx / len * (thickness / 2.0F);
         int p1x = (int)(x1 + perpX);
         int p1y = (int)(y1 + perpY);
         int p2x = (int)(x1 - perpX);
         int p2y = (int)(y1 - perpY);
         int p3x = (int)(x2 - perpX);
         int p3y = (int)(y2 - perpY);
         int p4x = (int)(x2 + perpX);
         int p4y = (int)(y2 + perpY);
         context.method_25294(Math.min(p1x, Math.min(p2x, p3x)), Math.min(p1y, Math.min(p2y, p3y)), Math.max(p1x, Math.max(p2x, Math.max(p3x, p4x))), Math.max(p1y, Math.max(p2y, Math.max(p3y, p4y))), color.getRGB());
      }
   }

   public void drawCenteredText(class_332 context, String text, float x, float y, float fontSize, Color color, boolean shadow) {
      if (context != null) {
         float width = this.getTextWidth(text, fontSize);
         float heightOffset = this.getTextHeight(fontSize) / 2.0F;
         this.drawText(context, text, x - width / 2.0F, y - heightOffset, fontSize, color, shadow);
      }
   }

   public float getTextWidth(String text, float fontSize) {
      float scaledFont = this.normalizeFontScale(this.getTextScale(fontSize) * CustomFont.getClientFontScale());
      class_2960 selectedFont = CustomFont.getSelectedFontId();
      if (selectedFont != null && CustomFont.isMsdfEnabled()) {
         float msdfWidth = MSDFFontRenderer.getTextWidth(selectedFont, text);
         if (msdfWidth > 0.0F) {
            return msdfWidth * scaledFont;
         }
      }

      return (float)CustomFont.getTextWidth(class_310.method_1551().field_1772, text) * scaledFont;
   }

   public float getTextHeight(float fontSize) {
      float scaledFont = this.normalizeFontScale(this.getTextScale(fontSize) * CustomFont.getClientFontScale());
      return CustomFont.getTextHeight(class_310.method_1551().field_1772) * scaledFont;
   }

   public void setFontBlur(float blur) {
   }

   public void drawTexture(class_332 context, int nvgImageId, float x, float y, float width, float height, float u, float v, float rW, float rH, float tW, float tH) {
      if (context != null) {
         class_2960 texture = (class_2960)this.textureMap.get(nvgImageId);
         if (texture != null) {
            Vector4f p1 = (new Vector4f(x, y, 0.0F, 1.0F)).mul(this.currentMatrix);
            Vector4f p2 = (new Vector4f(x + width, y + height, 0.0F, 1.0F)).mul(this.currentMatrix);
            int drawX = (int)p1.x;
            int drawY = (int)p1.y;
            int drawW = (int)(p2.x - p1.x);
            int drawH = (int)(p2.y - p1.y);
            if (drawW > 0 && drawH > 0) {
               int regionW = Math.max(1, (int)rW);
               int regionH = Math.max(1, (int)rH);
               int textureW = Math.max(regionW, (int)tW);
               int textureH = Math.max(regionH, (int)tH);
               context.method_25302(class_10799.field_56883, texture, drawX, drawY, u, v, drawW, drawH, regionW, regionH, textureW, textureH);
            }
         }
      }
   }

   public void drawTextureRounded(class_332 context, int nvgImageId, float x, float y, float width, float height, float u, float v, float rW, float rH, float tW, float tH, float radius) {
      this.drawTexture(context, nvgImageId, x, y, width, height, u, v, rW, rH, tW, tH);
   }

   public int createImageFromTexture(int textureId, int width, int height) {
      return -1;
   }

   public void deleteImage(int imageId) {
      this.textureMap.remove(imageId);
   }

   public int createImageFromFile(String resourcePath) {
      if (resourcePath == null) {
         return -1;
      } else {
         String cleanPath = resourcePath;
         if (resourcePath.startsWith("/")) {
            cleanPath = resourcePath.substring(1);
         }

         if (cleanPath.startsWith("assets/")) {
            int slashIndex = cleanPath.indexOf("/");
            if (slashIndex != -1) {
               String namespace = cleanPath.substring(0, slashIndex);
               String path = cleanPath.substring(slashIndex + 1);
               class_2960 id = class_2960.method_60655(namespace, path);
               int newId = this.nextTextureId++;
               this.textureMap.put(newId, id);
               return newId;
            }
         }

         try {
            if (cleanPath.contains(":")) {
               class_2960 id = class_2960.method_60654(cleanPath);
               int newId = this.nextTextureId++;
               this.textureMap.put(newId, id);
               return newId;
            }
         } catch (Exception var8) {
         }

         return -1;
      }
   }

   public void scissor(class_332 context, float x, float y, float width, float height) {
      Vector4f p1 = (new Vector4f(x, y, 0.0F, 1.0F)).mul(this.currentMatrix);
      Vector4f p2 = (new Vector4f(x + width, y + height, 0.0F, 1.0F)).mul(this.currentMatrix);
      double scale = (double)class_310.method_1551().method_22683().method_4495();
      int scX = (int)((double)p1.x * scale);
      int scY = (int)((double)this.windowHeight * scale) - (int)((double)p2.y * scale);
      int scW = (int)((double)(p2.x - p1.x) * scale);
      int scH = (int)((double)(p2.y - p1.y) * scale);
      if (scW < 0) {
         scW = 0;
      }

      if (scH < 0) {
         scH = 0;
      }

      GL11.glEnable(3089);
      GL11.glScissor(scX, scY, scW, scH);
      if (context != null) {
         this.activeContext = context;
         if (context.field_44659.method_70863() != null) {
            context.method_44380();
         }

         context.method_44379((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
      }

   }

   public void scissor(float x, float y, float width, float height) {
      this.scissor((class_332)null, x, y, width, height);
   }

   public void resetScissor() {
      GL11.glDisable(3089);
      if (this.activeContext != null && this.activeContext.field_44659.method_70863() != null) {
         this.activeContext.method_44380();
      }

   }

   private float snapToPixel(float value, float scale) {
      return scale <= 0.0F ? value : (float)Math.round(value * scale) / scale;
   }

   private Color interpolate(Color start, Color end, float t) {
      float clamped = Math.max(0.0F, Math.min(1.0F, t));
      int r = (int)((float)start.getRed() + (float)(end.getRed() - start.getRed()) * clamped);
      int g = (int)((float)start.getGreen() + (float)(end.getGreen() - start.getGreen()) * clamped);
      int b = (int)((float)start.getBlue() + (float)(end.getBlue() - start.getBlue()) * clamped);
      int a = (int)((float)start.getAlpha() + (float)(end.getAlpha() - start.getAlpha()) * clamped);
      return new Color(Math.max(0, Math.min(255, r)), Math.max(0, Math.min(255, g)), Math.max(0, Math.min(255, b)), Math.max(0, Math.min(255, a)));
   }

   private float normalizeFontScale(float scale) {
      if (scale <= 0.0F) {
         return 1.0F;
      } else {
         float snapped = (float)Math.round(scale * 16.0F) / 16.0F;
         return Math.abs(snapped - 1.0F) < 0.01F ? 1.0F : snapped;
      }
   }

   private float getTextScale(float fontSize) {
      return fontSize / (CustomFont.getSelectedFontId() != null ? 10.0F : 8.0F);
   }
}
