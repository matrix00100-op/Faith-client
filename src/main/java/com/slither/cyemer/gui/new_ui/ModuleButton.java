package com.slither.cyemer.gui.new_ui;

import com.slither.cyemer.gui.new_ui.settings.BooleanComponent;
import com.slither.cyemer.gui.new_ui.settings.ColorComponent;
import com.slither.cyemer.gui.new_ui.settings.KeybindComponent;
import com.slither.cyemer.gui.new_ui.settings.ModeComponent;
import com.slither.cyemer.gui.new_ui.settings.SettingComponent;
import com.slither.cyemer.gui.new_ui.settings.SliderComponent;
import com.slither.cyemer.gui.new_ui.settings.StringComponent;
import com.slither.cyemer.module.BooleanSetting;
import com.slither.cyemer.module.ColorSetting;
import com.slither.cyemer.module.ModeSetting;
import com.slither.cyemer.module.Module;
import com.slither.cyemer.module.Setting;
import com.slither.cyemer.module.SliderSetting;
import com.slither.cyemer.module.StringSetting;
import com.slither.cyemer.module.implementation.ClickGUIModule;
import com.slither.cyemer.module.implementation.KeybindSetting;
import com.slither.cyemer.util.Renderer;
import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_332;

@Environment(EnvType.CLIENT)
public class ModuleButton {
   public final Module module;
   private final List<SettingComponent> components = new ArrayList();
   public double x;
   public double y;
   public double width;
   public double height;
   private boolean expanded = false;
   public boolean isLastButton = false;
   private double currentSettingHeight = 0.0D;
   private final double animationSpeed = 0.2D;

   public ModuleButton(Module module) {
      this.module = module;
      module.getSettings().forEach((s) -> {
         if (s instanceof BooleanSetting) {
            BooleanSetting bs = (BooleanSetting)s;
            this.components.add(new BooleanComponent(bs));
         } else if (s instanceof ModeSetting) {
            ModeSetting ms = (ModeSetting)s;
            this.components.add(new ModeComponent(ms));
         } else if (s instanceof SliderSetting) {
            SliderSetting ss = (SliderSetting)s;
            this.components.add(new SliderComponent(ss));
         } else if (s instanceof ColorSetting) {
            ColorSetting cs = (ColorSetting)s;
            this.components.add(new ColorComponent(cs));
         } else if (s instanceof StringSetting) {
            StringSetting sts = (StringSetting)s;
            this.components.add(new StringComponent(sts));
         } else if (s instanceof KeybindSetting) {
            KeybindSetting ks = (KeybindSetting)s;
            this.components.add(new KeybindComponent(ks));
         }

      });
   }

