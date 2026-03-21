package com.slither.cyemer.module.implementation.combat;

import com.slither.cyemer.mixin.MinecraftClientAccessor;
import com.slither.cyemer.module.BooleanSetting;
import com.slither.cyemer.module.Category;
import com.slither.cyemer.module.ModeSetting;
import com.slither.cyemer.module.Module;
import com.slither.cyemer.module.SliderSetting;
import com.slither.cyemer.util.RotationManager;
import com.slither.cyemer.util.SystemInputSimulator;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3532;
import net.minecraft.class_3965;
import net.minecraft.class_4969;

@Environment(EnvType.CLIENT)
public class AutoAnchor extends Module {
   private final BooleanSetting speedMode = new BooleanSetting("Speed Mode", false);
   private final SliderSetting speedSetting = new SliderSetting("Speed", 7.5D, 1.0D, 10.0D, 1);
   private final BooleanSetting useTotem = new BooleanSetting("Use Totem", true);
   private final BooleanSetting simClick = new BooleanSetting("Sim Click", false);
   private final BooleanSetting safeMode = new BooleanSetting("Safe Mode", false);
   private final BooleanSetting onlyOwnAnchors = new BooleanSetting("Only Own Anchors", true);
   private final BooleanSetting silentRotation = new BooleanSetting("Silent Rotation", false);
   private final SliderSetting rotationStrength = new SliderSetting("Rotation Strength", 10.0D, 1.0D, 20.0D, 1);
   private final ModeSetting rotPattern = new ModeSetting("Pattern", new String[]{"Sine", "Smooth", "Linear", "Instant"});
   private final SliderSetting rotJitter = new SliderSetting("Jitter", 0.1D, 0.0D, 1.0D, 2);
   private static final double REACH_DISTANCE_SQ = 25.0D;
   private static final long COOLDOWN_MS = 400L;
   private static final long TIMEOUT_MS = 1250L;
   private static final long SLOT_RESTORE_DELAY_MS = 50L;
   private static final long ROTATION_WAIT_MS = 50L;
   private static final long GLOWSTONE_TIMEOUT_MS = 200L;
   private AutoAnchor.State currentState;
   private class_2338 anchorPos;
   private class_2338 glowstoneBlockPos;
   private class_2338 glowstonePlaceAgainst;
   private class_2350 glowstonePlaceDirection;
   private int originalSlot;
   private long lastTime;
   private long glowstoneAttemptStart;
   private long lastRenderExecTime;
   private final Set<class_2338> placedAnchors;
   private class_2680 cachedAnchorState;

   public AutoAnchor() {
      super("AutoAnchor", "Fills and explodes anchors with randomized patterns.", Category.COMBAT);
      this.currentState = AutoAnchor.State.IDLE;
      this.anchorPos = null;
      this.glowstoneBlockPos = null;
      this.glowstonePlaceAgainst = null;
      this.glowstonePlaceDirection = null;
      this.originalSlot = -1;
      this.lastTime = 0L;
      this.glowstoneAttemptStart = 0L;
      this.lastRenderExecTime = 0L;
      this.placedAnchors = new HashSet();
      this.cachedAnchorState = null;
      this.addSetting(this.speedMode);
      this.addSetting(this.speedSetting);
      this.addSetting(this.useTotem);
      this.addSetting(this.simClick);
      this.addSetting(this.safeMode);
      this.addSetting(this.onlyOwnAnchors);
      this.addSetting(this.silentRotation);
      this.addSetting(this.rotationStrength);
      this.addSetting(this.rotPattern);
      this.addSetting(this.rotJitter);
   }

   public void onEnable() {
      this.reset();
   }

   public void onDisable() {
      RotationManager.clearTarget(this);
      this.restoreOriginalSlot();
      this.reset();
      this.placedAnchors.clear();
   }

