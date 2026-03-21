package com.slither.cyemer.mixin;

import com.slither.cyemer.module.implementation.Interface;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_332;
import net.minecraft.class_490;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({class_490.class})
public class InventoryScreenMixin {
   @Inject(
      method = {"method_2389(Lnet/minecraft/class_332;FII)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onDrawBackground(class_332 context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
      if (Interface.INSTANCE != null && Interface.INSTANCE.shouldCancelVanillaInventory) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"method_25394(Lnet/minecraft/class_332;IIF)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRender(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      class_490 self = (class_490)this;
      if (Interface.INSTANCE != null && Interface.INSTANCE.shouldCancelVanillaInventory) {
         Interface.INSTANCE.renderInventory(context, mouseX, mouseY, delta, self);
         ci.cancel();
      }

   }
}
