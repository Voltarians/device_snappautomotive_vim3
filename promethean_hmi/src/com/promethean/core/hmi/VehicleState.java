package com.promethean.core.hmi;

final class VehicleState {
    final float speedMph;
    final String gear;
    final int batteryPercent;
    final int electricRangeMiles;
    final float tractionPowerKw;
    final String propulsionMode;
    final String vehicleLabel;
    final String connectionState;

    VehicleState(
            float speedMph,
            String gear,
            int batteryPercent,
            int electricRangeMiles,
            float tractionPowerKw,
            String propulsionMode,
            String vehicleLabel,
            String connectionState) {
        this.speedMph = speedMph;
        this.gear = gear;
        this.batteryPercent = batteryPercent;
        this.electricRangeMiles = electricRangeMiles;
        this.tractionPowerKw = tractionPowerKw;
        this.propulsionMode = propulsionMode;
        this.vehicleLabel = vehicleLabel;
        this.connectionState = connectionState;
    }
}