   public void onItemUse(class_3965 hitResult) {
      if (this.onlyOwnAnchors.isEnabled()) {
         if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
            class_2338 targetPos;
            class_2680 state;
            if (this.mc.field_1724.method_6047().method_7909() != class_1802.field_23141) {
               if (this.mc.field_1724.method_6047().method_7909() == class_1802.field_8801) {
                  targetPos = hitResult.method_17777();
                  state = this.mc.field_1687.method_8320(targetPos);
                  if (state.method_27852(class_2246.field_23152) && (Integer)state.method_11654(class_4969.field_23153) == 4) {
                     this.placedAnchors.remove(targetPos);
                  }
               }

            } else {
               targetPos = hitResult.method_17777();
               state = this.mc.field_1687.method_8320(targetPos);
               class_2338 anchorPlacementPos;
               if (state.method_45474()) {
                  anchorPlacementPos = targetPos;
               } else {
                  anchorPlacementPos = targetPos.method_10093(hitResult.method_17780());
               }

               this.placedAnchors.add(anchorPlacementPos);
            }
         }
      }
   }

   public void onTick() {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         if (!this.speedMode.isEnabled()) {
            this.updateLogic();
         }

         if (this.speedMode.isEnabled()) {
            long delay = this.getSpeedModeDelay();
            if (System.currentTimeMillis() - this.lastRenderExecTime >= delay) {
               this.updateLogic();
               this.lastRenderExecTime = System.currentTimeMillis();
            }
         }

      }
   }

   private long getSpeedModeDelay() {
      double speed = this.speedSetting.getValue();
      return speed >= 10.0D ? 0L : (long)((10.0D - speed) * 8.88888888888889D);
   }

   private void updateLogic() {
      if (this.currentState != AutoAnchor.State.IDLE && this.currentState != AutoAnchor.State.COOLDOWN && this.timePassed(1250L)) {
         this.reset();
      } else {
         int glowstoneBlockSlot;
         switch(this.currentState.ordinal()) {
         case 0:
            class_239 var2 = this.mc.field_1765;
            if (!(var2 instanceof class_3965)) {
               return;
            }

            class_3965 blockHitResult = (class_3965)var2;
            class_2338 targetPos = blockHitResult.method_17777();
            if (this.onlyOwnAnchors.isEnabled() && !this.placedAnchors.contains(targetPos)) {
               return;
            }

            if (this.isNewAnchor(targetPos)) {
               this.anchorPos = targetPos;
               this.originalSlot = this.mc.field_1724.method_31548().method_67532();
               this.resetTimer();
               this.startRotatingToAnchor();
               this.currentState = AutoAnchor.State.ROTATING_TO_FILL;
            }
            break;
         case 1:
            if (!this.isAnchorStillValid()) {
               this.reset();
               return;
            }

            if (this.timePassed(50L) || this.isRotationComplete()) {
               this.currentState = AutoAnchor.State.FILL_ANCHOR;
               this.resetTimer();
            }
            break;
         case 2:
            if (!this.isAnchorStillValid()) {
               this.reset();
               return;
            }

            int glowstoneSlot = this.findItemInHotbar(class_1802.field_8801);
            if (glowstoneSlot != -1) {
               this.mc.field_1724.method_31548().method_61496(glowstoneSlot);
               this.simClickUseKey();
               this.currentState = AutoAnchor.State.WAITING_FOR_FILL;
               this.resetTimer();
            } else {
               this.reset();
            }
            break;
         case 3:
            if (!this.isAnchorStillValid()) {
               this.reset();
               return;
            }

            if ((Integer)this.cachedAnchorState.method_11654(class_4969.field_23153) > 0) {
               if (this.shouldUseSafeMode()) {
                  this.currentState = AutoAnchor.State.ROTATING_TO_GLOWSTONE;
                  this.glowstoneAttemptStart = System.currentTimeMillis();
               } else {
                  this.currentState = AutoAnchor.State.PREPARE_TO_EXPLODE;
               }

               this.resetTimer();
            }
            break;
         case 4:
            if (!this.isAnchorStillValid()) {
               this.reset();
               return;
            }

            if (System.currentTimeMillis() - this.glowstoneAttemptStart > 200L) {
               this.resetTimer();
               this.currentState = AutoAnchor.State.PREPARE_TO_EXPLODE;
            } else {
               class_243 playerPos = this.mc.field_1724.method_73189();
               class_243 anchorCenter = this.anchorPos.method_46558();
               class_243 midpoint = playerPos.method_1019(anchorCenter).method_1021(0.5D);
               this.glowstoneBlockPos = new class_2338((int)Math.floor(midpoint.field_1352), this.anchorPos.method_10264(), (int)Math.floor(midpoint.field_1350));
               class_2680 glowstoneBlockState = this.mc.field_1687.method_8320(this.glowstoneBlockPos);
               if (glowstoneBlockState.method_45474()) {
                  glowstoneBlockSlot = this.findItemInHotbar(class_1802.field_8801);
                  if (glowstoneBlockSlot != -1) {
                     this.mc.field_1724.method_31548().method_61496(glowstoneBlockSlot);
                     this.glowstonePlaceAgainst = this.findAdjacentSolidBlock(this.glowstoneBlockPos);
                     if (this.glowstonePlaceAgainst != null) {
                        this.glowstonePlaceDirection = class_2350.method_10147((float)(this.glowstoneBlockPos.method_10263() - this.glowstonePlaceAgainst.method_10263()), (float)(this.glowstoneBlockPos.method_10264() - this.glowstonePlaceAgainst.method_10264()), (float)(this.glowstoneBlockPos.method_10260() - this.glowstonePlaceAgainst.method_10260()));
                        if (this.glowstonePlaceDirection != null) {
                           this.startRotatingToGlowstoneBlock(this.glowstonePlaceAgainst);
                           this.currentState = AutoAnchor.State.PLACE_GLOWSTONE_BLOCK;
                           this.resetTimer();
                           return;
                        }
                     }
                  } else {
                     this.resetTimer();
                     this.currentState = AutoAnchor.State.PREPARE_TO_EXPLODE;
                  }
               } else {
                  this.resetTimer();
                  this.currentState = AutoAnchor.State.ROTATING_TO_ANCHOR;
                  this.startRotatingToAnchor();
               }
            }
            break;
         case 5:
            if (!this.isAnchorStillValid()) {
               this.reset();
               return;
            }

            if (this.timePassed(50L) || this.isRotationComplete()) {
               if (this.glowstonePlaceAgainst != null && this.glowstonePlaceDirection != null) {
                  this.simClickUseKey();
                  this.resetTimer();
                  this.currentState = AutoAnchor.State.ROTATING_TO_ANCHOR;
                  this.startRotatingToAnchor();
               } else {
                  this.resetTimer();
                  this.currentState = AutoAnchor.State.PREPARE_TO_EXPLODE;
               }
            }
            break;
         case 6:
            if (!this.isAnchorStillValid()) {
               this.reset();
               return;
            }

            if (this.timePassed(50L) || this.isRotationComplete()) {
               this.currentState = AutoAnchor.State.PREPARE_TO_EXPLODE;
               this.resetTimer();
            }
            break;
         case 7:
            if (!this.isAnchorStillValid()) {
               this.reset();
               return;
            }

            glowstoneBlockSlot = this.useTotem.isEnabled() ? this.findItemInHotbar(class_1802.field_8288) : -1;
            if (glowstoneBlockSlot == -1) {
               glowstoneBlockSlot = this.findEmptyHotbarSlot();
            }

            if (glowstoneBlockSlot != -1) {
               this.mc.field_1724.method_31548().method_61496(glowstoneBlockSlot);
               this.startRotatingToAnchor();
               this.currentState = AutoAnchor.State.ROTATING_TO_EXPLODE;
               this.resetTimer();
            } else {
               this.reset();
            }
            break;
         case 8:
            if (!this.isAnchorStillValid()) {
               this.reset();
               return;
            }

            if (this.timePassed(50L) || this.isRotationComplete()) {
               this.currentState = AutoAnchor.State.ARMED;
               this.resetTimer();
            }
            break;
         case 9:
            if (!this.isAnchorStillValid()) {
               this.reset();
               return;
            }

            this.simClickUseKey();
            if (this.anchorPos != null) {
               this.placedAnchors.remove(this.anchorPos);
            }

            RotationManager.clearTarget(this);
            this.resetTimer();
            this.currentState = AutoAnchor.State.COOLDOWN;
            break;
         case 10:
            if (this.timePassed(50L) && this.originalSlot != -1) {
               this.restoreOriginalSlot();
            }

            if (this.timePassed(400L)) {
               this.reset();
            }
         }

      }
   }

   private boolean shouldUseSimClick() {
      return this.speedMode.isEnabled() ? false : this.simClick.isEnabled();
   }

   private boolean shouldUseSafeMode() {
      return this.speedMode.isEnabled() ? false : this.safeMode.isEnabled();
   }

   private void resetTimer() {
      this.lastTime = System.currentTimeMillis();
   }

   private boolean timePassed(long milliseconds) {
      return System.currentTimeMillis() - this.lastTime >= milliseconds;
   }

   private RotationManager.RotationMode getCurrentMode() {
      try {
         return RotationManager.RotationMode.valueOf(this.rotPattern.getCurrentMode().toUpperCase());
      } catch (Exception var2) {
         return RotationManager.RotationMode.SINE;
      }
   }

   private void startRotatingToAnchor() {
      if (this.anchorPos != null) {
         RotationManager.setRotationSupplier(this, RotationManager.Priority.HIGH, this::findVisibleAnchorPoint, this.rotationStrength.getValue(), this.getCurrentMode(), this.rotJitter.getValue(), this.silentRotation.isEnabled(), false);
      }

   }

   private void startRotatingToGlowstoneBlock(class_2338 targetBlock) {
      if (targetBlock != null) {
         RotationManager.setRotationSupplier(this, RotationManager.Priority.HIGH, () -> {
            return targetBlock.method_46558();
         }, this.rotationStrength.getValue(), this.getCurrentMode(), this.rotJitter.getValue(), this.silentRotation.isEnabled(), false);
      }

   }

   private class_243 findVisibleAnchorPoint() {
      if (this.anchorPos == null) {
         return class_243.field_1353;
      } else {
         return this.glowstoneBlockPos != null ? new class_243((double)this.anchorPos.method_10263() + 0.5D, (double)this.anchorPos.method_10264() + 0.9D, (double)this.anchorPos.method_10260() + 0.5D) : this.anchorPos.method_46558();
      }
   }

   private void simClickUseKey() {
      if (this.shouldUseSimClick()) {
         SystemInputSimulator.pressUse();
         SystemInputSimulator.releaseUse();
      } else {
         ((MinecraftClientAccessor)this.mc).useItem();
      }

   }

   private boolean isNewAnchor(class_2338 pos) {
      if (this.mc.field_1724.method_33571().method_1025(pos.method_46558()) >= 25.0D) {
         return false;
      } else {
         class_2680 state = this.mc.field_1687.method_8320(pos);
         return state.method_27852(class_2246.field_23152) && (Integer)state.method_11654(class_4969.field_23153) == 0;
      }
   }

   private boolean isAnchorStillValid() {
      if (this.anchorPos != null && !(this.mc.field_1724.method_33571().method_1025(this.anchorPos.method_46558()) >= 25.0D)) {
         this.cachedAnchorState = this.mc.field_1687.method_8320(this.anchorPos);
         return this.cachedAnchorState.method_27852(class_2246.field_23152);
      } else {
         return false;
      }
   }

   private boolean isRotationComplete() {
      if (this.anchorPos != null && this.mc.field_1724 != null) {
         if (!RotationManager.isActive()) {
            return true;
         } else {
            class_243 targetPoint = this.findVisibleAnchorPoint();
            float[] needed = RotationManager.calculateRotationsToPos(targetPoint, RotationManager.getFinalYaw());
            float currentYaw = RotationManager.getFinalYaw();
            float currentPitch = RotationManager.getFinalPitch();
            float yawDiff = Math.abs(class_3532.method_15393(needed[0] - currentYaw));
            float pitchDiff = Math.abs(needed[1] - currentPitch);
            return yawDiff < 3.0F && pitchDiff < 3.0F;
         }
      } else {
         return false;
      }
   }

   private int findItemInHotbar(class_1792 item) {
      for(int i = 0; i < 9; ++i) {
         if (this.mc.field_1724.method_31548().method_5438(i).method_7909() == item) {
            return i;
         }
      }

      return -1;
   }

   private int findEmptyHotbarSlot() {
      for(int i = 0; i < 9; ++i) {
         if (this.mc.field_1724.method_31548().method_5438(i).method_7960()) {
            return i;
         }
      }

      return -1;
   }

   private class_2338 findAdjacentSolidBlock(class_2338 pos) {
      class_2350[] directions = new class_2350[]{class_2350.field_11033, class_2350.field_11036, class_2350.field_11043, class_2350.field_11035, class_2350.field_11039, class_2350.field_11034};
      class_2350[] var3 = directions;
      int var4 = directions.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         class_2350 dir = var3[var5];
         class_2338 adjacentPos = pos.method_10093(dir);
         if (!this.mc.field_1687.method_8320(adjacentPos).method_45474()) {
            return adjacentPos;
         }
      }

      return null;
   }

   private void restoreOriginalSlot() {
      if (this.mc.field_1724 != null && this.originalSlot != -1) {
         this.mc.field_1724.method_31548().method_61496(this.originalSlot);
         this.originalSlot = -1;
      }

   }

   private void reset() {
      RotationManager.clearTarget(this);
      this.restoreOriginalSlot();
      this.currentState = AutoAnchor.State.IDLE;
      this.anchorPos = null;
      this.glowstoneBlockPos = null;
      this.glowstonePlaceAgainst = null;
      this.glowstonePlaceDirection = null;
      this.cachedAnchorState = null;
      this.resetTimer();
      this.glowstoneAttemptStart = 0L;
      this.lastRenderExecTime = 0L;
   }

   @Environment(EnvType.CLIENT)
   private static enum State {
      IDLE,
      ROTATING_TO_FILL,
      FILL_ANCHOR,
      WAITING_FOR_FILL,
      ROTATING_TO_GLOWSTONE,
      PLACE_GLOWSTONE_BLOCK,
      ROTATING_TO_ANCHOR,
      PREPARE_TO_EXPLODE,
      ROTATING_TO_EXPLODE,
      ARMED,
      COOLDOWN;

      // $FF: synthetic method
      private static AutoAnchor.State[] $values() {
         return new AutoAnchor.State[]{IDLE, ROTATING_TO_FILL, FILL_ANCHOR, WAITING_FOR_FILL, ROTATING_TO_GLOWSTONE, PLACE_GLOWSTONE_BLOCK, ROTATING_TO_ANCHOR, PREPARE_TO_EXPLODE, ROTATING_TO_EXPLODE, ARMED, COOLDOWN};
      }
   }
}
