package com.slither.cyemer.module.implementation.targeteffect;

import com.slither.cyemer.shader.CoreShaderManager;
import com.slither.cyemer.util.render.types.RenderTypes;
import com.slither.cyemer.util.render.types.ShaderRenderLayers;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1921;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_9799;
import net.minecraft.class_4597.class_4598;

@Environment(EnvType.CLIENT)
public class EffectRenderer {
   private final class_9799 allocator = new class_9799(1536);
   private final EffectFactory effectFactory = new EffectFactory();
   private final CoreShaderManager shaderManager = CoreShaderManager.getInstance();
   private static final class_1921 GLOW_LAYER = ShaderRenderLayers.getShaderLayerNoDepth("fallback", true);

   public void render(class_4587 matrices, class_243 cameraPos, class_243 playerPos, EffectRenderContext ctx) {
      BaseEffect effect = this.effectFactory.getEffect(ctx.effectType);
      class_4598 vertexConsumers;
      boolean useShader;
      String shaderName;
      class_1921 mainLayer;
      class_4588 mainBuffer;
      class_1921 glowLayer;
      class_4588 glowBuffer;
      if ("Overlay".equals(ctx.effectType)) {
         vertexConsumers = class_4597.method_22991(this.allocator);
         useShader = ctx.theme.usesShader();
         shaderName = ctx.theme.getShaderName();
         if (useShader && shaderName != null) {
            this.updateShaderUniforms(shaderName, ctx);
         }

         mainLayer = useShader ? ShaderRenderLayers.getShaderLayerNoDepth(shaderName, false) : RenderTypes.TRIANGLES_NO_DEPTH;
         mainBuffer = vertexConsumers.method_73477(mainLayer);
         effect.render(matrices, mainBuffer, (class_4588)null, ctx);
         vertexConsumers.method_22993();
         if (ctx.glowEnabled) {
            glowLayer = useShader ? ShaderRenderLayers.getShaderLayerNoDepth(shaderName, true) : GLOW_LAYER;
            if (useShader && shaderName != null) {
               this.updateShaderUniforms(shaderName, ctx);
            }

            glowBuffer = vertexConsumers.method_73477(glowLayer);
            effect.render(matrices, (class_4588)null, glowBuffer, ctx);
            vertexConsumers.method_22993();
         }

      } else {
         vertexConsumers = class_4597.method_22991(this.allocator);
         matrices.method_22903();
         matrices.method_22904(playerPos.field_1352 - cameraPos.field_1352, playerPos.field_1351 - cameraPos.field_1351, playerPos.field_1350 - cameraPos.field_1350);
         useShader = ctx.theme.usesShader();
         shaderName = ctx.theme.getShaderName();
         if (useShader && shaderName != null) {
            this.updateShaderUniforms(shaderName, ctx);
         }

         mainLayer = useShader ? ShaderRenderLayers.getShaderLayerNoDepth(shaderName, false) : RenderTypes.TRIANGLES_NO_DEPTH;
         mainBuffer = vertexConsumers.method_73477(mainLayer);
         effect.render(matrices, mainBuffer, (class_4588)null, ctx);
         vertexConsumers.method_22993();
         if (ctx.glowEnabled) {
            glowLayer = useShader ? ShaderRenderLayers.getShaderLayerNoDepth(shaderName, true) : GLOW_LAYER;
            if (useShader && shaderName != null) {
               this.updateShaderUniforms(shaderName, ctx);
            }

            glowBuffer = vertexConsumers.method_73477(glowLayer);
            effect.render(matrices, (class_4588)null, glowBuffer, ctx);
            vertexConsumers.method_22993();
         }

         matrices.method_22909();
      }
   }

   private void updateShaderUniforms(String shaderName, EffectRenderContext ctx) {
      float time = (float)((double)ctx.time / 1000.0D);
      float[] colorArray = new float[]{(float)ctx.color.getRed() / 255.0F, (float)ctx.color.getGreen() / 255.0F, (float)ctx.color.getBlue() / 255.0F, ctx.alpha};
      float speed = (float)ctx.speedMultiplier;
      this.shaderManager.setShaderUniforms(class_2960.method_60655("dynamic_fps", shaderName), time, colorArray, speed);
   }

   public static int calculateLOD(double distance, int maxSegments) {
      if (distance < 16.0D) {
         return maxSegments;
      } else if (distance < 32.0D) {
         return maxSegments / 2;
      } else {
         return distance < 64.0D ? maxSegments / 3 : Math.max(8, maxSegments / 4);
      }
   }

   public static Color getRainbowColor(long time) {
      float hue = (float)(time % 3000L) / 3000.0F;
      return Color.getHSBColor(hue, 0.8F, 1.0F);
   }
}
