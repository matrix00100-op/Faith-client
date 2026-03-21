package com.slither.cyemer.module.implementation.render;

import com.slither.cyemer.module.BooleanSetting;
import com.slither.cyemer.module.Category;
import com.slither.cyemer.module.ColorSetting;
import com.slither.cyemer.module.ModeSetting;
import com.slither.cyemer.module.Module;
import com.slither.cyemer.module.SliderSetting;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class HandCham extends Module {
   public static HandCham INSTANCE;
   private final ColorSetting color;
   private final BooleanSetting tinted;
   private final BooleanSetting breathing;
   private final ModeSetting quality;
   private final SliderSetting animationSpeed;
   private final SliderSetting outlineWidth;
   private final SliderSetting softness;
   private boolean handChamActive = false;
   private float previousLineWidth = 1.0F;

   public HandCham() {
      super("HandCham", "Draws a custom shader on your hand.", Category.RENDER);
      INSTANCE = this;
      this.color = new ColorSetting("Color", new Color(0, 255, 255, 255));
      this.tinted = new BooleanSetting("Tinted", true);
      this.breathing = new BooleanSetting("Breathing", true);
      this.quality = new ModeSetting("Quality", new String[]{"Low", "Medium", "High"});
      this.animationSpeed = new SliderSetting("Anim Speed", 2.0D, 0.1D, 10.0D, 1);
      this.outlineWidth = new SliderSetting("Outline Width", 10.0D, 1.0D, 50.0D, 1);
      this.softness = new SliderSetting("Glow Intensity", 0.6D, 0.1D, 1.0D, 2);
      this.addSetting(this.color);
      this.addSetting(this.tinted);
      this.addSetting(this.breathing);
      this.addSetting(this.quality);
      this.addSetting(this.animationSpeed);
      this.addSetting(this.outlineWidth);
      this.addSetting(this.softness);
   }

   public void prepareRender() {
      if (!this.shouldRenderHandCham()) {
         this.handChamActive = false;
      } else {
         float width = Math.max(1.0F, (float)this.outlineWidth.getValue() / 6.0F);
         this.previousLineWidth = GL11.glGetFloat(2849);
         GL11.glEnable(3042);
         GL11.glBlendFunc(770, 771);
         GL11.glLineWidth(width);
         String q = this.quality.getCurrentMode();
         if (!"High".equals(q) && !"Medium".equals(q)) {
            GL11.glDisable(2848);
         } else {
            GL11.glEnable(2848);
         }

         GL11.glPolygonMode(1032, 6913);
         this.handChamActive = true;
      }
   }

   public void drawRender() {
      if (this.handChamActive) {
         GL11.glPolygonMode(1032, 6914);
         GL11.glLineWidth(Math.max(1.0F, this.previousLineWidth));
         GL11.glDisable(2848);
         this.handChamActive = false;
      }
   }

   public void onDisable() {
      if (this.handChamActive) {
         GL11.glPolygonMode(1032, 6914);
         GL11.glLineWidth(Math.max(1.0F, this.previousLineWidth));
         GL11.glDisable(2848);
         this.handChamActive = false;
      }

   }

   private boolean shouldRenderHandCham() {
      if (this.mc.field_1724 == null) {
         return false;
      } else if (!this.mc.field_1690.method_31044().method_31034()) {
         return false;
      } else if (!this.mc.field_1724.method_6047().method_7960()) {
         return false;
      } else {
         return this.mc.field_1724.method_6079().method_7960();
      }
   }
}
