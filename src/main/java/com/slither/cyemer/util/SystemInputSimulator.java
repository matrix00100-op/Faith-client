package com.slither.cyemer.util;

import com.slither.cyemer.mixin.KeyBindingAccessor;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.win32.W32APIOptions;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_3675.class_306;
import net.minecraft.class_3675.class_307;

@Environment(EnvType.CLIENT)
public final class SystemInputSimulator {
   private static final SystemInputSimulator.IInputSimulator SIMULATOR;
   private static final boolean USE_NATIVE;

   private SystemInputSimulator() {
   }

   private static boolean isAndroidOrPojav() {
      String jvmArg = System.getProperty("cyemer.force.vanilla");
      if ("true".equalsIgnoreCase(jvmArg)) {
         return true;
      } else {
         try {
            File runDir = new File(".");
            if ((new File(runDir, "cyemer_force_vanilla")).exists() || (new File(runDir, "cyemer_force_vanilla.txt")).exists()) {
               System.out.println("[InputSim] Override file detected.");
               return true;
            }
         } catch (Throwable var6) {
         }

         if (System.getenv("POJAVEXEC_EGL") == null && System.getenv("POJAV_RENDERER") == null) {
            if (Platform.isAndroid()) {
               return true;
            } else {
               String vendor = System.getProperty("java.vendor", "").toLowerCase();
               String vmName = System.getProperty("java.vm.name", "").toLowerCase();
               String userHome = System.getProperty("user.home", "");
               if (vendor.contains("android")) {
                  return true;
               } else if (!vmName.contains("dalvik") && !vmName.contains("art")) {
                  if (!userHome.contains("/storage/emulated") && !userHome.contains("/data/user") && !userHome.contains("/data/data")) {
                     String os = System.getProperty("os.name", "").toLowerCase();
                     String arch = System.getProperty("os.arch", "").toLowerCase();
                     if (!os.contains("linux") || !arch.equals("aarch64") && !arch.startsWith("arm")) {
                        return false;
                     } else {
                        return System.getProperty("android.os.Build.VERSION") != null;
                     }
                  } else {
                     return true;
                  }
               } else {
                  return true;
               }
            }
         } else {
            return true;
         }
      }
   }

   public static void pressAttack() {
      SIMULATOR.simulatePress(getClient().field_1690.field_1886);
   }

   public static void releaseAttack() {
      SIMULATOR.simulateRelease(getClient().field_1690.field_1886);
   }

   public static void pressUse() {
      SIMULATOR.simulatePress(getClient().field_1690.field_1904);
   }

   public static void releaseUse() {
      SIMULATOR.simulateRelease(getClient().field_1690.field_1904);
   }

   public static void pressForward() {
      SIMULATOR.simulatePress(getClient().field_1690.field_1894);
   }

   public static void releaseForward() {
      SIMULATOR.simulateRelease(getClient().field_1690.field_1894);
   }

   public static void pressBack() {
      SIMULATOR.simulatePress(getClient().field_1690.field_1881);
   }

   public static void releaseBack() {
      SIMULATOR.simulateRelease(getClient().field_1690.field_1881);
   }

   public static void pressSneak() {
      SIMULATOR.simulatePress(getClient().field_1690.field_1832);
   }

   public static void releaseSneak() {
      SIMULATOR.simulateRelease(getClient().field_1690.field_1832);
   }

   public static boolean isUsingNative() {
      return USE_NATIVE;
   }

   private static class_310 getClient() {
      return class_310.method_1551();
   }

   static {
      boolean useNative = true;

      Object impl;
      try {
         if (isAndroidOrPojav()) {
            impl = new SystemInputSimulator.InternalSimulator();
            useNative = false;
            System.out.println("[InputSim] Detected Android/PojavLauncher - using internal simulation (with Accessor)");
         } else if (Platform.isWindows()) {
            impl = new SystemInputSimulator.WindowsSimulator();
            System.out.println("[InputSim] Using Windows native simulation");
         } else if (Platform.isMac()) {
            impl = new SystemInputSimulator.MacSimulator();
            System.out.println("[InputSim] Using macOS native simulation");
         } else if (Platform.isLinux()) {
            impl = new SystemInputSimulator.LinuxSimulator();
            System.out.println("[InputSim] Using Linux native simulation");
         } else {
            impl = new SystemInputSimulator.InternalSimulator();
            useNative = false;
            System.out.println("[InputSim] Unknown platform - using internal simulation");
         }
      } catch (Throwable var3) {
         System.err.println("[InputSim] Native simulation failed, falling back to internal: " + var3.getMessage());
         impl = new SystemInputSimulator.InternalSimulator();
         useNative = false;
      }

      SIMULATOR = (SystemInputSimulator.IInputSimulator)impl;
      USE_NATIVE = useNative;
   }

