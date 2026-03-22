package com.slither.cyemer.module.implementation.combat;

import com.slither.cyemer.module.Category;
import com.slither.cyemer.module.Module;
import com.slither.cyemer.module.SliderSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class HitBox extends Module {
    private static HitBox instance;

    private final SliderSetting hitboxSize = new SliderSetting(
        "HitBox Size", 0.1D, 0.0D, 4.0D, 1
    );

    public HitBox() {
        instance = this;
        super("HitBox", "Increases player hitbox size for easier hits", Category.COMBAT);
        this.addSetting(this.hitboxSize);
    }

    public static HitBox getInstance() {
        return instance;
    }

    public float getHitboxExpansion() {
        if (!this.isEnabled()) return 0.0f;
        return (float) this.hitboxSize.getValue();
    }
}
