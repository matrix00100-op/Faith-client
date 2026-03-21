package com.slither.cyemer.module.implementation.combat;

import com.slither.cyemer.Faith;
import com.slither.cyemer.event.EventBus;
import com.slither.cyemer.event.EventTarget;
import com.slither.cyemer.event.impl.AttackEvent;
import com.slither.cyemer.event.impl.TriggerBotRequestEvent;
import com.slither.cyemer.manager.TargetManager;
import com.slither.cyemer.mixin.KeyBindingAccessor;
import com.slither.cyemer.module.BooleanSetting;
import com.slither.cyemer.module.Category;
import com.slither.cyemer.module.ModeSetting;
import com.slither.cyemer.module.Module;
import com.slither.cyemer.module.SliderSetting;
import com.slither.cyemer.util.ModuleRandomDelay;
import com.slither.cyemer.util.RotationManager;
import com.slither.cyemer.util.combat.CombatAgent;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1743;
import net.minecraft.class_1799;
import net.minecraft.class_243;
import net.minecraft.class_304;
import net.minecraft.class_3489;
import net.minecraft.class_3532;
import net.minecraft.class_3675;

@Environment(EnvType.CLIENT)
public class AutoSword extends Module {
   private final ModeSetting attackStyle = new ModeSetting("Style", new String[]{"Crit", "Sweep"});
   private final ModeSetting playstyle = new ModeSetting("Playstyle", new String[]{"Aggressive", "Defensive"});
   private final SliderSetting activationRange = new SliderSetting("Distance", 3.5D, 3.0D, 6.0D, 1);
   private final BooleanSetting spacing = new BooleanSetting("Spacing", true);
   private final SliderSetting aimSpeed = new SliderSetting("Aim Speed", 5.0D, 1.0D, 20.0D, 1);
   private final SliderSetting aimRandom = new SliderSetting("Aim Random", 0.35D, 0.0D, 1.0D, 2);
   private final SliderSetting spacingDist = new SliderSetting("Spacing Dist", 2.5D, 1.0D, 3.0D, 1);
   private final SliderSetting techChance = new SliderSetting("Tech Chance %", 65.0D, 0.0D, 100.0D, 0);
   private final BooleanSetting randomization = new BooleanSetting("Randomization", false);
   private final SliderSetting randomMinDelay = new SliderSetting("Random Min (ms)", 0.0D, 0.0D, 500.0D, 0);
   private final SliderSetting randomMaxDelay = new SliderSetting("Random Max (ms)", 25.0D, 0.0D, 500.0D, 0);
   private final BooleanSetting activateTriggerBot = new BooleanSetting("Activate TriggerBot", false);
   private final BooleanSetting targetMode = new BooleanSetting("Target Mode", false);
   private final BooleanSetting legitAim = new BooleanSetting("Legit Aim", false);
   private final CombatAgent agent = new CombatAgent("autosword", 24, 96, 48, 7);
   private class_1309 currentTarget;
   private double[] lastState;
   private int lastAction;
   private int tickCounter = 0;
   private int strafeTimer = 0;
   private int techTicks = 0;
   private boolean strafeDirection = false;
   private boolean isJumpResetting = false;
   private boolean initialJumpDone = false;
   private boolean isEscaping = false;
   private int myComboCount = 0;
   private int enemyComboCount = 0;
   private boolean wasHurtLastTick = false;
   private boolean didHitLastTick = false;
   private int aggressiveBackTimer = 0;
   private int aggressiveBackCooldown = 0;
   private boolean botForward = false;
   private boolean botBack = false;
   private boolean botLeft = false;
   private boolean botRight = false;
   private boolean botJump = false;
   private class_243 prevEnemyPos;
   private float prevEnemyYaw;
   private float prevEnemyPitch;
   private double opponentAimConsistency;
   private double opponentTrackingScore;
   private static final int STRATEGY_AGGRESSIVE = 0;
   private static final int STRATEGY_DEFENSIVE = 1;
   private static final int STRATEGY_STRAFE = 2;
   private static final int STRATEGY_JUMP_CRIT = 3;
   private static final int STRATEGY_W_TAP = 4;
   private static final int STRATEGY_IDLE = 5;
   private static final int STRATEGY_S_TAP = 6;
   private boolean triggerBotEnabledByAutoSword;

