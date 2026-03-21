package com.slither.cyemer.mixin;

import com.slither.cyemer.module.implementation.CustomFont;
import java.lang.StackWalker.Option;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11719;
import net.minecraft.class_2960;
import net.minecraft.class_327;
import net.minecraft.class_5348;
import net.minecraft.class_5481;
import net.minecraft.class_11719.class_11721;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({class_327.class})
public class TextRendererMixin {
   private static final StackWalker STACK_WALKER;

   private static boolean cyemer$isUiTextPath() {
      return (Boolean)STACK_WALKER.walk((frames) -> {
         return frames.limit(24L).anyMatch((frame) -> {
            String className = frame.getClassName();
            return className.startsWith("net.minecraft.client.gui.") || className.startsWith("net.minecraft.client.hud.") || className.startsWith("net.minecraft.client.toast.");
         });
      });
   }

   private static float cyemer$getGlobalScale() {
      return CustomFont.isGlobalSettingsEnabled() && cyemer$isUiTextPath() ? CustomFont.getGlobalFontScale() : 1.0F;
   }

   private static float cyemer$unscaleCoord(float value) {
      float scale = cyemer$getGlobalScale();
      return scale == 1.0F ? value : value / scale;
   }

   private static int cyemer$unscaleWidth(int value) {
      float scale = cyemer$getGlobalScale();
      return scale == 1.0F ? value : Math.max(1, (int)Math.floor((double)((float)value / scale)));
   }

   private static int cyemer$scaleWidth(int value) {
      float scale = cyemer$getGlobalScale();
      return scale == 1.0F ? value : Math.max(1, (int)Math.ceil((double)((float)value * scale)));
   }

   private static Matrix4f cyemer$scaleMatrix(Matrix4f matrix) {
      float scale = cyemer$getGlobalScale();
      return scale != 1.0F && matrix != null ? (new Matrix4f(matrix)).scale(scale, scale, 1.0F) : matrix;
   }

   @ModifyVariable(
      method = {"method_72732(Lnet/minecraft/class_11719;)Lnet/minecraft/class_11603;"},
      at = @At("HEAD"),
      argsOnly = true
   )
   private class_11719 cyemer$applyGlobalFont(class_11719 spriteSource) {
      if (CustomFont.isGlobalFontEnabled() && class_11719.field_61972.equals(spriteSource)) {
         class_2960 fontId = CustomFont.getSelectedFontId();
         return (class_11719)(fontId == null ? spriteSource : new class_11721(fontId));
      } else {
         return spriteSource;
      }
   }

