package com.slither.cyemer.module.implementation;

import com.slither.cyemer.mixin.ClientPlayerInteractionManagerMixin;
import com.slither.cyemer.module.Category;
import com.slither.cyemer.module.Module;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_636;

@Environment(EnvType.CLIENT)
public class NoBreakDelay extends Module {
   public NoBreakDelay() {
      super("NoBreakDelay", "Removes the delay between breaking blocks.", Category.PLAYER);
   }

   public void onTick() {
      if (this.mc.field_1724 != null && this.mc.field_1761 != null) {
         class_636 var2 = this.mc.field_1761;
         if (var2 instanceof ClientPlayerInteractionManagerMixin.ClientPlayerInteractionManagerAccessor) {
            ClientPlayerInteractionManagerMixin.ClientPlayerInteractionManagerAccessor accessor = (ClientPlayerInteractionManagerMixin.ClientPlayerInteractionManagerAccessor)var2;
            accessor.setBlockBreakingCooldown(0);
         }

      }
   }
}
