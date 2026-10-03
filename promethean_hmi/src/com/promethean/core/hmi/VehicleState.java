package com.promethean.core.hmi;

final class VehicleState {
    final int batteryPercent;
    final int electricRangeMiles;
    final int fuelRangeMiles;
    final int totalRangeMiles;
    final float efficiencyMiPerKwh;
    final float fuelEconomyMpg;
    final String propulsionMode;
    final String chargeMode;
    final String climateSummary;
    final String audioSummary;
    final String tripSummary;
    final String vehicleLabel;
    final String connectionState;

    final boolean pcgConnected;
    final float auxBusVoltageV;
    final float apmOutputVoltageV;
    final float apmCurrentA;
    final float apmPowerW;
    final String apmState;
    final long pcgDataAgeMs;

    VehicleState(
            int batteryPercent,
            int electricRangeMiles,
            int fuelRangeMiles,
            int totalRangeMiles,
            float efficiencyMiPerKwh,
            float fuelEconomyMpg,
            String propulsionMode,
            String chargeMode,
            String climateSummary,
            String audioSummary,
            String tripSummary,
            String vehicleLabel,
            String connectionState,
            boolean pcgConnected,
            float auxBusVoltageV,
            float apmOutputVoltageV,
            float apmCurrentA,
            float apmPowerW,
            String apmState,
            long pcgDataAgeMs) {
        this.batteryPercent = batteryPercent;
        this.electricRangeMiles = electricRangeMiles;
        this.fuelRangeMiles = fuelRangeMiles;
        this.totalRangeMiles = totalRangeMiles;
        this.efficiencyMiPerKwh = efficiencyMiPerKwh;
        this.fuelEconomyMpg = fuelEconomyMpg;
        this.propulsionMode = propulsionMode;
        this.chargeMode = chargeMode;
        this.climateSummary = climateSummary;
        this.audioSummary = audioSummary;
        this.tripSummary = tripSummary;
        this.vehicleLabel = vehicleLabel;
        this.connectionState = connectionState;
        this.pcgConnected = pcgConnected;
        this.auxBusVoltageV = auxBusVoltageV;
        this.apmOutputVoltageV = apmOutputVoltageV;
        this.apmCurrentA = apmCurrentA;
        this.apmPowerW = apmPowerW;
        this.apmState = apmState;
        this.pcgDataAgeMs = pcgDataAgeMs;
    }
}
