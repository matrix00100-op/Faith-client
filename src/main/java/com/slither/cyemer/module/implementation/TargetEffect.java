package com.slither.cyemer.module.implementation;

import com.slither.cyemer.module.BooleanSetting;
import com.slither.cyemer.module.Category;
import com.slither.cyemer.module.ColorSetting;
import com.slither.cyemer.module.ModeSetting;
import com.slither.cyemer.module.Module;
import com.slither.cyemer.module.SliderSetting;
import com.slither.cyemer.module.implementation.targeteffect.EffectRenderContext;
import com.slither.cyemer.module.implementation.targeteffect.EffectRenderer;
import com.slither.cyemer.module.implementation.targeteffect.TextureTheme;
import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10055;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_591;

@Environment(EnvType.CLIENT)
public class TargetEffect extends Module {
   private final ModeSetting effect = new ModeSetting("Effect", new String[]{"Spinning Spheres", "Orbit Ring", "Pulse", "Helix", "Galaxy", "Tornado", "Rings", "Spiral", "Lightning", "Shockwave", "Scanlines", "Goofball", "Overlay"});
   private final ModeSetting theme = new ModeSetting("Theme", TextureTheme.getDisplayNames());
   private final ColorSetting color = new ColorSetting("Color", new Color(0, 255, 230));
   private final BooleanSetting opaque = new BooleanSetting("Opaque", false);
   private final BooleanSetting rainbow = new BooleanSetting("Rainbow", false);
   private final SliderSetting speed = new SliderSetting("Speed", 1.0D, 0.1D, 5.0D, 1);
   private final SliderSetting scale = new SliderSetting("Scale", 1.0D, 0.5D, 3.0D, 1);
   private final SliderSetting quality = new SliderSetting("Quality", 32.0D, 16.0D, 128.0D, 1);
   private final BooleanSetting glow = new BooleanSetting("Glow", true);
   private final SliderSetting glowSize = new SliderSetting("Glow Size", 0.05D, 0.001D, 0.1D, 3);
   private final SliderSetting glowOpacity = new SliderSetting("Glow Opacity", 0.6D, 0.1D, 1.0D, 2);
   private final SliderSetting maxDistance = new SliderSetting("Max Distance", 64.0D, 16.0D, 128.0D, 1);
   private final SliderSetting holdTime = new SliderSetting("Hold Time", 15.0D, 5.0D, 30.0D, 0);
   private class_1657 lastTarget = null;
   private long lastTargetTime = 0L;
   private final EffectRenderer renderer;
   private final Map<Integer, TargetEffect.OverlayData> overlayCache = new HashMap();

   public TargetEffect() {
      super("TargetEffect", "Visual effects on your combat target", Category.RENDER);
      this.addSetting(this.effect);
      this.addSetting(this.theme);
      this.addSetting(this.color);
      this.addSetting(this.rainbow);
      this.addSetting(this.opaque);
      this.addSetting(this.speed);
      this.addSetting(this.scale);
      this.addSetting(this.quality);
      this.addSetting(this.glow);
      this.addSetting(this.glowSize);
      this.addSetting(this.glowOpacity);
      this.addSetting(this.maxDistance);
      this.addSetting(this.holdTime);
      this.renderer = new EffectRenderer();
   }

