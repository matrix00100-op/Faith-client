package com.slither.cyemer.util;

import com.mojang.authlib.GameProfile;
import java.lang.reflect.Method;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class GameProfileCompat {
   private static final Method ID_METHOD = findAccessor(GameProfile.class, "getId", "id");
   private static final Method NAME_METHOD = findAccessor(GameProfile.class, "getName", "name");

   private GameProfileCompat() {
   }

   private static Method findAccessor(Class<?> clazz, String legacyName, String modernName) {
      Method m;
      try {
         m = clazz.getMethod(legacyName);
         m.setAccessible(true);
         return m;
      } catch (NoSuchMethodException var5) {
         try {
            m = clazz.getMethod(modernName);
            m.setAccessible(true);
            return m;
         } catch (NoSuchMethodException var4) {
            return null;
         }
      }
   }

   public static UUID getId(GameProfile profile) {
      if (profile != null && ID_METHOD != null) {
         try {
            Object value = ID_METHOD.invoke(profile);
            UUID var10000;
            if (value instanceof UUID) {
               UUID uuid = (UUID)value;
               var10000 = uuid;
            } else {
               var10000 = null;
            }

            return var10000;
         } catch (ReflectiveOperationException var3) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static String getName(GameProfile profile) {
      if (profile != null && NAME_METHOD != null) {
         try {
            Object value = NAME_METHOD.invoke(profile);
            String var10000;
            if (value instanceof String) {
               String s = (String)value;
               var10000 = s;
            } else {
               var10000 = null;
            }

            return var10000;
         } catch (ReflectiveOperationException var3) {
            return null;
         }
      } else {
         return null;
      }
   }
}
