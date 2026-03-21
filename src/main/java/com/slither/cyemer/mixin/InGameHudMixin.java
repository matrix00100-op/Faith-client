package com.slither.cyemer.mixin;

import com.slither.cyemer.Faith;
import com.slither.cyemer.module.implementation.Interface;
import com.slither.cyemer.module.implementation.StreamerModeModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_266;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({class_329.class})
public class InGameHudMixin {
   @Inject(
      method = {"method_1753(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V"},
      at = {@At("TAIL")}
   )
   private void onRender(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
      if (!Faith.selfDestructed) {
         float tickDelta = tickCounter.method_60637(true);
         if (Faith.INSTANCE != null && Faith.INSTANCE.getModuleManager() != null) {
            Faith.INSTANCE.getModuleManager().onRender(context, tickDelta);
         }

      }
   }

   @Inject(
      method = {"method_1757(Lnet/minecraft/class_332;Lnet/minecraft/class_266;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderScoreboardSidebar(class_332 context, class_266 objective, CallbackInfo ci) {
      if (StreamerModeModule.isStreamerModeActive()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"method_1759(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderHotbar(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
      if (Interface.INSTANCE != null && Interface.INSTANCE.shouldCancelVanillaHotbar) {
         ci.cancel();
      }

   }
}
