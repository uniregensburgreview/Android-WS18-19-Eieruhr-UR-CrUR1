package de.ur.mi.android.tasks.eggtimer;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import de.mi.eggtimer.R;
import de.ur.mi.android.tasks.eggtimer.debug.DebugHelper;
import de.ur.mi.android.tasks.eggtimer.listener.OnEggTimerStatusChangedListener;

public class EggTimerActivity extends Activity implements OnEggTimerStatusChangedListener {

    private TextView timerView;
    private Spinner spinnerEggSize;
    private Spinner spinnerDoneness;
    private Button startAndStop;

    private EggTimerService myEggTimerService;
    private ServiceConnection serviceConnection;

    private boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egg_timer);

        DebugHelper.logDebugMessage("onCreate");

        setupUI();
        createEggTimer();

        initServiceConnection();
    }

    private void createEggTimer() {
        int[] itemPositions = getSelectedItemPositions();
        new EggTimer(this, itemPositions[0], itemPositions[1]);
    }

    private int[] getSelectedItemPositions() {
        int[] itemPositions = new int[2];

        itemPositions[0] = spinnerEggSize.getSelectedItemPosition();
        itemPositions[1] = spinnerDoneness.getSelectedItemPosition();

        return itemPositions;
    }

    private void startEggTimerService() {

        Intent intent = new Intent(this, EggTimerService.class);
        int[] itemPositions = getSelectedItemPositions();
        intent.putExtra("selectedSize", itemPositions[0]);
        intent.putExtra("selectedCookTime", itemPositions[1]);
        startService(intent);
    }

    private void modifyButtonLayout(String caption, int colorId) {

        startAndStop.getBackground().setColorFilter(colorId, PorterDuff.Mode.MULTIPLY);
        startAndStop.setText(caption);
    }

    private void setupUI() {

        timerView = (TextView) findViewById(R.id.timerView);
        spinnerEggSize = (Spinner) findViewById(R.id.spinnerEggSize);
        spinnerDoneness = (Spinner) findViewById(R.id.spinnerDoneness);
        startAndStop = (Button) findViewById(R.id.button);

        initButton();
        initSpinner(spinnerEggSize, R.array.eggSizeArray);
        initSpinner(spinnerDoneness, R.array.donenessArray);
    }

    private void initButton() {

        modifyButtonLayout(getString(R.string.start), Color.GREEN);

        startAndStop.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (isRunning) {
                    DebugHelper.logDebugMessage("Service stopping");
                    myEggTimerService.stopTimer();
                    modifyButtonLayout(getString(R.string.start), Color.GREEN);
                    createEggTimer();
                } else {
                    DebugHelper.logDebugMessage("Service starting");
                    modifyButtonLayout(getString(R.string.stop), Color.RED);
                    startEggTimerService();
                }
                isRunning = !isRunning;
            }
        });

    }


    // diese Methode führt zweimal beinahe identischen Code für die zwei
    // Spinner aus und wurde deshalb parametrisiert (spinner und array id)
    private void initSpinner(Spinner spinner, int arrayID) {

        // Adaptersetup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                EggTimerActivity.this, arrayID,
                android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Benötigten Listener Implementieren und die Methoden überschreiben
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View v,
                                       int position, long arg3) {

                if (!isRunning) {
                    // wenn der Timer nicht läuft, muss die Zeit für den neu
                    // ausgewählten wert immer neu berechnet werden ->
                    // hierzu wird immer ein Timerobjekt mit entsprechender Zeit erstellt
                    // dieses ist dann sofort bereit zum Start
                    createEggTimer();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void initServiceConnection() {

        serviceConnection = new ServiceConnection() {

            @Override
            public void onServiceDisconnected(ComponentName name) {
                System.out.println("Service disconnected");
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                System.out.println("Service connected");

                myEggTimerService = ((EggTimerService.LocalBinder) service).getBinder();
                if (myEggTimerService != null)
                    myEggTimerService.setOnEggTimerStatusChangedListener(EggTimerActivity.this);
            }
        };
    }

    @Override
    public void onUpdateCountdownView(String toUpdate) {
        timerView.setText(toUpdate);
    }

    @Override
    public void onEggTimerFinished() {

        // vibriert sofort für 3 sekunden (3000ms)
        // dazu wird zuerst der Systemservice Vibrator geholt und durch einen Cast benutzbar
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(3000);

        // Standardcode zur Erzeugung eines Dialogs
        AlertDialog.Builder dialog = new AlertDialog.Builder(
                EggTimerActivity.this);
        dialog.setTitle(R.string.dialogTitle);
        dialog.setMessage(R.string.dialogTitle);
        dialog.setPositiveButton(R.string.dialogOk, null);
        dialog.show();

        // Verandern der Buttonfarbe von Rot nach Grün (Timer "lief" bzw. läuft)
        modifyButtonLayout(getString(R.string.start), Color.GREEN);

        // Es wird ein neuer Timer mit den selben Einstellungen angelegt. So kann
        createEggTimer();
        isRunning = false;
    }

    @Override
    public void onResetCountdownView(int stringID) {
        timerView.setText(stringID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.egg_timer, menu);
        return true;
    }

    @Override
    protected void onPause() {
        DebugHelper.logDebugMessage("onPause");
        ActivityVisibilityState.setIsVisible(false);
        unbindService(serviceConnection);
        super.onPause();
    }

    @Override
    protected void onResume() {
        DebugHelper.logDebugMessage("onResume");
        ActivityVisibilityState.setIsVisible(true);
        bindService(new Intent(EggTimerActivity.this, EggTimerService.class), serviceConnection, BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        DebugHelper.logDebugMessage("onDestroy");

//		stopService(new Intent(EggTimerActivity.this, EggTimerService.class));
        super.onDestroy();
    }
}
