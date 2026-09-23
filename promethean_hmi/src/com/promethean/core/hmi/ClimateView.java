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

public final class ClimateView extends View {
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

    private float temperatureF = 72f;
    private int fanLevel = 3;
    private boolean autoMode = true;
    private boolean acOn = true;
    private boolean frontDefrost = false;
    private boolean rearDefrost = false;
    private boolean recirculate = false;

    private RectF tempMinus = new RectF();
    private RectF tempPlus = new RectF();
    private RectF fanMinus = new RectF();
    private RectF fanPlus = new RectF();
    private RectF autoButton = new RectF();
    private RectF acButton = new RectF();
    private RectF frontDefrostButton = new RectF();
    private RectF rearDefrostButton = new RectF();
    private RectF recircButton = new RectF();

    public ClimateView(Context context) {
        super(context);
        init();
    }

    public ClimateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClimateView(Context context, AttributeSet attrs, int defStyleAttr) {
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

    public void applyTemperatureRotaryDelta(int detents) {
        if (detents == 0) {
            return;
        }
        temperatureF = clamp(temperatureF + detents, 60f, 90f);
        autoMode = false;
        invalidate();
    }

    public void applyFanRotaryDelta(int detents) {
        if (detents == 0) {
            return;
        }
        fanLevel = clamp(fanLevel + detents, 0, 8);
        autoMode = false;
        invalidate();
    }

    public float getTemperatureF() {
        return temperatureF;
    }

    public int getFanLevel() {
        return fanLevel;
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
        float gap = dp(18);
        float top = margin;
        float dialAreaHeight = h * 0.58f;

        float dialWidth = (w - margin * 2f - gap) / 2f;

        RectF tempCard = new RectF(
                margin, top,
                margin + dialWidth, dialAreaHeight);

        RectF fanCard = new RectF(
                margin + dialWidth + gap, top,
                w - margin, dialAreaHeight);

        drawTemperatureCard(canvas, tempCard);
        drawFanCard(canvas, fanCard);

        float buttonsTop = dialAreaHeight + gap;
        float usableWidth = w - margin * 2f;
        float buttonGap = dp(10);
        float buttonWidth = (usableWidth - buttonGap * 4f) / 5f;

        autoButton = new RectF(
                margin, buttonsTop,
                margin + buttonWidth, h - margin);
        acButton = shifted(autoButton, buttonWidth + buttonGap);
        frontDefrostButton = shifted(acButton, buttonWidth + buttonGap);
        rearDefrostButton = shifted(frontDefrostButton, buttonWidth + buttonGap);
        recircButton = shifted(rearDefrostButton, buttonWidth + buttonGap);

        drawToggleButton(canvas, autoButton, "AUTO", autoMode, COLOR_GREEN);
        drawToggleButton(canvas, acButton, "A/C", acOn, COLOR_CYAN);
        drawToggleButton(canvas, frontDefrostButton, "FRONT\nDEFROST",
                frontDefrost, COLOR_AMBER);
        drawToggleButton(canvas, rearDefrostButton, "REAR\nDEFROST",
                rearDefrost, COLOR_AMBER);
        drawToggleButton(canvas, recircButton, "RECIRC",
                recirculate, COLOR_CYAN);
    }

    private void drawTemperatureCard(Canvas canvas, RectF rect) {
        drawPanel(canvas, rect, COLOR_STROKE);

        float cx = rect.centerX();
        float cy = rect.top + rect.height() * 0.46f;
        float radius = Math.min(rect.width(), rect.height()) * 0.25f;

        drawDial(canvas, cx, cy, radius, COLOR_CYAN,
                temperatureF, 60f, 90f);

        drawCenteredText(canvas, "TEMPERATURE",
                cx, rect.top + dp(35),
                dp(12), COLOR_MUTED, true);

        drawCenteredText(canvas,
                String.format(Locale.US, "%.0f°", temperatureF),
                cx, cy + dp(13),
                dp(38), COLOR_TEXT, true);

        drawCenteredText(canvas, "ROTARY + TOUCH",
                cx, cy + radius + dp(34),
                dp(10), COLOR_CYAN, true);

        float buttonW = dp(82);
        float buttonH = dp(52);
        tempMinus = new RectF(
                cx - buttonW - dp(10),
                rect.bottom - buttonH - dp(18),
                cx - dp(10),
                rect.bottom - dp(18));
        tempPlus = new RectF(
                cx + dp(10),
                rect.bottom - buttonH - dp(18),
                cx + buttonW + dp(10),
                rect.bottom - dp(18));

        drawStepButton(canvas, tempMinus, "−");
        drawStepButton(canvas, tempPlus, "+");
    }

    private void drawFanCard(Canvas canvas, RectF rect) {
        drawPanel(canvas, rect, COLOR_STROKE);

        float cx = rect.centerX();
        float cy = rect.top + rect.height() * 0.46f;
        float radius = Math.min(rect.width(), rect.height()) * 0.25f;

        drawDial(canvas, cx, cy, radius, COLOR_CYAN,
                fanLevel, 0f, 8f);

        drawCenteredText(canvas, "FAN",
                cx, rect.top + dp(35),
                dp(12), COLOR_MUTED, true);

        drawCenteredText(canvas,
                Integer.toString(fanLevel),
                cx, cy + dp(13),
                dp(38), COLOR_TEXT, true);

        drawCenteredText(canvas, "ROTARY + TOUCH",
                cx, cy + radius + dp(34),
                dp(10), COLOR_CYAN, true);

        float buttonW = dp(82);
        float buttonH = dp(52);
        fanMinus = new RectF(
                cx - buttonW - dp(10),
                rect.bottom - buttonH - dp(18),
                cx - dp(10),
                rect.bottom - dp(18));
        fanPlus = new RectF(
                cx + dp(10),
                rect.bottom - buttonH - dp(18),
                cx + buttonW + dp(10),
                rect.bottom - dp(18));

        drawStepButton(canvas, fanMinus, "−");
        drawStepButton(canvas, fanPlus, "+");
    }

    private void drawDial(Canvas canvas,
                          float cx, float cy, float radius,
                          int accent,
                          float value, float min, float max) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(dp(8));
        paint.setColor(COLOR_STROKE);

        RectF ring = new RectF(
                cx - radius, cy - radius,
                cx + radius, cy + radius);

        canvas.drawArc(ring, 135f, 270f, false, paint);

        float fraction = (value - min) / (max - min);
        fraction = Math.max(0f, Math.min(1f, fraction));

        paint.setColor(accent);
        canvas.drawArc(ring, 135f, 270f * fraction, false, paint);

        double angle = Math.toRadians(135f + 270f * fraction);
        float markerRadius = radius - dp(2);
        float mx = cx + (float) Math.cos(angle) * markerRadius;
        float my = cy + (float) Math.sin(angle) * markerRadius;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(accent);
        canvas.drawCircle(mx, my, dp(7), paint);
    }

