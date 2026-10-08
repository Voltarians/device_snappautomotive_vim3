package com.promethean.core.hmi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.Locale;

public final class PowerFlowView extends View {
    private static final int COLOR_BG = 0xFF000000;
    private static final int COLOR_PANEL = 0xFF10181D;
    private static final int COLOR_STROKE = 0xFF27414A;
    private static final int COLOR_TEXT = 0xFFF2F7F8;
    private static final int COLOR_MUTED = 0xFF93A7AF;
    private static final int COLOR_CYAN = 0xFF4ED9F5;
    private static final int COLOR_GREEN = 0xFF69E7A7;
    private static final int COLOR_AMBER = 0xFFF1B84B;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final NeonVoltBody voltBody = new NeonVoltBody();

    private VehicleState state = new DemoVehicleDataSource().read();
    private float phase = 0f;

    public PowerFlowView(Context context) {
        super(context);
        init();
    }

    public PowerFlowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PowerFlowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setTypeface(android.graphics.Typeface.create(
                android.graphics.Typeface.SANS_SERIF,
                android.graphics.Typeface.NORMAL));
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void setVehicleState(VehicleState state) {
        if (state != null) {
            this.state = state;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(COLOR_BG);

        final float w = getWidth();
        final float h = getHeight();

        if (w <= 0 || h <= 0) {
            return;
        }

        phase += 0.018f;
        if (phase > 1f) {
            phase -= 1f;
        }

        // Gen-1 Volt-inspired power-flow presentation. Keep all live-data
        // semantics separate from the artwork so the proven gateway path is
        // untouched.
        drawGen1PowerFlow(canvas, w, h);

        drawMetrics(canvas, w, h);

        postInvalidateDelayed(45);
    }

    private void drawGen1PowerFlow(Canvas canvas, float w, float h) {
        // Fit the approved SVG into the artwork area, keeping its true aspect
        // ratio and leaving the lower live metrics and mode label unobstructed.
        float diagramBottom = h - dp(178);
        float diagramTop = dp(14);
        float diagramH = diagramBottom - diagramTop;
        float scale = Math.min((w - dp(36)) / 641f, (diagramH - dp(42)) / 250f);
        if (scale <= 0f) {
            return;
        }
        float graphicLeft = (w - 641f * scale) / 2f;
        float graphicTop = diagramTop + (diagramH - dp(42) - 250f * scale) / 2f;

        // The source body has no static wheels: retain the HMI's foreground
        // wheels, aligned to the source wheel arches, and its state-driven art.
        float frontWheelX = graphicLeft + 136f * scale;
        float rearWheelX = graphicLeft + 529f * scale;
        float wheelY = graphicTop + 185f * scale;
        float rearWheelY = graphicTop + 184f * scale;
        float wheelRadius = 46f * scale;
        float graphicScale = scale;
        float battCx = graphicLeft + 385f * scale;
        float battW = 160f * scale;
        float battH = 45f * scale;
        float engineW = 60f * scale;
        float engineH = 65f * scale;
        float componentBottom = wheelY + 14.5f * scale;
        float battCy = componentBottom - battH / 2f;
        float engineCy = componentBottom - engineH / 2f;

        voltBody.draw(canvas, graphicLeft, graphicTop, scale);

        drawGen1Battery(canvas, battCx, battCy, battW, battH);

        // Engine remains centered behind the front wheel.
        float engineCx = frontWheelX;
        drawGen1PowerUnit(canvas, engineCx, engineCy, engineW, engineH);

        // Wheels are the foreground layer.
        drawGen1Wheel(canvas, frontWheelX, wheelY, wheelRadius);
        drawGen1Wheel(canvas, rearWheelX, rearWheelY, wheelRadius);

        // Neutral flow paths until signed propulsion/regen power is validated.
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(3) * graphicScale);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(0xFF1A5470);
        canvas.drawLine(engineCx + engineW * 0.50f, battCy,
                battCx - battW * 0.52f, battCy, paint);
        canvas.drawLine(battCx + battW * 0.45f, battCy,
                rearWheelX - wheelRadius * 0.90f, wheelY - wheelRadius * 0.20f, paint);

        String mode = gen1PowerFlowLabel();
        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(android.graphics.Typeface.create(
                "sans-serif-condensed",
                android.graphics.Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(dp(25));
        paint.setColor(COLOR_TEXT);
        canvas.drawText(mode, w * 0.52f, diagramBottom - dp(14), paint);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawGen1Battery(Canvas canvas,
                                 float cx, float cy,
                                 float width, float height) {
        float capW = width * 0.16f;
        RectF body = new RectF(
                cx - width / 2f,
                cy - height / 2f,
                cx + width / 2f - capW,
                cy + height / 2f);

        // Battery shadow/glow.
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x332CFF51);
        RectF halo = new RectF(
                body.left - dp(8), body.top - dp(8),
                body.right + capW + dp(8), body.bottom + dp(8));
        canvas.drawRoundRect(halo, dp(14), dp(14), paint);

        paint.setColor(0xFFD6E0E4);
        canvas.drawRoundRect(body, dp(10), dp(10), paint);

        RectF cavity = new RectF(
                body.left + dp(3), body.top + dp(3),
                body.right - dp(3), body.bottom - dp(3));
        paint.setColor(0xFF29383F);
        canvas.drawRoundRect(cavity, dp(5), dp(5), paint);

        int cells = 12;
        float gap = dp(2);
        float cellW = (cavity.width() - gap * (cells + 1)) / cells;

        // The Gen-1 Volt keeps roughly the bottom 20% of gross SOC out of the
        // driver's usable display. Map only the 20-100% window to the graphic.
        // Five visible charge steps preserve that reserve behavior: at the
        // current ~36% gross SOC only one bar is illuminated.
        int activeCells = 0;
        if (!Float.isNaN(state.hvSocPct)) {
            float usablePct = Math.max(0f, Math.min(80f, state.hvSocPct - 20f));
            activeCells = Math.min(5, (int) Math.floor(usablePct / 16f));
            if (usablePct > 0f && activeCells == 0) {
                activeCells = 1;
            }
        }

        for (int i = 0; i < cells; i++) {
            float l = cavity.left + gap + i * (cellW + gap);
            RectF cell = new RectF(
                    l, cavity.top + gap,
                    l + cellW, cavity.bottom - gap);

            // Fill from the white end-cap toward the front, correcting the
            // previous backwards orientation.
            boolean active = i >= cells - activeCells;
            paint.setColor(active ? 0xFF9CDD36 : 0xFF46545A);
            canvas.drawRoundRect(cell, dp(1.5f), dp(1.5f), paint);
        }

        // Silver end cap used by the stock Gen-1 battery artwork.
        RectF cap = new RectF(
                body.right - dp(2), body.top - dp(6),
                cx + width / 2f, body.bottom + dp(6));
        paint.setColor(0xFFD9E5E9);
        canvas.drawRoundRect(cap, dp(8), dp(8), paint);
        paint.setColor(0xFF94A8B1);
        canvas.drawRect(cap.left, cap.top + dp(5),
                cap.left + dp(4), cap.bottom - dp(5), paint);

        paint.setTypeface(android.graphics.Typeface.create(
                android.graphics.Typeface.SANS_SERIF,
                android.graphics.Typeface.BOLD));
        paint.setTextSize(dp(15));
        paint.setColor(0xFF07111B);
        canvas.drawText("+", body.left + dp(12), body.bottom - dp(9), paint);
        canvas.drawText("−", body.right - dp(24), body.bottom - dp(9), paint);
    }

    private void drawGen1PowerUnit(Canvas canvas,
                                   float cx, float cy,
                                   float width, float height) {
        RectF unit = new RectF(
                cx - width / 2f,
                cy - height / 2f,
                cx + width / 2f,
                cy + height / 2f);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x4400FF58);
        RectF halo = new RectF(
                unit.left - dp(7), unit.top - dp(7),
                unit.right + dp(7), unit.bottom + dp(7));
        canvas.drawRoundRect(halo, dp(16), dp(16), paint);

        paint.setColor(0xFF11191D);
        canvas.drawRoundRect(unit, dp(9), dp(9), paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(3));
        paint.setColor(state.vehicleOn ? 0xFF8FDB39 : 0xFF62747C);
        canvas.drawRoundRect(unit, dp(9), dp(9), paint);

        // Simple engine/power-electronics ribs to echo the original artwork.
        paint.setStrokeWidth(dp(2));
        for (int i = 0; i < 4; i++) {
            float y = unit.top + height * (0.30f + i * 0.12f);
            canvas.drawLine(unit.left + width * 0.18f, y,
                    unit.right - width * 0.16f, y, paint);
        }
        canvas.drawCircle(unit.left + width * 0.23f,
                unit.bottom - height * 0.20f,
                Math.min(width, height) * 0.11f, paint);
    }

    private void drawGen1Wheel(Canvas canvas, float cx, float cy, float radius) {
        // Side-view tire: slightly taller than wide to read as an automotive
        // wheel instead of a top-down icon.
        RectF tire = new RectF(
                cx - radius * 0.78f,
                cy - radius,
                cx + radius * 0.78f,
                cy + radius);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF05090B);
        canvas.drawOval(tire, paint);

        RectF rim = new RectF(
                cx - radius * 0.54f,
                cy - radius * 0.70f,
                cx + radius * 0.54f,
                cy + radius * 0.70f);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(4));
        paint.setColor(0xFFCAD6DB);
        canvas.drawOval(rim, paint);

