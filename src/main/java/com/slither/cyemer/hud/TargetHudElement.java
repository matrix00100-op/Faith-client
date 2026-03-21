package com.slither.cyemer.hud;

import com.slither.cyemer.module.BooleanSetting;
import com.slither.cyemer.module.Setting;
import com.slither.cyemer.module.SliderSetting;
import com.slither.cyemer.util.Renderer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_239;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3966;
import net.minecraft.class_640;

@Environment(EnvType.CLIENT)
public class TargetHudElement extends HUDElement {
   private final BooleanSetting showHealth = new BooleanSetting("Show Health", true);
   public final BooleanSetting showSkin = new BooleanSetting("Show Skin", true);
   private final BooleanSetting showTotemPops = new BooleanSetting("Show Totem Pops", true);
   private final SliderSetting disappearDelay = new SliderSetting("Disappear Delay", 5.0D, 0.0D, 10.0D, 1);
   private final SliderSetting bgOpacity = new SliderSetting("BG Opacity", 0.56D, 0.0D, 1.0D, 2);
   private final SliderSetting cornerRadius = new SliderSetting("Corner Radius", 8.0D, 0.0D, 20.0D, 1);
   private final BooleanSetting glowEffect = new BooleanSetting("Glow Effect", true);
   private final SliderSetting glowSize = new SliderSetting("Glow Size", 12.0D, 0.0D, 30.0D, 1);
   private final SliderSetting glowIntensity = new SliderSetting("Glow Intensity", 0.6D, 0.0D, 1.0D, 2);
   private final BooleanSetting damageFlash = new BooleanSetting("Damage Flash", true);
   private final BooleanSetting particleEffect = new BooleanSetting("Particle Effect", true);
   private final SliderSetting particleCount = new SliderSetting("Particle Count", 5.0D, 1.0D, 15.0D, 0);
   private final SliderSetting particleLifespan = new SliderSetting("Particle Lifespan", 2.75D, 1.0D, 5.0D, 2);
   private final BooleanSetting popOnDamage = new BooleanSetting("Pop on Damage", true);
   private final SliderSetting popStrength = new SliderSetting("Pop Strength", 0.15D, 0.0D, 0.5D, 2);
   private final BooleanSetting headShake = new BooleanSetting("Head Shake", true);
   private final SliderSetting shakeIntensity = new SliderSetting("Shake Intensity", 3.0D, 0.0D, 10.0D, 1);
   private class_1309 lastTarget = null;
   private long lastTargetTime = 0L;
   private float lastHealth = 0.0F;
   private long lastDamageTime = 0L;
   private float damagePopAnimation = 0.0F;
   private float shakeAnimation = 0.0F;
   private final List<TargetHudElement.Particle> particles = new ArrayList();
   private long lastParticleUpdateTime = System.currentTimeMillis();
   private static final int MAX_PARTICLES = 50;

   public TargetHudElement(String name, double defaultX, double defaultY) {
      super(name, defaultX, defaultY);
   }

   public BooleanSetting getShowHealth() {
      return this.showHealth;
   }

   public BooleanSetting getShowSkin() {
      return this.showSkin;
   }

   public BooleanSetting getShowTotemPops() {
      return this.showTotemPops;
   }

   public SliderSetting getDisappearDelay() {
      return this.disappearDelay;
   }

   public SliderSetting getBgOpacity() {
      return this.bgOpacity;
   }

   public List<Setting> getSettings() {
      List<Setting> settings = new ArrayList();
      settings.add(this.showHealth);
      settings.add(this.showSkin);
      settings.add(this.showTotemPops);
      settings.add(this.disappearDelay);
      settings.add(this.bgOpacity);
      settings.add(this.cornerRadius);
      settings.add(this.glowEffect);
      settings.add(this.glowSize);
      settings.add(this.glowIntensity);
      settings.add(this.damageFlash);
      settings.add(this.particleEffect);
      settings.add(this.particleCount);
      settings.add(this.particleLifespan);
      settings.add(this.popOnDamage);
      settings.add(this.popStrength);
      settings.add(this.headShake);
      settings.add(this.shakeIntensity);
      return settings;
   }

