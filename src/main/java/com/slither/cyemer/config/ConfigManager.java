package com.slither.cyemer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.slither.cyemer.Faith;
import com.slither.cyemer.gui.new_ui.ClickGUI;
import com.slither.cyemer.gui.new_ui.FriendlistPanel;
import com.slither.cyemer.gui.new_ui.Panel;
import com.slither.cyemer.hud.HUDElement;
import com.slither.cyemer.module.BooleanSetting;
import com.slither.cyemer.module.ColorSetting;
import com.slither.cyemer.module.ModeSetting;
import com.slither.cyemer.module.Module;
import com.slither.cyemer.module.Setting;
import com.slither.cyemer.module.SliderSetting;
import com.slither.cyemer.module.StringSetting;
import com.slither.cyemer.theme.ThemeManager;
import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public class ConfigManager {
   private final Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
   private final File configsDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), "cyemer/configs");
   private Map<String, ConfigManager.PanelPosition> panelPositions = new HashMap();
   private static final ConfigManager INSTANCE = new ConfigManager();

   public static ConfigManager getInstance() {
      return INSTANCE;
   }

   public ConfigManager() {
      if (!this.configsDir.exists()) {
         this.configsDir.mkdirs();
      }

   }

   public void load(String name) {
      File configFile = new File(this.configsDir, name + ".json");
      if (!configFile.exists()) {
         if ("default".equals(name)) {
            ThemeManager.getInstance().setCurrentTheme("dark");
            Module guiModule = Faith.INSTANCE.getModuleManager().getModule("Gui");
            if (guiModule != null) {
               Setting styleSetting = guiModule.getSetting("Style");
               if (styleSetting instanceof ModeSetting) {
                  ModeSetting ms = (ModeSetting)styleSetting;
                  ms.setCurrentMode("Panels");
               }
            }

            this.save("default", new ArrayList());
         }

      } else {
         try {
            FileReader reader = new FileReader(configFile);

            label51: {
               try {
                  ConfigManager.ConfigData data = (ConfigManager.ConfigData)this.gson.fromJson(reader, ConfigManager.ConfigData.class);
                  if (data != null) {
                     this.applyConfig(data);
                     this.panelPositions = (Map)(data.panelPositions != null ? data.panelPositions : new HashMap());
                     break label51;
                  }
               } catch (Throwable var7) {
                  try {
                     reader.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }

                  throw var7;
               }

               reader.close();
               return;
            }

            reader.close();
         } catch (Exception var8) {
            var8.printStackTrace();
         }

      }
   }

   public void loadModule(String configName, Module module) {
      if (configName != null && !configName.isEmpty() && module != null) {
         if (!"Booster".equalsIgnoreCase(module.getName())) {
            File configFile = new File(this.configsDir, configName + ".json");
            if (configFile.exists()) {
               try {
                  FileReader reader = new FileReader(configFile);

                  label65: {
                     label64: {
                        try {
                           ConfigManager.ConfigData data = (ConfigManager.ConfigData)this.gson.fromJson(reader, ConfigManager.ConfigData.class);
                           if (data == null || data.modules == null) {
                              break label64;
                           }

                           ConfigManager.ModuleData moduleData = (ConfigManager.ModuleData)data.modules.get(module.getName());
                           if (moduleData != null) {
                              module.setEnabled(moduleData.enabled);
                              module.setKeyCode(moduleData.keyCode);
                              if (moduleData.settings == null) {
                                 break label65;
                              }

                              Iterator var7 = module.getSettings().iterator();

                              while(true) {
                                 if (!var7.hasNext()) {
                                    break label65;
                                 }

                                 Setting setting = (Setting)var7.next();
                                 this.loadSetting(moduleData.settings, setting);
                              }
                           }
                        } catch (Throwable var10) {
                           try {
                              reader.close();
                           } catch (Throwable var9) {
                              var10.addSuppressed(var9);
                           }

                           throw var10;
                        }

                        reader.close();
                        return;
                     }

                     reader.close();
                     return;
                  }

                  reader.close();
               } catch (Exception var11) {
               }

            }
         }
      }
   }

   public void save(String name, List<Panel> panels) {
      File configFile = new File(this.configsDir, name + ".json");
      ConfigManager.ConfigData data = new ConfigManager.ConfigData();
      data.theme = ThemeManager.getInstance().getCurrentTheme().name;
      data.hudElements = new HashMap();
      HUDElement element;
      ConfigManager.HUDElementData elementData;
      Iterator var9;
      Setting setting;
      if (Faith.INSTANCE != null && Faith.INSTANCE.getHudManager() != null) {
         for(Iterator var5 = Faith.INSTANCE.getHudManager().getElements().iterator(); var5.hasNext(); data.hudElements.put(element.getName(), elementData)) {
            element = (HUDElement)var5.next();
            elementData = new ConfigManager.HUDElementData(element.getX(), element.getY(), element.isEnabled());
            elementData.settings = new JsonObject();
            List<Setting> settings = element.getSettings();
            if (settings != null) {
               var9 = settings.iterator();

               while(var9.hasNext()) {
                  setting = (Setting)var9.next();
                  this.saveSetting(elementData.settings, setting);
               }
            }
         }
      }

      Module guiModule = Faith.INSTANCE.getModuleManager().getModule("Gui");
      if (guiModule != null) {
         Setting styleSetting = guiModule.getSetting("Style");
         if (styleSetting instanceof ModeSetting) {
            ModeSetting ms = (ModeSetting)styleSetting;
            data.guiStyle = ms.getCurrentMode();
         }
      }

      data.modules = new HashMap();
      Iterator var16 = Faith.INSTANCE.getModuleManager().getModules().iterator();

      while(true) {
         Module module;
         do {
            if (!var16.hasNext()) {
               if (panels != null) {
                  data.panelPositions = new HashMap();

                  Panel var20;
                  for(var16 = panels.iterator(); var16.hasNext(); var20 = (Panel)var16.next()) {
                  }
               } else {
                  data.panelPositions = this.panelPositions;
               }

               data.cameraX = ClickGUI.cameraX;
               data.cameraY = ClickGUI.cameraY;
               data.zoom = ClickGUI.zoom;
               this.panelPositions = data.panelPositions;

               try {
                  FileWriter writer = new FileWriter(configFile);

                  try {
                     this.gson.toJson(data, writer);
                  } catch (Throwable var12) {
                     try {
                        writer.close();
                     } catch (Throwable var11) {
                        var12.addSuppressed(var11);
                     }

                     throw var12;
                  }

                  writer.close();
               } catch (IOException var13) {
                  var13.printStackTrace();
               }

               return;
            }

            module = (Module)var16.next();
         } while("Booster".equalsIgnoreCase(module.getName()));

         ConfigManager.ModuleData moduleData = new ConfigManager.ModuleData();
         moduleData.enabled = module.isEnabled();
         moduleData.keyCode = module.getKeyCode();
         moduleData.settings = new JsonObject();
         var9 = module.getSettings().iterator();

         while(var9.hasNext()) {
            setting = (Setting)var9.next();
            this.saveSetting(moduleData.settings, setting);
         }

         data.modules.put(module.getName(), moduleData);
      }
   }

   public void save(String name, List<Panel> panels, FriendlistPanel friendlistPanel) {
      File configFile = new File(this.configsDir, name + ".json");
      ConfigManager.ConfigData data = new ConfigManager.ConfigData();
      data.theme = ThemeManager.getInstance().getCurrentTheme().name;
      data.hudElements = new HashMap();
      HUDElement element;
      ConfigManager.HUDElementData elementData;
      Iterator var10;
      Setting setting;
      if (Faith.INSTANCE != null && Faith.INSTANCE.getHudManager() != null) {
         for(Iterator var6 = Faith.INSTANCE.getHudManager().getElements().iterator(); var6.hasNext(); data.hudElements.put(element.getName(), elementData)) {
            element = (HUDElement)var6.next();
            elementData = new ConfigManager.HUDElementData(element.getX(), element.getY(), element.isEnabled());
            elementData.settings = new JsonObject();
            List<Setting> settings = element.getSettings();
            if (settings != null) {
               var10 = settings.iterator();

               while(var10.hasNext()) {
                  setting = (Setting)var10.next();
                  this.saveSetting(elementData.settings, setting);
               }
            }
         }
      }

      Module guiModule = Faith.INSTANCE.getModuleManager().getModule("Gui");
      if (guiModule != null) {
         Setting styleSetting = guiModule.getSetting("Style");
         if (styleSetting instanceof ModeSetting) {
            ModeSetting ms = (ModeSetting)styleSetting;
            data.guiStyle = ms.getCurrentMode();
         }
      }

      data.modules = new HashMap();
      Iterator var17 = Faith.INSTANCE.getModuleManager().getModules().iterator();

      while(true) {
         Module module;
         do {
            if (!var17.hasNext()) {
               if (panels != null) {
                  data.panelPositions = new HashMap();

                  Panel var21;
                  for(var17 = panels.iterator(); var17.hasNext(); var21 = (Panel)var17.next()) {
                  }

                  if (friendlistPanel != null) {
                     data.panelPositions.put("FRIENDLIST", new ConfigManager.PanelPosition(friendlistPanel.getX(), friendlistPanel.getY()));
                  }
               } else {
                  data.panelPositions = this.panelPositions;
               }

               data.cameraX = ClickGUI.cameraX;
               data.cameraY = ClickGUI.cameraY;
               data.zoom = ClickGUI.zoom;
               this.panelPositions = data.panelPositions;

               try {
                  FileWriter writer = new FileWriter(configFile);

                  try {
                     this.gson.toJson(data, writer);
                  } catch (Throwable var13) {
                     try {
                        writer.close();
                     } catch (Throwable var12) {
                        var13.addSuppressed(var12);
                     }

                     throw var13;
                  }

                  writer.close();
               } catch (IOException var14) {
                  var14.printStackTrace();
               }

               return;
            }

            module = (Module)var17.next();
         } while("Booster".equalsIgnoreCase(module.getName()));

         ConfigManager.ModuleData moduleData = new ConfigManager.ModuleData();
         moduleData.enabled = module.isEnabled();
         moduleData.keyCode = module.getKeyCode();
         moduleData.settings = new JsonObject();
         var10 = module.getSettings().iterator();

         while(var10.hasNext()) {
            setting = (Setting)var10.next();
            this.saveSetting(moduleData.settings, setting);
         }

         data.modules.put(module.getName(), moduleData);
      }
   }

   private void saveSetting(JsonObject jsonObject, Setting setting) {
      if (setting instanceof ModeSetting) {
         ModeSetting s = (ModeSetting)setting;
         jsonObject.addProperty(s.getName(), s.getCurrentMode());
      } else if (setting instanceof SliderSetting) {
         SliderSetting s = (SliderSetting)setting;
         jsonObject.addProperty(s.getName(), s.getPreciseValue());
      } else if (setting instanceof ColorSetting) {
         ColorSetting s = (ColorSetting)setting;
         jsonObject.addProperty(s.getName(), s.getValue().getRGB());
      } else if (setting instanceof BooleanSetting) {
         BooleanSetting s = (BooleanSetting)setting;
         jsonObject.addProperty(s.getName(), s.isEnabled());
      } else if (setting instanceof StringSetting) {
         StringSetting s = (StringSetting)setting;
         jsonObject.addProperty(s.getName(), s.getValue());
      }

   }

   private void loadSetting(JsonObject jsonObject, Setting setting) {
      if (jsonObject.has(setting.getName())) {
         try {
            if (setting instanceof ModeSetting) {
               ModeSetting s = (ModeSetting)setting;
               s.setCurrentMode(jsonObject.get(s.getName()).getAsString());
            } else if (setting instanceof SliderSetting) {
               SliderSetting s = (SliderSetting)setting;
               s.setValue(jsonObject.get(s.getName()).getAsDouble());
            } else if (setting instanceof ColorSetting) {
               ColorSetting s = (ColorSetting)setting;
               s.setValue(new Color(jsonObject.get(s.getName()).getAsInt(), true));
            } else if (setting instanceof BooleanSetting) {
               BooleanSetting s = (BooleanSetting)setting;
               s.setEnabled(jsonObject.get(s.getName()).getAsBoolean());
            } else if (setting instanceof StringSetting) {
               StringSetting s = (StringSetting)setting;
               s.setValue(jsonObject.get(s.getName()).getAsString());
            }
         } catch (Exception var8) {
            PrintStream var10000 = System.err;
            String var10001 = setting.getName();
            var10000.println("Failed to load setting: " + var10001 + " - " + var8.getMessage());
         }

      }
   }

   private void applyConfig(ConfigManager.ConfigData data) {
      if (data.theme != null) {
         ThemeManager.getInstance().setCurrentTheme(data.theme);
      }

      if (data.cameraX != null) {
         ClickGUI.cameraX = data.cameraX;
      }

      if (data.cameraY != null) {
         ClickGUI.cameraY = data.cameraY;
      }

      if (data.zoom != null) {
         ClickGUI.zoom = data.zoom;
      }

      if (data.hudElements != null && Faith.INSTANCE != null && Faith.INSTANCE.getHudManager() != null) {
         Iterator var2 = data.hudElements.entrySet().iterator();

         label90:
         while(true) {
            ConfigManager.HUDElementData elementData;
            List settings;
            do {
               HUDElement element;
               do {
                  Entry entry;
                  do {
                     if (!var2.hasNext()) {
                        break label90;
                     }

                     entry = (Entry)var2.next();
                     element = Faith.INSTANCE.getHudManager().getElement((String)entry.getKey());
                  } while(element == null);

                  elementData = (ConfigManager.HUDElementData)entry.getValue();
                  element.setX(elementData.x);
                  element.setY(elementData.y);
                  element.setEnabled(elementData.enabled);
               } while(elementData.settings == null);

               settings = element.getSettings();
            } while(settings == null);

            Iterator var7 = settings.iterator();

            while(var7.hasNext()) {
               Setting setting = (Setting)var7.next();
               this.loadSetting(elementData.settings, setting);
            }
         }
      }

      Module guiModule = Faith.INSTANCE.getModuleManager().getModule("Gui");
      if (guiModule != null && data.guiStyle != null) {
         Setting styleSetting = guiModule.getSetting("Style");
         if (styleSetting instanceof ModeSetting) {
            ModeSetting ms = (ModeSetting)styleSetting;
            ms.setCurrentMode(data.guiStyle);
         }
      }

      if (data.modules != null) {
         Iterator var11 = (new ArrayList(Faith.INSTANCE.getModuleManager().getModules())).iterator();

         while(true) {
            Module module;
            ConfigManager.ModuleData moduleData;
            do {
               do {
                  do {
                     if (!var11.hasNext()) {
                        return;
                     }

                     module = (Module)var11.next();
                  } while("Booster".equalsIgnoreCase(module.getName()));

                  moduleData = (ConfigManager.ModuleData)data.modules.get(module.getName());
               } while(moduleData == null);

               module.setEnabled(moduleData.enabled);
               module.setKeyCode(moduleData.keyCode);
            } while(moduleData.settings == null);

            Iterator var15 = module.getSettings().iterator();

            while(var15.hasNext()) {
               Setting setting = (Setting)var15.next();
               this.loadSetting(moduleData.settings, setting);
            }
         }
      }
   }

   public void save(String name) {
      this.save(name, (List)null);
   }

   public boolean delete(String name) {
      if (name != null && !name.isEmpty() && !"default".equalsIgnoreCase(name)) {
         File configFile = new File(this.configsDir, name + ".json");
         if (configFile.exists()) {
            try {
               return Files.deleteIfExists(configFile.toPath());
            } catch (IOException var4) {
               var4.printStackTrace();
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public List<String> getAvailableConfigs() {
      File[] files = this.configsDir.listFiles((dir, name) -> {
         return name.toLowerCase().endsWith(".json");
      });
      return (List)(files == null ? new ArrayList() : (List)Arrays.stream(files).map((file) -> {
         return file.getName().substring(0, file.getName().length() - 5);
      }).collect(Collectors.toList()));
   }

   public Map<String, ConfigManager.PanelPosition> getPanelPositions() {
      return this.panelPositions;
   }

   @Environment(EnvType.CLIENT)
   private static class ConfigData {
      public String theme;
      public String guiStyle;
      public Map<String, ConfigManager.HUDElementData> hudElements = new HashMap();
      public Map<String, ConfigManager.ModuleData> modules = new HashMap();
      public Map<String, ConfigManager.PanelPosition> panelPositions = new HashMap();
      public Double cameraX;
      public Double cameraY;
      public Double zoom;
   }

   @Environment(EnvType.CLIENT)
   private static class ModuleData {
      public boolean enabled;
      public int keyCode;
      public JsonObject settings = new JsonObject();
   }

   @Environment(EnvType.CLIENT)
   private static class HUDElementData {
      public double x;
      public double y;
      public boolean enabled;
      public JsonObject settings;

      public HUDElementData(double x, double y, boolean enabled) {
         this.x = x;
         this.y = y;
         this.enabled = enabled;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class PanelPosition {
      public double x;
      public double y;

      public PanelPosition(double x, double y) {
         this.x = x;
         this.y = y;
      }
   }
}
