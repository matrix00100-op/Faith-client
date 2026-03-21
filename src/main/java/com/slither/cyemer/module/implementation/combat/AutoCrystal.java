package com.slither.cyemer.module.implementation.combat;

import com.slither.cyemer.mixin.KeyBindingAccessor;
import com.slither.cyemer.module.BooleanSetting;
import com.slither.cyemer.module.Category;
import com.slither.cyemer.module.Module;
import com.slither.cyemer.module.SliderSetting;
import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1511;
import net.minecraft.class_1774;
import net.minecraft.class_1799;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_239;
import net.minecraft.class_3965;
import net.minecraft.class_3966;

@Environment(EnvType.CLIENT)
public class AutoCrystal extends Module {
   private final SliderSetting delay = new SliderSetting("Delay (ms)", 100.0D, 0.0D, 500.0D, 0);
   private final BooleanSetting onlyOnRightClick = new BooleanSetting("Only On Right Click", true);
   private final BooleanSetting simClick = new BooleanSetting("SimClick", true);
   private final BooleanSetting swapToCrystal = new BooleanSetting("Swap to crystal on Right Click", true);
   private long lastActionTime = 0L;
   private static final double REACH_DISTANCE_SQUARED = 20.25D;
   private static final double ATTACK_REACH = 4.5D;

   public AutoCrystal() {
      super("AutoCrystal", "Places and breaks crystals legit", Category.COMBAT);
      this.addSetting(this.delay);
      this.addSetting(this.onlyOnRightClick);
      this.addSetting(this.simClick);
      this.addSetting(this.swapToCrystal);
   }

   public void onTick() {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null && this.mc.field_1761 != null) {
         if ((!this.onlyOnRightClick.isEnabled() || this.mc.field_1690.field_1904.method_1434()) && (double)(System.currentTimeMillis() - this.lastActionTime) >= this.delay.getValue()) {
            class_1511 crystalToBreak = this.findCrystalToBreak();
            if (crystalToBreak != null) {
               this.performBreakAction(crystalToBreak);
            } else {
               class_239 target = this.mc.field_1765;
               class_3965 hit;
               if (this.swapToCrystal.isEnabled() && this.mc.field_1690.field_1904.method_1434() && target instanceof class_3965) {
                  hit = (class_3965)target;
                  if (this.mc.field_1687.method_8320(hit.method_17777()).method_26204() == class_2246.field_10540 && !(this.mc.field_1724.method_6047().method_7909() instanceof class_1774)) {
                     int crystalSlot = this.findCrystalInHotbar();
                     if (crystalSlot != -1) {
                        this.mc.field_1724.method_31548().method_61496(crystalSlot);
                     }
                  }
               }

               if (this.mc.field_1724.method_6047().method_7909() instanceof class_1774 && target instanceof class_3965) {
                  hit = (class_3965)target;
                  if (this.mc.field_1724.method_5707(hit.method_17777().method_46558()) <= 20.25D && this.isValidCrystalBase(this.mc.field_1687.method_8320(hit.method_17777()).method_26204()) && this.mc.field_1687.method_8320(hit.method_17777().method_10084()).method_26215()) {
                     this.performPlaceAction(hit);
                  }
               }
            }
         }

      }
   }

   private void performBreakAction(class_1511 crystal) {
      KeyBindingAccessor attackKey;
      if (this.simClick.isEnabled()) {
         attackKey = (KeyBindingAccessor)this.mc.field_1690.field_1886;
         attackKey.setTimesPressed(attackKey.getTimesPressed() + 1);
      } else {
         attackKey = (KeyBindingAccessor)this.mc.field_1690.field_1886;
         attackKey.setTimesPressed(attackKey.getTimesPressed() + 1);
      }

      this.lastActionTime = System.currentTimeMillis();
   }

   private void performPlaceAction(class_3965 hitResult) {
      KeyBindingAccessor useKey;
      if (this.simClick.isEnabled()) {
         useKey = (KeyBindingAccessor)this.mc.field_1690.field_1904;
         useKey.setTimesPressed(useKey.getTimesPressed() + 1);
      } else {
         useKey = (KeyBindingAccessor)this.mc.field_1690.field_1904;
         useKey.setTimesPressed(useKey.getTimesPressed() + 1);
      }

      this.lastActionTime = System.currentTimeMillis();
   }

   private class_1511 findCrystalToBreak() {
      class_1297 entity;
      class_1511 crystal;
      if (this.simClick.isEnabled()) {
         class_239 var5 = this.mc.field_1765;
         if (var5 instanceof class_3966) {
            class_3966 ehr = (class_3966)var5;
            entity = ehr.method_17782();
            if (entity instanceof class_1511) {
               crystal = (class_1511)entity;
               if ((double)this.mc.field_1724.method_5739(crystal) <= 4.5D) {
                  return crystal;
               }
            }
         }

         return null;
      } else {
         Iterator var1 = this.mc.field_1687.method_18112().iterator();

         while(var1.hasNext()) {
            entity = (class_1297)var1.next();
            if (entity instanceof class_1511) {
               crystal = (class_1511)entity;
               if (this.mc.field_1724.method_6057(entity) && (double)this.mc.field_1724.method_5739(entity) < 4.5D) {
                  return crystal;
               }
            }
         }

         return null;
      }
   }

   private int findCrystalInHotbar() {
      for(int i = 0; i < 9; ++i) {
         class_1799 stack = this.mc.field_1724.method_31548().method_5438(i);
         if (stack.method_7909() instanceof class_1774) {
            return i;
         }
      }

      return -1;
   }

   private boolean isValidCrystalBase(class_2248 block) {
      return block == class_2246.field_10540 || block == class_2246.field_9987;
   }
}