   public String render(class_332 context, int mouseX, int mouseY, float delta, double alpha) {
      String descriptionToRender = null;
      double realContentHeight = this.getSettingsHeight();
      boolean hovered = this.isButtonHovered((double)mouseX, (double)mouseY);
      double targetHeight = this.expanded ? realContentHeight : 0.0D;
      if (Math.abs(this.currentSettingHeight - targetHeight) > 0.01D) {
         this.currentSettingHeight += (targetHeight - this.currentSettingHeight) * 0.2D;
      } else {
         this.currentSettingHeight = targetHeight;
      }

      double clampedSettingHeight = Math.max(0.0D, this.currentSettingHeight);
      if (hovered) {
         descriptionToRender = this.module.getDescription();
      }

      boolean bottomRoundedRow = this.isLastButton && clampedSettingHeight <= 2.0D;
      if (bottomRoundedRow) {
         Renderer.get().scissor(context, (float)this.x, (float)this.y, (float)this.width, (float)this.height);
      }

      if (this.module.isEnabled()) {
         this.drawHorizontalGradient(context, alpha, bottomRoundedRow);
      } else {
         this.drawDisabledGlass(context, alpha, bottomRoundedRow);
      }

      if (bottomRoundedRow) {
         Renderer.get().resetScissor();
      }

      float textY = (float)(this.y + (this.height - 10.0D) / 2.0D);
      String moduleName = this.module.getName();
      Color textColor = this.module.isEnabled() ? ClickGUIModule.getColor(ClickGUIModule.getModuleEnabledText(), alpha) : ClickGUIModule.getColor(ClickGUIModule.getModuleDisabledText(), alpha);
      Renderer.get().drawText(context, moduleName, (float)this.x + 8.0F, textY, 10.0F, textColor, false);
      if (clampedSettingHeight > 0.5D) {
         double settingY = this.y + this.height + 3.0D;
         Color settingTextColor = ClickGUIModule.getColor(ClickGUIModule.getSettingsText(), alpha);
         double visibleHeight = Math.min(clampedSettingHeight, realContentHeight);
         Renderer.get().scissor(context, (float)this.x, (float)(this.y + this.height), (float)this.width, (float)visibleHeight);
         this.drawSettingsGlass(context, alpha, visibleHeight, this.isLastButton);

         SettingComponent comp;
         for(Iterator var24 = this.components.iterator(); var24.hasNext(); settingY += comp.height + 4.0D) {
            comp = (SettingComponent)var24.next();
            if (!(comp instanceof BooleanComponent)) {
               String title = comp.setting.getName();
               Setting var28 = comp.setting;
               if (var28 instanceof SliderSetting) {
                  SliderSetting sliderSetting = (SliderSetting)var28;
                  title = title + ": " + round(sliderSetting.getPreciseValue(), 2);
               }

               if (settingY < this.y + this.height + visibleHeight) {
                  Renderer.get().scissor(context, (float)this.x, (float)(this.y + this.height), (float)this.width, (float)visibleHeight);
                  Renderer.get().drawText(context, title, (float)(this.x + 10.0D), (float)settingY, 10.0F, settingTextColor, false);
               }

               settingY += 14.0D;
            }

            comp.x = this.x + 5.0D;
            comp.y = settingY;
            comp.width = this.width - 10.0D;
            comp.height = comp.getComponentHeight();
            if (settingY < this.y + this.height + visibleHeight) {
               Renderer.get().scissor(context, (float)this.x, (float)(this.y + this.height), (float)this.width, (float)visibleHeight);
               comp.render(context, mouseX, mouseY, delta, alpha);
            }
         }

         Renderer.get().resetScissor();
      }

      return descriptionToRender;
   }

   private void drawSettingsGlass(class_332 context, double alpha, double customHeight, boolean bottomRounded) {
      if (!(customHeight <= 0.0D)) {
         float settingsY = (float)(this.y + this.height);
         float settingsHeight = (float)customHeight;
         boolean gradientMenus = ClickGUIModule.useGradientMenus() && this.module.isEnabled();
         Color lightBg = ClickGUIModule.getColor(ClickGUIModule.getSettingsBackground(), alpha);
         Color gradientStart = ClickGUIModule.getColor(ClickGUIModule.getModuleEnabledGradientStart(), alpha * 0.92D);
         Color gradientEnd = ClickGUIModule.getColor(ClickGUIModule.getModuleEnabledGradientEnd(), alpha * 0.92D);
         Color glassOverlay = new Color(255, 255, 255, (int)(6.0D * alpha));
         if (bottomRounded) {
            if (gradientMenus) {
               this.drawBottomRoundedGradient(context, (float)this.x, settingsY, (float)this.width, settingsHeight, gradientStart, gradientEnd);
            } else {
               this.drawBottomRoundedFill(context, (float)this.x, settingsY, (float)this.width, settingsHeight, lightBg);
            }

         } else {
            if (gradientMenus) {
               Renderer.get().drawRoundedRectGradient(context, (float)this.x, settingsY, (float)this.width, settingsHeight, 0.0F, gradientStart, gradientEnd, false);
            } else {
               Renderer.get().drawRect(context, (float)this.x, settingsY, (float)this.width, settingsHeight, lightBg);
            }

            Renderer.get().drawRect(context, (float)this.x, settingsY, (float)this.width, settingsHeight, glassOverlay);
         }
      }
   }

