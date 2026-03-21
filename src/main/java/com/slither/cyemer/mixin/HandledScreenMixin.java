package com.slither.cyemer.mixin;

import com.slither.cyemer.module.implementation.Interface;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11908;
import net.minecraft.class_1735;
import net.minecraft.class_332;
import net.minecraft.class_465;
import net.minecraft.class_490;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({class_465.class})
public class HandledScreenMixin {
   @Inject(
      method = {"method_2385(Lnet/minecraft/class_332;Lnet/minecraft/class_1735;II)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onDrawSlot(class_332 context, class_1735 slot, int x, int y, CallbackInfo ci) {
      class_465<?> screen = (class_465)this;
      if (screen instanceof class_490 && Interface.INSTANCE != null && Interface.INSTANCE.shouldCancelVanillaInventory) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"method_64241(Lnet/minecraft/class_332;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onDrawSlotHighlightBack(class_332 context, CallbackInfo ci) {
      class_465<?> screen = (class_465)this;
      if (screen instanceof class_490 && Interface.INSTANCE != null && Interface.INSTANCE.shouldCancelVanillaInventory) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"method_64242(Lnet/minecraft/class_332;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onDrawSlotHighlightFront(class_332 context, CallbackInfo ci) {
      class_465<?> screen = (class_465)this;
      if (screen instanceof class_490 && Interface.INSTANCE != null && Interface.INSTANCE.shouldCancelVanillaInventory) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"method_25404(Lnet/minecraft/class_11908;)Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onKeyPressed(class_11908 keyInput, CallbackInfoReturnable<Boolean> cir) {
      class_465<?> screen = (class_465)this;
      if (screen instanceof class_490 && Interface.INSTANCE != null && Interface.INSTANCE.shouldCancelVanillaInventory) {
         boolean handled = Interface.INSTANCE.handleKeyPress((class_490)screen, keyInput);
         if (handled) {
            cir.setReturnValue(true);
         }
      }

   }
}
