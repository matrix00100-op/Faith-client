package com.slither.cyemer.module.implementation;

import com.slither.cyemer.Faith;
import com.slither.cyemer.module.BooleanSetting;
import com.slither.cyemer.module.Category;
import com.slither.cyemer.module.ColorSetting;
import com.slither.cyemer.module.ModeSetting;
import com.slither.cyemer.module.Module;
import com.slither.cyemer.module.SliderSetting;
import com.slither.cyemer.util.Renderer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_332;

@Environment(EnvType.CLIENT)
public class ArrayListModule extends Module {
   private final ModeSetting position = new ModeSetting("Position", new String[]{"Top-Right", "Top-Left", "Bottom-Right", "Bottom-Left"});
   private final BooleanSetting sort = new BooleanSetting("Sort", true);
   private final SliderSetting textSize = new SliderSetting("Text Size", 1.0D, 0.5D, 2.0D, 2);
   private final SliderSetting padding = new SliderSetting("Padding", 3.0D, 0.0D, 10.0D, 1);
   private final BooleanSetting defaultColours = new BooleanSetting("Default Colors", true);
   private final ColorSetting textStartColor = new ColorSetting("Text Color 1", new Color(255, 255, 255));
   private final ColorSetting textEndColor = new ColorSetting("Text Color 2", new Color(0, 150, 255));
   private final ColorSetting sidebarStartColor = new ColorSetting("Bar Color 1", new Color(40, 40, 255));
   private final ColorSetting sidebarEndColor = new ColorSetting("Bar Color 2", new Color(180, 40, 255));
   private final SliderSetting colorSpeed = new SliderSetting("Color Speed", 3.0D, 0.1D, 10.0D, 1);
   private final SliderSetting colorSpread = new SliderSetting("Color Spread", 0.5D, 0.1D, 2.0D, 1);
   private final SliderSetting backgroundOpacity = new SliderSetting("BG Opacity", 0.5D, 0.0D, 1.0D, 2);
   private final BooleanSetting sidebar = new BooleanSetting("Sidebar", true);
   private final SliderSetting sidebarWidth = new SliderSetting("Sidebar Width", 2.0D, 1.0D, 5.0D, 1);
   private final BooleanSetting particles = new BooleanSetting("Particles", true);
   private final BooleanSetting textShadow = new BooleanSetting("Text Shadow", true);
   private final ModeSetting popAnimation = new ModeSetting("Pop Animation", new String[]{"Slide", "Pop", "Fade", "None"});
   private final SliderSetting animationSpeed = new SliderSetting("Animation Speed", 4.0D, 0.5D, 10.0D, 1);
   private long lastFrameTime = System.currentTimeMillis();
   private final Map<String, Float> moduleAnimations = new HashMap();
   private final Map<String, Boolean> moduleRemoving = new HashMap();
   private final Map<String, Boolean> moduleIsNew = new HashMap();
   private final Set<String> flurryPlayed = new HashSet();
   private final List<ArrayListModule.Particle> particleList = new ArrayList();
   private final Random random = new Random();
   private final Color CYEMER_PINK = new Color(255, 100, 180);
   private final Color CYEMER_BLUE = new Color(100, 180, 255);

   public ArrayListModule() {
      super("ArrayList", "Displays enabled modules on screen", Category.CLIENT);
      this.setEnabled(true);
      this.addSetting(this.position);
      this.addSetting(this.sort);
      this.addSetting(this.textSize);
      this.addSetting(this.padding);
      this.addSetting(this.defaultColours);
      this.addSetting(this.textStartColor);
      this.addSetting(this.textEndColor);
      this.addSetting(this.sidebarStartColor);
      this.addSetting(this.sidebarEndColor);
      this.addSetting(this.colorSpeed);
      this.addSetting(this.colorSpread);
      this.addSetting(this.backgroundOpacity);
      this.addSetting(this.sidebar);
      this.addSetting(this.sidebarWidth);
      this.addSetting(this.particles);
      this.addSetting(this.textShadow);
      this.addSetting(this.popAnimation);
      this.addSetting(this.animationSpeed);
   }

