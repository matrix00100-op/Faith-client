package com.slither.cyemer.module.implementation;

import com.slither.cyemer.module.BooleanSetting;
import com.slither.cyemer.module.Category;
import com.slither.cyemer.module.Module;
import com.slither.cyemer.module.SliderSetting;
import java.util.Iterator;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_332;
import net.minecraft.class_418;
import net.minecraft.class_465;
import net.minecraft.class_490;

@Environment(EnvType.CLIENT)
public class AutoTotem extends Module {
   private static final int OFFHAND_CRAFTING_SLOT_ID = 45;
   private static final int OFFHAND_INVENTORY_INDEX = 40;
   private static final int OPEN_DELAY_MIN = 0;
   private static final int OPEN_DELAY_RANGE = 50;
   private static final int SCAN_DELAY_MIN = 0;
   private static final int SCAN_DELAY_RANGE = 50;
   private static final int CLOSE_DELAY_MIN = 100;
   private static final int CLOSE_DELAY_RANGE = 100;
   private static final int CURSOR_BUSY_DELAY = 200;
   private static final int IDLE_COOLDOWN = 500;
   private static final int POST_CLICK_SCAN_DELAY_MIN = 60;
   private static final int POST_CLICK_SCAN_DELAY_RANGE = 60;
   private static final long DELAY_MIN_CLAMP = 1L;
   private static final long DELAY_MAX_CLAMP = 1500L;
   private final SliderSetting fastDelay = new SliderSetting("Fast Delay (ms)", 67.0D, 0.0D, 300.0D, 0);
   private final SliderSetting slowDelay = new SliderSetting("Fumble Delay (ms)", 325.0D, 0.0D, 800.0D, 0);
   private final SliderSetting fumbleChance = new SliderSetting("Fumble Chance %", 55.0D, 0.0D, 100.0D, 0);
   private final BooleanSetting autoOpen = new BooleanSetting("Auto Open Inv", true);
   private final BooleanSetting shutInventory = new BooleanSetting("Auto Close", true);
   private AutoTotem.State currentState;
   private long actionTimer;
   private class_1735 targetSlot;
   private boolean openedByBot;
   private final Random random;

   public AutoTotem() {
      super("AutoTotem", "Automatically equips totems to offhand with humanized timing", Category.COMBAT);
      this.currentState = AutoTotem.State.IDLE;
      this.actionTimer = -1L;
      this.targetSlot = null;
      this.openedByBot = false;
      this.random = new Random();
      this.addSetting(this.fastDelay);
      this.addSetting(this.slowDelay);
      this.addSetting(this.fumbleChance);
      this.addSetting(this.autoOpen);
      this.addSetting(this.shutInventory);
   }

   public void onDisable() {
      this.resetState();
   }

