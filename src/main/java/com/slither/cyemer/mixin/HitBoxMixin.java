package com.slither.cyemer.mixin;

import com.slither.cyemer.module.implementation.combat.HitBox;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public class HitBoxMixin {

    @Inject(
        method = {"method_5661()Lnet/minecraft/class_238;"},
        at = {@At("RETURN")},
        cancellable = true
    )
    private void expandHitBox(CallbackInfoReturnable<Box> cir) {
        HitBox module = HitBox.getInstance();
        if (module != null && module.isEnabled()) {
            float expand = module.getHitboxExpansion();
            Box original = cir.getReturnValue();
            cir.setReturnValue(original.expand(expand));
        }
    }
}
