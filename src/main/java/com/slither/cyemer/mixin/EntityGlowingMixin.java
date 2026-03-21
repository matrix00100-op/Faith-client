package com.slither.cyemer.mixin;

import com.slither.cyemer.Faith;
import com.slither.cyemer.module.Module;
import com.slither.cyemer.module.implementation.ESP;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({class_1297.class})
public class EntityGlowingMixin {
   @Inject(
      method = {"method_5851()Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onIsGlowing(CallbackInfoReturnable<Boolean> cir) {
      if (!Faith.selfDestructed) {
         try {
            Module espModule = Faith.getInstance().getModuleManager().getModule("ESP");
            if (espModule instanceof ESP) {
               ESP esp = (ESP)espModule;
               if (esp.isEnabled() && esp.shouldApplyGlow()) {
                  class_1297 entity = (class_1297)this;
                  class_310 mc = class_310.method_1551();
                  if (entity instanceof class_1657 && entity != mc.field_1724 && entity.method_5805()) {
                     cir.setReturnValue(true);
                  }
               }
            }
         } catch (Exception var6) {
         }

      }
   }
}
