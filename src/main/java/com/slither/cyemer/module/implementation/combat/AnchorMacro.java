package com.slither.cyemer.module.implementation.combat;

import com.slither.cyemer.module.BooleanSetting;
import com.slither.cyemer.module.Category;
import com.slither.cyemer.module.Module;
import com.slither.cyemer.module.SliderSetting;
import com.slither.cyemer.util.PlaceValidator;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3532;
import net.minecraft.class_3965;
import net.minecraft.class_4969;

@Environment(EnvType.CLIENT)
public class AnchorMacro extends Module {
   private final BooleanSetting useTotem = new BooleanSetting("Use Totem", true);
   private final SliderSetting fillDelay = new SliderSetting("Fill Delay (ms)", 50.0D, 0.0D, 500.0D, 0);
   private final SliderSetting explodeDelay = new SliderSetting("Explode Delay (ms)", 50.0D, 0.0D, 500.0D, 0);
   private final BooleanSetting doubleClickExplode = new BooleanSetting("Double Click Explode", false);
   private final BooleanSetting safeMode = new BooleanSetting("Safe Mode", false);
   private final BooleanSetting aim = new BooleanSetting("Aim", false);
   private final BooleanSetting onlyOwnAnchors = new BooleanSetting("Only Own Anchors", true);
   private static final double REACH_DISTANCE_SQ = 25.0D;
   private int stage = 0;
   private class_2338 anchorPos = null;
   private int originalSlot = -1;
   private int explodeClickCount = 0;
   private long lastActionTime = 0L;
   private int lookTicks = 0;
   private boolean safeCycleDone = false;
   private boolean forcingSneak = false;
   private boolean sneakWasPressed = false;
   private boolean aimDone = false;
   private final Set<class_2338> placedAnchors = new HashSet();

   public AnchorMacro() {
      super("AnchorMacro", "Automatically fills and breaks anchors.", Category.COMBAT);
      this.addSetting(this.useTotem);
      this.addSetting(this.fillDelay);
      this.addSetting(this.explodeDelay);
      this.addSetting(this.doubleClickExplode);
      this.addSetting(this.safeMode);
      this.addSetting(this.aim);
      this.addSetting(this.onlyOwnAnchors);
   }

   public void onEnable() {
      this.reset();
   }

   public void onDisable() {
      this.restoreOriginalSlot();
      this.reset();
      this.placedAnchors.clear();
   }

