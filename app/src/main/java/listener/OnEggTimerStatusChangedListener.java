package de.ur.mi.android.tasks.eggtimer.listener;

public interface OnEggTimerStatusChangedListener {

   public void onUpdateCountdownView(String toUpdate);

   public void onResetCountdownView(int stringID);

   public void onEggTimerFinished();
}
