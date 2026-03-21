package com.slither.cyemer.util;

import com.slither.cyemer.module.implementation.SelfDestruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class JarUpdater {
   public static File getCurrentJarFile() {
      try {
         return new File(SelfDestruct.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      } catch (URISyntaxException var1) {
         var1.printStackTrace();
         return null;
      }
   }

   public static String downloadFile(String downloadUrl, File targetFile) {
      HttpURLConnection connection = null;

      String var3;
      try {
         if (!targetFile.exists()) {
            URL url = new URL(downloadUrl);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("User-Agent", "Faith-Client-Updater/1.0");
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
               InputStream inputStream = connection.getInputStream();

               Object var6;
               try {
                  Files.copy(inputStream, targetFile.toPath(), new CopyOption[0]);
                  var6 = null;
               } catch (Throwable var14) {
                  if (inputStream != null) {
                     try {
                        inputStream.close();
                     } catch (Throwable var13) {
                        var14.addSuppressed(var13);
                     }
                  }

                  throw var14;
               }

               if (inputStream != null) {
                  inputStream.close();
               }

               return (String)var6;
            }

            String var5 = "Download failed. Server responded with code: " + responseCode;
            return var5;
         }

         var3 = "The replacement file '" + targetFile.getName() + "' already exists in the mods folder.";
      } catch (IOException var15) {
         var15.printStackTrace();
         String var4 = "An I/O error occurred during download. Check console.";
         return var4;
      } finally {
         if (connection != null) {
            connection.disconnect();
         }

      }

      return var3;
   }
}
