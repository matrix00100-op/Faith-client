package com.slither.cyemer.gui.new_ui.settings;

import com.slither.cyemer.module.ModeSetting;
import com.slither.cyemer.module.implementation.ClickGUIModule;
import com.slither.cyemer.util.Renderer;
import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_332;

@Environment(EnvType.CLIENT)
public class ModeComponent extends SettingComponent {
   private static final float MODE_FONT_SIZE = 9.0F;
   private static final double MODE_HORIZONTAL_PADDING = 5.0D;
   private static final double MODE_VERTICAL_PADDING = 4.0D;
   private static final double MODE_OPTION_GAP_X = 3.0D;
   private static final double MODE_OPTION_GAP_Y = 3.0D;
   private final Map<String, Double> hoverAnimations = new HashMap();
   private final Map<String, Double> selectAnimations = new HashMap();
   private long lastFrame = System.currentTimeMillis();
   private String previousMode = null;

   public ModeComponent(ModeSetting setting) {
      super(setting);
      Iterator var2 = setting.getModes().iterator();

      while(var2.hasNext()) {
         String mode = (String)var2.next();
         this.hoverAnimations.put(mode, 0.0D);
         this.selectAnimations.put(mode, mode.equals(setting.getCurrentMode()) ? 1.0D : 0.0D);
      }

      this.previousMode = setting.getCurrentMode();
   }

   public double getBoxHeight() {
      double calculatedHeight = 8.0D;
      double currentX = 0.0D;
      double availableWidth = this.width - 10.0D;
      float rowHeight = 12.0F;
      if (!((ModeSetting)this.setting).getModes().isEmpty() && !(availableWidth <= 0.0D)) {
         calculatedHeight += (double)rowHeight;

         double modeWidth;
         for(Iterator var8 = ((ModeSetting)this.setting).getModes().iterator(); var8.hasNext(); currentX += modeWidth + 3.0D) {
            String mode = (String)var8.next();
            modeWidth = (double)(Renderer.get().getTextWidth(mode, 9.0F) + 10.0F);
            if (currentX + modeWidth > availableWidth && currentX != 0.0D) {
               calculatedHeight += (double)rowHeight + 3.0D;
               currentX = 0.0D;
            }
         }

         return calculatedHeight + 1.0D;
      } else {
         return calculatedHeight + (double)rowHeight;
      }
   }

