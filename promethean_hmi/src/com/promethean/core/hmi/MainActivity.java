package com.promethean.core.hmi;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class MainActivity extends Activity {
    private static final long REFRESH_MS = 500L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final SimpleDateFormat clockFormat =
            new SimpleDateFormat("h:mm", Locale.getDefault());
    private final VehicleDataSource vehicleDataSource = new DemoVehicleDataSource();

    private TextView clock;
    private TextView speed;
    private TextView gear;
    private TextView battery;
    private TextView range;
    private TextView power;
    private TextView propulsion;
    private TextView vehicle;
    private TextView connection;
    private TextView sectionTitle;
    private TextView sectionBody;

    private final Runnable refresh = new Runnable() {
        @Override
        public void run() {
            render(vehicleDataSource.read());
            clock.setText(clockFormat.format(new Date()));
            handler.postDelayed(this, REFRESH_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enterImmersiveMode();
        setContentView(R.layout.activity_main);
        bindViews();
        bindNavigation();
        selectSection("ENERGY");
    }

    @Override
    protected void onStart() {
        super.onStart();
        handler.removeCallbacks(refresh);
        handler.post(refresh);
    }

    @Override
    protected void onStop() {
        handler.removeCallbacks(refresh);
        super.onStop();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            enterImmersiveMode();
        }
    }

    private void bindViews() {
        clock = findViewById(R.id.clock);
        speed = findViewById(R.id.speed_value);
        gear = findViewById(R.id.gear_value);
        battery = findViewById(R.id.battery_value);
        range = findViewById(R.id.range_value);
        power = findViewById(R.id.power_value);
        propulsion = findViewById(R.id.propulsion_value);
        vehicle = findViewById(R.id.vehicle_label);
        connection = findViewById(R.id.connection_state);
        sectionTitle = findViewById(R.id.section_title);
        sectionBody = findViewById(R.id.section_body);
    }

    private void bindNavigation() {
        bindButton(R.id.nav_energy, "ENERGY");
        bindButton(R.id.nav_climate, "CLIMATE");
        bindButton(R.id.nav_audio, "AUDIO");
        bindButton(R.id.nav_vehicle, "VEHICLE");
        bindButton(R.id.nav_apps, "APPS");
    }

    private void bindButton(int id, String section) {
        Button button = findViewById(id);
        button.setOnClickListener(v -> selectSection(section));
    }

    private void selectSection(String section) {
        sectionTitle.setText(section);
        switch (section) {
            case "CLIMATE":
                sectionBody.setText("Climate foundation ready • physical rotary controls will map here");
                break;
            case "AUDIO":
                sectionBody.setText("Audio foundation ready • source, DSP and balance/fade integration");
                break;
            case "VEHICLE":
                sectionBody.setText("Vehicle foundation ready • Gen-1 and Gen-2 state normalization");
                break;
            case "APPS":
                sectionBody.setText("AAOS application surface • launcher integration follows acceptance");
                break;
            case "ENERGY":
            default:
                sectionBody.setText("Electric drive overview • canonical vehicle-state service next");
                break;
        }
    }

    private void render(VehicleState state) {
        speed.setText(String.format(Locale.US, "%.0f", state.speedMph));
        gear.setText(state.gear);
        battery.setText(String.format(Locale.US, "%d%%", state.batteryPercent));
        range.setText(String.format(Locale.US, "%d mi", state.electricRangeMiles));
        power.setText(String.format(Locale.US, "%.1f kW", state.tractionPowerKw));
        propulsion.setText(state.propulsionMode);
        vehicle.setText(state.vehicleLabel);
        connection.setText(state.connectionState);
    }

    @SuppressWarnings("deprecation")
    private void enterImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
}
