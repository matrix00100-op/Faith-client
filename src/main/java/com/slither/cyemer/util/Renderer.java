package com.slither.cyemer.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Renderer {
   private static IFaithRenderer instance;
   private static Boolean cachedIsAndroid = null;

   public static IFaithRenderer get() {
      if (instance == null) {
         setupRenderer();
      }

      return instance;
   }

   private static void setupRenderer() {
      instance = new VanillaRendererImpl();
      instance.init();
   }

   static boolean isAndroidOrPojav() {
      if (cachedIsAndroid != null) {
         return cachedIsAndroid;
      } else {
         String forceNvg = System.getProperty("cyemer.force.nanovg");
         if ("true".equalsIgnoreCase(forceNvg)) {
            return cachedIsAndroid = false;
         } else {
            String jvmArg = System.getProperty("cyemer.force.vanilla");
            if ("true".equalsIgnoreCase(jvmArg)) {
               return cachedIsAndroid = true;
            } else if (System.getenv("POJAVEXEC_EGL") == null && System.getenv("POJAV_RENDERER") == null) {
               String vendor = System.getProperty("java.vendor");
               if (vendor != null && vendor.toLowerCase().contains("android")) {
                  return cachedIsAndroid = true;
               } else {
                  String vmName = System.getProperty("java.vm.name");
                  if (vmName != null && vmName.toLowerCase().contains("dalvik")) {
                     return cachedIsAndroid = true;
                  } else {
                     String userHome = System.getProperty("user.home");
                     if (userHome == null || !userHome.contains("/storage/emulated") && !userHome.contains("/data/user") && !userHome.contains("/data/data")) {
                        String os = System.getProperty("os.name");
                        String arch = System.getProperty("os.arch");
                        boolean isLinux = os != null && os.toLowerCase().contains("linux");
                        boolean isArm = arch != null && (arch.equals("aarch64") || arch.startsWith("arm"));
                        if (isLinux && isArm) {
                           String androidBuild = System.getProperty("android.os.Build.VERSION");
                           return androidBuild != null ? cachedIsAndroid = true : cachedIsAndroid = false;
                        } else {
                           return cachedIsAndroid = false;
                        }
                     } else {
                        return cachedIsAndroid = true;
                     }
                  }
               }
            } else {
               return cachedIsAndroid = true;
            }
         }
      }
   }

   private static IFaithRenderer createNanoRenderer() {
      return new NanoVGRendererImpl();
   }

   public static void forceVanillaRenderer() {
      try {
         if (instance != null) {
            instance.cleanup();
         }
      } catch (Throwable var1) {
      }

      instance = new VanillaRendererImpl();
      instance.init();
   }
}
