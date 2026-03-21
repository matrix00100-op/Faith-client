package com.slither.cyemer;

import com.slither.cyemer.hud.HUDRenderer;
import com.slither.cyemer.module.implementation.Fakelag;
import com.slither.cyemer.shader.CoreShaderManager;
import com.slither.cyemer.shader.PostShaderManager;
import com.slither.cyemer.util.LogCleaner;
import com.slither.cyemer.util.RotationManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.class_2960;
import net.minecraft.class_310;

@Environment(EnvType.CLIENT)
public class FaithClient implements ClientModInitializer {
   public void onInitializeClient() {
      this.registerCoreShaders();
      this.registerPostShaders();
      WorldRenderEvents.END_MAIN.register((context) -> {
         RotationManager.update(class_310.method_1551().method_61966().method_60637(true));
         this.renderPostEffects();
      });
      WorldRenderEvents.AFTER_ENTITIES.register((context) -> {
         Faith.getInstance().getModuleManager().onWorldRender(context.matrices(), class_310.method_1551().method_61966().method_60637(true));
      });
      HudRenderCallback.EVENT.register(new HUDRenderer());
      HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
         Fakelag fakelag = (Fakelag)Faith.getInstance().getModuleManager().getModule("Fakelag");
         if (fakelag != null && fakelag.isEnabled()) {
            fakelag.onHudRender(drawContext, tickDelta);
         }

      });
      LogCleaner.clean();
   }

   private void registerCoreShaders() {
      CoreShaderManager manager = CoreShaderManager.getInstance();
      manager.registerPositionColorShader(class_2960.method_60655("dynamic_fps", "liquid_metal"));
      manager.registerPositionColorShader(class_2960.method_60655("dynamic_fps", "lava"));
      manager.registerPositionColorShader(class_2960.method_60655("dynamic_fps", "water"));
      manager.registerCurveShader(class_2960.method_60655("dynamic_fps", "curve"));
      manager.registerPositionTextureColorShader(class_2960.method_60655("dynamic_fps", "msdf_text"));
   }

   private void registerPostShaders() {
      PostShaderManager manager = PostShaderManager.getInstance();
   }

   private void renderPostEffects() {
      class_310 client = class_310.method_1551();
      PostShaderManager manager = PostShaderManager.getInstance();
      manager.render(client.method_1522());
   }
}
