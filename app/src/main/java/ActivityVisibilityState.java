package de.ur.mi.android.tasks.eggtimer;

public class ActivityVisibilityState {
   private static boolean mIsVisible = false;

   public static void setIsVisible(boolean visible) {
      mIsVisible = visible;
   }

   public static boolean getIsVisible() {
      return mIsVisible;
   }
}