   public void render(class_332 context, float delta) {
      class_310 mc = class_310.method_1551();
      class_327 textRenderer = mc.field_1772;
      if (mc.field_1755 != null) {
         this.renderPlaceholder(context, textRenderer, mc);
      } else {
         class_1309 currentTarget = null;
         class_239 var7 = mc.field_1765;
         if (var7 instanceof class_3966) {
            class_3966 hitResult = (class_3966)var7;
            if (hitResult.method_17782() instanceof class_1309) {
               currentTarget = (class_1309)hitResult.method_17782();
            }
         }

         if (currentTarget != null && currentTarget.method_5805()) {
            this.lastTarget = currentTarget;
            this.lastTargetTime = System.currentTimeMillis();
         }

         if (this.lastTarget != null) {
            long timeSinceLastSeen = System.currentTimeMillis() - this.lastTargetTime;
            boolean shouldRender = this.disappearDelay.getValue() == 0.0D && this.lastTarget.method_5805() || this.disappearDelay.getValue() > 0.0D && (double)timeSinceLastSeen < this.disappearDelay.getValue() * 1000.0D;
            if (shouldRender) {
               this.renderInGame(context, textRenderer, this.lastTarget, delta, mc);
            } else {
               this.setWidth(0.0D);
               this.setHeight(0.0D);
            }
         } else {
            this.setWidth(0.0D);
            this.setHeight(0.0D);
         }

      }
   }

