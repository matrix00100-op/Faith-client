package com.slither.cyemer.gui.new_ui.settings;

import com.slither.cyemer.module.BooleanSetting;
import com.slither.cyemer.module.implementation.ClickGUIModule;
import com.slither.cyemer.util.Renderer;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_332;

@Environment(EnvType.CLIENT)
public class BooleanComponent extends SettingComponent {
   private double toggleAnimation = 0.0D;
   private long lastFrame = System.currentTimeMillis();

   public BooleanComponent(BooleanSetting setting) {
      super(setting);
      this.toggleAnimation = setting.isEnabled() ? 1.0D : 0.0D;
   }

   public void render(class_332 context, int mouseX, int mouseY, float delta, double alpha) {
      BooleanSetting boolSet = (BooleanSetting)this.setting;
      long now = System.currentTimeMillis();
      double dt = Math.min((double)(now - this.lastFrame) / 1000.0D, 0.05D);
      this.lastFrame = now;
      double target = boolSet.isEnabled() ? 1.0D : 0.0D;
      double speed = 14.0D;
      double step = Math.min(1.0D, speed * dt);
      if (Math.abs(this.toggleAnimation - target) < 0.003D) {
         this.toggleAnimation = target;
      } else {
         this.toggleAnimation += (target - this.toggleAnimation) * step;
      }

      Color bgColor = ClickGUIModule.getColor(ClickGUIModule.getSettingsBackground(), alpha * 0.3D);
      Renderer.get().drawRoundedRect(context, (float)this.x, (float)this.y, (float)this.width, (float)this.height, 4.0F, bgColor);
      Color textColor = ClickGUIModule.getColor(ClickGUIModule.getSettingsText(), alpha);
      float textY = (float)(this.y + (this.height - 10.0D) / 2.0D);
      Renderer.get().drawText(context, this.setting.getName(), (float)this.x + 6.0F, textY, 10.0F, textColor, false);
      float trackWidth = 32.0F;
      float trackHeight = 14.0F;
      float trackX = (float)(this.x + this.width - (double)trackWidth - 6.0D);
      float trackY = (float)(this.y + (this.height - (double)trackHeight) / 2.0D);
      float trackRadius = trackHeight / 2.0F;
      Color trackOff = ClickGUIModule.getColor(ClickGUIModule.getSettingsBackground(), alpha * 0.8D);
      Color trackOn = ClickGUIModule.getColor(ClickGUIModule.getModuleEnabledGradientStart(), alpha);
      Color trackColor = this.interpolateColor(trackOff, trackOn, this.toggleAnimation);
      float handleY;
      float handleRadius;
      if (this.toggleAnimation < 0.99D) {
         new Color(0, 0, 0, 0);
         int shadowLayers = 5;

         for(int i = shadowLayers; i >= 1; --i) {
            handleY = (float)i * 0.8F;
            handleRadius = (float)Math.pow((double)(1.0F - (float)i / (float)(shadowLayers + 1)), 1.7999999523162842D);
            int shadowA = Math.max(0, Math.min(255, (int)(56.1D * alpha * (1.0D - this.toggleAnimation) * (double)handleRadius)));
            Color shadowLayer = new Color(0, 0, 0, shadowA);
            Renderer.get().drawRoundedRect(context, trackX - handleY, trackY - handleY, trackWidth + handleY * 2.0F, trackHeight + handleY * 2.0F, trackRadius + handleY, shadowLayer);
         }
      }

      Renderer.get().drawRoundedRect(context, trackX, trackY, trackWidth, trackHeight, trackRadius, trackColor);
      float handleSize = 10.0F;
      float handlePadding = 2.0F;
      float handleX = trackX + handlePadding + (float)(this.toggleAnimation * (double)(trackWidth - handleSize - handlePadding * 2.0F));
      handleY = trackY + (trackHeight - handleSize) / 2.0F;
      handleRadius = handleSize / 2.0F;
      Color glowBase;
      if (ClickGUIModule.useShadows() && this.toggleAnimation > 0.01D) {
         glowBase = ClickGUIModule.getColor(ClickGUIModule.getModuleEnabledGradientEnd(), alpha);
         int layers = 8;

         for(int i = layers; i >= 1; --i) {
            float expand = (float)i * 0.6F;
            float falloff = (float)Math.pow((double)(1.0F - (float)i / (float)(layers + 1)), 2.200000047683716D);
            int glowA = Math.max(0, Math.min(255, (int)(38.25D * alpha * this.toggleAnimation * (double)falloff)));
            Color glowLayer = new Color(glowBase.getRed(), glowBase.getGreen(), glowBase.getBlue(), glowA);
            Renderer.get().drawRoundedRect(context, handleX - expand, handleY - expand, handleSize + expand * 2.0F, handleSize + expand * 2.0F, handleRadius + expand, glowLayer);
         }
      }

      glowBase = new Color(255, 255, 255, Math.max(0, Math.min(255, (int)(255.0D * alpha))));
      Renderer.get().drawRoundedRect(context, handleX, handleY, handleSize, handleSize, handleRadius, glowBase);
   }

   private Color interpolateColor(Color a, Color b, double t) {
      return new Color(Math.max(0, Math.min(255, (int)((double)a.getRed() + (double)(b.getRed() - a.getRed()) * t))), Math.max(0, Math.min(255, (int)((double)a.getGreen() + (double)(b.getGreen() - a.getGreen()) * t))), Math.max(0, Math.min(255, (int)((double)a.getBlue() + (double)(b.getBlue() - a.getBlue()) * t))), Math.max(0, Math.min(255, (int)((double)a.getAlpha() + (double)(b.getAlpha() - a.getAlpha()) * t))));
   }

   public void mouseClicked(double mouseX, double mouseY, int button) {
      if (this.isHovered(mouseX, mouseY) && button == 0) {
         ((BooleanSetting)this.setting).toggle();
      }

   }

   public void mouseReleased(double mouseX, double mouseY, int button) {
   }

   public double getComponentHeight() {
      return 20.0D;
   }
}
