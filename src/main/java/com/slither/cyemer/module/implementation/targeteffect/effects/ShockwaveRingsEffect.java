package com.slither.cyemer.module.implementation.targeteffect.effects;

import com.slither.cyemer.module.implementation.targeteffect.BaseEffect;
import com.slither.cyemer.module.implementation.targeteffect.EffectRenderContext;
import com.slither.cyemer.module.implementation.targeteffect.RenderHelpers;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_4587;
import net.minecraft.class_4588;

@Environment(EnvType.CLIENT)
public class ShockwaveRingsEffect extends BaseEffect {
   private final List<ShockwaveRingsEffect.Shockwave> activeWaves = new ArrayList();
   private long lastSpawnTime = 0L;

   public void render(class_4587 matrices, class_4588 mainBuffer, class_4588 glowBuffer, EffectRenderContext ctx) {
      this.renderShockwave(matrices, mainBuffer, glowBuffer, ctx, false);
   }

   public void renderTextured(class_4587 matrices, class_4588 mainBuffer, class_4588 glowBuffer, EffectRenderContext ctx) {
      this.renderShockwave(matrices, mainBuffer, glowBuffer, ctx, true);
   }

   private void renderShockwave(class_4587 matrices, class_4588 mainBuffer, class_4588 glowBuffer, EffectRenderContext ctx, boolean textured) {
      long currentTime = ctx.time;
      double maxRadius = (double)ctx.player.method_17681() * 3.0D * ctx.scaleMultiplier;
      Color color = textured ? ctx.getThemedColor() : ctx.color;
      int spawnInterval = (int)(800.0D / ctx.speedMultiplier);
      if (currentTime - this.lastSpawnTime > (long)spawnInterval) {
         this.activeWaves.add(new ShockwaveRingsEffect.Shockwave(currentTime));
         this.lastSpawnTime = currentTime;
      }

      this.activeWaves.removeIf((wavex) -> {
         return wavex.isDead(currentTime, maxRadius);
      });
      matrices.method_22903();
      matrices.method_22904(0.0D, 0.05D, 0.0D);
      Iterator var12 = this.activeWaves.iterator();

      while(var12.hasNext()) {
         ShockwaveRingsEffect.Shockwave wave = (ShockwaveRingsEffect.Shockwave)var12.next();
         double radius = wave.getRadius(currentTime, ctx.speedMultiplier);
         float alpha = wave.getAlpha(currentTime, maxRadius) * ctx.alpha;
         double thickness = 0.08D * ctx.scaleMultiplier;
         double pulse = Math.sin((double)currentTime / 100.0D + (double)wave.spawnTime) * 0.2D + 1.0D;
         thickness *= pulse;
         if (mainBuffer != null) {
            if (textured) {
               RenderHelpers.renderTorusTextured(matrices, mainBuffer, radius, thickness, 0.0D, color, alpha, Math.max(16, ctx.segments), 8, ctx.uvOffsetU, ctx.uvOffsetV);
            } else {
               RenderHelpers.renderTorusOptimized(matrices, mainBuffer, radius, thickness, 0.0D, color, alpha, Math.max(16, ctx.segments), 8);
            }
         }

         if (glowBuffer != null) {
            double glowThickness = thickness + ctx.glowSize;
            float glowAlpha = alpha * (float)ctx.glowOpacity;
            if (textured) {
               RenderHelpers.renderTorusTextured(matrices, glowBuffer, radius, glowThickness, 0.0D, color, glowAlpha, Math.max(12, ctx.segments / 2), 6, ctx.uvOffsetU, ctx.uvOffsetV);
            } else {
               RenderHelpers.renderTorusOptimized(matrices, glowBuffer, radius, glowThickness, 0.0D, color, glowAlpha, Math.max(12, ctx.segments / 2), 6);
            }
         }
      }

      matrices.method_22909();
      if (ctx.time % 100L < 50L) {
         this.renderGroundGlow(matrices, mainBuffer, glowBuffer, ctx, textured, color);
      }

   }

   private void renderGroundGlow(class_4587 matrices, class_4588 mainBuffer, class_4588 glowBuffer, EffectRenderContext ctx, boolean textured, Color color) {
      double radius = (double)ctx.player.method_17681() * 0.4D * ctx.scaleMultiplier;
      double pulse = Math.sin((double)ctx.time / 200.0D) * 0.5D + 0.5D;
      float glowAlpha = ctx.alpha * (float)pulse * 0.3F;
      matrices.method_22903();
      matrices.method_22904(0.0D, 0.01D, 0.0D);
      if (mainBuffer != null) {
         if (textured) {
            RenderHelpers.renderTorusTextured(matrices, mainBuffer, radius, 0.05D * ctx.scaleMultiplier, 0.0D, color, glowAlpha, 24, 6, ctx.uvOffsetU, ctx.uvOffsetV);
         } else {
            RenderHelpers.renderTorusOptimized(matrices, mainBuffer, radius, 0.05D * ctx.scaleMultiplier, 0.0D, color, glowAlpha, 24, 6);
         }
      }

      if (glowBuffer != null) {
         if (textured) {
            RenderHelpers.renderTorusTextured(matrices, glowBuffer, radius * 1.2D, 0.08D * ctx.scaleMultiplier, 0.0D, color, glowAlpha * (float)ctx.glowOpacity, 16, 4, ctx.uvOffsetU, ctx.uvOffsetV);
         } else {
            RenderHelpers.renderTorusOptimized(matrices, glowBuffer, radius * 1.2D, 0.08D * ctx.scaleMultiplier, 0.0D, color, glowAlpha * (float)ctx.glowOpacity, 16, 4);
         }
      }

      matrices.method_22909();
   }

   @Environment(EnvType.CLIENT)
   private static class Shockwave {
      long spawnTime;
      double initialRadius;

      Shockwave(long spawnTime) {
         this.spawnTime = spawnTime;
         this.initialRadius = 0.0D;
      }

      double getRadius(long currentTime, double speed) {
         double age = (double)(currentTime - this.spawnTime) / 1000.0D;
         return this.initialRadius + age * 2.0D * speed;
      }

      float getAlpha(long currentTime, double maxRadius) {
         double radius = this.getRadius(currentTime, 1.0D);
         return (float)Math.max(0.0D, 1.0D - radius / maxRadius);
      }

      boolean isDead(long currentTime, double maxRadius) {
         return this.getRadius(currentTime, 1.0D) > maxRadius;
      }
   }
}