   private void renderInGame(class_332 context, class_327 textRenderer, class_1309 entity, float delta, class_310 mc) {
      int padding = true;
      int gap = true;
      int headSize = this.showSkin.isEnabled() ? 32 : 0;
      float currentHealth = entity.method_6032();
      boolean tookDamage = this.lastHealth - currentHealth > 0.01F;
      float particleX;
      float flashIntensity;
      if (tookDamage) {
         this.lastDamageTime = System.currentTimeMillis();
         if (this.popOnDamage.isEnabled()) {
            this.damagePopAnimation = 1.0F;
         }

         if (this.headShake.isEnabled()) {
            this.shakeAnimation = 1.0F;
         }

         if (this.particleEffect.isEnabled() && this.particles.size() < 50) {
            float baseX = (float)this.getX();
            float baseY = (float)this.getY();
            particleX = baseX + 8.0F + (this.showSkin.isEnabled() ? (float)headSize / 2.0F : 0.0F);
            flashIntensity = baseY + 8.0F + (this.showSkin.isEnabled() ? (float)headSize / 2.0F : 0.0F);
            int count = (int)this.particleCount.getValue();
            int allowedCount = Math.min(count, 50 - this.particles.size());

            for(int i = 0; i < allowedCount; ++i) {
               this.particles.add(new TargetHudElement.Particle(particleX, flashIntensity, this.particleLifespan.getValue()));
            }
         }
      }

      this.lastHealth = currentHealth;
      long currentTime = System.currentTimeMillis();
      particleX = (float)(currentTime - this.lastDamageTime) / 1000.0F;
      flashIntensity = Math.max(0.0F, 1.0F - particleX / 0.5F);
      float particleDeltaTime = (float)(currentTime - this.lastParticleUpdateTime) / 1000.0F;
      this.lastParticleUpdateTime = currentTime;
      particleDeltaTime = Math.min(particleDeltaTime, 0.1F);
      if (this.damagePopAnimation > 0.0F) {
         this.damagePopAnimation = Math.max(0.0F, this.damagePopAnimation - delta * 4.0F);
      }

      if (this.shakeAnimation > 0.0F) {
         this.shakeAnimation = Math.max(0.0F, this.shakeAnimation - delta * 6.0F);
      }

      this.particles.removeIf((px) -> {
         px.update(particleDeltaTime);
         return px.isDead();
      });
      List<String> lines = new ArrayList();
      lines.add(entity.method_5477().getString());
      if (this.showHealth.isEnabled()) {
         lines.add(String.format("Health: %.1f/%.1f", entity.method_6032(), entity.method_6063()));
      }

      if (this.showTotemPops.isEnabled() && entity instanceof class_1657) {
         int pops = TotemPopManager.getInstance().getPopCount(entity);
         if (pops > 0) {
            lines.add(String.format("Pops: %d", pops));
         }
      }

      float maxTextWidth = 0.0F;
      float nameFontSize = 16.0F;
      float infoFontSize = 13.0F;

      int contentWidth;
      for(contentWidth = 0; contentWidth < lines.size(); ++contentWidth) {
         float fontSize = contentWidth == 0 ? nameFontSize : infoFontSize;
         float lineWidth = Renderer.get().getTextWidth((String)lines.get(contentWidth), fontSize);
         maxTextWidth = Math.max(maxTextWidth, lineWidth);
      }

      contentWidth = (int)maxTextWidth;
      if (this.showSkin.isEnabled()) {
         contentWidth = Math.max(headSize, (int)maxTextWidth) + headSize + 6;
      }

      int totalWidth = contentWidth + 16;
      int textBlockHeight = (int)(nameFontSize + 3.0F + (float)(lines.size() - 1) * (infoFontSize + 3.0F));
      int skinHeight = this.showSkin.isEnabled() ? headSize : 0;
      int contentHeight = Math.max(skinHeight, textBlockHeight);
      int totalHeight = contentHeight + 16;
      this.setWidth((double)totalWidth);
      this.setHeight((double)totalHeight);
      int screenWidth = mc.method_22683().method_4486();
      int screenHeight = mc.method_22683().method_4502();
      float pixelRatio = (float)mc.method_22683().method_4495();
      float x = (float)this.getX();
      float y = (float)this.getY();
      Color bgColor = new Color(0, 0, 0, (int)(this.bgOpacity.getValue() * 255.0D));
      if (this.damageFlash.isEnabled() && flashIntensity > 0.0F) {
         int red = Math.min(255, bgColor.getRed() + (int)(100.0F * flashIntensity));
         bgColor = new Color(red, bgColor.getGreen(), bgColor.getBlue(), bgColor.getAlpha());
      }

      float popScale = 1.0F;
      float centerX;
      float centerY;
      if (this.popOnDamage.isEnabled() && this.damagePopAnimation > 0.0F) {
         centerX = this.damagePopAnimation;
         centerY = 1.0F - (1.0F - centerX) * (1.0F - centerX);
         popScale = 1.0F + centerY * (float)this.popStrength.getValue();
      }

      centerX = x + (float)(totalWidth / 2);
      centerY = y + (float)(totalHeight / 2);
      int contentY = (int)y + 8;
      float shakeOffsetX = 0.0F;
      float shakeOffsetY = 0.0F;
      if (this.headShake.isEnabled() && this.shakeAnimation > 0.0F) {
         float shakeAmount = this.shakeAnimation * (float)this.shakeIntensity.getValue();
         shakeOffsetX = (float)(Math.sin((double)(this.shakeAnimation * 40.0F)) * (double)shakeAmount);
         shakeOffsetY = (float)(Math.cos((double)(this.shakeAnimation * 40.0F)) * (double)shakeAmount);
      }

      Color borderColor = new Color(255, 255, 255, 80);
      if (this.damageFlash.isEnabled() && flashIntensity > 0.0F) {
         borderColor = new Color(255, 50, 50, (int)(200.0F * flashIntensity));
      }

      int skinX = (int)x + 8;
      float finalSkinX = (float)skinX + shakeOffsetX + (float)headSize / 2.0F * (1.0F - popScale);
      float finalSkinY = (float)contentY + shakeOffsetY + (float)headSize / 2.0F * (1.0F - popScale);
      float finalHeadSize = (float)headSize * popScale;
      if (Renderer.get().beginFrame((float)screenWidth, (float)screenHeight, pixelRatio)) {
         try {
            Color textColor;
            float fontSize;
            if (this.particleEffect.isEnabled() && !this.particles.isEmpty()) {
               Iterator var45 = this.particles.iterator();

               while(var45.hasNext()) {
                  TargetHudElement.Particle p = (TargetHudElement.Particle)var45.next();
                  float pAlpha = p.life / p.maxLife;
                  int brightness = (int)(255.0F * pAlpha);
                  textColor = new Color(255, brightness / 2, brightness / 4, (int)(255.0F * pAlpha));
                  fontSize = p.size * 2.0F;
                  Renderer.get().setFontBlur(fontSize);
                  Renderer.get().drawRect(context, p.x - fontSize / 2.0F, p.y - fontSize / 2.0F, fontSize, fontSize, new Color(255, 100, 50, (int)(128.0F * pAlpha)));
                  Renderer.get().setFontBlur(0.0F);
                  Renderer.get().drawRect(context, p.x - p.size / 2.0F, p.y - p.size / 2.0F, p.size, p.size, textColor);
               }
            }

            Renderer.get().save();
            Renderer.get().translate(centerX, centerY);
            Renderer.get().scale(popScale, popScale);
            Renderer.get().translate(-centerX, -centerY);
            int i;
            if (this.glowEffect.isEnabled()) {
               int numGlowLayers = 6;
               float radius = (float)this.cornerRadius.getValue();

               for(i = numGlowLayers; i >= 1; --i) {
                  float layerProgress = (float)i / (float)numGlowLayers;
                  float layerSize = (float)this.glowSize.getValue() * layerProgress;
                  fontSize = (float)this.glowIntensity.getValue() * (1.0F - layerProgress) / (float)numGlowLayers;
                  Color glowColor = new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), (int)(fontSize * 255.0F));
                  Renderer.get().drawRoundedRect(context, x - layerSize, y - layerSize, (float)totalWidth + layerSize * 2.0F, (float)totalHeight + layerSize * 2.0F, radius + layerSize, glowColor);
               }
            }

            Renderer.get().drawRoundedRect(context, x, y, (float)totalWidth, (float)totalHeight, (float)this.cornerRadius.getValue(), bgColor);
            Renderer.get().drawRoundedRectOutline(context, x, y, (float)totalWidth, (float)totalHeight, (float)this.cornerRadius.getValue(), 2.0F, borderColor);
            int textX = (int)x + 8;
            if (this.showSkin.isEnabled()) {
               textX += headSize + 6;
            }

            int textY = contentY;
            i = 0;

            while(true) {
               if (i >= lines.size()) {
                  Renderer.get().restore();
                  break;
               }

               String line = (String)lines.get(i);
               textColor = i == 0 ? new Color(255, 255, 255, 255) : new Color(200, 200, 200, 255);
               fontSize = i == 0 ? nameFontSize : infoFontSize;
               Renderer.get().drawText(context, line, (float)textX, (float)textY, fontSize, textColor, true);
               textY += (int)fontSize + 3;
               ++i;
            }
         } finally {
            Renderer.get().endFrame();
         }

