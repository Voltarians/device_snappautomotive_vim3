package com.promethean.core.hmi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public final class AppsView extends View {
    private static final int COLOR_BG = 0xFF070D10;
    private static final int COLOR_PANEL = 0xFF10181D;
    private static final int COLOR_STROKE = 0xFF27414A;
    private static final int COLOR_TEXT = 0xFFF2F7F8;
    private static final int COLOR_MUTED = 0xFF93A7AF;
    private static final int COLOR_CYAN = 0xFF4ED9F5;
    private static final int COLOR_GREEN = 0xFF69E7A7;
    private static final int COLOR_AMBER = 0xFFF1B84B;

    private static final String[] TITLES = {
            "VOLTARIAN",
            "OBD ATLAS",
            "PROMETHEAN\nVISION",
            "MEDIA",
            "FILES",
            "ALL APPS"
    };

    private static final String[] SUBTITLES = {
            "Vehicle companion",
            "Diagnostics + research",
            "Perception system",
            "Music + local media",
            "Local + network files",
            "AAOS applications"
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF[] tiles = {
            new RectF(), new RectF(), new RectF(),
            new RectF(), new RectF(), new RectF()
    };

    private int selected = -1;

    public AppsView(Context context) {
        super(context);
        init();
    }

    public AppsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AppsView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        float headerHeight = dp(58);

        drawText(canvas, "PROMETHEAN APPLICATIONS",
                margin, margin + dp(18),
                dp(11), COLOR_MUTED, true);

        drawText(canvas,
                selected >= 0
                        ? TITLES[selected].replace("\n", " ") + " • launch integration pending"
                        : "Select an application",
                margin, margin + dp(45),
                dp(15), selected >= 0 ? COLOR_CYAN : COLOR_TEXT, true);

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

            drawAppTile(canvas, tiles[i], i, i == selected);
        }
    }

    private void drawAppTile(Canvas canvas,
                             RectF rect,
                             int index,
                             boolean selectedTile) {
        int accent = selectedTile ? COLOR_CYAN : COLOR_STROKE;
        drawPanel(canvas, rect, accent);

        float cx = rect.centerX();
        float iconY = rect.top + rect.height() * 0.34f;

        drawAppGlyph(canvas, cx, iconY, index,
                selectedTile ? COLOR_CYAN : COLOR_TEXT);

        drawCenteredMultiline(canvas,
                TITLES[index],
                cx,
                rect.centerY() + dp(31),
                dp(18),
                selectedTile ? COLOR_CYAN : COLOR_TEXT,
                true);

        drawCenteredText(canvas,
                SUBTITLES[index],
                cx,
                rect.bottom - dp(28),
                dp(11),
                COLOR_MUTED,
                false);

        drawCenteredText(canvas,
                "INTEGRATION PENDING",
                cx,
                rect.bottom - dp(10),
                dp(9),
                index < 3 ? COLOR_GREEN : COLOR_AMBER,
                true);
    }

    private void drawAppGlyph(Canvas canvas,
                              float cx,
                              float cy,
                              int index,
                              int color) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(4));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(color);

        float r = dp(26);

        switch (index) {
            case 0:
                canvas.drawCircle(cx, cy, r, paint);
                canvas.drawLine(cx - dp(13), cy, cx + dp(13), cy, paint);
                canvas.drawLine(cx, cy - dp(13), cx, cy + dp(13), paint);
                break;
            case 1:
                canvas.drawRect(
                        cx - r, cy - r,
                        cx + r, cy + r, paint);
                canvas.drawLine(cx - dp(14), cy - dp(8),
                        cx + dp(14), cy - dp(8), paint);
                canvas.drawLine(cx - dp(14), cy + dp(8),
                        cx + dp(14), cy + dp(8), paint);
                break;
            case 2:
                canvas.drawCircle(cx, cy, r, paint);
                canvas.drawCircle(cx, cy, dp(8), paint);
                canvas.drawLine(cx - r - dp(10), cy,
                        cx - r, cy, paint);
                canvas.drawLine(cx + r, cy,
                        cx + r + dp(10), cy, paint);
                break;
            case 3:
                canvas.drawCircle(cx, cy, r, paint);
                canvas.drawLine(cx - dp(8), cy - dp(10),
                        cx - dp(8), cy + dp(10), paint);
                canvas.drawLine(cx - dp(8), cy - dp(10),
                        cx + dp(12), cy - dp(16), paint);
                canvas.drawLine(cx + dp(12), cy - dp(16),
                        cx + dp(12), cy + dp(6), paint);
                break;
            case 4:
                canvas.drawRoundRect(
                        new RectF(cx - r, cy - dp(18),
                                cx + r, cy + dp(18)),
                        dp(5), dp(5), paint);
                canvas.drawLine(cx - dp(18), cy - dp(18),
                        cx - dp(6), cy - dp(30), paint);
                canvas.drawLine(cx - dp(6), cy - dp(30),
                        cx + dp(5), cy - dp(30), paint);
                break;
            default:
                float s = dp(12);
                for (int row = -1; row <= 1; row++) {
                    for (int col = -1; col <= 1; col++) {
                        RectF box = new RectF(
                                cx + col * dp(22) - s / 2f,
                                cy + row * dp(22) - s / 2f,
                                cx + col * dp(22) + s / 2f,
                                cy + row * dp(22) + s / 2f);
                        canvas.drawRect(box, paint);
                    }
                }
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

    private void drawCenteredMultiline(Canvas canvas,
                                       String text,
                                       float x,
                                       float baseline,
                                       float size,
                                       int color,
                                       boolean bold) {
        String[] lines = text.split("\\n");
        float gap = size * 1.12f;
        float firstBaseline =
                baseline - (lines.length - 1) * gap / 2f;

        for (int i = 0; i < lines.length; i++) {
            drawCenteredText(canvas,
                    lines[i],
                    x,
                    firstBaseline + i * gap,
                    size,
                    color,
                    bold);
        }
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
