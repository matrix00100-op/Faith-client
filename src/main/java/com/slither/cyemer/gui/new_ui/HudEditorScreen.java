package com.slither.cyemer.gui.new_ui;

import com.slither.cyemer.Faith;
import com.slither.cyemer.hud.HUDElement;
import com.slither.cyemer.hud.HUDManager;
import com.slither.cyemer.module.BooleanSetting;
import com.slither.cyemer.module.Setting;
import com.slither.cyemer.module.SliderSetting;
import com.slither.cyemer.theme.Theme;
import com.slither.cyemer.theme.ThemeManager;
import com.slither.cyemer.util.Renderer;
import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_768;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class HudEditorScreen extends class_437 {
   private final class_437 parent;
   private Theme theme;
   private HUDElement draggingElement = null;
   private HUDElement settingsElement = null;
   private final Map<Setting, Boolean> draggingSliders = new HashMap();

   public HudEditorScreen(class_437 parent) {
      super(class_2561.method_43470("HUD Editor"));
      this.parent = parent;
   }

   private boolean isShiftDown() {
      if (this.field_22787 == null) {
         return false;
      } else {
         long handle = this.field_22787.method_22683().method_4490();
         return GLFW.glfwGetKey(handle, 340) == 1 || GLFW.glfwGetKey(handle, 344) == 1;
      }
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      this.theme = ThemeManager.getInstance().getCurrentTheme();
      int screenWidth = this.field_22789;
      int screenHeight = this.field_22790;
      float pixelRatio = (float)this.field_22787.method_22683().method_4495();
      if (Renderer.get().beginFrame((float)screenWidth, (float)screenHeight, pixelRatio)) {
         try {
            Renderer.get().drawRect(context, 0.0F, 0.0F, (float)this.field_22789, (float)this.field_22790, new Color(0, 0, 0, 128));
            String instructions = "Left-click drag to move. Right-click to toggle. Shift + Right-click for settings.";
            float instrWidth = Renderer.get().getTextWidth(instructions, 14.0F);
            Renderer.get().drawText(context, instructions, ((float)this.field_22789 - instrWidth) / 2.0F, 15.0F, 14.0F, Color.WHITE, true);
            Iterator var10 = HUDManager.getInstance().getElements().iterator();

            while(var10.hasNext()) {
               HUDElement element = (HUDElement)var10.next();
               if (!element.isEnabled()) {
                  float x = (float)element.getX();
                  float y = (float)element.getY();
                  float w = (float)element.getWidth();
                  float h = (float)element.getHeight();
                  Renderer.get().drawRoundedRect(context, x, y, w, h, 4.0F, new Color(170, 170, 170, 80));
                  String name = element.getName();
                  float nameWidth = Renderer.get().getTextWidth(name, 12.0F);
                  Renderer.get().drawText(context, name, x + (w - nameWidth) / 2.0F, y + (h - 12.0F) / 2.0F, 12.0F, new Color(170, 170, 170), true);
               }
            }
         } finally {
            Renderer.get().endFrame();
         }

         Iterator var21 = HUDManager.getInstance().getElements().iterator();

         while(var21.hasNext()) {
            HUDElement element = (HUDElement)var21.next();
            if (element.isEnabled()) {
               element.render(context, delta);
            }
         }

         if (this.settingsElement != null) {
            this.renderSettingsPopup(context, mouseX, mouseY, pixelRatio);
         }

      }
   }

   private void renderSettingsPopup(class_332 context, int mouseX, int mouseY, float pixelRatio) {
      if (this.settingsElement != null) {
         List<Setting> settings = this.settingsElement.getSettings();
         if (settings != null && !settings.isEmpty()) {
            int toggleCount = 0;
            int sliderCount = 0;
            Iterator var8 = settings.iterator();

            while(var8.hasNext()) {
               Setting setting = (Setting)var8.next();
               if (setting instanceof BooleanSetting) {
                  ++toggleCount;
               } else if (setting instanceof SliderSetting) {
                  ++sliderCount;
               }
            }

            int popupWidth = 180;
            int itemHeight = 18;
            int sliderSectionHeight = 28;
            int popupHeight = 10 + toggleCount * itemHeight + 5 + sliderCount * sliderSectionHeight + 5;
            int popupX = (int)(this.settingsElement.getX() + this.settingsElement.getWidth() + 8.0D);
            int popupY = (int)this.settingsElement.getY();
            if (popupX + popupWidth > this.field_22789) {
               popupX = (int)(this.settingsElement.getX() - (double)popupWidth - 8.0D);
            }

            int screenWidth = this.field_22789;
            int screenHeight = this.field_22790;
            if (Renderer.get().beginFrame((float)screenWidth, (float)screenHeight, pixelRatio)) {
               try {
                  float currentY;
                  for(int i = 3; i >= 0; --i) {
                     float offset = (float)(i * 2);
                     currentY = (float)(60 - i * 15) / 255.0F;
                     Renderer.get().drawRoundedRect(context, (float)popupX - offset, (float)popupY - offset, (float)popupWidth + offset * 2.0F, (float)popupHeight + offset * 2.0F, 8.0F, new Color(0, 0, 0, (int)(currentY * 255.0F)));
                  }

                  Renderer.get().drawRoundedRect(context, (float)popupX, (float)popupY, (float)popupWidth, (float)popupHeight, 8.0F, this.theme.panelBg);
                  Color topGradient = new Color(100, 150, 255, 60);
                  Color bottomGradient = new Color(100, 150, 255, 0);
                  Renderer.get().drawRoundedRectGradient(context, (float)popupX, (float)popupY, (float)popupWidth, (float)popupHeight, 8.0F, topGradient, bottomGradient, true);
                  Renderer.get().drawRoundedRectOutline(context, (float)popupX, (float)popupY, (float)popupWidth, (float)popupHeight, 8.0F, 1.5F, new Color(255, 255, 255, 60));
                  currentY = (float)(popupY + 8);
                  Iterator var19 = settings.iterator();

                  Setting setting;
                  String label;
                  while(var19.hasNext()) {
                     setting = (Setting)var19.next();
                     if (setting instanceof BooleanSetting) {
                        BooleanSetting boolSetting = (BooleanSetting)setting;
                        label = setting.getName();
                        boolean state = boolSetting.isEnabled();
                        Renderer.get().drawText(context, label, (float)(popupX + 10), currentY + 3.0F, 13.0F, Color.WHITE, true);
                        String stateText = state ? "ON" : "OFF";
                        Color stateColor = state ? new Color(100, 255, 100) : new Color(255, 100, 100);
                        float stateWidth = Renderer.get().getTextWidth(stateText, 13.0F);
                        Renderer.get().drawText(context, stateText, (float)(popupX + popupWidth) - stateWidth - 10.0F, currentY + 3.0F, 13.0F, stateColor, true);
                        if (this.isMouseOver((double)mouseX, (double)mouseY, new class_768(popupX + 5, (int)currentY, popupWidth - 10, itemHeight - 2))) {
                           Renderer.get().drawRoundedRect(context, (float)(popupX + 5), currentY, (float)(popupWidth - 10), (float)(itemHeight - 2), 4.0F, new Color(255, 255, 255, 20));
                        }

                        currentY += (float)itemHeight;
                     }
                  }

                  currentY += 5.0F;
                  var19 = settings.iterator();

                  while(true) {
                     do {
                        if (!var19.hasNext()) {
                           return;
                        }

                        setting = (Setting)var19.next();
                     } while(!(setting instanceof SliderSetting));

                     SliderSetting sliderSetting = (SliderSetting)setting;
                     label = setting.getName() + ":";
                     String valueText = this.formatSliderValue(sliderSetting);
                     Renderer.get().drawText(context, label, (float)(popupX + 10), currentY, 12.0F, Color.WHITE, true);
                     float valueWidth = Renderer.get().getTextWidth(valueText, 12.0F);
                     Renderer.get().drawText(context, valueText, (float)(popupX + popupWidth - 10) - valueWidth, currentY, 12.0F, new Color(200, 200, 200), true);
                     currentY += 14.0F;
                     int sliderX = popupX + 10;
                     int sliderWidth = popupWidth - 20;
                     Renderer.get().drawRoundedRect(context, (float)sliderX, currentY, (float)sliderWidth, 4.0F, 2.0F, new Color(60, 60, 60));
                     double percent = (sliderSetting.getValue() - sliderSetting.getMin()) / (sliderSetting.getMax() - sliderSetting.getMin());
                     if (percent > 0.01D) {
                        Color fillColor = this.theme.moduleEnabledBg.brighter();
                        Renderer.get().drawRoundedRect(context, (float)sliderX, currentY, (float)((double)sliderWidth * percent), 4.0F, 2.0F, fillColor);
                     }

                     int handleX = (int)((double)sliderX + (double)(sliderWidth - 6) * Math.max(0.0D, Math.min(1.0D, percent)));

                     for(int i = 3; i >= 1; --i) {
                        float glowSize = (float)(8 + i * 2);
                        float glowAlpha = 0.3F - (float)i * 0.08F;
                        Renderer.get().drawRoundedRect(context, (float)handleX - glowSize / 2.0F + 3.0F, currentY - glowSize / 2.0F + 2.0F, glowSize, glowSize, glowSize / 2.0F, new Color(255, 255, 255, (int)(glowAlpha * 255.0F)));
                     }

                     Renderer.get().drawRoundedRect(context, (float)handleX, currentY - 2.0F, 6.0F, 8.0F, 3.0F, Color.WHITE);
                     currentY += 14.0F;
                  }
               } finally {
                  Renderer.get().endFrame();
               }
            }
         }
      }
   }

   private String formatSliderValue(SliderSetting slider) {
      double value = slider.getValue();
      String name = slider.getName();
      if (!name.contains("Opacity") && !name.contains("Intensity")) {
         if (name.contains("Delay")) {
            if (value < 0.01D) {
               return "Never";
            } else {
               Object[] var10001 = new Object[]{value};
               return String.format("%.1f", var10001) + "s";
            }
         } else {
            return !name.contains("Size") && !name.contains("Radius") && !name.contains("Font") ? slider.getValueAsString() : String.format("%.0f", value);
         }
      } else {
         int percent = (int)Math.round(value * 100.0D);
         return percent + "%";
      }
   }

   public boolean method_25402(class_11909 click, boolean doubleClick) {
      double mouseX = click.comp_4798();
      double mouseY = click.comp_4799();
      int button = click.method_74245();
      if (this.settingsElement != null) {
         if (!this.isMouseOverSettings(mouseX, mouseY)) {
            this.settingsElement = null;
            return true;
         }

         if (this.handleSettingsPopupClick(mouseX, mouseY, button)) {
            return true;
         }
      }

      for(int i = HUDManager.getInstance().getElements().size() - 1; i >= 0; --i) {
         HUDElement element = (HUDElement)HUDManager.getInstance().getElements().get(i);
         class_768 bounds = new class_768((int)element.getX(), (int)element.getY(), (int)element.getWidth(), (int)element.getHeight());
         if (this.isMouseOver(mouseX, mouseY, bounds)) {
            if (button == 0) {
               this.draggingElement = element;
               element.startDragging(mouseX, mouseY);
            } else if (button == 1) {
               if (this.isShiftDown()) {
                  this.settingsElement = this.settingsElement == element ? null : element;
               } else {
                  element.setEnabled(!element.isEnabled());
               }
            }

            return true;
         }
      }

      return super.method_25402(click, doubleClick);
   }

   private boolean handleSettingsPopupClick(double mouseX, double mouseY, int button) {
      if (button == 0 && this.settingsElement != null) {
         List<Setting> settings = this.settingsElement.getSettings();
         if (settings != null && !settings.isEmpty()) {
            int popupWidth = 180;
            int popupX = (int)(this.settingsElement.getX() + this.settingsElement.getWidth() + 8.0D);
            if (popupX + popupWidth > this.field_22789) {
               popupX = (int)(this.settingsElement.getX() - (double)popupWidth - 8.0D);
            }

            int popupY = (int)this.settingsElement.getY();
            float currentY = (float)(popupY + 8);
            int itemHeight = 18;
            int sliderSectionHeight = true;
            Iterator var13 = settings.iterator();

            Setting setting;
            while(var13.hasNext()) {
               setting = (Setting)var13.next();
               if (setting instanceof BooleanSetting) {
                  BooleanSetting boolSetting = (BooleanSetting)setting;
                  if (this.isMouseOver(mouseX, mouseY, new class_768(popupX + 5, (int)currentY, popupWidth - 10, itemHeight - 2))) {
                     boolSetting.setEnabled(!boolSetting.isEnabled());
                     return true;
                  }

                  currentY += (float)itemHeight;
               }
            }

            currentY += 5.0F;
            var13 = settings.iterator();

            while(var13.hasNext()) {
               setting = (Setting)var13.next();
               if (setting instanceof SliderSetting) {
                  SliderSetting sliderSetting = (SliderSetting)setting;
                  currentY += 14.0F;
                  class_768 sliderBounds = new class_768(popupX + 10, (int)currentY - 2, popupWidth - 20, 10);
                  if (this.isMouseOver(mouseX, mouseY, sliderBounds)) {
                     this.draggingSliders.put(setting, true);
                     this.updateSlider(sliderSetting, mouseX);
                     return true;
                  }

                  currentY += 14.0F;
               }
            }

            return false;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean method_25403(class_11909 click, double deltaX, double deltaY) {
      double mouseX = click.comp_4798();
      double mouseY = click.comp_4799();
      if (this.draggingElement != null) {
         this.draggingElement.mouseDragged(mouseX, mouseY);
         return true;
      } else {
         Iterator var10 = this.draggingSliders.entrySet().iterator();

         while(var10.hasNext()) {
            Entry<Setting, Boolean> entry = (Entry)var10.next();
            if ((Boolean)entry.getValue()) {
               Object var13 = entry.getKey();
               if (var13 instanceof SliderSetting) {
                  SliderSetting sliderSetting = (SliderSetting)var13;
                  this.updateSlider(sliderSetting, mouseX);
                  return true;
               }
            }
         }

         return super.method_25403(click, deltaX, deltaY);
      }
   }

   public boolean method_25406(class_11909 click) {
      double mouseX = click.comp_4798();
      double mouseY = click.comp_4799();
      int button = click.method_74245();
      if (this.draggingElement != null && button == 0) {
         this.draggingElement.stopDragging();
         this.draggingElement = null;
      }

      this.draggingSliders.clear();
      return super.method_25406(click);
   }

   private void updateSlider(SliderSetting slider, double mouseX) {
      if (this.settingsElement != null) {
         int popupWidth = 180;
         int popupX = (int)(this.settingsElement.getX() + this.settingsElement.getWidth() + 8.0D);
         if (popupX + popupWidth > this.field_22789) {
            popupX = (int)(this.settingsElement.getX() - (double)popupWidth - 8.0D);
         }

         double percent = (mouseX - (double)(popupX + 10)) / (double)(popupWidth - 20);
         percent = Math.max(0.0D, Math.min(1.0D, percent));
         double range = slider.getMax() - slider.getMin();
         double newValue = slider.getMin() + percent * range;
         slider.setValue(newValue);
      }
   }

   public boolean method_25404(class_11908 keyInput) {
      int keyCode = keyInput.comp_4795();
      if (keyCode == 256) {
         this.method_25419();
         return true;
      } else {
         return super.method_25404(keyInput);
      }
   }

   public void method_25419() {
      Faith.INSTANCE.getConfigManager().save("default");
      this.field_22787.method_1507(this.parent);
   }

   public boolean method_25421() {
      return false;
   }

   private boolean isMouseOver(double mouseX, double mouseY, class_768 rect) {
      return rect != null && mouseX >= (double)rect.method_3321() && mouseX <= (double)(rect.method_3321() + rect.method_3319()) && mouseY >= (double)rect.method_3322() && mouseY <= (double)(rect.method_3322() + rect.method_3320());
   }

   private boolean isMouseOverSettings(double mouseX, double mouseY) {
      if (this.settingsElement == null) {
         return false;
      } else {
         List<Setting> settings = this.settingsElement.getSettings();
         if (settings != null && !settings.isEmpty()) {
            int toggleCount = 0;
            int sliderCount = 0;
            Iterator var8 = settings.iterator();

            while(var8.hasNext()) {
               Setting setting = (Setting)var8.next();
               if (setting instanceof BooleanSetting) {
                  ++toggleCount;
               } else if (setting instanceof SliderSetting) {
                  ++sliderCount;
               }
            }

            int popupWidth = 180;
            int itemHeight = 18;
            int sliderSectionHeight = 28;
            int popupHeight = 10 + toggleCount * itemHeight + 5 + sliderCount * sliderSectionHeight + 5;
            int popupX = (int)(this.settingsElement.getX() + this.settingsElement.getWidth() + 8.0D);
            int popupY = (int)this.settingsElement.getY();
            if (popupX + popupWidth > this.field_22789) {
               popupX = (int)(this.settingsElement.getX() - (double)popupWidth - 8.0D);
            }

            return this.isMouseOver(mouseX, mouseY, new class_768(popupX, popupY, popupWidth, popupHeight));
         } else {
            return false;
         }
      }
   }
}