   public void render(class_332 context, int mouseX, int mouseY, float delta, double alpha) {
      ModeSetting modeSet = (ModeSetting)this.setting;
      float fontSize = 9.0F;
      float rowHeight = fontSize + 3.0F;
      long now = System.currentTimeMillis();
      float deltaTime = (float)(now - this.lastFrame) / 1000.0F;
      this.lastFrame = now;
      String currentMode = modeSet.getCurrentMode();
      if (!currentMode.equals(this.previousMode)) {
         this.selectAnimations.put(this.previousMode, (Double)this.selectAnimations.getOrDefault(this.previousMode, 1.0D));
         this.selectAnimations.put(currentMode, 0.0D);
         this.previousMode = currentMode;
      }

      float boxRadius = Math.max(3.0F, Math.min(6.0F, (float)(this.getComponentHeight() * 0.25D)));
      Color boxColor = ClickGUIModule.getColor(ClickGUIModule.getSettingsBackground(), alpha * 0.6D);
      Color boxOverlay = new Color(255, 255, 255, (int)(5.0D * alpha));
      Renderer.get().drawRoundedRect(context, (float)this.x, (float)this.y, (float)this.width, (float)this.getComponentHeight(), boxRadius, boxColor);
      Renderer.get().drawRoundedRect(context, (float)this.x, (float)this.y, (float)this.width, (float)this.getComponentHeight(), boxRadius, boxOverlay);
      double innerX = this.x + 5.0D;
      double innerY = this.y + 4.0D;
      double optionX = innerX;
      double optionY = innerY;
      double availableWidth = this.width - 10.0D;

      double modeWidth;
      for(Iterator var27 = modeSet.getModes().iterator(); var27.hasNext(); optionX += modeWidth + 3.0D) {
         String mode = (String)var27.next();
         modeWidth = (double)(Renderer.get().getTextWidth(mode, fontSize) + 10.0F);
         if (optionX + modeWidth > innerX + availableWidth && optionX > innerX) {
            optionY += (double)rowHeight + 3.0D;
            optionX = innerX;
         }

         boolean isSelected = mode.equals(currentMode);
         boolean isHovered = (double)mouseX >= optionX && (double)mouseX <= optionX + modeWidth && (double)mouseY >= optionY && (double)mouseY <= optionY + (double)rowHeight;
         double targetHover = isHovered ? 1.0D : 0.0D;
         double currentHover = (Double)this.hoverAnimations.getOrDefault(mode, 0.0D);
         currentHover += (targetHover - currentHover) * Math.min(1.0D, (double)(12.0F * deltaTime));
         this.hoverAnimations.put(mode, currentHover);
         double targetSelect = isSelected ? 1.0D : 0.0D;
         double currentSelect = (Double)this.selectAnimations.getOrDefault(mode, isSelected ? 1.0D : 0.0D);
         if (isSelected && currentSelect < targetSelect) {
            currentSelect += (targetSelect - currentSelect) * Math.min(1.0D, (double)(15.0F * deltaTime));
         } else if (!isSelected && currentSelect > targetSelect) {
            currentSelect += (targetSelect - currentSelect) * Math.min(1.0D, (double)(10.0F * deltaTime));
         }

         currentSelect = Math.max(0.0D, Math.min(1.0D, currentSelect));
         this.selectAnimations.put(mode, currentSelect);
         float pillWidth = (float)modeWidth;
         float pillX = (float)optionX - 2.0F;
         float pillY = (float)optionY;
         float pillRadius = rowHeight / 2.0F;
         Color bgColor;
         double intensity;
         if (isSelected || currentSelect > 0.01D || currentHover > 0.01D) {
            if (isSelected && currentSelect >= 0.5D) {
               float b = 0.75F;
               Color borderColor = ClickGUIModule.getColor(ClickGUIModule.getModuleEnabledText(), alpha * 0.45D * Math.min(currentSelect, 1.0D));
               Renderer.get().drawRoundedRect(context, pillX - b, pillY - b, pillWidth + b * 2.0F, rowHeight + b * 2.0F, pillRadius + b, borderColor);
            }

            if (isSelected) {
               intensity = Math.min(currentSelect, 1.0D);
               bgColor = ClickGUIModule.getColor(ClickGUIModule.getModuleEnabledText(), alpha * 0.25D * intensity);
            } else {
               bgColor = ClickGUIModule.getColor(ClickGUIModule.getSettingsBackground(), alpha * 0.3D * currentHover);
            }

            Renderer.get().drawRoundedRect(context, pillX, pillY, pillWidth, rowHeight, pillRadius, bgColor);
         }

         if (isSelected) {
            intensity = Math.min(currentSelect, 1.0D);
            bgColor = ClickGUIModule.getColor(ClickGUIModule.getModuleEnabledText(), alpha * intensity);
         } else {
            intensity = 0.6D + currentHover * 0.3D;
            bgColor = ClickGUIModule.getColor(ClickGUIModule.getModuleDisabledText(), alpha * intensity);
         }

         float textY = (float)optionY + (rowHeight - fontSize) / 2.0F;
         Renderer.get().drawText(context, mode, (float)optionX + 3.0F, textY, fontSize, bgColor, false);
      }

   }

   public void mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0 && this.isHovered(mouseX, mouseY)) {
         ModeSetting modeSet = (ModeSetting)this.setting;
         float fontSize = 9.0F;
         float rowHeight = fontSize + 3.0F;
         double innerX = this.x + 5.0D;
         double innerY = this.y + 4.0D;
         double optionX = innerX;
         double optionY = innerY;
         double availableWidth = this.width - 10.0D;

         double modeWidth;
         for(Iterator var19 = modeSet.getModes().iterator(); var19.hasNext(); optionX += modeWidth + 3.0D) {
            String mode = (String)var19.next();
            modeWidth = (double)(Renderer.get().getTextWidth(mode, fontSize) + 10.0F);
            if (optionX + modeWidth > innerX + availableWidth && optionX > innerX) {
               optionY += (double)rowHeight + 3.0D;
               optionX = innerX;
            }

            boolean hovered = mouseX >= optionX && mouseX <= optionX + modeWidth && mouseY >= optionY && mouseY <= optionY + (double)rowHeight;
            if (hovered) {
               modeSet.setCurrentMode(mode);
               break;
            }
         }

      }
   }

   public void mouseReleased(double mouseX, double mouseY, int button) {
   }

   public double getComponentHeight() {
      return this.getBoxHeight();
   }
}
