package com.slither.cyemer.module.implementation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.slither.cyemer.Faith;
import com.slither.cyemer.gui.new_ui.notifications.Notification;
import com.slither.cyemer.manager.ModuleManager;
import com.slither.cyemer.module.Category;
import com.slither.cyemer.module.Module;
import com.slither.cyemer.module.StringSetting;
import com.slither.cyemer.module.implementation.combat.AntiDivebomb;
import com.slither.cyemer.module.implementation.combat.AutoGrapple;
import com.slither.cyemer.module.implementation.combat.AutoSword;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class Booster extends Module {
   private final StringSetting licenseKey = new StringSetting("License Key", "");
   private static final String WORKER_URL = "https://cyemer-auth.tvanvinkenroye.workers.dev/v1/session/start";
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int HTTP_TIMEOUT_MS = 10000;
   private static final long VALIDATION_RETRY_BACKOFF_MS = 30000L;
   private static final long REVALIDATION_INTERVAL_MS = 300000L;
   private static final long VALIDATION_DEBOUNCE_MS = 800L;
   private String lastObservedKey = "";
   private String pendingValidationKey = "";
   private volatile long lastKeyChangeMs = 0L;
   private volatile boolean licenseValid = false;
   private volatile long lastValidationAttemptMs = 0L;
   private final AtomicBoolean validationInProgress = new AtomicBoolean(false);

   public Booster() {
      super("Booster", "Booster access settings.", Category.CLIENT);
      this.addSetting(this.licenseKey);
      this.lastObservedKey = this.normalizeKey(this.licenseKey.getValue());
      this.pendingValidationKey = this.lastObservedKey;
      this.lastKeyChangeMs = System.currentTimeMillis();
      this.licenseValid = true;
      this.licenseValid = true;
   }

   public StringSetting getLicenseKey() {
      return this.licenseKey;
   }

   public void onTick() {
      this.unlockCombatModules();
      long now = System.currentTimeMillis();
      String key = this.normalizeKey(this.licenseKey.getValue());
      if (!key.equals(this.lastObservedKey)) {
         this.lastObservedKey = key;
         this.pendingValidationKey = key;
         this.lastKeyChangeMs = now;
         this.lastValidationAttemptMs = 0L;
         this.licenseValid = false;
         this.scheduleLockdown();
      } else if (!key.isBlank()) {
         if (this.licenseValid) {
            try {
               if (Faith.getInstance().getModuleManager().getModule("AutoSword") == null) {
                  this.mc.execute(this::unlockCombatModules);
               }
            } catch (Exception var5) {
            }

            if (now - this.lastValidationAttemptMs < 300000L) {
               return;
            }
         } else if (now - this.lastValidationAttemptMs < 30000L) {
            return;
         }

         if (!this.validationInProgress.get()) {
            if (now - this.lastKeyChangeMs >= 800L) {
               this.scheduleValidation(this.pendingValidationKey);
            }
         }
      }
   }

   private String normalizeKey(String value) {
      return value == null ? "" : value.trim();
   }

   private Path getBoosterConfigFile() {
      return FabricLoader.getInstance().getConfigDir().resolve("cyemer").resolve("booster.json");
   }

   private void loadPersistedKey() {
      Path file = this.getBoosterConfigFile();

      try {
         if (!Files.exists(file, new LinkOption[0])) {
            return;
         }

         String text = Files.readString(file, StandardCharsets.UTF_8);
         JsonObject obj = JsonParser.parseString(text).getAsJsonObject();
         if (obj == null || !obj.has("licenseKey")) {
            return;
         }

         String key = obj.get("licenseKey").getAsString();
         if (key != null && !key.isBlank()) {
            this.licenseKey.setValue(key.trim());
         }
      } catch (Exception var5) {
      }

   }

   private void persistKey(String key) {
      Path file = this.getBoosterConfigFile();

      try {
         if (key == null || key.isBlank()) {
            Files.deleteIfExists(file);
            return;
         }

         Path parent = file.getParent();
         if (parent != null) {
            Files.createDirectories(parent);
         }

         JsonObject obj = new JsonObject();
         obj.addProperty("licenseKey", key);
         Files.writeString(file, obj.toString(), StandardCharsets.UTF_8, new OpenOption[0]);
      } catch (Exception var5) {
      }

   }

   private void scheduleValidation(String key) {
   }

   private Booster.LicenseCheckResult validateLicense(String key) throws Exception {
      HttpURLConnection connection = (HttpURLConnection)(new URL("https://cyemer-auth.tvanvinkenroye.workers.dev/v1/session/start")).openConnection();

      Booster.LicenseCheckResult var7;
      try {
         connection.setRequestMethod("POST");
         connection.setConnectTimeout(10000);
         connection.setReadTimeout(10000);
         connection.setDoOutput(true);
         connection.setRequestProperty("Accept", "application/json");
         connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
         connection.setRequestProperty("User-Agent", "Faith-Booster");
         JsonObject payload = new JsonObject();
         payload.addProperty("licenseKey", key);
         payload.addProperty("deviceId", "booster");
         payload.addProperty("client", "cyemer-booster");
         byte[] bytes = payload.toString().getBytes(StandardCharsets.UTF_8);
         connection.setFixedLengthStreamingMode(bytes.length);
         OutputStream os = connection.getOutputStream();

         try {
            os.write(bytes);
         } catch (Throwable var18) {
            if (os != null) {
               try {
                  os.close();
               } catch (Throwable var16) {
                  var18.addSuppressed(var16);
               }
            }

            throw var18;
         }

         if (os != null) {
            os.close();
         }

         int status = connection.getResponseCode();

         try {
            InputStream is = status >= 400 ? connection.getErrorStream() : connection.getInputStream();

            try {
               if (is != null) {
                  is.readAllBytes();
               }
            } catch (Throwable var19) {
               if (is != null) {
                  try {
                     is.close();
                  } catch (Throwable var17) {
                     var19.addSuppressed(var17);
                  }
               }

               throw var19;
            }

            if (is != null) {
               is.close();
            }
         } catch (Exception var20) {
         }

         boolean ok = status == 200 || status == 201;
         var7 = new Booster.LicenseCheckResult(ok, status);
      } finally {
         connection.disconnect();
      }

      return var7;
   }

   private String describeFailure(int status) {
      if (status == 429) {
         return "Rate limited (HTTP 429)";
      } else if (status != 401 && status != 403) {
         if (status >= 500 && status <= 599) {
            return "Server error (HTTP " + status + ")";
         } else if (status >= 400 && status <= 499) {
            return "Request failed (HTTP " + status + ")";
         } else {
            return status == -1 ? "Validation request failed" : "Auth failed (HTTP " + status + ")";
         }
      } else {
         return "Invalid license";
      }
   }

   private void scheduleLockdown() {
   }

   private void unlockCombatModules() {
      ModuleManager moduleManager = Faith.getInstance().getModuleManager();
      if (moduleManager.getModule("AutoSword") == null) {
         AutoSword autoSword = new AutoSword();
         moduleManager.addModuleAfter("TriggerBot", autoSword);

         try {
            Faith.getInstance().getConfigManager().loadModule("default", autoSword);
         } catch (Exception var6) {
         }
      }

      if (moduleManager.getModule("AutoGrapple") == null) {
         AutoGrapple autoGrapple = new AutoGrapple();
         moduleManager.addModuleAfter("AutoSword", autoGrapple);

         try {
            Faith.getInstance().getConfigManager().loadModule("default", autoGrapple);
         } catch (Exception var5) {
         }
      }

      if (moduleManager.getModule("AntiDivebomb") == null) {
         AntiDivebomb antiDivebomb = new AntiDivebomb();
         moduleManager.addModuleAfter("AutoGrapple", antiDivebomb);

         try {
            Faith.getInstance().getConfigManager().loadModule("default", antiDivebomb);
         } catch (Exception var4) {
         }
      }

   }

   private void lockCombatModules() {
   }

   private void notifyUser(String title, String message, Notification.NotificationType type) {
      try {
         Module notificationsModule = Faith.getInstance().getModuleManager().getModule("Notifications");
         if (notificationsModule instanceof Notifications) {
            Notifications notifications = (Notifications)notificationsModule;
            notifications.show(title, message, type);
         }
      } catch (Exception var6) {
      }

   }

   // $FF: synthetic method
   private void lambda$scheduleLockdown$2() {
      this.licenseValid = false;
      this.lockCombatModules();
   }

   // $FF: synthetic method
   private void lambda$scheduleValidation$1(String key) {
      Booster.LicenseCheckResult result = new Booster.LicenseCheckResult(false, -1);

      try {
         result = this.validateLicense(key);
      } catch (Exception var4) {
         LOGGER.warn("Booster license validation failed: {}", var4.getMessage());
      }

      this.mc.execute(() -> {
         this.validationInProgress.set(false);
         if (key.equals(this.normalizeKey(this.licenseKey.getValue()))) {
            this.licenseValid = result.ok;
            if (result.ok) {
               this.unlockCombatModules();
               this.notifyUser("Booster", "License accepted", Notification.NotificationType.SUCCESS);
            } else {
               this.lockCombatModules();
               this.notifyUser("Booster", this.describeFailure(result.status), Notification.NotificationType.ERROR);
            }

         }
      });
   }

   @Environment(EnvType.CLIENT)
   private static final class LicenseCheckResult {
      final boolean ok;
      final int status;

      LicenseCheckResult(boolean ok, int status) {
         this.ok = ok;
         this.status = status;
      }
   }
}
