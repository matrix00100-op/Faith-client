package com.slither.cyemer.config.hub;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public class ConfigHubManager {
   private static final String REPO_BASE_URL = "https://raw.githubusercontent.com/Faith/Faith-ConfigHub/main/";
   private static final String MANIFEST_URL = "https://raw.githubusercontent.com/Faith/Faith-ConfigHub/main/manifest.json";
   private static final String CONFIGS_URL = "https://raw.githubusercontent.com/Faith/Faith-ConfigHub/main/configs/";
   private final Gson gson = new Gson();
   private final List<RemoteConfig> cachedConfigs = new ArrayList();

   public void fetchConfigs(Consumer<List<RemoteConfig>> onSuccess, Consumer<String> onError) {
      CompletableFuture.runAsync(() -> {
         try {
            URL url = new URL("https://raw.githubusercontent.com/Faith/Faith-ConfigHub/main/manifest.json");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            if (connection.getResponseCode() == 200) {
               InputStreamReader reader = new InputStreamReader(connection.getInputStream());
               List<RemoteConfig> configs = (List)this.gson.fromJson(reader, (new TypeToken<List<RemoteConfig>>(this) {
               }).getType());
               synchronized(this.cachedConfigs) {
                  this.cachedConfigs.clear();
                  this.cachedConfigs.addAll(configs);
               }

               onSuccess.accept(configs);
            } else {
               onError.accept("HTTP Error: " + connection.getResponseCode());
            }
         } catch (Exception var10) {
            var10.printStackTrace();
            onError.accept("Connection failed: " + var10.getMessage());
         }

      });
   }

   public void downloadConfig(RemoteConfig config, Runnable onSuccess, Consumer<String> onError) {
      CompletableFuture.runAsync(() -> {
         try {
            URL url = new URL("https://raw.githubusercontent.com/Faith/Faith-ConfigHub/main/configs/" + config.fileName);
            File configsDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), "cyemer/configs");
            if (!configsDir.exists()) {
               configsDir.mkdirs();
            }

            File outputFile = new File(configsDir, config.fileName);
            BufferedInputStream in = new BufferedInputStream(url.openStream());

            try {
               FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

               try {
                  byte[] dataBuffer = new byte[1024];

                  int bytesRead;
                  while((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                     fileOutputStream.write(dataBuffer, 0, bytesRead);
                  }
               } catch (Throwable var12) {
                  try {
                     fileOutputStream.close();
                  } catch (Throwable var11) {
                     var12.addSuppressed(var11);
                  }

                  throw var12;
               }

               fileOutputStream.close();
            } catch (Throwable var13) {
               try {
                  in.close();
               } catch (Throwable var10) {
                  var13.addSuppressed(var10);
               }

               throw var13;
            }

            in.close();
            onSuccess.run();
         } catch (Exception var14) {
            var14.printStackTrace();
            onError.accept("Download failed: " + var14.getMessage());
         }

      });
   }

   public List<RemoteConfig> getCachedConfigs() {
      return this.cachedConfigs;
   }
}