   private void drawHorizontalGradient(class_332 context, double alpha, boolean bottomRounded) {
      Color startColor = ClickGUIModule.getColor(ClickGUIModule.getModuleEnabledGradientStart(), alpha);
      Color endColor = ClickGUIModule.getColor(ClickGUIModule.getModuleEnabledGradientEnd(), alpha);
      if (bottomRounded) {
         this.drawBottomRoundedGradient(context, (float)this.x, (float)this.y, (float)this.width, (float)this.height, startColor, endColor);
      } else {
         Renderer.get().drawRoundedRectGradient(context, (float)this.x, (float)this.y, (float)this.width, (float)this.height, 0.0F, startColor, endColor, false);
      }
   }

   private void drawDisabledGlass(class_332 context, double alpha, boolean bottomRounded) {
      Color panelColor = ClickGUIModule.getColor(ClickGUIModule.getPanelBackground(), alpha);
      if (bottomRounded) {
         this.drawBottomRoundedFill(context, (float)this.x, (float)this.y, (float)this.width, (float)this.height, panelColor);
      } else {
         Renderer.get().drawRect(context, (float)this.x, (float)this.y, (float)this.width, (float)this.height, panelColor);
      }
   }

   private void drawBottomCutoffFill(class_332 context, float drawX, float drawY, float drawW, float drawH, Color color) {
      if (!(drawH <= 0.0F) && !(drawW <= 0.0F)) {
         float radius = Math.min(ClickGUIModule.getGuiCornerRadiusScaled(1.0D), Math.min(drawW * 0.5F, drawH * 0.5F));
         if (radius <= 0.5F) {
            Renderer.get().drawRect(context, drawX, drawY, drawW, drawH, color);
         } else {
            float capH = Math.min(drawH, radius);
            float rectH = drawH - capH;
            if (rectH > 0.0F) {
               Renderer.get().drawRect(context, drawX, drawY, drawW, rectH, color);
            }

            Renderer.get().scissor(context, drawX, drawY + rectH, drawW, capH);
            Renderer.get().drawRoundedRect(context, drawX, drawY + drawH - radius * 2.0F, drawW, radius * 2.0F, radius, color);
            Renderer.get().scissor(context, (float)this.x, (float)this.y, (float)this.width, (float)this.height);
         }
      }
   }

   private void drawBottomCutoffGradient(class_332 context, float drawX, float drawY, float drawW, float drawH, Color startColor, Color endColor) {
      if (!(drawH <= 0.0F) && !(drawW <= 0.0F)) {
         float radius = Math.min(ClickGUIModule.getGuiCornerRadiusScaled(1.0D), Math.min(drawW * 0.5F, drawH * 0.5F));
         if (radius <= 0.5F) {
            Renderer.get().drawRoundedRectGradient(context, drawX, drawY, drawW, drawH, 0.0F, startColor, endColor, false);
         } else {
            float capH = Math.min(drawH, radius);
            float rectH = drawH - capH;
            if (rectH > 0.0F) {
               Renderer.get().drawRoundedRectGradient(context, drawX, drawY, drawW, rectH, 0.0F, startColor, endColor, false);
            }

            Renderer.get().scissor(context, drawX, drawY + rectH, drawW, capH);
            Renderer.get().drawRoundedRectGradient(context, drawX, drawY + drawH - radius * 2.0F, drawW, radius * 2.0F, radius, startColor, endColor, false);
            Renderer.get().scissor(context, (float)this.x, (float)this.y, (float)this.width, (float)this.height);
         }
      }
   }

   private void drawBottomRoundedFill(class_332 context, float drawX, float drawY, float drawW, float drawH, Color color) {
      this.drawBottomCutoffFill(context, drawX, drawY, drawW, drawH, color);
   }

   private void drawBottomRoundedGradient(class_332 context, float drawX, float drawY, float drawW, float drawH, Color startColor, Color endColor) {
      this.drawBottomCutoffGradient(context, drawX, drawY, drawW, drawH, startColor, endColor);
   }

