package com.promethean.core.hmi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public final class SettingsView extends View {
    private static final int COLOR_BG = 0xFF070D10;
    private static final int COLOR_PANEL = 0xFF10181D;
    private static final int COLOR_STROKE = 0xFF27414A;
    private static final int COLOR_TEXT = 0xFFF2F7F8;
    private static final int COLOR_MUTED = 0xFF93A7AF;
    private static final int COLOR_CYAN = 0xFF4ED9F5;
    private static final int COLOR_GREEN = 0xFF69E7A7;
    private static final int COLOR_AMBER = 0xFFF1B84B;

    private static final String[] TITLES = {
            "DISPLAY",
            "AUDIO / DSP",
            "CONNECTIVITY",
            "VEHICLE",
            "SYSTEM",
            "DEVELOPER"
    };

    private static final String[] SUBTITLES = {
            "Brightness • theme • units",
            "EQ • balance • fade • outputs",
            "Wi-Fi • Bluetooth • Ethernet",
            "Promethean vehicle integration",
            "Updates • storage • information",
            "ADB • logging • diagnostics"
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF[] tiles = {
            new RectF(), new RectF(), new RectF(),
            new RectF(), new RectF(), new RectF()
    };

    private int selected = -1;

    public SettingsView(Context context) {
        super(context);
        init();
    }

    public SettingsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SettingsView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        float gap = dp(14);
        float headerHeight = dp(66);

        drawText(canvas, "PROMETHEAN CORE SETTINGS",
                margin, margin + dp(18),
                dp(11), COLOR_MUTED, true);

        drawText(canvas,
                selected >= 0
                        ? TITLES[selected] + " • configuration surface"
                        : "Select a settings category",
                margin, margin + dp(47),
                dp(15),
                selected >= 0 ? COLOR_CYAN : COLOR_TEXT,
                true);

        float gridTop = margin + headerHeight;
        float gridHeight = h - gridTop - margin;
        float tileWidth = (w - margin * 2f - gap * 2f) / 3f;
        float tileHeight = (gridHeight - gap) / 2f;

        for (int i = 0; i < tiles.length; i++) {
            int row = i / 3;
            int col = i % 3;

            float left = margin + col * (tileWidth + gap);
            float top = gridTop + row * (tileHeight + gap);

            tiles[i].set(
                    left,
                    top,
                    left + tileWidth,
                    top + tileHeight);

            drawSettingsTile(canvas, tiles[i], i, i == selected);
        }
    }

    private void drawSettingsTile(Canvas canvas,
                                  RectF rect,
                                  int index,
                                  boolean selectedTile) {
        int accent = selectedTile ? COLOR_CYAN : COLOR_STROKE;
        drawPanel(canvas, rect, accent);

        float cx = rect.centerX();
        float cy = rect.top + rect.height() * 0.34f;

        drawGlyph(canvas, cx, cy, index,
                selectedTile ? COLOR_CYAN : COLOR_TEXT);

        drawCenteredText(canvas,
                TITLES[index],
                cx,
                rect.centerY() + dp(29),
                dp(18),
                selectedTile ? COLOR_CYAN : COLOR_TEXT,
                true);

        drawCenteredText(canvas,
                SUBTITLES[index],
                cx,
                rect.bottom - dp(26),
                dp(10),
                COLOR_MUTED,
                false);

        drawCenteredText(canvas,
                statusText(index),
                cx,
                rect.bottom - dp(9),
                dp(9),
                statusColor(index),
                true);
    }

    private String statusText(int index) {
        switch (index) {
            case 0:
            case 1:
                return "UI FOUNDATION";
            case 2:
                return "AAOS SERVICES";
            case 3:
                return "INTEGRATION PENDING";
            case 4:
                return "SYSTEM FOUNDATION";
            default:
                return "DEVELOPMENT";
        }
    }

    private int statusColor(int index) {
        switch (index) {
            case 0:
            case 1:
            case 2:
            case 4:
                return COLOR_GREEN;
            case 3:
                return COLOR_AMBER;
            default:
                return COLOR_CYAN;
        }
    }

    private void drawGlyph(Canvas canvas,
                           float cx,
                           float cy,
                           int index,
                           int color) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(4));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(color);

        float r = dp(25);

        switch (index) {
            case 0:
                RectF screen = new RectF(
                        cx - r, cy - dp(18),
                        cx + r, cy + dp(16));
                canvas.drawRoundRect(screen, dp(5), dp(5), paint);
                canvas.drawLine(cx, cy + dp(16), cx, cy + dp(29), paint);
                canvas.drawLine(cx - dp(12), cy + dp(29),
                        cx + dp(12), cy + dp(29), paint);
                break;
            case 1:
                canvas.drawCircle(cx, cy, r, paint);
                canvas.drawLine(cx - dp(20), cy,
                        cx + dp(20), cy, paint);
                canvas.drawCircle(cx - dp(10), cy, dp(5), paint);
                canvas.drawCircle(cx + dp(11), cy, dp(5), paint);
                break;
            case 2:
                canvas.drawCircle(cx - dp(11), cy, dp(8), paint);
                canvas.drawCircle(cx + dp(11), cy, dp(8), paint);
                canvas.drawLine(cx - dp(3), cy,
                        cx + dp(3), cy, paint);
                canvas.drawArc(
                        new RectF(cx - dp(32), cy - dp(31),
                                cx + dp(32), cy + dp(31)),
                        205f, 130f, false, paint);
                break;
            case 3:
                RectF car = new RectF(
                        cx - dp(27), cy - dp(13),
                        cx + dp(27), cy + dp(16));
                canvas.drawRoundRect(car, dp(8), dp(8), paint);
                canvas.drawCircle(cx - dp(17), cy + dp(18), dp(6), paint);
                canvas.drawCircle(cx + dp(17), cy + dp(18), dp(6), paint);
                break;
            case 4:
                canvas.drawCircle(cx, cy, r, paint);
                canvas.drawCircle(cx, cy, dp(8), paint);
                canvas.drawLine(cx, cy - r - dp(8),
                        cx, cy - r + dp(2), paint);
                canvas.drawLine(cx, cy + r - dp(2),
                        cx, cy + r + dp(8), paint);
                canvas.drawLine(cx - r - dp(8), cy,
                        cx - r + dp(2), cy, paint);
                canvas.drawLine(cx + r - dp(2), cy,
                        cx + r + dp(8), cy, paint);
                break;
            default:
                canvas.drawRect(
                        cx - dp(24), cy - dp(19),
                        cx + dp(24), cy + dp(19), paint);
                canvas.drawLine(cx - dp(13), cy - dp(8),
                        cx + dp(13), cy - dp(8), paint);
                canvas.drawLine(cx - dp(13), cy + dp(1),
                        cx + dp(4), cy + dp(1), paint);
                canvas.drawLine(cx - dp(13), cy + dp(10),
                        cx + dp(9), cy + dp(10), paint);
                break;
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

        for (int i = 0; i < tiles.length; i++) {
            if (tiles[i].contains(x, y)) {
                selected = i;
                invalidate();
                performClick();
                return true;
            }
        }

        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
