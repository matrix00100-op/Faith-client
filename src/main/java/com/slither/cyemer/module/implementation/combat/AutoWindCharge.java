package com.slither.cyemer.module.implementation.combat;

import com.slither.cyemer.mixin.KeyBindingAccessor;
import com.slither.cyemer.module.BooleanSetting;
import com.slither.cyemer.module.Category;
import com.slither.cyemer.module.ModeSetting;
import com.slither.cyemer.module.Module;
import com.slither.cyemer.module.SliderSetting;
import com.slither.cyemer.util.RotationManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_4587;

@Environment(EnvType.CLIENT)
public class AutoWindCharge extends Module {
   private final SliderSetting strength = new SliderSetting("Rot Speed", 12.0D, 1.0D, 20.0D, 1);
   private final ModeSetting rotPattern = new ModeSetting("Pattern", new String[]{"Sine", "Smooth", "Instant"});
   private final SliderSetting rotRandom = new SliderSetting("Randomness", 0.05D, 0.0D, 1.0D, 2);
   private final BooleanSetting silentRotations = new BooleanSetting("Silent", true);
   private final SliderSetting preparationDelay = new SliderSetting("Prep Delay", 0.0D, 0.0D, 1000.0D, 10);
   private final BooleanSetting autoJump = new BooleanSetting("Auto Jump", true);
   private final SliderSetting jumpDelay = new SliderSetting("Jump Delay", 150.0D, 0.0D, 500.0D, 10);
   private final BooleanSetting autoCrouch = new BooleanSetting("Auto Crouch", true);
   private final SliderSetting crouchDelay = new SliderSetting("Crouch Delay", 150.0D, 0.0D, 500.0D, 10);
   private AutoWindCharge.State currentState;
   private int chargeSlot;
   private int originalSlot;
   private long throwTime;
   private long enableTime;

   public AutoWindCharge() {
      super("AutoWindCharge", "Performs mace wind charge jumps.", Category.COMBAT);
      this.currentState = AutoWindCharge.State.SEARCHING;
      this.chargeSlot = -1;
      this.originalSlot = -1;
      this.throwTime = 0L;
      this.enableTime = 0L;
      this.addSetting(this.strength);
      this.addSetting(this.rotPattern);
      this.addSetting(this.rotRandom);
      this.addSetting(this.silentRotations);
      this.addSetting(this.preparationDelay);
      this.addSetting(this.autoJump);
      this.addSetting(this.jumpDelay);
      this.addSetting(this.autoCrouch);
      this.addSetting(this.crouchDelay);
   }

   public void onEnable() {
      this.currentState = AutoWindCharge.State.SEARCHING;
      this.chargeSlot = -1;
      this.originalSlot = -1;
      this.throwTime = 0L;
      this.enableTime = System.currentTimeMillis();
   }

   public void onDisable() {
      if (this.mc.field_1690 != null) {
         this.mc.field_1690.field_1904.method_23481(false);
         this.mc.field_1690.field_1903.method_23481(false);
         this.mc.field_1690.field_1832.method_23481(false);
      }

      if (this.originalSlot != -1 && this.mc.field_1724 != null) {
         this.mc.field_1724.method_31548().method_61496(this.originalSlot);
      }

      RotationManager.clearTarget(this);
   }

   public void onTick() {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         switch(this.currentState.ordinal()) {
         case 0:
            this.chargeSlot = this.findWindCharge();
            if (this.chargeSlot == -1) {
               this.toggle();
               return;
            }

            this.originalSlot = this.mc.field_1724.method_31548().method_67532();
            RotationManager.setRotationSupplier(this, RotationManager.Priority.HIGHEST, () -> {
               float yaw = RotationManager.getVisualYaw();
               double rad = Math.toRadians((double)yaw);
               double dx = -Math.sin(rad) * 0.1D;
               double dz = Math.cos(rad) * 0.1D;
               return this.mc.field_1724.method_73189().method_1031(dx, -5.0D, dz);
            }, this.strength.getValue(), this.getRotationMode(), 0.0D, this.silentRotations.isEnabled(), false);
            this.currentState = AutoWindCharge.State.ROTATING;
            break;
         case 6:
            this.mc.field_1724.method_31548().method_61496(this.originalSlot);
            RotationManager.clearTarget(this);
            this.mc.field_1690.field_1903.method_23481(false);
            this.mc.field_1690.field_1832.method_23481(false);
            this.currentState = AutoWindCharge.State.FINISHING;
            break;
         case 7:
            this.toggle();
         }

      } else {
         this.toggle();
      }
   }

   public void onWorldRender(class_4587 matrices, float tickDelta) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         switch(this.currentState.ordinal()) {
         case 1:
            boolean isLookingDown = RotationManager.getFinalPitch() >= 88.0F;
            boolean isDelayPassed = (double)(System.currentTimeMillis() - this.enableTime) >= this.preparationDelay.getValue();
            if (isLookingDown && isDelayPassed) {
               this.currentState = AutoWindCharge.State.SWAPPING;
            }
            break;
         case 2:
            this.mc.field_1724.method_31548().method_61496(this.chargeSlot);
            this.currentState = AutoWindCharge.State.THROWING;
            break;
         case 3:
            KeyBindingAccessor useKey = (KeyBindingAccessor)this.mc.field_1690.field_1904;
            useKey.setTimesPressed(useKey.getTimesPressed() + 1);
            this.throwTime = System.currentTimeMillis();
            this.currentState = AutoWindCharge.State.WAITING_FOR_ACTION;
            break;
         case 4:
            this.currentState = AutoWindCharge.State.EXECUTING_ACTION;
            break;
         case 5:
            long elapsed = System.currentTimeMillis() - this.throwTime;
            if (this.autoJump.isEnabled() && (double)elapsed >= this.jumpDelay.getValue() && this.mc.field_1724.method_24828()) {
               this.mc.field_1690.field_1903.method_23481(true);
            }

            if (this.autoCrouch.isEnabled() && (double)elapsed >= this.crouchDelay.getValue()) {
               this.mc.field_1690.field_1832.method_23481(true);
            }

            double maxDelay = 0.0D;
            if (this.autoJump.isEnabled()) {
               maxDelay = Math.max(maxDelay, this.jumpDelay.getValue());
            }

            if (this.autoCrouch.isEnabled()) {
               maxDelay = Math.max(maxDelay, this.crouchDelay.getValue());
            }

            if ((double)elapsed > maxDelay + 50.0D) {
               this.currentState = AutoWindCharge.State.CLEANUP;
            }
         }

      }
   }

   private RotationManager.RotationMode getRotationMode() {
      try {
         return RotationManager.RotationMode.valueOf(this.rotPattern.getCurrentMode().toUpperCase());
      } catch (Exception var2) {
         return RotationManager.RotationMode.SINE;
      }
   }

   private int findWindCharge() {
      for(int i = 0; i < 9; ++i) {
         class_1799 stack = this.mc.field_1724.method_31548().method_5438(i);
         if (stack.method_7909() == class_1802.field_49098) {
            return i;
         }
      }

      return -1;
   }

   @Environment(EnvType.CLIENT)
   private static enum State {
      SEARCHING,
      ROTATING,
      SWAPPING,
      THROWING,
      WAITING_FOR_ACTION,
      EXECUTING_ACTION,
      CLEANUP,
      FINISHING;

      // $FF: synthetic method
      private static AutoWindCharge.State[] $values() {
         return new AutoWindCharge.State[]{SEARCHING, ROTATING, SWAPPING, THROWING, WAITING_FOR_ACTION, EXECUTING_ACTION, CLEANUP, FINISHING};
      }
   }
}
