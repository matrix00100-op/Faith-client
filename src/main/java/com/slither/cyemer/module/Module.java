package com.slither.cyemer.module;

import com.slither.cyemer.Faith;
import com.slither.cyemer.gui.new_ui.notifications.Notification;
import com.slither.cyemer.module.implementation.KeybindSetting;
import com.slither.cyemer.module.implementation.Notifications;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class Module {
   protected class_310 mc;
   private String name;
   private String description;
   private final Category category;
   private boolean enabled;
   private boolean expanded;
   public double animatedHeight;
   private int keyCode;
   private boolean binding;
   public double settingsAnimation;
   private long bindStartTime;
   private final List<Setting> settings;

   public Module(String name, String description, Category category) {
      this.mc = class_310.method_1551();
      this.keyCode = -1;
      this.settingsAnimation = 0.0D;
      this.bindStartTime = 0L;
      this.settings = new ArrayList();
      this.name = name;
      this.description = description;
      this.category = category;
      this.animatedHeight = 14.0D;
      this.addSetting(new KeybindSetting("Keybind", this));
   }

   public Module(String name, Category category) {
      this(name, "", category);
   }

   protected void addSetting(Setting setting) {
      this.settings.add(setting);
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onTick() {
   }

   public void onRender(class_332 context, float tickDelta) {
   }

   public void onWorldRender(class_4587 matrices, float tickDelta) {
   }

   public void onMs() {
   }

   public void toggle() {
      this.setEnabled(!this.isEnabled());
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public Category getCategory() {
      return this.category;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      if (this.enabled != enabled) {
         this.enabled = enabled;
         if (this.enabled) {
            this.onEnable();
         } else {
            this.onDisable();
         }

         if (!this.getName().equals("Notifications")) {
            try {
               Module notificationsModule = Faith.getInstance().getModuleManager().getModule("Notifications");
               if (notificationsModule instanceof Notifications) {
                  Notifications notifications = (Notifications)notificationsModule;
                  if (this.enabled) {
                     notifications.show(this.getName(), "Enabled", Notification.NotificationType.SUCCESS);
                  } else {
                     notifications.show(this.getName(), "Disabled", Notification.NotificationType.ERROR);
                  }
               }
            } catch (Exception var4) {
            }

         }
      }
   }

   public boolean isExpanded() {
      return this.expanded;
   }

   public void setExpanded(boolean expanded) {
      this.expanded = expanded;
   }

   public Setting getSetting(String name) {
      Iterator var2 = this.settings.iterator();

      Setting setting;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         setting = (Setting)var2.next();
      } while(!setting.getName().equalsIgnoreCase(name));

      return setting;
   }

   public List<Setting> getSettings() {
      return this.settings;
   }

   public int getKeyCode() {
      return this.keyCode;
   }

   public void setKeyCode(int keyCode) {
      this.keyCode = keyCode;
   }

   public boolean isBinding() {
      return this.binding;
   }

   public void setBinding(boolean binding) {
      this.binding = binding;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public String getKeyDisplayName() {
      if (this.keyCode == -1) {
         return "NONE";
      } else if (this.keyCode < 0) {
         int button = this.keyCode + 100;
         return "MOUSE" + (button + 1);
      } else {
         switch(this.keyCode) {
         case 256:
            return "ESC";
         case 260:
            return "INSERT";
         case 261:
            return "DELETE";
         case 266:
            return "PAGE UP";
         case 267:
            return "PAGE DOWN";
         case 283:
            return "PRINT SCREEN";
         case 340:
            return "LSHIFT";
         case 341:
            return "LCTRL";
         case 342:
            return "LALT";
         case 344:
            return "RSHIFT";
         case 345:
            return "RCTRL";
         case 346:
            return "RALT";
         default:
            String name = GLFW.glfwGetKeyName(this.keyCode, 0);
            return name == null ? "UNKNOWN" : name.toUpperCase();
         }
      }
   }

   public long getBindStartTime() {
      return this.bindStartTime;
   }

   public void setBindStartTime(long bindStartTime) {
      this.bindStartTime = bindStartTime;
   }
}
