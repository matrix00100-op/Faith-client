package com.slither.cyemer.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public class LogCleaner {
   public static void clean() {
      Path logPath = FabricLoader.getInstance().getGameDir().resolve("logs").resolve("latest.log");
      File file = logPath.toFile();
      if (file.exists()) {
         String[] remove = new String[]{"cyemer", "nanovg"};

         try {
            List<String> lines = new ArrayList();
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;
            try {
               while((line = reader.readLine()) != null) {
                  boolean skip = false;
                  String[] var7 = remove;
                  int var8 = remove.length;

                  for(int var9 = 0; var9 < var8; ++var9) {
                     String keyword = var7[var9];
                     if (line.toLowerCase().contains(keyword.toLowerCase())) {
                        skip = true;
                        break;
                     }
                  }

                  if (!skip) {
                     lines.add(line);
                  }
               }
            } catch (Throwable var14) {
               try {
                  reader.close();
               } catch (Throwable var12) {
                  var14.addSuppressed(var12);
               }

               throw var14;
            }

            reader.close();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            try {
               Iterator var17 = lines.iterator();

               while(var17.hasNext()) {
                  String l = (String)var17.next();
                  writer.write(l);
                  writer.newLine();
               }
            } catch (Throwable var13) {
               try {
                  writer.close();
               } catch (Throwable var11) {
                  var13.addSuppressed(var11);
               }

               throw var13;
            }

            writer.close();
         } catch (IOException var15) {
         }

      }
   }
}