   public void onWorldRender(class_4587 matrices, float tickDelta) {
      if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
         if ("Overlay".equals(this.effect.getCurrentMode())) {
            this.updateTarget();
         } else {
            class_1657 currentTarget = this.findTarget();
            if (currentTarget != null) {
               this.lastTarget = currentTarget;
               this.lastTargetTime = System.currentTimeMillis();
            }

            long currentTime = System.currentTimeMillis();
            long holdTimeMs = (long)(this.holdTime.getValue() * 1000.0D);
            class_1657 targetToRender = null;
            if (this.lastTarget != null && this.lastTarget.method_5805()) {
               if (currentTime - this.lastTargetTime <= holdTimeMs) {
                  targetToRender = this.lastTarget;
               } else {
                  this.lastTarget = null;
               }
            }

            if (targetToRender != null) {
               class_243 cameraPos = this.mc.field_1773.method_19418().method_71156();
               class_243 playerPos = targetToRender.method_30950(tickDelta);
               double distance = cameraPos.method_1022(playerPos);
               if (!(distance > this.maxDistance.getValue())) {
                  TextureTheme currentTheme = TextureTheme.fromString(this.theme.getCurrentMode());
                  EffectRenderContext ctx = new EffectRenderContext(targetToRender, this.effect.getCurrentMode(), this.rainbow.isEnabled() ? EffectRenderer.getRainbowColor(System.currentTimeMillis()) : this.color.getValue(), this.opaque.isEnabled() ? 1.0F : 0.6F, System.currentTimeMillis(), this.speed.getValue(), this.scale.getValue(), EffectRenderer.calculateLOD(distance, (int)this.quality.getValue()), this.glow.isEnabled(), this.glowSize.getValue(), this.glowOpacity.getValue(), this.opaque.isEnabled(), currentTheme);
                  this.renderer.render(matrices, cameraPos, playerPos, ctx);
               }
            }
         }
      }
   }

   public void renderPlayerOverlay(class_10055 state, class_4587 matrices, class_4597 vertexConsumers, class_591 model, int light) {
      if ("Overlay".equals(this.effect.getCurrentMode())) {
         TargetEffect.OverlayData data = (TargetEffect.OverlayData)this.overlayCache.get(state.field_53528);
         if (data != null && data.shouldRender(System.currentTimeMillis())) {
            TextureTheme currentTheme = TextureTheme.fromString(this.theme.getCurrentMode());
            EffectRenderContext ctx = new EffectRenderContext(data.player, this.effect.getCurrentMode(), this.rainbow.isEnabled() ? EffectRenderer.getRainbowColor(System.currentTimeMillis()) : this.color.getValue(), this.opaque.isEnabled() ? 1.0F : 0.6F, System.currentTimeMillis(), this.speed.getValue(), this.scale.getValue(), (int)this.quality.getValue(), this.glow.isEnabled(), this.glowSize.getValue(), this.glowOpacity.getValue(), this.opaque.isEnabled(), currentTheme, vertexConsumers, model, state, light);
            this.renderer.render(matrices, (class_243)null, (class_243)null, ctx);
         }
      }
   }

   public boolean shouldHideModel(class_10055 state) {
      if (!"Overlay".equals(this.effect.getCurrentMode())) {
         return false;
      } else {
         TargetEffect.OverlayData data = (TargetEffect.OverlayData)this.overlayCache.get(state.field_53528);
         return data != null && data.shouldRender(System.currentTimeMillis());
      }
   }

   private void updateTarget() {
      class_1657 currentTarget = this.findTarget();
      if (currentTarget != null) {
         this.lastTarget = currentTarget;
         this.lastTargetTime = System.currentTimeMillis();
         this.overlayCache.put(currentTarget.method_5628(), new TargetEffect.OverlayData(currentTarget, System.currentTimeMillis()));
      }

      long currentTime = System.currentTimeMillis();
      this.overlayCache.entrySet().removeIf((entry) -> {
         return !((TargetEffect.OverlayData)entry.getValue()).shouldRender(currentTime);
      });
   }

   private class_1657 findTarget() {
      class_1657 closest;
      if (this.mc.field_1692 instanceof class_1657) {
         closest = (class_1657)this.mc.field_1692;
         if (closest != this.mc.field_1724 && closest.method_5805()) {
            return closest;
         }
      }

      closest = null;
      double closestDist = Double.MAX_VALUE;
      Iterator var4 = this.mc.field_1687.method_18456().iterator();

      while(var4.hasNext()) {
         class_1657 player = (class_1657)var4.next();
         if (player != this.mc.field_1724 && player.method_5805() && player.field_6235 > 0) {
            double dist = this.mc.field_1724.method_5858(player);
            if (dist < closestDist && dist < 36.0D) {
               closest = player;
               closestDist = dist;
            }
         }
      }

      return closest;
   }

   @Environment(EnvType.CLIENT)
   private class OverlayData {
      final class_1657 player;
      final long timestamp;

      OverlayData(class_1657 player, long timestamp) {
         this.player = player;
         this.timestamp = timestamp;
      }

      boolean shouldRender(long currentTime) {
         long holdTimeMs = (long)(TargetEffect.this.holdTime.getValue() * 1000.0D);
         return this.player.method_5805() && currentTime - this.timestamp <= holdTimeMs;
      }
   }
}
