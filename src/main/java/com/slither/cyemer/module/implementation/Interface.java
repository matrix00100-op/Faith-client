package com.slither.cyemer.module.implementation;

import com.slither.cyemer.mixin.HandledScreenAccessor;
import com.slither.cyemer.module.BooleanSetting;
import com.slither.cyemer.module.Category;
import com.slither.cyemer.module.ColorSetting;
import com.slither.cyemer.module.Module;
import com.slither.cyemer.module.SliderSetting;
import com.slither.cyemer.util.Renderer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.class_11908;
import net.minecraft.class_1661;
import net.minecraft.class_1713;
import net.minecraft.class_1723;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_1836;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_490;
import net.minecraft.class_1792.class_9635;

@Environment(EnvType.CLIENT)
public class Interface extends Module {
   public static Interface INSTANCE;
   private final BooleanSetting customHotbar = new BooleanSetting("Custom Hotbar", true);
   private final BooleanSetting customInventory = new BooleanSetting("Custom Inventory", true);
   private final BooleanSetting blur = new BooleanSetting("Blur", true);
   private final SliderSetting blurRadius = new SliderSetting("Blur Radius", 10.0D, 0.0D, 30.0D, 1);
   private final SliderSetting backgroundAlpha = new SliderSetting("Background Alpha", 200.0D, 0.0D, 255.0D, 0);
   private final SliderSetting cornerRadius = new SliderSetting("Corner Radius", 6.0D, 0.0D, 15.0D, 1);
   private final ColorSetting backgroundColor = new ColorSetting("Background Color", new Color(18, 18, 22, 200));
   private final ColorSetting selectedSlotColor = new ColorSetting("Selected Slot", new Color(100, 150, 255, 150));
   private final ColorSetting accentColor = new ColorSetting("Accent Color", new Color(120, 160, 255, 200));
   private final BooleanSetting smoothSlide = new BooleanSetting("Smooth Selection", true);
   private final BooleanSetting inventoryAnimation = new BooleanSetting("Inventory Animation", true);
   private final SliderSetting animationSpeed = new SliderSetting("Animation Speed", 0.08D, 0.02D, 0.2D, 2);
   private final BooleanSetting glowEffect = new BooleanSetting("Glow Effect", true);
   private final SliderSetting glowIntensity = new SliderSetting("Glow Intensity", 0.5D, 0.0D, 1.0D, 2);
   private final BooleanSetting particles = new BooleanSetting("Selection Particles", true);
   private final BooleanSetting gradientBorder = new BooleanSetting("Gradient Border", true);
   private final BooleanSetting showPlayerModel = new BooleanSetting("Show Player Model", true);
   private final BooleanSetting invText = new BooleanSetting("Inv Text", true);
   private static final int SLOT_SIZE = 18;
   private static final int SLOT_SPACING = 2;
   private static final int HOTBAR_SLOTS = 9;
   public boolean shouldCancelVanillaHotbar = false;
   public boolean shouldCancelVanillaInventory = false;
   private float currentSlideX = 0.0F;
   private boolean firstFrame = true;
   private float slideProgress = 0.0F;
   private long lastRenderTime = 0L;
   private long lastFrameTime = 0L;
   private static final int P_X = 0;
   private static final int P_Y = 1;
   private static final int P_VX = 2;
   private static final int P_VY = 3;
   private static final int P_LIFE = 4;
   private static final int P_MAX_LIFE = 5;
   private static final int P_R = 6;
   private static final int P_G = 7;
   private static final int P_B = 8;
   private final List<float[]> particleList = new ArrayList();

