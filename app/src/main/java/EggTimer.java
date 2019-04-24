package de.ur.mi.android.tasks.eggtimer;

import android.content.ContentValues;
import android.os.CountDownTimer;

import de.mi.eggtimer.R;
import de.ur.mi.android.tasks.eggtimer.listener.OnEggTimerStatusChangedListener;

public class EggTimer {

   // Dies sind Wertpaare zur Zuweisung von Kochzeiten zu
   // den ausgewählten Punkten im Spinner, der im Prinzip
   // die Ordnung der Begriffe im zugehörigen string-Array (strings.xml)
   // übernimmt. Deshalb ist die Arrayposition 1 (also Index 0)
   // hier ein "kleines Ei"
   public static final int EGGSIZE_SMALL = 0;
   public static final int VALUE_EGGSIZE_SMALL = 1; // 1 Minute

   public static final int EGGSIZE_MEDIUM = 1;
   public static final int VALUE_EGGSIZE_MEDIUM = 3;

   public static final int EGGSIZE_BIG = 2;
   public static final int VALUE_EGGSIZE_BIG = 5;

   public static final int VALUE_EGGSIZE_DEFAULT = 2; // sofern keine Auswahl getroffen wird

   // Dieselbe Zuweisung gilt für den Zweiten Spinner:
   // Array (0: kurz bzw. weich, 1: lang bzw. hart)
   public static final int PRODUCTIONTIME_SHORT = 0;
   public static final int VALUE_PRODUCTIONTIME_SHORT = 2;

   public static final int PRODUCTIONTIME_LONG = 1;
   public static final int VALUE_PRODUCTIONTIME_LONG = 4;

   public static final int VALUE_PRODUCTIONTIME_DEFAULT = 3;

   // Läuft der Timer gerade? Auslagerung der Werte in Configdatei
   public static final int TIMERSTATUS_STOPPED = 0;
   public static final int TIMERSTATUS_RUNNING = 1;

   // Schlüssel für das Contentvalues-Objekt der MIN:SEC Darstellung
   // werden hier hinterlegt, Fehler zu vermeiden
   public static final String KEY_MINUTES = "Minuten";
   public static final String KEY_SECONDS = "Sekunden";


   private MyCountDownTimer myCountDownTimer;
   private OnEggTimerStatusChangedListener onEggTimerStatusChangedListener;

   private int status = TIMERSTATUS_STOPPED;

   public EggTimer(OnEggTimerStatusChangedListener onEggTimerStatusChangedListener, int selectedEggSize, int selectedCookTime) {

      this.onEggTimerStatusChangedListener = onEggTimerStatusChangedListener;
      myCountDownTimer = new MyCountDownTimer(calculateEggProductionTime(selectedEggSize, selectedCookTime), 1000);
   }

   private long calculateEggProductionTime(int selectedEggSize, int selectedCookSize) {
      // Zeit in Millisekunden - vorerst in Minuten (in config werden minuten
      // gespeichert)
      long timeInMinutes = 0;

      // Die Zuweisung der Kochzeit zu einem ausgewählten Punkt
      // im Spinner funktioniert folgendermaßen:
      // StringArray hält die Label in Spinner-Reihenfolge
      // Spinner wird nach Array gefällt
      // Positionen der Einträge im Spinner entsprechen denen im Array
      // die Zuweisung der Werte zu den Labels geschieht mittels der
      // Values-Klasse in welcher die Indizes in "sprechenden" Konstanten gespeichert
      // werden. Diese Konstanten existieren als Paar. Die Partnerkonstante beginnt
      // entsprechend mit VALUE_
      switch (selectedEggSize) {
         case EGGSIZE_SMALL:
            timeInMinutes = VALUE_EGGSIZE_SMALL;
            break;
         case EGGSIZE_MEDIUM:
            timeInMinutes = VALUE_EGGSIZE_MEDIUM;
            break;
         case EGGSIZE_BIG:
            timeInMinutes = VALUE_EGGSIZE_BIG;
            break;
         default:
            timeInMinutes = VALUE_EGGSIZE_DEFAULT;
      }

      switch (selectedCookSize) {
         case PRODUCTIONTIME_SHORT:
            timeInMinutes += VALUE_PRODUCTIONTIME_SHORT;
            break;
         case PRODUCTIONTIME_LONG:
            timeInMinutes += VALUE_PRODUCTIONTIME_LONG;
            break;
         default:
            timeInMinutes += VALUE_PRODUCTIONTIME_DEFAULT;
      }

      // Millisekunden erzeugen
      return timeInMinutes * 60000;
   }

   public void start() {
      myCountDownTimer.start();
   }

   public void cancel() {
      myCountDownTimer.cancel();
   }

   public int getTimerStatus() {
       return status;
   }

   class MyCountDownTimer extends CountDownTimer {

      private long millisInFuture;

      public MyCountDownTimer(long millisInFuture,
                              long countDownInterval) {
         super(millisInFuture, countDownInterval);

         this.millisInFuture = millisInFuture;
         updateTimerView();
      }

      @Override
      public void onFinish() {
         onEggTimerStatusChangedListener.onResetCountdownView(R.string.standard);
         status = TIMERSTATUS_STOPPED;
         onEggTimerStatusChangedListener.onEggTimerFinished();
      }

      @Override
      public void onTick(long millisUntilFinished) {
         status = TIMERSTATUS_RUNNING;
         millisInFuture = millisUntilFinished;
         updateTimerView();
      }

      private void updateTimerView() {

         ContentValues formattedTime = getFormattedTime();
         String mins = formattedTime.getAsString(KEY_MINUTES);
         String seks = formattedTime.getAsString(KEY_SECONDS);

         onEggTimerStatusChangedListener.onUpdateCountdownView(mins + ":" + seks);
      }

      private ContentValues getFormattedTime() {

         ContentValues formattedTime = new ContentValues();

         int min = (int) (millisInFuture / 60000);
         int sek = ((int) (millisInFuture / 1000)) - (min * 60);
         // Sekunden sind der Rest von millisInFuture, es müssen also die Minuten subtrahiert werden

         String sekunden = String.valueOf(sek);
         if (sek < 10) {
            sekunden = "0" + sekunden;
         }

         String minuten = String.valueOf(min);
         if (min < 10) {
            minuten = "0" + minuten;
         }

         formattedTime.put(KEY_SECONDS, sekunden);
         formattedTime.put(KEY_MINUTES, minuten);

         return formattedTime;
      }
   }

}
