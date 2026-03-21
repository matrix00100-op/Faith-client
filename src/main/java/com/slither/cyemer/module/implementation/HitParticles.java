package com.slither.cyemer.module.implementation;

import com.slither.cyemer.module.BooleanSetting;
import com.slither.cyemer.module.Category;
import com.slither.cyemer.module.ColorSetting;
import com.slither.cyemer.module.ModeSetting;
import com.slither.cyemer.module.Module;
import com.slither.cyemer.module.SliderSetting;
import com.slither.cyemer.util.render.types.TexturedRenderLayers;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1921;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_2902.class_2903;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class HitParticles extends Module {
   private static final class_2960 FIREFLY_TEXTURE = class_2960.method_60655("dynamic_fps", "textures/visuals/firefly.png");
   private static final class_2960 HEART_TEXTURE = class_2960.method_60655("dynamic_fps", "textures/visuals/heart.png");
   private static final class_2960 STAR_TEXTURE = class_2960.method_60655("dynamic_fps", "textures/visuals/star.png");
   private final SliderSetting particleCount = new SliderSetting("Particle Count", 15.0D, 5.0D, 50.0D, 0);
   private final SliderSetting particleLifespan = new SliderSetting("Lifespan", 2.0D, 0.5D, 5.0D, 1);
   private final SliderSetting particleSize = new SliderSetting("Size", 0.3D, 0.1D, 1.0D, 1);
   private final SliderSetting levitateTime = new SliderSetting("Levitate Time", 1.5D, 0.5D, 3.0D, 1);
   private final SliderSetting levitateHeight = new SliderSetting("Levitate Height", 0.3D, 0.1D, 1.0D, 1);
   private final ColorSetting particleColor = new ColorSetting("Color", new Color(255, 100, 100));
   private final BooleanSetting rainbow = new BooleanSetting("Rainbow", false);
   private final ModeSetting particleType = new ModeSetting("Type", new String[]{"Circle", "Star", "Heart"});
   private final BooleanSetting glow = new BooleanSetting("Glow", true);
   private final SliderSetting glowIntensity = new SliderSetting("Glow Intensity", 0.7D, 0.0D, 1.0D, 2);
   private final BooleanSetting groundCollision = new BooleanSetting("Ground Collision", true);
   private final SliderSetting bounciness = new SliderSetting("Bounciness", 0.5D, 0.0D, 1.0D, 2);
   private final SliderSetting fadeOutTime = new SliderSetting("Fade Out Time", 0.5D, 0.1D, 2.0D, 1);
   private final SliderSetting speed = new SliderSetting("Speed", 0.5D, 0.1D, 5.0D, 1);
   private final List<HitParticles.HitEffect> activeEffects = new ArrayList();
   private final Random random = new Random();
   private long lastUpdateTime = -1L;

   public HitParticles() {
      super("HitParticles", "Spawns particles when hitting players", Category.RENDER);
      this.addSetting(this.particleCount);
      this.addSetting(this.particleLifespan);
      this.addSetting(this.particleSize);
      this.addSetting(this.levitateTime);
      this.addSetting(this.levitateHeight);
      this.addSetting(this.speed);
      this.addSetting(this.particleColor);
      this.addSetting(this.rainbow);
      this.addSetting(this.particleType);
      this.addSetting(this.glow);
      this.addSetting(this.glowIntensity);
      this.addSetting(this.groundCollision);
      this.addSetting(this.bounciness);
      this.addSetting(this.fadeOutTime);
   }

   public void onEntityHit(class_1297 target) {
      if (this.isEnabled() && target instanceof class_1657 && this.mc.field_1724 != null && this.mc.field_1687 != null) {
         class_243 hitPos = target.method_73189().method_1031(0.0D, (double)(target.method_17682() / 2.0F), 0.0D);
         this.activeEffects.add(new HitParticles.HitEffect(hitPos, target, (int)this.particleCount.getValue()));
      }
   }

   public void onWorldRender(class_4587 matrices, float tickDelta) {
      if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
         long now = System.nanoTime();
         if (this.lastUpdateTime < 0L) {
            this.lastUpdateTime = now;
         }

         float deltaSeconds = (float)(now - this.lastUpdateTime) / 1.0E9F;
         this.lastUpdateTime = now;
         if (deltaSeconds > 0.1F) {
            deltaSeconds = 0.1F;
         }

         class_243 cameraPos = this.mc.field_1773.method_19418().method_71156();
         this.activeEffects.removeIf((effect) -> {
            effect.update(deltaSeconds);
            if (!effect.isDead()) {
               effect.render(matrices, cameraPos);
               return false;
            } else {
               return true;
            }
         });
      }
   }

   private Color getRainbowColor(long offset) {
      float hue = (float)((System.currentTimeMillis() + offset) % 3000L) / 3000.0F;
      return Color.getHSBColor(hue, 0.8F, 1.0F);
   }

   private class_2960 getTextureForType() {
      String var1 = this.particleType.getCurrentMode();
      byte var2 = -1;
      switch(var1.hashCode()) {
      case 2587250:
         if (var1.equals("Star")) {
            var2 = 1;
         }
         break;
      case 69599270:
         if (var1.equals("Heart")) {
            var2 = 0;
         }
      }

      class_2960 var10000;
      switch(var2) {
      case 0:
         var10000 = HEART_TEXTURE;
         break;
      case 1:
         var10000 = STAR_TEXTURE;
         break;
      default:
         var10000 = FIREFLY_TEXTURE;
      }

      return var10000;
   }

   @Environment(EnvType.CLIENT)
   private class HitEffect {
      private final List<HitParticles.HitParticle> particles = new ArrayList();
      private final float maxLevitateTime;
      private final float maxLevitateHeight;
      private float levitateTimer;

      public HitEffect(class_243 position, class_1297 target, int count) {
         this.maxLevitateTime = (float)HitParticles.this.levitateTime.getValue();
         this.maxLevitateHeight = (float)HitParticles.this.levitateHeight.getValue();
         this.levitateTimer = 0.0F;

         for(int i = 0; i < count; ++i) {
            double angle = 6.283185307179586D * (double)i / (double)count + HitParticles.this.random.nextDouble() * 0.3D;
            double spd = (0.8D + HitParticles.this.random.nextDouble() * 0.6D) * (HitParticles.this.speed.getValue() / 1.4D);
            this.particles.add(HitParticles.this.new HitParticle(position.field_1352, position.field_1351, position.field_1350, Math.cos(angle) * spd, (0.4D + HitParticles.this.random.nextDouble() * 0.5D) * (HitParticles.this.speed.getValue() / 1.4D), Math.sin(angle) * spd, (long)i * 100L));
         }

      }

      public void update(float deltaSeconds) {
         this.levitateTimer += deltaSeconds;
         this.particles.forEach((p) -> {
            p.update(deltaSeconds);
         });
      }

      public void render(class_4587 matrices, class_243 cameraPos) {
         if (HitParticles.this.mc.method_22940() != null) {
            class_2960 texture = HitParticles.this.getTextureForType();
            class_4598 immediate = HitParticles.this.mc.method_22940().method_23000();
            class_1921 renderLayer = TexturedRenderLayers.getTexturedLayer(texture, true);
            class_4588 buffer = immediate.method_73477(renderLayer);
            float levitateOffset;
            if (this.levitateTimer < this.maxLevitateTime) {
               float t = this.levitateTimer / this.maxLevitateTime;
               levitateOffset = (1.0F - (1.0F - t) * (1.0F - t)) * this.maxLevitateHeight;
            } else {
               levitateOffset = 0.0F;
            }

            Iterator var28 = this.particles.iterator();

            while(var28.hasNext()) {
               HitParticles.HitParticle particle = (HitParticles.HitParticle)var28.next();
               if (!particle.isDead()) {
                  double renderX = particle.x - cameraPos.field_1352;
                  double renderY = particle.y + (double)levitateOffset - cameraPos.field_1351;
                  double renderZ = particle.z - cameraPos.field_1350;
                  matrices.method_22903();
                  matrices.method_22904(renderX, renderY, renderZ);
                  matrices.method_22907(HitParticles.this.mc.field_1773.method_19418().method_23767());
                  Matrix4f matrix = matrices.method_23760().method_23761();
                  float lifeProgress = particle.life / particle.maxLife;
                  float fadeOutDuration = (float)HitParticles.this.fadeOutTime.getValue();
                  float fadeOutThreshold = fadeOutDuration / particle.maxLife;
                  float alpha;
                  if (lifeProgress < fadeOutThreshold) {
                     alpha = lifeProgress / fadeOutThreshold;
                  } else {
                     alpha = 1.0F;
                  }

                  Color color = HitParticles.this.rainbow.isEnabled() ? HitParticles.this.getRainbowColor(particle.colorOffset) : HitParticles.this.particleColor.getValue();
                  float r = (float)color.getRed() / 255.0F;
                  float g = (float)color.getGreen() / 255.0F;
                  float b = (float)color.getBlue() / 255.0F;
                  float size = (float)HitParticles.this.particleSize.getValue() * Math.min(alpha, lifeProgress);
                  if (HitParticles.this.glow.isEnabled()) {
                     float glowSize = size * (1.0F + (float)HitParticles.this.glowIntensity.getValue());
                     float glowAlpha = alpha * 0.3F;
                     this.renderQuad(buffer, matrix, glowSize, r, g, b, glowAlpha);
                  }

                  this.renderQuad(buffer, matrix, size, r, g, b, alpha);
                  matrices.method_22909();
               }
            }

            immediate.method_22994(renderLayer);
         }
      }

      private void renderQuad(class_4588 buffer, Matrix4f matrix, float size, float r, float g, float b, float a) {
         float half = size / 2.0F;
         buffer.method_22918(matrix, -half, -half, 0.0F).method_22915(r, g, b, a).method_22913(0.0F, 1.0F);
         buffer.method_22918(matrix, -half, half, 0.0F).method_22915(r, g, b, a).method_22913(0.0F, 0.0F);
         buffer.method_22918(matrix, half, half, 0.0F).method_22915(r, g, b, a).method_22913(1.0F, 0.0F);
         buffer.method_22918(matrix, -half, -half, 0.0F).method_22915(r, g, b, a).method_22913(0.0F, 1.0F);
         buffer.method_22918(matrix, half, half, 0.0F).method_22915(r, g, b, a).method_22913(1.0F, 0.0F);
         buffer.method_22918(matrix, half, -half, 0.0F).method_22915(r, g, b, a).method_22913(1.0F, 1.0F);
      }

      public boolean isDead() {
         return this.particles.stream().allMatch(HitParticles.HitParticle::isDead);
      }
   }

   @Environment(EnvType.CLIENT)
   private class HitParticle {
      double x;
      double y;
      double z;
      double vx;
      double vy;
      double vz;
      float life;
      float maxLife;
      long colorOffset;

      public HitParticle(double x, double y, double z, double vx, double vy, double vz, long colorOffset) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.vx = vx;
         this.vy = vy;
         this.vz = vz;
         this.maxLife = (float)HitParticles.this.particleLifespan.getValue();
         this.life = this.maxLife;
         this.colorOffset = colorOffset;
      }

      public void update(float deltaSeconds) {
         this.vy -= 0.5D * (double)deltaSeconds;
         this.vx *= Math.pow(0.98D, (double)(deltaSeconds * 20.0F));
         this.vz *= Math.pow(0.98D, (double)(deltaSeconds * 20.0F));
         this.x += this.vx * (double)deltaSeconds;
         this.y += this.vy * (double)deltaSeconds;
         this.z += this.vz * (double)deltaSeconds;
         if (HitParticles.this.groundCollision.isEnabled() && HitParticles.this.mc.field_1687 != null) {
            int var10002 = (int)this.x;
            int groundY = HitParticles.this.mc.field_1687.method_8624(class_2903.field_13197, var10002, (int)this.z);
            if (this.y <= (double)groundY) {
               this.y = (double)groundY;
               this.vy = Math.abs(this.vy) * (double)((float)HitParticles.this.bounciness.getValue());
               if (this.vy < 0.05D) {
                  this.vy = 0.0D;
                  this.vx *= 0.8D;
                  this.vz *= 0.8D;
               }
            }
         }

         this.life -= deltaSeconds;
      }

      public boolean isDead() {
         return this.life <= 0.0F;
      }
   }
}
