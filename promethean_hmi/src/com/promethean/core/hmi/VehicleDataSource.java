package com.promethean.core.hmi;

interface VehicleDataSource {
    VehicleState read();
    String sourceName();
}
