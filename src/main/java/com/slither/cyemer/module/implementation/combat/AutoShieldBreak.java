package com.slither.cyemer.module.implementation.combat;

import com.slither.cyemer.event.EventBus;
import com.slither.cyemer.event.impl.ShieldDrainEvent;
import com.slither.cyemer.friend.FriendManager;
import com.slither.cyemer.manager.TargetManager;
import com.slither.cyemer.mixin.PlayerInventoryAccessor;
import com.slither.cyemer.module.BooleanSetting;
import com.slither.cyemer.module.Category;
import com.slither.cyemer.module.Module;
import com.slither.cyemer.module.SliderSetting;
import com.slither.cyemer.util.AttackValidator;
import com.slither.cyemer.util.Renderer;
import java.awt.Color;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1743;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_332;
import net.minecraft.class_3489;
import net.minecraft.class_3966;
import net.minecraft.class_4184;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

@Environment(EnvType.CLIENT)
public class AutoShieldBreak extends Module {
   public static AutoShieldBreak INSTANCE;
   private final BooleanSetting randomization = new BooleanSetting("Randomization", false);
   private final SliderSetting minHoldTime = new SliderSetting("Min Hold (ms)", 0.0D, 0.0D, 1000.0D, 0);
   private final SliderSetting maxHoldTime = new SliderSetting("Max Hold (ms)", 0.0D, 0.0D, 1000.0D, 0);
   private final SliderSetting minBreakDelay = new SliderSetting("Min Break Delay", 2.0D, 0.0D, 10.0D, 0);
   private final SliderSetting maxBreakDelay = new SliderSetting("Max Break Delay", 2.0D, 0.0D, 10.0D, 0);
   private final SliderSetting minSwapDelay = new SliderSetting("Min Swap (Ticks)", 4.0D, 0.0D, 20.0D, 0);
   private final SliderSetting maxSwapDelay = new SliderSetting("Max Swap (Ticks)", 4.0D, 0.0D, 20.0D, 0);
   private final SliderSetting globalCooldown = new SliderSetting("Spam Prevent (ms)", 300.0D, 100.0D, 2000.0D, 0);
   private final SliderSetting minHitboxAccuracy = new SliderSetting("Hitbox Accuracy %", 5.0D, 0.0D, 99.0D, 0);
   private final BooleanSetting autoSwitch = new BooleanSetting("Auto Switch", true);
   private final BooleanSetting doubleClick = new BooleanSetting("Double Click", false);
   private final BooleanSetting onlyWeapons = new BooleanSetting("Only Weapons", false);
   private final BooleanSetting showTimeText = new BooleanSetting("Show Text", true);
   private final BooleanSetting ignoreFriends = new BooleanSetting("Ignore Friends", true);
   private final BooleanSetting targetMode = new BooleanSetting("Target Mode", false);
   private final Map<UUID, Long> disabledShields = new ConcurrentHashMap();
   private final Map<UUID, Long> shieldHoldStartTimes = new ConcurrentHashMap();
   private final Map<UUID, Long> shieldHoldRequirements = new ConcurrentHashMap();
   private final Map<UUID, AutoShieldBreak.BarRenderData> barsToRender = new ConcurrentHashMap();
   private static final int SHIELD_DISABLE_TICKS = 100;
   private static final double VISUAL_RANGE_SQ = 49.0D;
   private static final double ATTACK_RANGE = 3.0D;
   private long lastAttackTime = 0L;
   private long lastItemUseTime = 0L;
   private boolean wasGuiOpen = false;
   private long lastGuiCloseTime = 0L;
   private AutoShieldBreak.BreakState currentState;
   private int stateTimer;
   private int currentBreakDelayTarget;
   private int currentSwapKeepTarget;
   private int originalSlot;
   private class_1657 currentTarget;

