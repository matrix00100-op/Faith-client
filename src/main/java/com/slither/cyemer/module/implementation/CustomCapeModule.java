package com.slither.cyemer.module.implementation;

import com.slither.cyemer.module.Category;
import com.slither.cyemer.module.ModeSetting;
import com.slither.cyemer.module.Module;
import com.slither.cyemer.util.CapeTextureManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_332;

@Environment(EnvType.CLIENT)
public class CustomCapeModule extends Module {
   public static CustomCapeModule INSTANCE;
   private final ModeSetting capeName = (ModeSetting)(new ModeSetting("Cape", new String[]{"cyemer", "weedhack", "astolfo", "kitty", "frieren", "snowoman"})).onChange(() -> {
      if (this.isEnabled()) {
         this.updateCape();
      }

   });

   public CustomCapeModule() {
      super("CustomCape", "Renders a high-resolution custom cape.", Category.RENDER);
      this.addSetting(this.capeName);
      INSTANCE = this;
   }

   public void updateCape() {
      CapeTextureManager.loadCapeTexture(this.capeName.getCurrentMode());
   }

   public void onEnable() {
      this.updateCape();
   }

   public void onDisable() {
   }

   public void onTick() {
   }

   public void onRender(class_332 context, float tickDelta) {
   }
}
