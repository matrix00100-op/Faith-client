package com.slither.cyemer.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;

@Environment(EnvType.CLIENT)
public class RotationManager {
   private static final class_310 mc = class_310.method_1551();
   private static final SecureRandom random = new SecureRandom();
   private static Supplier<class_243> targetSupplier = null;
   private static boolean isActive = false;
   private static boolean isSilent = false;
   private static double baseStrength = 5.0D;
   private static double randomness = 0.0D;
   private static long reactionDelayMs = 0L;
   private static RotationManager.RotationMode mode;
   private static boolean isReleasing;
   private static boolean isYawOnly;
   private static Object currentOwner;
   private static RotationManager.Priority currentPriority;
   private static float visualYaw;
   private static float visualPitch;
   private static float finalYaw;
   private static float finalPitch;
   private static float lastSetYaw;
   private static float lastSetPitch;
   private static long lastUpdateTime;
   private static final double TARGET_MS_PER_FRAME = 16.666666666666668D;
   private static float velocityYaw;
   private static float velocityPitch;
   private static final float SMOOTHING_FACTOR = 0.25F;
   private static final float YAW_SMOOTHING = 0.22F;
   private static final float PITCH_SMOOTHING = 0.28F;
   private static float pitchLag;
   private static final float PITCH_LAG_FACTOR = 0.85F;
   private static long lastInputTime;
   private static final List<RotationManager.MouseInput> inputBuffer;
   private static double accumulatedYaw;
   private static double accumulatedPitch;
   private static long nextReactionTime;
   private static boolean hasReacted;
   private static class_243 lastKnownTarget;
   private static long lastTargetUpdateTime;
   private static boolean lostTarget;
   private static long targetLossTime;
   private static float microAdjustmentYaw;
   private static float microAdjustmentPitch;
   private static long lastMicroAdjustment;
   private static int yawUpdateCounter;
   private static int pitchUpdateCounter;
   private static final Object LEGACY_OWNER;

   public static void setRotationSupplier(Object owner, RotationManager.Priority priority, Supplier<class_243> supplier, double str, RotationManager.RotationMode rotMode, double rand, boolean silent, boolean yawOnly) {
      setRotationSupplier(owner, priority, supplier, str, rotMode, rand, 0L, silent, yawOnly);
   }

   public static void setRotationSupplier(Object owner, RotationManager.Priority priority, Supplier<class_243> supplier, double str, RotationManager.RotationMode rotMode, double rand, long reactionTime, boolean silent, boolean yawOnly) {
      if (mc.field_1724 != null) {
         if (!isActive || currentOwner == owner || priority.ordinal() >= currentPriority.ordinal()) {
            if (!isActive || isReleasing || currentOwner != owner) {
               isActive = true;
               isReleasing = false;
               currentOwner = owner;
               currentPriority = priority;
               float currentYaw = mc.field_1724.method_36454();
               float currentPitch = mc.field_1724.method_36455();
               visualYaw = currentYaw;
               visualPitch = currentPitch;
               finalYaw = currentYaw;
               finalPitch = currentPitch;
               lastSetYaw = currentYaw;
               lastSetPitch = currentPitch;
               velocityYaw = 0.0F;
               velocityPitch = 0.0F;
               pitchLag = 0.0F;
               inputBuffer.clear();
               accumulatedYaw = 0.0D;
               accumulatedPitch = 0.0D;
               hasReacted = reactionTime <= 0L;
               nextReactionTime = System.currentTimeMillis() + reactionTime;
               lastUpdateTime = System.currentTimeMillis();
               yawUpdateCounter = 0;
               pitchUpdateCounter = 0;
            }

            targetSupplier = supplier;
            baseStrength = str;
            mode = rotMode;
            randomness = rand;
            reactionDelayMs = reactionTime;
            isSilent = silent;
            isYawOnly = yawOnly;
         }
      }
   }

   public static void setRotationSupplier(Object owner, RotationManager.Priority priority, Supplier<class_243> supplier, double str, boolean silent) {
      setRotationSupplier(owner, priority, supplier, str, RotationManager.RotationMode.SMOOTH, 0.0D, 0L, silent, false);
   }

   public static void clearTarget() {
      clearTarget(LEGACY_OWNER);
   }

   public static void stop() {
      forceStop();
   }

