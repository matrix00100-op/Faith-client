package com.slither.cyemer.module.implementation.combat;

import com.slither.cyemer.event.EventBus;
import com.slither.cyemer.event.EventTarget;
import com.slither.cyemer.event.impl.TickEvent;
import com.slither.cyemer.friend.FriendManager;
import com.slither.cyemer.mixin.MinecraftClientAccessor;
import com.slither.cyemer.module.BooleanSetting;
import com.slither.cyemer.module.Category;
import com.slither.cyemer.module.ModeSetting;
import com.slither.cyemer.module.Module;
import com.slither.cyemer.module.SliderSetting;
import com.slither.cyemer.util.RotationManager;
import java.util.Iterator;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_243;

@Environment(EnvType.CLIENT)
public class AutoGrapple extends Module {
   private final SliderSetting range = new SliderSetting("Range", 15.0D, 5.0D, 50.0D, 1);
   private final SliderSetting nearDist = new SliderSetting("Near Dist", 7.0D, 0.0D, 20.0D, 1);
   private final SliderSetting minHeight = new SliderSetting("Min Height", 2.0D, 0.0D, 10.0D, 1);
   private final SliderSetting aimSpeed = new SliderSetting("Aim Speed", 150.0D, 1.0D, 180.0D, 1);
   private final ModeSetting aimMode = new ModeSetting("Aim Mode", new String[]{"Instant", "Smooth", "Sine"});
   private final ModeSetting aimTarget = new ModeSetting("Aim Target", new String[]{"Legs", "Chest", "Head"});
   private final ModeSetting trajectoryMode = new ModeSetting("Trajectory Mode", new String[]{"Full", "Horizontal Only"});
   private final SliderSetting hitCooldownTicks = new SliderSetting("Hit Cooldown Ticks", 35.0D, 0.0D, 200.0D, 0);
   private final BooleanSetting onlyOnPearl = new BooleanSetting("Only On Pearl", false);
   private final SliderSetting pearlHoldTicks = new SliderSetting("Pearl Hold Ticks", 4.0D, 0.0D, 20.0D, 0);
   private final BooleanSetting macro = new BooleanSetting("Macro", false);
   private final SliderSetting macroAimTicks = new SliderSetting("Macro Aim Ticks", 3.0D, 0.0D, 20.0D, 0);
   private final BooleanSetting autoThrow = new BooleanSetting("Auto Throw", true);
   private final SliderSetting autoThrowWait = new SliderSetting("Auto Throw Wait", 0.0D, 0.0D, 20.0D, 0);
   private final BooleanSetting swapBack = new BooleanSetting("SwapBack", true);
   private final BooleanSetting ignoreFriends = new BooleanSetting("Ignore Friends", true);
   private static final double PEARL_GRAVITY = 0.03D;
   private static final float PEARL_DRAG = 0.99F;
   private static final float PEARL_VELOCITY = 1.5F;
   private static final int MAX_SIMULATION_TICKS = 300;
   private static final int MAX_TARGET_PREDICTION_TICKS = 120;
   private static final int GRAPPLE_CONFIRM_TICKS = 25;
   private static final float MIN_PROJECTILE_PITCH = -75.0F;
   private static final double HORIZONTAL_LOCK_THRESHOLD = 0.98D;
   private static final double HORIZONTAL_LOCK_THRESHOLD_VOLATILE = 0.992D;
   private static final double LOOK_LOCK_THRESHOLD = 0.85D;
   private static final double LOOK_LOCK_THRESHOLD_VOLATILE = 0.93D;
   private static final float HORIZONTAL_ONLY_PITCH_TOLERANCE = 4.0F;
   private static final float HORIZONTAL_ONLY_PITCH_TOLERANCE_VOLATILE = 2.5F;
   private static final double TARGET_GRAVITY = 0.08D;
   private static final double TARGET_DRAG = 0.98D;
   private static final double MIN_TELEPORT_DISTANCE_SQ = 9.0D;
   private static final double HIT_CONFIRM_RADIUS = 2.0D;
   private static final double VERTICAL_SPEED_ACTIVITY_THRESHOLD = 0.12D;
   private static final double VERTICAL_SPEED_VOLATILE_THRESHOLD = 0.35D;
   private static final int VOLATILITY_SCORE_THRESHOLD = 6;
   private static final int VOLATILITY_SCORE_MAX = 24;
   private static final int VOLATILE_REQUIRED_LOCK_TICKS = 4;
   private static final double VOLATILE_AIM_SPEED_MULTIPLIER = 0.55D;
   private static final double MIN_VOLATILE_AIM_SPEED = 25.0D;
   private static final double EPSILON = 1.0E-4D;
   private int cooldown = 0;
   private int pendingConfirmTicks = 0;
   private UUID pendingTargetUuid = null;
   private class_243 throwOrigin = null;
   private UUID volatilityTargetUuid = null;
   private double previousTargetVerticalVelocity = 0.0D;
   private int verticalVolatilityScore = 0;
   private int aimLockTicks = 0;
   private int pearlMainhandTicksHeld = 0;
   private int autoThrowHoldTicks = 0;
   private int autoThrowRestoreSlot = -1;
   private UUID autoThrowTargetUuid = null;
   private int macroAimTickProgress = 0;
   private UUID macroAimTargetUuid = null;

