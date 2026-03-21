package com.slither.cyemer.module.implementation;

import com.slither.cyemer.module.BooleanSetting;
import com.slither.cyemer.module.Category;
import com.slither.cyemer.module.ModeSetting;
import com.slither.cyemer.module.Module;
import com.slither.cyemer.module.SliderSetting;
import com.slither.cyemer.util.RotationManager;
import com.slither.cyemer.util.render.RenderUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4597.class_4598;

@Environment(EnvType.CLIENT)
public class PearlCatch extends Module {
   private final SliderSetting rotationStrength = new SliderSetting("Rotation Speed", 15.0D, 1.0D, 20.0D, 1);
   private final ModeSetting rotPattern = new ModeSetting("Pattern", new String[]{"Sine", "Smooth", "Linear", "Instant"});
   private final SliderSetting rotRandom = new SliderSetting("Randomness", 0.0D, 0.0D, 1.0D, 2);
   private final BooleanSetting silentRotation = new BooleanSetting("Silent Aim", true);
   private final SliderSetting predictionTicks = new SliderSetting("Max Sim Ticks", 30.0D, 20.0D, 50.0D, 0);
   private final BooleanSetting showTrajectory = new BooleanSetting("Show Trajectory", true);
   private static final double PEARL_GRAVITY = 0.03D;
   private static final double PEARL_DRAG = 0.99D;
   private static final double PEARL_INITIAL_SPEED = 1.5D;
   private static final double WIND_CHARGE_SPEED = 1.5D;
   private static final double WIND_CHARGE_DRAG = 1.0D;
   private static final double WIND_CHARGE_GRAVITY = 0.0D;
   private static final double WIND_CHARGE_ACCELERATION = 0.0D;
   private static final int THROW_DELAY_TICKS = 2;
   private PearlCatch.State currentState;
   private int originalSlot;
   private int pearlThrowTick;
   private int executionTimer;
   private class_243 pearlStartPos;
   private class_243 pearlStartVel;
   private class_243 playerVelAtThrow;
   private List<class_243> predictedPearlPath;
   private class_243 renderIntercept;
   private class_243 renderAim;

   public PearlCatch() {
      super("PearlCatch", "BETA ASF.", Category.MOVEMENT);
      this.currentState = PearlCatch.State.IDLE;
      this.originalSlot = -1;
      this.pearlThrowTick = 0;
      this.executionTimer = 0;
      this.pearlStartPos = class_243.field_1353;
      this.pearlStartVel = class_243.field_1353;
      this.playerVelAtThrow = class_243.field_1353;
      this.predictedPearlPath = new ArrayList();
      this.renderIntercept = null;
      this.renderAim = null;
      this.addSetting(this.rotationStrength);
      this.addSetting(this.rotPattern);
      this.addSetting(this.rotRandom);
      this.addSetting(this.silentRotation);
      this.addSetting(this.predictionTicks);
      this.addSetting(this.showTrajectory);
   }

   public void onEnable() {
      if (this.mc.field_1724 == null) {
         this.toggle();
      } else if (this.hasItem(class_1802.field_8634) && this.hasItem(class_1802.field_49098)) {
         this.currentState = PearlCatch.State.THROWING_PEARL;
         this.originalSlot = this.mc.field_1724.method_31548().method_67532();
         this.predictedPearlPath.clear();
         this.renderIntercept = null;
         this.renderAim = null;
         this.executionTimer = 0;
      } else {
         this.toggle();
      }
   }

   public void onDisable() {
      if (this.mc.field_1724 != null && this.originalSlot != -1) {
         this.mc.field_1724.method_31548().method_61496(this.originalSlot);
      }

      if (this.mc.field_1690 != null) {
         this.mc.field_1690.field_1904.method_23481(false);
      }

      RotationManager.stop(this);
      this.currentState = PearlCatch.State.IDLE;
      this.renderIntercept = null;
      this.renderAim = null;
   }

