package com.promethean.core.hmi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.Locale;

public final class TwelveVoltView extends View {
    private static final int COLOR_BG = 0xFF070D10;
    private static final int COLOR_PANEL = 0xFF10181D;
    private static final int COLOR_STROKE = 0xFF27414A;
    private static final int COLOR_TEXT = 0xFFF2F7F8;
    private static final int COLOR_MUTED = 0xFF93A7AF;
    private static final int COLOR_CYAN = 0xFF4ED9F5;
    private static final int COLOR_GREEN = 0xFF69E7A7;
    private static final int COLOR_AMBER = 0xFFF1B84B;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private VehicleState state = new DemoVehicleDataSource().read();

    public TwelveVoltView(Context context) {
        super(context);
        init();
    }

    public TwelveVoltView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TwelveVoltView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setTypeface(android.graphics.Typeface.create(
                android.graphics.Typeface.SANS_SERIF,
                android.graphics.Typeface.NORMAL));
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

        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }

        float margin = dp(18);
        float gap = dp(12);
        float cardWidth = (w - margin * 2f - gap * 2f) / 3f;
        float cardHeight = (h - margin * 2f - gap) / 2f;

        RectF bus = card(margin, margin, cardWidth, cardHeight);
        RectF voltage = card(bus.right + gap, margin, cardWidth, cardHeight);
        RectF current = card(voltage.right + gap, margin, cardWidth, cardHeight);
        RectF power = card(margin, bus.bottom + gap, cardWidth, cardHeight);
        RectF dcDc = card(power.right + gap, bus.bottom + gap, cardWidth, cardHeight);
        RectF gateway = card(dcDc.right + gap, bus.bottom + gap, cardWidth, cardHeight);

        drawCard(canvas, bus,
                "12 V BUS",
                formatVoltage(state.auxBusVoltageV),
                "Measured low-voltage bus",
                state.pcgConnected ? COLOR_GREEN : COLOR_MUTED);

        drawCard(canvas, voltage,
                "APM OUTPUT",
                formatVoltage(state.apmOutputVoltageV),
                "Accessory Power Module output",
                state.pcgConnected ? COLOR_CYAN : COLOR_MUTED);

        drawCard(canvas, current,
                "APM CURRENT",
                formatCurrent(state.apmCurrentA),
                "Live output current",
                state.pcgConnected ? COLOR_CYAN : COLOR_MUTED);

        drawCard(canvas, power,
                "APM POWER",
                formatPower(state.apmPowerW),
                "Live output power",
                state.pcgConnected ? COLOR_CYAN : COLOR_MUTED);

        drawCard(canvas, dcDc,
                "DC/DC STATE",
                safeState(state.apmState),
                "APM operating state",
                state.pcgConnected ? COLOR_GREEN : COLOR_AMBER);

        drawCard(canvas, gateway,
                "PCG-1 LINK",
                state.pcgConnected ? "ONLINE" : "OFFLINE",
                formatAge(state.pcgDataAgeMs),
                state.pcgConnected ? COLOR_GREEN : COLOR_AMBER);
    }

    private RectF card(float left, float top, float width, float height) {
        return new RectF(left, top, left + width, top + height);
    }

    private void drawCard(Canvas canvas,
                          RectF rect,
                          String title,
                          String value,
                          String detail,
                          int valueColor) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(rect, dp(16), dp(16), paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(COLOR_STROKE);
        canvas.drawRoundRect(rect, dp(16), dp(16), paint);

        drawText(canvas, title,
                rect.left + dp(18), rect.top + dp(30),
                dp(11), COLOR_MUTED, true);

        float size = dp(28);
        paint.setTextSize(size);
        paint.setTypeface(android.graphics.Typeface.create(
                android.graphics.Typeface.SANS_SERIF,
                android.graphics.Typeface.BOLD));
        while (paint.measureText(value) > rect.width() - dp(36)
                && size > dp(15)) {
            size -= dp(1);
            paint.setTextSize(size);
        }

        drawText(canvas, value,
                rect.left + dp(18), rect.centerY() + dp(8),
                size, valueColor, true);

        drawText(canvas, detail,
                rect.left + dp(18), rect.bottom - dp(20),
                dp(10), COLOR_MUTED, false);
    }

    private String formatVoltage(float value) {
        return Float.isNaN(value)
                ? "-- V"
                : String.format(Locale.US, "%.2f V", value);
    }

    private String formatCurrent(float value) {
        return Float.isNaN(value)
                ? "-- A"
                : String.format(Locale.US, "%.1f A", value);
    }

    private String formatPower(float value) {
        return Float.isNaN(value)
                ? "-- W"
                : String.format(Locale.US, "%.0f W", value);
    }

    private String safeState(String value) {
        if (value == null || value.isEmpty() || "UNKNOWN".equals(value)) {
            return "UNKNOWN";
        }
        return value.toUpperCase(Locale.US);
    }

    private String formatAge(long ageMs) {
        if (ageMs < 0L) {
            return "No gateway data received";
        }
        if (ageMs < 1000L) {
            return String.format(Locale.US, "Data age %d ms", ageMs);
        }
        return String.format(Locale.US, "Data age %.1f s", ageMs / 1000.0f);
    }

    private void drawText(Canvas canvas,
                          String text,
                          float x,
                          float baseline,
                          float size,
                          int color,
                          boolean bold) {
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(android.graphics.Typeface.create(
                android.graphics.Typeface.SANS_SERIF,
                bold
                        ? android.graphics.Typeface.BOLD
                        : android.graphics.Typeface.NORMAL));
        paint.setTextSize(size);
        paint.setColor(color);
        canvas.drawText(text, x, baseline, paint);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
