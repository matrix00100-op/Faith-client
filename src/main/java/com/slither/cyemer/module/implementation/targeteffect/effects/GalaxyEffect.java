package com.slither.cyemer.module.implementation.targeteffect.effects;

import com.slither.cyemer.module.implementation.targeteffect.BaseEffect;
import com.slither.cyemer.module.implementation.targeteffect.EffectRenderContext;
import com.slither.cyemer.module.implementation.targeteffect.RenderHelpers;
import com.slither.cyemer.util.render.TexturedRenderUtils;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class GalaxyEffect extends BaseEffect {
   public void render(class_4587 matrices, class_4588 mainBuffer, class_4588 glowBuffer, EffectRenderContext ctx) {
      this.renderGalaxy(matrices, mainBuffer, glowBuffer, ctx, false);
   }

   public void renderTextured(class_4587 matrices, class_4588 mainBuffer, class_4588 glowBuffer, EffectRenderContext ctx) {
      this.renderGalaxy(matrices, mainBuffer, glowBuffer, ctx, true);
   }

   private void renderGalaxy(class_4587 matrices, class_4588 mainBuffer, class_4588 glowBuffer, EffectRenderContext ctx, boolean textured) {
      double torsoHeight = (double)ctx.player.method_17682() / 2.0D;
      double rotation = (double)ctx.time * ctx.speedMultiplier * 0.5D / 1000.0D;
      Color color = textured ? ctx.getThemedColor() : ctx.color;
      matrices.method_22903();
      matrices.method_22904(0.0D, torsoHeight, 0.0D);
      int arms = 4;
      int particlesPerArm = Math.max(8, ctx.segments);

      for(int arm = 0; arm < arms; ++arm) {
         double armOffset = 6.283185307179586D * (double)arm / (double)arms;

         for(int i = 0; i < particlesPerArm; ++i) {
            double t = (double)i / (double)particlesPerArm;
            double angle = t * 3.141592653589793D * 3.0D + rotation + armOffset;
            double dist = t * (double)ctx.player.method_17681() * 1.2D * ctx.scaleMultiplier;
            double x = Math.cos(angle) * dist;
            double z = Math.sin(angle) * dist;
            double particleSize = 0.04D * ctx.scaleMultiplier * (1.0D - t * 0.5D);
            float particleAlpha = ctx.alpha * (1.0F - (float)t * 0.3F);
            matrices.method_22903();
            matrices.method_22904(x, 0.0D, z);
            if (textured) {
               this.renderTexturedParticle(matrices, mainBuffer, glowBuffer, particleSize, color, particleAlpha, ctx);
            } else {
               if (mainBuffer != null) {
                  RenderHelpers.renderSphereOptimized(matrices, mainBuffer, particleSize, color, particleAlpha, Math.max(6, ctx.segments / 4));
               }

               if (glowBuffer != null) {
                  double glowRadius = particleSize + ctx.glowSize * 0.5D;
                  float glowAlpha = particleAlpha * (float)ctx.glowOpacity;
                  RenderHelpers.renderSphereOptimized(matrices, glowBuffer, glowRadius, color, glowAlpha, Math.max(4, ctx.segments / 6));
               }
            }

            matrices.method_22909();
         }
      }

      matrices.method_22909();
   }

   private void renderTexturedParticle(class_4587 matrices, class_4588 mainBuffer, class_4588 glowBuffer, double size, Color color, float alpha, EffectRenderContext ctx) {
      Matrix4f matrix = matrices.method_23760().method_23761();
      if (mainBuffer != null) {
         TexturedRenderUtils.drawTexturedSphere(mainBuffer, matrix, class_243.field_1353, size, color, alpha, Math.max(6, ctx.segments / 4), ctx.uvOffsetU, ctx.uvOffsetV);
      }

      if (glowBuffer != null) {
         double glowRadius = size + ctx.glowSize * 0.5D;
         float glowAlpha = alpha * (float)ctx.glowOpacity;
         TexturedRenderUtils.drawTexturedSphere(glowBuffer, matrix, class_243.field_1353, glowRadius, color, glowAlpha, Math.max(4, ctx.segments / 6), ctx.uvOffsetU, ctx.uvOffsetV);
      }

   }
}
