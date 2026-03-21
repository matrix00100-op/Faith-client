package com.slither.cyemer.mixin;

import com.slither.cyemer.module.implementation.render.HandCham;
import com.slither.cyemer.shader.PostShaderManager;
import com.slither.cyemer.util.RotationManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_4184;
import net.minecraft.class_5912;
import net.minecraft.class_757;
import net.minecraft.class_7833;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({class_757.class})
public class GameRendererMixin {
   @Redirect(
      method = {"method_3188(Lnet/minecraft/class_9779;)V"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_4184;method_23767()Lorg/joml/Quaternionf;"
)
   )
   private Quaternionf redirectCameraRotation(class_4184 instance) {
      if (RotationManager.isSilentRotationActive() && !instance.method_19333()) {
         float visualYaw = RotationManager.getVisualYaw() + 180.0F;
         float visualPitch = RotationManager.getVisualPitch();
         Quaternionf customRotation = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
         customRotation.mul(class_7833.field_40716.rotationDegrees(-visualYaw));
         customRotation.mul(class_7833.field_40713.rotationDegrees(visualPitch));
         return customRotation;
      } else {
         return instance.method_23767();
      }
   }

   @Inject(
      method = {"method_3172(FZLorg/joml/Matrix4f;)V"},
      at = {@At("HEAD")},
      require = 0
   )
   private void onRenderHandHead(CallbackInfo ci) {
      if (HandCham.INSTANCE != null && HandCham.INSTANCE.isEnabled()) {
         HandCham.INSTANCE.prepareRender();
      }

   }

   @Inject(
      method = {"method_3172(FZLorg/joml/Matrix4f;)V"},
      at = {@At("RETURN")},
      require = 0
   )
   private void onRenderHandReturn(CallbackInfo ci) {
      if (HandCham.INSTANCE != null && HandCham.INSTANCE.isEnabled()) {
         HandCham.INSTANCE.drawRender();
      }

   }

   @Inject(
      method = {"method_34521(Lnet/minecraft/class_5912;)V"},
      at = {@At("HEAD")}
   )
   private void onPreloadProgramsStart(class_5912 factory, CallbackInfo ci) {
      try {
         PostShaderManager.getInstance().clear();
      } catch (Exception var4) {
         var4.printStackTrace();
      }

   }

   @Inject(
      method = {"method_34521(Lnet/minecraft/class_5912;)V"},
      at = {@At("TAIL")}
   )
   private void onPreloadProgramsEnd(class_5912 factory, CallbackInfo ci) {
      try {
         PostShaderManager manager = PostShaderManager.getInstance();
         manager.registerShader(PostShaderManager.Effects.BLUR);
      } catch (Exception var4) {
         var4.printStackTrace();
      }

   }
}