   @Environment(EnvType.CLIENT)
   private interface IInputSimulator {
      void simulatePress(class_304 var1);

      void simulateRelease(class_304 var1);
   }

   @Environment(EnvType.CLIENT)
   private static class InternalSimulator implements SystemInputSimulator.IInputSimulator {
      public void simulatePress(class_304 keyBinding) {
         try {
            keyBinding.method_23481(true);
            class_310 mc = SystemInputSimulator.getClient();
            if ((keyBinding == mc.field_1690.field_1886 || keyBinding == mc.field_1690.field_1904) && keyBinding instanceof KeyBindingAccessor) {
               KeyBindingAccessor accessor = (KeyBindingAccessor)keyBinding;
               accessor.setTimesPressed(accessor.getTimesPressed() + 1);
            }
         } catch (Exception var4) {
            System.err.println("[InputSim] Failed to press key: " + var4.getMessage());
         }

      }

      public void simulateRelease(class_304 keyBinding) {
         try {
            keyBinding.method_23481(false);
         } catch (Exception var3) {
            System.err.println("[InputSim] Failed to release key: " + var3.getMessage());
         }

      }
   }

   @Environment(EnvType.CLIENT)
   private static class WindowsSimulator implements SystemInputSimulator.IInputSimulator {
      private static final Map<String, Integer> KEY_NAME_TO_CODE_MAP = new HashMap();

      public void simulatePress(class_304 keyBinding) {
         class_306 key = class_3675.method_15981(keyBinding.method_1428());
         keyBinding.method_23481(true);
         if (key.method_1442() == class_307.field_1672) {
            if (key.method_1444() == 0) {
               SystemInputSimulator.WindowsSimulator.NativeUser32.INSTANCE.mouse_event(2, 0, 0, 0, 0);
            } else if (key.method_1444() == 1) {
               SystemInputSimulator.WindowsSimulator.NativeUser32.INSTANCE.mouse_event(8, 0, 0, 0, 0);
            }
         } else if (key.method_1442() == class_307.field_1668) {
            int vk = this.getVirtualKeyCode(key);
            if (vk != 0) {
               SystemInputSimulator.WindowsSimulator.NativeUser32.INSTANCE.keybd_event((byte)vk, (byte)0, 0, 0);
            }
         }

      }

      public void simulateRelease(class_304 keyBinding) {
         class_306 key = class_3675.method_15981(keyBinding.method_1428());
         keyBinding.method_23481(false);
         if (key.method_1442() == class_307.field_1672) {
            if (key.method_1444() == 0) {
               SystemInputSimulator.WindowsSimulator.NativeUser32.INSTANCE.mouse_event(4, 0, 0, 0, 0);
            } else if (key.method_1444() == 1) {
               SystemInputSimulator.WindowsSimulator.NativeUser32.INSTANCE.mouse_event(16, 0, 0, 0, 0);
            }
         } else if (key.method_1442() == class_307.field_1668) {
            int vk = this.getVirtualKeyCode(key);
            if (vk != 0) {
               SystemInputSimulator.WindowsSimulator.NativeUser32.INSTANCE.keybd_event((byte)vk, (byte)0, 2, 0);
            }
         }

      }

      private int getVirtualKeyCode(class_306 key) {
         String translationKey = key.method_1441();
         String name = translationKey.substring(translationKey.lastIndexOf(46) + 1);
         if (KEY_NAME_TO_CODE_MAP.containsKey(name)) {
            return (Integer)KEY_NAME_TO_CODE_MAP.get(name);
         } else {
            return name.length() == 1 ? KeyEvent.getExtendedKeyCodeForChar(name.toUpperCase().charAt(0)) : 0;
         }
      }

