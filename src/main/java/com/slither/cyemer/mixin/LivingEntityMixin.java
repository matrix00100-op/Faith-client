package com.slither.cyemer.mixin;

import com.slither.cyemer.module.implementation.HitAnimations;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({class_1309.class})
public abstract class LivingEntityMixin {
   @Shadow
   protected boolean field_6252;
   @Shadow
   protected int field_6279;

   @Inject(
      method = {"method_6028()I"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void modifySwingDuration(CallbackInfoReturnable<Integer> info) {
      HitAnimations module = HitAnimations.getInstance();
      if (module != null && module.isEnabled()) {
         int defaultDuration = 6;
         double speed = module.getSwingSpeed();
         if (speed <= 0.1D) {
            speed = 0.1D;
         }

         int newDuration = (int)((double)defaultDuration / speed);
         info.setReturnValue(newDuration);
      }

   }

   @Inject(
      method = {"method_23667(Lnet/minecraft/class_1268;Z)V"},
      at = {@At("HEAD")}
   )
   private void allowReSwing(class_1268 hand, boolean fromServer, CallbackInfo ci) {
      HitAnimations module = HitAnimations.getInstance();
      if (module != null && module.isEnabled()) {
         this.field_6252 = false;
         this.field_6279 = -1;
      }

   }
}
