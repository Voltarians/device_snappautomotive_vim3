package com.promethean.core.hmi;

/**
 * Safe placeholder used until the canonical Promethean vehicle-data service is connected.
 * All values are explicitly presented as demo data by the HMI.
 */
final class DemoVehicleDataSource implements VehicleDataSource {
    @Override
    public VehicleState read() {
        return new VehicleState(
                78,
                42,
                310,
                352,
                4.2f,
                28.5f,
                "ELECTRIC DRIVE",
                "IMMEDIATELY",
                "AUTO 72°",
                "XM1 Ch 2 • SiriusXM Preview",
                "6.1 mi • 8.0 mi • 0.30 gal",
                "VOLT",
                "DEMO DATA • VEHICLE BUS NOT CONNECTED");
    }

    @Override
    public String sourceName() {
        return "demo";
    }
}
