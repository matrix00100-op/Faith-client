package com.slither.cyemer.event;

import com.slither.cyemer.Faith;
import com.slither.cyemer.module.implementation.AutoDrain;
import com.slither.cyemer.module.implementation.combat.AnchorMacro;
import com.slither.cyemer.module.implementation.combat.AutoAnchor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_239;
import net.minecraft.class_3965;

@Environment(EnvType.CLIENT)
public class ItemUseListener {
   public static void onItemUse(class_239 hitResult) {
      if (hitResult instanceof class_3965) {
         class_3965 blockHitResult = (class_3965)hitResult;
         AutoAnchor autoAnchor = (AutoAnchor)Faith.getInstance().getModuleManager().getModule("AutoAnchor");
         if (autoAnchor != null && autoAnchor.isEnabled()) {
            autoAnchor.onItemUse(blockHitResult);
         }

         AutoDrain autoDrain = (AutoDrain)Faith.getInstance().getModuleManager().getModule("AutoDrain");
         if (autoDrain != null && autoDrain.isEnabled()) {
            autoDrain.onItemUse(blockHitResult);
         }

         AnchorMacro anchorMacro = (AnchorMacro)Faith.getInstance().getModuleManager().getModule("AnchorMacro");
         if (anchorMacro != null && anchorMacro.isEnabled()) {
            anchorMacro.onItemUse(blockHitResult);
         }
      }

   }
}