   public Interface() {
      super("Interface", "Enrage Lwes", Category.RENDER);
      INSTANCE = this;
      this.addSetting(this.customHotbar);
      this.addSetting(this.customInventory);
      this.addSetting(this.blur);
      this.addSetting(this.blurRadius);
      this.addSetting(this.backgroundAlpha);
      this.addSetting(this.cornerRadius);
      this.addSetting(this.backgroundColor);
      this.addSetting(this.selectedSlotColor);
      this.addSetting(this.accentColor);
      this.addSetting(this.smoothSlide);
      this.addSetting(this.inventoryAnimation);
      this.addSetting(this.animationSpeed);
      this.addSetting(this.glowEffect);
      this.addSetting(this.glowIntensity);
      this.addSetting(this.particles);
      this.addSetting(this.gradientBorder);
      this.addSetting(this.showPlayerModel);
      this.addSetting(this.invText);
      ClientTickEvents.END_CLIENT_TICK.register((client) -> {
         this.shouldCancelVanillaInventory = this.isEnabled() && this.customInventory.isEnabled() && client.field_1755 instanceof class_490;
      });
      ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
         if (screen instanceof class_490 && this.isEnabled() && this.customInventory.isEnabled()) {
            this.shouldCancelVanillaInventory = true;
         }

      });
      ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
         if (screen instanceof class_490) {
            this.slideProgress = 0.0F;
         }

      });
   }

   public boolean handleKeyPress(class_490 screen, class_11908 keyInput) {
      if (this.mc.field_1724 != null && this.mc.field_1761 != null) {
         if (!((class_1723)screen.method_17577()).method_34255().method_7960()) {
            return false;
         } else {
            HandledScreenAccessor accessor = (HandledScreenAccessor)screen;
            int mouseX = (int)(this.mc.field_1729.method_1603() * (double)this.mc.method_22683().method_4486() / (double)this.mc.method_22683().method_4480());
            int mouseY = (int)(this.mc.field_1729.method_1604() * (double)this.mc.method_22683().method_4502() / (double)this.mc.method_22683().method_4507());
            class_1735 hoveredSlot = null;
            Iterator var7 = ((class_1723)screen.method_17577()).field_7761.iterator();

            while(var7.hasNext()) {
               class_1735 slot = (class_1735)var7.next();
               if (this.isPointWithinBounds(slot.field_7873, slot.field_7872, 16, 16, (double)mouseX, (double)mouseY, accessor.getX(), accessor.getY())) {
                  hoveredSlot = slot;
                  break;
               }
            }

            if (hoveredSlot != null && hoveredSlot.method_7681()) {
               if (this.mc.field_1690.field_1831.method_1417(keyInput)) {
                  this.mc.field_1761.method_2906(((class_1723)screen.method_17577()).field_7763, hoveredSlot.field_7874, 40, class_1713.field_7791, this.mc.field_1724);
                  return true;
               }

               for(int i = 0; i < 9; ++i) {
                  if (this.mc.field_1690.field_1852[i].method_1417(keyInput)) {
                     this.mc.field_1761.method_2906(((class_1723)screen.method_17577()).field_7763, hoveredSlot.field_7874, i, class_1713.field_7791, this.mc.field_1724);
                     return true;
                  }
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   private void renderHotbar(class_332 context, float tickDelta) {
      if (this.mc.field_1724 != null) {
         int width = this.mc.method_22683().method_4486();
         int height = this.mc.method_22683().method_4502();
         long currentTime = System.currentTimeMillis();
         float frameDelta = Math.min((float)(currentTime - this.lastFrameTime) / 16.666F, 3.0F);
         this.lastFrameTime = currentTime;
         float deltaFrame = (float)(currentTime - this.lastRenderTime) / 10.0F;
         if (deltaFrame > 2.0F) {
            deltaFrame = 1.0F;
         }

         this.lastRenderTime = currentTime;
         if (this.particles.isEnabled()) {
            this.particleList.removeIf(this::isParticleDead);
            Iterator var9 = this.particleList.iterator();

            while(var9.hasNext()) {
               float[] p = (float[])var9.next();
               this.updateParticle(p, frameDelta);
            }
         }

         float totalWidth = 182.0F;
         int startX = (int)(((float)width - totalWidth) / 2.0F);
         int startY = height - 18 - 5;
         int offhandX = startX - 18 - 7;
         float cornerRad = (float)this.cornerRadius.getValue();
         float blurRad = this.blur.isEnabled() ? (float)this.blurRadius.getValue() : 0.0F;
         float globalGlowSize = (float)this.glowIntensity.getValue() * 10.0F;
         float w = (float)this.mc.method_22683().method_4486();
         float h = (float)this.mc.method_22683().method_4502();
         float pixelRatio = (float)this.mc.method_22683().method_4489() / w;
         int selectedSlot;
         float oldSlideX;
         if (Renderer.get().beginFrame(w, h, pixelRatio)) {
            Color bgColor = this.applyAlpha(this.backgroundColor.getValue());
            if (blurRad > 0.0F) {
               this.drawBlurredRect(context, (float)(startX - 4), (float)(startY - 4), totalWidth + 8.0F, 26.0F, cornerRad + 2.0F, blurRad);
            }

            if (this.gradientBorder.isEnabled()) {
               this.drawGradientGlow(context, (float)(startX - 2), (float)(startY - 2), totalWidth + 4.0F, 22.0F, cornerRad, globalGlowSize);
            } else if (this.glowEffect.isEnabled()) {
               this.drawSoftGlow(context, (float)(startX - 2), (float)(startY - 2), totalWidth + 4.0F, 22.0F, cornerRad, this.accentColor.getValue(), globalGlowSize);
            }

            Renderer.get().drawRoundedRect(context, (float)(startX - 2), (float)(startY - 2), totalWidth + 4.0F, 22.0F, cornerRad, bgColor);
            if (blurRad > 0.0F) {
               this.drawBlurredRect(context, (float)(offhandX - 4), (float)(startY - 4), 26.0F, 26.0F, cornerRad + 2.0F, blurRad);
            }

            if (this.gradientBorder.isEnabled()) {
               this.drawGradientGlow(context, (float)(offhandX - 2), (float)(startY - 2), 22.0F, 22.0F, cornerRad, globalGlowSize);
            } else if (this.glowEffect.isEnabled()) {
               this.drawSoftGlow(context, (float)(offhandX - 2), (float)(startY - 2), 22.0F, 22.0F, cornerRad, this.accentColor.getValue(), globalGlowSize);
            }

            Renderer.get().drawRoundedRect(context, (float)(offhandX - 2), (float)(startY - 2), 22.0F, 22.0F, cornerRad, bgColor);
            class_1661 inventory = this.mc.field_1724.method_31548();
            selectedSlot = inventory.method_67532();
            float targetSlideX = (float)(startX + selectedSlot * 20 + 2);
            float pulse;
            int slotX;
            float size;
            if (!this.firstFrame && this.smoothSlide.isEnabled()) {
               oldSlideX = this.currentSlideX;
               this.currentSlideX = class_3532.method_16439(0.3F * deltaFrame, this.currentSlideX, targetSlideX);
               if (this.particles.isEnabled() && Math.abs(oldSlideX - this.currentSlideX) > 0.1F) {
                  pulse = Math.abs(this.currentSlideX - oldSlideX);
                  slotX = (int)(pulse * 0.5F);

                  for(int i = 0; i < Math.min(slotX, 3); ++i) {
                     size = this.currentSlideX + 9.0F + (float)(Math.random() - 0.5D) * 18.0F;
                     float spawnY = (float)startY + 9.0F + (float)(Math.random() - 0.5D) * 18.0F;
                     this.particleList.add(this.createParticle(size, spawnY, this.accentColor.getValue()));
                  }
               }
            } else {
               this.currentSlideX = targetSlideX;
               this.firstFrame = false;
            }

            Color selectColor = this.selectedSlotColor.getValue();
            if (this.glowEffect.isEnabled()) {
               pulse = (float)(0.8D + Math.sin((double)System.currentTimeMillis() / 300.0D) * 0.2D);
               float selectionGlowSize = globalGlowSize * 0.8F;
               Color pulsedColor = new Color(selectColor.getRed(), selectColor.getGreen(), selectColor.getBlue(), (int)((float)selectColor.getAlpha() * pulse));
               this.drawSoftGlow(context, this.currentSlideX - 1.0F, (float)(startY - 1), 20.0F, 20.0F, cornerRad - 1.0F, pulsedColor, selectionGlowSize);
            }

            Renderer.get().drawRoundedRect(context, this.currentSlideX - 1.0F, (float)(startY - 1), 20.0F, 20.0F, cornerRad - 1.0F, selectColor);

            for(int i = 0; i < 9; ++i) {
               slotX = startX + i * 20 + 2;
               Renderer.get().drawRoundedRect(context, (float)slotX, (float)startY, 18.0F, 18.0F, cornerRad - 2.0F, new Color(0, 0, 0, 80));
            }

            Renderer.get().drawRoundedRect(context, (float)offhandX, (float)startY, 18.0F, 18.0F, cornerRad - 2.0F, new Color(0, 0, 0, 80));
            if (this.particles.isEnabled()) {
               Iterator var43 = this.particleList.iterator();

               label98:
               while(true) {
                  float[] p;
                  do {
                     if (!var43.hasNext()) {
                        break label98;
                     }

                     p = (float[])var43.next();
                  } while(p[4] <= 0.0F);

                  float alpha = this.getParticleAlpha(p);
                  size = this.getParticleSize(p);

                  for(int i = 0; i < 3; ++i) {
                     float layerSize = size * (1.0F + (float)i * 0.4F);
                     float layerAlpha = alpha / ((float)i + 1.5F);
                     int finalAlpha = class_3532.method_15340((int)(layerAlpha * 200.0F), 0, 255);
                     Color pColor = new Color((int)p[6], (int)p[7], (int)p[8], finalAlpha);
                     Renderer.get().drawRoundedRect(context, p[0] - layerSize / 2.0F, p[1] - layerSize / 2.0F, layerSize, layerSize, layerSize / 2.0F, pColor);
                  }
               }
            }

            Renderer.get().endFrame();
         }

         class_1661 inventory = this.mc.field_1724.method_31548();

         for(int i = 0; i < 9; ++i) {
            selectedSlot = startX + i * 20 + 2;
            class_1799 stack = inventory.method_5438(i);
            oldSlideX = i == inventory.method_67532() && this.smoothSlide.isEnabled() ? -2.0F : 0.0F;
            if (!stack.method_7960()) {
               this.renderItemStack(context, stack, selectedSlot + 1, (int)((float)startY + oldSlideX + 1.0F));
            }
         }

         class_1799 offhandStack = this.mc.field_1724.method_6079();
         if (!offhandStack.method_7960()) {
            this.renderItemStack(context, offhandStack, offhandX + 1, startY + 1);
         }

      }
   }

   public void renderInventory(class_332 context, int mouseX, int mouseY, float tickDelta, class_490 invScreen) {
      if (this.mc.field_1724 != null) {
         if (this.inventoryAnimation.isEnabled() && this.slideProgress < 1.0F) {
            this.slideProgress += (float)this.animationSpeed.getValue() * tickDelta;
            if (this.slideProgress > 1.0F) {
               this.slideProgress = 1.0F;
            }
         } else if (!this.inventoryAnimation.isEnabled()) {
            this.slideProgress = 1.0F;
         }

         float animProgress = 1.0F - (float)Math.pow((double)(1.0F - this.slideProgress), 3.0D);
         int yOffset = (int)((1.0F - animProgress) * 100.0F);
         HandledScreenAccessor accessor = (HandledScreenAccessor)invScreen;
         int containerX = accessor.getX();
         int containerY = accessor.getY();
         float totalWidth = 176.0F;
         float totalHeight = 166.0F;
         Color bgColor = this.applyAlpha(this.backgroundColor.getValue());
         Color selectColor = this.selectedSlotColor.getValue();
         float cornerRad = (float)this.cornerRadius.getValue();
         float blurRad = this.blur.isEnabled() ? (float)this.blurRadius.getValue() : 0.0F;
         float globalGlowSize = (float)this.glowIntensity.getValue() * 12.0F;
         float w = (float)this.mc.method_22683().method_4486();
         float h = (float)this.mc.method_22683().method_4502();
         float pixelRatio = (float)this.mc.method_22683().method_4489() / w;
         class_1735 hoveredSlot = null;
         int selectedSlot = this.mc.field_1724.method_31548().method_67532();
         if (Renderer.get().beginFrame(w, h, pixelRatio)) {
            float bgX = (float)(containerX - 4);
            float bgY = (float)(containerY - 3 + yOffset);
            float bgW = totalWidth + 6.0F;
            float bgH = totalHeight + 6.0F;
            if (blurRad > 0.0F) {
               this.drawBlurredRect(context, bgX - 4.0F, bgY - 4.0F, bgW + 8.0F, bgH + 8.0F, cornerRad + 2.0F, blurRad);
            }

            if (this.gradientBorder.isEnabled()) {
               this.drawGradientGlow(context, bgX - 2.0F, bgY - 2.0F, bgW + 4.0F, bgH + 4.0F, cornerRad + 1.0F, globalGlowSize);
            } else if (this.glowEffect.isEnabled()) {
               this.drawSoftGlow(context, bgX - 2.0F, bgY - 2.0F, bgW + 4.0F, bgH + 4.0F, cornerRad + 1.0F, this.accentColor.getValue(), globalGlowSize);
            }

            Renderer.get().drawRoundedRect(context, bgX, bgY, bgW, bgH, cornerRad, bgColor);
            if (this.invText.isEnabled()) {
               if (this.glowEffect.isEnabled()) {
                  Renderer.get().drawText(context, "Inventory", bgX + 10.0F, bgY - 15.0F, 14.0F, new Color(this.accentColor.getValue().getRed(), this.accentColor.getValue().getGreen(), this.accentColor.getValue().getBlue(), 100), true);
               }

               Renderer.get().drawText(context, "Inventory", bgX + 10.0F, bgY - 15.0F, 14.0F, Color.WHITE, true);
            }

            int slotX;
            int slotY;
            for(Iterator var27 = ((class_1723)invScreen.method_17577()).field_7761.iterator(); var27.hasNext(); Renderer.get().drawRoundedRect(context, (float)(slotX - 1), (float)(slotY - 1), 18.0F, 18.0F, cornerRad - 2.0F, new Color(0, 0, 0, 120))) {
               class_1735 slot = (class_1735)var27.next();
               slotX = containerX + slot.field_7873;
               slotY = containerY + slot.field_7872 + yOffset;
               boolean isHovered = this.isPointWithinBounds(slot.field_7873, slot.field_7872, 16, 16, (double)mouseX, (double)(mouseY - yOffset), containerX, containerY);
               if (isHovered) {
                  hoveredSlot = slot;
                  if (this.glowEffect.isEnabled()) {
                     this.drawSoftGlow(context, (float)(slotX - 2), (float)(slotY - 2), 20.0F, 20.0F, cornerRad, this.accentColor.getValue(), 8.0F);
                  }

                  Renderer.get().drawRoundedRect(context, (float)(slotX - 2), (float)(slotY - 2), 20.0F, 20.0F, cornerRad - 1.0F, new Color(255, 255, 255, 80));
               }

               if (slot.field_7871 instanceof class_1661 && slot.method_34266() == selectedSlot && slot.method_34266() < 9) {
                  if (this.glowEffect.isEnabled()) {
                     this.drawSoftGlow(context, (float)(slotX - 2), (float)(slotY - 2), 20.0F, 20.0F, cornerRad, selectColor, 8.0F);
                  }

                  Renderer.get().drawRoundedRect(context, (float)(slotX - 1), (float)(slotY - 1), 18.0F, 18.0F, cornerRad - 1.0F, selectColor);
               }
            }

            Renderer.get().endFrame();
         }

         context.method_51448().pushMatrix();
         context.method_51448().translate(0.0F, (float)yOffset);
         if (this.showPlayerModel.isEnabled()) {
            class_490.method_2486(context, containerX + 26, containerY + 8, containerX + 75, containerY + 78, 30, 0.0625F, (float)mouseX, (float)mouseY - (float)yOffset, this.mc.field_1724);
         }

         Iterator var32 = ((class_1723)invScreen.method_17577()).field_7761.iterator();

         while(var32.hasNext()) {
            class_1735 slot = (class_1735)var32.next();
            class_1799 stack = slot.method_7677();
            if (!stack.method_7960()) {
               this.renderItemStack(context, stack, containerX + slot.field_7873, containerY + slot.field_7872);
            }
         }

         context.method_51448().popMatrix();
         class_1799 cursorStack = this.mc.field_1724.field_7512.method_34255();
         if (!cursorStack.method_7960()) {
            context.method_51448().pushMatrix();
            context.method_51448().translate(0.0F, 0.0F);
            context.method_51427(cursorStack, mouseX - 8, mouseY - 8);
            context.method_51431(this.mc.field_1772, cursorStack, mouseX - 8, mouseY - 8);
            context.method_51448().popMatrix();
         }

         if (hoveredSlot != null && hoveredSlot.method_7681() && cursorStack.method_7960()) {
            context.method_51448().pushMatrix();
            context.method_51448().translate(0.0F, 0.0F);
            class_1799 stack = hoveredSlot.method_7677();
            class_1836 type = this.mc.field_1690.field_1827 ? class_1836.field_41071 : class_1836.field_41070;
            context.method_64038(this.mc.field_1772, stack.method_7950(class_9635.method_59528(this.mc.field_1687), this.mc.field_1724, type), stack.method_32347(), mouseX, mouseY);
            context.method_51448().popMatrix();
         }

      }
   }

   private float[] createParticle(float x, float y, Color color) {
      float[] p = new float[9];
      p[0] = x;
      p[1] = y;
      p[2] = (float)(Math.random() - 0.5D) * 3.0F;
      p[3] = (float)(Math.random() - 0.5D) * 3.0F - 1.0F;
      p[5] = 30.0F + (float)Math.random() * 30.0F;
      p[4] = p[5];
      p[6] = (float)color.getRed();
      p[7] = (float)color.getGreen();
      p[8] = (float)color.getBlue();
      return p;
   }

   private void updateParticle(float[] p, float deltaTime) {
      p[0] += p[2] * deltaTime;
      p[1] += p[3] * deltaTime;
      p[4] -= 1.0F * deltaTime;
      p[3] += 0.15F * deltaTime;
      p[2] *= 0.98F;
      p[3] *= 0.98F;
   }

   private boolean isParticleDead(float[] p) {
      return p[4] <= 0.0F;
   }

   private float getParticleAlpha(float[] p) {
      float progress = p[4] / Math.max(1.0F, p[5]);
      if (progress <= 0.0F) {
         return 0.0F;
      } else {
         return progress > 0.8F ? (1.0F - progress) * 5.0F : Math.min(1.0F, progress * 2.0F);
      }
   }

   private float getParticleSize(float[] p) {
      float progress = p[4] / Math.max(1.0F, p[5]);
      return progress > 0.7F ? 2.0F + (1.0F - progress) * 10.0F : 2.0F + progress * 2.0F;
   }

   private void drawSoftGlow(class_332 context, float x, float y, float w, float h, float cornerRadius, Color color, float glowRadius) {
      if (!(glowRadius <= 0.0F)) {
         int layers = 10;
         float baseAlpha = (float)color.getAlpha() / 255.0F;

         for(int i = 0; i < layers; ++i) {
            float progress = (float)(i + 1) / (float)layers;
            float currentExpansion = glowRadius * progress;
            float alphaFactor = (float)Math.pow((double)(1.0F - progress), 2.0D);
            int finalAlpha = (int)(baseAlpha * 255.0F * alphaFactor * 0.4F);
            finalAlpha = class_3532.method_15340(finalAlpha, 0, 255);
            if (finalAlpha > 1) {
               Color layerColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), finalAlpha);
               Renderer.get().drawRoundedRect(context, x - currentExpansion, y - currentExpansion, w + currentExpansion * 2.0F, h + currentExpansion * 2.0F, cornerRadius + currentExpansion, layerColor);
            }
         }

      }
   }

   private void drawGradientGlow(class_332 context, float x, float y, float w, float h, float rad, float size) {
      float time = (float)System.currentTimeMillis() / 1000.0F;
      Color c1 = this.accentColor.getValue();
      Color c2 = this.selectedSlotColor.getValue();
      int r = (int)((double)c1.getRed() + (double)(c2.getRed() - c1.getRed()) * (Math.sin((double)time) * 0.5D + 0.5D));
      int g = (int)((double)c1.getGreen() + (double)(c2.getGreen() - c1.getGreen()) * (Math.sin((double)(time + 2.0F)) * 0.5D + 0.5D));
      int b = (int)((double)c1.getBlue() + (double)(c2.getBlue() - c1.getBlue()) * (Math.sin((double)(time + 4.0F)) * 0.5D + 0.5D));
      Color gradColor = new Color(r, g, b, 200);
      this.drawSoftGlow(context, x, y, w, h, rad, gradColor, size);
   }

   private void renderItemStack(class_332 context, class_1799 stack, int x, int y) {
      context.method_51427(stack, x, y);
      context.method_51431(this.mc.field_1772, stack, x, y);
   }

   private void drawBlurredRect(class_332 context, float x, float y, float w, float h, float rad, float blur) {
      Renderer.get().drawRoundedRect(context, x, y, w, h, rad, new Color(0, 0, 0, (int)(blur * 3.0F)));
   }

   private boolean isPointWithinBounds(int xPosition, int yPosition, int width, int height, double pointX, double pointY, int leftPos, int topPos) {
      return pointX >= (double)(xPosition + leftPos) && pointX < (double)(xPosition + leftPos + width) && pointY >= (double)(yPosition + topPos) && pointY < (double)(yPosition + topPos + height);
   }

   private Color applyAlpha(Color color) {
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)this.backgroundAlpha.getValue());
   }

   public void onDisable() {
      this.shouldCancelVanillaHotbar = false;
      this.shouldCancelVanillaInventory = false;
      this.particleList.clear();
   }

   public void onRender(class_332 context, float tickDelta) {
      if (!this.isEnabled()) {
         this.shouldCancelVanillaHotbar = false;
      } else {
         boolean isInvOpen = this.mc.field_1755 instanceof class_490;
         if (!this.customHotbar.isEnabled() || this.mc.field_1755 != null && !isInvOpen) {
            this.shouldCancelVanillaHotbar = false;
         } else {
            this.renderHotbar(context, tickDelta);
            this.shouldCancelVanillaHotbar = true;
         }

      }
   }
}