      private static void initializeKeyMap() {
         KEY_NAME_TO_CODE_MAP.put("left.shift", 16);
         KEY_NAME_TO_CODE_MAP.put("right.shift", 16);
         KEY_NAME_TO_CODE_MAP.put("left.control", 17);
         KEY_NAME_TO_CODE_MAP.put("right.control", 17);
         KEY_NAME_TO_CODE_MAP.put("left.alt", 18);
         KEY_NAME_TO_CODE_MAP.put("right.alt", 65406);
         KEY_NAME_TO_CODE_MAP.put("space", 32);
         KEY_NAME_TO_CODE_MAP.put("enter", 10);
      }

      static {
         initializeKeyMap();
      }

      @Environment(EnvType.CLIENT)
      private interface NativeUser32 extends User32 {
         SystemInputSimulator.WindowsSimulator.NativeUser32 INSTANCE = (SystemInputSimulator.WindowsSimulator.NativeUser32)Native.load("user32", SystemInputSimulator.WindowsSimulator.NativeUser32.class, W32APIOptions.DEFAULT_OPTIONS);
         int MOUSEEVENTF_LEFTDOWN = 2;
         int MOUSEEVENTF_LEFTUP = 4;
         int MOUSEEVENTF_RIGHTDOWN = 8;
         int MOUSEEVENTF_RIGHTUP = 16;
         int KEYEVENTF_KEYDOWN = 0;
         int KEYEVENTF_KEYUP = 2;

         void mouse_event(int var1, int var2, int var3, int var4, int var5);

         void keybd_event(byte var1, byte var2, int var3, int var4);
      }
   }

   @Environment(EnvType.CLIENT)
   private static class MacSimulator implements SystemInputSimulator.IInputSimulator {
      private static final Map<Integer, Short> LWJGL_TO_MAC = new HashMap();

      public MacSimulator() {
         LWJGL_TO_MAC.put(87, Short.valueOf((short)13));
         LWJGL_TO_MAC.put(65, Short.valueOf((short)0));
         LWJGL_TO_MAC.put(83, Short.valueOf((short)1));
         LWJGL_TO_MAC.put(68, Short.valueOf((short)2));
         LWJGL_TO_MAC.put(340, Short.valueOf((short)56));
         LWJGL_TO_MAC.put(344, Short.valueOf((short)60));
         LWJGL_TO_MAC.put(32, Short.valueOf((short)49));
         LWJGL_TO_MAC.put(341, Short.valueOf((short)59));
         LWJGL_TO_MAC.put(345, Short.valueOf((short)62));
      }

      public void simulatePress(class_304 kb) {
         kb.method_23481(true);
         this.handle(kb, true);
      }

      public void simulateRelease(class_304 kb) {
         kb.method_23481(false);
         this.handle(kb, false);
      }

      private void handle(class_304 kb, boolean press) {
         class_306 k = class_3675.method_15981(kb.method_1428());
         Pointer event = null;

         try {
            if (k.method_1442() == class_307.field_1672) {
               int mouseType = 0;
               int mouseButton = 0;
               if (k.method_1444() == 0) {
                  mouseType = press ? 1 : 2;
                  mouseButton = 0;
               } else if (k.method_1444() == 1) {
                  mouseType = press ? 3 : 4;
                  mouseButton = 1;
               }

               event = SystemInputSimulator.MacSimulator.CoreGraphics.INSTANCE.CGEventCreateMouseEvent((Pointer)null, mouseType, (Pointer)null, mouseButton);
            } else if (k.method_1442() == class_307.field_1668) {
               Short virtualKey = (Short)LWJGL_TO_MAC.get(k.method_1444());
               if (virtualKey != null) {
                  event = SystemInputSimulator.MacSimulator.CoreGraphics.INSTANCE.CGEventCreateKeyboardEvent((Pointer)null, virtualKey, press);
               }
            }

            if (event != null) {
               SystemInputSimulator.MacSimulator.CoreGraphics.INSTANCE.CGEventPost(0, event);
               SystemInputSimulator.MacSimulator.CoreGraphics.INSTANCE.CFRelease(event);
            }
         } catch (Exception var7) {
            System.err.println("[InputSim] macOS event failed: " + var7.getMessage());
         }

      }

