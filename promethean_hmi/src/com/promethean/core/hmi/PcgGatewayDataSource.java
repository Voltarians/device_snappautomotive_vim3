package com.promethean.core.hmi;

import android.os.SystemClock;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Transport-independent IP client for the PCG-1 gateway.
 *
 * The development endpoint is currently PCG-1 Wi-Fi at 192.168.10.242:47001.
 * Moving the two nodes to Ethernet later does not change the message protocol.
 */
final class PcgGatewayDataSource implements VehicleDataSource, AutoCloseable {
    static final String DEFAULT_HOST = "192.168.10.242";
    static final int DEFAULT_PORT = 47001;

    private static final int CONNECT_TIMEOUT_MS = 1500;
    private static final int READ_TIMEOUT_MS = 5000;
    private static final long STALE_AFTER_MS = 5000L;
    private static final long RECONNECT_DELAY_MS = 1000L;

    private final DemoVehicleDataSource fallback = new DemoVehicleDataSource();
    private final Thread worker;

    private volatile boolean running = true;
    private volatile boolean socketConnected;
    private volatile Socket activeSocket;
    private volatile long lastMessageElapsedMs = -1L;

    private volatile float system12VoltageV = Float.NaN;
    private volatile float auxBusVoltageV = Float.NaN;
    private volatile float apmOutputVoltageV = Float.NaN;
    private volatile float apmCurrentA = Float.NaN;
    private volatile float apmPowerW = Float.NaN;
    private volatile String apmState = "UNKNOWN";
    private volatile boolean hasTwelveVoltData;
    private volatile boolean hasSystemTwelveVoltData;

    PcgGatewayDataSource() {
        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                runGatewayLoop();
            }
        }, "pcg1-gateway");
        worker.setDaemon(true);
        worker.start();
    }

    @Override
    public VehicleState read() {
        VehicleState base = fallback.read();

        long now = SystemClock.elapsedRealtime();
        long age = lastMessageElapsedMs < 0L
                ? -1L
                : Math.max(0L, now - lastMessageElapsedMs);
        boolean online = socketConnected && age >= 0L && age <= STALE_AFTER_MS;

        String connectionState;
        if (online && hasSystemTwelveVoltData) {
            connectionState = "PCG-1 ONLINE • 12 V SYSTEM LIVE • OTHER VALUES DEMO";
        } else if (online && hasTwelveVoltData) {
            connectionState = "PCG-1 ONLINE • APM LIVE • OTHER VALUES DEMO";
        } else if (online) {
            connectionState = "PCG-1 ONLINE • WAITING FOR VEHICLE DATA";
        } else if (socketConnected) {
            connectionState = "PCG-1 CONNECTED • DATA STALE";
        } else {
            connectionState = "PCG-1 OFFLINE • RECONNECTING";
        }

        return new VehicleState(
                base.batteryPercent,
                base.electricRangeMiles,
                base.fuelRangeMiles,
                base.totalRangeMiles,
                base.efficiencyMiPerKwh,
                base.fuelEconomyMpg,
                base.propulsionMode,
                base.chargeMode,
                base.climateSummary,
                base.audioSummary,
                base.tripSummary,
                base.vehicleLabel,
                connectionState,
                online,
                system12VoltageV,
                auxBusVoltageV,
                apmOutputVoltageV,
                apmCurrentA,
                apmPowerW,
                apmState,
                age);
    }

    @Override
    public String sourceName() {
        return "pcg1-ip";
    }

    private void runGatewayLoop() {
        while (running) {
            Socket socket = null;
            try {
                socket = new Socket();
                activeSocket = socket;
                socket.connect(
                        new InetSocketAddress(DEFAULT_HOST, DEFAULT_PORT),
                        CONNECT_TIMEOUT_MS);
                socket.setKeepAlive(true);
                socket.setSoTimeout(READ_TIMEOUT_MS);
                socketConnected = true;

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream(),
                                StandardCharsets.UTF_8));

                String line;
                while (running && (line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        parseMessage(line);
                    }
                }
            } catch (Exception ignored) {
                // Status is reflected through read(); reconnect below.
            } finally {
                socketConnected = false;
                activeSocket = null;
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (Exception ignored) {
                    }
                }
            }

            if (running) {
                SystemClock.sleep(RECONNECT_DELAY_MS);
            }
        }
    }

    private void parseMessage(String line) {
        try {
            JSONObject root = new JSONObject(line);
            JSONObject data = root.optJSONObject("data");
            if (data == null) {
                data = root;
            }
            JSONObject apm = data.optJSONObject("apm");

            float newSystem12 = firstNumber(
                    data,
                    "system_12v_voltage_v");
            float newAux = firstNumber(
                    data,
                    "bus12_voltage_v",
                    "aux_voltage_v",
                    "battery_12v_v");
            float newApmVoltage = firstNumber(
                    data,
                    "apm_output_voltage_v",
                    "apm_voltage_v");
            float newApmCurrent = firstNumber(
                    data,
                    "apm_current_a",
                    "apm_output_current_a");
            float newApmPower = firstNumber(
                    data,
                    "apm_power_w",
                    "apm_output_power_w");

            if (apm != null) {
                if (Float.isNaN(newApmVoltage)) {
                    newApmVoltage = firstNumber(
                            apm, "output_voltage_v", "voltage_v");
                }
                if (Float.isNaN(newApmCurrent)) {
                    newApmCurrent = firstNumber(
                            apm, "output_current_a", "current_a");
                }
                if (Float.isNaN(newApmPower)) {
                    newApmPower = firstNumber(
                            apm, "output_power_w", "power_w");
                }
            }

            if (!Float.isNaN(newSystem12)) {
                system12VoltageV = newSystem12;
                hasSystemTwelveVoltData = true;
                hasTwelveVoltData = true;
            }
            if (!Float.isNaN(newAux)) {
                auxBusVoltageV = newAux;
                hasTwelveVoltData = true;
            }
            if (!Float.isNaN(newApmVoltage)) {
                apmOutputVoltageV = newApmVoltage;
                hasTwelveVoltData = true;
            }
            if (!Float.isNaN(newApmCurrent)) {
                apmCurrentA = newApmCurrent;
                hasTwelveVoltData = true;
            }
            if (!Float.isNaN(newApmPower)) {
                apmPowerW = newApmPower;
                hasTwelveVoltData = true;
            }

            String newState = firstString(data, "apm_state", "dc_dc_state");
            if ((newState == null || newState.isEmpty()) && apm != null) {
                newState = firstString(apm, "state", "dc_dc_state");
            }
            if (newState != null && !newState.isEmpty()) {
                apmState = newState;
                hasTwelveVoltData = true;
            }

            lastMessageElapsedMs = SystemClock.elapsedRealtime();
        } catch (Exception ignored) {
            // Ignore malformed lines without replacing the last valid state.
        }
    }

    private static float firstNumber(JSONObject object, String... keys) {
        for (String key : keys) {
            if (object.has(key) && !object.isNull(key)) {
                double value = object.optDouble(key, Double.NaN);
                if (!Double.isNaN(value)) {
                    return (float) value;
                }
            }
        }
        return Float.NaN;
    }

    private static String firstString(JSONObject object, String... keys) {
        for (String key : keys) {
            if (object.has(key) && !object.isNull(key)) {
                String value = object.optString(key, "");
                if (!value.isEmpty()) {
                    return value;
                }
            }
        }
        return null;
    }

    @Override
    public void close() {
        running = false;
        Socket socket = activeSocket;
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception ignored) {
            }
        }
        worker.interrupt();
    }
}
