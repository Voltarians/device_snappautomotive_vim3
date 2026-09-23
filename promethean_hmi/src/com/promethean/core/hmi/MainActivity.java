package com.promethean.core.hmi;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;\nimport android.widget.GridLayout;\nimport android.widget.LinearLayout;

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
    private TextView vehicle;
    private TextView connection;
    private TextView screenTitle;
    private TextView screenSubtitle;
    private TextView battery;
    private TextView evRange;
    private TextView fuelRange;
    private TextView totalRange;
    private TextView efficiency;
    private TextView mpg;
    private TextView propulsion;
    private TextView chargingTile;
    private TextView tripTile;
    private TextView climateTile;
    private TextView audioTile;\n    private GridLayout homeGrid;\n    private LinearLayout energyContent;

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
        showHome();
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
        vehicle = findViewById(R.id.vehicle_label);
        connection = findViewById(R.id.connection_state);
        screenTitle = findViewById(R.id.screen_title);
        screenSubtitle = findViewById(R.id.screen_subtitle);
        battery = findViewById(R.id.battery_value);
        evRange = findViewById(R.id.ev_range_value);
        fuelRange = findViewById(R.id.fuel_range_value);
        totalRange = findViewById(R.id.total_range_value);
        efficiency = findViewById(R.id.efficiency_value);
        mpg = findViewById(R.id.mpg_value);
        propulsion = findViewById(R.id.propulsion_value);
        chargingTile = findViewById(R.id.charging_tile_value);
        tripTile = findViewById(R.id.trip_tile_value);
        climateTile = findViewById(R.id.climate_tile_value);
        audioTile = findViewById(R.id.audio_tile_value);\n        homeGrid = findViewById(R.id.home_grid);\n        energyContent = findViewById(R.id.energy_content);
    }

    private void bindNavigation() {
        bindButton(R.id.nav_home, "HOME");
        bindButton(R.id.nav_energy, "ENERGY");
        bindButton(R.id.nav_charging, "CHARGING");
        bindButton(R.id.nav_radio, "RADIO");
        bindButton(R.id.nav_phone, "PHONE");
        bindButton(R.id.nav_nav, "NAV");
        bindButton(R.id.nav_vehicle, "VEHICLE");
        bindButton(R.id.nav_climate, "CLIMATE");
        bindButton(R.id.nav_apps, "APPS");
        bindButton(R.id.nav_settings, "SETTINGS");
        bindButton(R.id.home_tile_energy, "ENERGY");
        bindButton(R.id.home_tile_climate, "CLIMATE");
        bindButton(R.id.home_tile_radio, "RADIO");
        bindButton(R.id.home_tile_apps, "APPS");
        bindButton(R.id.home_tile_phone, "PHONE");
        bindButton(R.id.home_tile_nav, "NAV");
        bindButton(R.id.home_tile_vehicle, "VEHICLE");
        bindButton(R.id.home_tile_charging, "CHARGING");
        bindButton(R.id.home_tile_settings, "SETTINGS");
    }

    private void bindButton(int id, String section) {
        Button button = findViewById(id);
        button.setOnClickListener(v -> selectSection(section));
    }

    private void selectSection(String section) {
        if ("HOME".equals(section)) {
            showHome();
            return;
        }
        if ("ENERGY".equals(section)) {
            showEnergy();
            return;
        }
        homeGrid.setVisibility(View.GONE);
        energyContent.setVisibility(View.GONE);
        screenTitle.setText(section);
        switch (section) {
            case "CHARGING":
                screenSubtitle.setText("Charging controls and schedule foundation");
                break;
            case "RADIO":
                screenSubtitle.setText("Radio, media source, DSP, balance and fade foundation");
                break;
            case "PHONE":
                screenSubtitle.setText("Phone and communications foundation");
                break;
            case "NAV":
                screenSubtitle.setText("Navigation foundation");
                break;
            case "VEHICLE":
                screenSubtitle.setText("Vehicle settings and status foundation");
                break;
            case "CLIMATE":
                screenSubtitle.setText("Climate controls and physical rotary integration");
                break;
            case "APPS":
                screenSubtitle.setText("Applications and projection foundation");
                break;
            case "SETTINGS":
                screenSubtitle.setText("Promethean Core system settings");
                break;
            default:
                screenSubtitle.setText("");
                break;
        }
    }

    private void showHome() {
        homeGrid.setVisibility(View.VISIBLE);
        energyContent.setVisibility(View.GONE);
        screenTitle.setText("HOME");
        screenSubtitle.setText("Promethean Core");
    }

    private void showEnergy() {
        homeGrid.setVisibility(View.GONE);
        energyContent.setVisibility(View.VISIBLE);
        screenTitle.setText("POWER FLOW");
        screenSubtitle.setText("Electric Drive");
    }

    private void render(VehicleState state) {
        vehicle.setText(state.vehicleLabel);
        connection.setText(state.connectionState);
        battery.setText(String.format(Locale.US, "%d%%", state.batteryPercent));
        evRange.setText(String.format(Locale.US, "%d mi", state.electricRangeMiles));
        fuelRange.setText(String.format(Locale.US, "%d mi", state.fuelRangeMiles));
        totalRange.setText(String.format(Locale.US, "%d mi", state.totalRangeMiles));
        efficiency.setText(String.format(Locale.US, "%.1f mi/kWh", state.efficiencyMiPerKwh));
        mpg.setText(String.format(Locale.US, "%.1f mpg", state.fuelEconomyMpg));
        propulsion.setText(state.propulsionMode);
        chargingTile.setText("Charge Mode: " + state.chargeMode);
        tripTile.setText(state.tripSummary);
        climateTile.setText(state.climateSummary);
        audioTile.setText(state.audioSummary);
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
