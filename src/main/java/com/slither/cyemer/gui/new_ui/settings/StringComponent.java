package com.slither.cyemer.gui.new_ui.settings;

import com.slither.cyemer.module.StringSetting;
import com.slither.cyemer.module.implementation.ClickGUIModule;
import com.slither.cyemer.util.Renderer;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;

@Environment(EnvType.CLIENT)
public class StringComponent extends SettingComponent {
   private final StringSetting stringSetting;
   private boolean isEditing = false;
   private long lastInteractionTime = 0L;
   private double focusAnimation = 0.0D;
   private long lastFrame = System.currentTimeMillis();

   public StringComponent(StringSetting setting) {
      super(setting);
      this.stringSetting = setting;
   }

   public void render(class_332 context, int mouseX, int mouseY, float delta, double alpha) {
      long now = System.currentTimeMillis();
      float deltaTime = (float)(now - this.lastFrame) / 1000.0F;
      this.lastFrame = now;
      double targetFocus = this.isEditing ? 1.0D : 0.0D;
      this.focusAnimation += (targetFocus - this.focusAnimation) * Math.min(1.0D, (double)(8.0F * deltaTime));
      float fontSize = 9.0F;
      float boxRadius = 6.0F;
      Color bgColor = ClickGUIModule.getColor(ClickGUIModule.getSearchBoxBackground(), alpha * (0.5D + this.focusAnimation * 0.3D));
      Renderer.get().drawRoundedRect(context, (float)this.x, (float)this.y, (float)this.width, (float)this.height, boxRadius, bgColor);
      if (this.focusAnimation > 0.01D) {
         Color borderColor = ClickGUIModule.getColor(ClickGUIModule.getModuleEnabledText(), alpha * this.focusAnimation);
         float borderWidth = 1.0F + (float)(this.focusAnimation * 0.5D);
         Renderer.get().drawRoundedRectOutline(context, (float)this.x, (float)this.y, (float)this.width, (float)this.height, boxRadius, borderWidth, borderColor);
      }

      String textToDisplay = this.stringSetting.getValue();
      if (textToDisplay.isEmpty() && !this.isEditing) {
         textToDisplay = "Enter text...";
      } else if (this.isEditing && (System.currentTimeMillis() - this.lastInteractionTime) % 1000L < 500L) {
         textToDisplay = textToDisplay + "|";
      }

      double padding = 4.0D;

      for(double availableWidth = this.width - padding * 2.0D; (double)Renderer.get().getTextWidth(textToDisplay, fontSize) > availableWidth && !textToDisplay.isEmpty(); textToDisplay = textToDisplay.substring(1)) {
      }

      float textX = (float)(this.x + padding);
      float textY = (float)(this.y + (this.height - (double)fontSize) / 2.0D);
      Color textColor;
      if (this.stringSetting.getValue().isEmpty() && !this.isEditing) {
         textColor = ClickGUIModule.getColor(ClickGUIModule.getModuleDisabledText(), alpha * 0.5D);
      } else {
         textColor = ClickGUIModule.getColor(ClickGUIModule.getSearchBoxText(), alpha);
      }

      Renderer.get().drawText(context, textToDisplay, textX, textY, fontSize, textColor, false);
   }

   public void mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
         boolean wasEditing = this.isEditing;
         this.isEditing = this.isHovered(mouseX, mouseY);
         if (this.isEditing && !wasEditing) {
            this.lastInteractionTime = System.currentTimeMillis();
         }
      }

   }

   public void mouseReleased(double mouseX, double mouseY, int button) {
   }

   public void charTyped(char chr, int modifiers) {
      if (this.isEditing && chr >= ' ' && chr <= '~') {
         StringSetting var10000 = this.stringSetting;
         String var10001 = this.stringSetting.getValue();
         var10000.setValue(var10001 + chr);
         this.lastInteractionTime = System.currentTimeMillis();
      }

   }

   public void keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.isEditing) {
         this.lastInteractionTime = System.currentTimeMillis();
         String currentString = this.stringSetting.getValue();
         if (keyCode == 259) {
            if (!currentString.isEmpty()) {
               this.stringSetting.setValue(currentString.substring(0, currentString.length() - 1));
            }
         } else if (keyCode == 86 && (modifiers & 10) != 0) {
            String clipboard = class_310.method_1551().field_1774.method_1460();
            this.stringSetting.setValue(currentString + clipboard);
         } else if (keyCode == 256 || keyCode == 257) {
            this.isEditing = false;
         }

      }
   }

   public double getComponentHeight() {
      return 14.0D;
   }
}