   public AutoShieldBreak() {
      super("AutoShieldBreak", "Automatically breaks shields of the player you look at", Category.COMBAT);
      this.currentState = AutoShieldBreak.BreakState.SCANNING;
      this.stateTimer = 0;
      this.currentBreakDelayTarget = 0;
      this.currentSwapKeepTarget = 0;
      this.originalSlot = -1;
      this.currentTarget = null;
      INSTANCE = this;
      this.addSetting(this.randomization);
      this.addSetting(this.minHoldTime);
      this.addSetting(this.maxHoldTime);
      this.addSetting(this.minBreakDelay);
      this.addSetting(this.maxBreakDelay);
      this.addSetting(this.minSwapDelay);
      this.addSetting(this.maxSwapDelay);
      this.addSetting(this.globalCooldown);
      this.addSetting(this.minHitboxAccuracy);
      this.addSetting(this.autoSwitch);
      this.addSetting(this.doubleClick);
      this.addSetting(this.onlyWeapons);
      this.addSetting(this.showTimeText);
      this.addSetting(this.ignoreFriends);
      this.addSetting(this.targetMode);
      HudRenderCallback.EVENT.register((context, renderTickCounter) -> {
         if (!this.isEnabled()) {
            this.barsToRender.clear();
         } else {
            float tickDelta = renderTickCounter.method_60637(false);
            this.renderBarsNVG(context, tickDelta);
         }
      });
   }

   public boolean isInBreakSequence() {
      return this.currentState == AutoShieldBreak.BreakState.WAITING_TO_BREAK || this.currentState == AutoShieldBreak.BreakState.RECOVERING;
   }

   public void breakConfirmed(class_1657 player) {
      if (this.mc.field_1687 != null && player != null) {
         long currentTime = this.mc.field_1687.method_75260();
         long recoveryTick = currentTime + 100L;
         this.disabledShields.put(player.method_5667(), recoveryTick);
         this.shieldHoldStartTimes.remove(player.method_5667());
      }
   }

   public void onEnable() {
      this.wasGuiOpen = this.mc.field_1755 != null;
   }

