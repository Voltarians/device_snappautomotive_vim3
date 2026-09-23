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
    private static final int COLOR_BG = 0xFF070D10;
    private static final int COLOR_PANEL = 0xFF10181D;
    private static final int COLOR_STROKE = 0xFF27414A;
    private static final int COLOR_TEXT = 0xFFF2F7F8;
    private static final int COLOR_MUTED = 0xFF93A7AF;
    private static final int COLOR_CYAN = 0xFF4ED9F5;
    private static final int COLOR_GREEN = 0xFF69E7A7;
    private static final int COLOR_AMBER = 0xFFF1B84B;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

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

        float batteryX = w * 0.16f;
        float engineX = w * 0.48f;
        float motorX = w * 0.50f;
        float wheelsX = w * 0.82f;
        float upperY = h * 0.31f;
        float lowerY = h * 0.62f;

        float nodeW = Math.min(w * 0.18f, dp(190));
        float nodeH = Math.min(h * 0.25f, dp(150));

        RectF battery = centeredRect(batteryX, lowerY, nodeW, nodeH);
        RectF engine = centeredRect(engineX, upperY, nodeW, nodeH);
        RectF motor = centeredRect(motorX, lowerY, nodeW, nodeH);
        RectF wheels = centeredRect(wheelsX, lowerY, nodeW, nodeH);

        boolean regen = state.propulsionMode.toUpperCase(Locale.US).contains("REGEN");
        boolean engineOn = state.propulsionMode.toUpperCase(Locale.US).contains("ENGINE");

        drawBasePath(canvas, battery.centerX() + nodeW / 2f, battery.centerY(),
                motor.centerX() - nodeW / 2f, motor.centerY());
        drawBasePath(canvas, motor.centerX() + nodeW / 2f, motor.centerY(),
                wheels.centerX() - nodeW / 2f, wheels.centerY());
        drawBasePath(canvas, engine.centerX(), engine.bottom,
                motor.centerX(), motor.top);

        if (regen) {
            drawAnimatedFlow(canvas,
                    wheels.centerX() - nodeW / 2f, wheels.centerY(),
                    motor.centerX() + nodeW / 2f, motor.centerY(),
                    COLOR_GREEN);
            drawAnimatedFlow(canvas,
                    motor.centerX() - nodeW / 2f, motor.centerY(),
                    battery.centerX() + nodeW / 2f, battery.centerY(),
                    COLOR_GREEN);
        } else {
            drawAnimatedFlow(canvas,
                    battery.centerX() + nodeW / 2f, battery.centerY(),
                    motor.centerX() - nodeW / 2f, motor.centerY(),
                    COLOR_CYAN);
            drawAnimatedFlow(canvas,
                    motor.centerX() + nodeW / 2f, motor.centerY(),
                    wheels.centerX() - nodeW / 2f, wheels.centerY(),
                    COLOR_CYAN);
            if (engineOn) {
                drawAnimatedFlow(canvas,
                        engine.centerX(), engine.bottom,
                        motor.centerX(), motor.top,
                        COLOR_AMBER);
            }
        }

        drawBatteryNode(canvas, battery);
        drawEngineNode(canvas, engine, engineOn);
        drawMotorNode(canvas, motor, regen);
        drawWheelsNode(canvas, wheels);

        drawMetrics(canvas, w, h);

        postInvalidateDelayed(45);
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

        float fill = Math.max(0f, Math.min(1f, state.batteryPercent / 100f));
        RectF charge = new RectF(
                body.left + dp(5),
                body.bottom - dp(5) - (body.height() - dp(10)) * fill,
                body.right - dp(5),
                body.bottom - dp(5));

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOR_CYAN);
        canvas.drawRoundRect(charge, dp(3), dp(3), paint);

        drawNodeLabel(canvas, rect, "BATTERY",
                String.format(Locale.US, "%d%%", state.batteryPercent),
                COLOR_CYAN);
    }

    private void drawEngineNode(Canvas canvas, RectF rect, boolean engineOn) {
        int accent = engineOn ? COLOR_AMBER : COLOR_MUTED;
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

        drawNodeLabel(canvas, rect, "ENGINE / GENERATOR",
                engineOn ? "ON" : "OFF",
                accent);
    }

    private void drawMotorNode(Canvas canvas, RectF rect, boolean regen) {
        int accent = regen ? COLOR_GREEN : COLOR_CYAN;
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
                regen ? "REGEN" : "DRIVE",
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

        drawNodeLabel(canvas, rect, "WHEELS", "DRIVE", COLOR_TEXT);
    }

    private void drawNodeLabel(Canvas canvas,
                               RectF rect,
                               String title,
                               String value,
                               int accent) {
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.create(
                android.graphics.Typeface.SANS_SERIF,
                android.graphics.Typeface.BOLD));

        paint.setColor(COLOR_MUTED);
        paint.setTextSize(dp(11));
        canvas.drawText(title, rect.centerX(), rect.bottom - dp(28), paint);

        paint.setColor(accent);
        paint.setTextSize(dp(15));
        canvas.drawText(value, rect.centerX(), rect.bottom - dp(10), paint);

        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawMetrics(Canvas canvas, float w, float h) {
        float top = h - dp(92);
        float left = dp(22);
        float gap = w * 0.22f;

        drawMetric(canvas, left, top,
                "ELECTRIC RANGE",
                String.format(Locale.US, "%d mi", state.electricRangeMiles),
                COLOR_GREEN);

        drawMetric(canvas, left + gap, top,
                "EFFICIENCY",
                String.format(Locale.US, "%.1f mi/kWh", state.efficiencyMiPerKwh),
                COLOR_CYAN);

        drawMetric(canvas, left + gap * 2f, top,
                "TOTAL RANGE",
                String.format(Locale.US, "%d mi", state.totalRangeMiles),
                COLOR_TEXT);

        drawMetric(canvas, left + gap * 3f, top,
                "CHARGE MODE",
                state.chargeMode,
                COLOR_TEXT);
    }

    private void drawMetric(Canvas canvas,
                            float x, float y,
                            String label, String value,
                            int valueColor) {
        paint.setTypeface(android.graphics.Typeface.create(
                android.graphics.Typeface.SANS_SERIF,
                android.graphics.Typeface.BOLD));

        paint.setColor(COLOR_MUTED);
        paint.setTextSize(dp(11));
        canvas.drawText(label, x, y, paint);

        paint.setColor(valueColor);
        paint.setTextSize(dp(20));
        canvas.drawText(value, x, y + dp(28), paint);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
