package com.slither.cyemer.module.implementation.render;

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
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_4587;

@Environment(EnvType.CLIENT)
public class Effectesp extends Module {
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
   private final EffectRenderer renderer = new EffectRenderer();
   private final Map<Integer, Long> lastSeen = new HashMap();

   public Effectesp() {
      super("EffectESP", "Effect visuals for nearby players", Category.RENDER);
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
   }

   public void onWorldRender(class_4587 matrices, float tickDelta) {
      if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
         long now = System.currentTimeMillis();
         class_243 cameraPos = this.mc.field_1773.method_19418().method_71156();
         TextureTheme currentTheme = TextureTheme.fromString(this.theme.getCurrentMode());
         Color currentColor = this.rainbow.isEnabled() ? EffectRenderer.getRainbowColor(now) : this.color.getValue();
         String selectedEffect = this.effect.getCurrentMode();
         long holdMs = (long)(this.holdTime.getValue() * 1000.0D);
         if ("Overlay".equals(selectedEffect)) {
            selectedEffect = "Pulse";
         }

         Iterator var11 = this.mc.field_1687.method_18456().iterator();

         while(true) {
            class_1657 player;
            class_243 playerPos;
            double distance;
            Long seenAt;
            do {
               do {
                  if (!var11.hasNext()) {
                     long cutoff = now - holdMs;
                     this.lastSeen.entrySet().removeIf((entry) -> {
                        return (Long)entry.getValue() < cutoff;
                     });
                     return;
                  }

                  player = (class_1657)var11.next();
               } while(!this.shouldRenderPlayer(player));

               playerPos = player.method_30950(tickDelta);
               distance = cameraPos.method_1022(playerPos);
               boolean inRange = distance <= this.maxDistance.getValue();
               if (inRange) {
                  this.lastSeen.put(player.method_5628(), now);
                  break;
               }

               seenAt = (Long)this.lastSeen.get(player.method_5628());
            } while(seenAt == null || now - seenAt > holdMs);

            if (!(distance > this.maxDistance.getValue() * 1.5D)) {
               EffectRenderContext context = new EffectRenderContext(player, selectedEffect, currentColor, this.opaque.isEnabled() ? 1.0F : 0.6F, now, this.speed.getValue(), this.scale.getValue(), EffectRenderer.calculateLOD(distance, (int)this.quality.getValue()), this.glow.isEnabled(), this.glowSize.getValue(), this.glowOpacity.getValue(), this.opaque.isEnabled(), currentTheme);
               this.renderer.render(matrices, cameraPos, playerPos, context);
            }
         }
      }
   }

   private boolean shouldRenderPlayer(class_1657 player) {
      return player != null && player != this.mc.field_1724 && player.method_5805() && !player.method_7325() && !player.method_6113();
   }
}