   public static void clearTarget(Object owner) {
      if (isActive && !isReleasing) {
         if (currentOwner == owner || currentOwner == null) {
            targetSupplier = null;
            isReleasing = true;
         }

      }
   }

   public static void stop(Object owner) {
      if (isActive && (currentOwner == null || currentOwner == owner)) {
         forceStop();
      }
   }

   private static void forceStop() {
      if (isSilent && mc.field_1724 != null) {
         mc.field_1724.method_36456(visualYaw);
         mc.field_1724.field_5982 = visualYaw;
         if (!isYawOnly) {
            mc.field_1724.method_36457(visualPitch);
            mc.field_1724.field_6004 = visualPitch;
         }
      }

      isActive = false;
      targetSupplier = null;
      isReleasing = false;
      currentOwner = null;
      currentPriority = RotationManager.Priority.LOWEST;
      velocityYaw = 0.0F;
      velocityPitch = 0.0F;
      pitchLag = 0.0F;
      inputBuffer.clear();
      accumulatedYaw = 0.0D;
      accumulatedPitch = 0.0D;
      lastKnownTarget = null;
   }

   public static void update(float tickDelta) {
      if (isActive && mc.field_1724 != null) {
         long currentTime = System.currentTimeMillis();
         double deltaTime = 1.0D;
         if (lastUpdateTime > 0L) {
            double elapsed = (double)(currentTime - lastUpdateTime);
            deltaTime = elapsed / 16.666666666666668D;
            deltaTime = class_3532.method_15350(deltaTime, 0.5D, 3.0D);
         }

         lastUpdateTime = currentTime;
         float mouseYawDelta = class_3532.method_15393(mc.field_1724.method_36454() - lastSetYaw);
         float mousePitchDelta = mc.field_1724.method_36455() - lastSetPitch;
         if (isSilent) {
            visualYaw += mouseYawDelta;
            visualPitch = class_3532.method_15363(visualPitch + mousePitchDelta, -90.0F, 90.0F);
         } else {
            visualYaw = mc.field_1724.method_36454();
            visualPitch = mc.field_1724.method_36455();
         }

         boolean isAiming = targetSupplier != null && !isReleasing;
         if (isAiming && !hasReacted) {
            if (currentTime < nextReactionTime) {
               return;
            }

            hasReacted = true;
         }

         float targetYaw;
         float targetPitch;
         if (isAiming) {
            class_243 targetPos = (class_243)targetSupplier.get();
            if (targetPos == null) {
               isReleasing = true;
               return;
            }

            float[] needed = calculateRotationsToPos(targetPos, finalYaw);
            targetYaw = needed[0];
            targetPitch = needed[1];
            if (randomness > 0.0D && currentTime - lastMicroAdjustment > (long)(80 + random.nextInt(70))) {
               microAdjustmentYaw = (float)(random.nextGaussian() * randomness * 0.15D);
               microAdjustmentPitch = (float)(random.nextGaussian() * randomness * 0.15D);
               lastMicroAdjustment = currentTime;
            }

            targetYaw += microAdjustmentYaw;
            targetPitch += microAdjustmentPitch;
         } else {
            if (!isReleasing) {
               forceStop();
               return;
            }

            targetYaw = visualYaw;
            targetPitch = visualPitch;
            if (Math.abs(class_3532.method_15393(targetYaw - finalYaw)) < 0.5F && Math.abs(targetPitch - finalPitch) < 0.5F) {
               forceStop();
               return;
            }
         }

         applyRotationStep(targetYaw, targetPitch, deltaTime);
         mc.field_1724.method_36456(finalYaw);
         if (!isYawOnly) {
            mc.field_1724.method_36457(finalPitch);
         }

         lastSetYaw = finalYaw;
         lastSetPitch = finalPitch;
      } else {
         if (isActive) {
            forceStop();
         }

      }
   }