   public AutoGrapple() {
      super("AutoGrapple", "Automatically pearls to enemies.", Category.COMBAT);
      this.addSetting(this.range);
      this.addSetting(this.nearDist);
      this.addSetting(this.minHeight);
      this.addSetting(this.aimSpeed);
      this.addSetting(this.aimMode);
      this.addSetting(this.aimTarget);
      this.addSetting(this.trajectoryMode);
      this.addSetting(this.hitCooldownTicks);
      this.addSetting(this.onlyOnPearl);
      this.addSetting(this.pearlHoldTicks);
      this.addSetting(this.macro);
      this.addSetting(this.macroAimTicks);
      this.addSetting(this.autoThrow);
      this.addSetting(this.autoThrowWait);
      this.addSetting(this.swapBack);
      this.addSetting(this.ignoreFriends);
      this.aimMode.setCurrentMode("Smooth");
      this.aimTarget.setCurrentMode("Chest");
      this.trajectoryMode.setCurrentMode("Full");
   }

   public void onEnable() {
      EventBus.register(this);
      this.cooldown = 0;
      this.clearPendingGrapple();
      this.resetVolatilityTracking();
      this.pearlMainhandTicksHeld = 0;
      this.resetAutoThrowPrep();
      this.resetMacroAimProgress();
   }

   public void onDisable() {
      EventBus.unregister(this);
      RotationManager.stop(this);
      this.cooldown = 0;
      this.clearPendingGrapple();
      this.resetVolatilityTracking();
      this.pearlMainhandTicksHeld = 0;
      this.resetAutoThrowPrep();
      this.resetMacroAimProgress();
   }

   @EventTarget
   public void onTick(TickEvent event) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         if (this.cooldown > 0) {
            --this.cooldown;
         }