   public void onTick() {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         switch(this.currentState.ordinal()) {
         case 1:
            this.handlePearlThrow();
            break;
         case 2:
            this.handleNormalThrow();
            break;
         case 3:
            this.handleTracking();
            break;
         case 4:
            this.handleExecution();
            break;
         case 5:
            this.handleReset();
         }

      }
   }

   private void handlePearlThrow() {
      int pearlSlot = this.findItemSlot(class_1802.field_8634);
      if (pearlSlot == -1) {
         this.toggle();
      } else {
         this.mc.field_1724.method_31548().method_61496(pearlSlot);
         this.mc.field_1690.field_1904.method_23481(true);
         this.pearlStartPos = this.mc.field_1724.method_33571();
         this.playerVelAtThrow = this.mc.field_1724.method_18798();
         class_243 throwDirection = this.mc.field_1724.method_5828(1.0F);
         class_243 inheritedVelocity = new class_243(this.playerVelAtThrow.field_1352, this.mc.field_1724.method_24828() ? 0.0D : this.playerVelAtThrow.field_1351, this.playerVelAtThrow.field_1350);
         this.pearlStartVel = throwDirection.method_1021(1.5D).method_1019(inheritedVelocity);
         this.simulateProjectile(this.pearlStartPos, this.pearlStartVel, 0.99D, 0.03D, 0.0D, this.predictedPearlPath);
         if (!this.predictedPearlPath.isEmpty() && this.isInterceptPossible()) {
            this.currentState = PearlCatch.State.TRACKING;
            this.pearlThrowTick = this.mc.field_1724.field_6012;
         } else {
            this.currentState = PearlCatch.State.NORMAL_THROW;
            this.executionTimer = 0;
         }
      }
   }

   private void handleNormalThrow() {
      this.mc.field_1690.field_1904.method_23481(true);
      ++this.executionTimer;
      if (this.executionTimer > 1) {
         this.mc.field_1690.field_1904.method_23481(false);
         this.mc.field_1724.method_31548().method_61496(this.originalSlot);
         this.toggle();
      }

   }

   private void handleTracking() {
      this.mc.field_1690.field_1904.method_23481(false);
      int ticksSinceThrow = this.mc.field_1724.field_6012 - this.pearlThrowTick;
      if (ticksSinceThrow >= this.predictedPearlPath.size() - 5) {
         RotationManager.clearTarget(this);
         this.currentState = PearlCatch.State.RESETTING;
      } else if (ticksSinceThrow >= 1) {
         if (ticksSinceThrow > 60) {
            RotationManager.clearTarget(this);
            this.currentState = PearlCatch.State.RESETTING;
         } else {
            class_243 predictedPos = this.mc.field_1724.method_73189().method_1019(this.mc.field_1724.method_18798());
            class_243 predictedEyePos = new class_243(predictedPos.field_1352, predictedPos.field_1351 + (double)this.mc.field_1724.method_5751(), predictedPos.field_1350);
            class_243 predictedVel = this.mc.field_1724.method_18798();
            PearlCatch.SolverResult result = this.solveInterceptWithPrediction(ticksSinceThrow, predictedEyePos, predictedVel);
            if (result != null) {
               this.renderIntercept = result.interceptPos;
               this.renderAim = result.aimDirection;
               RotationManager.setRotationSupplier(this, RotationManager.Priority.HIGHEST, () -> {
                  int currentTicks = this.mc.field_1724.field_6012 - this.pearlThrowTick;
                  class_243 nextPos = this.mc.field_1724.method_73189().method_1019(this.mc.field_1724.method_18798());
                  class_243 nextEyePos = new class_243(nextPos.field_1352, nextPos.field_1351 + (double)this.mc.field_1724.method_5751(), nextPos.field_1350);
                  class_243 nextVel = this.mc.field_1724.method_18798();
                  PearlCatch.SolverResult freshResult = this.solveInterceptWithPrediction(currentTicks, nextEyePos, nextVel);
                  return freshResult != null ? freshResult.aimDirection : result.aimDirection;
               }, this.rotationStrength.getValue(), this.getRotationMode(), this.rotRandom.getValue(), this.silentRotation.isEnabled(), false);
               if (RotationManager.isRotationComplete(1.5F)) {
                  int windSlot = this.findItemSlot(class_1802.field_49098);
                  if (windSlot != -1) {
                     this.mc.field_1724.method_31548().method_61496(windSlot);
                     this.currentState = PearlCatch.State.EXECUTING_CATCH;
                     this.executionTimer = 0;
                  }
               }
            } else {
               this.renderIntercept = null;
               RotationManager.clearTarget(this);
            }

         }
      }
   }

   private void handleExecution() {
      int windSlot = this.findItemSlot(class_1802.field_49098);
      if (windSlot == -1) {
         RotationManager.clearTarget(this);
         this.currentState = PearlCatch.State.RESETTING;
      } else {
         this.mc.field_1724.method_31548().method_61496(windSlot);
         this.mc.field_1690.field_1904.method_23481(true);
         ++this.executionTimer;
         if (this.executionTimer > 3) {
            RotationManager.clearTarget(this);
            this.currentState = PearlCatch.State.RESETTING;
         }

      }
   }

   private void handleReset() {
      this.mc.field_1690.field_1904.method_23481(false);
      if (RotationManager.isActive() && this.executionTimer <= 10) {
         ++this.executionTimer;
      } else {
         RotationManager.stop(this);
         this.mc.field_1724.method_31548().method_61496(this.originalSlot);
         this.toggle();
      }

   }

   private RotationManager.RotationMode getRotationMode() {
      try {
         return RotationManager.RotationMode.valueOf(this.rotPattern.getCurrentMode().toUpperCase());
      } catch (Exception var2) {
         return RotationManager.RotationMode.SINE;
      }
   }

   private void simulateProjectile(class_243 startPos, class_243 startVel, double drag, double gravity, double acceleration, List<class_243> output) {
      output.clear();
      class_243 pos = startPos;
      class_243 vel = startVel;
      int maxTicks = (int)this.predictionTicks.getPreciseValue();

      for(int i = 0; i < maxTicks; ++i) {
         output.add(pos);
         vel = vel.method_1023(0.0D, gravity, 0.0D);
         if (acceleration != 0.0D) {
            vel = vel.method_1019(vel.method_1029().method_1021(acceleration));
         }

         vel = vel.method_1021(drag);
         pos = pos.method_1019(vel);
         if (pos.field_1351 < -64.0D) {
            break;
         }
      }

   }

   private boolean isInterceptPossible() {
      class_243 windStartPos = this.mc.field_1724.method_33571();
      class_243 currentPlayerVel = this.mc.field_1724.method_18798();
      class_243 inheritedVelocity = new class_243(currentPlayerVel.field_1352, this.mc.field_1724.method_24828() ? 0.0D : currentPlayerVel.field_1351, currentPlayerVel.field_1350);

      for(int pearlTick = 5; pearlTick < Math.min(this.predictedPearlPath.size(), 50); ++pearlTick) {
         class_243 pearlPos = (class_243)this.predictedPearlPath.get(pearlTick);
         class_243 aimDir = pearlPos.method_1020(windStartPos).method_1029();
         List<class_243> windPath = new ArrayList();
         class_243 windVel = aimDir.method_1021(1.5D).method_1019(inheritedVelocity);
         this.simulateProjectile(windStartPos, windVel, 1.0D, 0.0D, 0.0D, windPath);

         for(int w = 0; w < Math.min(windPath.size(), pearlTick - 2); ++w) {
            int pearlIdx = w + 2;
            if (pearlIdx >= this.predictedPearlPath.size()) {
               break;
            }

            if (((class_243)windPath.get(w)).method_1022((class_243)this.predictedPearlPath.get(pearlIdx)) < 3.0D) {
               return true;
            }
         }
      }

      return false;
   }

   private PearlCatch.SolverResult solveInterceptWithPrediction(int currentPearlTick, class_243 predictedEyePos, class_243 predictedVel) {
      class_243 windStartPos = predictedEyePos;
      class_243 inheritedVelocity = new class_243(predictedVel.field_1352, this.mc.field_1724.method_24828() ? 0.0D : predictedVel.field_1351, predictedVel.field_1350);
      PearlCatch.SolverResult bestResult = null;
      double bestDistance = Double.MAX_VALUE;
      int searchStart = currentPearlTick + 2;
      int searchEnd = Math.min(this.predictedPearlPath.size(), currentPearlTick + 50);

      for(int targetPearlTick = searchStart; targetPearlTick < searchEnd; ++targetPearlTick) {
         class_243 targetPearlPos = (class_243)this.predictedPearlPath.get(targetPearlTick);
         class_243 baseAim = targetPearlPos.method_1020(windStartPos).method_1029();

         for(double yawOff = -0.3D; yawOff <= 0.3D; yawOff += 0.05D) {
            for(double pitchOff = -0.3D; pitchOff <= 0.3D; pitchOff += 0.05D) {
               class_243 testAim = this.rotateVector(baseAim, pitchOff, yawOff);
               List<class_243> windPath = new ArrayList();
               class_243 windVel = testAim.method_1021(1.5D).method_1019(inheritedVelocity);
               this.simulateProjectile(windStartPos, windVel, 1.0D, 0.0D, 0.0D, windPath);

               for(int w = 0; w < windPath.size(); ++w) {
                  int pearlIdx = currentPearlTick + w + 2;
                  if (pearlIdx >= this.predictedPearlPath.size()) {
                     break;
                  }

                  double dist = ((class_243)windPath.get(w)).method_1022((class_243)this.predictedPearlPath.get(pearlIdx));
                  if (dist < bestDistance && dist < 1.5D) {
                     bestDistance = dist;
                     class_243 aimPoint = windStartPos.method_1019(testAim.method_1021(100.0D));
                     bestResult = new PearlCatch.SolverResult((class_243)this.predictedPearlPath.get(pearlIdx), aimPoint, pearlIdx - currentPearlTick);
                  }
               }
            }
         }
      }

      if (bestResult != null && bestDistance < 1.2D) {
         return this.refineAim(windStartPos, inheritedVelocity, bestResult, currentPearlTick);
      } else {
         return bestResult;
      }
   }

   private PearlCatch.SolverResult refineAim(class_243 windStartPos, class_243 inheritedVelocity, PearlCatch.SolverResult coarse, int currentPearlTick) {
      class_243 coarseAim = coarse.aimDirection.method_1020(windStartPos).method_1029();
      PearlCatch.SolverResult bestResult = coarse;
      double bestDistance = Double.MAX_VALUE;

      for(double yawOff = -0.04D; yawOff <= 0.04D; yawOff += 0.01D) {
         for(double pitchOff = -0.04D; pitchOff <= 0.04D; pitchOff += 0.01D) {
            class_243 testAim = this.rotateVector(coarseAim, pitchOff, yawOff);
            List<class_243> windPath = new ArrayList();
            class_243 windVel = testAim.method_1021(1.5D).method_1019(inheritedVelocity);
            this.simulateProjectile(windStartPos, windVel, 1.0D, 0.0D, 0.0D, windPath);

            for(int w = 0; w < windPath.size(); ++w) {
               int pearlIdx = currentPearlTick + w + 2;
               if (pearlIdx >= this.predictedPearlPath.size()) {
                  break;
               }

               double dist = ((class_243)windPath.get(w)).method_1022((class_243)this.predictedPearlPath.get(pearlIdx));
               if (dist < bestDistance) {
                  bestDistance = dist;
                  class_243 aimPoint = windStartPos.method_1019(testAim.method_1021(100.0D));
                  bestResult = new PearlCatch.SolverResult((class_243)this.predictedPearlPath.get(pearlIdx), aimPoint, pearlIdx - currentPearlTick);
               }
            }
         }
      }

      return bestResult;
   }

   private class_243 rotateVector(class_243 vec, double pitchOffset, double yawOffset) {
      double x = vec.field_1352;
      double y = vec.field_1351;
      double z = vec.field_1350;
      double cosYaw = Math.cos(yawOffset);
      double sinYaw = Math.sin(yawOffset);
      double newX = x * cosYaw - z * sinYaw;
      double newZ = x * sinYaw + z * cosYaw;
      double cosPitch = Math.cos(pitchOffset);
      double sinPitch = Math.sin(pitchOffset);
      double newY = y * cosPitch - newZ * sinPitch;
      newZ = y * sinPitch + newZ * cosPitch;
      return (new class_243(newX, newY, newZ)).method_1029();
   }

   private int findItemSlot(class_1792 item) {
      for(int i = 0; i < 9; ++i) {
         if (this.mc.field_1724.method_31548().method_5438(i).method_7909() == item) {
            return i;
         }
      }

      return -1;
   }

   private boolean hasItem(class_1792 item) {
      return this.findItemSlot(item) != -1;
   }

   public void onWorldRender(class_4587 matrices, float tickDelta) {
      if (this.showTrajectory.isEnabled() && !this.predictedPearlPath.isEmpty() && this.mc.field_1724 != null) {
         class_4184 camera = this.mc.field_1773.method_19418();
         class_243 cameraPos = camera.method_71156();
         class_4598 vertexConsumers = this.mc.method_22940().method_23000();
         matrices.method_22903();
         matrices.method_22904(-cameraPos.field_1352, -cameraPos.field_1351, -cameraPos.field_1350);

         class_243 aimDir;
         class_243 aimEnd;
         for(int i = 0; i < this.predictedPearlPath.size() - 1; ++i) {
            aimDir = (class_243)this.predictedPearlPath.get(i);
            aimEnd = (class_243)this.predictedPearlPath.get(i + 1);
            float progress = (float)i / (float)this.predictedPearlPath.size();
            int r = 0;
            int g = (int)(255.0F - progress * 100.0F);
            int b = 255;
            RenderUtils.drawLine(matrices, vertexConsumers, aimDir, aimEnd, new Color(r, g, b), 0.78431374F);
         }

         if (this.renderIntercept != null) {
            double size = 0.3D;
            class_238 interceptBox = new class_238(this.renderIntercept.field_1352 - size, this.renderIntercept.field_1351 - size, this.renderIntercept.field_1350 - size, this.renderIntercept.field_1352 + size, this.renderIntercept.field_1351 + size, this.renderIntercept.field_1350 + size);
            RenderUtils.drawBox(matrices, vertexConsumers, interceptBox, new Color(0, 255, 0), 0.7058824F, true);
            class_243 playerPos = this.mc.field_1724.method_33571();
            RenderUtils.drawLine(matrices, vertexConsumers, playerPos, this.renderIntercept, new Color(255, 255, 0), 0.5882353F);
         }

         if (this.renderAim != null) {
            class_243 playerPos = this.mc.field_1724.method_33571();
            aimDir = this.renderAim.method_1020(playerPos).method_1029().method_1021(50.0D);
            aimEnd = playerPos.method_1019(aimDir);
            RenderUtils.drawLine(matrices, vertexConsumers, playerPos, aimEnd, new Color(255, 0, 0), 0.78431374F);
         }

         matrices.method_22909();
         vertexConsumers.method_22993();
      }
   }

   @Environment(EnvType.CLIENT)
   private static enum State {
      IDLE,
      THROWING_PEARL,
      NORMAL_THROW,
      TRACKING,
      EXECUTING_CATCH,
      RESETTING;

      // $FF: synthetic method
      private static PearlCatch.State[] $values() {
         return new PearlCatch.State[]{IDLE, THROWING_PEARL, NORMAL_THROW, TRACKING, EXECUTING_CATCH, RESETTING};
      }
   }

   @Environment(EnvType.CLIENT)
   private static record SolverResult(class_243 interceptPos, class_243 aimDirection, int ticksFromNow) {
      private SolverResult(class_243 interceptPos, class_243 aimDirection, int ticksFromNow) {
         this.interceptPos = interceptPos;
         this.aimDirection = aimDirection;
         this.ticksFromNow = ticksFromNow;
      }

      public class_243 interceptPos() {
         return this.interceptPos;
      }

      public class_243 aimDirection() {
         return this.aimDirection;
      }

      public int ticksFromNow() {
         return this.ticksFromNow;
      }
   }
}
