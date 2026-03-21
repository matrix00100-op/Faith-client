package com.slither.cyemer.mixin;

import com.slither.cyemer.Faith;
import com.slither.cyemer.module.implementation.ESP;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10034;
import net.minecraft.class_10055;
import net.minecraft.class_11659;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_970;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({class_970.class})
public class ArmorFeatureRendererMixin {
   @Inject(
      method = {"method_17157(Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;ILnet/minecraft/class_10034;FF)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderArmor(class_4587 matrices, class_11659 queue, int light, class_10034 state, float limbAngle, float limbDistance, CallbackInfo ci) {
      if (state instanceof class_10055) {
         class_10055 playerState = (class_10055)state;
         ESP espModule = (ESP)Faith.getInstance().getModuleManager().getModule("ESP");
         if (espModule != null && espModule.isEnabled() && espModule.hideArmor.isEnabled()) {
            boolean isSelf = class_310.method_1551().field_1724 != null && playerState.field_53520.equals(class_310.method_1551().field_1724.method_52814());
            if (!isSelf) {
               ci.cancel();
            }
         }
      }

   }
}
