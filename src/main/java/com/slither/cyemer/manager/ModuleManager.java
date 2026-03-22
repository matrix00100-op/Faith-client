package com.slither.cyemer.manager;

import com.slither.cyemer.module.Category;
import com.slither.cyemer.module.Module;
import com.slither.cyemer.module.implementation.ArrayListModule;
import com.slither.cyemer.module.implementation.AutoDrain;
import com.slither.cyemer.module.implementation.AutoTool;
import com.slither.cyemer.module.implementation.AutoTotem;
import com.slither.cyemer.module.implementation.Blink;
import com.slither.cyemer.module.implementation.Booster;
import com.slither.cyemer.module.implementation.ClickGUIModule;
import com.slither.cyemer.module.implementation.ConfigManagerModule;
import com.slither.cyemer.module.implementation.CustomCapeModule;
import com.slither.cyemer.module.implementation.CustomFont;
import com.slither.cyemer.module.implementation.CustomRender;
import com.slither.cyemer.module.implementation.ESP;
import com.slither.cyemer.module.implementation.ElytraSwap;
import com.slither.cyemer.module.implementation.Fakelag;
import com.slither.cyemer.module.implementation.FastPlace;
import com.slither.cyemer.module.implementation.FullBright;
import com.slither.cyemer.module.implementation.HitAnimations;
import com.slither.cyemer.module.implementation.HitParticles;
import com.slither.cyemer.module.implementation.HoverTotem;
import com.slither.cyemer.module.implementation.HudEditor;
import com.slither.cyemer.module.implementation.Interface;
import com.slither.cyemer.module.implementation.KeyPearl;
import com.slither.cyemer.module.implementation.MaceSwap;
import com.slither.cyemer.module.implementation.NoBreakDelay;
import com.slither.cyemer.module.implementation.Notifications;
import com.slither.cyemer.module.implementation.ObsidianGlow;
import com.slither.cyemer.module.implementation.PearlCatch;
import com.slither.cyemer.module.implementation.Prevent;
import com.slither.cyemer.module.implementation.SafeWalk;
import com.slither.cyemer.module.implementation.SelfDestruct;
import com.slither.cyemer.module.implementation.Sprint;
import com.slither.cyemer.module.implementation.StreamerModeModule;
import com.slither.cyemer.module.implementation.TargetEffect;
import com.slither.cyemer.module.implementation.ViewModel;
import com.slither.cyemer.module.implementation.Watermark;
import com.slither.cyemer.module.implementation.WebBreaker;
import com.slither.cyemer.module.implementation.WindChargeKey;
import com.slither.cyemer.module.implementation.combat.AimAssist;
import com.slither.cyemer.module.implementation.combat.AnchorMacro;
import com.slither.cyemer.module.implementation.combat.AntiDivebomb;
import com.slither.cyemer.module.implementation.combat.AutoAnchor;
import com.slither.cyemer.module.implementation.combat.AutoCrit;
import com.slither.cyemer.module.implementation.combat.HitBox;
import com.slither.cyemer.module.implementation.combat.AutoCrystal;
import com.slither.cyemer.module.implementation.combat.AutoElytra;
import com.slither.cyemer.module.implementation.combat.AutoGrapple;
import com.slither.cyemer.module.implementation.combat.AutoJumpReset;
import com.slither.cyemer.module.implementation.combat.AutoMace;
import com.slither.cyemer.module.implementation.combat.AutoObsidian;
import com.slither.cyemer.module.implementation.combat.AutoPot;
import com.slither.cyemer.module.implementation.combat.AutoShield;
import com.slither.cyemer.module.implementation.combat.AutoShieldBreak;
import com.slither.cyemer.module.implementation.combat.AutoWindCharge;
import com.slither.cyemer.module.implementation.combat.BowAimbot;
import com.slither.cyemer.module.implementation.combat.CartRefill;
import com.slither.cyemer.module.implementation.combat.InstaCart;
import com.slither.cyemer.module.implementation.combat.Lungemacro;
import com.slither.cyemer.module.implementation.combat.Nick;
import com.slither.cyemer.module.implementation.combat.PearlCharge;
import com.slither.cyemer.module.implementation.combat.PearlMacro;
import com.slither.cyemer.module.implementation.combat.Shielddrain;
import com.slither.cyemer.module.implementation.combat.TriggerBot;
import com.slither.cyemer.module.implementation.combat.WTap;
import com.slither.cyemer.module.implementation.combat.XbowCart;
import com.slither.cyemer.module.implementation.render.Effectesp;
import com.slither.cyemer.module.implementation.render.HandCham;
import com.slither.cyemer.module.implementation.render.Nametags;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3928;
import net.minecraft.class_408;
import net.minecraft.class_412;
import net.minecraft.class_4587;

@Environment(EnvType.CLIENT)
public class ModuleManager {
   private final List<Module> modules = new ArrayList();
   private final class_310 mc = class_310.method_1551();

