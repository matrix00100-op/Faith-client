package com.slither.cyemer.gui.new_ui.notifications;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_327;

@Environment(EnvType.CLIENT)
public class Notification {
   private final String title;
   private final String message;
   private final Notification.NotificationType type;
   private final long creationTime;
   private final long duration;
   private final double width;
   private final double height;
   private static final double ANIMATION_TIME = 400.0D;

   public Notification(String title, String message, Notification.NotificationType type, long duration) {
      this.title = title;
      this.message = message;
      this.type = type;
      this.duration = duration;
      this.creationTime = System.currentTimeMillis();
      class_327 textRenderer = class_310.method_1551().field_1772;
      int titleWidth = textRenderer.method_1727(title);
      int messageWidth = textRenderer.method_1727(message);
      int maxTextWidth = Math.max(titleWidth, messageWidth);
      this.width = (double)(40 + maxTextWidth + 24);
      this.height = message.isEmpty() ? 40.0D : 56.0D;
   }

   public Notification(String title, Notification.NotificationType type, long duration) {
      this(title, "", type, duration);
   }

   public boolean isExpired() {
      return (double)System.currentTimeMillis() > (double)(this.creationTime + this.duration) + 800.0D;
   }

   public double getAniProg() {
      long currentTime = System.currentTimeMillis();
      long timeSinceCreation = currentTime - this.creationTime;
      if ((double)timeSinceCreation < 400.0D) {
         double linear = (double)timeSinceCreation / 400.0D;
         return this.easeOutCubic(linear);
      } else if ((double)currentTime > (double)(this.creationTime + this.duration) + 400.0D) {
         long timeIntoFadeOut = (long)((double)currentTime - ((double)(this.creationTime + this.duration) + 400.0D));
         double linear = 1.0D - Math.min((double)timeIntoFadeOut / 400.0D, 1.0D);
         return this.easeInCubic(linear);
      } else {
         return 1.0D;
      }
   }

   private double easeOutCubic(double x) {
      return 1.0D - Math.pow(1.0D - x, 3.0D);
   }

   private double easeInCubic(double x) {
      return Math.pow(x, 3.0D);
   }

   public String getTitle() {
      return this.title;
   }

   public String getMessage() {
      return this.message;
   }

   public Notification.NotificationType getType() {
      return this.type;
   }

   public double getWidth() {
      return this.width;
   }

   public double getHeight() {
      return this.height;
   }

   public long getCreationTime() {
      return this.creationTime;
   }

   public long getDuration() {
      return this.duration;
   }

   @Environment(EnvType.CLIENT)
   public static enum NotificationType {
      SUCCESS,
      WARNING,
      ERROR,
      INFO;

      // $FF: synthetic method
      private static Notification.NotificationType[] $values() {
         return new Notification.NotificationType[]{SUCCESS, WARNING, ERROR, INFO};
      }
   }
}
