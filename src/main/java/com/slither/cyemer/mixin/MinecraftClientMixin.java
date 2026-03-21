package com.slither.cyemer.mixin;

import com.slither.cyemer.Faith;
import com.slither.cyemer.friend.FriendHelper;
import com.slither.cyemer.friend.FriendKeybindManager;
import com.slither.cyemer.module.Module;
import com.slither.cyemer.module.implementation.ClickGUIModule;
import com.slither.cyemer.module.implementation.MaceSwap;
import com.slither.cyemer.module.implementation.Prevent;
import com.slither.cyemer.util.Renderer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1041;
import net.minecraft.class_310;
import net.minecraft.class_408;
import net.minecraft.class_437;
import net.minecraft.class_746;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({class_310.class})
public abstract class MinecraftClientMixin {
   private final Map<Integer, Boolean> wasPressedMap = new HashMap();
   @Shadow
   @Nullable
   public class_746 field_1724;
   @Shadow
   public class_437 field_1755;

   @Shadow
   public abstract class_1041 method_22683();

   @Inject(
      method = {"method_1574()V"},
      at = {@At("HEAD")}
   )
   private void onTick(CallbackInfo ci) {
      if (!Faith.selfDestructed) {
         class_310 client = (class_310)this;
         if (client.field_1724 != null) {
            Faith.getInstance().getModuleManager().onTick();
            boolean allowKeybinds = client.field_1755 == null || this.shouldHandleKeybindsInScreen(client.field_1755);
            if (allowKeybinds) {
               Iterator var4 = Faith.getInstance().getModuleManager().getModules().iterator();

               int key;
               while(var4.hasNext()) {
                  Module module = (Module)var4.next();
                  key = module.getKeyCode();
                  if (key == -1 && module instanceof ClickGUIModule) {
                     key = 344;
                  }

                  if (key != -1) {
                     boolean isPressed;
                     if (key < 0) {
                        int button = key + 100;
                        isPressed = GLFW.glfwGetMouseButton(client.method_22683().method_4490(), button) == 1;
                     } else {
                        isPressed = GLFW.glfwGetKey(client.method_22683().method_4490(), key) == 1;
                     }

                     if (isPressed && !(Boolean)this.wasPressedMap.getOrDefault(key, false)) {
                        module.toggle();
                     }

                     this.wasPressedMap.put(key, isPressed);
                  }
               }

               int friendKey = FriendKeybindManager.getInstance().getKeyCode();
               if (friendKey != -1) {
                  boolean friendKeyPressed;
                  if (friendKey < 0) {
                     key = friendKey + 100;
                     friendKeyPressed = GLFW.glfwGetMouseButton(client.method_22683().method_4490(), key) == 1;
                  } else {
                     friendKeyPressed = GLFW.glfwGetKey(client.method_22683().method_4490(), friendKey) == 1;
                  }

                  if (friendKeyPressed && !(Boolean)this.wasPressedMap.getOrDefault(friendKey, false)) {
                     FriendHelper.toggleTargetedPlayer();
                  }

                  this.wasPressedMap.put(friendKey, friendKeyPressed);
               }
            } else if (!this.wasPressedMap.isEmpty()) {
               this.wasPressedMap.clear();
            }

         }
      }
   }

   private boolean shouldHandleKeybindsInScreen(class_437 screen) {
      return !(screen instanceof class_408);
   }

   @Inject(
      method = {"method_1536()Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onDoAttack(CallbackInfoReturnable<Boolean> cir) {
      if (!Faith.selfDestructed) {
         MaceSwap maceSwapModule = (MaceSwap)Faith.getInstance().getModuleManager().getModule("MaceSwap");
         if (maceSwapModule != null && maceSwapModule.isEnabled() && maceSwapModule.handleAttack()) {
            cir.setReturnValue(false);
         }
      }
   }

   @Inject(
      method = {"close()V"},
      at = {@At("HEAD")}
   )
   private void onClose(CallbackInfo ci) {
      Renderer.get().cleanup();
   }

   @Inject(
      method = {"method_20539(Z)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onOpenGameMenu(boolean pauseOnly, CallbackInfo ci) {
      if (!Faith.selfDestructed) {
         if (this.field_1755 == null) {
            Prevent preventModule = (Prevent)Faith.getInstance().getModuleManager().getModule("Prevent");
            if (preventModule != null && preventModule.shouldPreventEscape()) {
               ci.cancel();
            }

         }
      }
   }

   @Inject(
      method = {"method_15995(Z)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onWindowFocusChanged(boolean focused, CallbackInfo ci) {
      if (!Faith.selfDestructed) {
         Prevent preventModule = (Prevent)Faith.getInstance().getModuleManager().getModule("Prevent");
         if (preventModule != null && preventModule.shouldPreventTabOut()) {
            if (!focused) {
               ci.cancel();
            } else {
               long handle = this.method_22683().method_4490();
               int width = this.method_22683().method_4480();
               int height = this.method_22683().method_4507();
               GLFW.glfwSetCursorPos(handle, (double)width / 2.0D, (double)height / 2.0D);
            }
         }

      }
   }
}
