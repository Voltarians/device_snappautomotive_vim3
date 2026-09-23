package com.promethean.core.hmi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public final class NavigationView extends View {
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

    private final RectF searchButton = new RectF();
    private final RectF recentButton = new RectF();
    private final RectF favoriteButton = new RectF();
    private final RectF gasButton = new RectF();
    private final RectF foodButton = new RectF();
    private final RectF chargingButton = new RectF();

    private String selectedNearby = "";

    public NavigationView(Context context) {
        super(context);
        init();
    }

    public NavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
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

        float margin = dp(18);
        float gap = dp(12);
        float rightWidth = Math.min(dp(270), w * 0.31f);

        RectF map = new RectF(
                margin,
                margin,
                w - rightWidth - gap,
                h - margin);

        RectF panel = new RectF(
                map.right + gap,
                margin,
                w - margin,
                h - margin);

        drawMap(canvas, map);
        drawSidePanel(canvas, panel);
    }

    private void drawMap(Canvas canvas, RectF rect) {
        drawPanel(canvas, rect, COLOR_STROKE);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(COLOR_STROKE);

        for (int i = 1; i < 5; i++) {
            float x = rect.left + rect.width() * i / 5f;
            canvas.drawLine(x, rect.top, x, rect.bottom, paint);
        }
        for (int i = 1; i < 5; i++) {
            float y = rect.top + rect.height() * i / 5f;
            canvas.drawLine(rect.left, y, rect.right, y, paint);
        }

        paint.setStrokeWidth(dp(5));
        paint.setColor(COLOR_CYAN);
        path.reset();
        path.moveTo(rect.left + rect.width() * 0.14f,
                rect.bottom - rect.height() * 0.16f);
        path.cubicTo(
                rect.left + rect.width() * 0.28f,
                rect.bottom - rect.height() * 0.35f,
                rect.left + rect.width() * 0.50f,
                rect.top + rect.height() * 0.60f,
                rect.left + rect.width() * 0.66f,
                rect.top + rect.height() * 0.42f);
        path.cubicTo(
                rect.left + rect.width() * 0.78f,
                rect.top + rect.height() * 0.30f,
                rect.left + rect.width() * 0.82f,
                rect.top + rect.height() * 0.18f,
                rect.left + rect.width() * 0.90f,
                rect.top + rect.height() * 0.15f);
        canvas.drawPath(path, paint);

        float carX = rect.left + rect.width() * 0.50f;
        float carY = rect.top + rect.height() * 0.60f;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOR_GREEN);
        canvas.drawCircle(carX, carY, dp(9), paint);

        drawText(canvas, "MAP",
                rect.left + dp(18),
                rect.top + dp(28),
                dp(11), COLOR_MUTED, true);

        drawText(canvas, "Map provider / GPS integration pending",
                rect.left + dp(18),
                rect.bottom - dp(18),
                dp(10), COLOR_MUTED, false);

        if (!selectedNearby.isEmpty()) {
            drawCenteredText(canvas,
                    "NEARBY: " + selectedNearby,
                    rect.centerX(),
                    rect.top + dp(44),
                    dp(16), COLOR_CYAN, true);
        }
    }

    private void drawSidePanel(Canvas canvas, RectF rect) {
        drawPanel(canvas, rect, COLOR_STROKE);

        float pad = dp(14);
        float buttonGap = dp(9);
        float top = rect.top + pad;

        searchButton.set(
                rect.left + pad, top,
                rect.right - pad, top + dp(54));
        drawAction(canvas, searchButton, "DESTINATION SEARCH", COLOR_CYAN);

        top += dp(54) + buttonGap;

        float half = (rect.width() - pad * 2f - buttonGap) / 2f;
        recentButton.set(
                rect.left + pad, top,
                rect.left + pad + half, top + dp(52));
        favoriteButton.set(
                recentButton.right + buttonGap, top,
                rect.right - pad, top + dp(52));

        drawAction(canvas, recentButton, "RECENTS", COLOR_STROKE);
        drawAction(canvas, favoriteButton, "FAVORITES", COLOR_STROKE);

        top += dp(52) + dp(20);

        drawText(canvas, "NEARBY",
                rect.left + pad, top,
                dp(11), COLOR_MUTED, true);

        top += dp(12);

        float categoryHeight = dp(62);
        gasButton.set(
                rect.left + pad, top + dp(10),
                rect.right - pad, top + dp(10) + categoryHeight);
        foodButton.set(
                rect.left + pad,
                gasButton.bottom + buttonGap,
                rect.right - pad,
                gasButton.bottom + buttonGap + categoryHeight);
        chargingButton.set(
                rect.left + pad,
                foodButton.bottom + buttonGap,
                rect.right - pad,
                foodButton.bottom + buttonGap + categoryHeight);

        drawNearby(canvas, gasButton, "GAS STATIONS", "Fuel nearby",
                "GAS STATIONS".equals(selectedNearby), COLOR_AMBER);
        drawNearby(canvas, foodButton, "FOOD", "Restaurants nearby",
                "FOOD".equals(selectedNearby), COLOR_GREEN);
        drawNearby(canvas, chargingButton, "CHARGING", "EV charging nearby",
                "CHARGING".equals(selectedNearby), COLOR_CYAN);

        float guidanceTop = chargingButton.bottom + dp(20);
        if (guidanceTop < rect.bottom - dp(86)) {
            drawText(canvas, "ROUTE",
                    rect.left + pad, guidanceTop,
                    dp(11), COLOR_MUTED, true);

            drawText(canvas, "No active route",
                    rect.left + pad, guidanceTop + dp(30),
                    dp(18), COLOR_TEXT, true);

            drawText(canvas, "ETA --   •   Distance --",
                    rect.left + pad, guidanceTop + dp(54),
                    dp(11), COLOR_MUTED, false);
        }
    }

    private void drawNearby(Canvas canvas,
                            RectF rect,
                            String title,
                            String detail,
                            boolean selected,
                            int accent) {
        drawPanel(canvas, rect, selected ? accent : COLOR_STROKE);

        drawText(canvas, title,
                rect.left + dp(16),
                rect.top + dp(26),
                dp(14), selected ? accent : COLOR_TEXT, true);

        drawText(canvas, detail,
                rect.left + dp(16),
                rect.bottom - dp(13),
                dp(10), COLOR_MUTED, false);
    }

    private void drawAction(Canvas canvas,
                            RectF rect,
                            String label,
                            int accent) {
        drawPanel(canvas, rect, accent);
        drawCenteredText(canvas, label,
                rect.centerX(), rect.centerY() + dp(5),
                dp(12), accent == COLOR_STROKE ? COLOR_TEXT : accent, true);
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

        if (gasButton.contains(x, y)) {
            selectedNearby = "GAS STATIONS";
        } else if (foodButton.contains(x, y)) {
            selectedNearby = "FOOD";
        } else if (chargingButton.contains(x, y)) {
            selectedNearby = "CHARGING";
        } else if (searchButton.contains(x, y)
                || recentButton.contains(x, y)
                || favoriteButton.contains(x, y)) {
            performClick();
            return true;
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

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