   public void onItemUse(class_3965 hitResult) {
      if (this.onlyOwnAnchors.isEnabled()) {
         if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
            if (this.mc.field_1724.method_6047().method_7909() == class_1802.field_23141) {
               class_2338 clickedPos = hitResult.method_17777();
               class_2680 clickedState = this.mc.field_1687.method_8320(clickedPos);
               class_2338 anchorPlacementPos;
               if (clickedState.method_45474()) {
                  anchorPlacementPos = clickedPos;
               } else {
                  anchorPlacementPos = clickedPos.method_10093(hitResult.method_17780());
               }

               this.placedAnchors.add(anchorPlacementPos);
            }

         }
      }
   }

   public void onTick() {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         if (this.mc.field_1755 == null) {
            if (this.anchorPos != null && this.canInteractWithAnchor()) {
               ++this.lookTicks;
            } else {
               this.lookTicks = 0;
            }

            if (this.stage == 0) {
               if (this.mc.field_1724.method_6115()) {
                  return;
               }

               if (!this.isEmptyAnchorInReach()) {
                  return;
               }

               class_2338 targetPos = PlaceValidator.getBlockHitResult(this.mc).method_17777();
               if (this.onlyOwnAnchors.isEnabled() && !this.placedAnchors.contains(targetPos)) {
                  return;
               }

               this.anchorPos = targetPos;
               this.originalSlot = this.mc.field_1724.method_31548().method_67532();
               this.safeCycleDone = false;
               this.aimDone = false;
               this.stage = 1;
            }

            int requiredClicks;
            if (this.stage == 1) {
               if (!this.isAnchorStillValid()) {
                  this.reset();
                  return;
               }

               requiredClicks = this.findItemInHotbar(class_1802.field_8801);
               if (requiredClicks != -1) {
                  this.mc.field_1724.method_31548().method_61496(requiredClicks);
                  this.stage = 2;
                  this.lastActionTime = System.currentTimeMillis();
               } else {
                  this.reset();
               }
            }

            if (this.stage == 2) {
               if (!this.isAnchorStillValid()) {
                  this.reset();
                  return;
               }

               if ((double)(System.currentTimeMillis() - this.lastActionTime) < this.fillDelay.getValue()) {
                  return;
               }

               if (!this.canInteractWithAnchor() || this.lookTicks < 2) {
                  return;
               }

               if (this.mc.field_1724.method_5624()) {
                  this.mc.field_1724.method_5728(false);
               }

               class_2680 anchorState = this.mc.field_1687.method_8320(this.anchorPos);
               int currentCharges = (Integer)anchorState.method_11654(class_4969.field_23153);
               if (this.safeMode.isEnabled()) {
                  if (currentCharges == 0) {
                     if (PlaceValidator.canPlace(this.mc) && this.tryUseAnchor("combat.use.anchormacro.fill")) {
                        this.lastActionTime = System.currentTimeMillis();
                     }

                     return;
                  }

                  if (this.isAnchorFloating()) {
                     this.stopForcedSneak();
                     this.safeCycleDone = true;
                     this.stage = 3;
                     this.lastActionTime = System.currentTimeMillis();
                     return;
                  }

                  if (!this.safeCycleDone) {
                     if (!this.forcingSneak) {
                        this.startForcedSneak();
                        this.lastActionTime = System.currentTimeMillis();
                        return;
                     }

                     if (System.currentTimeMillis() - this.lastActionTime < 35L) {
                        return;
                     }

                     this.tryUseAnchor("combat.use.anchormacro.safe.place");
                     this.stopForcedSneak();
                     this.safeCycleDone = true;
                     this.stage = 3;
                     this.lastActionTime = System.currentTimeMillis();
                     return;
                  }

                  this.stage = 3;
                  return;
               }

               if (currentCharges == 0) {
                  if (PlaceValidator.canPlace(this.mc) && this.tryUseAnchor("combat.use.anchormacro.fill")) {
                     this.stage = 3;
                  }
               } else if (currentCharges > 0) {
                  this.stage = 3;
               }
            }

            if (this.stage == 3) {
               if (!this.isAnchorStillValid()) {
                  this.reset();
                  return;
               }

               requiredClicks = this.useTotem.isEnabled() ? this.findItemInHotbar(class_1802.field_8288) : -1;
               if (requiredClicks == -1) {
                  requiredClicks = this.originalSlot;
               }

               if (requiredClicks != -1) {
                  this.mc.field_1724.method_31548().method_61496(requiredClicks);
                  this.stage = 4;
                  this.lastActionTime = System.currentTimeMillis();
               } else {
                  this.reset();
               }
            }

            if (this.stage == 4) {
               if (!this.isAnchorStillValid()) {
                  this.reset();
                  return;
               }

               if (this.aim.isEnabled() && !this.aimDone) {
                  if (!this.isAnchorFloating()) {
                     this.snapAimToAnchor();
                  }

                  this.aimDone = true;
               }

               if ((double)(System.currentTimeMillis() - this.lastActionTime) < this.explodeDelay.getValue()) {
                  return;
               }

               if (!this.canInteractWithAnchor() || this.lookTicks < 2) {
                  return;
               }

               if (this.mc.field_1724.method_5624()) {
                  this.mc.field_1724.method_5728(false);
               }

               requiredClicks = this.doubleClickExplode.isEnabled() ? 2 : 1;
               if (this.explodeClickCount < requiredClicks) {
                  if (this.tryUseAnchor("combat.use.anchormacro.explode")) {
                     ++this.explodeClickCount;
                  }
               } else {
                  if (this.anchorPos != null) {
                     this.placedAnchors.remove(this.anchorPos);
                  }

                  this.reset();
               }
            }

         }
      }
   }

   private boolean isEmptyAnchorInReach() {
      if (!PlaceValidator.canPlace(this.mc)) {
         return false;
      } else if (PlaceValidator.getBlockHitResult(this.mc) == null) {
         return false;
      } else {
         class_2338 targetPos = PlaceValidator.getBlockHitResult(this.mc).method_17777();
         if (this.mc.field_1724.method_33571().method_1025(targetPos.method_46558()) >= 25.0D) {
            return false;
         } else {
            class_2680 state = this.mc.field_1687.method_8320(targetPos);
            return state.method_27852(class_2246.field_23152) && (Integer)state.method_11654(class_4969.field_23153) == 0;
         }
      }
   }

   private boolean canInteractWithAnchor() {
      return PlaceValidator.canPlace(this.mc) && PlaceValidator.getBlockHitResult(this.mc) != null && PlaceValidator.getBlockHitResult(this.mc).method_17777().equals(this.anchorPos);
   }

   private boolean isAnchorStillValid() {
      if (this.anchorPos == null) {
         return false;
      } else if (this.mc.field_1724.method_33571().method_1025(this.anchorPos.method_46558()) >= 25.0D) {
         return false;
      } else {
         class_2680 state = this.mc.field_1687.method_8320(this.anchorPos);
         return state.method_27852(class_2246.field_23152);
      }
   }

   private boolean isAnchorFloating() {
      return this.anchorPos != null && this.mc.field_1687 != null ? this.mc.field_1687.method_8320(this.anchorPos.method_10074()).method_45474() : false;
   }

   private int findItemInHotbar(class_1792 item) {
      for(int i = 0; i < 9; ++i) {
         if (this.mc.field_1724.method_31548().method_5438(i).method_7909() == item) {
            return i;
         }
      }

      return -1;
   }

   private void restoreOriginalSlot() {
      if (this.mc.field_1724 != null && this.originalSlot != -1) {
         this.mc.field_1724.method_31548().method_61496(this.originalSlot);
         this.originalSlot = -1;
      }

   }

   private void reset() {
      this.stopForcedSneak();
      this.restoreOriginalSlot();
      this.stage = 0;
      this.anchorPos = null;
      this.explodeClickCount = 0;
      this.lastActionTime = 0L;
      this.lookTicks = 0;
      this.safeCycleDone = false;
      this.aimDone = false;
   }

   private boolean tryUseAnchor(String actionKey) {
      return PlaceValidator.tryPlace(this.mc);
   }

   private void startForcedSneak() {
      if (this.mc != null && this.mc.field_1690 != null && !this.forcingSneak) {
         this.sneakWasPressed = this.mc.field_1690.field_1832.method_1434();
         this.mc.field_1690.field_1832.method_23481(true);
         this.forcingSneak = true;
      }
   }

   private void stopForcedSneak() {
      if (this.mc != null && this.mc.field_1690 != null && this.forcingSneak) {
         this.mc.field_1690.field_1832.method_23481(this.sneakWasPressed);
         this.forcingSneak = false;
         this.sneakWasPressed = false;
      }
   }

   private void snapAimToAnchor() {
      if (this.mc.field_1724 != null && this.anchorPos != null) {
         class_243 eyePos = this.mc.field_1724.method_33571();
         class_243 targetPos = this.anchorPos.method_46558();
         double deltaX = targetPos.field_1352 - eyePos.field_1352;
         double deltaY = targetPos.field_1351 - eyePos.field_1351;
         double deltaZ = targetPos.field_1350 - eyePos.field_1350;
         double horizontalDist = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
         if (!(horizontalDist < 1.0E-4D)) {
            float targetYaw = (float)Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F;
            float targetPitch = (float)(-Math.toDegrees(Math.atan2(deltaY, horizontalDist)));
            this.mc.field_1724.method_36456(class_3532.method_15393(targetYaw));
            this.mc.field_1724.method_36457(class_3532.method_15363(targetPitch, -90.0F, 90.0F));
         }
      }
   }
}
