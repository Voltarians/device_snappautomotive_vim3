package com.promethean.core.hmi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.Locale;

public final class ChargingView extends View {
    private static final int COLOR_BG = 0xFF070D10;
    private static final int COLOR_PANEL = 0xFF10181D;
    private static final int COLOR_STROKE = 0xFF27414A;
    private static final int COLOR_TEXT = 0xFFF2F7F8;
    private static final int COLOR_MUTED = 0xFF93A7AF;
    private static final int COLOR_CYAN = 0xFF4ED9F5;
    private static final int COLOR_GREEN = 0xFF69E7A7;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    private VehicleState state = new DemoVehicleDataSource().read();

    public ChargingView(Context context) {
        super(context);
        init();
    }

    public ChargingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChargingView(Context context, AttributeSet attrs, int defStyleAttr) {
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

        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }

        float margin = dp(22);
        float gap = dp(16);

        float topArea = h * 0.60f;
        float leftWidth = w * 0.42f;
        RectF batteryCard = new RectF(
                margin,
                margin,
                leftWidth,
                topArea - gap);

        RectF modeCard = new RectF(
                leftWidth + gap,
                margin,
                w - margin,
                topArea - gap);

        drawBatteryCard(canvas, batteryCard);
        drawModeCard(canvas, modeCard);

        float bottomTop = topArea;
        float usable = w - margin * 2f - gap * 2f;
        float cardWidth = usable / 3f;

        RectF connectionCard = new RectF(
                margin,
                bottomTop,
                margin + cardWidth,
                h - margin);

        RectF powerCard = new RectF(
                margin + cardWidth + gap,
                bottomTop,
                margin + cardWidth * 2f + gap,
                h - margin);

        RectF completeCard = new RectF(
                margin + cardWidth * 2f + gap * 2f,
                bottomTop,
                w - margin,
                h - margin);

        drawInfoCard(canvas, connectionCard,
                "CONNECTION", "NOT AVAILABLE",
                "Waiting for live vehicle data", COLOR_MUTED);

        drawInfoCard(canvas, powerCard,
                "CHARGING POWER", "-- kW",
                "Live EVSE power", COLOR_CYAN);

        drawInfoCard(canvas, completeCard,
                "EST. COMPLETE", "--",
                "Completion estimate", COLOR_TEXT);
    }

    private void drawBatteryCard(Canvas canvas, RectF rect) {
        drawPanel(canvas, rect, COLOR_CYAN);

        float cx = rect.centerX();
        float batteryTop = rect.top + rect.height() * 0.20f;
        float batteryWidth = rect.width() * 0.52f;
        float batteryHeight = rect.height() * 0.34f;

        RectF body = new RectF(
                cx - batteryWidth / 2f,
                batteryTop,
                cx + batteryWidth / 2f,
                batteryTop + batteryHeight);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(4));
        paint.setColor(COLOR_TEXT);
        canvas.drawRoundRect(body, dp(8), dp(8), paint);
        canvas.drawRect(
                body.right,
                body.centerY() - dp(13),
                body.right + dp(11),
                body.centerY() + dp(13),
                paint);

        float fill = Math.max(0f, Math.min(1f, state.batteryPercent / 100f));
        RectF charge = new RectF(
                body.left + dp(7),
                body.bottom - dp(7) - (body.height() - dp(14)) * fill,
                body.right - dp(7),
                body.bottom - dp(7));

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOR_CYAN);
        canvas.drawRoundRect(charge, dp(4), dp(4), paint);

        drawCenteredText(canvas, "BATTERY",
                cx, rect.bottom - dp(74),
                dp(12), COLOR_MUTED, true);

        drawCenteredText(canvas,
                String.format(Locale.US, "%d%%", state.batteryPercent),
                cx, rect.bottom - dp(35),
                dp(34), COLOR_GREEN, true);
    }

    private void drawModeCard(Canvas canvas, RectF rect) {
        drawPanel(canvas, rect, COLOR_STROKE);

        float cx = rect.centerX();

        drawPlugIcon(canvas, cx, rect.top + rect.height() * 0.29f);

        drawCenteredText(canvas, "CHARGE MODE",
                cx, rect.centerY() + dp(18),
                dp(12), COLOR_MUTED, true);

        String mode = state.chargeMode == null
                ? "UNKNOWN"
                : state.chargeMode.toUpperCase(Locale.US);

        drawCenteredText(canvas, mode,
                cx, rect.centerY() + dp(52),
                dp(25), COLOR_CYAN, true);

        drawCenteredText(canvas, "Live control integration pending",
                cx, rect.bottom - dp(24),
                dp(11), COLOR_MUTED, false);
    }

    private void drawPlugIcon(Canvas canvas, float cx, float cy) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(5));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(COLOR_CYAN);

        float half = dp(18);
        RectF plug = new RectF(
                cx - half,
                cy - dp(10),
                cx + half,
                cy + dp(18));

        canvas.drawRoundRect(plug, dp(5), dp(5), paint);
        canvas.drawLine(cx - dp(9), cy - dp(25),
                cx - dp(9), cy - dp(10), paint);
        canvas.drawLine(cx + dp(9), cy - dp(25),
                cx + dp(9), cy - dp(10), paint);
        canvas.drawLine(cx, cy + dp(18),
                cx, cy + dp(40), paint);

        path.reset();
        path.moveTo(cx, cy + dp(40));
        path.cubicTo(
                cx, cy + dp(58),
                cx + dp(28), cy + dp(58),
                cx + dp(28), cy + dp(76));
        canvas.drawPath(path, paint);
    }

    private void drawInfoCard(Canvas canvas,
                              RectF rect,
                              String title,
                              String value,
                              String detail,
                              int valueColor) {
        drawPanel(canvas, rect, COLOR_STROKE);

        drawText(canvas, title,
                rect.left + dp(18),
                rect.top + dp(28),
                dp(11), COLOR_MUTED, true);

        float valueSize = dp(22);
        paint.setTypeface(android.graphics.Typeface.create(
                android.graphics.Typeface.SANS_SERIF,
                android.graphics.Typeface.BOLD));
        paint.setTextSize(valueSize);
        while (paint.measureText(value) > rect.width() - dp(36)
                && valueSize > dp(14)) {
            valueSize -= dp(1);
            paint.setTextSize(valueSize);
        }

        drawText(canvas, value,
                rect.left + dp(18),
                rect.centerY() + dp(4),
                valueSize, valueColor, true);

        drawText(canvas, detail,
                rect.left + dp(18),
                rect.bottom - dp(22),
                dp(10), COLOR_MUTED, false);
    }

    private void drawPanel(Canvas canvas, RectF rect, int strokeColor) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOR_PANEL);
        canvas.drawRoundRect(rect, dp(18), dp(18), paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(strokeColor);
        canvas.drawRoundRect(rect, dp(18), dp(18), paint);
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

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
