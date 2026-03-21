package com.slither.cyemer.gui.new_ui;

import com.slither.cyemer.Faith;
import com.slither.cyemer.module.Category;
import com.slither.cyemer.module.implementation.ClickGUIModule;
import com.slither.cyemer.util.Renderer;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1011;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import net.minecraft.class_332;

@Environment(EnvType.CLIENT)
public class Panel {
   public double x;
   public double y;
   public double width;
   public double headerHeight;
   private final Category category;
   private final List<ModuleButton> buttons = new ArrayList();
   public boolean dragging = false;
   private double dragX;
   private double dragY;
   private boolean collapsed = false;
   private double collapseAnimation = 1.0D;
   private boolean isCollapsing = false;
   private boolean isExpanding = false;
   private String searchQuery = "";
   private boolean searchFocused = false;
   private static final double MODULE_ROW_HEIGHT = 21.0D;
   private static final double BOTTOM_CORNER_GAP = 0.0D;
   private double searchBoxHeight = 21.0D;
   private int categoryIconImageId = -1;
   private int categoryIconTextureWidth = 256;
   private int categoryIconTextureHeight = 256;
   private String lastIconColorMode = null;

   public Panel(Category category, double x, double y, double width, double headerHeight) {
      this.category = category;
      this.x = x;
      this.y = y;
      this.width = width;
      this.headerHeight = headerHeight;
      Faith.getInstance().getModuleManager().getModules().stream().filter((m) -> {
         return m.getCategory() == category;
      }).forEach((m) -> {
         this.buttons.add(new ModuleButton(m));
      });
      if (category == Category.CLIENT) {
         this.moveModuleToBottom("ClickGUI");
      } else if (category == Category.RENDER) {
         this.moveModuleToBottom("CustomCape");
      }

      this.loadCategoryIcon();
   }

   private void moveModuleToBottom(String moduleName) {
      this.buttons.sort((a, b) -> {
         boolean aMatches = moduleName.equalsIgnoreCase(a.getModule().getName());
         boolean bMatches = moduleName.equalsIgnoreCase(b.getModule().getName());
         if (aMatches == bMatches) {
            return 0;
         } else {
            return aMatches ? 1 : -1;
         }
      });
   }

   private void loadCategoryIcon() {
      try {
         boolean blackIcons = ClickGUIModule.useBlackIcons();
         String preferredFile = null;
         switch(this.category) {
         case CLIENT:
            preferredFile = blackIcons ? "chip.png" : "client.png";
            break;
         case MISC:
            preferredFile = blackIcons ? "tab.png" : "misc.png";
            break;
         case RENDER:
            preferredFile = blackIcons ? "view.png" : "render.png";
            break;
         case COMBAT:
            preferredFile = blackIcons ? "swords.png" : "COMBAT.png";
            break;
         case MOVEMENT:
            preferredFile = blackIcons ? "race.png" : "MOVEMENT.png";
            break;
         case PLAYER:
            preferredFile = blackIcons ? "man.png" : "player.png";
         }

         int iconId = -1;
         if (preferredFile != null) {
            iconId = this.tryLoadGuiIcon(preferredFile);
         }

         String exactName;
         if (iconId == -1) {
            exactName = this.category.name().toLowerCase() + ".png";
            iconId = this.tryLoadGuiIcon(exactName);
         }

         if (iconId == -1) {
            exactName = this.category.name() + ".png";
            iconId = this.tryLoadGuiIcon(exactName);
         }

         this.categoryIconImageId = iconId;
      } catch (Exception var5) {
         this.categoryIconImageId = -1;
      }

      this.lastIconColorMode = ClickGUIModule.getIconColorMode();
   }

   private int tryLoadGuiIcon(String fileName) {
      int iconId = Renderer.get().createImageFromFile("dynamic_fps:textures/gui/" + fileName);
      if (iconId != -1) {
         this.updateIconTextureSize(fileName);
         return iconId;
      } else {
         iconId = Renderer.get().createImageFromFile("/assets/dynamic_fps/textures/gui/" + fileName);
         if (iconId != -1) {
            this.updateIconTextureSize(fileName);
         }

         return iconId;
      }
   }

