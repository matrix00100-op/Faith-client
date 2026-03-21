package com.slither.cyemer.module.implementation.targeteffect.effects;

import com.slither.cyemer.module.implementation.targeteffect.BaseEffect;
import com.slither.cyemer.module.implementation.targeteffect.EffectRenderContext;
import com.slither.cyemer.module.implementation.targeteffect.RenderHelpers;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_4587;
import net.minecraft.class_4588;

@Environment(EnvType.CLIENT)
public class PulseEffect extends BaseEffect {
   public void render(class_4587 matrices, class_4588 mainBuffer, class_4588 glowBuffer, EffectRenderContext ctx) {
      this.renderPulse(matrices, mainBuffer, glowBuffer, ctx, false);
   }

   public void renderTextured(class_4587 matrices, class_4588 mainBuffer, class_4588 glowBuffer, EffectRenderContext ctx) {
      this.renderPulse(matrices, mainBuffer, glowBuffer, ctx, true);
   }

   private void renderPulse(class_4587 matrices, class_4588 mainBuffer, class_4588 glowBuffer, EffectRenderContext ctx, boolean textured) {
      double torsoHeight = (double)ctx.player.method_17682() / 2.0D;
      double phase = (double)ctx.time * ctx.speedMultiplier / 1000.0D;
      double pulse = phase % 2.0D / 2.0D;
      double baseRadius = (double)ctx.player.method_17681() * 0.3D * ctx.scaleMultiplier;
      double maxRadius = (double)ctx.player.method_17681() * 1.5D * ctx.scaleMultiplier;
      double radius = baseRadius + (maxRadius - baseRadius) * pulse;
      float ringAlpha = ctx.alpha * (1.0F - (float)pulse);
      double thickness = 0.06D * ctx.scaleMultiplier * (1.0D - pulse * 0.5D);
      Color color = textured ? ctx.getThemedColor() : ctx.color;
      matrices.method_22903();
      matrices.method_22904(0.0D, torsoHeight, 0.0D);
      if (mainBuffer != null) {
         if (textured) {
            RenderHelpers.renderTorusTextured(matrices, mainBuffer, radius, thickness, 0.0D, color, ringAlpha, ctx.segments, 8, ctx.uvOffsetU, ctx.uvOffsetV);
         } else {
            RenderHelpers.renderTorusOptimized(matrices, mainBuffer, radius, thickness, 0.0D, color, ringAlpha, ctx.segments, 8);
         }
      }

      if (glowBuffer != null) {
         double glowThickness = thickness + ctx.glowSize;
         float glowAlpha = ringAlpha * (float)ctx.glowOpacity;
         if (textured) {
            RenderHelpers.renderTorusTextured(matrices, glowBuffer, radius, glowThickness, 0.0D, color, glowAlpha, Math.max(16, ctx.segments / 2), 6, ctx.uvOffsetU, ctx.uvOffsetV);
         } else {
            RenderHelpers.renderTorusOptimized(matrices, glowBuffer, radius, glowThickness, 0.0D, color, glowAlpha, Math.max(16, ctx.segments / 2), 6);
         }
      }

      matrices.method_22909();
   }
}
