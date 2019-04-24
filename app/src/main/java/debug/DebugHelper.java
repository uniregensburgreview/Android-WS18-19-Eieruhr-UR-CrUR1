package de.ur.mi.android.tasks.eggtimer.debug;

import android.util.Log;

public class DebugHelper {

   private static boolean isDebugging = true;
   private static String logTag = "eggtimer";

   public static void setDebugging(boolean debuggingState, String tag) {
      isDebugging = debuggingState;
      logTag = tag;
   }

   public static void logDebugMessage(String msg) {
      if (isDebugging) {
         Log.d(logTag, msg);
      }
   }

}
