package com.slither.cyemer.event;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class EventBus {
   private static final Map<Class<? extends Event>, List<EventBus.Listener>> registry = new HashMap();

   public static void register(Object subscriber) {
      Method[] var1 = subscriber.getClass().getDeclaredMethods();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         Method method = var1[var3];
         if (method.isAnnotationPresent(EventTarget.class) && method.getParameterCount() == 1) {
            Class<?> eventType = method.getParameterTypes()[0];
            if (Event.class.isAssignableFrom(eventType)) {
               EventTarget annotation = (EventTarget)method.getAnnotation(EventTarget.class);
               EventBus.Listener listener = new EventBus.Listener(subscriber, method, annotation.priority());
               ((List)registry.computeIfAbsent(eventType, (k) -> {
                  return new CopyOnWriteArrayList();
               })).add(listener);
               ((List)registry.get(eventType)).sort(Comparator.comparingInt(EventBus.Listener::getPriority).reversed());
            }
         }
      }

   }

   public static void unregister(Object subscriber) {
      registry.values().forEach((listeners) -> {
         listeners.removeIf((listener) -> {
            return listener.subscriber == subscriber;
         });
      });
   }

   public static void post(Event event) {
      List<EventBus.Listener> listeners = (List)registry.get(event.getClass());
      if (listeners != null) {
         Iterator var2 = listeners.iterator();

         while(var2.hasNext()) {
            EventBus.Listener listener = (EventBus.Listener)var2.next();

            try {
               listener.method.setAccessible(true);
               listener.method.invoke(listener.subscriber, event);
            } catch (Exception var5) {
               var5.printStackTrace();
            }
         }
      }

   }

   @Environment(EnvType.CLIENT)
   private static class Listener {
      private final Object subscriber;
      private final Method method;
      private final byte priority;

      public Listener(Object subscriber, Method method, byte priority) {
         this.subscriber = subscriber;
         this.method = method;
         this.priority = priority;
      }

      public int getPriority() {
         return this.priority;
      }
   }
}