   private static void applyRotationStep(float targetYaw, float targetPitch, double deltaTime) {
      float yawDiff = class_3532.method_15393(targetYaw - finalYaw);
      float pitchDiff = targetPitch - finalPitch;
      double absYaw = (double)Math.abs(yawDiff);
      double absPitch = (double)Math.abs(pitchDiff);
      double dist = Math.sqrt(absYaw * absYaw + absPitch * absPitch);
      if (!(dist < 0.05D)) {
         double degreesPerSecond = baseStrength * 60.0D;
         double baseSpeed = degreesPerSecond / 60.0D * deltaTime;
         double yawSpeedVar = 0.97D + random.nextDouble() * 0.06D;
         double pitchSpeedVar = 0.96D + random.nextDouble() * 0.08D;
         double yawSpeed = baseSpeed * yawSpeedVar;
         double pitchSpeed = baseSpeed * pitchSpeedVar * 0.8500000238418579D;
         double yawEasing = calculateEasing(absYaw, dist);
         double pitchEasing = calculateEasing(absPitch, dist);
         if (mode == RotationManager.RotationMode.SINE) {
            double phaseOffset = 0.1D;
            pitchEasing = Math.sin(absPitch / 90.0D * 3.141592653589793D / 2.0D + phaseOffset);
            pitchEasing = Math.max(pitchEasing, 0.2D);
         }

         yawSpeed *= yawEasing;
         pitchSpeed *= pitchEasing;
         yawSpeed = Math.min(yawSpeed, absYaw);
         pitchSpeed = Math.min(pitchSpeed, absPitch);
         float desiredVelYaw = (float)((double)yawDiff / absYaw * yawSpeed);
         float desiredVelPitch = (float)((double)pitchDiff / absPitch * pitchSpeed);
         velocityYaw = class_3532.method_16439(0.22F, velocityYaw, desiredVelYaw);
         velocityPitch = class_3532.method_16439(0.28F, velocityPitch, desiredVelPitch);
         ++yawUpdateCounter;
         ++pitchUpdateCounter;
         float yawMove = velocityYaw;
         float pitchMove = velocityPitch;
         if (yawUpdateCounter % 7 == 0 && absYaw < 3.0D) {
            yawMove *= 0.3F;
         }

         if (pitchUpdateCounter % 5 == 0 && absPitch < 2.0D) {
            pitchMove *= 0.4F;
         }

         float finalYawMove = (float)applyGCD((double)yawMove);
         float finalPitchMove = (float)applyGCD((double)pitchMove);
         finalYaw += finalYawMove;
         finalPitch += finalPitchMove;
         finalPitch = class_3532.method_15363(finalPitch, -90.0F, 90.0F);
      }
   }

   private static double calculateEasing(double axisDist, double totalDist) {
      double easingFactor = 1.0D;
      double normalizedDist;
      if (mode == RotationManager.RotationMode.SMOOTH) {
         normalizedDist = Math.min(axisDist / 90.0D, 1.0D);
         easingFactor = 1.0D - Math.pow(1.0D - normalizedDist, 3.0D);
         easingFactor = Math.max(easingFactor, 0.15D);
      } else if (mode == RotationManager.RotationMode.SINE) {
         normalizedDist = Math.min(axisDist / 90.0D, 1.0D);
         easingFactor = Math.sin(normalizedDist * 3.141592653589793D / 2.0D);
         easingFactor = Math.max(easingFactor, 0.2D);
      } else if (mode == RotationManager.RotationMode.LINEAR) {
         if (axisDist < 5.0D) {
            easingFactor = Math.max(axisDist / 5.0D, 0.3D);
         }
      } else if (mode == RotationManager.RotationMode.INSTANT) {
         easingFactor = 1.0D;
      }

      return easingFactor;
   }

   private static double applyGCD(double deltaRotation) {
      if (Math.abs(deltaRotation) < 0.001D) {
         return 0.0D;
      } else {
         float sensitivity = ((Double)mc.field_1690.method_42495().method_41753()).floatValue();
         float f = sensitivity * 0.6F + 0.2F;
         float gcd = f * f * f * 1.2F;
         long steps = Math.round(deltaRotation / (double)gcd);
         return (double)((float)steps * gcd);
      }
   }

   public static class_243 getCurrentRotation(Object owner) {
      if (isActive && currentOwner == owner && mc.field_1724 != null) {
         class_243 eyePos = mc.field_1724.method_33571();
         float yawRad = (float)Math.toRadians((double)finalYaw);
         float pitchRad = (float)Math.toRadians((double)finalPitch);
         double xDir = -Math.sin((double)yawRad) * Math.cos((double)pitchRad);
         double yDir = -Math.sin((double)pitchRad);
         double zDir = Math.cos((double)yawRad) * Math.cos((double)pitchRad);
         class_243 direction = (new class_243(xDir, yDir, zDir)).method_1029();
         return eyePos.method_1019(direction.method_1021(100.0D));
      } else {
         return null;
      }
   }