   private void updateIconTextureSize(String fileName) {
      this.categoryIconTextureWidth = 256;
      this.categoryIconTextureHeight = 256;
      class_310 client = class_310.method_1551();
      if (client != null && client.method_1478() != null) {
         class_2960 id = class_2960.method_60655("dynamic_fps", "textures/gui/" + fileName);
         Optional<class_3298> optional = client.method_1478().method_14486(id);
         if (!optional.isEmpty()) {
            try {
               InputStream stream = ((class_3298)optional.get()).method_14482();

               try {
                  class_1011 image = class_1011.method_4309(stream);

                  try {
                     this.categoryIconTextureWidth = Math.max(1, image.method_4307());
                     this.categoryIconTextureHeight = Math.max(1, image.method_4323());
                  } catch (Throwable var11) {
                     if (image != null) {
                        try {
                           image.close();
                        } catch (Throwable var10) {
                           var11.addSuppressed(var10);
                        }
                     }

                     throw var11;
                  }

                  if (image != null) {
                     image.close();
                  }
               } catch (Throwable var12) {
                  if (stream != null) {
                     try {
                        stream.close();
                     } catch (Throwable var9) {
                        var12.addSuppressed(var9);
                     }
                  }

                  throw var12;
               }

               if (stream != null) {
                  stream.close();
               }
            } catch (IOException var13) {
            }

         }
      }
   }

   public String render(class_332 context, int mouseX, int mouseY, float delta, double alpha) {
      if (this.lastIconColorMode == null || !this.lastIconColorMode.equalsIgnoreCase(ClickGUIModule.getIconColorMode())) {
         this.loadCategoryIcon();
      }

      if (alpha < 0.01D) {
         return null;
      } else {
         String tooltip = null;
         Renderer.get().resetScissor();
         if (this.isCollapsing && this.collapseAnimation > 0.0D) {
            this.collapseAnimation -= (double)delta * 6.0D;
            if (this.collapseAnimation <= 0.0D) {
               this.collapseAnimation = 0.0D;
               this.isCollapsing = false;
            }
         } else if (this.isExpanding && this.collapseAnimation < 1.0D) {
            this.collapseAnimation += (double)delta * 6.0D;
            if (this.collapseAnimation >= 1.0D) {
               this.collapseAnimation = 1.0D;
               this.isExpanding = false;
            }
         }

         this.renderPanelBody(context, alpha);
         this.renderHeaderWithGlass(context, alpha);
         Renderer.get().resetScissor();
         String titleString = this.collapsed ? this.category.name + " [" + this.buttons.size() + "]" : this.category.name;
         float titleX = (float)(this.x + 10.0D);
         float titleY = (float)(this.y + (this.headerHeight - 12.0D) / 2.0D);
         Color titleColor = new Color(255, 255, 255, (int)(255.0D * alpha));
         Renderer.get().drawText(context, titleString, titleX, titleY, 12.0F, titleColor, false);
         float logoSize = 15.0F;
         float logoX = (float)(this.x + this.width - (double)logoSize - 9.0D);
         float logoY = (float)(this.y + (this.headerHeight - (double)logoSize) / 2.0D);
         if (this.categoryIconImageId != -1) {
            Renderer.get().drawTexture(context, this.categoryIconImageId, logoX, logoY, logoSize, logoSize, 0.0F, 0.0F, (float)this.categoryIconTextureWidth, (float)this.categoryIconTextureHeight, (float)this.categoryIconTextureWidth, (float)this.categoryIconTextureHeight);
         }

         if (this.collapseAnimation > 0.0D) {
            double contentY = this.y + this.headerHeight;
            double contentAlpha = alpha * this.collapseAnimation;
            if (!this.collapsed) {
               this.renderSearchBox(context, mouseX, mouseY, contentY, contentAlpha);
               contentY += this.searchBoxHeight;
            }

            List<ModuleButton> visibleButtons = new ArrayList();
            Iterator var22 = this.buttons.iterator();

            while(true) {
               ModuleButton button;
               do {
                  if (!var22.hasNext()) {
                     double totalContentHeight = this.getTotalHeightExpanded() - this.headerHeight - (this.collapsed ? 0.0D : this.searchBoxHeight);
                     double var10000 = totalContentHeight * this.collapseAnimation;
                     boolean clipContent = this.collapseAnimation < 0.999D;
                     double panelTotalHeight = this.getTotalHeight();
                     Renderer.get().scissor(context, (float)this.x, (float)(this.y + this.headerHeight), (float)this.width, (float)(panelTotalHeight - this.headerHeight));
                     double buttonY = contentY;

                     for(int i = 0; i < visibleButtons.size(); ++i) {
                        ModuleButton button = (ModuleButton)visibleButtons.get(i);
                        button.isLastButton = i == visibleButtons.size() - 1;
                        button.x = this.x + 1.0D;
                        button.y = buttonY;
                        button.width = this.width - 2.0D;
                        button.height = 21.0D;
                        String btnTooltip = button.render(context, mouseX, mouseY, delta, contentAlpha);
                        if (btnTooltip != null && !btnTooltip.isEmpty()) {
                           tooltip = btnTooltip;
                        }

                        buttonY += button.getTotalHeight();
                     }

                     Renderer.get().resetScissor();
                     return tooltip;
                  }

                  button = (ModuleButton)var22.next();
               } while(!this.searchQuery.isEmpty() && !button.getModule().getName().toLowerCase().contains(this.searchQuery.toLowerCase()));

               visibleButtons.add(button);
            }
         } else {
            return tooltip;
         }
      }
   }