        paint.setStrokeWidth(dp(3));
        for (int i = 0; i < 5; i++) {
            double a = -Math.PI / 2.0 + i * (Math.PI * 2.0 / 5.0);
            float x = cx + (float) Math.cos(a) * radius * 0.48f;
            float y = cy + (float) Math.sin(a) * radius * 0.62f;
            canvas.drawLine(cx, cy, x, y, paint);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF8197A0);
        canvas.drawCircle(cx, cy, radius * 0.12f, paint);
    }

    private String gen1PowerFlowLabel() {
        // Match Gen-1 terminology while refusing to invent an engine-active
        // state from vehicle_on alone.
        if (!state.vehicleOn) {
            return "Battery Power";
        }
        if (!Float.isNaN(state.liveVehicleSpeedMph)
                && Math.abs(state.liveVehicleSpeedMph) < 0.5f) {
            return "Battery Power";
        }
        return "Battery Power";
    }

    private RectF centeredRect(float cx, float cy, float width, float height) {
        return new RectF(cx - width / 2f, cy - height / 2f,
                cx + width / 2f, cy + height / 2f);
    }

    private void drawBasePath(Canvas canvas, float x1, float y1, float x2, float y2) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(5));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(COLOR_STROKE);
        canvas.drawLine(x1, y1, x2, y2, paint);
    }

    private void drawAnimatedFlow(Canvas canvas,
                                  float x1, float y1, float x2, float y2,
                                  int color) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(4));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(color);
        canvas.drawLine(x1, y1, x2, y2, paint);

        float dx = x2 - x1;
        float dy = y2 - y1;
        for (int i = 0; i < 5; i++) {
            float t = (phase + i * 0.20f) % 1f;
            float x = x1 + dx * t;
            float y = y1 + dy * t;
            drawFlowMarker(canvas, x, y, dx, dy, color);
        }
    }

    private void drawFlowMarker(Canvas canvas,
                                float x, float y,
                                float dx, float dy,
                                int color) {
        double angle = Math.atan2(dy, dx);
        float size = dp(8);

        path.reset();
        path.moveTo(
                x + (float) Math.cos(angle) * size,
                y + (float) Math.sin(angle) * size);
        path.lineTo(
                x + (float) Math.cos(angle + 2.45) * size,
                y + (float) Math.sin(angle + 2.45) * size);
        path.lineTo(
                x + (float) Math.cos(angle - 2.45) * size,
                y + (float) Math.sin(angle - 2.45) * size);
        path.close();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawPath(path, paint);
    }

    private void drawNodeBackground(Canvas canvas, RectF rect, int strokeColor) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(rect, dp(18), dp(18), paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(strokeColor);
        canvas.drawRoundRect(rect, dp(18), dp(18), paint);
    }

    private void drawBatteryNode(Canvas canvas, RectF rect) {
        drawNodeBackground(canvas, rect, COLOR_CYAN);

        float pad = dp(20);
        RectF body = new RectF(
                rect.left + pad,
                rect.top + pad + dp(12),
                rect.right - pad - dp(10),
                rect.bottom - pad - dp(12));

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(3));
        paint.setColor(COLOR_TEXT);
        canvas.drawRoundRect(body, dp(5), dp(5), paint);
        canvas.drawRect(body.right, body.centerY() - dp(12),
                body.right + dp(10), body.centerY() + dp(12), paint);

        if (!Float.isNaN(state.hvSocPct)) {
            float fill = Math.max(0f, Math.min(1f, state.hvSocPct / 100f));
            RectF charge = new RectF(
                    body.left + dp(5),
                    body.bottom - dp(5) - (body.height() - dp(10)) * fill,
                    body.right - dp(5),
                    body.bottom - dp(5));

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(COLOR_CYAN);
            canvas.drawRoundRect(charge, dp(3), dp(3), paint);
        }

        drawNodeLabel(canvas, rect, "BATTERY",
                Float.isNaN(state.hvSocPct)
                        ? "SOC --"
                        : String.format(Locale.US, "%.1f%%", state.hvSocPct),
                Float.isNaN(state.hvSocPct) ? COLOR_MUTED : COLOR_CYAN);
    }

    private void drawEngineNode(Canvas canvas, RectF rect) {
        int accent = state.vehicleOn ? COLOR_TEXT : COLOR_MUTED;
        drawNodeBackground(canvas, rect, accent);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(5));
        paint.setColor(accent);

        float cx = rect.centerX();
        float cy = rect.centerY() - dp(12);
        float r = Math.min(rect.width(), rect.height()) * 0.18f;
        canvas.drawCircle(cx, cy, r, paint);
        canvas.drawLine(cx - r * 1.7f, cy, cx - r, cy, paint);
        canvas.drawLine(cx + r, cy, cx + r * 1.7f, cy, paint);
        canvas.drawLine(cx, cy - r * 1.7f, cx, cy - r, paint);

        drawNodeLabel(canvas, rect, "ENGINE",
                state.vehicleOn ? "AUTO" : "OFF",
                accent);
    }

    private void drawMotorNode(Canvas canvas, RectF rect) {
        int accent = state.vehicleOn ? COLOR_CYAN : COLOR_MUTED;
        drawNodeBackground(canvas, rect, accent);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(5));
        paint.setColor(accent);
        canvas.drawCircle(rect.centerX(), rect.centerY() - dp(12),
                Math.min(rect.width(), rect.height()) * 0.20f, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.create(
                android.graphics.Typeface.SANS_SERIF,
                android.graphics.Typeface.BOLD));
        paint.setTextSize(dp(27));
        canvas.drawText("M", rect.centerX(), rect.centerY() - dp(3), paint);
        paint.setTextAlign(Paint.Align.LEFT);

        drawNodeLabel(canvas, rect, "DRIVE MOTOR",
                motorStateLabel(),
                accent);
    }

    private void drawWheelsNode(Canvas canvas, RectF rect) {
        drawNodeBackground(canvas, rect, COLOR_TEXT);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(5));
        paint.setColor(COLOR_TEXT);

        float cy = rect.centerY() - dp(8);
        float r = Math.min(rect.width(), rect.height()) * 0.13f;
        canvas.drawCircle(rect.centerX() - r * 1.7f, cy, r, paint);
        canvas.drawCircle(rect.centerX() + r * 1.7f, cy, r, paint);
        canvas.drawLine(rect.centerX() - r * 0.7f, cy,
                rect.centerX() + r * 0.7f, cy, paint);

        drawNodeLabel(canvas, rect, "WHEELS",
                Float.isNaN(state.liveVehicleSpeedMph)
                        ? "--"
                        : String.format(Locale.US, "%.0f mph", state.liveVehicleSpeedMph),
                COLOR_TEXT);
    }

    private void drawNodeLabel(Canvas canvas,
                               RectF rect,
                               String title,
                               String value,
                               int accent) {
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.create(
                android.graphics.Typeface.SANS_SERIF,
                android.graphics.Typeface.BOLD));

        paint.setColor(COLOR_MUTED);
        paint.setTextSize(dp(9));
        canvas.drawText(title, rect.centerX(), rect.bottom - dp(23), paint);

        paint.setColor(accent);
        paint.setTextSize(dp(12));
        canvas.drawText(value, rect.centerX(), rect.bottom - dp(7), paint);

        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawMetrics(Canvas canvas, float w, float h) {
        float top = h - dp(80);
        float liveTop = top - dp(52);
        float diagnosticTop = liveTop - dp(52);
        float margin = dp(18);
        float gap = dp(10);
        float columnWidth = (w - margin * 2f - gap * 4f) / 5f;

        drawMetric(canvas, margin, diagnosticTop, columnWidth,
                "HV ENERGY",
                formatEnergy(state.hvRemainingEnergyKwh),
                Float.isNaN(state.hvRemainingEnergyKwh) ? COLOR_MUTED : COLOR_GREEN);

        drawMetric(canvas, margin + (columnWidth + gap), diagnosticTop, columnWidth,
                "HV SOC",
                Float.isNaN(state.hvSocPct)
                        ? "-- %"
                        : String.format(Locale.US, "%.1f%%", state.hvSocPct),
                Float.isNaN(state.hvSocPct) ? COLOR_MUTED : COLOR_CYAN);

        drawMetric(canvas, margin + (columnWidth + gap) * 2f, diagnosticTop, columnWidth,
                "VEHICLE",
                state.vehicleOn ? "READY" : "OFF",
                state.vehicleOn ? COLOR_GREEN : COLOR_MUTED);

        drawMetric(canvas, margin + (columnWidth + gap) * 3f, diagnosticTop, columnWidth,
                "PRNDL",
                isKnown(state.liveShiftPosition) ? state.liveShiftPosition : "--",
                isKnown(state.liveShiftPosition) ? COLOR_TEXT : COLOR_MUTED);

        drawMetric(canvas, margin + (columnWidth + gap) * 4f, diagnosticTop, columnWidth,
                "BUS HEALTH",
                shortBusHealth(),
                busHealthGood() ? COLOR_GREEN : COLOR_MUTED);

        drawMetric(canvas, margin, liveTop, columnWidth,
                "HV PACK",
                formatVoltage(state.hvPackVoltageV),
                Float.isNaN(state.hvPackVoltageV) ? COLOR_MUTED : COLOR_CYAN);

        drawMetric(canvas, margin + (columnWidth + gap), liveTop, columnWidth,
                "CELL MIN",
                formatCellVoltage(state.hvCellMinV),
                Float.isNaN(state.hvCellMinV) ? COLOR_MUTED : COLOR_AMBER);

        drawMetric(canvas, margin + (columnWidth + gap) * 2f, liveTop, columnWidth,
                "CELL MAX",
                formatCellVoltage(state.hvCellMaxV),
                Float.isNaN(state.hvCellMaxV) ? COLOR_MUTED : COLOR_GREEN);

        drawMetric(canvas, margin + (columnWidth + gap) * 3f, liveTop, columnWidth,
                "CELL DELTA",
                formatDelta(state.hvCellDeltaMv),
                Float.isNaN(state.hvCellDeltaMv) ? COLOR_MUTED : COLOR_AMBER);

        drawMetric(canvas, margin + (columnWidth + gap) * 4f, liveTop, columnWidth,
                "BATTERY TEMP",
                formatTemperatureRange(state.hvTempMinC, state.hvTempMaxC),
                Float.isNaN(state.hvTempMinC) ? COLOR_MUTED : COLOR_CYAN);

        drawMetric(canvas, margin, top, columnWidth,
                "ELECTRIC RANGE",
                formatMiles(state.liveElectricRangeMiles),
                Float.isNaN(state.liveElectricRangeMiles) ? COLOR_MUTED : COLOR_GREEN);

        drawMetric(canvas, margin + (columnWidth + gap), top, columnWidth,
                "EFFICIENCY",
                formatEfficiency(state.liveEfficiencyMiPerKwh),
                Float.isNaN(state.liveEfficiencyMiPerKwh) ? COLOR_MUTED : COLOR_CYAN);

        drawMetric(canvas, margin + (columnWidth + gap) * 2f, top, columnWidth,
                "TOTAL RANGE",
                formatMiles(state.liveTotalRangeMiles),
                Float.isNaN(state.liveTotalRangeMiles) ? COLOR_MUTED : COLOR_TEXT);

        drawMetric(canvas, margin + (columnWidth + gap) * 3f, top, columnWidth,
                "CHARGE MODE",
                shortChargeMode(state.liveChargeMode),
                isKnown(state.liveChargeMode) ? COLOR_TEXT : COLOR_MUTED);

        drawMetric(canvas, margin + (columnWidth + gap) * 4f, top, columnWidth,
                "12 V SYSTEM",
                formatVoltage(state.system12VoltageV),
                Float.isNaN(state.system12VoltageV) ? COLOR_MUTED : COLOR_GREEN);
    }

    private void drawMetric(Canvas canvas,
                            float x, float y, float width,
                            String label, String value,
                            int valueColor) {
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(android.graphics.Typeface.create(
                android.graphics.Typeface.SANS_SERIF,
                android.graphics.Typeface.BOLD));

        paint.setColor(COLOR_MUTED);
        paint.setTextSize(dp(9));
        canvas.drawText(label, x, y, paint);

        paint.setColor(valueColor);
        float textSize = dp(17);
        paint.setTextSize(textSize);

        while (paint.measureText(value) > width && textSize > dp(11)) {
            textSize -= dp(1);
            paint.setTextSize(textSize);
        }

        canvas.drawText(value, x, y + dp(25), paint);
    }

    private String formatVoltage(float value) {
        return Float.isNaN(value)
                ? "-- V"
                : String.format(Locale.US, "%.2f V", value);
    }

    private String formatCellVoltage(float value) {
        return Float.isNaN(value)
                ? "-- V"
                : String.format(Locale.US, "%.3f V", value);
    }

    private String formatDelta(float value) {
        return Float.isNaN(value)
                ? "-- mV"
                : String.format(Locale.US, "%.0f mV", value);
    }

    private String formatTemperatureRange(float min, float max) {
        if (Float.isNaN(min) || Float.isNaN(max)) {
            return "-- °C";
        }
        return String.format(Locale.US, "%.0f–%.0f °C", min, max);
    }

    private String formatMiles(float value) {
        return Float.isNaN(value)
                ? "-- mi"
                : String.format(Locale.US, "%.0f mi", value);
    }

    private String formatEfficiency(float value) {
        return Float.isNaN(value)
                ? "-- mi/kWh"
                : String.format(Locale.US, "%.1f mi/kWh", value);
    }

    private String formatEnergy(float value) {
        return Float.isNaN(value)
                ? "-- kWh"
                : String.format(Locale.US, "%.2f kWh", value);
    }

    private boolean busHealthGood() {
        return state.physicalVehicleBusesWithTraffic >= 5
                || "all_expected_buses_live".equalsIgnoreCase(
                        state.physicalVehicleBusHealth);
    }

    private String shortBusHealth() {
        if (busHealthGood()) {
            return state.physicalVehicleBusesWithTraffic > 0
                    ? state.physicalVehicleBusesWithTraffic + "/5 LIVE"
                    : "ALL LIVE";
        }
        if (state.physicalVehicleBusesWithTraffic > 0) {
            return state.physicalVehicleBusesWithTraffic + "/5 LIVE";
        }
        return "--";
    }

    private boolean isKnown(String value) {
        return value != null
                && !value.trim().isEmpty()
                && !"UNKNOWN".equalsIgnoreCase(value.trim());
    }

    private String motorStateLabel() {
        if (!state.vehicleOn) {
            return "OFF";
        }
        if (isKnown(state.liveShiftPosition)) {
            return state.liveShiftPosition;
        }
        return state.liveMotorRpm != 0 ? "ACTIVE" : "READY";
    }

    private String shortChargeMode(String chargeMode) {
        if (!isKnown(chargeMode)) {
            return "--";
        }
        String normalized = chargeMode.trim().toUpperCase(Locale.US);
        if ("IMMEDIATELY".equals(normalized)) {
            return "IMMEDIATE";
        }
        if ("DEPARTURE".equals(normalized)) {
            return "DEPARTURE";
        }
        return normalized;
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
