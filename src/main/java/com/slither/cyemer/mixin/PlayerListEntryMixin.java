package com.slither.cyemer.mixin;

import com.mojang.authlib.GameProfile;
import com.slither.cyemer.Faith;
import com.slither.cyemer.module.implementation.combat.Nick;
import com.slither.cyemer.util.GameProfileCompat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_640;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({class_640.class})
public class PlayerListEntryMixin {
   @Shadow
   @Final
   private GameProfile field_3741;

   @Inject(
      method = {"method_2971()Lnet/minecraft/class_2561;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetDisplayName(CallbackInfoReturnable<class_2561> cir) {
      Nick nickModule = (Nick)Faith.getInstance().getModuleManager().getModule("Nick");
      if (nickModule != null && nickModule.isEnabled() && class_310.method_1551().field_1724 != null) {
         if (class_310.method_1551().field_1724 != null && class_310.method_1551().field_1724.method_5667().equals(GameProfileCompat.getId(this.field_3741))) {
            String nickname = nickModule.nickname.getValue();
            cir.setReturnValue(class_2561.method_30163(nickname));
         }

      }
   }
}
