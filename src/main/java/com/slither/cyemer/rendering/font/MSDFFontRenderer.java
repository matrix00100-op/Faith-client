package com.slither.cyemer.rendering.font;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.slither.cyemer.shader.CoreShaderManager;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_10799;
import net.minecraft.class_11231;
import net.minecraft.class_11241;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import net.minecraft.class_332;
import org.joml.Matrix3x2f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public final class MSDFFontRenderer {
   private static final Logger LOGGER = LoggerFactory.getLogger(MSDFFontRenderer.class);
   private static final int FIRST_CHAR = 32;
   private static final int LAST_CHAR = 126;
   private static final int CHAR_COUNT = 95;
   private static final int COLUMNS = 16;
   private static final int BASE_FONT_SIZE = 192;
   private static final int PADDING = 8;
   private static final float TARGET_LINE_HEIGHT = 9.0F;
   private static final float BASELINE_SHIFT = -0.75F;
   private static final class_2960 MSDF_PIPELINE_ID = class_2960.method_60655("dynamic_fps", "msdf_text");
   private static final Map<class_2960, MSDFFontRenderer.FontAtlas> ATLASES = new HashMap();
   private static final Set<class_2960> FAILED_ATLASES = new HashSet();

   private MSDFFontRenderer() {
   }

   public static boolean drawText(class_332 context, class_2960 fontId, String text, float x, float y, float scale, int argb, boolean shadow) {
      if (context != null && fontId != null && text != null && !text.isEmpty()) {
         MSDFFontRenderer.FontAtlas atlas = getOrCreateAtlas(fontId);
         if (atlas == null) {
            return false;
         } else {
            if (shadow) {
               float shadowOffset = Math.max(1.0F, scale * 0.75F);
               drawInternal(context, atlas, text, x + shadowOffset, y + shadowOffset, scale, toShadowColor(argb));
            }

            drawInternal(context, atlas, text, x, y, scale, argb);
            return true;
         }
      } else {
         return false;
      }
   }

   public static float getTextWidth(class_2960 fontId, String text) {
      if (fontId != null && text != null && !text.isEmpty()) {
         MSDFFontRenderer.FontAtlas atlas = getOrCreateAtlas(fontId);
         if (atlas == null) {
            return 0.0F;
         } else {
            float lineWidth = 0.0F;
            float maxWidth = 0.0F;

            for(int i = 0; i < text.length(); ++i) {
               char c = text.charAt(i);
               if (c == '\n') {
                  maxWidth = Math.max(maxWidth, lineWidth);
                  lineWidth = 0.0F;
               } else {
                  MSDFFontRenderer.Glyph glyph = atlas.getGlyph(c);
                  lineWidth += (float)(glyph != null ? glyph.advance : atlas.spaceAdvance) * atlas.metricScale;
               }
            }

            return Math.max(maxWidth, lineWidth);
         }
      } else {
         return 0.0F;
      }
   }

   public static float getTextHeight(class_2960 fontId) {
      MSDFFontRenderer.FontAtlas atlas = getOrCreateAtlas(fontId);
      return atlas == null ? 9.0F : (float)atlas.lineHeight * atlas.metricScale;
   }

   private static void drawInternal(class_332 context, MSDFFontRenderer.FontAtlas atlas, String text, float x, float y, float scale, int color) {
      RenderPipeline pipeline = CoreShaderManager.getInstance().getPipeline(MSDF_PIPELINE_ID);
      if (pipeline == null) {
         pipeline = class_10799.field_56883;
      }

      class_1044 texture = class_310.method_1551().method_1531().method_4619(atlas.textureId);
      if (texture != null && texture.method_71659() != null && texture.method_75484() != null) {
         class_11231 textureSetup = class_11231.method_70900(texture.method_71659(), texture.method_75484());
         float renderScale = scale * atlas.metricScale;
         float cursorX = x;
         float cursorY = y + -0.75F * scale;
         float lineHeightScaled = (float)atlas.lineHeight * renderScale;

         for(int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (c == '\n') {
               cursorX = x;
               cursorY += lineHeightScaled;
            } else {
               MSDFFontRenderer.Glyph glyph = atlas.getGlyph(c);
               if (glyph == null) {
                  cursorX += (float)atlas.spaceAdvance * renderScale;
               } else {
                  int x1 = (int)Math.floor((double)cursorX);
                  int y1 = (int)Math.floor((double)cursorY);
                  int x2 = (int)Math.ceil((double)(cursorX + (float)glyph.width * renderScale));
                  int y2 = (int)Math.ceil((double)(cursorY + (float)glyph.height * renderScale));
                  if (x2 > x1 && y2 > y1) {
                     float u1 = (float)glyph.u / (float)atlas.width;
                     float u2 = (float)(glyph.u + glyph.width) / (float)atlas.width;
                     float v1 = (float)glyph.v / (float)atlas.height;
                     float v2 = (float)(glyph.v + glyph.height) / (float)atlas.height;
                     context.field_59826.method_70919(new class_11241(pipeline, textureSetup, new Matrix3x2f(context.method_51448()), x1, y1, x2, y2, u1, u2, v1, v2, color, context.field_44659.method_70863()));
                  }

                  cursorX += (float)glyph.advance * renderScale;
               }
            }
         }

      }
   }

   private static MSDFFontRenderer.FontAtlas getOrCreateAtlas(class_2960 fontId) {
      if (FAILED_ATLASES.contains(fontId)) {
         return null;
      } else {
         MSDFFontRenderer.FontAtlas cached = (MSDFFontRenderer.FontAtlas)ATLASES.get(fontId);
         if (cached != null) {
            return cached;
         } else {
            MSDFFontRenderer.FontAtlas atlas = buildAtlas(fontId);
            if (atlas == null) {
               FAILED_ATLASES.add(fontId);
               return null;
            } else {
               ATLASES.put(fontId, atlas);
               return atlas;
            }
         }
      }
   }

   private static MSDFFontRenderer.FontAtlas buildAtlas(class_2960 fontId) {
      class_2960 ttfResource = resolveTtfResource(fontId);
      if (ttfResource == null) {
         return null;
      } else {
         try {
            InputStream stream = openFontStream(ttfResource);

            MSDFFontRenderer.FontAtlas var38;
            try {
               Font font = Font.createFont(0, stream).deriveFont(192.0F);
               BufferedImage probe = new BufferedImage(1, 1, 2);
               Graphics2D probeGraphics = probe.createGraphics();
               configureGraphics(probeGraphics);
               probeGraphics.setFont(font);
               FontMetrics metrics = probeGraphics.getFontMetrics();
               int ascent = metrics.getAscent();
               int lineHeight = Math.max(1, metrics.getHeight());
               int maxAdvance = 1;

               int spaceAdvance;
               for(spaceAdvance = 32; spaceAdvance <= 126; ++spaceAdvance) {
                  maxAdvance = Math.max(maxAdvance, metrics.charWidth((char)spaceAdvance));
               }

               spaceAdvance = Math.max(1, metrics.charWidth(' '));
               probeGraphics.dispose();
               int cellWidth = maxAdvance + 16;
               int cellHeight = lineHeight + 16;
               int rows = 6;
               int atlasWidth = nextPowerOfTwo(cellWidth * 16);
               int atlasHeight = nextPowerOfTwo(cellHeight * rows);
               BufferedImage image = new BufferedImage(atlasWidth, atlasHeight, 2);
               Graphics2D graphics = image.createGraphics();
               configureGraphics(graphics);
               graphics.setComposite(AlphaComposite.Src);
               graphics.setColor(new Color(0, 0, 0, 0));
               graphics.fillRect(0, 0, atlasWidth, atlasHeight);
               graphics.setFont(font);
               graphics.setColor(Color.WHITE);
               Map<Character, MSDFFontRenderer.Glyph> glyphs = new HashMap();
               int index = 0;

               int px;
               int argb;
               int alpha;
               int outArgb;
               for(int c = 32; c <= 126; ++c) {
                  char ch = (char)c;
                  px = index / 16;
                  argb = index % 16;
                  alpha = argb * cellWidth;
                  int cellY = px * cellHeight;
                  outArgb = alpha + 8;
                  int drawY = cellY + 8 + ascent;
                  int advance = Math.max(1, metrics.charWidth(ch));
                  int drawWidth = Math.max(1, advance);
                  graphics.drawString(String.valueOf(ch), outArgb, drawY);
                  glyphs.put(ch, new MSDFFontRenderer.Glyph(outArgb, cellY + 8, drawWidth, lineHeight, advance));
                  ++index;
               }

               graphics.dispose();
               class_1011 nativeImage = new class_1011(atlasWidth, atlasHeight, true);

               for(int py = 0; py < atlasHeight; ++py) {
                  for(px = 0; px < atlasWidth; ++px) {
                     argb = image.getRGB(px, py);
                     alpha = argb >>> 24 & 255;
                     outArgb = alpha << 24 | alpha << 16 | alpha << 8 | alpha;
                     nativeImage.method_61941(px, py, outArgb);
                  }
               }

               class_2960 textureId = class_2960.method_60655("dynamic_fps", "generated/msdf_" + fontId.method_12832());
               class_1043 texture = new class_1043(() -> {
                  return "cyemer_msdf_" + fontId.method_12832();
               }, nativeImage);
               configureTextureFilter(texture);
               texture.method_4524();
               class_310.method_1551().method_1531().method_4616(textureId, texture);
               float metricScale = 9.0F / (float)lineHeight;
               var38 = new MSDFFontRenderer.FontAtlas(textureId, atlasWidth, atlasHeight, lineHeight, spaceAdvance, metricScale, glyphs);
            } catch (Throwable var31) {
               if (stream != null) {
                  try {
                     stream.close();
                  } catch (Throwable var30) {
                     var31.addSuppressed(var30);
                  }
               }

               throw var31;
            }

            if (stream != null) {
               stream.close();
            }

            return var38;
         } catch (FontFormatException | IOException var32) {
            LOGGER.warn("Failed to build MSDF atlas for font {}", fontId, var32);
            return null;
         }
      }
   }

   private static InputStream openFontStream(class_2960 resourceId) throws IOException {
      class_310 client = class_310.method_1551();
      if (client != null && client.method_1478() != null) {
         Optional<class_3298> optional = client.method_1478().method_14486(resourceId);
         if (optional.isPresent()) {
            return ((class_3298)optional.get()).method_14482();
         }
      }

      String var10000 = resourceId.method_12836();
      String classpathPath = "/assets/" + var10000 + "/" + resourceId.method_12832();
      InputStream classpathStream = MSDFFontRenderer.class.getResourceAsStream(classpathPath);
      if (classpathStream != null) {
         return classpathStream;
      } else {
         throw new IOException("Missing font resource " + String.valueOf(resourceId));
      }
   }

   private static void configureGraphics(Graphics2D graphics) {
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
   }

   private static void configureTextureFilter(class_1043 texture) {
      Method blurOnly;
      try {
         blurOnly = texture.getClass().getMethod("setFilter", Boolean.TYPE, Boolean.TYPE);
         blurOnly.invoke(texture, true, false);
      } catch (ReflectiveOperationException var3) {
         try {
            blurOnly = texture.getClass().getMethod("setFilter", Boolean.TYPE);
            blurOnly.invoke(texture, true);
         } catch (ReflectiveOperationException var2) {
         }

      }
   }

   private static class_2960 resolveTtfResource(class_2960 fontId) {
      String path = fontId.method_12832();
      if ("sans".equals(path)) {
         return class_2960.method_60655("dynamic_fps", "font/opensans-regular.ttf");
      } else if ("semibold".equals(path)) {
         return class_2960.method_60655("dynamic_fps", "font/semibold.ttf");
      } else {
         return "cyemer".equals(path) ? class_2960.method_60655("dynamic_fps", "font/font.ttf") : null;
      }
   }

   private static int nextPowerOfTwo(int value) {
      int size;
      for(size = 1; size < value; size <<= 1) {
      }

      return size;
   }

   private static int toShadowColor(int argb) {
      int alpha = argb >>> 24 & 255;
      int shadowAlpha = Math.max(0, Math.min(255, Math.round((float)alpha * 0.45F)));
      return shadowAlpha << 24;
   }

   @Environment(EnvType.CLIENT)
   private static record FontAtlas(class_2960 textureId, int width, int height, int lineHeight, int spaceAdvance, float metricScale, Map<Character, MSDFFontRenderer.Glyph> glyphs) {
      private FontAtlas(class_2960 textureId, int width, int height, int lineHeight, int spaceAdvance, float metricScale, Map<Character, MSDFFontRenderer.Glyph> glyphs) {
         this.textureId = textureId;
         this.width = width;
         this.height = height;
         this.lineHeight = lineHeight;
         this.spaceAdvance = spaceAdvance;
         this.metricScale = metricScale;
         this.glyphs = glyphs;
      }

      private MSDFFontRenderer.Glyph getGlyph(char c) {
         MSDFFontRenderer.Glyph glyph = (MSDFFontRenderer.Glyph)this.glyphs.get(c);
         return glyph != null ? glyph : (MSDFFontRenderer.Glyph)this.glyphs.get('?');
      }

      public class_2960 textureId() {
         return this.textureId;
      }

      public int width() {
         return this.width;
      }

      public int height() {
         return this.height;
      }

      public int lineHeight() {
         return this.lineHeight;
      }

      public int spaceAdvance() {
         return this.spaceAdvance;
      }

      public float metricScale() {
         return this.metricScale;
      }

      public Map<Character, MSDFFontRenderer.Glyph> glyphs() {
         return this.glyphs;
      }
   }

   @Environment(EnvType.CLIENT)
   private static record Glyph(int u, int v, int width, int height, int advance) {
      private Glyph(int u, int v, int width, int height, int advance) {
         this.u = u;
         this.v = v;
         this.width = width;
         this.height = height;
         this.advance = advance;
      }

      public int u() {
         return this.u;
      }

      public int v() {
         return this.v;
      }

      public int width() {
         return this.width;
      }

      public int height() {
         return this.height;
      }

      public int advance() {
         return this.advance;
      }
   }
}
