package com.slither.cyemer.module.implementation.targeteffect;

import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;

@Environment(EnvType.CLIENT)
public enum TextureTheme {
   SOLID("Solid", (class_2960)null, false, (String)null),
   LIQUID_METAL("Liquid Metal", (class_2960)null, false, "liquid_metal"),
   LAVA("Lava", (class_2960)null, false, "lava"),
   WATER("Water", (class_2960)null, false, "water");

   private final String displayName;
   private final class_2960 textureId;
   private final boolean animated;
   private final String shaderName;

   private TextureTheme(String displayName, class_2960 textureId, boolean animated, String shaderName) {
      this.displayName = displayName;
      this.textureId = textureId;
      this.animated = animated;
      this.shaderName = shaderName;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public class_2960 getTextureId() {
      return this.textureId;
   }

   public class_2960 getTexture() {
      return this.textureId;
   }

   /** @deprecated */
   @Deprecated
   public boolean isAnimated() {
      return this.animated;
   }

   public String getShaderName() {
      return this.shaderName;
   }

   public boolean usesShader() {
      return this.shaderName != null;
   }

   public boolean hasTexture() {
      return this.usesShader() || this.textureId != null;
   }

   /** @deprecated */
   @Deprecated
   public float[] getAnimatedUVs(long time, double speedMultiplier) {
      return new float[]{0.0F, 0.0F};
   }

   public Color applyThemeTint(Color baseColor) {
      float[] hsb;
      switch(this.ordinal()) {
      case 1:
         hsb = Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (float[])null);
         hsb[1] *= 0.8F;
         hsb[2] *= 1.1F;
         return Color.getHSBColor(hsb[0], hsb[1], Math.min(1.0F, hsb[2]));
      case 2:
         hsb = Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (float[])null);
         hsb[1] = Math.min(1.0F, hsb[1] * 1.2F);
         hsb[2] *= 1.3F;
         return Color.getHSBColor(hsb[0], hsb[1], Math.min(1.0F, hsb[2]));
      case 3:
         hsb = Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (float[])null);
         hsb[0] = (hsb[0] + 0.55F) % 1.0F;
         hsb[1] *= 0.9F;
         return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
      default:
         return baseColor;
      }
   }

   public static TextureTheme fromString(String name) {
      TextureTheme[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         TextureTheme theme = var1[var3];
         if (theme.displayName.equalsIgnoreCase(name)) {
            return theme;
         }
      }

      return SOLID;
   }

   public static String[] getDisplayNames() {
      TextureTheme[] themes = values();
      String[] names = new String[themes.length];

      for(int i = 0; i < themes.length; ++i) {
         names[i] = themes[i].displayName;
      }

      return names;
   }

   // $FF: synthetic method
   private static TextureTheme[] $values() {
      return new TextureTheme[]{SOLID, LIQUID_METAL, LAVA, WATER};
   }
}