   public ModuleManager() {
      this.modules.add(new Sprint());
      this.modules.add(new ArrayListModule());
      this.modules.add(new FullBright());
      this.modules.add(new TriggerBot());
      this.modules.add(new AimAssist());
      this.modules.add(new ESP());
      this.modules.add(new WTap());
      this.modules.add(new AutoShieldBreak());
      this.modules.add(new AutoCrystal());
      this.modules.add(new KeyPearl());
      this.modules.add(new AutoAnchor());
      this.modules.add(new Nick());
      this.modules.add(new AutoObsidian());
      this.modules.add(new Notifications());
      this.modules.add(new HoverTotem());
      this.modules.add(new MaceSwap());
      this.modules.add(new WebBreaker());
      this.modules.add(new StreamerModeModule());
      this.modules.add(new SelfDestruct());
      this.modules.add(new NoBreakDelay());
      this.modules.add(new FastPlace());
      this.modules.add(new Fakelag());
      this.modules.add(new CustomCapeModule());
      this.modules.add(new HitAnimations());
      this.modules.add(new ViewModel());
      this.modules.add(new AutoMace());
      this.modules.add(new AutoElytra());
      this.modules.add(new AutoGrapple());
      this.modules.add(new AntiDivebomb());
      this.modules.add(new HudEditor());
      this.modules.add(new ObsidianGlow());
      this.modules.add(new AutoPot());
      this.modules.add(new AutoTool());
      this.modules.add(new SafeWalk());
      this.modules.add(new ConfigManagerModule());
      this.modules.add(new Booster());
      this.modules.add(new AutoJumpReset());
      this.modules.add(new Blink());
      this.modules.add(new Watermark());
      this.modules.add(new Prevent());
      this.modules.add(new AutoDrain());
      this.modules.add(new Effectesp());
      this.modules.add(new AutoWindCharge());
      this.modules.add(new AutoTotem());
      this.modules.add(new HandCham());
      this.modules.add(new WindChargeKey());
      this.modules.add(new Nametags());
      this.modules.add(new PearlCatch());
      this.modules.add(new ElytraSwap());
      this.modules.add(new ClickGUIModule());
      this.modules.add(new CustomFont());
      this.modules.add(new Interface());
      this.modules.add(new TargetEffect());
      this.modules.add(new CustomRender());
      this.modules.add(new AnchorMacro());
      this.modules.add(new HitParticles());
      this.modules.add(new PearlMacro());
      this.modules.add(new PearlCharge());
      this.modules.add(new BowAimbot());
      this.modules.add(new AutoShield());
      this.modules.add(new Lungemacro());
      this.modules.add(new Shielddrain());
      this.modules.add(new InstaCart());
      this.modules.add(new CartRefill());
      this.modules.add(new XbowCart());
      this.modules.add(new AutoCrit());
        this.modules.add(new HitBox());
   }

   public void onTick() {
      TargetManager.update();
      Iterator var1 = (new ArrayList(this.modules)).iterator();

      while(true) {
         Module module;
         do {
            if (!var1.hasNext()) {
               return;
            }

            module = (Module)var1.next();
         } while(!module.isEnabled() && !module.getName().equals("FullBright") && !module.getName().equals("Booster"));

         module.onTick();
      }
   }

   public void onRender(class_332 context, float tickDelta) {
      Iterator var3 = this.getModules().iterator();

      while(var3.hasNext()) {
         Module module = (Module)var3.next();
         if (module.isEnabled()) {
            module.onRender(context, tickDelta);
         }
      }

   }

   public void onMs() {
      Iterator var1 = this.modules.iterator();

      while(var1.hasNext()) {
         Module module = (Module)var1.next();
         if (module.isEnabled()) {
            module.onMs();
         }
      }

   }

   public boolean shouldBlockNanoVG() {
      if (this.mc.field_1755 == null) {
         return false;
      } else {
         return this.mc.field_1755 instanceof class_408 || this.mc.field_1755 instanceof class_412 || this.mc.field_1755.getClass().getName().endsWith("DownloadingTerrainScreen") || this.mc.field_1755 instanceof class_3928;
      }
   }

   public void onWorldRender(class_4587 matrices, float tickDelta) {
      Iterator var3 = this.getModules().iterator();

      while(var3.hasNext()) {
         Module module = (Module)var3.next();
         if (module.isEnabled()) {
            module.onWorldRender(matrices, tickDelta);
         }
      }

   }

   public List<Module> getModules() {
      return this.modules;
   }

   public boolean addModuleAfter(String existingModuleName, Module module) {
      if (module == null) {
         return false;
      } else if (this.getModule(module.getName()) != null) {
         return false;
      } else {
         int insertAfter = -1;
         if (existingModuleName != null && !existingModuleName.isBlank()) {
            for(int i = 0; i < this.modules.size(); ++i) {
               Module existing = (Module)this.modules.get(i);
               if (existing != null && existing.getName().equalsIgnoreCase(existingModuleName)) {
                  insertAfter = i;
                  break;
               }
            }
         }

         if (insertAfter >= 0 && insertAfter + 1 <= this.modules.size()) {
            this.modules.add(insertAfter + 1, module);
            return true;
         } else {
            this.modules.add(module);
            return true;
         }
      }
   }

   public boolean removeModule(String name) {
      if (name != null && !name.isBlank()) {
         Module module = this.getModule(name);
         if (module == null) {
            return false;
         } else {
            try {
               module.setEnabled(false);
            } catch (Exception var4) {
            }

            return this.modules.remove(module);
         }
      } else {
         return false;
      }
   }

   public List<Module> getModulesForCategory(Category category) {
      return (List)this.modules.stream().filter((module) -> {
         return module.getCategory() == category;
      }).collect(Collectors.toList());
   }

   public Module getModule(String name) {
      Iterator var2 = this.modules.iterator();

      Module module;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         module = (Module)var2.next();
      } while(!module.getName().equalsIgnoreCase(name));

      return module;
   }
}
