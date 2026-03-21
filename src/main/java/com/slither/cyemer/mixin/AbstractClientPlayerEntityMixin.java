package com.slither.cyemer.mixin;

import com.slither.cyemer.module.implementation.CustomCapeModule;
import com.slither.cyemer.util.CapeTextureManager;
import com.slither.cyemer.util.GameProfileCompat;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_640;
import net.minecraft.class_742;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({class_742.class})
public abstract class AbstractClientPlayerEntityMixin {
   @Shadow
   protected abstract class_640 method_3123();

   @Inject(
      method = {"method_52814()Lnet/minecraft/class_8685;"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void onGetSkin(CallbackInfoReturnable<Object> cir) {
      class_310 client = class_310.method_1551();
      if (client.field_1724 != null) {
         if (this.method_3123() != null && client.field_1724.method_5667().equals(GameProfileCompat.getId(this.method_3123().method_2966())) && CustomCapeModule.INSTANCE != null && CustomCapeModule.INSTANCE.isEnabled()) {
            Object currentTextures = this.method_3123().method_52810();
            if (currentTextures == null) {
               return;
            }

            Object newTextures = withCapeTexture(currentTextures, CapeTextureManager.CUSTOM_CAPE_IDENTIFIER);
            if (newTextures != null) {
               cir.setReturnValue(newTextures);
            }
         }

      }
   }

   private static Object withCapeTexture(Object currentTextures, class_2960 capeTexture) {
      try {
         Class clazz = currentTextures.getClass();

         try {
            Class<?> skinOverrideClass = Class.forName(clazz.getName() + "$SkinOverride", false, clazz.getClassLoader());
            Class textureAssetInfoClass = Class.forName("net.minecraft.util.AssetInfo$TextureAssetInfo", false, clazz.getClassLoader());

            Object capeInfo;
            try {
               capeInfo = textureAssetInfoClass.getConstructor(class_2960.class).newInstance(capeTexture);
            } catch (NoSuchMethodException var10) {
               capeInfo = textureAssetInfoClass.getConstructor(class_2960.class, class_2960.class).newInstance(capeTexture, capeTexture);
            }

            Method createOverride = skinOverrideClass.getMethod("create", Optional.class, Optional.class, Optional.class, Optional.class);
            Object skinOverride = createOverride.invoke((Object)null, Optional.empty(), Optional.of(capeInfo), Optional.empty(), Optional.empty());
            Method withOverride = clazz.getMethod("withOverride", skinOverrideClass);
            Object overridden = withOverride.invoke(currentTextures, skinOverride);
            if (overridden != null) {
               return overridden;
            }
         } catch (Throwable var11) {
         }

         if (clazz.isRecord()) {
            RecordComponent[] components = clazz.getRecordComponents();
            Class<?>[] types = new Class[components.length];
            Object[] args = new Object[components.length];

            for(int i = 0; i < components.length; ++i) {
               RecordComponent c = components[i];
               types[i] = c.getType();
               Object value = c.getAccessor().invoke(currentTextures);
               String name = c.getName().toLowerCase();
               if (name.contains("cape") && class_2960.class.isAssignableFrom(c.getType())) {
                  value = capeTexture;
               }

               args[i] = value;
            }

            Constructor<?> ctor = clazz.getDeclaredConstructor(types);
            ctor.setAccessible(true);
            return ctor.newInstance(args);
         }
      } catch (Throwable var12) {
      }

      return null;
   }
}