   @ModifyVariable(
      method = {"method_27521(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/class_4597;Lnet/minecraft/class_327$class_6415;II)V"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 2
   )
   private float cyemer$applyGlobalScaleStringX(float x) {
      return cyemer$unscaleCoord(x);
   }

   @ModifyVariable(
      method = {"method_27521(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/class_4597;Lnet/minecraft/class_327$class_6415;II)V"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 3
   )
   private float cyemer$applyGlobalScaleStringY(float y) {
      return cyemer$unscaleCoord(y);
   }

   @ModifyVariable(
      method = {"method_27521(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/class_4597;Lnet/minecraft/class_327$class_6415;II)V"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 6
   )
   private Matrix4f cyemer$applyGlobalScaleStringMatrix(Matrix4f matrix) {
      return cyemer$scaleMatrix(matrix);
   }

   @ModifyVariable(
      method = {"method_27521(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/class_4597;Lnet/minecraft/class_327$class_6415;II)V"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 5
   )
   private boolean cyemer$applyGlobalShadowString(boolean shadow) {
      return !CustomFont.isGlobalSettingsEnabled() ? shadow : CustomFont.useGlobalShadow(shadow);
   }

   @ModifyVariable(
      method = {"method_27522(Lnet/minecraft/class_2561;FFIZLorg/joml/Matrix4f;Lnet/minecraft/class_4597;Lnet/minecraft/class_327$class_6415;II)V"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 2
   )
   private float cyemer$applyGlobalScaleTextX(float x) {
      return cyemer$unscaleCoord(x);
   }

   @ModifyVariable(
      method = {"method_27522(Lnet/minecraft/class_2561;FFIZLorg/joml/Matrix4f;Lnet/minecraft/class_4597;Lnet/minecraft/class_327$class_6415;II)V"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 3
   )
   private float cyemer$applyGlobalScaleTextY(float y) {
      return cyemer$unscaleCoord(y);
   }

   @ModifyVariable(
      method = {"method_27522(Lnet/minecraft/class_2561;FFIZLorg/joml/Matrix4f;Lnet/minecraft/class_4597;Lnet/minecraft/class_327$class_6415;II)V"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 6
   )
   private Matrix4f cyemer$applyGlobalScaleTextMatrix(Matrix4f matrix) {
      return cyemer$scaleMatrix(matrix);
   }

   @ModifyVariable(
      method = {"method_27522(Lnet/minecraft/class_2561;FFIZLorg/joml/Matrix4f;Lnet/minecraft/class_4597;Lnet/minecraft/class_327$class_6415;II)V"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 5
   )
   private boolean cyemer$applyGlobalShadowText(boolean shadow) {
      return !CustomFont.isGlobalSettingsEnabled() ? shadow : CustomFont.useGlobalShadow(shadow);
   }

   @ModifyVariable(
      method = {"method_22942(Lnet/minecraft/class_5481;FFIZLorg/joml/Matrix4f;Lnet/minecraft/class_4597;Lnet/minecraft/class_327$class_6415;II)V"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 2
   )
   private float cyemer$applyGlobalScaleOrderedTextX(float x) {
      return cyemer$unscaleCoord(x);
   }

   @ModifyVariable(
      method = {"method_22942(Lnet/minecraft/class_5481;FFIZLorg/joml/Matrix4f;Lnet/minecraft/class_4597;Lnet/minecraft/class_327$class_6415;II)V"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 3
   )
   private float cyemer$applyGlobalScaleOrderedTextY(float y) {
      return cyemer$unscaleCoord(y);
   }

   @ModifyVariable(
      method = {"method_22942(Lnet/minecraft/class_5481;FFIZLorg/joml/Matrix4f;Lnet/minecraft/class_4597;Lnet/minecraft/class_327$class_6415;II)V"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 6
   )
   private Matrix4f cyemer$applyGlobalScaleOrderedTextMatrix(Matrix4f matrix) {
      return cyemer$scaleMatrix(matrix);
   }

   @ModifyVariable(
      method = {"method_22942(Lnet/minecraft/class_5481;FFIZLorg/joml/Matrix4f;Lnet/minecraft/class_4597;Lnet/minecraft/class_327$class_6415;II)V"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 5
   )
   private boolean cyemer$applyGlobalShadowOrderedText(boolean shadow) {
      return !CustomFont.isGlobalSettingsEnabled() ? shadow : CustomFont.useGlobalShadow(shadow);
   }

   @ModifyVariable(
      method = {"method_71796(Ljava/lang/String;FFIZI)Lnet/minecraft/class_327$class_11465;"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 5
   )
   private boolean cyemer$applyGlobalShadowPrepareString(boolean shadow) {
      return !CustomFont.isGlobalSettingsEnabled() ? shadow : CustomFont.useGlobalShadow(shadow);
   }

   @ModifyVariable(
      method = {"method_71795(Lnet/minecraft/class_5481;FFIZZI)Lnet/minecraft/class_327$class_11465;"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 5
   )
   private boolean cyemer$applyGlobalShadowPrepareOrderedText(boolean shadow) {
      return !CustomFont.isGlobalSettingsEnabled() ? shadow : CustomFont.useGlobalShadow(shadow);
   }

   @ModifyVariable(
      method = {"method_37296(Lnet/minecraft/class_5481;FFIILorg/joml/Matrix4f;Lnet/minecraft/class_4597;I)V"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 2
   )
   private float cyemer$applyGlobalScaleOutlineX(float x) {
      return cyemer$unscaleCoord(x);
   }

   @ModifyVariable(
      method = {"method_37296(Lnet/minecraft/class_5481;FFIILorg/joml/Matrix4f;Lnet/minecraft/class_4597;I)V"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 3
   )
   private float cyemer$applyGlobalScaleOutlineY(float y) {
      return cyemer$unscaleCoord(y);
   }

   @ModifyVariable(
      method = {"method_37296(Lnet/minecraft/class_5481;FFIILorg/joml/Matrix4f;Lnet/minecraft/class_4597;I)V"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 6
   )
   private Matrix4f cyemer$applyGlobalScaleOutlineMatrix(Matrix4f matrix) {
      return cyemer$scaleMatrix(matrix);
   }

   @ModifyVariable(
      method = {"method_27524(Ljava/lang/String;IZ)Ljava/lang/String;"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 2
   )
   private int cyemer$applyGlobalScaleTrimToWidthBackwards(int width) {
      return cyemer$unscaleWidth(width);
   }

   @ModifyVariable(
      method = {"method_27523(Ljava/lang/String;I)Ljava/lang/String;"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 2
   )
   private int cyemer$applyGlobalScaleTrimToWidth(int width) {
      return cyemer$unscaleWidth(width);
   }

   @ModifyVariable(
      method = {"method_1714(Lnet/minecraft/class_5348;I)Lnet/minecraft/class_5348;"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 2
   )
   private int cyemer$applyGlobalScaleTrimToWidthVisitable(int width) {
      return cyemer$unscaleWidth(width);
   }

   @ModifyVariable(
      method = {"method_1728(Lnet/minecraft/class_5348;I)Ljava/util/List;"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 2
   )
   private int cyemer$applyGlobalScaleWrapLines(int width) {
      return cyemer$unscaleWidth(width);
   }

   @ModifyVariable(
      method = {"method_72101(Lnet/minecraft/class_5348;I)Ljava/util/List;"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 2
   )
   private int cyemer$applyGlobalScaleWrapLinesWithoutLanguage(int width) {
      return cyemer$unscaleWidth(width);
   }

   @ModifyVariable(
      method = {"method_44378(Lnet/minecraft/class_5348;I)I"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 2
   )
   private int cyemer$applyGlobalScaleWrappedLineInputWidth(int width) {
      return cyemer$unscaleWidth(width);
   }

   @Inject(
      method = {"method_1727(Ljava/lang/String;)I"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void cyemer$applyGlobalScaleWidthString(String text, CallbackInfoReturnable<Integer> cir) {
      cir.setReturnValue(cyemer$scaleWidth((Integer)cir.getReturnValue()));
   }

   @Inject(
      method = {"method_27525(Lnet/minecraft/class_5348;)I"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void cyemer$applyGlobalScaleWidthVisitable(class_5348 text, CallbackInfoReturnable<Integer> cir) {
      cir.setReturnValue(cyemer$scaleWidth((Integer)cir.getReturnValue()));
   }

   @Inject(
      method = {"method_30880(Lnet/minecraft/class_5481;)I"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void cyemer$applyGlobalScaleWidthOrderedText(class_5481 text, CallbackInfoReturnable<Integer> cir) {
      cir.setReturnValue(cyemer$scaleWidth((Integer)cir.getReturnValue()));
   }

   @Inject(
      method = {"method_44378(Lnet/minecraft/class_5348;I)I"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void cyemer$applyGlobalScaleWrappedLineHeight(class_5348 text, int width, CallbackInfoReturnable<Integer> cir) {
      cir.setReturnValue(cyemer$scaleWidth((Integer)cir.getReturnValue()));
   }

   static {
      STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);
   }
}