   public AutoSword() {
      super("AutoSword", "Most Advanced Sword bot", Category.COMBAT);
      this.prevEnemyPos = class_243.field_1353;
      this.prevEnemyYaw = 0.0F;
      this.prevEnemyPitch = 0.0F;
      this.opponentAimConsistency = 0.0D;
      this.opponentTrackingScore = 0.0D;
      this.triggerBotEnabledByAutoSword = false;
      this.addSetting(this.attackStyle);
      this.addSetting(this.playstyle);
      this.addSetting(this.activationRange);
      this.addSetting(this.spacing);
      this.addSetting(this.aimSpeed);
      this.addSetting(this.aimRandom);
      this.addSetting(this.spacingDist);
      this.addSetting(this.techChance);
      this.addSetting(this.randomization);
      this.addSetting(this.randomMinDelay);
      this.addSetting(this.randomMaxDelay);
      this.addSetting(this.activateTriggerBot);
      this.addSetting(this.targetMode);
      this.addSetting(this.legitAim);
   }

   public void onEnable() {
      EventBus.register(this);
      this.triggerBotEnabledByAutoSword = false;
      this.lastState = null;
      this.strafeDirection = Math.random() > 0.5D;
      this.isJumpResetting = false;
      this.initialJumpDone = false;
      this.isEscaping = false;
      this.myComboCount = 0;
      this.enemyComboCount = 0;
      if (this.activateTriggerBot.isEnabled()) {
         this.enableTriggerBotModule();
      }

      this.resetBotIntents();
   }

   public void onDisable() {
      EventBus.unregister(this);
      this.agent.saveData();
      RotationManager.clearTarget(this);
      this.clearPressGate("input.forward.autosword");
      this.clearPressGate("input.back.autosword");
      this.clearPressGate("input.left.autosword");
      this.clearPressGate("input.right.autosword");
      this.clearPressGate("input.jump.autosword");
      this.disableTriggerBotModuleIfNeeded();
      this.resetBotIntents();
      this.applyInputs();
   }

