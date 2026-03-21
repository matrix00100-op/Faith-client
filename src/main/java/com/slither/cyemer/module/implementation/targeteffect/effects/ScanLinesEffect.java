package com.slither.cyemer.module.implementation.targeteffect.effects;

import com.slither.cyemer.module.implementation.targeteffect.BaseEffect;
import com.slither.cyemer.module.implementation.targeteffect.EffectRenderContext;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class ScanLinesEffect extends BaseEffect {
   public void render(class_4587 matrices, class_4588 mainBuffer, class_4588 glowBuffer, EffectRenderContext ctx) {
      this.renderScanLines(matrices, mainBuffer, glowBuffer, ctx, false);
   }

   public void renderTextured(class_4587 matrices, class_4588 mainBuffer, class_4588 glowBuffer, EffectRenderContext ctx) {
      this.renderScanLines(matrices, mainBuffer, glowBuffer, ctx, true);
   }

   private void renderScanLines(class_4587 matrices, class_4588 mainBuffer, class_4588 glowBuffer, EffectRenderContext ctx, boolean textured) {
      double height = (double)ctx.player.method_17682();
      double width = (double)ctx.player.method_17681();
      double radius = width * 1.0D;
      Color color = textured ? ctx.getThemedColor() : ctx.color;
      double speed = ctx.speedMultiplier * 0.003D;
      double rawSine = Math.sin((double)ctx.time * speed);
      double phase = (rawSine + 1.0D) / 2.0D;
      double scanY = height * phase;
      boolean movingUp = Math.cos((double)ctx.time * speed) > 0.0D;
      double scanThickness = 0.05D * ctx.scaleMultiplier;
      double trailSpacing = 0.06D * ctx.scaleMultiplier;
      this.renderRing(matrices, mainBuffer, radius, scanY, scanThickness, color, ctx.alpha, false, textured, ctx);
      int trailSegments = 5;

      for(int i = 1; i <= trailSegments; ++i) {
         double offset = (double)i * trailSpacing;
         double trailY = movingUp ? scanY - offset : scanY + offset;
         if (!(trailY < 0.0D) && !(trailY > height)) {
            float trailAlpha = ctx.alpha * (1.0F - (float)i / (float)trailSegments) * 0.5F;
            double trailThick = scanThickness * (1.0D - (double)i / (double)trailSegments * 0.5D);
            this.renderRing(matrices, mainBuffer, radius, trailY, trailThick, color, trailAlpha, false, textured, ctx);
         }
      }

      if (glowBuffer != null) {
         this.renderRing(matrices, glowBuffer, radius, scanY, scanThickness + ctx.glowSize, color, ctx.alpha * (float)ctx.glowOpacity, true, textured, ctx);
      }

   }

   private void renderRing(class_4587 matrices, class_4588 buffer, double radius, double y, double thickness, Color color, float alpha, boolean isGlow, boolean textured, EffectRenderContext ctx) {
      if (buffer != null) {
         Matrix4f matrix = matrices.method_23760().method_23761();
         float r = (float)color.getRed() / 255.0F;
         float g = (float)color.getGreen() / 255.0F;
         float b = (float)color.getBlue() / 255.0F;
         if (!isGlow && !textured) {
            float pulse = (float)(Math.sin((double)System.currentTimeMillis() / 100.0D) * 0.2D + 0.8D);
            r *= pulse;
            g *= pulse;
            b *= pulse;
         }

         double halfThick = thickness / 2.0D;
         double yBottom = y - halfThick;
         double yTop = y + halfThick;
         int segments = 32;
         double increment = 6.283185307179586D / (double)segments;

         for(int i = 0; i < segments; ++i) {
            double angle1 = (double)i * increment;
            double angle2 = (double)(i + 1) * increment;
            float x1 = (float)(Math.cos(angle1) * radius);
            float z1 = (float)(Math.sin(angle1) * radius);
            float x2 = (float)(Math.cos(angle2) * radius);
            float z2 = (float)(Math.sin(angle2) * radius);
            float u1;
            float u2;
            float v0;
            float v1;
            if (textured) {
               u1 = (float)i / (float)segments + ctx.uvOffsetU;
               u2 = (float)(i + 1) / (float)segments + ctx.uvOffsetU;
               v0 = 0.0F + ctx.uvOffsetV;
               v1 = 1.0F + ctx.uvOffsetV;
               buffer.method_22918(matrix, x1, (float)yBottom, z1).method_22915(r, g, b, alpha).method_22913(u1, v0);
               buffer.method_22918(matrix, x2, (float)yBottom, z2).method_22915(r, g, b, alpha).method_22913(u2, v0);
               buffer.method_22918(matrix, x2, (float)yTop, z2).method_22915(r, g, b, alpha).method_22913(u2, v1);
               buffer.method_22918(matrix, x1, (float)yBottom, z1).method_22915(r, g, b, alpha).method_22913(u1, v0);
               buffer.method_22918(matrix, x2, (float)yTop, z2).method_22915(r, g, b, alpha).method_22913(u2, v1);
               buffer.method_22918(matrix, x1, (float)yTop, z1).method_22915(r, g, b, alpha).method_22913(u1, v1);
            } else {
               u1 = (x1 + x2) / 2.0F;
               u2 = (z1 + z2) / 2.0F;
               v0 = (float)Math.sqrt((double)(u1 * u1 + u2 * u2));
               u1 /= v0;
               u2 /= v0;
               v1 = 0.0F;
               buffer.method_22918(matrix, x1, (float)yBottom, z1).method_22915(r, g, b, alpha).method_22914(u1, v1, u2);
               buffer.method_22918(matrix, x2, (float)yBottom, z2).method_22915(r, g, b, alpha).method_22914(u1, v1, u2);
               buffer.method_22918(matrix, x2, (float)yTop, z2).method_22915(r, g, b, alpha).method_22914(u1, v1, u2);
               buffer.method_22918(matrix, x1, (float)yBottom, z1).method_22915(r, g, b, alpha).method_22914(u1, v1, u2);
               buffer.method_22918(matrix, x2, (float)yTop, z2).method_22915(r, g, b, alpha).method_22914(u1, v1, u2);
               buffer.method_22918(matrix, x1, (float)yTop, z1).method_22915(r, g, b, alpha).method_22914(u1, v1, u2);
            }
         }

      }
   }
}