   private void renderPanelBody(class_332 context, double alpha) {
      float radius = ClickGUIModule.getGuiCornerRadiusScaled(1.0D);
      Color panelBg = ClickGUIModule.getColor(ClickGUIModule.getPanelBackground(), alpha);
      Color glassOverlay = new Color(255, 255, 255, (int)(8.0D * alpha));
      Renderer.get().drawRoundedRect(context, (float)this.x, (float)this.y, (float)this.width, (float)this.getTotalHeight(), radius, panelBg);
      Renderer.get().drawRoundedRect(context, (float)this.x, (float)this.y, (float)this.width, (float)this.getTotalHeight(), radius, glassOverlay);
   }

   private void renderShadow(class_332 context, double alpha) {
      float totalHeight = (float)this.getTotalHeight();
      float cornerRadius = ClickGUIModule.getCornerRadiusScaled(1.0D);
      float shadowRadius = Math.min(8.0F, cornerRadius);
      int layers = 7;
      float maxOffset = 5.5F;

      for(int i = 0; i < layers; ++i) {
         float progress = (float)i / (float)layers;
         float layerOffset = 1.5F + progress * maxOffset;
         float layerAlpha = (float)(alpha * 0.06D * (1.0D - (double)progress * 0.8D));
         Color shadowColor = new Color(0, 0, 0, (int)(layerAlpha * 255.0F));
         Renderer.get().drawRoundedRect(context, (float)(this.x + (double)layerOffset), (float)(this.y + (double)layerOffset), (float)this.width, totalHeight, shadowRadius, shadowColor);
      }

   }

   private void renderHeaderWithGlass(class_332 context, double alpha) {
   }

   private void drawTopRoundedFill(class_332 context, float drawX, float drawY, float drawW, float drawH, float radius, Color color) {
      if (!(drawW <= 0.0F) && !(drawH <= 0.0F)) {
         float clampedRadius = Math.min(radius, drawW * 0.5F);
         if (clampedRadius <= 0.5F) {
            Renderer.get().drawRect(context, drawX, drawY, drawW, drawH, color);
         } else {
            float capH = Math.min(drawH, clampedRadius);
            if (capH > 0.0F) {
               Renderer.get().scissor(context, drawX, drawY, drawW, capH);
               Renderer.get().drawRoundedRect(context, drawX, drawY, drawW, Math.max(drawH, clampedRadius * 2.0F), clampedRadius, color);
               Renderer.get().resetScissor();
            }

            float bodyY = drawY + capH;
            float bodyH = drawH - capH;
            if (bodyH > 0.0F) {
               Renderer.get().drawRect(context, drawX, bodyY, drawW, bodyH, color);
            }

         }
      }
   }

