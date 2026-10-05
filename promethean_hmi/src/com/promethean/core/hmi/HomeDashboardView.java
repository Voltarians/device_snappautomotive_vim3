package com.promethean.core.hmi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class HomeDashboardView extends View {
    public interface OnSectionSelectedListener {
        void onSectionSelected(String section);
    }

    private static final float DESIGN_W = 1280f;
    private static final float DESIGN_H = 720f;

    private static final int BG_TOP = Color.rgb(11, 20, 29);
    private static final int BG_BOTTOM = Color.rgb(5, 9, 13);
    private static final int BLUE = Color.rgb(22, 159, 232);
    private static final int BLUE_LIGHT = Color.rgb(77, 199, 255);
    private static final int TRACK = Color.rgb(21, 36, 49);
    private static final int STROKE = Color.rgb(49, 82, 105);
    private static final int TEXT = Color.rgb(243, 247, 250);
    private static final int MUTED = Color.rgb(143, 166, 184);

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("h:mm a", Locale.getDefault());

    private VehicleState state;
    private OnSectionSelectedListener sectionListener;

    private final String[] sections = {
            "HOME", "RADIO", "CLIMATE", "VEHICLE", "ENERGY",
            "PHONE", "NAV", "APPS", "SETTINGS"
    };

    public HomeDashboardView(Context context) {
        super(context);
        init();
    }

    public HomeDashboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HomeDashboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundColor(Color.TRANSPARENT);
        setClickable(true);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);
    }

    public void setVehicleState(VehicleState newState) {
        state = newState;
        invalidate();
    }

    public void setOnSectionSelectedListener(OnSectionSelectedListener listener) {
        sectionListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float sx = getWidth() / DESIGN_W;
        float sy = getHeight() / DESIGN_H;
        canvas.save();
        canvas.scale(sx, sy);

        paint.setShader(new LinearGradient(
                0f, 0f, 0f, DESIGN_H, BG_TOP, BG_BOTTOM, Shader.TileMode.CLAMP));
        canvas.drawRect(0f, 0f, DESIGN_W, DESIGN_H, paint);
        paint.setShader(null);

        paint.setColor(BLUE);
        paint.setAlpha(140);
        canvas.drawRect(0f, 0f, DESIGN_W, 3f, paint);
        paint.setAlpha(255);

        drawLogo(canvas);

        Date now = new Date();
        drawText(canvas, dateFormat.format(now).toUpperCase(Locale.getDefault()),
                64f, 118f, 22f, MUTED, Paint.Align.LEFT, false);
        drawText(canvas, timeFormat.format(now),
                1216f, 118f, 26f, TEXT, Paint.Align.RIGHT, true);

        strokePaint.setColor(Color.rgb(41, 64, 82));
        strokePaint.setStrokeWidth(2f);
        canvas.drawLine(64f, 140f, 1216f, 140f, strokePaint);

        int evMiles = displayEvMiles();
        int fuelMiles = displayFuelMiles();
        int totalMiles = displayTotalMiles();

        drawMetricBar(canvas, "EV RANGE", evMiles + " mi", 64f, 192f, 210f,
                evFraction(), false);
        drawMetricBar(canvas, "FUEL RANGE", fuelMiles + " mi", 64f, 316f, 334f,
                fuelFraction(), true);

        drawLowerCards(canvas, totalMiles);
        drawBottomNav(canvas);

        canvas.restore();
    }

    private void drawLogo(Canvas canvas) {
        final float y = 68f;

        paint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        paint.setTextSize(66f);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.rgb(201, 209, 218));
        canvas.drawText("PROMETHE", 400f, y, paint);

        float ax = 724f;
        path.reset();
        path.moveTo(ax, y);
        path.lineTo(ax + 28f, y - 58f);
        path.lineTo(ax + 56f, y);
        path.lineTo(ax + 42f, y);
        path.lineTo(ax + 28f, y - 30f);
        path.lineTo(ax + 14f, y);
        path.close();
        paint.setColor(BLUE);
        canvas.drawPath(path, paint);

        paint.setColor(Color.rgb(201, 209, 218));
        canvas.drawText("N", 784f, y, paint);

        paint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        paint.setTextSize(28f);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setLetterSpacing(0.22f);
        paint.setColor(Color.rgb(183, 194, 205));
        canvas.drawText("C O R E", 640f, 101f, paint);
        paint.setLetterSpacing(0f);
    }

    private void drawMetricBar(
            Canvas canvas,
            String label,
            String value,
            float x,
            float labelY,
            float barY,
            float fraction,
            boolean alternate) {

        drawText(canvas, label, x, labelY, 30f, TEXT, Paint.Align.LEFT, true);
        drawText(canvas, value, 1216f, labelY, 34f, TEXT, Paint.Align.RIGHT, true);

        float width = 1152f;
        float height = 54f;
        float radius = 17f;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(TRACK);
        canvas.drawRoundRect(new RectF(x, barY, x + width, barY + height), radius, radius, paint);

        strokePaint.setColor(STROKE);
        strokePaint.setStrokeWidth(2f);
        canvas.drawRoundRect(new RectF(x, barY, x + width, barY + height), radius, radius, strokePaint);

        float innerX = x + 7f;
        float innerY = barY + 7f;
        float innerW = (width - 14f) * clamp(fraction, 0f, 1f);
        float innerH = 40f;
        if (innerW > 4f) {
            int start = alternate ? Color.rgb(19, 127, 200) : Color.rgb(11, 111, 200);
            int end = alternate ? Color.rgb(90, 200, 240) : BLUE_LIGHT;
            paint.setShader(new LinearGradient(
                    innerX, innerY, innerX + Math.max(innerW, 1f), innerY,
                    start, end, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(
                    new RectF(innerX, innerY, innerX + innerW, innerY + innerH),
                    12f, 12f, paint);
            paint.setShader(null);

            paint.setColor(Color.argb(110, 220, 245, 255));
            canvas.drawRoundRect(
                    new RectF(innerX + 4f, innerY + 3f,
                            Math.max(innerX + 4f, innerX + innerW - 6f), innerY + 8f),
                    3f, 3f, paint);
        }
    }

    private void drawLowerCards(Canvas canvas, int totalMiles) {
        float top = 430f;

        drawText(canvas, "12V SYSTEM", 64f, top, 22f, MUTED, Paint.Align.LEFT, true);
        drawFancyBar(canvas, 64f, top + 18f, 430f, 44f, voltageFraction(), false);
        String voltage = display12Voltage();
        drawText(canvas, voltage, 64f, top + 94f, 34f, TEXT, Paint.Align.LEFT, true);

        drawText(canvas, "TOTAL DISTANCE TO DEPLETION",
                624f, top, 22f, MUTED, Paint.Align.LEFT, true);

        float x = 624f;
        float y = top + 18f;
        float w = 592f;
        float h = 44f;
        paint.setColor(TRACK);
        canvas.drawRoundRect(new RectF(x, y, x + w, y + h), 14f, 14f, paint);
        strokePaint.setColor(STROKE);
        strokePaint.setStrokeWidth(2f);
        canvas.drawRoundRect(new RectF(x, y, x + w, y + h), 14f, 14f, strokePaint);

        int ev = displayEvMiles();
        int fuel = displayFuelMiles();
        float combined = Math.max(1f, ev + fuel);
        float fillTotal = Math.min(0.78f, (ev + fuel) / 450f);
        float fillW = (w - 12f) * fillTotal;
        float evW = fillW * (ev / combined);

        paint.setColor(Color.rgb(32, 167, 238));
        canvas.drawRoundRect(new RectF(x + 6f, y + 6f, x + 6f + evW, y + h - 6f),
                9f, 9f, paint);
        paint.setColor(Color.rgb(39, 142, 216));
        canvas.drawRect(x + 6f + evW, y + 6f, x + 6f + fillW, y + h - 6f, paint);

        if (fillW > 10f) {
            paint.setColor(Color.argb(95, 215, 243, 255));
            canvas.drawRoundRect(
                    new RectF(x + 10f, y + 9f, x + 4f + fillW, y + 13f),
                    2f, 2f, paint);
        }

        drawText(canvas, totalMiles + " mi",
                1216f, top + 94f, 34f, TEXT, Paint.Align.RIGHT, true);
    }

    private void drawFancyBar(
            Canvas canvas, float x, float y, float w, float h,
            float fraction, boolean alternate) {
        paint.setColor(TRACK);
        canvas.drawRoundRect(new RectF(x, y, x + w, y + h), 14f, 14f, paint);
        strokePaint.setColor(STROKE);
        strokePaint.setStrokeWidth(2f);
        canvas.drawRoundRect(new RectF(x, y, x + w, y + h), 14f, 14f, strokePaint);

        float fillW = (w - 12f) * clamp(fraction, 0f, 1f);
        if (fillW > 2f) {
            int start = alternate ? Color.rgb(19, 127, 200) : Color.rgb(11, 111, 200);
            int end = alternate ? Color.rgb(90, 200, 240) : BLUE_LIGHT;
            paint.setShader(new LinearGradient(
                    x + 6f, y + 6f, x + 6f + Math.max(fillW, 1f), y + 6f,
                    start, end, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(
                    new RectF(x + 6f, y + 6f, x + 6f + fillW, y + h - 6f),
                    9f, 9f, paint);
            paint.setShader(null);
        }
    }

    private void drawBottomNav(Canvas canvas) {
        float top = 598f;
        paint.setColor(Color.rgb(7, 16, 23));
        canvas.drawRect(0f, top, DESIGN_W, DESIGN_H, paint);
        strokePaint.setColor(Color.rgb(38, 58, 73));
        strokePaint.setStrokeWidth(2f);
        canvas.drawLine(0f, top, DESIGN_W, top, strokePaint);

        float[] centers = {76f, 217f, 358f, 499f, 640f, 781f, 922f, 1063f, 1204f};
        String[] labels = {"HOME", "AUDIO", "CLIMATE", "VEHICLE", "ENERGY",
                "PHONE", "NAV", "APPS", "SETTINGS"};

        for (int i = 0; i < centers.length; i++) {
            float cx = centers[i];
            if (i == 0) {
                paint.setColor(Color.rgb(13, 33, 48));
                canvas.drawRoundRect(new RectF(cx - 58f, top + 8f, cx + 58f, top + 96f),
                        18f, 18f, paint);
                strokePaint.setColor(BLUE);
                strokePaint.setStrokeWidth(2f);
                canvas.drawRoundRect(new RectF(cx - 58f, top + 8f, cx + 58f, top + 96f),
                        18f, 18f, strokePaint);
            }

            drawIcon(canvas, i, cx, top + 50f, 31f);
            drawText(canvas, labels[i], cx, top + 112f, 15f,
                    i == 0 ? TEXT : MUTED, Paint.Align.CENTER, true);
        }
    }

    private void drawIcon(Canvas canvas, int index, float cx, float cy, float s) {
        strokePaint.setColor(BLUE);
        strokePaint.setStrokeWidth(6f);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);

        path.reset();
        switch (index) {
            case 0: // home
                path.moveTo(cx - s, cy);
                path.lineTo(cx, cy - s * .78f);
                path.lineTo(cx + s, cy);
                path.moveTo(cx - s * .7f, cy - 2f);
                path.lineTo(cx - s * .7f, cy + s * .75f);
                path.lineTo(cx - s * .2f, cy + s * .75f);
                path.lineTo(cx - s * .2f, cy + s * .2f);
                path.lineTo(cx + s * .2f, cy + s * .2f);
                path.lineTo(cx + s * .2f, cy + s * .75f);
                path.lineTo(cx + s * .7f, cy + s * .75f);
                path.lineTo(cx + s * .7f, cy - 2f);
                canvas.drawPath(path, strokePaint);
                break;
            case 1: // audio
                canvas.drawLine(cx - 8f, cy - 24f, cx - 8f, cy + 18f, strokePaint);
                canvas.drawLine(cx - 8f, cy - 18f, cx + 23f, cy - 25f, strokePaint);
                canvas.drawLine(cx + 23f, cy - 25f, cx + 23f, cy + 10f, strokePaint);
                canvas.drawCircle(cx - 18f, cy + 22f, 11f, strokePaint);
                canvas.drawCircle(cx + 13f, cy + 14f, 11f, strokePaint);
                break;
            case 2: // climate
                canvas.drawCircle(cx, cy, 8f, strokePaint);
                for (int k = 0; k < 4; k++) {
                    double a = Math.PI / 2 * k;
                    float x1 = cx + (float)Math.cos(a) * 16f;
                    float y1 = cy + (float)Math.sin(a) * 16f;
                    float x2 = cx + (float)Math.cos(a) * 29f;
                    float y2 = cy + (float)Math.sin(a) * 29f;
                    canvas.drawLine(x1, y1, x2, y2, strokePaint);
                    canvas.drawCircle(x2, y2, 7f, strokePaint);
                }
                break;
            case 3: // vehicle
                path.moveTo(cx - 31f, cy + 8f);
                path.lineTo(cx - 23f, cy - 12f);
                path.quadTo(cx - 18f, cy - 22f, cx - 7f, cy - 22f);
                path.lineTo(cx + 12f, cy - 22f);
                path.quadTo(cx + 23f, cy - 22f, cx + 28f, cy - 12f);
                path.lineTo(cx + 34f, cy + 8f);
                path.lineTo(cx + 34f, cy + 21f);
                path.lineTo(cx - 31f, cy + 21f);
                path.close();
                canvas.drawPath(path, strokePaint);
                canvas.drawCircle(cx - 18f, cy + 24f, 6f, strokePaint);
                canvas.drawCircle(cx + 21f, cy + 24f, 6f, strokePaint);
                break;
            case 4: // energy
                canvas.drawRoundRect(new RectF(cx - 23f, cy - 29f, cx + 20f, cy + 27f),
                        5f, 5f, strokePaint);
                canvas.drawLine(cx + 21f, cy - 8f, cx + 29f, cy - 8f, strokePaint);
                canvas.drawLine(cx + 29f, cy - 8f, cx + 29f, cy + 8f, strokePaint);
                path.moveTo(cx + 1f, cy - 20f);
                path.lineTo(cx - 11f, cy + 1f);
                path.lineTo(cx, cy + 1f);
                path.lineTo(cx - 7f, cy + 19f);
                path.lineTo(cx + 15f, cy - 7f);
                path.lineTo(cx + 4f, cy - 7f);
                path.close();
                canvas.drawPath(path, strokePaint);
                break;
            case 5: // phone
                path.moveTo(cx - 24f, cy - 25f);
                path.quadTo(cx - 34f, cy - 9f, cx - 21f, cy + 12f);
                path.quadTo(cx - 7f, cy + 33f, cx + 17f, cy + 29f);
                path.lineTo(cx + 29f, cy + 19f);
                path.lineTo(cx + 16f, cy + 6f);
                path.lineTo(cx + 6f, cy + 13f);
                path.quadTo(cx - 6f, cy + 5f, cx - 13f, cy - 7f);
                path.lineTo(cx - 6f, cy - 17f);
                path.close();
                canvas.drawPath(path, strokePaint);
                break;
            case 6: // nav
                path.moveTo(cx, cy - 31f);
                path.lineTo(cx + 27f, cy + 30f);
                path.lineTo(cx, cy + 14f);
                path.lineTo(cx - 27f, cy + 30f);
                path.close();
                canvas.drawPath(path, strokePaint);
                break;
            case 7: // apps
                for (int r = -1; r <= 1; r += 2) {
                    for (int c = -1; c <= 1; c += 2) {
                        float x = cx + c * 15f;
                        float y = cy + r * 15f;
                        canvas.drawRoundRect(new RectF(x - 9f, y - 9f, x + 9f, y + 9f),
                                3f, 3f, strokePaint);
                    }
                }
                break;
            default: // settings
                canvas.drawCircle(cx, cy, 12f, strokePaint);
                canvas.drawCircle(cx, cy, 27f, strokePaint);
                for (int k = 0; k < 8; k++) {
                    double a = Math.PI / 4 * k;
                    float x1 = cx + (float)Math.cos(a) * 27f;
                    float y1 = cy + (float)Math.sin(a) * 27f;
                    float x2 = cx + (float)Math.cos(a) * 35f;
                    float y2 = cy + (float)Math.sin(a) * 35f;
                    canvas.drawLine(x1, y1, x2, y2, strokePaint);
                }
                break;
        }
    }

    private void drawText(
            Canvas canvas, String text, float x, float y, float size,
            int color, Paint.Align align, boolean bold) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setTextSize(size);
        paint.setTextAlign(align);
        paint.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        canvas.drawText(text, x, y, paint);
    }

    private int displayEvMiles() {
        if (state == null) return 42;
        if (finite(state.liveElectricRangeMiles) && state.liveElectricRangeMiles >= 0f) {
            return Math.round(state.liveElectricRangeMiles);
        }
        return Math.max(0, state.electricRangeMiles);
    }

    private int displayFuelMiles() {
        if (state == null) return 310;
        if (finite(state.liveFuelRangeMiles) && state.liveFuelRangeMiles >= 0f) {
            return Math.round(state.liveFuelRangeMiles);
        }
        return Math.max(0, state.fuelRangeMiles);
    }

    private int displayTotalMiles() {
        if (state == null) return 352;
        if (finite(state.liveTotalRangeMiles) && state.liveTotalRangeMiles >= 0f) {
            return Math.round(state.liveTotalRangeMiles);
        }
        if (state.totalRangeMiles >= 0) return state.totalRangeMiles;
        return displayEvMiles() + displayFuelMiles();
    }

    private String display12Voltage() {
        if (state != null && finite(state.system12VoltageV) && state.system12VoltageV > 0f) {
            return String.format(Locale.US, "%.1f V", state.system12VoltageV);
        }
        return "--.- V";
    }

    private float evFraction() {
        if (state != null && state.batteryPercent >= 0 && state.batteryPercent <= 100) {
            return state.batteryPercent / 100f;
        }
        return clamp(displayEvMiles() / 55f, 0f, 1f);
    }

    private float fuelFraction() {
        return clamp(displayFuelMiles() / 400f, 0f, 1f);
    }

    private float voltageFraction() {
        if (state == null || !finite(state.system12VoltageV)) return 0f;
        return clamp((state.system12VoltageV - 10.5f) / 4.5f, 0f, 1f);
    }

    private boolean finite(float value) {
        return !Float.isNaN(value) && !Float.isInfinite(value);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) {
            return true;
        }

        float x = event.getX() * DESIGN_W / Math.max(1f, getWidth());
        float y = event.getY() * DESIGN_H / Math.max(1f, getHeight());

        if (y >= 590f) {
            int index = Math.min(8, Math.max(0, (int)(x / (DESIGN_W / 9f))));
            if (sectionListener != null) {
                sectionListener.onSectionSelected(sections[index]);
            }
            performClick();
            return true;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
}