      @Environment(EnvType.CLIENT)
      private interface CoreGraphics extends Library {
         SystemInputSimulator.MacSimulator.CoreGraphics INSTANCE = (SystemInputSimulator.MacSimulator.CoreGraphics)Native.load("CoreGraphics", SystemInputSimulator.MacSimulator.CoreGraphics.class);

         Pointer CGEventCreateMouseEvent(Pointer var1, int var2, Pointer var3, int var4);

         Pointer CGEventCreateKeyboardEvent(Pointer var1, short var2, boolean var3);

         void CGEventPost(int var1, Pointer var2);

         void CFRelease(Pointer var1);
      }
   }

   @Environment(EnvType.CLIENT)
   private static class LinuxSimulator implements SystemInputSimulator.IInputSimulator {
      private static final Map<Integer, Long> LWJGL_TO_X11 = new HashMap();
      private final Pointer display;

      public LinuxSimulator() {
         this.display = SystemInputSimulator.LinuxSimulator.X11.INSTANCE.XOpenDisplay((String)null);
         if (this.display == null) {
            throw new RuntimeException("Could not open X11 display");
         } else {
            LWJGL_TO_X11.put(87, 119L);
            LWJGL_TO_X11.put(65, 97L);
            LWJGL_TO_X11.put(83, 115L);
            LWJGL_TO_X11.put(68, 100L);
            LWJGL_TO_X11.put(340, 65505L);
            LWJGL_TO_X11.put(344, 65506L);
            LWJGL_TO_X11.put(32, 32L);
            LWJGL_TO_X11.put(341, 65507L);
            LWJGL_TO_X11.put(345, 65508L);
         }
      }

      public void simulatePress(class_304 kb) {
         kb.method_23481(true);
         this.handle(kb, true);
      }

      public void simulateRelease(class_304 kb) {
         kb.method_23481(false);
         this.handle(kb, false);
      }

      private void handle(class_304 kb, boolean press) {
         if (this.display != null) {
            class_306 k = class_3675.method_15981(kb.method_1428());

            try {
               if (k.method_1442() == class_307.field_1672) {
                  int button = 0;
                  if (k.method_1444() == 0) {
                     button = 1;
                  } else if (k.method_1444() == 1) {
                     button = 3;
                  }

                  if (button != 0) {
                     SystemInputSimulator.LinuxSimulator.XTest.INSTANCE.XTestFakeButtonEvent(this.display, button, press, 0L);
                     SystemInputSimulator.LinuxSimulator.X11.INSTANCE.XFlush(this.display);
                  }
               } else if (k.method_1442() == class_307.field_1668) {
                  Long keysym = (Long)LWJGL_TO_X11.get(k.method_1444());
                  if (keysym != null) {
                     int keycode = SystemInputSimulator.LinuxSimulator.X11.INSTANCE.XKeysymToKeycode(this.display, keysym);
                     if (keycode != 0) {
                        SystemInputSimulator.LinuxSimulator.XTest.INSTANCE.XTestFakeKeyEvent(this.display, keycode, press, 0L);
                        SystemInputSimulator.LinuxSimulator.X11.INSTANCE.XFlush(this.display);
                     }
                  }
               }
            } catch (Exception var6) {
               System.err.println("[InputSim] Linux event failed: " + var6.getMessage());
            }

         }
      }

      @Environment(EnvType.CLIENT)
      private interface X11 extends Library {
         SystemInputSimulator.LinuxSimulator.X11 INSTANCE = (SystemInputSimulator.LinuxSimulator.X11)Native.load("X11", SystemInputSimulator.LinuxSimulator.X11.class);

         Pointer XOpenDisplay(String var1);

         int XKeysymToKeycode(Pointer var1, long var2);

         int XFlush(Pointer var1);
      }

      @Environment(EnvType.CLIENT)
      private interface XTest extends Library {
         SystemInputSimulator.LinuxSimulator.XTest INSTANCE = (SystemInputSimulator.LinuxSimulator.XTest)Native.load("Xtst", SystemInputSimulator.LinuxSimulator.XTest.class);

         void XTestFakeKeyEvent(Pointer var1, int var2, boolean var3, long var4);

         void XTestFakeButtonEvent(Pointer var1, int var2, boolean var3, long var4);
      }
   }
}
