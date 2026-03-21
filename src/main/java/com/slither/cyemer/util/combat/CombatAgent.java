package com.slither.cyemer.util.combat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.slither.cyemer.Faith;
import com.slither.cyemer.gui.new_ui.notifications.Notification;
import com.slither.cyemer.module.Module;
import com.slither.cyemer.module.implementation.Booster;
import com.slither.cyemer.module.implementation.Notifications;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class CombatAgent {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private static final boolean READ_ONLY_MODEL = Boolean.parseBoolean(System.getProperty("cyemer.autosword.readOnlyModel", "true"));
   private static final boolean CLOUDFLARE_DB_ONLY = true;
   private static final int DATABASE_SCHEMA_VERSION = 2;
   private static final String DATABASE_FILE_NAME = "Faith_Database.db";
   private static final String ENCRYPTED_DATABASE_FILE_NAME = "Faith_Database.db.enc";
   private static final String DATABASE_DIR_NAME = "database";
   private static final String LEGACY_TYPO_DATABASE_DIR_NAME = "databse";
   private static final String LEGACY_RELATIVE_FILE_PATH = "cyemer/autosword/database/Faith_Database.db";
   private static final String LEGACY_TYPO_RELATIVE_FILE_PATH = "cyemer/autosword/databse/Faith_Database.db";
   private static final int MIN_MEMORY_FOR_TRAINING = 128;
   private static final int TRAINING_BATCH_SIZE = 32;
   private static final long NETWORK_SEED = 1337L;
   private static final int AUTO_SAVE_BATCH_INTERVAL = 64;
   private static final long AUTO_SAVE_TIME_INTERVAL_MS = 15000L;
   private static final String SQLITE_URL_PREFIX = "jdbc:sqlite:";
   private static final String SQLITE_HEADER = "SQLite format 3";
   private static final byte[] ENCRYPTED_DB_MAGIC;
   private static final String ENCRYPTION_SEED = "cyemer.autosword.pretrained.database.v1";
   private static final String WORKER_BASE_URL_DEFAULT = "https://cyemer-auth.tvanvinkenroye.workers.dev";
   private static final String WORKER_SESSION_PATH = "/v1/session/start";
   private static final String WORKER_MODEL_PATH = "/v1/model/current";
   private static final String MODEL_CACHE_DIR_NAME = "cache";
   private static final String MODEL_CACHE_FILE_NAME = "model-cache.enc";
   private static final long DEFAULT_MODEL_CACHE_MAX_AGE_MS = 86400000L;
   private static final int HTTP_TIMEOUT_MS = 10000;
   private static final int GCM_IV_LENGTH = 12;
   private static final int GCM_TAG_BITS = 128;
   private static final String TABLE_METADATA = "model_metadata";
   private static final String TABLE_PARAMS = "model_network_params";
   private static final String TABLE_HISTORY = "model_training_history";
   private static final String LEGACY_TABLE_METADATA;
   private static final String LEGACY_TABLE_PARAMS;
   private static final String LEGACY_TABLE_HISTORY;
   private static final String META_SCHEMA_VERSION = "schema_version";
   private static final String META_FRAMEWORK = "framework";
   private static final String META_EPSILON = "epsilon";
   private static final String META_MEMORY_SIZE = "memory_size";
   private static final String META_TRAINING_STEPS = "training_step_count";
   private static final String META_TRAINED_BATCHES = "trained_batch_count";
   private static final String META_LAST_UPDATED = "last_updated_epoch_ms";
   private final String modelName;
   private final int inputSize;
   private final int h1Size;
   private final int h2Size;
   private final int outputSize;
   private final double learningRate = 0.002D;
   private final double discountFactor = 0.99D;
   private double epsilon = 0.15D;
   private final List<CombatAgent.Experience> memory = new ArrayList();
   private final int maxMemory = 15000;
   private final Random random = new Random();
   private final CombatAgent.Network network;
   private final Object modelLock = new Object();
   private final AtomicBoolean saveInProgress = new AtomicBoolean(false);
   private final AtomicBoolean loadInProgress = new AtomicBoolean(false);
   private static final AtomicBoolean MISSING_LICENSE_NOTICE_SHOWN;
   private static final long LOAD_RETRY_BACKOFF_MS = 30000L;
   private volatile long lastLoadAttemptMs = 0L;
   private volatile boolean modelLoaded = false;
   private volatile boolean dirty = false;
   private volatile long trainedBatchCount = 0L;
   private volatile long trainingStepCount = 0L;
   private volatile long lastSavedBatchCount = 0L;
   private volatile long lastSaveRequestTimeMs = 0L;
   private volatile long lastSuccessfulSaveTimeMs = 0L;

   private static String legacyTableName(String suffix) {
      String var10000 = new String(new char[]{'a', 'i'});
      return var10000 + suffix;
   }

   public CombatAgent(String modelName, int inputSize, int h1Size, int h2Size, int outputSize) {
      this.modelName = modelName;
      this.inputSize = inputSize;
      this.h1Size = h1Size;
      this.h2Size = h2Size;
      this.outputSize = outputSize;
      this.network = new CombatAgent.Network(inputSize, h1Size, h2Size, outputSize, 1337L);
   }

   public int predict(double[] state) {
      this.ensureLoaded();
      if (!this.modelLoaded) {
         return 0;
      } else {
         double[] output;
         synchronized(this.modelLock) {
            output = this.network.predict(this.normalizeState(state));
         }

         int bestAction = 0;
         double maxQ = -1.7976931348623157E308D;

         for(int i = 0; i < output.length; ++i) {
            double q = output[i];
            if (q > maxQ) {
               maxQ = q;
               bestAction = i;
            }
         }

         return bestAction;
      }
   }

   private void ensureLoaded() {
      if (!this.modelLoaded) {
         long now = System.currentTimeMillis();
         long lastAttempt = this.lastLoadAttemptMs;
         if (lastAttempt <= 0L || now - lastAttempt >= 30000L) {
            if (this.loadInProgress.compareAndSet(false, true)) {
               this.lastLoadAttemptMs = now;
               Thread loadThread = new Thread(() -> {
                  try {
                     if (this.licenseKeyMissing()) {
                        this.notifyMissingLicenseOnce();
                        return;
                     }

                     this.loadData();
                     this.modelLoaded = true;
                  } catch (Exception var5) {
                     LOGGER.warn("CombatAgent failed to load pretrained database: {}", var5.getMessage());
                  } finally {
                     this.loadInProgress.set(false);
                  }

               }, "cyemer-autosword-load");
               loadThread.setDaemon(true);
               loadThread.start();
            }
         }
      }
   }

   private boolean licenseKeyMissing() {
      String key = this.resolveWorkerLicenseKey();
      if (key != null && !key.isBlank()) {
         return false;
      } else {
         Path cacheFile = this.getModelCacheFilePath();
         if (Files.exists(cacheFile, new LinkOption[0])) {
            return false;
         } else {
            return !this.allowLocalFallback();
         }
      }
   }

   private void notifyMissingLicenseOnce() {
      if (MISSING_LICENSE_NOTICE_SHOWN.compareAndSet(false, true)) {
         try {
            Module notificationsModule = Faith.getInstance().getModuleManager().getModule("Notifications");
            if (notificationsModule instanceof Notifications) {
               Notifications notifications = (Notifications)notificationsModule;
               notifications.show("AutoSword", "Set license in Booster (Client) to download the model", Notification.NotificationType.ERROR);
            }
         } catch (Exception var3) {
         }

      }
   }

   public void train(double[] state, int action, double reward, double[] nextState) {
      if (!READ_ONLY_MODEL) {
         double[] safeState = this.normalizeState(state);
         double[] safeNextState = this.normalizeState(nextState);
         int safeAction = this.clampAction(action);
         synchronized(this.modelLock) {
            this.memory.add(new CombatAgent.Experience(safeState, safeAction, reward, safeNextState));
            if (this.memory.size() > 15000) {
               this.memory.remove(0);
            }

            if (this.memory.size() < 128) {
               return;
            }

            CombatAgent.Experience[] batch = new CombatAgent.Experience[32];
            int i = 0;

            while(true) {
               if (i >= 32) {
                  this.network.trainBatch(batch, 0.99D, 0.002D);
                  if (this.epsilon > 0.01D) {
                     this.epsilon *= 0.99995D;
                  }

                  ++this.trainedBatchCount;
                  this.trainingStepCount += 32L;
                  this.dirty = true;
                  break;
               }

               batch[i] = (CombatAgent.Experience)this.memory.get(this.random.nextInt(this.memory.size()));
               ++i;
            }
         }

         this.maybeAutoSave();
      }
   }

   public void saveData() {
   }

   private void requestSave(boolean force) {
      if (force || this.dirty) {
         if (this.saveInProgress.compareAndSet(false, true)) {
            this.lastSaveRequestTimeMs = System.currentTimeMillis();
            Thread saveThread = new Thread(() -> {
               try {
                  this.saveToDatabase();
               } catch (Exception var5) {
                  LOGGER.warn("Failed to persist CombatAgent database: {}", var5.getMessage());
               } finally {
                  this.saveInProgress.set(false);
               }

            }, "cyemer-autosword-save");
            saveThread.setDaemon(true);
            saveThread.start();
         }
      }
   }

   private void maybeAutoSave() {
   }

   private double[] normalizeState(double[] state) {
      double[] normalized = new double[this.inputSize];
      if (state == null) {
         return normalized;
      } else {
         int length = Math.min(this.inputSize, state.length);
         System.arraycopy(state, 0, normalized, 0, length);
         return normalized;
      }
   }

   private int clampAction(int action) {
      if (action < 0) {
         return 0;
      } else {
         return action >= this.outputSize ? this.outputSize - 1 : action;
      }
   }

   private void saveToDatabase() throws Exception {
      CombatAgent.Snapshot snapshot = this.buildSnapshot();
      this.writeSqliteDatabase(this.getDatabaseFilePath(), snapshot);
      this.cleanupLegacyTypoDatabase();
      synchronized(this.modelLock) {
         this.lastSuccessfulSaveTimeMs = System.currentTimeMillis();
         this.lastSavedBatchCount = Math.max(this.lastSavedBatchCount, snapshot.trainedBatchCount);
         if (this.trainedBatchCount == snapshot.trainedBatchCount) {
            this.dirty = false;
         }

      }
   }

   private void loadData() throws Exception {
      if (!this.tryLoadRemoteEncryptedDatabase()) {
         throw new FileNotFoundException("Cloudflare AutoSword database unavailable");
      }
   }

   private boolean tryLoadRemoteEncryptedDatabase() {
      Path cacheFile = this.getModelCacheFilePath();
      long maxAgeMs = this.resolveModelCacheMaxAgeMs();
      if (this.tryLoadModelCache(cacheFile, maxAgeMs, true)) {
         return true;
      } else {
         String baseUrl = this.resolveWorkerBaseUrl();
         String licenseKey = this.resolveWorkerLicenseKey();
         if (licenseKey != null && !licenseKey.isBlank()) {
            String deviceId = this.resolveWorkerDeviceId();
            String token = this.requestWorkerSessionToken(baseUrl, licenseKey, deviceId);
            if (token != null && !token.isBlank()) {
               byte[] payload = this.requestWorkerModel(baseUrl, token, deviceId);
               if (payload != null && payload.length != 0) {
                  String source = this.joinBaseUrl(baseUrl, "/v1/model/current");
                  if (!this.tryLoadEncryptedBytes(payload, source)) {
                     LOGGER.warn("CombatAgent worker model payload could not be loaded; trying stale cache");
                     return this.tryLoadModelCache(cacheFile, Long.MAX_VALUE, false);
                  } else {
                     this.writeModelCache(cacheFile, payload);
                     return true;
                  }
               } else {
                  LOGGER.warn("CombatAgent worker returned empty model payload; trying stale cache");
                  return this.tryLoadModelCache(cacheFile, Long.MAX_VALUE, false);
               }
            } else {
               LOGGER.warn("CombatAgent could not start worker session; trying stale cache");
               return this.tryLoadModelCache(cacheFile, Long.MAX_VALUE, false);
            }
         } else {
            LOGGER.warn("CombatAgent worker license key is missing (set CYEMER_DB_LICENSE_KEY or cyemer.autosword.licenseKey)");
            return this.tryLoadModelCache(cacheFile, Long.MAX_VALUE, false);
         }
      }
   }

   private boolean tryLoadModelCache(Path cacheFile, long maxAgeMs, boolean requireFresh) {
      try {
         if (!Files.exists(cacheFile, new LinkOption[0])) {
            return false;
         } else {
            long ageMs = Math.max(0L, System.currentTimeMillis() - Files.getLastModifiedTime(cacheFile).toMillis());
            if (requireFresh && ageMs > maxAgeMs) {
               return false;
            } else {
               byte[] bytes = Files.readAllBytes(cacheFile);
               return this.tryLoadEncryptedBytes(bytes, cacheFile.toString());
            }
         }
      } catch (Exception var8) {
         LOGGER.warn("Failed loading CombatAgent model cache {}: {}", cacheFile, var8.getMessage());
         return false;
      }
   }

   private void writeModelCache(Path cacheFile, byte[] bytes) {
      try {
         Path parent = cacheFile.getParent();
         if (parent != null) {
            Files.createDirectories(parent);
         }

         Files.write(cacheFile, bytes, new OpenOption[0]);
      } catch (Exception var4) {
         LOGGER.warn("Failed writing CombatAgent model cache {}: {}", cacheFile, var4.getMessage());
      }

   }

   private String requestWorkerSessionToken(String baseUrl, String licenseKey, String deviceId) {
      HttpURLConnection connection = null;

      String token;
      try {
         JsonObject payload;
         try {
            String urlStr = this.joinBaseUrl(baseUrl, "/v1/session/start");
            connection = (HttpURLConnection)(new URL(urlStr)).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("User-Agent", "Faith-CombatAgent");
            payload = new JsonObject();
            payload.addProperty("licenseKey", licenseKey);
            payload.addProperty("deviceId", deviceId);
            payload.addProperty("client", "cyemer-combat-agent");
            byte[] body = payload.toString().getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(body.length);
            OutputStream os = connection.getOutputStream();

            try {
               os.write(body);
            } catch (Throwable var19) {
               if (os != null) {
                  try {
                     os.close();
                  } catch (Throwable var18) {
                     var19.addSuppressed(var18);
                  }
               }

               throw var19;
            }

            if (os != null) {
               os.close();
            }

            int status = connection.getResponseCode();
            byte[] response = this.readResponseBytes(connection, status);
            JsonObject json;
            if (status != 200 && status != 201) {
               LOGGER.warn("CombatAgent session request failed with status {} ({})", status, this.previewResponse(response));
               json = null;
               return json;
            }

            json = this.parseJson(response);
            if (json != null) {
               token = this.firstNonBlank(this.getJsonString(json, "sessionToken"), this.getJsonString(json, "token"), this.getJsonString(json, "accessToken"));
               String var12;
               if (token != null && !token.isBlank()) {
                  var12 = token;
                  return var12;
               }

               LOGGER.warn("CombatAgent session response did not include a token");
               var12 = null;
               return var12;
            }

            LOGGER.warn("CombatAgent session response was not JSON");
            token = null;
         } catch (Exception var20) {
            LOGGER.warn("CombatAgent session request failed: {}", var20.getMessage());
            payload = null;
            return payload;
         }
      } finally {
         if (connection != null) {
            connection.disconnect();
         }

      }

      return token;
   }

   private byte[] requestWorkerModel(String baseUrl, String token, String deviceId) {
      HttpURLConnection connection = null;

      byte[] var10;
      try {
         String urlStr = this.joinBaseUrl(baseUrl, "/v1/model/current");
         connection = (HttpURLConnection)(new URL(urlStr)).openConnection();
         connection.setRequestMethod("GET");
         connection.setConnectTimeout(10000);
         connection.setReadTimeout(10000);
         connection.setRequestProperty("Accept", "application/octet-stream, application/json");
         connection.setRequestProperty("Authorization", "Bearer " + token);
         connection.setRequestProperty("X-Faith-Device-Id", deviceId);
         connection.setRequestProperty("User-Agent", "Faith-CombatAgent");
         int status = connection.getResponseCode();
         byte[] response = this.readResponseBytes(connection, status);
         String contentType;
         if (status != 200) {
            LOGGER.warn("CombatAgent model request failed with status {} ({})", status, this.previewResponse(response));
            contentType = null;
            return (byte[])contentType;
         }

         contentType = connection.getContentType();
         byte[] decoded;
         if (!this.isJsonContentType(contentType) && !this.looksLikeJson(response)) {
            decoded = response;
            return decoded;
         }

         decoded = this.decodeModelPayloadFromJson(response);
         if (decoded == null || decoded.length == 0) {
            LOGGER.warn("CombatAgent model response JSON did not contain base64 payload");
            Object var16 = null;
            return (byte[])var16;
         }

         var10 = decoded;
      } catch (Exception var14) {
         LOGGER.warn("CombatAgent model request failed: {}", var14.getMessage());
         Object var6 = null;
         return (byte[])var6;
      } finally {
         if (connection != null) {
            connection.disconnect();
         }

      }

      return var10;
   }

   private byte[] readResponseBytes(HttpURLConnection connection, int status) throws Exception {
      InputStream is = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
      if (is == null) {
         return new byte[0];
      } else {
         InputStream in = is;

         byte[] var5;
         try {
            var5 = in.readAllBytes();
         } catch (Throwable var8) {
            if (is != null) {
               try {
                  in.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (is != null) {
            is.close();
         }

         return var5;
      }
   }

   private JsonObject parseJson(byte[] bytes) {
      if (bytes != null && bytes.length != 0) {
         try {
            String text = new String(bytes, StandardCharsets.UTF_8);
            return JsonParser.parseString(text).getAsJsonObject();
         } catch (Exception var3) {
            return null;
         }
      } else {
         return null;
      }
   }

   private byte[] decodeModelPayloadFromJson(byte[] bytes) {
      JsonObject json = this.parseJson(bytes);
      if (json == null) {
         return null;
      } else {
         byte[] direct = this.decodeBase64Field(json, "payloadBase64", "modelBase64", "contentBase64", "dataBase64", "encryptedBase64", "payload", "model", "data");
         if (direct != null) {
            return direct;
         } else {
            String encoding = this.getJsonString(json, "encoding");
            String content = this.getJsonString(json, "content");
            if (content != null && "base64".equalsIgnoreCase(encoding)) {
               try {
                  return Base64.getMimeDecoder().decode(content);
               } catch (Exception var9) {
                  try {
                     return Base64.getDecoder().decode(content);
                  } catch (Exception var8) {
                  }
               }
            }

            return null;
         }
      }
   }

   private byte[] decodeBase64Field(JsonObject json, String... keys) {
      String[] var3 = keys;
      int var4 = keys.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String key = var3[var5];
         String value = this.getJsonString(json, key);
         if (value != null && !value.isBlank()) {
            try {
               return Base64.getMimeDecoder().decode(value);
            } catch (Exception var11) {
               try {
                  return Base64.getDecoder().decode(value);
               } catch (Exception var10) {
               }
            }
         }
      }

      return null;
   }

   private boolean looksLikeJson(byte[] bytes) {
      if (bytes != null && bytes.length != 0) {
         int i;
         for(i = 0; i < bytes.length && Character.isWhitespace((char)bytes[i]); ++i) {
         }

         if (i >= bytes.length) {
            return false;
         } else {
            return bytes[i] == 123 || bytes[i] == 91;
         }
      } else {
         return false;
      }
   }

   private boolean isJsonContentType(String contentType) {
      if (contentType == null) {
         return false;
      } else {
         String lower = contentType.toLowerCase();
         return lower.contains("application/json") || lower.contains("text/json");
      }
   }

   private String previewResponse(byte[] bytes) {
      if (bytes != null && bytes.length != 0) {
         String text = (new String(bytes, StandardCharsets.UTF_8)).trim();
         return text.length() > 160 ? text.substring(0, 160) + "..." : text;
      } else {
         return "empty response";
      }
   }

   private String joinBaseUrl(String baseUrl, String path) {
      String trimmed = baseUrl == null ? "" : baseUrl.trim();
      if (trimmed.endsWith("/")) {
         trimmed = trimmed.substring(0, trimmed.length() - 1);
      }

      if (path != null && !path.isBlank()) {
         return path.startsWith("/") ? trimmed + path : trimmed + "/" + path;
      } else {
         return trimmed;
      }
   }

   private String getJsonString(JsonObject json, String key) {
      if (json != null && key != null && json.has(key)) {
         try {
            return json.get(key).getAsString();
         } catch (Exception var4) {
            return null;
         }
      } else {
         return null;
      }
   }

   private String firstNonBlank(String... values) {
      if (values == null) {
         return null;
      } else {
         String[] var2 = values;
         int var3 = values.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            String value = var2[var4];
            if (value != null && !value.isBlank()) {
               return value.trim();
            }
         }

         return null;
      }
   }

   private boolean tryLoadEncryptedDatabase(Path file) {
      try {
         if (!Files.exists(file, new LinkOption[0])) {
            return false;
         } else {
            byte[] bytes = Files.readAllBytes(file);
            return this.tryLoadEncryptedBytes(bytes, file.toString());
         }
      } catch (Exception var3) {
         LOGGER.warn("Failed loading encrypted CombatAgent DB {}: {}", file, var3.getMessage());
         return false;
      }
   }

   private boolean tryLoadEncryptedBytes(byte[] encryptedBytes, String source) {
      Path tempFile = null;

      boolean var5;
      try {
         byte[] decrypted = this.decryptEncryptedDatabase(encryptedBytes);
         if (!this.isSQLiteBytes(decrypted)) {
            var5 = false;
            return var5;
         }

         tempFile = Files.createTempFile("cyemer-db-", ".db");
         Files.write(tempFile, decrypted, new OpenOption[0]);
         Connection connection = this.openReadOnlyConnection(tempFile);

         label225: {
            boolean var7;
            label208: {
               try {
                  double[] params = this.readNetworkParams(connection);
                  if (params == null || params.length != this.network.numParams()) {
                     var7 = false;
                     break label208;
                  }

                  synchronized(this.modelLock) {
                     this.network.setParams(params);
                     this.loadMetadata(connection);
                     this.dirty = false;
                  }
               } catch (Throwable var25) {
                  if (connection != null) {
                     try {
                        connection.close();
                     } catch (Throwable var23) {
                        var25.addSuppressed(var23);
                     }
                  }

                  throw var25;
               }

               if (connection != null) {
                  connection.close();
               }
               break label225;
            }

            if (connection != null) {
               connection.close();
            }

            return var7;
         }

         var5 = true;
         return var5;
      } catch (Exception var26) {
         LOGGER.warn("Failed parsing encrypted CombatAgent DB {}: {}", source, var26.getMessage());
         var5 = false;
      } finally {
         if (tempFile != null) {
            try {
               Files.deleteIfExists(tempFile);
            } catch (Exception var22) {
            }
         }

      }

      return var5;
   }

   private byte[] decryptEncryptedDatabase(byte[] encryptedBytes) throws Exception {
      if (encryptedBytes != null && encryptedBytes.length >= ENCRYPTED_DB_MAGIC.length + 12 + 1) {
         ByteBuffer buffer = ByteBuffer.wrap(encryptedBytes);
         byte[] magic = new byte[ENCRYPTED_DB_MAGIC.length];
         buffer.get(magic);
         if (!Arrays.equals(magic, ENCRYPTED_DB_MAGIC)) {
            throw new IllegalArgumentException("Encrypted database magic mismatch");
         } else {
            byte[] iv = new byte[12];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(2, this.getEncryptionKey(), new GCMParameterSpec(128, iv));
            return cipher.doFinal(ciphertext);
         }
      } else {
         throw new IllegalArgumentException("Encrypted database payload too short");
      }
   }

   private SecretKeySpec getEncryptionKey() throws Exception {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] key = digest.digest("cyemer.autosword.pretrained.database.v1".getBytes(StandardCharsets.UTF_8));
      return new SecretKeySpec(key, "AES");
   }

   private boolean loadAnyDatabase(Path databaseFile) throws Exception {
      String databaseJson = Files.readString(databaseFile, StandardCharsets.UTF_8);
      JsonObject root = JsonParser.parseString(databaseJson).getAsJsonObject();
      if (root != null && root.has("schemaVersion")) {
         int schemaVersion = root.get("schemaVersion").getAsInt();
         if (schemaVersion == 2) {
            return this.loadCurrentFrameworkDatabase(root);
         } else {
            return schemaVersion == 1 ? this.loadLegacyMatrixDatabase(root) : false;
         }
      } else {
         return false;
      }
   }

   private boolean tryLoadLegacyJsonDatabase(Path databaseFile) {
      if (!Files.exists(databaseFile, new LinkOption[0])) {
         return false;
      } else if (this.isSQLiteFile(databaseFile)) {
         return false;
      } else {
         try {
            return this.loadAnyDatabase(databaseFile);
         } catch (Exception var3) {
            return false;
         }
      }
   }

   private boolean tryLoadSqliteDatabase(Path databaseFile) {
      if (Files.exists(databaseFile, new LinkOption[0]) && this.isSQLiteFile(databaseFile)) {
         try {
            Connection connection = this.openConnection(databaseFile);

            boolean var4;
            label65: {
               try {
                  this.ensureSchema(connection);
                  double[] params = this.readNetworkParams(connection);
                  if (params != null && params.length == this.network.numParams()) {
                     synchronized(this.modelLock) {
                        this.network.setParams(params);
                        this.loadMetadata(connection);
                        this.dirty = false;
                     }

                     var4 = true;
                     break label65;
                  }

                  var4 = false;
               } catch (Throwable var8) {
                  if (connection != null) {
                     try {
                        connection.close();
                     } catch (Throwable var6) {
                        var8.addSuppressed(var6);
                     }
                  }

                  throw var8;
               }

               if (connection != null) {
                  connection.close();
               }

               return var4;
            }

            if (connection != null) {
               connection.close();
            }

            return var4;
         } catch (Exception var9) {
            LOGGER.warn("Failed loading SQLite CombatAgent DB {}: {}", databaseFile, var9.getMessage());
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean loadCurrentFrameworkDatabase(JsonObject root) {
      JsonArray paramsArray = root.getAsJsonArray("params");
      if (paramsArray == null) {
         return false;
      } else {
         double[] params = new double[paramsArray.size()];

         for(int i = 0; i < paramsArray.size(); ++i) {
            params[i] = paramsArray.get(i).getAsDouble();
         }

         synchronized(this.modelLock) {
            if (params.length != this.network.numParams()) {
               return false;
            } else {
               this.network.setParams(params);
               if (root.has("epsilon")) {
                  this.epsilon = this.clamp(root.get("epsilon").getAsDouble(), 0.01D, 1.0D);
               }

               if (root.has("trainingStepCount")) {
                  this.trainingStepCount = Math.max(0L, root.get("trainingStepCount").getAsLong());
               }

               if (root.has("trainedBatchCount")) {
                  this.trainedBatchCount = Math.max(0L, root.get("trainedBatchCount").getAsLong());
                  this.lastSavedBatchCount = this.trainedBatchCount;
               } else {
                  this.lastSavedBatchCount = 0L;
               }

               if (root.has("lastUpdatedEpochMs")) {
                  this.lastSuccessfulSaveTimeMs = Math.max(0L, root.get("lastUpdatedEpochMs").getAsLong());
               }

               this.dirty = false;
               return true;
            }
         }
      }
   }

   private boolean loadLegacyMatrixDatabase(JsonObject root) {
      JsonObject model = root.getAsJsonObject("model");
      if (model == null) {
         return false;
      } else {
         double[][] weightsInputH1 = this.deserializeMatrix(model.getAsJsonObject("weightsInputH1"), this.inputSize, this.h1Size);
         double[] biasH1 = this.deserializeVector(model.getAsJsonObject("biasH1"), this.h1Size);
         double[][] weightsH1H2 = this.deserializeMatrix(model.getAsJsonObject("weightsH1H2"), this.h1Size, this.h2Size);
         double[] biasH2 = this.deserializeVector(model.getAsJsonObject("biasH2"), this.h2Size);
         double[][] weightsH2Output = this.deserializeMatrix(model.getAsJsonObject("weightsH2Output"), this.h2Size, this.outputSize);
         double[] biasOutput = this.deserializeVector(model.getAsJsonObject("biasOutput"), this.outputSize);
         if (weightsInputH1 != null && biasH1 != null && weightsH1H2 != null && biasH2 != null && weightsH2Output != null && biasOutput != null) {
            this.applyLegacyParameters(weightsInputH1, biasH1, weightsH1H2, biasH2, weightsH2Output, biasOutput);
            synchronized(this.modelLock) {
               if (root.has("epsilon")) {
                  this.epsilon = this.clamp(root.get("epsilon").getAsDouble(), 0.01D, 1.0D);
               }

               this.dirty = true;
               return true;
            }
         } else {
            return false;
         }
      }
   }

   private CombatAgent.Snapshot buildSnapshot() {
      long snapshotBatchCount;
      long snapshotTrainingStepCount;
      long snapshotLastUpdatedEpochMs;
      double snapshotEpsilon;
      int snapshotMemorySize;
      double[] snapshotParams;
      synchronized(this.modelLock) {
         snapshotBatchCount = this.trainedBatchCount;
         snapshotTrainingStepCount = this.trainingStepCount;
         snapshotEpsilon = this.epsilon;
         snapshotMemorySize = this.memory.size();
         snapshotLastUpdatedEpochMs = System.currentTimeMillis();
         snapshotParams = this.network.getParams();
      }

      return new CombatAgent.Snapshot(snapshotBatchCount, snapshotTrainingStepCount, snapshotLastUpdatedEpochMs, snapshotEpsilon, snapshotMemorySize, snapshotParams);
   }

   private void writeSqliteDatabase(Path databaseFile, CombatAgent.Snapshot snapshot) throws Exception {
      Connection connection = this.openConnection(databaseFile);

      try {
         connection.setAutoCommit(false);

         try {
            this.ensureSchema(connection);
            this.writeMetadata(connection, snapshot);
            this.writeNetworkParams(connection, snapshot.params);
            this.writeTrainingHistory(connection, snapshot);
            connection.commit();
         } catch (Exception var11) {
            connection.rollback();
            throw var11;
         } finally {
            connection.setAutoCommit(true);
         }
      } catch (Throwable var13) {
         if (connection != null) {
            try {
               connection.close();
            } catch (Throwable var10) {
               var13.addSuppressed(var10);
            }
         }

         throw var13;
      }

      if (connection != null) {
         connection.close();
      }

   }

   private Connection openConnection(Path databaseFile) throws Exception {
      Path parent = databaseFile.getParent();
      if (parent != null) {
         Files.createDirectories(parent);
      }

      if (Files.exists(databaseFile, new LinkOption[0]) && !this.isSQLiteFile(databaseFile)) {
         Files.delete(databaseFile);
      }

      return DriverManager.getConnection("jdbc:sqlite:" + String.valueOf(databaseFile.toAbsolutePath()));
   }

   private void ensureSchema(Connection connection) throws Exception {
      Statement statement = connection.createStatement();

      try {
         statement.executeUpdate("CREATE TABLE IF NOT EXISTS model_metadata (meta_key TEXT PRIMARY KEY,meta_value TEXT NOT NULL)");
         statement.executeUpdate("CREATE TABLE IF NOT EXISTS model_network_params (param_index INTEGER PRIMARY KEY,param_value REAL NOT NULL)");
         statement.executeUpdate("CREATE TABLE IF NOT EXISTS model_training_history (id INTEGER PRIMARY KEY AUTOINCREMENT,saved_at_epoch_ms INTEGER NOT NULL,trained_batch_count INTEGER NOT NULL,training_step_count INTEGER NOT NULL,epsilon REAL NOT NULL,memory_size INTEGER NOT NULL)");
      } catch (Throwable var6) {
         if (statement != null) {
            try {
               statement.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (statement != null) {
         statement.close();
      }

   }

   private void writeMetadata(Connection connection, CombatAgent.Snapshot snapshot) throws Exception {
      PreparedStatement statement = connection.prepareStatement("INSERT OR REPLACE INTO model_metadata (meta_key, meta_value) VALUES (?, ?)");

      try {
         this.insertMeta(statement, "schema_version", String.valueOf(2));
         this.insertMeta(statement, "framework", "cyemer-java-mlp");
         this.insertMeta(statement, "epsilon", String.valueOf(snapshot.epsilon));
         this.insertMeta(statement, "memory_size", String.valueOf(snapshot.memorySize));
         this.insertMeta(statement, "training_step_count", String.valueOf(snapshot.trainingStepCount));
         this.insertMeta(statement, "trained_batch_count", String.valueOf(snapshot.trainedBatchCount));
         this.insertMeta(statement, "last_updated_epoch_ms", String.valueOf(snapshot.lastUpdatedEpochMs));
      } catch (Throwable var7) {
         if (statement != null) {
            try {
               statement.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (statement != null) {
         statement.close();
      }

   }

   private void insertMeta(PreparedStatement statement, String key, String value) throws Exception {
      statement.setString(1, key);
      statement.setString(2, value);
      statement.executeUpdate();
   }

   private void writeNetworkParams(Connection connection, double[] params) throws Exception {
      Statement clear = connection.createStatement();

      try {
         clear.executeUpdate("DELETE FROM model_network_params");
      } catch (Throwable var8) {
         if (clear != null) {
            try {
               clear.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }

         throw var8;
      }

      if (clear != null) {
         clear.close();
      }

      PreparedStatement insert = connection.prepareStatement("INSERT INTO model_network_params (param_index, param_value) VALUES (?, ?)");

      try {
         int i = 0;

         while(true) {
            if (i >= params.length) {
               insert.executeBatch();
               break;
            }

            insert.setInt(1, i);
            insert.setDouble(2, params[i]);
            insert.addBatch();
            ++i;
         }
      } catch (Throwable var9) {
         if (insert != null) {
            try {
               insert.close();
            } catch (Throwable var6) {
               var9.addSuppressed(var6);
            }
         }

         throw var9;
      }

      if (insert != null) {
         insert.close();
      }

   }

   private void writeTrainingHistory(Connection connection, CombatAgent.Snapshot snapshot) throws Exception {
      PreparedStatement insert = connection.prepareStatement("INSERT INTO model_training_history (saved_at_epoch_ms, trained_batch_count, training_step_count, epsilon, memory_size) VALUES (?, ?, ?, ?, ?)");

      try {
         insert.setLong(1, snapshot.lastUpdatedEpochMs);
         insert.setLong(2, snapshot.trainedBatchCount);
         insert.setLong(3, snapshot.trainingStepCount);
         insert.setDouble(4, snapshot.epsilon);
         insert.setInt(5, snapshot.memorySize);
         insert.executeUpdate();
      } catch (Throwable var8) {
         if (insert != null) {
            try {
               insert.close();
            } catch (Throwable var6) {
               var8.addSuppressed(var6);
            }
         }

         throw var8;
      }

      if (insert != null) {
         insert.close();
      }

      Statement cleanup = connection.createStatement();

      try {
         cleanup.executeUpdate("DELETE FROM model_training_history WHERE id NOT IN (SELECT id FROM model_training_history ORDER BY id DESC LIMIT 5000)");
      } catch (Throwable var9) {
         if (cleanup != null) {
            try {
               cleanup.close();
            } catch (Throwable var7) {
               var9.addSuppressed(var7);
            }
         }

         throw var9;
      }

      if (cleanup != null) {
         cleanup.close();
      }

   }

   private double[] readNetworkParams(Connection connection) throws Exception {
      double[] params = this.readNetworkParamsFromTable(connection, "model_network_params");
      return params != null ? params : this.readNetworkParamsFromTable(connection, LEGACY_TABLE_PARAMS);
   }

   private double[] readNetworkParamsFromTable(Connection connection, String tableName) {
      try {
         int expected = this.network.numParams();
         double[] params = new double[expected];
         boolean[] seen = new boolean[expected];
         int count = 0;
         PreparedStatement statement = connection.prepareStatement("SELECT param_index, param_value FROM " + tableName + " ORDER BY param_index");

         Object var10;
         label90: {
            try {
               label91: {
                  ResultSet resultSet = statement.executeQuery();

                  label92: {
                     try {
                        while(resultSet.next()) {
                           int index = resultSet.getInt("param_index");
                           if (index < 0 || index >= expected || seen[index]) {
                              var10 = null;
                              break label92;
                           }

                           params[index] = resultSet.getDouble("param_value");
                           seen[index] = true;
                           ++count;
                        }
                     } catch (Throwable var13) {
                        if (resultSet != null) {
                           try {
                              resultSet.close();
                           } catch (Throwable var12) {
                              var13.addSuppressed(var12);
                           }
                        }

                        throw var13;
                     }

                     if (resultSet != null) {
                        resultSet.close();
                     }
                     break label91;
                  }

                  if (resultSet != null) {
                     resultSet.close();
                  }
                  break label90;
               }
            } catch (Throwable var14) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var11) {
                     var14.addSuppressed(var11);
                  }
               }

               throw var14;
            }

            if (statement != null) {
               statement.close();
            }

            if (count != expected) {
               return null;
            }

            return params;
         }

         if (statement != null) {
            statement.close();
         }

         return (double[])var10;
      } catch (Exception var15) {
         return null;
      }
   }

   private void loadMetadata(Connection connection) throws Exception {
      boolean loaded = this.loadMetadataFromTable(connection, "model_metadata");
      if (!loaded) {
         this.loadMetadataFromTable(connection, LEGACY_TABLE_METADATA);
      }

   }

   private boolean loadMetadataFromTable(Connection connection, String tableName) {
      boolean loadedAny = false;

      try {
         PreparedStatement statement = connection.prepareStatement("SELECT meta_key, meta_value FROM " + tableName);

         try {
            ResultSet resultSet = statement.executeQuery();

            try {
               while(resultSet.next()) {
                  String key = resultSet.getString("meta_key");
                  String value = resultSet.getString("meta_value");
                  if (key != null && value != null) {
                     loadedAny = true;
                     byte var9 = -1;
                     switch(key.hashCode()) {
                     case -1718316447:
                        if (key.equals("training_step_count")) {
                           var9 = 1;
                        }
                        break;
                     case -1644040142:
                        if (key.equals("trained_batch_count")) {
                           var9 = 2;
                        }
                        break;
                     case -1535503510:
                        if (key.equals("epsilon")) {
                           var9 = 0;
                        }
                        break;
                     case 199614121:
                        if (key.equals("last_updated_epoch_ms")) {
                           var9 = 3;
                        }
                     }

                     switch(var9) {
                     case 0:
                        this.epsilon = this.clamp(this.parseDouble(value, this.epsilon), 0.01D, 1.0D);
                        break;
                     case 1:
                        this.trainingStepCount = Math.max(0L, this.parseLong(value, this.trainingStepCount));
                        break;
                     case 2:
                        this.trainedBatchCount = Math.max(0L, this.parseLong(value, this.trainedBatchCount));
                        this.lastSavedBatchCount = this.trainedBatchCount;
                        break;
                     case 3:
                        this.lastSuccessfulSaveTimeMs = Math.max(0L, this.parseLong(value, this.lastSuccessfulSaveTimeMs));
                     }
                  }
               }
            } catch (Throwable var12) {
               if (resultSet != null) {
                  try {
                     resultSet.close();
                  } catch (Throwable var11) {
                     var12.addSuppressed(var11);
                  }
               }

               throw var12;
            }

            if (resultSet != null) {
               resultSet.close();
            }
         } catch (Throwable var13) {
            if (statement != null) {
               try {
                  statement.close();
               } catch (Throwable var10) {
                  var13.addSuppressed(var10);
               }
            }

            throw var13;
         }

         if (statement != null) {
            statement.close();
         }

         return loadedAny;
      } catch (Exception var14) {
         return false;
      }
   }

   private long parseLong(String value, long fallback) {
      try {
         return Long.parseLong(value);
      } catch (Exception var5) {
         return fallback;
      }
   }

   private double parseDouble(String value, double fallback) {
      try {
         return Double.parseDouble(value);
      } catch (Exception var5) {
         return fallback;
      }
   }

   private boolean isSQLiteFile(Path file) {
      if (!Files.exists(file, new LinkOption[0])) {
         return false;
      } else {
         byte[] header = new byte[16];

         try {
            InputStream inputStream = Files.newInputStream(file);

            boolean var10;
            label54: {
               boolean var6;
               try {
                  int read = inputStream.read(header);
                  if (read < "SQLite format 3".length()) {
                     var10 = false;
                     break label54;
                  }

                  String headerText = new String(header, 0, "SQLite format 3".length(), StandardCharsets.US_ASCII);
                  var6 = "SQLite format 3".equals(headerText);
               } catch (Throwable var8) {
                  if (inputStream != null) {
                     try {
                        inputStream.close();
                     } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                     }
                  }

                  throw var8;
               }

               if (inputStream != null) {
                  inputStream.close();
               }

               return var6;
            }

            if (inputStream != null) {
               inputStream.close();
            }

            return var10;
         } catch (Exception var9) {
            return false;
         }
      }
   }

   private boolean tryLoadLegacySerializedDatabase(Path legacyFilePath) {
      if (!Files.exists(legacyFilePath, new LinkOption[0])) {
         return false;
      } else {
         try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(legacyFilePath.toFile()));

            boolean var9;
            label47: {
               try {
                  double[][] weightsInputH1 = (double[][])ois.readObject();
                  double[] biasH1 = (double[])ois.readObject();
                  double[][] weightsH1H2 = (double[][])ois.readObject();
                  double[] biasH2 = (double[])ois.readObject();
                  double[][] weightsH2Output = (double[][])ois.readObject();
                  double[] biasOutput = (double[])ois.readObject();
                  if (!this.isMatrixShape(weightsInputH1, this.inputSize, this.h1Size) || !this.isVectorShape(biasH1, this.h1Size) || !this.isMatrixShape(weightsH1H2, this.h1Size, this.h2Size) || !this.isVectorShape(biasH2, this.h2Size) || !this.isMatrixShape(weightsH2Output, this.h2Size, this.outputSize) || !this.isVectorShape(biasOutput, this.outputSize)) {
                     var9 = false;
                     break label47;
                  }

                  this.applyLegacyParameters(weightsInputH1, biasH1, weightsH1H2, biasH2, weightsH2Output, biasOutput);
                  var9 = true;
               } catch (Throwable var11) {
                  try {
                     ois.close();
                  } catch (Throwable var10) {
                     var11.addSuppressed(var10);
                  }

                  throw var11;
               }

               ois.close();
               return var9;
            }

            ois.close();
            return var9;
         } catch (Exception var12) {
            return false;
         }
      }
   }

   private void applyLegacyParameters(double[][] weightsInputH1, double[] biasH1, double[][] weightsH1H2, double[] biasH2, double[][] weightsH2Output, double[] biasOutput) {
      int totalParams = this.network.numParams();
      double[] params = new double[totalParams];
      int index = 0;
      int index = this.appendMatrix(params, index, weightsInputH1, this.inputSize, this.h1Size);
      index = this.appendVector(params, index, biasH1, this.h1Size);
      index = this.appendMatrix(params, index, weightsH1H2, this.h1Size, this.h2Size);
      index = this.appendVector(params, index, biasH2, this.h2Size);
      index = this.appendMatrix(params, index, weightsH2Output, this.h2Size, this.outputSize);
      index = this.appendVector(params, index, biasOutput, this.outputSize);
      if (index != totalParams) {
         throw new IllegalStateException("Legacy parameter conversion failed");
      } else {
         synchronized(this.modelLock) {
            this.network.setParams(params);
         }
      }
   }

   private int appendMatrix(double[] target, int start, double[][] matrix, int rows, int cols) {
      int index = start;

      for(int row = 0; row < rows; ++row) {
         for(int col = 0; col < cols; ++col) {
            target[index++] = matrix[row][col];
         }
      }

      return index;
   }

   private int appendVector(double[] target, int start, double[] vector, int length) {
      int index = start;

      for(int i = 0; i < length; ++i) {
         target[index++] = vector[i];
      }

      return index;
   }

   private double[][] deserializeMatrix(JsonObject table, int expectedRows, int expectedCols) {
      if (table != null && table.has("rows") && table.has("cols")) {
         int rows = table.get("rows").getAsInt();
         int cols = table.get("cols").getAsInt();
         if (rows == expectedRows && cols == expectedCols) {
            JsonArray values = table.getAsJsonArray("values");
            if (values != null && values.size() == rows * cols) {
               double[][] matrix = new double[rows][cols];
               int index = 0;

               for(int row = 0; row < rows; ++row) {
                  for(int col = 0; col < cols; ++col) {
                     matrix[row][col] = values.get(index++).getAsDouble();
                  }
               }

               return matrix;
            } else {
               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private double[] deserializeVector(JsonObject table, int expectedLength) {
      if (table != null && table.has("length")) {
         int length = table.get("length").getAsInt();
         if (length != expectedLength) {
            return null;
         } else {
            JsonArray values = table.getAsJsonArray("values");
            if (values != null && values.size() == length) {
               double[] vector = new double[length];

               for(int i = 0; i < length; ++i) {
                  vector[i] = values.get(i).getAsDouble();
               }

               return vector;
            } else {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   private boolean isMatrixShape(double[][] matrix, int rows, int cols) {
      if (matrix != null && matrix.length == rows) {
         double[][] var4 = matrix;
         int var5 = matrix.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            double[] row = var4[var6];
            if (row == null || row.length != cols) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean isVectorShape(double[] vector, int length) {
      return vector != null && vector.length == length;
   }

   private double clamp(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }

   private boolean allowLocalFallback() {
      String prop = System.getProperty("cyemer.autosword.allowLocalFallback");
      String env = System.getenv("CYEMER_DB_ALLOW_LOCAL_FALLBACK");
      return this.isTruthy(prop) || this.isTruthy(env);
   }

   private String resolveWorkerBaseUrl() {
      return this.firstNonBlank(System.getProperty("cyemer.autosword.workerBaseUrl"), System.getenv("CYEMER_DB_WORKER_BASE_URL"), "https://cyemer-auth.tvanvinkenroye.workers.dev");
   }

   private String resolveWorkerLicenseKey() {
      return this.firstNonBlank(System.getProperty("cyemer.autosword.licenseKey"), System.getenv("CYEMER_DB_LICENSE_KEY"), System.getenv("CYEMER_DB_LICENSE"), this.resolveBoosterLicenseKey());
   }

   private String resolveWorkerDeviceId() {
      String override = this.firstNonBlank(System.getProperty("cyemer.autosword.deviceId"), System.getenv("CYEMER_DB_DEVICE_ID"));
      if (override != null && !override.isBlank()) {
         return override;
      } else {
         try {
            String fingerprint = String.join("|", this.firstNonBlank(System.getProperty("os.name"), "unknown-os"), this.firstNonBlank(System.getProperty("os.arch"), "unknown-arch"), this.firstNonBlank(System.getProperty("user.name"), "unknown-user"));
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fingerprint.getBytes(StandardCharsets.UTF_8));
            byte[] shortHash = Arrays.copyOf(hash, 12);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(shortHash);
         } catch (Exception var6) {
            return "cyemer-unknown-device";
         }
      }
   }

   private String resolveBoosterLicenseKey() {
      try {
         Module module = Faith.getInstance().getModuleManager().getModule("Booster");
         if (module instanceof Booster) {
            Booster booster = (Booster)module;
            String key = booster.getLicenseKey().getValue();
            if (key != null && !key.isBlank()) {
               return key.trim();
            }
         }
      } catch (Exception var4) {
      }

      return null;
   }

   private long resolveModelCacheMaxAgeMs() {
      String value = this.firstNonBlank(System.getProperty("cyemer.autosword.modelCacheMaxAgeMs"), System.getenv("CYEMER_DB_MODEL_CACHE_MAX_AGE_MS"));
      if (value == null) {
         return 86400000L;
      } else {
         try {
            return Math.max(0L, Long.parseLong(value));
         } catch (Exception var3) {
            return 86400000L;
         }
      }
   }

   private boolean isTruthy(String value) {
      if (value == null) {
         return false;
      } else {
         String normalized = value.trim().toLowerCase();
         return "1".equals(normalized) || "true".equals(normalized) || "yes".equals(normalized) || "on".equals(normalized);
      }
   }

   private boolean isSQLiteBytes(byte[] bytes) {
      if (bytes != null && bytes.length >= "SQLite format 3".length()) {
         String header = new String(bytes, 0, "SQLite format 3".length(), StandardCharsets.US_ASCII);
         return "SQLite format 3".equals(header);
      } else {
         return false;
      }
   }

   private Path getDatabaseDirectory() {
      return FabricLoader.getInstance().getGameDir().resolve("cyemer").resolve("autosword").resolve("database");
   }

   private Path getModelCacheDirectory() {
      return FabricLoader.getInstance().getGameDir().resolve("cyemer").resolve("autosword").resolve("cache");
   }

   private Path getModelCacheFilePath() {
      return this.getModelCacheDirectory().resolve("model-cache.enc");
   }

   private Path getDatabaseFilePath() {
      return this.getDatabaseDirectory().resolve("Faith_Database.db");
   }

   private Path getEncryptedDatabaseFilePath() {
      return this.getDatabaseDirectory().resolve("Faith_Database.db.enc");
   }

   private void cleanupLegacyTypoDatabase() {
      Path legacyFile = this.getLegacyTypoDatabaseFilePath();
      Path legacyEncFile = this.getLegacyTypoEncryptedDatabaseFilePath();

      try {
         if (Files.exists(legacyFile, new LinkOption[0])) {
            Files.delete(legacyFile);
         }

         if (Files.exists(legacyEncFile, new LinkOption[0])) {
            Files.delete(legacyEncFile);
         }

         Path legacyDir = legacyFile.getParent();
         if (legacyDir != null && Files.exists(legacyDir, new LinkOption[0])) {
            Stream entries = Files.list(legacyDir);

            try {
               if (entries.findAny().isEmpty()) {
                  Files.delete(legacyDir);
               }
            } catch (Throwable var8) {
               if (entries != null) {
                  try {
                     entries.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (entries != null) {
               entries.close();
            }
         }
      } catch (Exception var9) {
      }

   }

   private Path getLegacyTypoDatabaseFilePath() {
      return FabricLoader.getInstance().getGameDir().resolve("cyemer").resolve("autosword").resolve("databse").resolve("Faith_Database.db");
   }

   private Path getLegacyTypoEncryptedDatabaseFilePath() {
      return FabricLoader.getInstance().getGameDir().resolve("cyemer").resolve("autosword").resolve("databse").resolve("Faith_Database.db.enc");
   }

   private Connection openReadOnlyConnection(Path databaseFile) throws Exception {
      Connection connection = DriverManager.getConnection("jdbc:sqlite:" + String.valueOf(databaseFile.toAbsolutePath()));
      connection.setReadOnly(true);
      return connection;
   }

   static {
      ENCRYPTED_DB_MAGIC = "CYEMERDB1".getBytes(StandardCharsets.US_ASCII);
      LEGACY_TABLE_METADATA = legacyTableName("_metadata");
      LEGACY_TABLE_PARAMS = legacyTableName("_network_params");
      LEGACY_TABLE_HISTORY = legacyTableName("_training_history");
      MISSING_LICENSE_NOTICE_SHOWN = new AtomicBoolean(false);
   }

   @Environment(EnvType.CLIENT)
   private static final class Network {
      private final int inputSize;
      private final int h1Size;
      private final int h2Size;
      private final int outputSize;
      private final Random random;
      private final double[][] weightsInputH1;
      private final double[] biasH1;
      private final double[][] weightsH1H2;
      private final double[] biasH2;
      private final double[][] weightsH2Output;
      private final double[] biasOutput;

      Network(int inputSize, int h1Size, int h2Size, int outputSize, long seed) {
         this.inputSize = inputSize;
         this.h1Size = h1Size;
         this.h2Size = h2Size;
         this.outputSize = outputSize;
         this.random = new Random(seed);
         this.weightsInputH1 = new double[inputSize][h1Size];
         this.biasH1 = new double[h1Size];
         this.weightsH1H2 = new double[h1Size][h2Size];
         this.biasH2 = new double[h2Size];
         this.weightsH2Output = new double[h2Size][outputSize];
         this.biasOutput = new double[outputSize];
         this.initializeXavier(this.weightsInputH1, inputSize, h1Size);
         this.initializeXavier(this.weightsH1H2, h1Size, h2Size);
         this.initializeXavier(this.weightsH2Output, h2Size, outputSize);
      }

      int numParams() {
         return this.inputSize * this.h1Size + this.h1Size + this.h1Size * this.h2Size + this.h2Size + this.h2Size * this.outputSize + this.outputSize;
      }

      double[] getParams() {
         double[] params = new double[this.numParams()];
         int index = 0;
         int index = this.appendMatrix(params, index, this.weightsInputH1);
         index = this.appendVector(params, index, this.biasH1);
         index = this.appendMatrix(params, index, this.weightsH1H2);
         index = this.appendVector(params, index, this.biasH2);
         index = this.appendMatrix(params, index, this.weightsH2Output);
         this.appendVector(params, index, this.biasOutput);
         return params;
      }

      void setParams(double[] params) {
         if (params != null && params.length == this.numParams()) {
            int index = 0;
            int index = this.readMatrix(params, index, this.weightsInputH1);
            index = this.readVector(params, index, this.biasH1);
            index = this.readMatrix(params, index, this.weightsH1H2);
            index = this.readVector(params, index, this.biasH2);
            index = this.readMatrix(params, index, this.weightsH2Output);
            this.readVector(params, index, this.biasOutput);
         } else {
            throw new IllegalArgumentException("Invalid parameter vector length");
         }
      }

      double[] predict(double[] input) {
         return this.forward(input).out;
      }

      void trainBatch(CombatAgent.Experience[] batch, double discountFactor, double learningRate) {
         if (batch != null && batch.length != 0) {
            double[][] gradW1 = new double[this.inputSize][this.h1Size];
            double[] gradB1 = new double[this.h1Size];
            double[][] gradW2 = new double[this.h1Size][this.h2Size];
            double[] gradB2 = new double[this.h2Size];
            double[][] gradW3 = new double[this.h2Size][this.outputSize];
            double[] gradB3 = new double[this.outputSize];
            double[] deltaOutput = new double[this.outputSize];
            double[] deltaH2 = new double[this.h2Size];
            double[] deltaH1 = new double[this.h1Size];
            CombatAgent.Experience[] var15 = batch;
            int var16 = batch.length;

            for(int var17 = 0; var17 < var16; ++var17) {
               CombatAgent.Experience exp = var15[var17];
               CombatAgent.ForwardPass current = this.forward(exp.state);
               CombatAgent.ForwardPass next = this.forward(exp.nextState);
               double maxNextQ = next.out[0];

               int i;
               for(i = 1; i < next.out.length; ++i) {
                  if (next.out[i] > maxNextQ) {
                     maxNextQ = next.out[i];
                  }
               }

               Arrays.fill(deltaOutput, 0.0D);
               i = Math.max(0, Math.min(exp.action, this.outputSize - 1));
               double targetQ = exp.reward + discountFactor * maxNextQ;
               deltaOutput[i] = current.out[i] - targetQ;

               int i;
               double sum;
               int j;
               for(i = 0; i < this.h2Size; ++i) {
                  sum = 0.0D;

                  for(j = 0; j < this.outputSize; ++j) {
                     sum += this.weightsH2Output[i][j] * deltaOutput[j];
                  }

                  deltaH2[i] = sum * this.leakyReluDerivative(current.z2[i]);
               }

               for(i = 0; i < this.h1Size; ++i) {
                  sum = 0.0D;

                  for(j = 0; j < this.h2Size; ++j) {
                     sum += this.weightsH1H2[i][j] * deltaH2[j];
                  }

                  deltaH1[i] = sum * this.leakyReluDerivative(current.z1[i]);
               }

               int j;
               for(i = 0; i < this.h2Size; ++i) {
                  for(j = 0; j < this.outputSize; ++j) {
                     gradW3[i][j] += current.a2[i] * deltaOutput[j];
                  }
               }

               for(i = 0; i < this.outputSize; ++i) {
                  gradB3[i] += deltaOutput[i];
               }

               for(i = 0; i < this.h1Size; ++i) {
                  for(j = 0; j < this.h2Size; ++j) {
                     gradW2[i][j] += current.a1[i] * deltaH2[j];
                  }
               }

               for(i = 0; i < this.h2Size; ++i) {
                  gradB2[i] += deltaH2[i];
               }

               for(i = 0; i < this.inputSize; ++i) {
                  for(j = 0; j < this.h1Size; ++j) {
                     gradW1[i][j] += exp.state[i] * deltaH1[j];
                  }
               }

               for(i = 0; i < this.h1Size; ++i) {
                  gradB1[i] += deltaH1[i];
               }
            }

            double step = learningRate / (double)batch.length;
            this.applyGradient(this.weightsInputH1, gradW1, step);
            this.applyGradient(this.biasH1, gradB1, step);
            this.applyGradient(this.weightsH1H2, gradW2, step);
            this.applyGradient(this.biasH2, gradB2, step);
            this.applyGradient(this.weightsH2Output, gradW3, step);
            this.applyGradient(this.biasOutput, gradB3, step);
         }
      }

      private CombatAgent.ForwardPass forward(double[] input) {
         CombatAgent.ForwardPass pass = new CombatAgent.ForwardPass(this.h1Size, this.h2Size, this.outputSize);

         int j;
         double sum;
         int i;
         for(j = 0; j < this.h1Size; ++j) {
            sum = this.biasH1[j];

            for(i = 0; i < this.inputSize; ++i) {
               sum += input[i] * this.weightsInputH1[i][j];
            }

            pass.z1[j] = sum;
            pass.a1[j] = this.leakyRelu(sum);
         }

         for(j = 0; j < this.h2Size; ++j) {
            sum = this.biasH2[j];

            for(i = 0; i < this.h1Size; ++i) {
               sum += pass.a1[i] * this.weightsH1H2[i][j];
            }

            pass.z2[j] = sum;
            pass.a2[j] = this.leakyRelu(sum);
         }

         for(j = 0; j < this.outputSize; ++j) {
            sum = this.biasOutput[j];

            for(i = 0; i < this.h2Size; ++i) {
               sum += pass.a2[i] * this.weightsH2Output[i][j];
            }

            pass.out[j] = sum;
         }

         return pass;
      }

      private void initializeXavier(double[][] matrix, int fanIn, int fanOut) {
         double limit = Math.sqrt(6.0D / (double)(fanIn + fanOut));

         for(int i = 0; i < matrix.length; ++i) {
            for(int j = 0; j < matrix[i].length; ++j) {
               matrix[i][j] = this.random.nextDouble() * 2.0D * limit - limit;
            }
         }

      }

      private int appendMatrix(double[] target, int startIndex, double[][] matrix) {
         int index = startIndex;
         double[][] var5 = matrix;
         int var6 = matrix.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            double[] row = var5[var7];
            double[] var9 = row;
            int var10 = row.length;

            for(int var11 = 0; var11 < var10; ++var11) {
               double value = var9[var11];
               target[index++] = value;
            }
         }

         return index;
      }

      private int appendVector(double[] target, int startIndex, double[] vector) {
         int index = startIndex;
         double[] var5 = vector;
         int var6 = vector.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            double value = var5[var7];
            target[index++] = value;
         }

         return index;
      }

      private int readMatrix(double[] source, int startIndex, double[][] matrix) {
         int index = startIndex;

         for(int i = 0; i < matrix.length; ++i) {
            for(int j = 0; j < matrix[i].length; ++j) {
               matrix[i][j] = source[index++];
            }
         }

         return index;
      }

      private int readVector(double[] source, int startIndex, double[] vector) {
         int index = startIndex;

         for(int i = 0; i < vector.length; ++i) {
            vector[i] = source[index++];
         }

         return index;
      }

      private void applyGradient(double[][] weights, double[][] gradients, double step) {
         for(int i = 0; i < weights.length; ++i) {
            for(int j = 0; j < weights[i].length; ++j) {
               weights[i][j] -= gradients[i][j] * step;
            }
         }

      }

      private void applyGradient(double[] biases, double[] gradients, double step) {
         for(int i = 0; i < biases.length; ++i) {
            biases[i] -= gradients[i] * step;
         }

      }

      private double leakyRelu(double value) {
         return value > 0.0D ? value : value * 0.01D;
      }

      private double leakyReluDerivative(double value) {
         return value > 0.0D ? 1.0D : 0.01D;
      }
   }

   @Environment(EnvType.CLIENT)
   private static class Experience {
      final double[] state;
      final int action;
      final double reward;
      final double[] nextState;

      Experience(double[] state, int action, double reward, double[] nextState) {
         this.state = state;
         this.action = action;
         this.reward = reward;
         this.nextState = nextState;
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class Snapshot {
      final long trainedBatchCount;
      final long trainingStepCount;
      final long lastUpdatedEpochMs;
      final double epsilon;
      final int memorySize;
      final double[] params;

      private Snapshot(long trainedBatchCount, long trainingStepCount, long lastUpdatedEpochMs, double epsilon, int memorySize, double[] params) {
         this.trainedBatchCount = trainedBatchCount;
         this.trainingStepCount = trainingStepCount;
         this.lastUpdatedEpochMs = lastUpdatedEpochMs;
         this.epsilon = epsilon;
         this.memorySize = memorySize;
         this.params = params;
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class ForwardPass {
      final double[] z1;
      final double[] a1;
      final double[] z2;
      final double[] a2;
      final double[] out;

      ForwardPass(int h1Size, int h2Size, int outputSize) {
         this.z1 = new double[h1Size];
         this.a1 = new double[h1Size];
         this.z2 = new double[h2Size];
         this.a2 = new double[h2Size];
         this.out = new double[outputSize];
      }
   }
}