         if (this.showSkin.isEnabled() && entity instanceof class_1657) {
            class_640 entry = mc.method_1562().method_2871(entity.method_5667());
            if (entry != null) {
               class_2960 skinTexture = entry.method_52810().comp_1626().comp_3627();
               context.method_25302(class_10799.field_56883, skinTexture, (int)finalSkinX, (int)finalSkinY, 8.0F, 8.0F, (int)finalHeadSize, (int)finalHeadSize, 8, 8, 64, 64);
               context.method_25302(class_10799.field_56883, skinTexture, (int)finalSkinX, (int)finalSkinY, 40.0F, 8.0F, (int)finalHeadSize, (int)finalHeadSize, 8, 8, 64, 64);
               if (!Renderer.get().beginFrame((float)screenWidth, (float)screenHeight, pixelRatio)) {
                  return;
               }

               try {
                  Renderer.get().drawRectOutline(context, finalSkinX, finalSkinY, finalHeadSize, finalHeadSize, 2.0F, new Color(255, 255, 255, 100));
               } finally {
                  Renderer.get().endFrame();
               }
            }
         }

      }
   }

   private void renderPlaceholder(class_332 context, class_327 textRenderer, class_310 mc) {
      String text = "[Target HUD]";
      int width = textRenderer.method_1727(text) + 16;
      Objects.requireNonNull(textRenderer);
      int height = 9 + 16;
      this.setWidth((double)width);
      this.setHeight((double)height);
      int screenWidth = mc.method_22683().method_4486();
      int screenHeight = mc.method_22683().method_4502();
      float pixelRatio = (float)mc.method_22683().method_4495();
      if (Renderer.get().beginFrame((float)screenWidth, (float)screenHeight, pixelRatio)) {
         try {
            float x = (float)this.getX();
            float y = (float)this.getY();
            Renderer.get().drawRoundedRect(context, x, y, (float)width, (float)height, (float)this.cornerRadius.getValue(), new Color(0, 0, 0, (int)(this.bgOpacity.getValue() * 255.0D)));
            Renderer.get().drawRoundedRectOutline(context, x, y, (float)width, (float)height, (float)this.cornerRadius.getValue(), 2.0F, new Color(255, 255, 255, 80));
            Renderer.get().drawCenteredText(context, text, x + (float)(width / 2), y + (float)(height / 2), 14.0F, Color.WHITE, true);
         } finally {
            Renderer.get().endFrame();
         }

      }
   }

   @Environment(EnvType.CLIENT)
   private static class Particle {
      float x;
      float y;
      float vx;
      float vy;
      float size;
      float life;
      float maxLife;

      public Particle(float x, float y, double maxLifespan) {
         this.x = x;
         this.y = y;
         double angle = Math.random() * 3.141592653589793D * 2.0D;
         float speed = 40.0F + (float)(Math.random() * 30.0D);
         this.vx = (float)Math.cos(angle) * speed;
         this.vy = (float)Math.sin(angle) * speed - 20.0F;
         this.size = 3.0F + (float)(Math.random() * 3.0D);
         this.maxLife = (float)maxLifespan + (float)(Math.random() * 0.5D);
         this.life = this.maxLife;
      }

      public void update(float deltaSeconds) {
         this.x += this.vx * deltaSeconds;
         this.y += this.vy * deltaSeconds;
         this.vy += 60.0F * deltaSeconds;
         this.vx *= 1.0F - 0.5F * deltaSeconds;
         this.life -= deltaSeconds;
      }

      public boolean isDead() {
         return this.life <= 0.0F;
      }
   }
}