   public void onTick() {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         ShieldDrainEvent drainEvent = new ShieldDrainEvent();
         EventBus.post(drainEvent);
         if (!drainEvent.isActive()) {
            if (this.mc.field_1755 != null) {
               this.wasGuiOpen = true;
               this.resetState();
            } else {
               if (this.wasGuiOpen) {
                  this.lastGuiCloseTime = System.currentTimeMillis();
                  this.wasGuiOpen = false;
               }

               if (System.currentTimeMillis() - this.lastGuiCloseTime < 100L) {
                  this.resetState();
               } else if (this.mc.field_1724.method_6115()) {
                  this.lastItemUseTime = System.currentTimeMillis();
                  this.resetState();
               } else if (System.currentTimeMillis() - this.lastItemUseTime < 1L) {
                  this.resetState();
               } else {
                  long currentTime = this.mc.field_1687.method_75260();
                  this.disabledShields.entrySet().removeIf((entry) -> {
                     return currentTime >= (Long)entry.getValue();
                  });
                  switch(this.currentState.ordinal()) {
                  case 0:
                     this.scanForTarget();
                     break;
                  case 1:
                     this.handleWaitingToBreak();
                     break;
                  case 2:
                     this.handleRecovering();
                  }

               }
            }
         }
      }
   }

   private void handleWaitingToBreak() {
      ++this.stateTimer;
      if (this.currentTarget != null && this.currentTarget.method_5805() && this.currentTarget.method_6039()) {
         int axeSlot = this.findAxe();
         if (axeSlot == -1) {
            this.resetState();
         } else {
            if (this.mc.field_1724.method_31548().method_67532() != axeSlot) {
               if (!this.autoSwitch.isEnabled()) {
                  this.resetState();
                  return;
               }

               ((PlayerInventoryAccessor)this.mc.field_1724.method_31548()).setSelectedSlot(axeSlot);
            }

            if (this.stateTimer >= this.currentBreakDelayTarget) {
               if (AttackValidator.tryAttack(this.mc, "combat.attack.autoshieldbreak")) {
                  this.lastAttackTime = System.currentTimeMillis();
                  this.shieldHoldStartTimes.remove(this.currentTarget.method_5667());
                  if (this.doubleClick.isEnabled()) {
                     AttackValidator.tryAttack(this.mc, "combat.attack.autoshieldbreak.double");
                  }
               }

               this.currentState = AutoShieldBreak.BreakState.RECOVERING;
               this.stateTimer = 0;
            }

         }
      } else {
         this.resetState();
      }
   }

   private void handleRecovering() {
      ++this.stateTimer;
      if (this.stateTimer >= this.currentSwapKeepTarget) {
         this.resetState();
      }

   }

   private void scanForTarget() {
      if (!this.mc.field_1724.method_6115()) {
         class_1799 mainHandStack = this.mc.field_1724.method_6047();
         if (!this.onlyWeapons.isEnabled() || this.isHoldingWeapon(mainHandStack)) {
            class_239 var3 = this.mc.field_1765;
            if (var3 instanceof class_3966) {
               class_3966 hitResult = (class_3966)var3;
               class_1297 var4 = hitResult.method_17782();
               if (var4 instanceof class_1657) {
                  class_1657 target = (class_1657)var4;
                  if (target != this.mc.field_1724 && target.method_5805()) {
                     if (this.ignoreFriends.isEnabled() && FriendManager.getInstance().isFriend(target.method_5667())) {
                        this.shieldHoldStartTimes.remove(target.method_5667());
                     } else if (this.targetMode.isEnabled() && !TargetManager.isLocked(target)) {
                        this.shieldHoldStartTimes.remove(target.method_5667());
                     } else if (this.disabledShields.containsKey(target.method_5667())) {
                        this.shieldHoldStartTimes.remove(target.method_5667());
                     } else {
                        double distSq = this.mc.field_1724.method_5858(target);
                        if (!(distSq > 9.0D)) {
                           if (this.isHitboxAccurate(target, hitResult)) {
                              if (!target.method_6039()) {
                                 this.shieldHoldStartTimes.remove(target.method_5667());
                              } else {
                                 class_243 dirToMe = this.mc.field_1724.method_73189().method_1020(target.method_73189()).method_1029();
                                 class_243 targetLook = target.method_5828(1.0F).method_1029();
                                 if (dirToMe.method_1026(targetLook) <= -0.6D) {
                                    this.shieldHoldStartTimes.remove(target.method_5667());
                                 } else {
                                    long currentMs = System.currentTimeMillis();
                                    long requirementMs;
                                    if (!this.shieldHoldStartTimes.containsKey(target.method_5667())) {
                                       this.shieldHoldStartTimes.put(target.method_5667(), currentMs);
                                       requirementMs = (long)this.maxHoldTime.getValue();
                                       if (this.randomization.isEnabled()) {
                                          double minH = this.minHoldTime.getValue();
                                          double maxH = this.maxHoldTime.getValue();
                                          if (minH >= maxH) {
                                             requirementMs = (long)maxH;
                                          } else {
                                             double mean = (minH + maxH) / 2.0D;
                                             double stdDev = (maxH - minH) / 4.0D;
                                             Random r = new Random();
                                             double val = mean + r.nextGaussian() * stdDev;
                                             if (r.nextDouble() < 0.05D) {
                                                val += (maxH - minH) * 0.5D;
                                             }

                                             if (val < minH) {
                                                val = minH;
                                             }

                                             if (val > maxH * 1.5D) {
                                                val = maxH * 1.5D;
                                             }

                                             requirementMs = (long)val;
                                          }
                                       }

                                       this.shieldHoldRequirements.put(target.method_5667(), requirementMs);
                                    }

                                    requirementMs = currentMs - (Long)this.shieldHoldStartTimes.get(target.method_5667());
                                    long targetHoldTime = (Long)this.shieldHoldRequirements.getOrDefault(target.method_5667(), (long)this.maxHoldTime.getValue());
                                    if (requirementMs >= targetHoldTime) {
                                       if (!((double)(currentMs - this.lastAttackTime) < this.globalCooldown.getValue())) {
                                          if (!(this.mc.field_1724.method_7261(0.0F) < 0.1F)) {
                                             this.startBreakSequence(target);
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void startBreakSequence(class_1657 target) {
      int axeSlot = this.findAxe();
      if (axeSlot != -1) {
         this.originalSlot = ((PlayerInventoryAccessor)this.mc.field_1724.method_31548()).getSelectedSlot();
         this.currentTarget = target;
         if (this.autoSwitch.isEnabled()) {
            ((PlayerInventoryAccessor)this.mc.field_1724.method_31548()).setSelectedSlot(axeSlot);
         }

         this.currentState = AutoShieldBreak.BreakState.WAITING_TO_BREAK;
         this.stateTimer = 0;
         this.currentBreakDelayTarget = (int)this.maxBreakDelay.getValue();
         this.currentSwapKeepTarget = (int)this.maxSwapDelay.getValue();
         if (this.randomization.isEnabled()) {
            double minB = this.minBreakDelay.getValue();
            double maxB = this.maxBreakDelay.getValue();
            double minS;
            double maxS;
            if (minB < maxB) {
               minS = (minB + maxB) / 2.0D;
               maxS = (maxB - minB) / 4.0D;
               Random r = new Random();
               double val = minS + r.nextGaussian() * maxS;
               if (r.nextDouble() < 0.05D) {
                  val += (maxB - minB) * 0.5D;
               }

               if (val < minB) {
                  val = minB;
               }

               this.currentBreakDelayTarget = (int)Math.round(val);
            }

            minS = this.minSwapDelay.getValue();
            maxS = this.maxSwapDelay.getValue();
            if (minS < maxS) {
               double mean = (minS + maxS) / 2.0D;
               double stdDev = (maxS - minS) / 4.0D;
               Random r = new Random();
               double val = mean + r.nextGaussian() * stdDev;
               if (r.nextDouble() < 0.05D) {
                  val += (maxS - minS) * 0.5D;
               }

               if (val < minS) {
                  val = minS;
               }

               this.currentSwapKeepTarget = (int)Math.round(val);
            }
         }

      }
   }

   private void resetState() {
      if (this.originalSlot != -1 && this.autoSwitch.isEnabled()) {
         ((PlayerInventoryAccessor)this.mc.field_1724.method_31548()).setSelectedSlot(this.originalSlot);
      }

      this.originalSlot = -1;
      this.currentTarget = null;
      this.currentState = AutoShieldBreak.BreakState.SCANNING;
      this.stateTimer = 0;
   }

   private boolean isHoldingWeapon(class_1799 stack) {
      if (stack.method_7960()) {
         return false;
      } else {
         return stack.method_7909() instanceof class_1743 || stack.method_7909() == class_1802.field_49814 || stack.method_7909() == class_1802.field_8547 || stack.method_7909().toString().contains("sword");
      }
   }

   private boolean isHitboxAccurate(class_1309 target, class_3966 entityHit) {
      class_243 hitPos = entityHit.method_17784();
      class_238 targetBox = target.method_5829();
      class_243 center = targetBox.method_1005();
      double dx = Math.abs(hitPos.field_1352 - center.field_1352);
      double dy = Math.abs(hitPos.field_1351 - center.field_1351);
      double dz = Math.abs(hitPos.field_1350 - center.field_1350);
      double halfWidth = targetBox.method_17939() / 2.0D;
      double halfHeight = targetBox.method_17940() / 2.0D;
      double halfDepth = targetBox.method_17941() / 2.0D;
      double xEdgeDistance = halfWidth - dx;
      double yEdgeDistance = halfHeight - dy;
      double zEdgeDistance = halfDepth - dz;
      double minEdgeDistance = Math.min(Math.min(xEdgeDistance, yEdgeDistance), zEdgeDistance);
      double accuracy;
      double yAccuracy;
      double zAccuracy;
      if (minEdgeDistance == yEdgeDistance) {
         yAccuracy = (1.0D - dx / halfWidth) * 100.0D;
         zAccuracy = (1.0D - dz / halfDepth) * 100.0D;
         accuracy = Math.min(yAccuracy, zAccuracy);
      } else if (minEdgeDistance == xEdgeDistance) {
         yAccuracy = (1.0D - dy / halfHeight) * 100.0D;
         zAccuracy = (1.0D - dz / halfDepth) * 100.0D;
         accuracy = Math.min(yAccuracy, zAccuracy);
      } else {
         yAccuracy = (1.0D - dx / halfWidth) * 100.0D;
         zAccuracy = (1.0D - dy / halfHeight) * 100.0D;
         accuracy = Math.min(yAccuracy, zAccuracy);
      }

      return accuracy >= this.minHitboxAccuracy.getValue();
   }

   private void renderBarsNVG(class_332 context, float tickDelta) {
      this.updateBars(tickDelta);
      if (!this.barsToRender.isEmpty()) {
         float width = (float)this.mc.method_22683().method_4486();
         float height = (float)this.mc.method_22683().method_4502();
         float pixelRatio = (float)this.mc.method_22683().method_4489() / width;
         if (Renderer.get().beginFrame(width, height, pixelRatio)) {
            Iterator var6 = this.barsToRender.values().iterator();

            while(var6.hasNext()) {
               AutoShieldBreak.BarRenderData data = (AutoShieldBreak.BarRenderData)var6.next();
               float barWidth = 50.0F;
               float barHeight = 6.0F;
               float x = (float)(data.x - (double)barWidth / 2.0D);
               float y = (float)data.y;
               Renderer.get().drawRoundedRect(context, x - 1.0F, y - 1.0F, barWidth + 2.0F, barHeight + 2.0F, 1.0F, new Color(0, 0, 0, 150));
               Renderer.get().drawRoundedRect(context, x, y, barWidth, barHeight, 1.0F, new Color(40, 40, 40, 200));
               float progressWidth = barWidth * Math.min(data.progress, 1.0F);
               if (progressWidth > 0.0F) {
                  Renderer.get().drawRoundedRect(context, x, y, progressWidth, barHeight, 1.0F, data.color);
               }

               if (this.showTimeText.isEnabled() && data.text != null) {
                  float fontSize = 12.0F;
                  float textWidth = Renderer.get().getTextWidth(data.text, fontSize);
                  Renderer.get().drawText(context, data.text, (float)data.x - textWidth / 2.0F, y + barHeight + 3.0F, fontSize, Color.WHITE, true);
               }
            }

            Renderer.get().endFrame();
         }
      }
   }

   private void updateBars(float tickDelta) {
      this.barsToRender.clear();
      if (this.mc.field_1687 != null && this.mc.field_1773 != null && this.mc.field_1724 != null) {
         class_4184 camera = this.mc.field_1773.method_19418();
         long currentMs = System.currentTimeMillis();
         long worldTime = this.mc.field_1687.method_75260();
         Iterator var7 = this.mc.field_1687.method_18456().iterator();

         while(true) {
            class_1657 player;
            class_243 screenPos;
            do {
               double distSq;
               do {
                  do {
                     do {
                        do {
                           do {
                              if (!var7.hasNext()) {
                                 return;
                              }

                              player = (class_1657)var7.next();
                           } while(player == null);
                        } while(player == this.mc.field_1724);
                     } while(!player.method_5805());
                  } while(this.ignoreFriends.isEnabled() && FriendManager.getInstance().isFriend(player.method_5667()));

                  distSq = this.mc.field_1724.method_5858(player);
               } while(distSq > 49.0D);

               double x = player.field_6014 + (player.method_23317() - player.field_6014) * (double)tickDelta;
               double y = player.field_6036 + (player.method_23318() - player.field_6036) * (double)tickDelta + (double)player.method_17682() + 0.5D;
               double z = player.field_5969 + (player.method_23321() - player.field_5969) * (double)tickDelta;
               screenPos = this.worldToScreen(new class_243(x, y, z), camera);
            } while(screenPos == null);

            boolean isShieldDisabled = this.disabledShields.containsKey(player.method_5667()) && worldTime < (Long)this.disabledShields.get(player.method_5667());
            long timeHeld;
            float requiredTime;
            float progress;
            if (isShieldDisabled) {
               timeHeld = (Long)this.disabledShields.get(player.method_5667());
               requiredTime = (float)Math.max(0L, timeHeld - worldTime);
               progress = 1.0F - requiredTime / 100.0F;
               String text = String.format("%.1fs", requiredTime / 20.0F);
               this.barsToRender.put(player.method_5667(), new AutoShieldBreak.BarRenderData(screenPos.field_1352, screenPos.field_1351, progress, text, new Color(255, 50, 50, 200)));
            } else if (this.shieldHoldStartTimes.containsKey(player.method_5667())) {
               timeHeld = currentMs - (Long)this.shieldHoldStartTimes.get(player.method_5667());
               requiredTime = Math.max(1.0F, (float)(Long)this.shieldHoldRequirements.getOrDefault(player.method_5667(), (long)this.maxHoldTime.getValue()));
               progress = Math.min(1.0F, (float)timeHeld / requiredTime);
               this.barsToRender.put(player.method_5667(), new AutoShieldBreak.BarRenderData(screenPos.field_1352, screenPos.field_1351, progress, (int)timeHeld + "ms", new Color(50, 200, 255, 200)));
            }
         }
      }
   }

   private int findAxe() {
      for(int i = 0; i < 9; ++i) {
         if (this.mc.field_1724.method_31548().method_5438(i).method_31573(class_3489.field_42612)) {
            return i;
         }
      }

      return -1;
   }

   private class_243 worldToScreen(class_243 worldPos, class_4184 camera) {
      class_243 cameraPos = camera.method_71156();
      Vector3f relativePos = worldPos.method_46409().sub((float)cameraPos.field_1352, (float)cameraPos.field_1351, (float)cameraPos.field_1350);
      Matrix4f projectionMatrix = new Matrix4f();
      float fov = (float)Math.toRadians((double)(Integer)this.mc.field_1690.method_41808().method_41753());
      float aspectRatio = (float)this.mc.method_22683().method_4489() / (float)this.mc.method_22683().method_4506();
      projectionMatrix.perspective(fov, aspectRatio, 0.05F, this.mc.field_1773.method_3193() * 16.0F);
      Matrix4f viewMatrix = new Matrix4f();
      viewMatrix.rotate((float)Math.toRadians((double)camera.method_19329()), 1.0F, 0.0F, 0.0F);
      viewMatrix.rotate((float)Math.toRadians((double)(camera.method_19330() + 180.0F)), 0.0F, 1.0F, 0.0F);
      Vector4f clipSpacePos = (new Vector4f(relativePos.x, relativePos.y, relativePos.z, 1.0F)).mul(viewMatrix).mul(projectionMatrix);
      if ((double)clipSpacePos.w <= 0.0D) {
         return null;
      } else {
         Vector3f ndcSpacePos = new Vector3f(clipSpacePos.x / clipSpacePos.w, clipSpacePos.y / clipSpacePos.w, clipSpacePos.z / clipSpacePos.w);
         if (!(Math.abs(ndcSpacePos.x) > 1.0F) && !(Math.abs(ndcSpacePos.y) > 1.0F)) {
            double screenX = ((double)ndcSpacePos.x + 1.0D) / 2.0D * (double)this.mc.method_22683().method_4486();
            double screenY = (1.0D - (double)ndcSpacePos.y) / 2.0D * (double)this.mc.method_22683().method_4502();
            return new class_243(screenX, screenY, (double)ndcSpacePos.z);
         } else {
            return null;
         }
      }
   }

   public void onDisable() {
      this.disabledShields.clear();
      this.shieldHoldStartTimes.clear();
      this.barsToRender.clear();
      this.lastAttackTime = 0L;
      this.resetState();
   }

   @Environment(EnvType.CLIENT)
   private static enum BreakState {
      SCANNING,
      WAITING_TO_BREAK,
      RECOVERING;

      // $FF: synthetic method
      private static AutoShieldBreak.BreakState[] $values() {
         return new AutoShieldBreak.BreakState[]{SCANNING, WAITING_TO_BREAK, RECOVERING};
      }
   }

   @Environment(EnvType.CLIENT)
   private static class BarRenderData {
      double x;
      double y;
      float progress;
      String text;
      Color color;

      public BarRenderData(double x, double y, float progress, String text, Color color) {
         this.x = x;
         this.y = y;
         this.progress = progress;
         this.text = text;
         this.color = color;
      }
   }
}
