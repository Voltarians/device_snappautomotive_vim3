# Promethean Core ↔ PCG-1 Gateway

Promethean Core consumes PCG-1 vehicle state over newline-delimited JSON on TCP port 47001.

## Development endpoint

- PCG-1: `192.168.10.242:47001`
- Promethean Core VIM3 Wi-Fi: `192.168.10.239`
- VIM3 development Ethernet/ADB remains `192.168.137.3:5555`

The application protocol is ordinary TCP/IP and is intentionally independent of the physical link. The same protocol is intended to remain in place when PCG-1 and Promethean Core move from Wi-Fi to Ethernet.

## Message schema

Each line is one JSON object.

Example with validated values:

```json
{
  "schema": "promethean.pcg1.gateway.v1",
  "source": "PCG-1",
  "type": "vehicle_state",
  "status": "online",
  "sequence": 42,
  "timestamp_utc": "2026-10-03T15:00:00Z",
  "data_valid": true,
  "data": {
    "bus12_voltage_v": 12.64,
    "apm_output_voltage_v": 14.42,
    "apm_current_a": 18.7,
    "apm_power_w": 270.0,
    "apm_state": "ACTIVE"
  }
}
```

Unknown or unvalidated vehicle values are omitted. They are never synthesized by the gateway.

## HMI behavior

The HMI reconnects automatically to PCG-1. The Vehicle screen shows a live 12 V summary and opens a dedicated **12 V POWER** screen containing:

- 12 V bus voltage
- APM output voltage
- APM output current
- APM output power
- DC/DC / APM state
- PCG-1 link state and data age

If PCG-1 is connected but no evidence-backed APM decoder is producing values, the HMI explicitly shows that it is waiting for APM data rather than displaying simulated values.

## Data-source boundary

The existing demo source remains only as an explicit fallback for HMI fields not yet supplied by PCG-1. The connection banner distinguishes live 12 V data from remaining demo values.