   private Color blend(Color a, Color b, float t) {
      float clamped = Math.max(0.0F, Math.min(1.0F, t));
      int r = (int)((float)a.getRed() + (float)(b.getRed() - a.getRed()) * clamped);
      int g = (int)((float)a.getGreen() + (float)(b.getGreen() - a.getGreen()) * clamped);
      int bl = (int)((float)a.getBlue() + (float)(b.getBlue() - a.getBlue()) * clamped);
      int al = (int)((float)a.getAlpha() + (float)(b.getAlpha() - a.getAlpha()) * clamped);
      return new Color(r, g, bl, al);
   }

   public void mouseClicked(double mouseX, double mouseY, int button) {
      if (this.isButtonHovered(mouseX, mouseY)) {
         if (button == 0) {
            this.module.toggle();
         } else if (button == 1 && !this.components.isEmpty()) {
            this.expanded = !this.expanded;
            if (!this.expanded) {
               Iterator var6 = this.components.iterator();

               while(var6.hasNext()) {
                  SettingComponent component = (SettingComponent)var6.next();
                  if (component instanceof ColorComponent) {
                     ColorComponent colorComponent = (ColorComponent)component;
                     colorComponent.setExpanded(false);
                  }
               }
            }
         }
      } else if (this.expanded && this.currentSettingHeight > 0.0D) {
         this.components.forEach((c) -> {
            c.mouseClicked(mouseX, mouseY, button);
         });
      }

   }

   public double getTotalHeight() {
      return this.height + Math.max(0.0D, this.currentSettingHeight);
   }

   private double getSettingsHeight() {
      if (this.components.isEmpty()) {
         return 0.0D;
      } else {
         double h = 6.0D;

         SettingComponent c;
         for(Iterator var3 = this.components.iterator(); var3.hasNext(); h += c.getComponentHeight() + 4.0D) {
            c = (SettingComponent)var3.next();
            if (!(c instanceof BooleanComponent)) {
               h += 14.0D;
            }
         }

         return h + 4.0D;
      }
   }

   private boolean isButtonHovered(double mouseX, double mouseY) {
      if (mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height) {
         boolean bottomRoundedRow = this.isLastButton && this.currentSettingHeight <= 2.0D;
         if (!bottomRoundedRow) {
            return true;
         } else {
            double radius = Math.min((double)ClickGUIModule.getGuiCornerRadiusScaled(1.0D), Math.min(this.width * 0.5D, this.height * 0.5D));
            if (radius <= 0.5D) {
               return true;
            } else {
               double relX = mouseX - this.x;
               double relY = mouseY - this.y;
               double cornerStartY = this.height - radius;
               if (relY <= cornerStartY) {
                  return true;
               } else {
                  double dy;
                  double dx;
                  if (relX < radius) {
                     dx = relX - radius;
                     dy = relY - cornerStartY;
                     return dx * dx + dy * dy <= radius * radius;
                  } else if (relX > this.width - radius) {
                     dx = relX - (this.width - radius);
                     dy = relY - cornerStartY;
                     return dx * dx + dy * dy <= radius * radius;
                  } else {
                     return true;
                  }
               }
            }
         }
      } else {
         return false;
      }
   }

   public void keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.expanded) {
         this.components.forEach((c) -> {
            c.keyPressed(keyCode, scanCode, modifiers);
         });
      }

   }

   public void charTyped(char chr, int modifiers) {
      if (this.expanded) {
         this.components.forEach((c) -> {
            c.charTyped(chr, modifiers);
         });
      }

   }

   public void mouseReleased(double mouseX, double mouseY, int button) {
      if (this.expanded) {
         this.components.forEach((c) -> {
            c.mouseReleased(mouseX, mouseY, button);
         });
      }

   }

   private static String round(double value, int places) {
      if (places < 0) {
         throw new IllegalArgumentException();
      } else {
         BigDecimal bd = new BigDecimal(Double.toString(value));
         bd = bd.setScale(places, RoundingMode.HALF_UP);
         return bd.toString();
      }
   }

   public Module getModule() {
      return this.module;
   }
}
