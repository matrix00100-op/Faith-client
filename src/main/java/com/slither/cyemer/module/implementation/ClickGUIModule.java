package com.slither.cyemer.module.implementation;

import com.slither.cyemer.gui.new_ui.ClickGUI;
import com.slither.cyemer.module.BooleanSetting;
import com.slither.cyemer.module.Category;
import com.slither.cyemer.module.ColorSetting;
import com.slither.cyemer.module.ModeSetting;
import com.slither.cyemer.module.Module;
import com.slither.cyemer.module.SliderSetting;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ClickGUIModule extends Module {
   public static ModeSetting theme;
   public static ModeSetting iconColor;
   public static BooleanSetting gradientMenus;
   public static ColorSetting customBackgroundColor;
   public static SliderSetting backgroundOpacity;
   public static BooleanSetting customHeaderColour;
   public static ColorSetting customTextColor;
   public static ColorSetting customAccentColor;
   private static Map<String, ClickGUIModule.ThemeColors> themes = new HashMap();

   public ClickGUIModule() {
      super("ClickGUI", "Opens the ClickGUI interface", Category.CLIENT);
      this.initializeThemes();
      theme = new ModeSetting("Theme", new String[]{"Tenacity", "Dark Purple", "Cyberpunk", "Ocean Blue", "Forest Green", "Sunset Orange", "Cherry Blossom", "Midnight", "Lava", "Custom"});
      iconColor = new ModeSetting("Icon Color", new String[]{"Black", "White"});
      gradientMenus = new BooleanSetting("Gradient Menus", false);
      customBackgroundColor = new ColorSetting("Custom Background", new Color(24, 24, 30, 220));
      backgroundOpacity = new SliderSetting("Background Opacity", 100.0D, 0.0D, 100.0D, 0);
      customHeaderColour = new BooleanSetting("Custom Header Colour", false);
      customTextColor = new ColorSetting("Custom Text", new Color(238, 238, 238));
      customAccentColor = new ColorSetting("Custom Accent", new Color(255, 120, 185));
      this.addSetting(theme);
      this.addSetting(iconColor);
      this.addSetting(gradientMenus);
      this.addSetting(customBackgroundColor);
      this.addSetting(backgroundOpacity);
      this.addSetting(customHeaderColour);
      this.addSetting(customTextColor);
      this.addSetting(customAccentColor);
   }

   private void initializeThemes() {
      themes.put("Tenacity", new ClickGUIModule.ThemeColors(new Color(25, 25, 25, 220), new Color(255, 180, 255, 200), new Color(215, 138, 255, 255), new Color(215, 138, 255, 255), new Color(30, 30, 30, 255), new Color(35, 35, 35, 255), new Color(215, 138, 255, 150), new Color(255, 255, 255, 255), new Color(100, 100, 100, 255), new Color(40, 45, 55, 140), new Color(255, 255, 255, 12), new Color(255, 255, 255, 255), new Color(170, 170, 170, 255), new Color(0, 255, 128, 255), new Color(255, 50, 50, 255), new Color(25, 25, 25, 180), new Color(255, 255, 255, 200), new Color(20, 20, 20, 230), new Color(215, 138, 255, 180), new Color(255, 255, 255, 255), new Color(255, 105, 180, 255), new Color(135, 206, 250, 255)));
      themes.put("Dark Purple", new ClickGUIModule.ThemeColors(new Color(20, 15, 30, 220), new Color(138, 43, 226, 200), new Color(186, 85, 211, 255), new Color(147, 112, 219, 255), new Color(25, 20, 35, 255), new Color(30, 25, 40, 255), new Color(138, 43, 226, 150), new Color(240, 230, 255, 255), new Color(120, 100, 140, 255), new Color(30, 25, 40, 140), new Color(255, 255, 255, 12), new Color(240, 230, 255, 255), new Color(160, 160, 180, 255), new Color(138, 43, 226, 255), new Color(220, 20, 60, 255), new Color(20, 15, 30, 180), new Color(180, 170, 200, 255), new Color(15, 10, 25, 230), new Color(138, 43, 226, 180), new Color(240, 230, 255, 255), new Color(138, 43, 226, 255), new Color(186, 85, 211, 255)));
      themes.put("Cyberpunk", new ClickGUIModule.ThemeColors(new Color(10, 10, 15, 220), new Color(0, 255, 255, 200), new Color(255, 0, 255, 255), new Color(0, 255, 255, 255), new Color(15, 15, 20, 255), new Color(20, 20, 30, 255), new Color(0, 255, 255, 150), new Color(0, 255, 255, 255), new Color(100, 100, 120, 255), new Color(20, 20, 30, 140), new Color(255, 255, 255, 12), new Color(0, 255, 255, 255), new Color(150, 150, 170, 255), new Color(0, 255, 255, 255), new Color(255, 0, 100, 255), new Color(10, 10, 15, 180), new Color(200, 200, 220, 255), new Color(5, 5, 10, 230), new Color(255, 0, 255, 180), new Color(0, 255, 255, 255), new Color(255, 0, 255, 255), new Color(0, 255, 255, 255)));
      themes.put("Ocean Blue", new ClickGUIModule.ThemeColors(new Color(15, 25, 35, 220), new Color(64, 156, 255, 200), new Color(100, 180, 255, 255), new Color(64, 156, 255, 255), new Color(20, 30, 40, 255), new Color(25, 35, 45, 255), new Color(64, 156, 255, 150), new Color(220, 240, 255, 255), new Color(100, 130, 160, 255), new Color(25, 35, 45, 140), new Color(255, 255, 255, 12), new Color(220, 240, 255, 255), new Color(160, 180, 200, 255), new Color(0, 200, 255, 255), new Color(255, 100, 100, 255), new Color(15, 25, 35, 180), new Color(180, 200, 220, 255), new Color(10, 20, 30, 230), new Color(64, 156, 255, 180), new Color(220, 240, 255, 255), new Color(30, 144, 255, 255), new Color(135, 206, 250, 255)));
      themes.put("Forest Green", new ClickGUIModule.ThemeColors(new Color(15, 25, 15, 220), new Color(50, 205, 50, 200), new Color(124, 252, 0, 255), new Color(50, 205, 50, 255), new Color(20, 30, 20, 255), new Color(25, 35, 25, 255), new Color(50, 205, 50, 150), new Color(240, 255, 240, 255), new Color(100, 130, 100, 255), new Color(25, 35, 25, 140), new Color(255, 255, 255, 12), new Color(240, 255, 240, 255), new Color(170, 180, 170, 255), new Color(50, 205, 50, 255), new Color(255, 69, 0, 255), new Color(15, 25, 15, 180), new Color(190, 200, 190, 255), new Color(10, 20, 10, 230), new Color(50, 205, 50, 180), new Color(240, 255, 240, 255), new Color(34, 139, 34, 255), new Color(124, 252, 0, 255)));
      themes.put("Sunset Orange", new ClickGUIModule.ThemeColors(new Color(30, 20, 15, 220), new Color(255, 140, 0, 200), new Color(255, 165, 0, 255), new Color(255, 140, 0, 255), new Color(35, 25, 20, 255), new Color(40, 30, 25, 255), new Color(255, 140, 0, 150), new Color(255, 245, 230, 255), new Color(140, 110, 90, 255), new Color(40, 30, 25, 140), new Color(255, 255, 255, 12), new Color(255, 245, 230, 255), new Color(180, 170, 160, 255), new Color(255, 215, 0, 255), new Color(220, 20, 60, 255), new Color(30, 20, 15, 180), new Color(200, 190, 180, 255), new Color(25, 15, 10, 230), new Color(255, 140, 0, 180), new Color(255, 245, 230, 255), new Color(255, 69, 0, 255), new Color(255, 215, 0, 255)));
      themes.put("Cherry Blossom", new ClickGUIModule.ThemeColors(new Color(30, 20, 25, 220), new Color(255, 182, 193, 200), new Color(255, 192, 203, 255), new Color(255, 182, 193, 255), new Color(35, 25, 30, 255), new Color(40, 30, 35, 255), new Color(255, 182, 193, 150), new Color(255, 240, 245, 255), new Color(140, 110, 120, 255), new Color(40, 30, 35, 140), new Color(255, 255, 255, 12), new Color(255, 240, 245, 255), new Color(180, 170, 175, 255), new Color(255, 192, 203, 255), new Color(199, 21, 133, 255), new Color(30, 20, 25, 180), new Color(200, 190, 195, 255), new Color(25, 15, 20, 230), new Color(255, 182, 193, 180), new Color(255, 240, 245, 255), new Color(255, 105, 180, 255), new Color(255, 192, 203, 255)));
      themes.put("Midnight", new ClickGUIModule.ThemeColors(new Color(10, 10, 20, 220), new Color(70, 130, 180, 200), new Color(135, 206, 250, 255), new Color(70, 130, 180, 255), new Color(15, 15, 25, 255), new Color(20, 20, 30, 255), new Color(70, 130, 180, 150), new Color(230, 240, 255, 255), new Color(80, 90, 110, 255), new Color(20, 20, 30, 140), new Color(255, 255, 255, 12), new Color(230, 240, 255, 255), new Color(150, 160, 180, 255), new Color(100, 200, 255, 255), new Color(220, 20, 60, 255), new Color(10, 10, 20, 180), new Color(170, 180, 200, 255), new Color(5, 5, 15, 230), new Color(70, 130, 180, 180), new Color(230, 240, 255, 255), new Color(25, 25, 112, 255), new Color(135, 206, 250, 255)));
      themes.put("Lava", new ClickGUIModule.ThemeColors(new Color(30, 10, 5, 220), new Color(255, 69, 0, 200), new Color(255, 99, 71, 255), new Color(255, 69, 0, 255), new Color(35, 15, 10, 255), new Color(40, 20, 15, 255), new Color(255, 69, 0, 150), new Color(255, 230, 200, 255), new Color(130, 90, 80, 255), new Color(40, 20, 15, 140), new Color(255, 255, 255, 12), new Color(255, 230, 200, 255), new Color(170, 150, 140, 255), new Color(255, 140, 0, 255), new Color(139, 0, 0, 255), new Color(30, 10, 5, 180), new Color(190, 170, 160, 255), new Color(25, 5, 0, 230), new Color(255, 69, 0, 180), new Color(255, 230, 200, 255), new Color(178, 34, 34, 255), new Color(255, 140, 0, 255)));
   }

   public void onEnable() {
      if (this.mc.field_1755 == null) {
         this.mc.method_1507(new ClickGUI());
      }

      this.setEnabled(false);
   }

   private static ClickGUIModule.ThemeColors getCurrentTheme() {
      if (theme == null) {
         return (ClickGUIModule.ThemeColors)themes.get("Tenacity");
      } else {
         return "Custom".equals(theme.getCurrentMode()) ? buildCustomTheme() : (ClickGUIModule.ThemeColors)themes.getOrDefault(theme.getCurrentMode(), (ClickGUIModule.ThemeColors)themes.get("Tenacity"));
      }
   }

   private static ClickGUIModule.ThemeColors buildCustomTheme() {
      Color baseSource = customBackgroundColor != null ? customBackgroundColor.getValue() : new Color(24, 24, 30, 220);
      Color base = withAlpha(baseSource, 220);
      Color text = customTextColor != null ? withAlpha(customTextColor.getValue(), 255) : new Color(238, 238, 238, 255);
      Color accent = customAccentColor != null ? withAlpha(customAccentColor.getValue(), 255) : new Color(255, 120, 185, 255);
      Color panelBg = withAlpha(base, 220);
      Color panelGlow = withAlpha(accent, 180);
      Color panelParticles = withAlpha(accent, 210);
      Color searchBg = withAlpha(darken(base, 0.14D), 170);
      Color searchFocusedBg = withAlpha(darken(base, 0.06D), 208);
      Color searchBorder = withAlpha(accent, 165);
      Color moduleBg = withAlpha(darken(base, 0.18D), 150);
      Color moduleHoverBg = withAlpha(darken(base, 0.1D), 188);
      Color moduleDisabledText = withAlpha(blend(text, base, 0.35D), 230);
      Color moduleDisabledDot = withAlpha(blend(text, base, 0.55D), 200);
      Color settingsBg = withAlpha(darken(base, 0.12D), 160);
      Color tooltipBg = withAlpha(darken(base, 0.2D), 235);
      Color tooltipBorder = withAlpha(accent, 170);
      Color placeholder = withAlpha(blend(text, base, 0.5D), 210);
      Color gradStart = brighten(accent, 0.12D);
      Color gradEnd = darken(accent, 0.2D);
      return new ClickGUIModule.ThemeColors(panelBg, panelGlow, text, panelParticles, searchBg, searchFocusedBg, searchBorder, text, placeholder, moduleBg, moduleHoverBg, text, moduleDisabledText, accent, moduleDisabledDot, settingsBg, text, tooltipBg, tooltipBorder, text, gradStart, gradEnd);
   }

   public static Color getPanelBackground() {
      return applyBackgroundOpacity(getCurrentTheme().panelBackground);
   }

   public static Color getHeaderBackground() {
      Color panelBg = getPanelBackground();
      return customHeaderColour != null && customHeaderColour.isEnabled() ? withAlpha(darken(panelBg, 0.06D), panelBg.getAlpha()) : panelBg;
   }

   public static boolean useCustomHeaderColour() {
      return customHeaderColour != null && customHeaderColour.isEnabled();
   }

   public static Color getPanelTitleGlow() {
      return applyBackgroundOpacity(getCurrentTheme().panelTitleGlow);
   }

   public static Color getPanelTitleText() {
      return getCurrentTheme().panelTitleText;
   }

   public static Color getPanelParticles() {
      return applyBackgroundOpacity(getCurrentTheme().panelParticles);
   }

   public static Color getSearchBoxBackground() {
      return applyBackgroundOpacity(getCurrentTheme().searchBoxBackground);
   }

   public static Color getSearchBoxFocusedBackground() {
      return applyBackgroundOpacity(getCurrentTheme().searchBoxFocusedBackground);
   }

   public static Color getSearchBoxBorder() {
      return applyBackgroundOpacity(getCurrentTheme().searchBoxBorder);
   }

   public static Color getSearchBoxText() {
      return getCurrentTheme().searchBoxText;
   }

   public static Color getSearchBoxPlaceholder() {
      return getCurrentTheme().searchBoxPlaceholder;
   }

   public static Color getModuleButtonBackground() {
      return applyBackgroundOpacity(getCurrentTheme().moduleButtonBackground);
   }

   public static Color getModuleButtonHoverBackground() {
      return applyBackgroundOpacity(getCurrentTheme().moduleButtonHoverBackground);
   }

   public static Color getModuleEnabledText() {
      return getCurrentTheme().moduleEnabledText;
   }

   public static Color getModuleDisabledText() {
      return getCurrentTheme().moduleDisabledText;
   }

   public static Color getModuleEnabledDot() {
      return getCurrentTheme().moduleEnabledDot;
   }

   public static Color getModuleDisabledDot() {
      return getCurrentTheme().moduleDisabledDot;
   }

   public static Color getSettingsBackground() {
      return applyBackgroundOpacity(getCurrentTheme().settingsBackground);
   }

   public static Color getSettingsText() {
      return getCurrentTheme().settingsText;
   }

   public static Color getTooltipBackground() {
      return applyBackgroundOpacity(getCurrentTheme().tooltipBackground);
   }

   public static Color getTooltipBorder() {
      return applyBackgroundOpacity(getCurrentTheme().tooltipBorder);
   }

   public static Color getTooltipText() {
      return getCurrentTheme().tooltipText;
   }

   public static Color getModuleEnabledGradientStart() {
      return getCurrentTheme().moduleEnabledGradientStart;
   }

   public static Color getModuleEnabledGradientEnd() {
      return getCurrentTheme().moduleEnabledGradientEnd;
   }

   public static float getCornerRadius() {
      return 60.0F;
   }

   public static float getCornerRadiusScaled(double factor) {
      return (float)Math.max(0.0D, (double)getCornerRadius() * factor);
   }

   public static float getGuiCornerRadius() {
      return Math.max(0.0F, Math.min(36.0F, getCornerRadius() * 0.6F));
   }

   public static float getGuiCornerRadiusScaled(double factor) {
      return (float)Math.max(0.0D, (double)getGuiCornerRadius() * factor);
   }

   public static String getIconColorMode() {
      return iconColor == null ? "Black" : iconColor.getCurrentMode();
   }

   public static boolean useBlackIcons() {
      return "Black".equalsIgnoreCase(getIconColorMode());
   }

   public static boolean useGradientMenus() {
      return gradientMenus != null && gradientMenus.isEnabled();
   }

   public static boolean useShadows() {
      return true;
   }

   public static float getGradientQualityFactor() {
      return 1.0F;
   }

   private static Color applyBackgroundOpacity(Color color) {
      if (backgroundOpacity == null) {
         return color;
      } else {
         double percent = Math.max(0.0D, Math.min(100.0D, backgroundOpacity.getValue()));
         int alpha = (int)Math.round((double)color.getAlpha() * (percent / 100.0D));
         return withAlpha(color, alpha);
      }
   }

   public static Color getColor(Color color, double alpha) {
      int computedAlpha = (int)Math.round((double)color.getAlpha() * alpha);
      computedAlpha = Math.max(0, Math.min(255, computedAlpha));
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), computedAlpha);
   }

   private static Color withAlpha(Color color, int alpha) {
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, alpha)));
   }

   private static Color blend(Color a, Color b, double t) {
      double clamped = Math.max(0.0D, Math.min(1.0D, t));
      int r = (int)Math.round((double)a.getRed() + (double)(b.getRed() - a.getRed()) * clamped);
      int g = (int)Math.round((double)a.getGreen() + (double)(b.getGreen() - a.getGreen()) * clamped);
      int bl = (int)Math.round((double)a.getBlue() + (double)(b.getBlue() - a.getBlue()) * clamped);
      int al = (int)Math.round((double)a.getAlpha() + (double)(b.getAlpha() - a.getAlpha()) * clamped);
      return new Color(Math.max(0, Math.min(255, r)), Math.max(0, Math.min(255, g)), Math.max(0, Math.min(255, bl)), Math.max(0, Math.min(255, al)));
   }

   private static Color darken(Color color, double amount) {
      double clamped = Math.max(0.0D, Math.min(1.0D, amount));
      int r = (int)Math.round((double)color.getRed() * (1.0D - clamped));
      int g = (int)Math.round((double)color.getGreen() * (1.0D - clamped));
      int b = (int)Math.round((double)color.getBlue() * (1.0D - clamped));
      return new Color(Math.max(0, Math.min(255, r)), Math.max(0, Math.min(255, g)), Math.max(0, Math.min(255, b)), color.getAlpha());
   }

   private static Color brighten(Color color, double amount) {
      double clamped = Math.max(0.0D, Math.min(1.0D, amount));
      int r = (int)Math.round((double)color.getRed() + (double)(255 - color.getRed()) * clamped);
      int g = (int)Math.round((double)color.getGreen() + (double)(255 - color.getGreen()) * clamped);
      int b = (int)Math.round((double)color.getBlue() + (double)(255 - color.getBlue()) * clamped);
      return new Color(Math.max(0, Math.min(255, r)), Math.max(0, Math.min(255, g)), Math.max(0, Math.min(255, b)), color.getAlpha());
   }

   @Environment(EnvType.CLIENT)
   private static class ThemeColors {
      public final Color panelBackground;
      public final Color panelTitleGlow;
      public final Color panelTitleText;
      public final Color panelParticles;
      public final Color searchBoxBackground;
      public final Color searchBoxFocusedBackground;
      public final Color searchBoxBorder;
      public final Color searchBoxText;
      public final Color searchBoxPlaceholder;
      public final Color moduleButtonBackground;
      public final Color moduleButtonHoverBackground;
      public final Color moduleEnabledText;
      public final Color moduleDisabledText;
      public final Color moduleEnabledDot;
      public final Color moduleDisabledDot;
      public final Color settingsBackground;
      public final Color settingsText;
      public final Color tooltipBackground;
      public final Color tooltipBorder;
      public final Color tooltipText;
      public final Color moduleEnabledGradientStart;
      public final Color moduleEnabledGradientEnd;

      public ThemeColors(Color panelBackground, Color panelTitleGlow, Color panelTitleText, Color panelParticles, Color searchBoxBackground, Color searchBoxFocusedBackground, Color searchBoxBorder, Color searchBoxText, Color searchBoxPlaceholder, Color moduleButtonBackground, Color moduleButtonHoverBackground, Color moduleEnabledText, Color moduleDisabledText, Color moduleEnabledDot, Color moduleDisabledDot, Color settingsBackground, Color settingsText, Color tooltipBackground, Color tooltipBorder, Color tooltipText, Color moduleEnabledGradientStart, Color moduleEnabledGradientEnd) {
         this.panelBackground = panelBackground;
         this.panelTitleGlow = panelTitleGlow;
         this.panelTitleText = panelTitleText;
         this.panelParticles = panelParticles;
         this.searchBoxBackground = searchBoxBackground;
         this.searchBoxFocusedBackground = searchBoxFocusedBackground;
         this.searchBoxBorder = searchBoxBorder;
         this.searchBoxText = searchBoxText;
         this.searchBoxPlaceholder = searchBoxPlaceholder;
         this.moduleButtonBackground = moduleButtonBackground;
         this.moduleButtonHoverBackground = moduleButtonHoverBackground;
         this.moduleEnabledText = moduleEnabledText;
         this.moduleDisabledText = moduleDisabledText;
         this.moduleEnabledDot = moduleEnabledDot;
         this.moduleDisabledDot = moduleDisabledDot;
         this.settingsBackground = settingsBackground;
         this.settingsText = settingsText;
         this.tooltipBackground = tooltipBackground;
         this.tooltipBorder = tooltipBorder;
         this.tooltipText = tooltipText;
         this.moduleEnabledGradientStart = moduleEnabledGradientStart;
         this.moduleEnabledGradientEnd = moduleEnabledGradientEnd;
      }
   }
}
