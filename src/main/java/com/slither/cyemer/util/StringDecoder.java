package com.slither.cyemer.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class StringDecoder {
   public static String decode(String obfuscated) {
      try {
         int separatorIndex = obfuscated.lastIndexOf(58);
         if (separatorIndex == -1) {
            return obfuscated;
         } else {
            String encoded = obfuscated.substring(0, separatorIndex);
            byte key = Byte.parseByte(obfuscated.substring(separatorIndex + 1));
            char[] chars = encoded.toCharArray();
            byte[] decoded = new byte[chars.length];

            for(int i = 0; i < chars.length; ++i) {
               decoded[i] = (byte)(chars[i] ^ key);
            }

            return new String(decoded);
         }
      } catch (Exception var7) {
         return obfuscated;
      }
   }

   public static String d(String obfuscated) {
      return decode(obfuscated);
   }
}
