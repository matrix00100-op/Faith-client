package com.slither.cyemer.hud;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_332;

@Environment(EnvType.CLIENT)
public class HUDManager {
   private static HUDManager instance;
   private final List<HUDElement> elements = new ArrayList();
   private static double lastHitReach = 0.0D;

   private HUDManager() {
      this.elements.add(new FPSHudElement("FPS", 10.0D, 10.0D));
      this.elements.add(new CoordinatesHudElement("Coordinates", 10.0D, 35.0D));
      this.elements.add(new TargetHudElement("TargetHUD", 10.0D, 60.0D));
      this.elements.add(new ReachHudElement("Last Reach", 10.0D, 90.0D));
      this.elements.add(new EffectHudElement("EffectHUD", 10.0D, 120.0D));
   }

   public static double getLastHitReach() {
      return lastHitReach;
   }

   public static void updateLastHitReach(double reach) {
      lastHitReach = reach;
   }

   public void initialize() {
      TotemPopManager.getInstance();
   }

   public void render(class_332 context, float delta) {
      Iterator var3 = this.elements.iterator();

      while(var3.hasNext()) {
         HUDElement element = (HUDElement)var3.next();
         if (element.isEnabled()) {
            element.render(context, delta);
         }
      }

   }

   public List<HUDElement> getElements() {
      return this.elements;
   }

   public HUDElement getElement(String name) {
      Iterator var2 = this.elements.iterator();

      HUDElement element;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         element = (HUDElement)var2.next();
      } while(!element.getName().equalsIgnoreCase(name));

      return element;
   }

   public static HUDManager getInstance() {
      if (instance == null) {
         instance = new HUDManager();
      }

      return instance;
   }
}
