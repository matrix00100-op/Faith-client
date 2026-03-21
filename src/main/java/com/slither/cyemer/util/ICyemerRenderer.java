package com.slither.cyemer.util;

import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_332;

@Environment(EnvType.CLIENT)
public interface IFaithRenderer {
   void init();

   boolean beginFrame(float var1, float var2, float var3);

   void endFrame();

   void drawRect(class_332 var1, float var2, float var3, float var4, float var5, Color var6);

   void drawRoundedRect(class_332 var1, float var2, float var3, float var4, float var5, float var6, Color var7);

   void drawGlowingRect(class_332 var1, float var2, float var3, float var4, float var5, float var6, Color var7, Color var8, float var9);

   void drawRectOutline(class_332 var1, float var2, float var3, float var4, float var5, float var6, Color var7);

   void drawRoundedRectOutline(class_332 var1, float var2, float var3, float var4, float var5, float var6, float var7, Color var8);

   void drawRoundedRectGradient(class_332 var1, float var2, float var3, float var4, float var5, float var6, Color var7, Color var8, boolean var9);

   void drawCircle(class_332 var1, float var2, float var3, float var4, Color var5);

   void drawArc(class_332 var1, float var2, float var3, float var4, float var5, float var6, float var7, Color var8);

   void drawBlur(class_332 var1, float var2, float var3, float var4, float var5, float var6);

   void rotate(float var1);

   void drawText(class_332 var1, String var2, float var3, float var4, float var5, Color var6, boolean var7);

   void drawCenteredText(class_332 var1, String var2, float var3, float var4, float var5, Color var6, boolean var7);

   void setFontBlur(float var1);

   float getTextWidth(String var1, float var2);

   float getTextHeight(float var1);

   void drawTexture(class_332 var1, int var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11, float var12);

   void drawTextureRounded(class_332 var1, int var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11, float var12, float var13);

   int createImageFromTexture(int var1, int var2, int var3);

   void deleteImage(int var1);

   int createImageFromFile(String var1);

   void scissor(class_332 var1, float var2, float var3, float var4, float var5);

   void scissor(float var1, float var2, float var3, float var4);

   void resetScissor();

   void save();

   void restore();

   void translate(float var1, float var2);

   void scale(float var1, float var2);

   void cleanup();
}
