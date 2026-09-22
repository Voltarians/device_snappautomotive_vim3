package com.promethean.core.hmi;

/**
 * Safe placeholder used until the canonical Promethean vehicle-data service is connected.
 *
 * This intentionally reports zero road speed and clearly identifies itself as demo data.
 */
final class DemoVehicleDataSource implements VehicleDataSource {
    @Override
    public VehicleState read() {
        return new VehicleState(
                0.0f,
                "P",
                72,
                37,
                0.0f,
                "ELECTRIC",
                "VOLT",
                "DEMO DATA • VEHICLE BUS NOT CONNECTED");
    }

    @Override
    public String sourceName() {
        return "demo";
    }
}