   public void onRender(class_332 context, float tickDelta) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         List<Module> enabledModules = (List)Faith.getInstance().getModuleManager().getModules().stream().filter((modulex) -> {
            return modulex.isEnabled() && modulex != this;
         }).collect(Collectors.toList());
         float scale = (float)this.textSize.getValue();
         float pad = (float)this.padding.getValue();
         Objects.requireNonNull(this.mc.field_1772);
         float fontSize = 9.0F * scale;
         float renderedTextHeight = Renderer.get().getTextHeight(fontSize);
         float itemHeight = (float)Math.ceil((double)(renderedTextHeight + pad * 2.0F));
         int screenWidth = this.mc.method_22683().method_4486();
         int screenHeight = this.mc.method_22683().method_4502();
         float pixelRatio = (float)this.mc.method_22683().method_4495();
         if (Renderer.get().beginFrame((float)screenWidth, (float)screenHeight, pixelRatio)) {
            List<Module> allModules = new ArrayList(enabledModules);
            Iterator var13 = (new HashMap(this.moduleAnimations)).keySet().iterator();

            boolean alignRight;
            while(var13.hasNext()) {
               String moduleName = (String)var13.next();
               boolean stillEnabled = enabledModules.stream().anyMatch((m) -> {
                  return m.getName().equals(moduleName);
               });
               alignRight = (Boolean)this.moduleRemoving.getOrDefault(moduleName, false);
               if (!stillEnabled && !alignRight) {
                  this.moduleRemoving.put(moduleName, true);
                  this.flurryPlayed.remove(moduleName);
                  Optional var10000 = Faith.getInstance().getModuleManager().getModules().stream().filter((m) -> {
                     return m.getName().equals(moduleName);
                  }).findFirst();
                  Objects.requireNonNull(allModules);
                  var10000.ifPresent(allModules::add);
               }
            }

            if (this.sort.isEnabled()) {
               try {
                  allModules.sort(Comparator.comparingInt((m) -> {
                     return (int)Renderer.get().getTextWidth(m.getName(), fontSize);
                  }).reversed());
               } catch (Exception var52) {
                  allModules.sort(Comparator.comparingInt((m) -> {
                     return this.mc.field_1772.method_1727(m.getName());
                  }).reversed());
               }
            }

            long currentTime = System.currentTimeMillis();
            float deltaTime = (float)(currentTime - this.lastFrameTime) / 1000.0F;
            this.lastFrameTime = currentTime;
            this.updateAniStates(enabledModules, deltaTime);
            alignRight = this.position.getCurrentMode().contains("Right");
            boolean alignBottom = this.position.getCurrentMode().contains("Bottom");
            float currentY = alignBottom ? (float)(screenHeight - 2) : 2.0F;
            Color tStart;
            Color tEnd;
            Color sStart;
            Color sEnd;
            if (this.defaultColours.isEnabled()) {
               tStart = this.CYEMER_PINK;
               tEnd = this.CYEMER_BLUE;
               sStart = this.CYEMER_PINK;
               sEnd = this.CYEMER_BLUE;
            } else {
               tStart = this.textStartColor.getValue();
               tEnd = this.textEndColor.getValue();
               sStart = this.sidebarStartColor.getValue();
               sEnd = this.sidebarEndColor.getValue();
            }

            if (this.particles.isEnabled()) {
               this.particles(context, deltaTime, tStart, tEnd);
            }

            int index = 0;
            Iterator var24 = allModules.iterator();

            while(true) {
               while(var24.hasNext()) {
                  Module module = (Module)var24.next();
                  String name = module.getName();
                  boolean isRemoving = (Boolean)this.moduleRemoving.getOrDefault(name, false);
                  float animProgress = (Float)this.moduleAnimations.getOrDefault(name, 1.0F);
                  boolean isNew = (Boolean)this.moduleIsNew.getOrDefault(name, false);
                  if (isRemoving && animProgress <= 0.0F) {
                     this.flurryPlayed.remove(name);
                  } else {
                     float itemWidth;
                     try {
                        itemWidth = Renderer.get().getTextWidth(name, fontSize);
                     } catch (Exception var51) {
                        itemWidth = (float)this.mc.field_1772.method_1727(name) * scale;
                     }

                     String animMode = this.popAnimation.getCurrentMode();
                     float xOffset = 0.0F;
                     float heightScale = 1.0F;
                     float alpha = 1.0F;
                     if (!animMode.equals("None") && animProgress < 1.0F) {
                        heightScale = animProgress;
                        if (animMode.equals("Slide")) {
                           xOffset = (1.0F - animProgress) * (itemWidth + 20.0F);
                           if (alignRight) {
                              xOffset = -xOffset;
                           }
                        } else if (animMode.equals("Fade")) {
                           alpha = animProgress;
                        }
                     }

                     double timeFactor = (double)currentTime / 1000.0D * this.colorSpeed.getValue();
                     double indexFactor = (double)index * this.colorSpread.getValue();
                     float waveT = (float)(Math.sin(timeFactor - indexFactor) + 1.0D) / 2.0F;
                     Color renderTextColor = this.interColor(tStart, tEnd, waveT);
                     Color renderSidebarColor = this.interColor(sStart, sEnd, waveT);
                     renderSidebarColor = this.applyAlpha(renderSidebarColor, alpha);
                     renderTextColor = this.applyAlpha(renderTextColor, alpha);
                     float sideWidth = this.sidebar.isEnabled() ? (float)this.sidebarWidth.getValue() : 0.0F;
                     float fullWidth = itemWidth + pad * 2.0F + sideWidth;
                     float x;
                     if (alignRight) {
                        x = (float)screenWidth - fullWidth - 2.0F;
                        x -= xOffset;
                     } else {
                        x = 2.0F;
                        x += xOffset;
                     }

                     float drawHeight = itemHeight * heightScale;
                     if (heightScale >= 1.0F) {
                        drawHeight = (float)Math.ceil((double)drawHeight);
                     }

                     if (alignBottom) {
                        currentY -= drawHeight;
                     }

                     float textX;
                     if (this.particles.isEnabled() && !this.flurryPlayed.contains(name) && (isNew && animProgress > 0.01F || isRemoving && animProgress < 0.99F)) {
                        float centerX = x + fullWidth / 2.0F;
                        textX = currentY + drawHeight / 2.0F;
                        this.spawnFlurryThingo(centerX, textX, renderTextColor);
                        this.flurryPlayed.add(name);
                     }

                     int bgAlpha = (int)(this.backgroundOpacity.getValue() * 255.0D * (double)alpha);
                     if (bgAlpha > 0) {
                        Renderer.get().drawRect(context, x, currentY, fullWidth, drawHeight, new Color(0, 0, 0, bgAlpha));
                     }

                     if (this.sidebar.isEnabled()) {
                        textX = alignRight ? x + fullWidth - sideWidth : x;
                        Renderer.get().drawRect(context, textX, currentY, sideWidth, drawHeight, renderSidebarColor);
                     }

                     textX = alignRight ? x + pad : x + sideWidth + pad;
                     float textY = currentY + (drawHeight - renderedTextHeight) / 2.0F;
                     if (animMode.equals("Pop") && animProgress < 1.0F) {
                        Renderer.get().save();
                        float centerX = x + fullWidth / 2.0F;
                        float centerY = currentY + drawHeight / 2.0F;
                        Renderer.get().translate(centerX, centerY);
                        Renderer.get().scale(animProgress, animProgress);
                        Renderer.get().translate(-centerX, -centerY);
                     }

                     Renderer.get().drawText(context, name, textX, textY, fontSize, renderTextColor, this.textShadow.isEnabled());
                     if (animMode.equals("Pop") && animProgress < 1.0F) {
                        Renderer.get().restore();
                     }

                     if (!alignBottom) {
                        currentY += drawHeight;
                     }

                     ++index;
                  }
               }

               Renderer.get().endFrame();
               this.cleanupAni();
               return;
            }
         }
      }
   }

   private void spawnFlurryThingo(float x, float y, Color color) {
      for(int i = 0; i < 15; ++i) {
         ArrayListModule.Particle p = new ArrayListModule.Particle(this, x, y, color);
         p.isFlurry = true;
         p.velocityX = (this.random.nextFloat() - 0.5F) * 60.0F;
         p.velocityY = (this.random.nextFloat() - 0.5F) * 60.0F;
         p.maxLife = 1.0F;
         p.life = p.maxLife;
         this.particleList.add(p);
      }

   }

   private void particles(class_332 context, float deltaTime, Color startC, Color endC) {
      int screenWidth = this.mc.method_22683().method_4486();
      int screenHeight = this.mc.method_22683().method_4502();
      float alpha;
      if (this.particleList.size() < 60 && this.random.nextFloat() < 0.1F) {
         boolean alignRight = this.position.getCurrentMode().contains("Right");
         float pX = alignRight ? (float)screenWidth - this.random.nextFloat() * 120.0F : this.random.nextFloat() * 120.0F;
         alpha = this.random.nextFloat() * (float)screenHeight;
         float waveT = (float)(Math.sin((double)System.currentTimeMillis() / 1000.0D * this.colorSpeed.getValue()) + 1.0D) / 2.0F;
         Color pColor = this.interColor(startC, endC, waveT);
         this.particleList.add(new ArrayListModule.Particle(this, pX, alpha, pColor));
      }

      Iterator it = this.particleList.iterator();

      while(it.hasNext()) {
         ArrayListModule.Particle p = (ArrayListModule.Particle)it.next();
         if (p.isFlurry) {
            p.x += p.velocityX * deltaTime;
            p.y += p.velocityY * deltaTime;
            p.velocityY = (float)((double)p.velocityY + 9.8D * (double)deltaTime);
         } else {
            p.y -= 10.0F * deltaTime;
            p.x = (float)((double)p.x + Math.sin((double)System.currentTimeMillis() / 300.0D + (double)p.offset) * 10.0D * (double)deltaTime);
         }

         p.life -= deltaTime;
         if (p.life <= 0.0F) {
            it.remove();
         } else {
            alpha = p.life / p.maxLife;
            Color renderColor = this.applyAlpha(p.color, alpha);
            Renderer.get().drawRect(context, p.x, p.y, 2.0F, 2.0F, renderColor);
         }
      }

   }

   private Color interColor(Color c1, Color c2, float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      int r = (int)((float)c1.getRed() + t * (float)(c2.getRed() - c1.getRed()));
      int g = (int)((float)c1.getGreen() + t * (float)(c2.getGreen() - c1.getGreen()));
      int b = (int)((float)c1.getBlue() + t * (float)(c2.getBlue() - c1.getBlue()));
      return new Color(r, g, b);
   }

   private Color applyAlpha(Color c, float alpha) {
      if (alpha >= 1.0F) {
         return c;
      } else {
         return alpha <= 0.0F ? new Color(c.getRed(), c.getGreen(), c.getBlue(), 0) : new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)((float)c.getAlpha() * alpha));
      }
   }

   private void updateAniStates(List<Module> enabledModules, float deltaTime) {
      String currentAnimMode = this.popAnimation.getCurrentMode();
      boolean hasAnimations = !currentAnimMode.equals("None");
      Iterator var5 = enabledModules.iterator();

      String moduleName;
      while(var5.hasNext()) {
         Module module = (Module)var5.next();
         moduleName = module.getName();
         if (!this.moduleAnimations.containsKey(moduleName)) {
            this.moduleRemoving.put(moduleName, false);
            this.flurryPlayed.remove(moduleName);
            if (hasAnimations) {
               this.moduleAnimations.put(moduleName, 0.0F);
               this.moduleIsNew.put(moduleName, true);
            } else {
               this.moduleAnimations.put(moduleName, 1.0F);
               this.moduleIsNew.put(moduleName, false);
            }
         }
      }

      float speed = (float)this.animationSpeed.getValue();
      Iterator var12 = (new HashMap(this.moduleAnimations)).keySet().iterator();

      while(var12.hasNext()) {
         moduleName = (String)var12.next();
         boolean isRemoving = (Boolean)this.moduleRemoving.getOrDefault(moduleName, false);
         boolean isNew = (Boolean)this.moduleIsNew.getOrDefault(moduleName, false);
         float animProgress = (Float)this.moduleAnimations.getOrDefault(moduleName, 1.0F);
         if (hasAnimations) {
            if (isRemoving) {
               animProgress = Math.max(0.0F, animProgress - deltaTime * speed);
            } else if (isNew) {
               animProgress = Math.min(1.0F, animProgress + deltaTime * speed);
               if (animProgress >= 1.0F) {
                  this.moduleIsNew.put(moduleName, false);
               }
            }

            this.moduleAnimations.put(moduleName, animProgress);
         } else {
            this.moduleAnimations.put(moduleName, 1.0F);
         }
      }

   }

   private void cleanupAni() {
      List<String> toCleanup = new ArrayList();
      Iterator var2 = this.moduleAnimations.keySet().iterator();

      String name;
      while(var2.hasNext()) {
         name = (String)var2.next();
         boolean isRemoving = (Boolean)this.moduleRemoving.getOrDefault(name, false);
         float progress = (Float)this.moduleAnimations.getOrDefault(name, 1.0F);
         if (isRemoving && progress <= 0.0F) {
            toCleanup.add(name);
         }
      }

      var2 = toCleanup.iterator();

      while(var2.hasNext()) {
         name = (String)var2.next();
         this.moduleAnimations.remove(name);
         this.moduleRemoving.remove(name);
         this.moduleIsNew.remove(name);
         this.flurryPlayed.remove(name);
      }

   }

   @Environment(EnvType.CLIENT)
   private class Particle {
      float x;
      float y;
      float life;
      float maxLife;
      float offset;
      Color color;
      boolean isFlurry = false;
      float velocityX;
      float velocityY;

      public Particle(final ArrayListModule param1, float x, float y, Color c) {
         this.x = x;
         this.y = y;
         this.color = c;
         this.maxLife = 1.5F + var1.random.nextFloat() * 1.5F;
         this.life = this.maxLife;
         this.offset = var1.random.nextFloat() * 10.0F;
      }
   }
}
