package com.slither.cyemer.mixin;

import com.slither.cyemer.module.implementation.HitAnimations;
import com.slither.cyemer.module.implementation.ViewModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11659;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_4587;
import net.minecraft.class_742;
import net.minecraft.class_759;
import net.minecraft.class_811;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({class_759.class})
public abstract class HeldItemRendererMixin {
   private static final ThreadLocal<class_1268> currentHand = new ThreadLocal();
   private static final ThreadLocal<class_1799> currentItem = new ThreadLocal();

   @ModifyVariable(
      method = {"method_3228(Lnet/minecraft/class_742;FFLnet/minecraft/class_1268;FLnet/minecraft/class_1799;FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;I)V"},
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 3
   )
   private float modifyEquipProgress(float originalEquipProgress, class_742 player) {
      HitAnimations module = HitAnimations.getInstance();
      return module != null && module.isEnabled() && module.isInstantEquipEnabled() && !player.method_6115() ? 0.0F : originalEquipProgress;
   }

   @Inject(
      method = {"method_3217(Lnet/minecraft/class_4587;Lnet/minecraft/class_1306;F)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onApplySwingOffset(class_4587 matrices, class_1306 arm, float swingProgress, CallbackInfo ci) {
      HitAnimations module = HitAnimations.getInstance();
      if (module != null && module.isEnabled()) {
         class_1268 hand = (class_1268)currentHand.get();
         class_1799 stack = (class_1799)currentItem.get();
         if (hand == null || stack == null) {
            return;
         }

         boolean applied = module.onRenderFirstPerson(matrices, swingProgress, stack, hand);
         if (applied) {
            ci.cancel();
         }
      }

   }

   @Inject(
      method = {"method_3228(Lnet/minecraft/class_742;FFLnet/minecraft/class_1268;FLnet/minecraft/class_1799;FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;I)V"},
      at = {@At("HEAD")}
   )
   private void onRenderFirstPersonItemHead(class_742 player, float tickDelta, float pitch, class_1268 hand, float swingProgress, class_1799 item, float equipProgress, class_4587 matrices, class_11659 queue, int light, CallbackInfo ci) {
      currentHand.set(hand);
      currentItem.set(item);
      ViewModel module = ViewModel.getInstance();
      if (module != null && module.isEnabled() && module.getApplyToMode().equals("Both") && hand == class_1268.field_5808) {
         matrices.method_22903();
         module.applyTransformations(matrices);
      }

   }

   @Inject(
      method = {"method_3228(Lnet/minecraft/class_742;FFLnet/minecraft/class_1268;FLnet/minecraft/class_1799;FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;I)V"},
      at = {@At("TAIL")}
   )
   private void onRenderFirstPersonItemTail(class_742 player, float tickDelta, float pitch, class_1268 hand, float swingProgress, class_1799 item, float equipProgress, class_4587 matrices, class_11659 queue, int light, CallbackInfo ci) {
      ViewModel module = ViewModel.getInstance();
      if (module != null && module.isEnabled() && module.getApplyToMode().equals("Both") && hand == class_1268.field_5808) {
         matrices.method_22909();
      }

      currentHand.remove();
      currentItem.remove();
   }

   @Inject(
      method = {"method_3233(Lnet/minecraft/class_1309;Lnet/minecraft/class_1799;Lnet/minecraft/class_811;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;I)V"},
      at = {@At("HEAD")}
   )
   private void onRenderItemHead(class_1309 entity, class_1799 stack, class_811 renderMode, class_4587 matrices, class_11659 queue, int light, CallbackInfo ci) {
      ViewModel module = ViewModel.getInstance();
      if (module != null && module.isEnabled() && module.getApplyToMode().equals("Item Only") && currentHand.get() == class_1268.field_5808) {
         matrices.method_22903();
         module.applyTransformations(matrices);
      }

   }

   @Inject(
      method = {"method_3233(Lnet/minecraft/class_1309;Lnet/minecraft/class_1799;Lnet/minecraft/class_811;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;I)V"},
      at = {@At("TAIL")}
   )
   private void onRenderItemTail(class_1309 entity, class_1799 stack, class_811 renderMode, class_4587 matrices, class_11659 queue, int light, CallbackInfo ci) {
      ViewModel module = ViewModel.getInstance();
      if (module != null && module.isEnabled() && module.getApplyToMode().equals("Item Only") && currentHand.get() == class_1268.field_5808) {
         matrices.method_22909();
      }

   }
}
