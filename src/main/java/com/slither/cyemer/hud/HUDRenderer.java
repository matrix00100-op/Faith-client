package com.slither.cyemer.hud;

import com.slither.cyemer.Faith;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_9779;

@Environment(EnvType.CLIENT)
public class HUDRenderer implements HudRenderCallback {
   public void onHudRender(class_332 drawContext, class_9779 tickCounter) {
      if (!Faith.selfDestructed) {
         if (class_310.method_1551().field_1755 == null) {
            HUDManager.getInstance().render(drawContext, tickCounter.method_60637(true));
         }

      }
   }
}
