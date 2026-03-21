package com.slither.cyemer.gui.new_ui.settings;

import com.slither.cyemer.module.SliderSetting;
import com.slither.cyemer.module.implementation.ClickGUIModule;
import com.slither.cyemer.util.Renderer;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_332;
import net.minecraft.class_3532;

@Environment(EnvType.CLIENT)
public class SliderComponent extends SettingComponent {
   private boolean dragging = false;
   private final SliderSetting sliderSetting;
   private double trueFill = 0.0D;
   private double displayFill = 0.0D;
   private double displayHandle = 0.0D;
   private double hoverAnimation = 0.0D;
   private static final double HOVER_SPEED = 14.0D;
   private static final double FILL_SNAP_SPEED = 22.0D;
   private static final double FILL_DRAG_SPEED = 6.0D;
   private static final double HANDLE_SNAP_SPEED = 28.0D;
   private static final double HANDLE_DRAG_SPEED = 9.0D;

   public SliderComponent(SliderSetting setting) {
      super(setting);
      this.sliderSetting = setting;
      double initialFill = (setting.getPreciseValue() - setting.getMin()) / (setting.getMax() - setting.getMin());
      this.trueFill = initialFill;
      this.displayFill = initialFill;
      this.displayHandle = initialFill;
   }

   public void render(class_332 context, int mouseX, int mouseY, float delta, double alpha) {
      double dt = Math.min((double)delta / 20.0D, 0.05D);
      boolean hovered = (double)mouseX >= this.x && (double)mouseX <= this.x + this.width && (double)mouseY >= this.y - 2.0D && (double)mouseY <= this.y + this.getComponentHeight() + 2.0D;
      double hoverTarget = !hovered && !this.dragging ? 0.0D : 1.0D;
      this.hoverAnimation += (hoverTarget - this.hoverAnimation) * (1.0D - Math.exp(-14.0D * dt));
      double fillSpeed;
      if (this.dragging) {
         fillSpeed = this.sliderSetting.getMin() + class_3532.method_15350(((double)mouseX - this.x) / this.width, 0.0D, 1.0D) * (this.sliderSetting.getMax() - this.sliderSetting.getMin());
         this.sliderSetting.setValue(fillSpeed);
      }

      this.trueFill = (this.sliderSetting.getPreciseValue() - this.sliderSetting.getMin()) / (this.sliderSetting.getMax() - this.sliderSetting.getMin());
      fillSpeed = this.dragging ? 6.0D : 22.0D;
      double handleSpeed = this.dragging ? 9.0D : 28.0D;
      this.displayFill += (this.trueFill - this.displayFill) * (1.0D - Math.exp(-fillSpeed * dt));
      this.displayHandle += (this.trueFill - this.displayHandle) * (1.0D - Math.exp(-handleSpeed * dt));
      float centerY = (float)(this.y + this.getComponentHeight() / 2.0D);
      float trackHeight = 4.0F + (float)(this.hoverAnimation * 1.2000000476837158D);
      float trackRadius = trackHeight / 2.0F;
      float trackY = centerY - trackHeight / 2.0F;
      Color trackBase = ClickGUIModule.getColor(ClickGUIModule.getSettingsBackground(), alpha * (0.72D + this.hoverAnimation * 0.08D));
      Color trackNeutral = this.blend(trackBase, ClickGUIModule.getColor(ClickGUIModule.getModuleDisabledText(), alpha * 0.6D), 0.34D);
      Color track = new Color(trackNeutral.getRed(), trackNeutral.getGreen(), trackNeutral.getBlue(), Math.max(trackBase.getAlpha(), trackNeutral.getAlpha()));
      Renderer.get().drawRoundedRect(context, (float)this.x, trackY, (float)this.width, trackHeight, trackRadius, track);
      double fillW = this.width * this.displayFill;
      if (fillW > 1.0D) {
         Color gradStart = ClickGUIModule.getColor(ClickGUIModule.getModuleEnabledGradientStart(), alpha);
         Color gradEnd = ClickGUIModule.getColor(ClickGUIModule.getModuleEnabledGradientEnd(), alpha);
         Renderer.get().drawRoundedRectGradient(context, (float)this.x, trackY, (float)fillW, trackHeight, trackRadius, gradStart, gradEnd, false);
      }

      float handleSize = 8.0F + (float)(this.hoverAnimation * 1.7999999523162842D);
      float handleRadius = handleSize / 2.0F;
      double handleX = class_3532.method_15350(this.x + this.width * this.displayHandle - (double)handleSize / 2.0D, this.x, this.x + this.width - (double)handleSize);
      float handleY = centerY - handleSize / 2.0F;
      Color glowBase;
      if ((this.hoverAnimation > 0.01D || this.dragging) && alpha > 0.05D) {
         glowBase = ClickGUIModule.getColor(ClickGUIModule.getModuleEnabledGradientEnd(), alpha);
         int layers = 7;

         for(int i = layers; i >= 1; --i) {
            float expand = (float)i * (0.65F + (float)this.hoverAnimation * 0.35F);
            float falloff = (float)Math.pow((double)(1.0F - (float)i / (float)(layers + 1)), 2.0D);
            int glowA = Math.max(0, Math.min(255, (int)(71.4D * alpha * (0.25D + 0.45D * this.hoverAnimation) * (double)falloff)));
            Color glowLayer = new Color(glowBase.getRed(), glowBase.getGreen(), glowBase.getBlue(), glowA);
            Renderer.get().drawRoundedRect(context, (float)handleX - expand, handleY - expand, handleSize + expand * 2.0F, handleSize + expand * 2.0F, handleRadius + expand, glowLayer);
         }
      }

      glowBase = new Color(255, 255, 255, Math.max(0, Math.min(255, (int)(255.0D * alpha))));
      Renderer.get().drawRoundedRect(context, (float)handleX, handleY, handleSize, handleSize, handleRadius, glowBase);
   }

   public void mouseClicked(double mouseX, double mouseY, int button) {
      if (this.isHovered(mouseX, mouseY) && button == 0) {
         this.dragging = true;
      }

   }

   public void mouseReleased(double mouseX, double mouseY, int button) {
      if (button == 0) {
         this.dragging = false;
      }

   }

   public double getComponentHeight() {
      return 8.0D;
   }

   private Color blend(Color a, Color b, double t) {
      double c = Math.max(0.0D, Math.min(1.0D, t));
      return new Color(Math.max(0, Math.min(255, (int)((double)a.getRed() + (double)(b.getRed() - a.getRed()) * c))), Math.max(0, Math.min(255, (int)((double)a.getGreen() + (double)(b.getGreen() - a.getGreen()) * c))), Math.max(0, Math.min(255, (int)((double)a.getBlue() + (double)(b.getBlue() - a.getBlue()) * c))), Math.max(0, Math.min(255, (int)((double)a.getAlpha() + (double)(b.getAlpha() - a.getAlpha()) * c))));
   }
}
