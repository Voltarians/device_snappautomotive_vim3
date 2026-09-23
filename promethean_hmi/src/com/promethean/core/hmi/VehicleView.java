package com.promethean.core.hmi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Locale;

public final class VehicleView extends View {
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

    private final RectF settingsButton = new RectF();
    private final RectF diagnosticsButton = new RectF();
    private final RectF tpmsButton = new RectF();
    private final RectF maintenanceButton = new RectF();

    private String selectedAction = "";

    public VehicleView(Context context) {
        super(context);
        init();
    }

    public VehicleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VehicleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setTypeface(android.graphics.Typeface.create(
                android.graphics.Typeface.SANS_SERIF,
                android.graphics.Typeface.NORMAL));
        setClickable(true);
        setFocusable(true);
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

        float topCardHeight = h * 0.23f;
        float topWidth = (w - margin * 2f - gap * 2f) / 3f;

        RectF hvCard = new RectF(
                margin, margin,
                margin + topWidth, margin + topCardHeight);
        RectF auxCard = shifted(hvCard, topWidth + gap);
        RectF dtcCard = shifted(auxCard, topWidth + gap);

        drawStatusCard(canvas, hvCard,
                "HV BATTERY",
                String.format(Locale.US, "%d%%", state.batteryPercent),
                String.format(Locale.US, "%d mi electric range",
                        state.electricRangeMiles),
                COLOR_GREEN);

        drawStatusCard(canvas, auxCard,
                "12 V SYSTEM",
                "-- V",
                "Waiting for live vehicle data",
                COLOR_MUTED);

        drawStatusCard(canvas, dtcCard,
                "DIAGNOSTICS",
                "NOT SCANNED",
                "Voltarian / OBD Atlas integration",
                COLOR_AMBER);

        float middleTop = hvCard.bottom + gap;
        float actionHeight = dp(72);
        float middleBottom = h - margin - actionHeight - gap;
        float leftWidth = w * 0.55f;

        RectF tpmsCard = new RectF(
                margin, middleTop,
                leftWidth, middleBottom);

        RectF healthCard = new RectF(
                leftWidth + gap, middleTop,
                w - margin, middleBottom);

        drawTpmsCard(canvas, tpmsCard);
        drawHealthCard(canvas, healthCard);

        float actionTop = h - margin - actionHeight;
        float actionWidth = (w - margin * 2f - gap * 3f) / 4f;

        settingsButton.set(
                margin, actionTop,
                margin + actionWidth, h - margin);
        diagnosticsButton.set(
                margin + (actionWidth + gap), actionTop,
                margin + actionWidth * 2f + gap, h - margin);
        tpmsButton.set(
                margin + (actionWidth + gap) * 2f, actionTop,
                margin + actionWidth * 3f + gap * 2f, h - margin);
        maintenanceButton.set(
                margin + (actionWidth + gap) * 3f, actionTop,
                w - margin, h - margin);

