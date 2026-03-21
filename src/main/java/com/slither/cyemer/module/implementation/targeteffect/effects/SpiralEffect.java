package com.slither.cyemer.module.implementation.targeteffect.effects;

import com.slither.cyemer.module.implementation.targeteffect.BaseEffect;
import com.slither.cyemer.module.implementation.targeteffect.EffectRenderContext;
import com.slither.cyemer.module.implementation.targeteffect.RenderHelpers;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.minecraft.class_4588;

@Environment(EnvType.CLIENT)
public class SpiralEffect extends BaseEffect {
   public void render(class_4587 matrices, class_4588 mainBuffer, class_4588 glowBuffer, EffectRenderContext ctx) {
      this.renderSpiral(matrices, mainBuffer, glowBuffer, ctx, false);
   }

   public void renderTextured(class_4587 matrices, class_4588 mainBuffer, class_4588 glowBuffer, EffectRenderContext ctx) {
      this.renderSpiral(matrices, mainBuffer, glowBuffer, ctx, true);
   }

   private void renderSpiral(class_4587 matrices, class_4588 mainBuffer, class_4588 glowBuffer, EffectRenderContext ctx, boolean textured) {
      double height = (double)ctx.player.method_17682();
      double radius = (double)ctx.player.method_17681() * 0.5D * ctx.scaleMultiplier;
      double timeOffset = (double)ctx.time * ctx.speedMultiplier / 1000.0D;
      double tubeRadius = 0.04D * ctx.scaleMultiplier;
      Color color = textured ? ctx.getThemedColor() : ctx.color;
      int points = ctx.segments * 4;
      int tubeSides = Math.max(6, ctx.segments / 6);
      List<class_243> pathPoints = new ArrayList();

      for(int i = 0; i <= points; ++i) {
         double t = (double)i / (double)points;
         double y = t * height;
         double angle = t * 3.141592653589793D * 8.0D + timeOffset;
         double x = Math.cos(angle) * radius;
         double z = Math.sin(angle) * radius;
         pathPoints.add(new class_243(x, y, z));
      }

      if (mainBuffer != null) {
         if (textured) {
            RenderHelpers.renderContinuousTubeTextured(matrices, mainBuffer, pathPoints, tubeRadius, color, ctx.alpha, tubeSides, ctx.uvOffsetU, ctx.uvOffsetV);
         } else {
            RenderHelpers.renderContinuousTube(matrices, mainBuffer, pathPoints, tubeRadius, color, ctx.alpha, tubeSides);
         }
      }

      if (glowBuffer != null) {
         double glowRadius = tubeRadius + ctx.glowSize;
         float glowAlpha = ctx.alpha * (float)ctx.glowOpacity;
         if (textured) {
            RenderHelpers.renderContinuousTubeTextured(matrices, glowBuffer, pathPoints, glowRadius, color, glowAlpha, Math.max(4, tubeSides - 2), ctx.uvOffsetU, ctx.uvOffsetV);
         } else {
            RenderHelpers.renderContinuousTube(matrices, glowBuffer, pathPoints, glowRadius, color, glowAlpha, Math.max(4, tubeSides - 2));
         }
      }

   }
}