    private void drawStepButton(Canvas canvas, RectF rect, String label) {
        drawPanel(canvas, rect, COLOR_STROKE);
        drawCenteredText(canvas, label,
                rect.centerX(), rect.centerY() + dp(12),
                dp(34), COLOR_CYAN, true);
    }

    private void drawToggleButton(Canvas canvas,
                                  RectF rect,
                                  String label,
                                  boolean active,
                                  int accent) {
        drawPanel(canvas, rect, active ? accent : COLOR_STROKE);

        String[] lines = label.split("\\n");
        if (lines.length == 1) {
            drawCenteredText(canvas, lines[0],
                    rect.centerX(), rect.centerY() + dp(6),
                    dp(16), active ? accent : COLOR_TEXT, true);
        } else {
            drawCenteredText(canvas, lines[0],
                    rect.centerX(), rect.centerY() - dp(4),
                    dp(13), active ? accent : COLOR_TEXT, true);
            drawCenteredText(canvas, lines[1],
                    rect.centerX(), rect.centerY() + dp(16),
                    dp(13), active ? accent : COLOR_TEXT, true);
        }
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) {
            return true;
        }

        float x = event.getX();
        float y = event.getY();

        if (tempMinus.contains(x, y)) {
            applyTemperatureRotaryDelta(-1);
        } else if (tempPlus.contains(x, y)) {
            applyTemperatureRotaryDelta(1);
        } else if (fanMinus.contains(x, y)) {
            applyFanRotaryDelta(-1);
        } else if (fanPlus.contains(x, y)) {
            applyFanRotaryDelta(1);
        } else if (autoButton.contains(x, y)) {
            autoMode = !autoMode;
            invalidate();
        } else if (acButton.contains(x, y)) {
            acOn = !acOn;
            autoMode = false;
            invalidate();
        } else if (frontDefrostButton.contains(x, y)) {
            frontDefrost = !frontDefrost;
            autoMode = false;
            invalidate();
        } else if (rearDefrostButton.contains(x, y)) {
            rearDefrost = !rearDefrost;
            invalidate();
        } else if (recircButton.contains(x, y)) {
            recirculate = !recirculate;
            autoMode = false;
            invalidate();
        }

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

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