        drawActionButton(canvas, settingsButton,
                "VEHICLE", "SETTINGS");
        drawActionButton(canvas, diagnosticsButton,
                "OPEN", "DIAGNOSTICS");
        drawActionButton(canvas, tpmsButton,
                "TPMS", "DETAILS");
        drawActionButton(canvas, maintenanceButton,
                "SERVICE", "HISTORY");
    }

    private void drawStatusCard(Canvas canvas,
                                RectF rect,
                                String title,
                                String value,
                                String detail,
                                int valueColor) {
        drawPanel(canvas, rect, COLOR_STROKE);

        drawText(canvas, title,
                rect.left + dp(18), rect.top + dp(28),
                dp(11), COLOR_MUTED, true);

        float size = dp(25);
        paint.setTypeface(android.graphics.Typeface.create(
                android.graphics.Typeface.SANS_SERIF,
                android.graphics.Typeface.BOLD));
        paint.setTextSize(size);
        while (paint.measureText(value) > rect.width() - dp(36)
                && size > dp(14)) {
            size -= dp(1);
            paint.setTextSize(size);
        }

        drawText(canvas, value,
                rect.left + dp(18), rect.centerY() + dp(8),
                size, valueColor, true);

        drawText(canvas, detail,
                rect.left + dp(18), rect.bottom - dp(18),
                dp(10), COLOR_MUTED, false);
    }

    private void drawTpmsCard(Canvas canvas, RectF rect) {
        drawPanel(canvas, rect, COLOR_STROKE);

        drawText(canvas, "TIRE / TPMS",
                rect.left + dp(18), rect.top + dp(28),
                dp(11), COLOR_MUTED, true);

        float cx = rect.centerX();
        float cy = rect.centerY() + dp(6);
        float carW = rect.width() * 0.28f;
        float carH = rect.height() * 0.56f;

        RectF body = new RectF(
                cx - carW / 2f, cy - carH / 2f,
                cx + carW / 2f, cy + carH / 2f);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(3));
        paint.setColor(COLOR_CYAN);
        canvas.drawRoundRect(body, dp(20), dp(20), paint);

        float wheelW = dp(12);
        float wheelH = carH * 0.22f;
        drawWheel(canvas, body.left - wheelW, body.top + dp(22),
                wheelW, wheelH);
        drawWheel(canvas, body.right, body.top + dp(22),
                wheelW, wheelH);
        drawWheel(canvas, body.left - wheelW, body.bottom - wheelH - dp(22),
                wheelW, wheelH);
        drawWheel(canvas, body.right, body.bottom - wheelH - dp(22),
                wheelW, wheelH);

        drawPressure(canvas,
                body.left - dp(70), body.top + dp(42),
                "LF", "-- PSI");
        drawPressure(canvas,
                body.right + dp(26), body.top + dp(42),
                "RF", "-- PSI");
        drawPressure(canvas,
                body.left - dp(70), body.bottom - dp(34),
                "LR", "-- PSI");
        drawPressure(canvas,
                body.right + dp(26), body.bottom - dp(34),
                "RR", "-- PSI");

        drawCenteredText(canvas,
                "Waiting for live TPMS IDs and pressures",
                cx, rect.bottom - dp(18),
                dp(10), COLOR_MUTED, false);
    }

    private void drawWheel(Canvas canvas,
                           float left, float top,
                           float width, float height) {
        RectF wheel = new RectF(left, top, left + width, top + height);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOR_TEXT);
        canvas.drawRoundRect(wheel, dp(4), dp(4), paint);
    }

    private void drawPressure(Canvas canvas,
                              float x, float y,
                              String position,
                              String pressure) {
        drawText(canvas, position,
                x, y,
                dp(10), COLOR_MUTED, true);
        drawText(canvas, pressure,
                x, y + dp(19),
                dp(14), COLOR_TEXT, true);
    }

    private void drawHealthCard(Canvas canvas, RectF rect) {
        drawPanel(canvas, rect, COLOR_STROKE);

        drawText(canvas, "VEHICLE HEALTH",
                rect.left + dp(18), rect.top + dp(28),
                dp(11), COLOR_MUTED, true);

        float x = rect.left + dp(18);
        float top = rect.top + dp(58);
        float lineGap = dp(48);

        drawHealthLine(canvas, x, top,
                "HV COOLANT", "-- °F", COLOR_CYAN);
        drawHealthLine(canvas, x, top + lineGap,
                "ENGINE COOLANT", "-- °F", COLOR_CYAN);
        drawHealthLine(canvas, x, top + lineGap * 2f,
                "OIL LIFE", "-- %", COLOR_TEXT);
        drawHealthLine(canvas, x, top + lineGap * 3f,
                "TIRE ROTATION", "UNKNOWN", COLOR_AMBER);

        drawText(canvas, "Live thermal and maintenance feed pending",
                x, rect.bottom - dp(18),
                dp(10), COLOR_MUTED, false);
    }

    private void drawHealthLine(Canvas canvas,
                                float x, float y,
                                String label,
                                String value,
                                int valueColor) {
        drawText(canvas, label,
                x, y,
                dp(10), COLOR_MUTED, true);
        drawText(canvas, value,
                x, y + dp(20),
                dp(18), valueColor, true);
    }

    private void drawActionButton(Canvas canvas,
                                  RectF rect,
                                  String line1,
                                  String line2) {
        boolean selected = (line1 + " " + line2).equals(selectedAction);
        drawPanel(canvas, rect, selected ? COLOR_CYAN : COLOR_STROKE);

        drawCenteredText(canvas, line1,
                rect.centerX(), rect.centerY() - dp(2),
                dp(12), selected ? COLOR_CYAN : COLOR_TEXT, true);
        drawCenteredText(canvas, line2,
                rect.centerX(), rect.centerY() + dp(17),
                dp(12), selected ? COLOR_CYAN : COLOR_TEXT, true);
    }

    private void drawPanel(Canvas canvas, RectF rect, int strokeColor) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(rect, dp(16), dp(16), paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(strokeColor);
        canvas.drawRoundRect(rect, dp(16), dp(16), paint);
    }

    private void drawCenteredText(Canvas canvas,
                                  String text,
                                  float x,
                                  float baseline,
                                  float size,
                                  int color,
                                  boolean bold) {
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.create(
                android.graphics.Typeface.SANS_SERIF,
                bold ? android.graphics.Typeface.BOLD
                        : android.graphics.Typeface.NORMAL));
        paint.setTextSize(size);
        paint.setColor(color);
        canvas.drawText(text, x, baseline, paint);
        paint.setTextAlign(Paint.Align.LEFT);
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
                bold ? android.graphics.Typeface.BOLD
                        : android.graphics.Typeface.NORMAL));
        paint.setTextSize(size);
        paint.setColor(color);
        canvas.drawText(text, x, baseline, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) {
            return true;
        }

        float x = event.getX();
        float y = event.getY();

        if (settingsButton.contains(x, y)) {
            selectedAction = "VEHICLE SETTINGS";
        } else if (diagnosticsButton.contains(x, y)) {
            selectedAction = "OPEN DIAGNOSTICS";
        } else if (tpmsButton.contains(x, y)) {
            selectedAction = "TPMS DETAILS";
        } else if (maintenanceButton.contains(x, y)) {
            selectedAction = "SERVICE HISTORY";
        } else {
            return true;
        }

        invalidate();
        performClick();
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private RectF shifted(RectF source, float dx) {
        return new RectF(
                source.left + dx,
                source.top,
                source.right + dx,
                source.bottom);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
