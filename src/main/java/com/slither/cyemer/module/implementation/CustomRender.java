package com.slither.cyemer.module.implementation;

import com.slither.cyemer.module.BooleanSetting;
import com.slither.cyemer.module.Category;
import com.slither.cyemer.module.ModeSetting;
import com.slither.cyemer.module.Module;
import com.slither.cyemer.module.SliderSetting;
import com.slither.cyemer.shader.PostShaderManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;

@Environment(EnvType.CLIENT)
public class CustomRender extends Module {
   private static CustomRender instance;
   private final ModeSetting shaderMode = new ModeSetting("Shader", new String[]{"Blur", "Invert", "Creeper", "Spider", "Rainbow", "Chromatic", "Wave", "Pixelate", "Glitch"});
   private final SliderSetting intensity = new SliderSetting("Intensity", 5.0D, 0.0D, 20.0D, 1);
   private final BooleanSetting dynamicIntensity = new BooleanSetting("Dynamic", false);
   private final SliderSetting pulseSpeed = new SliderSetting("Pulse Speed", 1.0D, 0.1D, 5.0D, 1);
   private long startTime;

   public CustomRender() {
      super("CustomRender", "Apply post-processing shaders to the game", Category.RENDER);
      this.addSetting(this.shaderMode);
      this.addSetting(this.intensity);
      this.addSetting(this.dynamicIntensity);
      this.addSetting(this.pulseSpeed);
      instance = this;
      this.startTime = System.currentTimeMillis();
   }

   public static CustomRender getInstance() {
      return instance;
   }

   public void onEnable() {
      this.startTime = System.currentTimeMillis();
   }

   public void onDisable() {
      PostShaderManager.getInstance().clearEffect();
   }

   public void onTick() {
      if (this.isEnabled() && this.mc.field_1724 != null) {
         this.updateShaderUniforms();
         this.applyCurrentShader();
      }
   }

   private void applyCurrentShader() {
      PostShaderManager manager = PostShaderManager.getInstance();
      String mode = this.shaderMode.getCurrentMode();
      byte var4 = -1;
      switch(mode.hashCode()) {
      case -2099835914:
         if (mode.equals("Invert")) {
            var4 = 2;
         }
         break;
      case -1812086011:
         if (mode.equals("Spider")) {
            var4 = 4;
         }
         break;
      case -1656737386:
         if (mode.equals("Rainbow")) {
            var4 = 5;
         }
         break;
      case -1601644210:
         if (mode.equals("Creeper")) {
            var4 = 3;
         }
         break;
      case -78399892:
         if (mode.equals("Pixelate")) {
            var4 = 8;
         }
         break;
      case 2073735:
         if (mode.equals("Blur")) {
            var4 = 1;
         }
         break;
      case 2433880:
         if (mode.equals("None")) {
            var4 = 0;
         }
         break;
      case 2688793:
         if (mode.equals("Wave")) {
            var4 = 7;
         }
         break;
      case 1400992312:
         if (mode.equals("Chromatic")) {
            var4 = 6;
         }
         break;
      case 2135652693:
         if (mode.equals("Glitch")) {
            var4 = 9;
         }
      }

      switch(var4) {
      case 0:
         manager.clearEffect();
         break;
      case 1:
         manager.applyEffect(PostShaderManager.Effects.BLUR);
         break;
      case 2:
         manager.applyEffect(PostShaderManager.Effects.INVERT);
         break;
      case 3:
         manager.applyEffect(PostShaderManager.Effects.CREEPER);
         break;
      case 4:
         manager.applyEffect(PostShaderManager.Effects.SPIDER);
         break;
      case 5:
         manager.applyEffect(class_2960.method_60655("dynamic_fps", "rainbow"));
         break;
      case 6:
         manager.applyEffect(class_2960.method_60655("dynamic_fps", "chromatic"));
         break;
      case 7:
         manager.applyEffect(class_2960.method_60655("dynamic_fps", "wave"));
         break;
      case 8:
         manager.applyEffect(class_2960.method_60655("dynamic_fps", "pixelate"));
         break;
      case 9:
         manager.applyEffect(class_2960.method_60655("dynamic_fps", "glitch"));
      }

   }

   private void updateShaderUniforms() {
      PostShaderManager manager = PostShaderManager.getInstance();
      String mode = this.shaderMode.getCurrentMode();
      float time = (float)(System.currentTimeMillis() - this.startTime) / 1000.0F;
      float intensityValue;
      float pulse;
      if (mode.equals("Blur") || mode.equals("Rainbow")) {
         intensityValue = (float)this.intensity.getValue();
         if (this.dynamicIntensity.isEnabled()) {
            pulse = (float)(Math.sin((double)time * this.pulseSpeed.getValue()) * 0.5D + 0.5D);
            intensityValue *= 0.5F + pulse * 0.5F;
         }

         manager.setUniform("Radius", intensityValue);
      }

      if (mode.equals("Chromatic") || mode.equals("Wave") || mode.equals("Pixelate") || mode.equals("Glitch")) {
         intensityValue = (float)this.intensity.getValue();
         if (this.dynamicIntensity.isEnabled()) {
            pulse = (float)(Math.sin((double)time * this.pulseSpeed.getValue()) * 0.5D + 0.5D);
            intensityValue *= 0.5F + pulse * 0.5F;
         }

         manager.setUniform("Intensity", intensityValue);
         manager.setUniform("Time", time);
      }

      if (mode.equals("Rainbow")) {
         manager.setUniform("Time", time);
      }

   }

   public double getIntensity() {
      return this.intensity.getValue();
   }

   public void setIntensity(double value) {
      this.intensity.setValue(Math.max(0.0D, Math.min(20.0D, value)));
      if (this.isEnabled()) {
         this.updateShaderUniforms();
      }

   }
}
