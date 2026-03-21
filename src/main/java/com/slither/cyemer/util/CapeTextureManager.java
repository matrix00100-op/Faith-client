package com.slither.cyemer.util;

import java.io.InputStream;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;

@Environment(EnvType.CLIENT)
public class CapeTextureManager {
   public static final class_2960 CUSTOM_CAPE_IDENTIFIER = class_2960.method_60655("dynamic_fps", "loaded/custom_cape");

   public static void loadCapeTexture(String capeName) {
      class_2960 sourceCapePath = class_2960.method_60655("dynamic_fps", "textures/" + capeName + ".png");
      class_310.method_1551().execute(() -> {
         try {
            class_310 client = class_310.method_1551();
            Optional<class_3298> resourceOptional = client.method_1478().method_14486(sourceCapePath);
            if (resourceOptional.isPresent()) {
               InputStream stream = ((class_3298)resourceOptional.get()).method_14482();

               try {
                  class_1011 image = class_1011.method_4309(stream);
                  class_1043 texture = new class_1043(() -> {
                     return "cyemer_custom_cape";
                  }, image);
                  texture.method_4524();
                  client.method_1531().method_4616(CUSTOM_CAPE_IDENTIFIER, texture);
               } catch (Throwable var7) {
                  if (stream != null) {
                     try {
                        stream.close();
                     } catch (Throwable var6) {
                        var7.addSuppressed(var6);
                     }
                  }

                  throw var7;
               }

               if (stream != null) {
                  stream.close();
               }
            }
         } catch (Exception var8) {
         }

      });
   }
}