   public void onRender(class_332 context, float tickDelta) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null && this.mc.field_1761 != null) {
         if (!this.mc.field_1724.method_29504() && !(this.mc.field_1724.method_6032() <= 0.0F) && !(this.mc.field_1755 instanceof class_418)) {
            if (this.mc.field_1755 == null) {
               this.handlePlayingLogic();
            } else if (this.mc.field_1755 instanceof class_465) {
               this.handleInventoryLogic((class_465)this.mc.field_1755);
            } else {
               this.resetState();
            }

         } else {
            this.resetState();
         }
      } else {
         this.resetState();
      }
   }

   private void handlePlayingLogic() {
      if (this.currentState == AutoTotem.State.IDLE) {
         this.openedByBot = false;
      }

      boolean needsRefill = !this.mc.field_1724.method_6079().method_31574(class_1802.field_8288);
      if (this.autoOpen.isEnabled() && needsRefill && this.hasTotemInInventory()) {
         switch(this.currentState.ordinal()) {
         case 0:
         case 6:
         case 7:
            this.scheduleNextState(AutoTotem.State.PRE_OPENING, this.getBimodalDelay());
            break;
         case 1:
            if (System.currentTimeMillis() >= this.actionTimer) {
               if (this.mc.field_1755 == null) {
                  this.openedByBot = true;
                  this.mc.method_1507(new class_490(this.mc.field_1724));
                  this.scheduleNextState(AutoTotem.State.OPENING_WAIT, (long)(0 + this.random.nextInt(50)));
               } else {
                  this.resetState();
               }
            }
            break;
         case 2:
         case 3:
         case 4:
         case 5:
         default:
            this.resetState();
         }
      } else {
         this.currentState = AutoTotem.State.IDLE;
      }

   }

   private void handleInventoryLogic(class_465<?> screen) {
      if (!screen.method_17577().method_34255().method_7960()) {
         this.currentState = AutoTotem.State.IDLE;
         this.actionTimer = System.currentTimeMillis() + 200L;
      } else {
         switch(this.currentState.ordinal()) {
         case 0:
         case 1:
         case 2:
            this.scheduleNextState(AutoTotem.State.SCANNING, (long)(0 + this.random.nextInt(50)));
            break;
         case 3:
            if (System.currentTimeMillis() < this.actionTimer) {
               return;
            }

            if (!this.mc.field_1724.method_6079().method_31574(class_1802.field_8288)) {
               class_1735 totem = this.findTotem(screen);
               if (totem != null) {
                  this.targetSlot = totem;
                  this.scheduleNextState(AutoTotem.State.SCHEDULED, this.getBimodalDelay());
                  return;
               }
            }

            if (this.openedByBot && this.shutInventory.isEnabled()) {
               this.scheduleNextState(AutoTotem.State.CLOSING, (long)(100 + this.random.nextInt(100)));
            } else {
               this.currentState = AutoTotem.State.IDLE;
               this.actionTimer = System.currentTimeMillis() + 500L;
            }
            break;
         case 4:
            if (System.currentTimeMillis() >= this.actionTimer) {
               this.currentState = AutoTotem.State.EXECUTING;
            }
            break;
         case 5:
            this.performClick();
            this.scheduleNextState(AutoTotem.State.SCANNING, (long)(60 + this.random.nextInt(60)));
            break;
         case 6:
            this.scheduleNextState(AutoTotem.State.SCANNING, 10L);
            break;
         case 7:
            if (System.currentTimeMillis() >= this.actionTimer) {
               if (this.openedByBot) {
                  this.mc.field_1724.method_7346();
               }

               this.resetState();
            }
         }

      }
   }

   private void performClick() {
      if (this.targetSlot != null && this.mc.field_1761 != null) {
         int syncId = this.mc.field_1724.field_7512.field_7763;
         this.mc.field_1761.method_2906(syncId, this.targetSlot.field_7874, 40, class_1713.field_7791, this.mc.field_1724);
      }
   }

   private long getBimodalDelay() {
      boolean isFumble = (double)this.random.nextInt(100) < this.fumbleChance.getValue();
      long delay;
      long mean;
      if (isFumble) {
         mean = (long)this.slowDelay.getValue();
         delay = (long)((double)mean + this.random.nextGaussian() * 50.0D);
      } else {
         mean = (long)this.fastDelay.getValue();
         delay = (long)((double)mean + this.random.nextGaussian() * 20.0D);
      }

      return Math.max(1L, Math.min(delay, 1500L));
   }

   private void scheduleNextState(AutoTotem.State state, long delayMs) {
      this.currentState = state;
      this.actionTimer = System.currentTimeMillis() + delayMs;
   }

   private void resetState() {
      this.currentState = AutoTotem.State.IDLE;
      this.actionTimer = -1L;
      this.targetSlot = null;
      this.openedByBot = false;
   }

   private boolean hasTotemInInventory() {
      Iterator var1 = this.mc.field_1724.method_31548().method_67533().iterator();

      class_1799 stack;
      do {
         if (!var1.hasNext()) {
            return false;
         }

         stack = (class_1799)var1.next();
      } while(!stack.method_31574(class_1802.field_8288));

      return true;
   }

   private class_1735 findTotem(class_465<?> screen) {
      Iterator var2 = screen.method_17577().field_7761.iterator();

      class_1735 slot;
      do {
         do {
            do {
               do {
                  if (!var2.hasNext()) {
                     return null;
                  }

                  slot = (class_1735)var2.next();
               } while(!slot.method_7681());
            } while(!slot.method_7677().method_31574(class_1802.field_8288));
         } while(slot.field_7874 == 45);
      } while(slot.field_7871 == this.mc.field_1724.method_31548() && slot.method_34266() == 40);

      return slot;
   }

   @Environment(EnvType.CLIENT)
   private static enum State {
      IDLE,
      PRE_OPENING,
      OPENING_WAIT,
      SCANNING,
      SCHEDULED,
      EXECUTING,
      COOLDOWN,
      CLOSING;

      // $FF: synthetic method
      private static AutoTotem.State[] $values() {
         return new AutoTotem.State[]{IDLE, PRE_OPENING, OPENING_WAIT, SCANNING, SCHEDULED, EXECUTING, COOLDOWN, CLOSING};
      }
   }
}
