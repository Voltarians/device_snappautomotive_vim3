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
    final float system12VoltageV;
    final float hvSocPct;
    final float hvRemainingEnergyKwh;
    final float liveElectricRangeMiles;
    final float liveFuelRangeMiles;
    final float liveTotalRangeMiles;
    final float liveEfficiencyMiPerKwh;
    final boolean vehicleOn;
    final String liveShiftPosition;
    final float liveVehicleSpeedMph;
    final int liveMotorRpm;
    final String liveChargeMode;
    final float hvPackVoltageV;
    final float hvCellMinV;
    final float hvCellMaxV;
    final float hvCellDeltaMv;
    final float hvTempMinC;
    final float hvTempMaxC;
    final int physicalVehicleBusesWithTraffic;
    final String physicalVehicleBusHealth;
    final float auxBusVoltageV;
    final float apmOutputVoltageV;
    final float apmCurrentA;
    final float apmPowerW;
    final String apmState;

    final float cabinTemperatureC;
    final float climateBlowerPct;
    final boolean climateAcActive;
    final String climateAcState;
    final String climateGeneralStatusRawHex;
    final float coolantHeaterPowerKw;
    final float acEvaporatorTemperatureC;
    final int acCompressorRpm;
    final float heaterCoreInletTemperatureC;
    final boolean remoteClimateActive;
    final boolean seatHeatActive;

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
            float system12VoltageV,
            float hvSocPct,
            float hvRemainingEnergyKwh,
            float liveElectricRangeMiles,
            float liveFuelRangeMiles,
            float liveTotalRangeMiles,
            float liveEfficiencyMiPerKwh,
            boolean vehicleOn,
            String liveShiftPosition,
            float liveVehicleSpeedMph,
            int liveMotorRpm,
            String liveChargeMode,
            float hvPackVoltageV,
            float hvCellMinV,
            float hvCellMaxV,
            float hvCellDeltaMv,
            float hvTempMinC,
            float hvTempMaxC,
            int physicalVehicleBusesWithTraffic,
            String physicalVehicleBusHealth,
            float auxBusVoltageV,
            float apmOutputVoltageV,
            float apmCurrentA,
            float apmPowerW,
            String apmState,
            float cabinTemperatureC,
            float climateBlowerPct,
            boolean climateAcActive,
            String climateAcState,
            String climateGeneralStatusRawHex,
            float coolantHeaterPowerKw,
            float acEvaporatorTemperatureC,
            int acCompressorRpm,
            float heaterCoreInletTemperatureC,
            boolean remoteClimateActive,
            boolean seatHeatActive,
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
        this.system12VoltageV = system12VoltageV;
        this.hvSocPct = hvSocPct;
        this.hvRemainingEnergyKwh = hvRemainingEnergyKwh;
        this.liveElectricRangeMiles = liveElectricRangeMiles;
        this.liveFuelRangeMiles = liveFuelRangeMiles;
        this.liveTotalRangeMiles = liveTotalRangeMiles;
        this.liveEfficiencyMiPerKwh = liveEfficiencyMiPerKwh;
        this.vehicleOn = vehicleOn;
        this.liveShiftPosition = liveShiftPosition;
        this.liveVehicleSpeedMph = liveVehicleSpeedMph;
        this.liveMotorRpm = liveMotorRpm;
        this.liveChargeMode = liveChargeMode;
        this.hvPackVoltageV = hvPackVoltageV;
        this.hvCellMinV = hvCellMinV;
        this.hvCellMaxV = hvCellMaxV;
        this.hvCellDeltaMv = hvCellDeltaMv;
        this.hvTempMinC = hvTempMinC;
        this.hvTempMaxC = hvTempMaxC;
        this.physicalVehicleBusesWithTraffic = physicalVehicleBusesWithTraffic;
        this.physicalVehicleBusHealth = physicalVehicleBusHealth;
        this.auxBusVoltageV = auxBusVoltageV;
        this.apmOutputVoltageV = apmOutputVoltageV;
        this.apmCurrentA = apmCurrentA;
        this.apmPowerW = apmPowerW;
        this.apmState = apmState;
        this.cabinTemperatureC = cabinTemperatureC;
        this.climateBlowerPct = climateBlowerPct;
        this.climateAcActive = climateAcActive;
        this.climateAcState = climateAcState;
        this.climateGeneralStatusRawHex = climateGeneralStatusRawHex;
        this.coolantHeaterPowerKw = coolantHeaterPowerKw;
        this.acEvaporatorTemperatureC = acEvaporatorTemperatureC;
        this.acCompressorRpm = acCompressorRpm;
        this.heaterCoreInletTemperatureC = heaterCoreInletTemperatureC;
        this.remoteClimateActive = remoteClimateActive;
        this.seatHeatActive = seatHeatActive;
        this.pcgDataAgeMs = pcgDataAgeMs;
    }
}
