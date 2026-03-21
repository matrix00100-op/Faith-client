package com.slither.cyemer.mixin;

import com.slither.cyemer.module.implementation.Blink;
import com.slither.cyemer.module.implementation.Fakelag;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2535;
import net.minecraft.class_2596;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({class_2535.class})
public class ClientConnectionMixin {
   @Inject(
      method = {"method_10743(Lnet/minecraft/class_2596;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSendPacket(class_2596<?> packet, CallbackInfo ci) {
      Blink blink = Blink.getInstance();
      if (blink != null && blink.isEnabled() && blink.handleOutgoingPacket(packet)) {
         ci.cancel();
      } else {
         Fakelag fakelag = Fakelag.getInstance();
         if (fakelag != null && fakelag.isEnabled() && fakelag.handleOutgoingPacket(packet)) {
            ci.cancel();
         }

      }
   }
}