   private void renderSearchBox(class_332 context, int mouseX, int mouseY, double searchY, double alpha) {
      double searchX = this.x + 1.0D;
      double searchW = this.width - 2.0D;
      boolean hovered = (double)mouseX >= searchX && (double)mouseX <= searchX + searchW && (double)mouseY >= searchY && (double)mouseY <= searchY + this.searchBoxHeight;
      Color searchBg = ClickGUIModule.getColor(!this.searchFocused && !hovered ? ClickGUIModule.getModuleButtonBackground() : ClickGUIModule.getModuleButtonHoverBackground(), alpha);
      Color searchGlass = new Color(255, 255, 255, (int)((double)(!this.searchFocused && !hovered ? 6 : 9) * alpha));
      Renderer.get().drawRect(context, (float)searchX, (float)searchY, (float)searchW, (float)this.searchBoxHeight, searchBg);
      Renderer.get().drawRect(context, (float)searchX, (float)searchY, (float)searchW, (float)this.searchBoxHeight, searchGlass);
      String displayText = this.searchQuery.isEmpty() ? "Search modules..." : this.searchQuery;
      Color textColor = this.searchQuery.isEmpty() ? ClickGUIModule.getColor(ClickGUIModule.getSearchBoxPlaceholder(), alpha) : ClickGUIModule.getColor(ClickGUIModule.getSearchBoxText(), alpha);
      float fontSize = 10.0F;
      float textHeight = Renderer.get().getTextHeight(fontSize);
      float textY = (float)(searchY + (this.searchBoxHeight - (double)textHeight) / 2.0D);
      Renderer.get().drawText(context, displayText, (float)(searchX + 6.0D), textY, fontSize, textColor, false);
   }

   public void mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.isHeaderHovered(mouseX, mouseY)) {
         if (!this.collapsed && this.isSearchBoxHovered(mouseX, mouseY)) {
            this.searchFocused = true;
         } else {
            this.searchFocused = false;
            if (!this.collapsed && this.collapseAnimation > 0.5D) {
               this.buttons.forEach((b) -> {
                  b.mouseClicked(mouseX, mouseY, button);
               });
            }

         }
      }
   }

   public void mouseReleased(double mouseX, double mouseY, int button) {
      if (!this.collapsed) {
         this.buttons.forEach((b) -> {
            b.mouseReleased(mouseX, mouseY, button);
         });
      }

   }

   private void toggleCollapse() {
      this.collapsed = !this.collapsed;
      if (this.collapsed) {
         this.isCollapsing = true;
         this.isExpanding = false;
      } else {
         this.isExpanding = true;
         this.isCollapsing = false;
      }

   }

   double getTotalHeight() {
      return !this.collapsed && this.collapseAnimation != 0.0D ? this.headerHeight + (this.getTotalHeightExpanded() - this.headerHeight) * this.collapseAnimation : this.headerHeight;
   }

   private double getTotalHeightExpanded() {
      double h = this.headerHeight;
      if (!this.collapsed) {
         h += this.searchBoxHeight;
      }

      Iterator var3 = this.buttons.iterator();

      while(true) {
         ModuleButton b;
         do {
            if (!var3.hasNext()) {
               h += 0.0D;
               return h;
            }

            b = (ModuleButton)var3.next();
         } while(!this.searchQuery.isEmpty() && !b.getModule().getName().toLowerCase().contains(this.searchQuery.toLowerCase()));

         h += b.getTotalHeight();
      }
   }

   private boolean isHeaderHovered(double mouseX, double mouseY) {
      return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.headerHeight;
   }

   private boolean isSearchBoxHovered(double mouseX, double mouseY) {
      double searchY = this.y + this.headerHeight;
      double searchX = this.x + 1.0D;
      double searchW = this.width - 2.0D;
      return mouseX >= searchX && mouseX <= searchX + searchW && mouseY >= searchY && mouseY <= searchY + this.searchBoxHeight;
   }

   public List<ModuleButton> getButtons() {
      return this.buttons;
   }

   public void keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.searchFocused) {
         if (keyCode == 259 && !this.searchQuery.isEmpty()) {
            this.searchQuery = this.searchQuery.substring(0, this.searchQuery.length() - 1);
            return;
         }

         if (keyCode == 256) {
            this.searchFocused = false;
            return;
         }
      }

      if (!this.collapsed) {
         this.buttons.forEach((b) -> {
            b.keyPressed(keyCode, scanCode, modifiers);
         });
      }

   }

   public void charTyped(char chr, int modifiers) {
      if (this.searchFocused) {
         if (chr >= ' ' && chr <= '~') {
            this.searchQuery = this.searchQuery + chr;
         }

      } else {
         if (!this.collapsed) {
            this.buttons.forEach((b) -> {
               b.charTyped(chr, modifiers);
            });
         }

      }
   }
}
