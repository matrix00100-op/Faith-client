package com.slither.cyemer.module.implementation;

import com.slither.cyemer.module.Category;
import com.slither.cyemer.module.ModeSetting;
import com.slither.cyemer.module.Module;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class GuiModule extends Module {
   public GuiModule() {
      super("Gui", Category.CLIENT);
      this.addSetting(new ModeSetting("Style", new String[]{"Blob", "Panels"}));
   }
}