   public void onTick() {
      this.syncTriggerBotActivation();
      this.resetBotIntents();
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         if (this.mc.field_1755 != null) {
            RotationManager.clearTarget(this);
            this.releaseAllKeys();
         } else {
            this.updateComboStats();
            if (!this.isHoldingWeapon()) {
               RotationManager.clearTarget(this);
               this.applyInputs();
            } else if (this.mc.field_1724.method_6115()) {
               RotationManager.clearTarget(this);
               this.applyInputs();
            } else {
               if (this.mc.field_1724.field_6235 > 0 && this.mc.field_1724.method_24828()) {
                  this.isJumpResetting = true;
                  this.botJump = true;
               } else if (this.isJumpResetting && this.mc.field_1724.method_24828() && this.mc.field_1724.field_6235 == 0) {
                  this.isJumpResetting = false;
                  this.botJump = false;
               }

               this.currentTarget = this.findTarget();
               if (this.currentTarget == null) {
                  RotationManager.clearTarget(this);
                  this.initialJumpDone = false;
                  this.applyInputs();
               } else {
                  this.updateOpponentTrends();
                  RotationManager.setRotationSupplier(this, RotationManager.Priority.HIGH, () -> {
                     return this.getSmartAimPos(this.currentTarget);
                  }, this.aimSpeed.getValue(), RotationManager.RotationMode.SMOOTH, this.aimRandom.getValue(), false, false);
                  this.applyFirstHitPressure();
                  boolean isComboEscaping = this.checkComboEscape();
                  if ((double)this.mc.field_1724.method_5739(this.currentTarget) <= 3.0D) {
                     if (this.gateAction("combat.attack.autosword.request")) {
                        EventBus.post(new TriggerBotRequestEvent());
                     }

                     this.handleCritJumping();
                  }

                  if (this.attackStyle.getCurrentMode().equals("Crit") && !this.initialJumpDone) {
                     this.performInitialJump();
                  }

                  if (this.mc.field_1724.field_6235 > 0 && !this.mc.field_1724.method_24828()) {
                     this.handleStrafing();
                  }

                  if (isComboEscaping) {
                     this.applyInputs();
                  } else if (this.techTicks > 0) {
                     --this.techTicks;
                     this.handleTechExecution();
                     this.applyInputs();
                  } else {
                     if (this.isUserOverrideActive()) {
                        this.learnFromUser();
                     } else if (this.tickCounter++ % 2 == 0) {
                        this.processCarpetLogic();
                     } else {
                        this.executeStrategy(this.lastAction);
                     }

                     this.applyInputs();
                  }
               }
            }
         }
      } else {
         RotationManager.clearTarget(this);
         this.applyInputs();
      }
   }

   @EventTarget
   public void onAttack(AttackEvent event) {
      this.didHitLastTick = true;
      if (this.currentTarget != null && this.mc.field_1724 != null) {
         if (!((double)this.mc.field_1724.method_5739(this.currentTarget) > this.activationRange.getValue() + 0.5D)) {
            if (Math.random() * 100.0D < this.techChance.getValue()) {
               boolean isSweep = this.attackStyle.getCurrentMode().equals("Sweep");
               if (this.lastAction == 4) {
                  this.techTicks = isSweep ? 2 : 3;
               } else if (this.lastAction == 6) {
                  this.techTicks = 2;
               }
            }

         }
      }
   }

   private void updateOpponentTrends() {
      if (this.prevEnemyPos != class_243.field_1353) {
         float yawChange = Math.abs(class_3532.method_15393(this.currentTarget.method_36454() - this.prevEnemyYaw));
         float pitchChange = Math.abs(this.currentTarget.method_36455() - this.prevEnemyPitch);
         double totalRot = (double)(yawChange + pitchChange);
         double consistency = 1.0D - Math.min(Math.abs(totalRot - this.opponentTrackingScore) / 10.0D, 1.0D);
         this.opponentAimConsistency = class_3532.method_16436(0.1D, this.opponentAimConsistency, consistency);
         this.opponentTrackingScore = class_3532.method_16436(0.1D, this.opponentTrackingScore, totalRot);
      }

      this.prevEnemyPos = this.currentTarget.method_73189();
      this.prevEnemyYaw = this.currentTarget.method_36454();
      this.prevEnemyPitch = this.currentTarget.method_36455();
   }

   private boolean isUserOverrideActive() {
      return this.isUserPressing(this.mc.field_1690.field_1894) || this.isUserPressing(this.mc.field_1690.field_1881) || this.isUserPressing(this.mc.field_1690.field_1913) || this.isUserPressing(this.mc.field_1690.field_1849) || this.isUserPressing(this.mc.field_1690.field_1903);
   }

   private void learnFromUser() {
      int userAction = 5;
      boolean fwd = this.isUserPressing(this.mc.field_1690.field_1894);
      boolean back = this.isUserPressing(this.mc.field_1690.field_1881);
      boolean left = this.isUserPressing(this.mc.field_1690.field_1913);
      boolean right = this.isUserPressing(this.mc.field_1690.field_1849);
      boolean jump = this.isUserPressing(this.mc.field_1690.field_1903);
      if (jump && fwd) {
         userAction = 3;
      } else if (fwd) {
         userAction = 0;
      } else if (back) {
         userAction = 1;
      } else if (left || right) {
         userAction = 2;
      }

      double[] currentState = this.captureState();
      double reward = this.calculateReward();
      reward += 0.5D;
      if (this.lastState != null) {
         this.agent.train(this.lastState, userAction, reward, currentState);
      }

      this.lastState = currentState;
      this.lastAction = userAction;
   }

   private void updateComboStats() {
      if (this.mc.field_1724.field_6235 > 0 && !this.wasHurtLastTick) {
         ++this.enemyComboCount;
         this.myComboCount = 0;
      }

      this.wasHurtLastTick = this.mc.field_1724.field_6235 > 0;
      if (this.didHitLastTick) {
         if (this.mc.field_1724.field_6235 == 0) {
            ++this.myComboCount;
         }

         this.enemyComboCount = 0;
         this.didHitLastTick = false;
      }

      if (this.mc.field_1724.field_6012 % 40 == 0) {
         if (this.mc.field_1724.field_6235 == 0) {
            this.enemyComboCount = 0;
         }

         if (!this.didHitLastTick) {
            this.myComboCount = 0;
         }
      }

   }

   private boolean isHoldingWeapon() {
      if (this.mc.field_1724 == null) {
         return false;
      } else {
         class_1799 stack = this.mc.field_1724.method_6047();
         if (stack.method_7960()) {
            return false;
         } else {
            return stack.method_31573(class_3489.field_42611) || stack.method_7909() instanceof class_1743;
         }
      }
   }

   private void releaseAllKeys() {
      this.clearPressGate("input.forward.autosword");
      this.clearPressGate("input.back.autosword");
      this.clearPressGate("input.left.autosword");
      this.clearPressGate("input.right.autosword");
      this.clearPressGate("input.jump.autosword");
      this.mc.field_1690.field_1894.method_23481(false);
      this.mc.field_1690.field_1881.method_23481(false);
      this.mc.field_1690.field_1913.method_23481(false);
      this.mc.field_1690.field_1849.method_23481(false);
      this.mc.field_1690.field_1903.method_23481(false);
   }

   private void applyInputs() {
      boolean userForward = this.isUserPressing(this.mc.field_1690.field_1894);
      boolean userBack = this.isUserPressing(this.mc.field_1690.field_1881);
      boolean userLeft = this.isUserPressing(this.mc.field_1690.field_1913);
      boolean userRight = this.isUserPressing(this.mc.field_1690.field_1849);
      boolean userJump = this.isUserPressing(this.mc.field_1690.field_1903);
      this.mc.field_1690.field_1894.method_23481(userForward || this.gatePress("input.forward.autosword", this.botForward && !userBack));
      this.mc.field_1690.field_1881.method_23481(userBack || this.gatePress("input.back.autosword", this.botBack && !userForward));
      this.mc.field_1690.field_1913.method_23481(userLeft || this.gatePress("input.left.autosword", this.botLeft && !userRight));
      this.mc.field_1690.field_1849.method_23481(userRight || this.gatePress("input.right.autosword", this.botRight && !userLeft));
      this.mc.field_1690.field_1903.method_23481(userJump || this.gatePress("input.jump.autosword", this.botJump));
   }

   private void resetBotIntents() {
      this.botForward = false;
      this.botBack = false;
      this.botLeft = false;
      this.botRight = false;
      this.botJump = false;
   }

   private boolean checkComboEscape() {
      if ("Aggressive".equals(this.playstyle.getCurrentMode())) {
         return false;
      } else if (this.currentTarget != null && this.enemyComboCount >= 2 && (double)this.mc.field_1724.method_5739(this.currentTarget) < this.activationRange.getValue() + 1.0D) {
         this.isEscaping = true;
         this.botJump = true;
         this.botBack = true;
         this.botForward = false;
         if (this.tickCounter % 3 == 0) {
            this.strafeDirection = !this.strafeDirection;
         }

         if (this.strafeDirection) {
            this.botRight = true;
         } else {
            this.botLeft = true;
         }

         this.performHitSelectEscapeAttempt();
         return true;
      } else {
         if (this.isEscaping) {
            this.isEscaping = false;
         }

         return false;
      }
   }

   private void applyFirstHitPressure() {
      if (this.currentTarget != null && this.mc.field_1724 != null) {
         boolean firstHitOpen = this.myComboCount == 0 && this.enemyComboCount == 0 && this.mc.field_1724.field_6235 == 0 && this.currentTarget.field_6235 == 0;
         if (firstHitOpen) {
            double distance = (double)this.mc.field_1724.method_5739(this.currentTarget);
            if (distance > 2.8D) {
               this.botForward = true;
               this.botBack = false;
            }

            if (distance <= 3.1D && this.mc.field_1724.method_7261(0.5F) >= 0.85F && this.gateAction("combat.attack.autosword.firsthit")) {
               EventBus.post(new TriggerBotRequestEvent());
            }

         }
      }
   }

   private void performHitSelectEscapeAttempt() {
      if (this.currentTarget != null && this.mc.field_1724 != null) {
         if (!((double)this.mc.field_1724.method_5739(this.currentTarget) > 3.2D)) {
            if (this.mc.field_1724.field_6235 > 0) {
               if (!(this.mc.field_1724.method_7261(0.5F) < 0.92F)) {
                  if (this.gateAction("combat.attack.autosword.hitselect")) {
                     EventBus.post(new TriggerBotRequestEvent());
                  }

               }
            }
         }
      }
   }

   private void performInitialJump() {
      if (this.mc.field_1724.method_24828() && (double)this.mc.field_1724.method_5739(this.currentTarget) < this.activationRange.getValue() + 1.0D) {
         this.botForward = true;
         this.botJump = true;
         this.initialJumpDone = true;
      }

   }

   private void handleTechExecution() {
      if (this.lastAction == 4) {
         if (this.techTicks == 1) {
            this.botForward = false;
            this.botBack = true;
         } else {
            this.botBack = false;
            this.botForward = true;
         }
      } else if (this.lastAction == 6) {
         this.botForward = false;
         this.botBack = true;
      }

   }

   private void processCarpetLogic() {
      double[] currentState = this.captureState();
      double reward = this.calculateReward();
      if (this.lastState != null) {
         this.agent.train(this.lastState, this.lastAction, reward, currentState);
      }

      int strategy = this.applyPlaystylePreference(this.agent.predict(currentState), currentState[0]);
      this.executeStrategy(strategy);
      this.lastState = currentState;
      this.lastAction = strategy;
   }

   private void executeStrategy(int strategy) {
      boolean isSweep = this.attackStyle.getCurrentMode().equals("Sweep");
      switch(strategy) {
      case 0:
         this.handleSpacing();
         break;
      case 1:
         this.botForward = false;
         this.botBack = true;
         break;
      case 2:
         this.handleSpacing();
         if (!this.legitAim.isEnabled()) {
            this.handleStrafing();
         }
         break;
      case 3:
         this.handleSpacing();
         if (!this.legitAim.isEnabled()) {
            this.handleStrafing();
         }

         if (isSweep) {
            this.botJump = false;
         } else if (this.mc.field_1724.method_24828()) {
            this.botJump = true;
         } else {
            this.botJump = false;
         }

         if (isSweep || this.mc.field_1724.field_6017 > 0.1D) {
            this.tryAttack();
         }
         break;
      case 4:
         this.handleSpacing();
         break;
      case 5:
         if ((double)this.mc.field_1724.method_5739(this.currentTarget) < this.spacingDist.getValue()) {
            this.botBack = true;
            this.botForward = false;
         } else {
            this.botForward = false;
            this.botBack = false;
         }
         break;
      case 6:
         if (this.techTicks == 0) {
            this.handleSpacing();
         }
      }

   }

   private void handleCritJumping() {
      if (this.attackStyle.getCurrentMode().equals("Crit")) {
         boolean canCrit = !this.mc.field_1724.method_24828() && this.mc.field_1724.field_6017 > 0.0D;
         if (!canCrit && !this.mc.field_1724.method_52535() && !this.mc.field_1724.method_6101() && this.mc.field_1724.method_24828()) {
            this.botJump = true;
         }
      }

   }

   private void handleSpacing() {
      if (!this.spacing.isEnabled()) {
         this.botForward = true;
         this.botBack = false;
      } else {
         double dist = (double)this.mc.field_1724.method_5739(this.currentTarget);
         double optimal = this.spacingDist.getValue();
         if (this.attackStyle.getCurrentMode().equals("Sweep")) {
            optimal -= 0.5D;
         }

         if (dist > optimal + 0.5D) {
            this.botForward = true;
            this.botBack = false;
         } else if (dist < optimal - 0.2D) {
            this.botForward = false;
            if ("Aggressive".equals(this.playstyle.getCurrentMode())) {
               if (this.aggressiveBackTimer > 0) {
                  this.botBack = true;
                  --this.aggressiveBackTimer;
               } else if (this.aggressiveBackCooldown > 0) {
                  this.botBack = false;
                  --this.aggressiveBackCooldown;
               } else {
                  this.aggressiveBackTimer = 3;
                  this.aggressiveBackCooldown = 10;
                  this.botBack = true;
               }
            } else {
               this.botBack = true;
            }
         }

      }
   }

   private void handleStrafing() {
      ++this.strafeTimer;
      if ((double)this.strafeTimer > 15.0D + Math.random() * 15.0D || this.mc.field_1724.field_5976) {
         this.strafeDirection = !this.strafeDirection;
         this.strafeTimer = 0;
      }

      if (this.strafeDirection) {
         this.botRight = true;
         this.botLeft = false;
      } else {
         this.botRight = false;
         this.botLeft = true;
      }

   }

   private boolean isUserPressing(class_304 key) {
      int code = ((KeyBindingAccessor)key).getBoundKey().method_1444();
      return class_3675.method_15987(this.mc.method_22683(), code);
   }

   private void tryAttack() {
      if (this.attackStyle.getCurrentMode().equals("Crit")) {
         boolean canCrit = !this.mc.field_1724.method_24828() && this.mc.field_1724.field_6017 > 0.0D;
         if (!canCrit && !this.mc.field_1724.method_52535() && !this.mc.field_1724.method_6101()) {
            if (this.mc.field_1724.method_24828()) {
               this.botJump = true;
            }

            return;
         }
      }

      if ((double)this.mc.field_1724.method_5739(this.currentTarget) <= 3.0D && this.gateAction("combat.attack.autosword.request")) {
         EventBus.post(new TriggerBotRequestEvent());
      }

   }

   private double[] captureState() {
      boolean enemyAir = !this.currentTarget.method_24828();
      boolean enemyMoving = Math.abs(this.currentTarget.method_18798().field_1352) > 0.05D || Math.abs(this.currentTarget.method_18798().field_1350) > 0.05D;
      class_243 predPos = this.currentTarget.method_73189().method_1019(this.currentTarget.method_18798().method_1021(10.0D));
      double distToPred = this.mc.field_1724.method_73189().method_1022(predPos);
      return new double[]{(double)this.mc.field_1724.method_5739(this.currentTarget), (double)this.mc.field_1724.method_7261(0.0F), (double)this.mc.field_1724.method_6032(), (double)this.currentTarget.method_6032(), this.mc.field_1724.method_24828() ? 1.0D : 0.0D, enemyAir ? 1.0D : 0.0D, (double)this.currentTarget.field_6235, (double)this.mc.field_1724.field_6235, this.mc.field_1724.method_18798().field_1351, this.isTargetFacingUs(this.currentTarget) ? 1.0D : 0.0D, this.mc.field_1724.field_5976 ? 1.0D : 0.0D, this.mc.field_1724.method_6039() ? 1.0D : 0.0D, this.mc.field_1724.method_6047().method_7909() instanceof class_1743 ? 1.0D : 0.0D, this.mc.field_1724.method_6047().method_31573(class_3489.field_42611) ? 1.0D : 0.0D, (double)this.myComboCount, (double)this.enemyComboCount, enemyMoving ? 1.0D : 0.0D, this.currentTarget.method_18798().field_1351, enemyAir && enemyMoving ? 1.0D : 0.0D, this.currentTarget.field_6235 > 0 && enemyAir ? 1.0D : 0.0D, this.opponentAimConsistency, this.opponentTrackingScore, distToPred, this.isUserOverrideActive() ? 1.0D : 0.0D};
   }

   private double calculateReward() {
      double reward = -0.05D;
      if (this.currentTarget.field_6235 > 0 && this.currentTarget.field_6235 <= 2) {
         ++reward;
      }

      if (this.mc.field_1724.field_6235 > 0 && this.mc.field_1724.field_6235 <= 2) {
         --reward;
      }

      if (this.myComboCount >= 2) {
         reward += 4.0D;
      }

      if (this.enemyComboCount >= 2) {
         reward -= 5.0D;
      }

      double idealDist = this.spacingDist.getValue();
      if (this.attackStyle.getCurrentMode().equals("Sweep")) {
         idealDist -= 0.5D;
      }

      double currentDist = (double)this.mc.field_1724.method_5739(this.currentTarget);
      if (Math.abs(currentDist - idealDist) < 0.5D) {
         reward += 0.5D;
      }

      if (this.attackStyle.getCurrentMode().equals("Crit")) {
         if (!this.mc.field_1724.method_24828() && this.currentTarget.field_6235 > 0) {
            ++reward;
         }
      } else if (this.mc.field_1724.method_24828() && this.currentTarget.field_6235 > 0) {
         reward += 0.5D;
      }

      reward += this.getPlaystyleRewardModifier(currentDist);
      return reward;
   }

   private int applyPlaystylePreference(int predictedStrategy, double currentDistance) {
      boolean aggressiveMode = "Aggressive".equals(this.playstyle.getCurrentMode());
      double preferChance = 0.75D;
      if (aggressiveMode) {
         if ((predictedStrategy == 1 || predictedStrategy == 5 || predictedStrategy == 6) && Math.random() < preferChance) {
            return currentDistance > this.spacingDist.getValue() + 0.35D ? 0 : 4;
         } else {
            return predictedStrategy;
         }
      } else if ((predictedStrategy == 0 || predictedStrategy == 4 || predictedStrategy == 3) && Math.random() < preferChance) {
         return currentDistance < this.spacingDist.getValue() + 0.5D ? 1 : 2;
      } else {
         return predictedStrategy;
      }
   }

   private double getPlaystyleRewardModifier(double currentDistance) {
      if ("Aggressive".equals(this.playstyle.getCurrentMode())) {
         return Math.max(0.0D, 3.0D - currentDistance) * 0.35D;
      } else {
         double spacingReward = Math.max(0.0D, currentDistance - 2.3D) * 0.25D;
         double avoidDamageBonus = this.mc.field_1724.field_6235 == 0 ? 0.15D : -0.15D;
         return spacingReward + avoidDamageBonus;
      }
   }

   private class_1309 findTarget() {
      List<class_1309> targets = (List)StreamSupport.stream(this.mc.field_1687.method_18112().spliterator(), false).filter((e) -> {
         return e instanceof class_1309;
      }).map((e) -> {
         return (class_1309)e;
      }).filter((e) -> {
         return e != this.mc.field_1724;
      }).filter((e) -> {
         return e.method_5805();
      }).filter((e) -> {
         return e instanceof class_1657;
      }).filter((e) -> {
         return (double)this.mc.field_1724.method_5739(e) <= this.activationRange.getValue() + 2.0D;
      }).filter((e) -> {
         return !this.targetMode.isEnabled() || TargetManager.isLocked(e);
      }).sorted(Comparator.comparingDouble((e) -> {
         return (double)this.mc.field_1724.method_5739(e);
      })).collect(Collectors.toList());
      return targets.isEmpty() ? null : (class_1309)targets.get(0);
   }

   private class_243 getSmartAimPos(class_1309 target) {
      return target.method_5829().method_61125().method_1031(0.0D, (double)target.method_17682() * 0.75D, 0.0D).method_1031((double)target.method_17681() / 2.0D, 0.0D, (double)target.method_17681() / 2.0D);
   }

   private boolean isTargetFacingUs(class_1309 target) {
      class_243 toUs = this.mc.field_1724.method_73189().method_1020(target.method_73189()).method_1029();
      class_243 look = target.method_5828(1.0F).method_1029();
      return toUs.method_1026(look) > 0.5D;
   }

   private long getRandomMinDelay() {
      return (long)this.randomMinDelay.getValue();
   }

   private long getRandomMaxDelay() {
      return (long)this.randomMaxDelay.getValue();
   }

   private boolean gateAction(String key) {
      return !this.randomization.isEnabled() ? true : ModuleRandomDelay.gateAction(key, this.getRandomMinDelay(), this.getRandomMaxDelay());
   }

   private boolean gatePress(String key, boolean desiredState) {
      return !this.randomization.isEnabled() ? desiredState : ModuleRandomDelay.gatePress(key, desiredState, this.getRandomMinDelay(), this.getRandomMaxDelay());
   }

   private void clearPressGate(String key) {
      if (this.randomization.isEnabled()) {
         ModuleRandomDelay.gatePress(key, false, this.getRandomMinDelay(), this.getRandomMaxDelay());
      }

   }

   private void enableTriggerBotModule() {
      Faith instance = Faith.getInstance();
      if (instance != null && instance.getModuleManager() != null) {
         Module triggerBotModule = instance.getModuleManager().getModule("TriggerBot");
         if (triggerBotModule != null) {
            if (!triggerBotModule.isEnabled()) {
               triggerBotModule.setEnabled(true);
               this.triggerBotEnabledByAutoSword = true;
            }

         }
      }
   }

   private void disableTriggerBotModuleIfNeeded() {
      if (this.triggerBotEnabledByAutoSword) {
         Faith instance = Faith.getInstance();
         if (instance != null && instance.getModuleManager() != null) {
            Module triggerBotModule = instance.getModuleManager().getModule("TriggerBot");
            if (triggerBotModule != null && triggerBotModule.isEnabled()) {
               triggerBotModule.setEnabled(false);
            }

            this.triggerBotEnabledByAutoSword = false;
         } else {
            this.triggerBotEnabledByAutoSword = false;
         }
      }
   }

   private void syncTriggerBotActivation() {
      if (this.activateTriggerBot.isEnabled()) {
         this.enableTriggerBotModule();
      } else {
         this.disableTriggerBotModuleIfNeeded();
      }

   }
}
