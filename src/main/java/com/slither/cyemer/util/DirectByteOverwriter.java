package com.slither.cyemer.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class DirectByteOverwriter {
   public static boolean overwriteWithBytes(String downloadUrl, File targetFile) {
      HttpURLConnection connection = null;

      boolean var5;
      try {
         URL url = new URL(downloadUrl);
         connection = (HttpURLConnection)url.openConnection();
         connection.setRequestProperty("User-Agent", "Faith-Client-Byte-Overwriter/1.0");
         connection.setConnectTimeout(8000);
         connection.setReadTimeout(15000);
         int responseCode = connection.getResponseCode();
         if (responseCode == 200) {
            InputStream inputStream = connection.getInputStream();

            boolean var9;
            try {
               FileOutputStream outputStream = new FileOutputStream(targetFile);

               try {
                  byte[] buffer = new byte[8192];

                  while(true) {
                     int bytesRead;
                     if ((bytesRead = inputStream.read(buffer)) == -1) {
                        var9 = true;
                        break;
                     }

                     outputStream.write(buffer, 0, bytesRead);
                  }
               } catch (Throwable var19) {
                  try {
                     outputStream.close();
                  } catch (Throwable var18) {
                     var19.addSuppressed(var18);
                  }

                  throw var19;
               }

               outputStream.close();
            } catch (Throwable var20) {
               if (inputStream != null) {
                  try {
                     inputStream.close();
                  } catch (Throwable var17) {
                     var20.addSuppressed(var17);
                  }
               }

               throw var20;
            }

            if (inputStream != null) {
               inputStream.close();
            }

            return var9;
         }

         var5 = false;
      } catch (Exception var21) {
         boolean var4 = false;
         return var4;
      } finally {
         if (connection != null) {
            connection.disconnect();
         }

      }

      return var5;
   }
}