         this.updatePendingHitConfirmation();
         if (this.mc.field_1755 != null) {
            RotationManager.stop(this);
            this.updateAimLockCounter(false);
            this.resetAutoThrowPrep();
            this.finishMacroRun();
         } else if (!this.passesOnlyOnPearlGate()) {
            RotationManager.stop(this);
            this.updateAimLockCounter(false);
            this.resetAutoThrowPrep();
            this.finishMacroRun();
         } else {
            class_1297 target = this.findTarget();
            boolean shouldGrapple = target != null && this.checkGrapple(target);
            this.updateVerticalVolatility(shouldGrapple ? target : null);
            if (!shouldGrapple) {
               RotationManager.stop(this);
               this.finishMacroRun();
            } else {
               this.performAiming(target);
               if (shouldGrapple && this.cooldown == 0) {
                  boolean horizontalAligned = this.isHorizontallyAligned(target, this.getHorizontalLockThreshold());
                  boolean fullyAligned;
                  if (this.isHorizontalOnlyMode()) {
                     fullyAligned = horizontalAligned && this.isVerticalThrowAligned(target, this.getVerticalThrowPitchTolerance());
                  } else {
                     fullyAligned = horizontalAligned && this.isLookingAtTarget(target, this.getLookLockThreshold());
                  }

                  this.updateAimLockCounter(fullyAligned);
                  if (!this.autoThrow.isEnabled()) {
                     this.resetAutoThrowPrep();
                     this.finishMacroRun();
                  } else if (this.macro.isEnabled() && this.findItemSlot(class_1802.field_8634) == -1) {
                     this.resetAutoThrowPrep();
                     this.finishMacroRun();
                  } else {
                     if (this.macro.isEnabled()) {
                        this.updateMacroAimProgress(target);
                        int requiredTicks = Math.max(0, (int)Math.round(this.macroAimTicks.getValue()));
                        if (this.macroAimTickProgress < requiredTicks) {
                           this.resetAutoThrowPrep();
                           return;
                        }
                     } else {
                        this.resetMacroAimProgress();
                     }

                     if (this.aimLockTicks >= this.getRequiredLockTicks()) {
                        boolean thrown = this.tryAutoThrow(target);
                        if (thrown) {
                           this.finishMacroRun();
                        }
                     } else {
                        this.resetAutoThrowPrep();
                     }

                  }
               } else {
                  this.updateAimLockCounter(false);
                  this.resetAutoThrowPrep();
                  this.finishMacroRun();
               }
            }
         }
      } else {
         this.finishMacroRun();
      }
   }

   private class_1297 findTarget() {
      class_1297 bestEntity = null;
      double bestDistance = this.range.getValue();
      Iterator var4 = this.mc.field_1687.method_18112().iterator();

      while(var4.hasNext()) {
         class_1297 entity = (class_1297)var4.next();
         if (this.isValidTarget(entity)) {
            double distance = (double)this.mc.field_1724.method_5739(entity);
            if (!(distance > this.range.getValue()) && distance < bestDistance) {
               bestDistance = distance;
               bestEntity = entity;
            }
         }
      }

      return bestEntity;
   }

   private boolean isValidTarget(class_1297 entity) {
      if (entity instanceof class_1309 && entity != this.mc.field_1724 && entity.method_5805()) {
         if (this.ignoreFriends.isEnabled() && entity instanceof class_1657) {
            class_1657 player = (class_1657)entity;
            return !FriendManager.getInstance().isFriend(player.method_5667());
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   private boolean checkGrapple(class_1297 target) {
      if (!this.mc.field_1724.method_24828()) {
         return false;
      } else if (this.hasElytra(target)) {
         return false;
      } else {
         double dist = (double)this.mc.field_1724.method_5739(target);
         if (dist < 5.0D) {
            return false;
         } else {
            double enemyHeightFromGround = this.getGroundDistance(target);
            return enemyHeightFromGround >= this.minHeight.getValue();
         }
      }
   }

   private void performAiming(class_1297 target) {
      boolean horizontalLocked = this.isHorizontallyAligned(target, this.getHorizontalLockThreshold());
      class_243 targetPoint;
      boolean yawOnly;
      if (horizontalLocked && !this.isHorizontalOnlyMode()) {
         if (this.isNearDistance(target)) {
            targetPoint = this.getDirectAimTargetPoint(target);
            yawOnly = false;
         } else {
            float solvedPitch = this.solvePitchForTarget(target);
            targetPoint = this.getAimPointForPitch(target, solvedPitch);
            yawOnly = false;
         }
      } else {
         targetPoint = this.getHorizontalAimPoint(target);
         yawOnly = true;
      }

      RotationManager.setRotationSupplier(this, RotationManager.Priority.HIGH, () -> {
         return targetPoint;
      }, this.getEffectiveAimSpeed(), this.getAimRotationMode(), 0.0D, false, yawOnly);
   }

   private class_243 getHorizontalAimPoint(class_1297 target) {
      class_243 eyePos = this.mc.field_1724.method_33571();
      class_243 predictedPos = this.predictTargetPosition(target, this.estimateInitialTargetTicks(target));
      return new class_243(predictedPos.field_1352, eyePos.field_1351, predictedPos.field_1350);
   }

   private class_243 getDirectAimTargetPoint(class_1297 target) {
      class_243 center = target.method_5829().method_1005();
      double aimY = target.method_23318() + (double)target.method_17682() * this.getAimTargetHeightMultiplier();
      return new class_243(center.field_1352, aimY, center.field_1350);
   }

   private class_243 getAimPointForPitch(class_1297 target, float pitch) {
      class_243 eyePos = this.mc.field_1724.method_33571();
      class_243 predictedPos = this.predictTargetForPitch(target, pitch);
      double dx = predictedPos.field_1352 - eyePos.field_1352;
      double dz = predictedPos.field_1350 - eyePos.field_1350;
      double horizontalDist = Math.sqrt(dx * dx + dz * dz);
      double yOffset = Math.tan(Math.toRadians((double)(-pitch))) * horizontalDist;
      class_243 aimPoint = new class_243(predictedPos.field_1352, eyePos.field_1351 + yOffset, predictedPos.field_1350);
      class_243 direction = aimPoint.method_1020(eyePos);
      return direction.method_1027() < 1.0E-4D ? predictedPos : eyePos.method_1019(direction.method_1029().method_1021(100.0D));
   }

   private float solvePitchForTarget(class_1297 target) {
      class_243 eyePos = this.mc.field_1724.method_33571();
      class_243 predictedPos = this.predictTargetPosition(target, this.estimateInitialTargetTicks(target));
      float minecraftPitch = this.mc.field_1724.method_36455();

      for(int i = 0; i < 3; ++i) {
         double targetY = predictedPos.field_1351 + (double)target.method_17682() * this.getAimTargetHeightMultiplier();
         double dx = predictedPos.field_1352 - eyePos.field_1352;
         double dz = predictedPos.field_1350 - eyePos.field_1350;
         double dy = targetY - eyePos.field_1351;
         double horizontalDist = Math.sqrt(dx * dx + dz * dz);
         if (horizontalDist < 1.0E-4D) {
            break;
         }

         float directPitch = (float)Math.toDegrees(Math.atan2(dy, horizontalDist));
         float maxProjectilePitch = this.getMaxProjectilePitch(horizontalDist, dy);
         float projectilePitch = this.binSearch(1.5F, horizontalDist, dy, directPitch, -75.0F, maxProjectilePitch);
         minecraftPitch = -projectilePitch;
         int flightTicks = this.simulateFlightTicks(1.5F, projectilePitch, horizontalDist);
         predictedPos = this.predictTargetPosition(target, flightTicks);
      }

      return minecraftPitch;
   }

   private float binSearch(float velocity, double horizontalDist, double targetYOffset, float directPitch, float minPitchBound, float maxPitchBound) {
      float minPitch = minPitchBound;
      float maxPitch = maxPitchBound;
      float bestPitch = this.clampFloat(directPitch, minPitchBound, maxPitchBound);
      double minError = Double.MAX_VALUE;

      for(int i = 0; i < 50; ++i) {
         float midPitch = (minPitch + maxPitch) / 2.0F;
         double hitY = this.simulateProjectile(velocity, midPitch, horizontalDist);
         if (Double.isInfinite(hitY)) {
            maxPitch = midPitch;
         } else {
            double error = Math.abs(hitY - targetYOffset);
            if (error < minError) {
               minError = error;
               bestPitch = midPitch;
            }

            if (error < 0.01D) {
               break;
            }

            if (hitY < targetYOffset) {
               minPitch = midPitch;
            } else {
               maxPitch = midPitch;
            }
         }
      }

      return bestPitch;
   }

   private float getMaxProjectilePitch(double horizontalDist, double targetYOffset) {
      double base = 18.0D + horizontalDist * 2.1D;
      if (targetYOffset > 1.5D) {
         base += Math.min(10.0D, targetYOffset * 1.8D);
      }

      return this.clampFloat((float)base, 24.0F, 80.0F);
   }

   private float clampFloat(float value, float min, float max) {
      return Math.max(min, Math.min(max, value));
   }

   private double simulateProjectile(float speed, float pitch, double targetDist) {
      double radPitch = Math.toRadians((double)pitch);
      double vx = (double)speed * Math.cos(radPitch);
      double vy = (double)speed * Math.sin(radPitch);
      double x = 0.0D;
      double y = 0.0D;

      for(int tick = 0; tick < 300; ++tick) {
         double prevX = x;
         double prevY = y;
         x += vx;
         y += vy;
         if (x >= targetDist) {
            double delta = x - prevX;
            if (Math.abs(delta) < 1.0E-4D) {
               return y;
            }

            double ratio = (targetDist - prevX) / delta;
            return prevY + (y - prevY) * ratio;
         }

         vx *= 0.9900000095367432D;
         vy *= 0.9900000095367432D;
         vy -= 0.03D;
      }

      return Double.NEGATIVE_INFINITY;
   }

   private int simulateFlightTicks(float speed, float pitch, double targetDist) {
      if (targetDist <= 1.0E-4D) {
         return 1;
      } else {
         double radPitch = Math.toRadians((double)pitch);
         double vx = (double)speed * Math.cos(radPitch);
         double vy = (double)speed * Math.sin(radPitch);
         double x = 0.0D;

         for(int tick = 1; tick <= 300; ++tick) {
            x += vx;
            if (x >= targetDist) {
               return Math.min(tick, 120);
            }

            vx *= 0.9900000095367432D;
            vy *= 0.9900000095367432D;
            vy -= 0.03D;
         }

         return 120;
      }
   }

   private int estimateInitialTargetTicks(class_1297 target) {
      double dist = (double)this.mc.field_1724.method_5739(target);
      return Math.max(1, (int)Math.ceil(dist / 1.5D));
   }

   private class_243 predictTargetForPitch(class_1297 target, float minecraftPitch) {
      class_243 eyePos = this.mc.field_1724.method_33571();
      class_243 predictedPos = this.predictTargetPosition(target, this.estimateInitialTargetTicks(target));
      float projectilePitch = -minecraftPitch;

      for(int i = 0; i < 2; ++i) {
         double dx = predictedPos.field_1352 - eyePos.field_1352;
         double dz = predictedPos.field_1350 - eyePos.field_1350;
         double horizontalDist = Math.sqrt(dx * dx + dz * dz);
         int flightTicks = this.simulateFlightTicks(1.5F, projectilePitch, horizontalDist);
         predictedPos = this.predictTargetPosition(target, flightTicks);
      }

      return predictedPos;
   }

   private class_243 predictTargetPosition(class_1297 target, int ticks) {
      int clampedTicks = Math.max(0, Math.min(ticks, 120));
      class_243 pos = new class_243(target.method_23317(), target.method_23318(), target.method_23321());
      class_243 vel = target.method_18798();
      boolean noGravity = target.method_5740();
      boolean grounded = target.method_24828() && Math.abs(vel.field_1351) < 0.01D;

      for(int i = 0; i < clampedTicks; ++i) {
         pos = pos.method_1019(vel);
         double nextVy = vel.field_1351;
         if (!grounded && !noGravity) {
            nextVy -= 0.08D;
         }

         vel = new class_243(vel.field_1352 * 0.98D, nextVy * 0.98D, vel.field_1350 * 0.98D);
      }

      return pos;
   }

   private void startPendingGrapple(class_1297 target) {
      if (target != null) {
         this.pendingTargetUuid = target.method_5667();
         this.pendingConfirmTicks = 25;
         this.throwOrigin = this.mc.field_1724.method_5829().method_1005();
      }
   }

   private void clearPendingGrapple() {
      this.pendingTargetUuid = null;
      this.pendingConfirmTicks = 0;
      this.throwOrigin = null;
   }

   private void resetAutoThrowPrep() {
      this.autoThrowHoldTicks = 0;
      this.autoThrowRestoreSlot = -1;
      this.autoThrowTargetUuid = null;
   }

   private void updatePendingHitConfirmation() {
      if (this.pendingTargetUuid != null && this.throwOrigin != null && this.mc.field_1687 != null && this.mc.field_1724 != null) {
         if (this.pendingConfirmTicks-- <= 0) {
            this.clearPendingGrapple();
         } else {
            class_1297 target = this.findEntityByUuid(this.pendingTargetUuid);
            if (target != null && target.method_5805()) {
               double movedSq = this.mc.field_1724.method_5649(this.throwOrigin.field_1352, this.throwOrigin.field_1351, this.throwOrigin.field_1350);
               if (!(movedSq < 9.0D)) {
                  class_243 playerCenter = this.mc.field_1724.method_5829().method_1005();
                  class_243 targetCenter = target.method_5829().method_1005();
                  double radiusSq = 4.0D;
                  if (playerCenter.method_1025(targetCenter) <= radiusSq) {
                     int hitCd = Math.max(0, (int)Math.round(this.hitCooldownTicks.getValue()));
                     this.cooldown = Math.max(this.cooldown, hitCd);
                  }

                  this.clearPendingGrapple();
               }
            } else {
               this.clearPendingGrapple();
            }
         }
      }
   }

   private void updateVerticalVolatility(class_1297 target) {
      if (target == null) {
         this.resetVolatilityTracking();
      } else {
         UUID targetUuid = target.method_5667();
         double currentVy = target.method_18798().field_1351;
         if (!targetUuid.equals(this.volatilityTargetUuid)) {
            this.volatilityTargetUuid = targetUuid;
            this.previousTargetVerticalVelocity = currentVy;
            this.verticalVolatilityScore = 0;
            this.aimLockTicks = 0;
         } else {
            this.verticalVolatilityScore = Math.max(0, this.verticalVolatilityScore - 1);
            boolean previousActive = Math.abs(this.previousTargetVerticalVelocity) > 0.12D;
            boolean currentActive = Math.abs(currentVy) > 0.12D;
            boolean signFlipped = this.previousTargetVerticalVelocity * currentVy < 0.0D;
            if (previousActive && currentActive && signFlipped) {
               this.verticalVolatilityScore = Math.min(24, this.verticalVolatilityScore + 4);
            }

            if (Math.abs(currentVy) > 0.35D) {
               this.verticalVolatilityScore = Math.min(24, this.verticalVolatilityScore + 2);
            }

            this.previousTargetVerticalVelocity = currentVy;
         }
      }
   }

   private void resetVolatilityTracking() {
      this.volatilityTargetUuid = null;
      this.previousTargetVerticalVelocity = 0.0D;
      this.verticalVolatilityScore = 0;
      this.aimLockTicks = 0;
   }

   private void updateAimLockCounter(boolean locked) {
      if (locked) {
         this.aimLockTicks = Math.min(this.aimLockTicks + 1, 40);
      } else {
         this.aimLockTicks = 0;
      }

   }

   private boolean isTargetVerticallyVolatile() {
      return this.verticalVolatilityScore >= 6;
   }

   private double getHorizontalLockThreshold() {
      return this.isTargetVerticallyVolatile() ? 0.992D : 0.98D;
   }

   private double getLookLockThreshold() {
      return this.isTargetVerticallyVolatile() ? 0.93D : 0.85D;
   }

   private int getRequiredLockTicks() {
      return this.isTargetVerticallyVolatile() ? 4 : 1;
   }

   private float getVerticalThrowPitchTolerance() {
      return this.isTargetVerticallyVolatile() ? 2.5F : 4.0F;
   }

   private double getEffectiveAimSpeed() {
      return !this.isTargetVerticallyVolatile() ? this.aimSpeed.getValue() : Math.max(25.0D, this.aimSpeed.getValue() * 0.55D);
   }

   private RotationManager.RotationMode getAimRotationMode() {
      String mode = this.aimMode.getCurrentMode();
      if ("Instant".equals(mode)) {
         return RotationManager.RotationMode.INSTANT;
      } else {
         return "Sine".equals(mode) ? RotationManager.RotationMode.SINE : RotationManager.RotationMode.SMOOTH;
      }
   }

   private double getAimTargetHeightMultiplier() {
      String mode = this.aimTarget.getCurrentMode();
      if ("Legs".equals(mode)) {
         return 0.08D;
      } else {
         return "Head".equals(mode) ? 0.74D : 0.4D;
      }
   }

   private boolean isHorizontalOnlyMode() {
      return "Horizontal Only".equals(this.trajectoryMode.getCurrentMode());
   }

   private boolean isNearDistance(class_1297 target) {
      if (target != null && this.mc.field_1724 != null) {
         if (this.isHorizontalOnlyMode()) {
            return false;
         } else {
            return (double)this.mc.field_1724.method_5739(target) <= this.nearDist.getValue();
         }
      } else {
         return false;
      }
   }

   private boolean passesOnlyOnPearlGate() {
      if (!this.onlyOnPearl.isEnabled()) {
         return true;
      } else {
         if (this.mc.field_1724.method_6047().method_7909() == class_1802.field_8634) {
            ++this.pearlMainhandTicksHeld;
         } else {
            this.pearlMainhandTicksHeld = 0;
         }

         return this.pearlMainhandTicksHeld >= Math.max(0, (int)Math.round(this.pearlHoldTicks.getValue()));
      }
   }

   private class_1297 findEntityByUuid(UUID uuid) {
      if (this.mc.field_1687 != null && uuid != null) {
         Iterator var2 = this.mc.field_1687.method_18112().iterator();

         class_1297 entity;
         do {
            if (!var2.hasNext()) {
               return null;
            }

            entity = (class_1297)var2.next();
         } while(!uuid.equals(entity.method_5667()));

         return entity;
      } else {
         return null;
      }
   }

   private boolean tryAutoThrow(class_1297 target) {
      if (target != null && this.mc.field_1724 != null) {
         int waitTicks = Math.max(0, (int)Math.round(this.autoThrowWait.getValue()));
         int requiredHoldTicks = Math.max(1, waitTicks);
         UUID targetUuid = target.method_5667();
         if (!targetUuid.equals(this.autoThrowTargetUuid)) {
            this.autoThrowTargetUuid = targetUuid;
            this.autoThrowHoldTicks = 0;
            this.autoThrowRestoreSlot = -1;
         }

         int pearlSlot = this.findItemSlot(class_1802.field_8634);
         if (pearlSlot == -1) {
            this.resetAutoThrowPrep();
            return false;
         } else {
            int selectedSlot = this.mc.field_1724.method_31548().method_67532();
            if (selectedSlot != pearlSlot) {
               if (this.autoThrowRestoreSlot == -1) {
                  this.autoThrowRestoreSlot = selectedSlot;
               }

               this.mc.field_1724.method_31548().method_61496(pearlSlot);
               this.autoThrowHoldTicks = 0;
               return false;
            } else {
               ++this.autoThrowHoldTicks;
               if (this.autoThrowHoldTicks < requiredHoldTicks) {
                  return false;
               } else {
                  MinecraftClientAccessor accessor = (MinecraftClientAccessor)this.mc;
                  int beforeCooldown = accessor.getItemUseCooldown();
                  int beforeCount = this.mc.field_1724.method_6047().method_7947();
                  this.pressUseAndThrow(accessor);
                  int afterCooldown = accessor.getItemUseCooldown();
                  int afterCount = this.mc.field_1724.method_6047().method_7947();
                  boolean used = afterCooldown > beforeCooldown || afterCount < beforeCount;
                  if (!used) {
                     return false;
                  } else {
                     this.startPendingGrapple(target);
                     this.cooldown = 10;
                     if (this.swapBack.isEnabled() && this.autoThrowRestoreSlot >= 0 && this.autoThrowRestoreSlot < 9) {
                        this.mc.field_1724.method_31548().method_61496(this.autoThrowRestoreSlot);
                     }

                     this.resetAutoThrowPrep();
                     return true;
                  }
               }
            }
         }
      } else {
         return false;
      }
   }

   private void pressUseAndThrow(MinecraftClientAccessor accessor) {
      this.mc.field_1690.field_1904.method_23481(true);

      try {
         accessor.useItem();
      } finally {
         this.mc.field_1690.field_1904.method_23481(false);
      }

   }

   private int findItemSlot(class_1792 item) {
      for(int i = 0; i < 9; ++i) {
         if (this.mc.field_1724.method_31548().method_5438(i).method_7909() == item) {
            return i;
         }
      }

      return -1;
   }

   private boolean isLookingAtTarget(class_1297 target, double threshold) {
      if (target == null) {
         return false;
      } else {
         class_243 lookVec = this.mc.field_1724.method_5828(1.0F).method_1029();
         class_243 center = target.method_5829().method_1005();
         double aimY = target.method_23318() + (double)target.method_17682() * this.getAimTargetHeightMultiplier();
         class_243 tPos = new class_243(center.field_1352, aimY, center.field_1350);
         class_243 toTarget = tPos.method_1020(this.mc.field_1724.method_33571()).method_1029();
         return lookVec.method_1026(toTarget) > threshold;
      }
   }

   private boolean isHorizontallyAligned(class_1297 target, double threshold) {
      if (target != null && this.mc.field_1724 != null) {
         class_243 eyePos = this.mc.field_1724.method_33571();
         class_243 predictedPos = this.predictTargetPosition(target, this.estimateInitialTargetTicks(target));
         class_243 look = this.mc.field_1724.method_5828(1.0F);
         class_243 lookHorizontal = new class_243(look.field_1352, 0.0D, look.field_1350);
         if (lookHorizontal.method_1027() < 1.0E-4D) {
            return false;
         } else {
            class_243 toPredicted = predictedPos.method_1020(eyePos);
            class_243 toPredictedHorizontal = new class_243(toPredicted.field_1352, 0.0D, toPredicted.field_1350);
            if (toPredictedHorizontal.method_1027() < 1.0E-4D) {
               return true;
            } else {
               return lookHorizontal.method_1029().method_1026(toPredictedHorizontal.method_1029()) >= threshold;
            }
         }
      } else {
         return false;
      }
   }

   private boolean isVerticalThrowAligned(class_1297 target, float maxPitchError) {
      if (target != null && this.mc.field_1724 != null) {
         float solvedPitch = this.solvePitchForTarget(target);
         float currentPitch = this.mc.field_1724.method_36455();
         return Math.abs(solvedPitch - currentPitch) <= maxPitchError;
      } else {
         return false;
      }
   }

   private boolean hasElytra(class_1297 entity) {
      if (entity instanceof class_1309) {
         class_1309 living = (class_1309)entity;
         return living.method_6118(class_1304.field_6174).method_7909() == class_1802.field_8833;
      } else {
         return false;
      }
   }

   private double getGroundDistance(class_1297 entity) {
      if (this.mc.field_1687 == null) {
         return 0.0D;
      } else {
         int y = (int)Math.floor(entity.method_23318());
         int minY = this.mc.field_1687.method_31607();

         for(int i = y; i >= minY; --i) {
            if (!this.mc.field_1687.method_8320(new class_2338((int)entity.method_23317(), i, (int)entity.method_23321())).method_26215()) {
               return entity.method_23318() - (double)i - 1.0D;
            }
         }

         return 0.0D;
      }
   }

   private boolean finishMacroRun() {
      if (this.macro.isEnabled() && this.isEnabled()) {
         this.cooldown = 0;
         this.resetAutoThrowPrep();
         this.clearPendingGrapple();
         this.updateAimLockCounter(false);
         this.resetMacroAimProgress();
         this.toggle();
         return true;
      } else {
         return false;
      }
   }

   private void updateMacroAimProgress(class_1297 target) {
      if (target == null) {
         this.resetMacroAimProgress();
      } else {
         UUID targetUuid = target.method_5667();
         if (!targetUuid.equals(this.macroAimTargetUuid)) {
            this.macroAimTargetUuid = targetUuid;
            this.macroAimTickProgress = 0;
         }

         this.macroAimTickProgress = Math.min(this.macroAimTickProgress + 1, 100);
      }
   }

   private void resetMacroAimProgress() {
      this.macroAimTickProgress = 0;
      this.macroAimTargetUuid = null;
   }
}
