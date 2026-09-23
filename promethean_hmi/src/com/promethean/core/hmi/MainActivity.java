package com.promethean.core.hmi;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.GridLayout;
import android.widget.LinearLayout;

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
    private TextView evRange;
    private TextView fuelRange;
    private TextView totalRange;
    private TextView efficiency;
    private TextView mpg;
    private TextView chargingTile;
    private TextView tripTile;
    private TextView climateTile;
    private TextView audioTile;
    private GridLayout homeGrid;
    private LinearLayout energyContent;
    private PowerFlowView powerFlowView;
    private LinearLayout chargingContent;
    private ChargingView chargingView;
    private LinearLayout climateContent;
    private ClimateView climateView;
    private LinearLayout radioContent;
    private RadioView radioView;
    private LinearLayout vehicleContent;
    private VehicleView vehicleView;
    private LinearLayout phoneContent;
    private PhoneView phoneView;
    private LinearLayout navContent;
    private NavigationView navigationView;
    private View topBar;
    private View sideNav;
    private View centerPanel;
    private View rightInfoPanel;
    private View bottomQuickBar;

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
        evRange = findViewById(R.id.ev_range_value);
        fuelRange = findViewById(R.id.fuel_range_value);
        totalRange = findViewById(R.id.total_range_value);
        efficiency = findViewById(R.id.efficiency_value);
        mpg = findViewById(R.id.mpg_value);
        chargingTile = findViewById(R.id.charging_tile_value);
        tripTile = findViewById(R.id.trip_tile_value);
        climateTile = findViewById(R.id.climate_tile_value);
        audioTile = findViewById(R.id.audio_tile_value);
        homeGrid = findViewById(R.id.home_grid);
        energyContent = findViewById(R.id.energy_content);
        powerFlowView = findViewById(R.id.power_flow_view);
        chargingContent = findViewById(R.id.charging_content);
        chargingView = findViewById(R.id.charging_view);
        climateContent = findViewById(R.id.climate_content);
        climateView = findViewById(R.id.climate_view);
        radioContent = findViewById(R.id.radio_content);
        radioView = findViewById(R.id.radio_view);
        vehicleContent = findViewById(R.id.vehicle_content);
        vehicleView = findViewById(R.id.vehicle_view);
        phoneContent = findViewById(R.id.phone_content);
        phoneView = findViewById(R.id.phone_view);
        navContent = findViewById(R.id.nav_content);
        navigationView = findViewById(R.id.navigation_view);
        topBar = findViewById(R.id.top_bar);
        sideNav = findViewById(R.id.side_nav);
        centerPanel = findViewById(R.id.center_panel);
        rightInfoPanel = findViewById(R.id.right_info_panel);
        bottomQuickBar = findViewById(R.id.bottom_quick_bar);
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
        if ("CHARGING".equals(section)) {
            showCharging();
            return;
        }
        if ("CLIMATE".equals(section)) {
            showClimate();
            return;
        }
        if ("RADIO".equals(section)) {
            showRadio();
            return;
        }
        if ("VEHICLE".equals(section)) {
            showVehicle();
            return;
        }
        if ("PHONE".equals(section)) {
            showPhone();
            return;
        }
        if ("NAV".equals(section)) {
            showNavigation();
            return;
        }
        showCategoryChrome();
        homeGrid.setVisibility(View.GONE);
        energyContent.setVisibility(View.GONE);
        chargingContent.setVisibility(View.GONE);
        climateContent.setVisibility(View.GONE);
        radioContent.setVisibility(View.GONE);
        vehicleContent.setVisibility(View.GONE);
        phoneContent.setVisibility(View.GONE);
        navContent.setVisibility(View.GONE);
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
        topBar.setVisibility(View.GONE);
        sideNav.setVisibility(View.GONE);
        rightInfoPanel.setVisibility(View.GONE);
        bottomQuickBar.setVisibility(View.GONE);
        screenTitle.setVisibility(View.GONE);
        screenSubtitle.setVisibility(View.GONE);
        homeGrid.setVisibility(View.VISIBLE);
        energyContent.setVisibility(View.GONE);
        chargingContent.setVisibility(View.GONE);
        climateContent.setVisibility(View.GONE);
        radioContent.setVisibility(View.GONE);
        vehicleContent.setVisibility(View.GONE);
        phoneContent.setVisibility(View.GONE);
        navContent.setVisibility(View.GONE);

        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) centerPanel.getLayoutParams();
        params.setMarginStart(0);
        centerPanel.setLayoutParams(params);
    }

    private void showEnergy() {
        showCategoryChrome();
        homeGrid.setVisibility(View.GONE);
        energyContent.setVisibility(View.VISIBLE);
        chargingContent.setVisibility(View.GONE);
        climateContent.setVisibility(View.GONE);
        radioContent.setVisibility(View.GONE);
        vehicleContent.setVisibility(View.GONE);
        phoneContent.setVisibility(View.GONE);
        navContent.setVisibility(View.GONE);
        screenTitle.setText("POWER FLOW");
        screenSubtitle.setText("Electric Drive");
    }

    private void showCharging() {
        showCategoryChrome();
        homeGrid.setVisibility(View.GONE);
        energyContent.setVisibility(View.GONE);
        chargingContent.setVisibility(View.VISIBLE);
        climateContent.setVisibility(View.GONE);
        radioContent.setVisibility(View.GONE);
        vehicleContent.setVisibility(View.GONE);
        phoneContent.setVisibility(View.GONE);
        navContent.setVisibility(View.GONE);
        screenTitle.setText("CHARGING");
        screenSubtitle.setText("Charge Status");
    }

    private void showClimate() {
        showCategoryChrome();
        homeGrid.setVisibility(View.GONE);
        energyContent.setVisibility(View.GONE);
        chargingContent.setVisibility(View.GONE);
        climateContent.setVisibility(View.VISIBLE);
        radioContent.setVisibility(View.GONE);
        vehicleContent.setVisibility(View.GONE);
        phoneContent.setVisibility(View.GONE);
        navContent.setVisibility(View.GONE);
        screenTitle.setText("CLIMATE");
        screenSubtitle.setText("Touch + Rotary Controls");
    }

    // Hardware rotary integration hooks. PCG/Core input service can call these
    // without maintaining a second climate state.
    public void onTemperatureRotaryDelta(int detents) {
        climateView.applyTemperatureRotaryDelta(detents);
    }

    public void onFanRotaryDelta(int detents) {
        climateView.applyFanRotaryDelta(detents);
    }

    private void showRadio() {
        showCategoryChrome();
        homeGrid.setVisibility(View.GONE);
        energyContent.setVisibility(View.GONE);
        chargingContent.setVisibility(View.GONE);
        climateContent.setVisibility(View.GONE);
        radioContent.setVisibility(View.VISIBLE);
        vehicleContent.setVisibility(View.GONE);
        phoneContent.setVisibility(View.GONE);
        navContent.setVisibility(View.GONE);
        screenTitle.setText("RADIO");
        screenSubtitle.setText("Touch + Rotary Audio Controls");
    }

    public void onVolumeRotaryDelta(int detents) {
        radioView.applyVolumeRotaryDelta(detents);
    }

    public void onTuneRotaryDelta(int detents) {
        radioView.applyTuneRotaryDelta(detents);
    }

    public void onTuneRotaryPress() {
        radioView.cycleTuneRotaryMode();
    }

    private void showVehicle() {
        showCategoryChrome();
        homeGrid.setVisibility(View.GONE);
        energyContent.setVisibility(View.GONE);
        chargingContent.setVisibility(View.GONE);
        climateContent.setVisibility(View.GONE);
        radioContent.setVisibility(View.GONE);
        vehicleContent.setVisibility(View.VISIBLE);
        phoneContent.setVisibility(View.GONE);
        navContent.setVisibility(View.GONE);
        screenTitle.setText("VEHICLE");
        screenSubtitle.setText("Volt Status + Diagnostics");
    }

    private void showPhone() {
        showCategoryChrome();
        homeGrid.setVisibility(View.GONE);
        energyContent.setVisibility(View.GONE);
        chargingContent.setVisibility(View.GONE);
        climateContent.setVisibility(View.GONE);
        radioContent.setVisibility(View.GONE);
        vehicleContent.setVisibility(View.GONE);
        phoneContent.setVisibility(View.VISIBLE);
        navContent.setVisibility(View.GONE);
        screenTitle.setText("PHONE");
        screenSubtitle.setText("Bluetooth + Calling");
    }

    private void showNavigation() {
        showCategoryChrome();
        homeGrid.setVisibility(View.GONE);
        energyContent.setVisibility(View.GONE);
        chargingContent.setVisibility(View.GONE);
        climateContent.setVisibility(View.GONE);
        radioContent.setVisibility(View.GONE);
        vehicleContent.setVisibility(View.GONE);
        phoneContent.setVisibility(View.GONE);
        navContent.setVisibility(View.VISIBLE);
        screenTitle.setText("NAVIGATION");
        screenSubtitle.setText("Map + Nearby");
    }

    private void showCategoryChrome() {
        topBar.setVisibility(View.VISIBLE);
        sideNav.setVisibility(View.VISIBLE);
        rightInfoPanel.setVisibility(View.GONE);
        bottomQuickBar.setVisibility(View.GONE);
        screenTitle.setVisibility(View.VISIBLE);
        screenSubtitle.setVisibility(View.VISIBLE);

        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) centerPanel.getLayoutParams();
        params.setMarginStart(dp(12));
        centerPanel.setLayoutParams(params);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void render(VehicleState state) {
        vehicle.setText(state.vehicleLabel);
        connection.setText(state.connectionState);
        evRange.setText(String.format(Locale.US, "%d mi", state.electricRangeMiles));
        fuelRange.setText(String.format(Locale.US, "%d mi", state.fuelRangeMiles));
        totalRange.setText(String.format(Locale.US, "%d mi", state.totalRangeMiles));
        efficiency.setText(String.format(Locale.US, "%.1f mi/kWh", state.efficiencyMiPerKwh));
        mpg.setText(String.format(Locale.US, "%.1f mpg", state.fuelEconomyMpg));
        chargingTile.setText("Charge Mode: " + state.chargeMode);
        tripTile.setText(state.tripSummary);
        climateTile.setText(state.climateSummary);
        audioTile.setText(state.audioSummary);
        powerFlowView.setVehicleState(state);
        chargingView.setVehicleState(state);
        vehicleView.setVehicleState(state);
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