   public static float[] calculateRotationsToPos(class_243 targetPos, float currentYaw) {
      if (mc.field_1724 == null) {
         return new float[]{0.0F, 0.0F};
      } else {
         class_243 playerPos = mc.field_1724.method_33571();
         double deltaX = targetPos.field_1352 - playerPos.field_1352;
         double deltaY = targetPos.field_1351 - playerPos.field_1351;
         double deltaZ = targetPos.field_1350 - playerPos.field_1350;
         double dist = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
         float baseYaw = (float)Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F;
         float pitch = (float)(-Math.toDegrees(Math.atan2(deltaY, dist)));
         float yawDiff = baseYaw - currentYaw;
         float unwrappedYaw = currentYaw + class_3532.method_15393(yawDiff);
         return new float[]{unwrappedYaw, class_3532.method_15363(pitch, -90.0F, 90.0F)};
      }
   }

   public static boolean isActive() {
      return isActive;
   }

   public static boolean isSilentRotationActive() {
      return isActive && isSilent;
   }

   public static float getVisualYaw() {
      return isActive ? visualYaw : (mc.field_1724 != null ? mc.field_1724.method_36454() : 0.0F);
   }

   public static float getVisualPitch() {
      return isActive ? visualPitch : (mc.field_1724 != null ? mc.field_1724.method_36455() : 0.0F);
   }

   public static float getFinalYaw() {
      return isActive ? finalYaw : (mc.field_1724 != null ? mc.field_1724.method_36454() : 0.0F);
   }

   public static float getFinalPitch() {
      return isActive ? finalPitch : (mc.field_1724 != null ? mc.field_1724.method_36455() : 0.0F);
   }

   public static boolean isRotationComplete(float threshold) {
      if (isActive && targetSupplier != null && !isReleasing) {
         class_243 targetPos = (class_243)targetSupplier.get();
         if (targetPos == null) {
            return false;
         } else {
            float[] targetRots = calculateRotationsToPos(targetPos, finalYaw);
            float yawDiff = Math.abs(class_3532.method_15393(finalYaw - targetRots[0]));
            float pitchDiff = Math.abs(class_3532.method_15393(finalPitch - targetRots[1]));
            return yawDiff < threshold && (isYawOnly || pitchDiff < threshold);
         }
      } else {
         return false;
      }
   }

   static {
      mode = RotationManager.RotationMode.SMOOTH;
      isReleasing = false;
      isYawOnly = false;
      currentOwner = null;
      currentPriority = RotationManager.Priority.LOWEST;
      lastUpdateTime = -1L;
      velocityYaw = 0.0F;
      velocityPitch = 0.0F;
      pitchLag = 0.0F;
      lastInputTime = 0L;
      inputBuffer = new ArrayList();
      accumulatedYaw = 0.0D;
      accumulatedPitch = 0.0D;
      nextReactionTime = 0L;
      hasReacted = false;
      lastKnownTarget = null;
      lastTargetUpdateTime = 0L;
      lostTarget = false;
      targetLossTime = 0L;
      microAdjustmentYaw = 0.0F;
      microAdjustmentPitch = 0.0F;
      lastMicroAdjustment = 0L;
      yawUpdateCounter = 0;
      pitchUpdateCounter = 0;
      LEGACY_OWNER = new Object();
   }

   @Environment(EnvType.CLIENT)
   public static enum Priority {
      LOWEST,
      LOW,
      NORMAL,
      HIGH,
      HIGHEST;

      // $FF: synthetic method
      private static RotationManager.Priority[] $values() {
         return new RotationManager.Priority[]{LOWEST, LOW, NORMAL, HIGH, HIGHEST};
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum RotationMode {
      INSTANT,
      SMOOTH,
      SINE,
      LINEAR;

      // $FF: synthetic method
      private static RotationManager.RotationMode[] $values() {
         return new RotationManager.RotationMode[]{INSTANT, SMOOTH, SINE, LINEAR};
      }
   }

   @Environment(EnvType.CLIENT)
   private static class MouseInput {
      double yaw;
      double pitch;
      long timestamp;

      MouseInput(double y, double p, long t) {
         this.yaw = y;
         this.pitch = p;
         this.timestamp = t;
      }
   }
}
