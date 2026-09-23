package com.promethean.core.hmi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public final class PhoneView extends View {
    private static final int COLOR_BG = 0xFF070D10;
    private static final int COLOR_PANEL = 0xFF10181D;
    private static final int COLOR_STROKE = 0xFF27414A;
    private static final int COLOR_TEXT = 0xFFF2F7F8;
    private static final int COLOR_MUTED = 0xFF93A7AF;
    private static final int COLOR_CYAN = 0xFF4ED9F5;
    private static final int COLOR_GREEN = 0xFF69E7A7;
    private static final int COLOR_RED = 0xFFFF6B6B;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final String[] TABS = {
            "FAVORITES", "RECENTS", "CONTACTS", "MESSAGES", "KEYPAD"
    };

    private final RectF[] tabButtons = {
            new RectF(), new RectF(), new RectF(), new RectF(), new RectF()
    };

    private final RectF pairButton = new RectF();
    private final RectF answerButton = new RectF();
    private final RectF endButton = new RectF();

    private int selectedTab = 0;

    public PhoneView(Context context) {
        super(context);
        init();
    }

    public PhoneView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PhoneView(Context context, AttributeSet attrs, int defStyleAttr) {
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

        float margin = dp(20);
        float gap = dp(14);

        RectF connectionCard = new RectF(
                margin, margin,
                w * 0.43f, h - margin);

        RectF contentCard = new RectF(
                connectionCard.right + gap, margin,
                w - margin, h - margin);

        drawConnectionCard(canvas, connectionCard);
        drawContentCard(canvas, contentCard);
    }

    private void drawConnectionCard(Canvas canvas, RectF rect) {
        drawPanel(canvas, rect, COLOR_STROKE);

        drawCenteredText(canvas, "PHONE",
                rect.centerX(), rect.top + dp(42),
                dp(13), COLOR_MUTED, true);

        float iconY = rect.top + rect.height() * 0.30f;
        drawPhoneIcon(canvas, rect.centerX(), iconY);

        drawCenteredText(canvas, "NO PHONE CONNECTED",
                rect.centerX(), rect.centerY() + dp(24),
                dp(23), COLOR_TEXT, true);

        drawCenteredText(canvas,
                "Bluetooth / Telecom integration pending",
                rect.centerX(), rect.centerY() + dp(54),
                dp(11), COLOR_MUTED, false);

        pairButton.set(
                rect.left + dp(26),
                rect.bottom - dp(78),
                rect.right - dp(26),
                rect.bottom - dp(24));

        drawPanel(canvas, pairButton, COLOR_CYAN);
        drawCenteredText(canvas, "PAIR / MANAGE PHONE",
                pairButton.centerX(),
                pairButton.centerY() + dp(6),
                dp(14), COLOR_CYAN, true);
    }

    private void drawContentCard(Canvas canvas, RectF rect) {
        drawPanel(canvas, rect, COLOR_STROKE);

        float pad = dp(16);
        float tabGap = dp(8);
        float tabHeight = dp(52);
        float tabWidth =
                (rect.width() - pad * 2f - tabGap * 4f) / 5f;

        for (int i = 0; i < TABS.length; i++) {
            float left = rect.left + pad + i * (tabWidth + tabGap);
            tabButtons[i].set(
                    left, rect.top + pad,
                    left + tabWidth, rect.top + pad + tabHeight);

            boolean selected = i == selectedTab;
            drawPanel(canvas, tabButtons[i],
                    selected ? COLOR_CYAN : COLOR_STROKE);
            drawCenteredText(canvas, TABS[i],
                    tabButtons[i].centerX(),
                    tabButtons[i].centerY() + dp(5),
                    dp(11),
                    selected ? COLOR_CYAN : COLOR_TEXT,
                    true);
        }

        RectF body = new RectF(
                rect.left + pad,
                rect.top + pad + tabHeight + dp(12),
                rect.right - pad,
                rect.bottom - pad);

        drawTabBody(canvas, body);
    }

    private void drawTabBody(Canvas canvas, RectF rect) {
        switch (selectedTab) {
            case 1:
                drawUnavailableList(canvas, rect,
                        "RECENT CALLS",
                        "Call history will appear when a phone is connected.");
                break;
            case 2:
                drawUnavailableList(canvas, rect,
                        "CONTACTS",
                        "Phone contacts will appear after Bluetooth sync.");
                break;
            case 3:
                drawUnavailableList(canvas, rect,
                        "MESSAGES",
                        "Texts will appear after phone messaging sync.");
                break;
            case 4:
                drawKeypad(canvas, rect);
                break;
            default:
                drawFavorites(canvas, rect);
                break;
        }
    }

    private void drawFavorites(Canvas canvas, RectF rect) {
        drawText(canvas, "FAVORITES",
                rect.left + dp(6), rect.top + dp(22),
                dp(11), COLOR_MUTED, true);

        drawCenteredText(canvas, "NO FAVORITES AVAILABLE",
                rect.centerX(), rect.centerY() - dp(12),
                dp(22), COLOR_TEXT, true);

        drawCenteredText(canvas,
                "Connect a phone to sync favorites.",
                rect.centerX(), rect.centerY() + dp(20),
                dp(12), COLOR_MUTED, false);

        drawCallControls(canvas, rect);
    }

    private void drawUnavailableList(Canvas canvas,
                                     RectF rect,
                                     String title,
                                     String message) {
        drawText(canvas, title,
                rect.left + dp(6), rect.top + dp(22),
                dp(11), COLOR_MUTED, true);

        drawCenteredText(canvas, "NOT AVAILABLE",
                rect.centerX(), rect.centerY() - dp(12),
                dp(22), COLOR_TEXT, true);

        drawCenteredText(canvas, message,
                rect.centerX(), rect.centerY() + dp(20),
                dp(12), COLOR_MUTED, false);

        drawCallControls(canvas, rect);
    }

    private void drawKeypad(Canvas canvas, RectF rect) {
        drawText(canvas, "DIAL PAD",
                rect.left + dp(6), rect.top + dp(22),
                dp(11), COLOR_MUTED, true);

        float areaTop = rect.top + dp(34);
        float keypadWidth = rect.width() * 0.62f;
        float buttonGap = dp(8);
        float buttonWidth = (keypadWidth - buttonGap * 2f) / 3f;
        float buttonHeight =
                (rect.height() - dp(72) - buttonGap * 3f) / 4f;

        String[] labels = {
                "1", "2", "3",
                "4", "5", "6",
                "7", "8", "9",
                "*", "0", "#"
        };

        for (int i = 0; i < labels.length; i++) {
            int row = i / 3;
            int col = i % 3;

            RectF key = new RectF(
                    rect.left + col * (buttonWidth + buttonGap),
                    areaTop + row * (buttonHeight + buttonGap),
                    rect.left + col * (buttonWidth + buttonGap) + buttonWidth,
                    areaTop + row * (buttonHeight + buttonGap) + buttonHeight);

            drawPanel(canvas, key, COLOR_STROKE);
            drawCenteredText(canvas, labels[i],
                    key.centerX(), key.centerY() + dp(8),
                    dp(22), COLOR_TEXT, true);
        }

        RectF status = new RectF(
                rect.left + keypadWidth + dp(16),
                areaTop,
                rect.right,
                rect.bottom);

        drawCenteredText(canvas, "PHONE",
                status.centerX(), status.top + dp(36),
                dp(11), COLOR_MUTED, true);

        drawCenteredText(canvas, "NOT CONNECTED",
                status.centerX(), status.centerY() - dp(8),
                dp(16), COLOR_TEXT, true);

        drawCenteredText(canvas,
                "Dialing disabled until\nTelecom integration",
                status.centerX(), status.centerY() + dp(22),
                dp(10), COLOR_MUTED, false);

        drawCallControls(canvas, status);
    }

    private void drawCallControls(Canvas canvas, RectF rect) {
        float width = Math.min(dp(120), rect.width() * 0.28f);
        float height = dp(48);
        float gap = dp(14);
        float center = rect.centerX();

        answerButton.set(
                center - width - gap / 2f,
                rect.bottom - height - dp(8),
                center - gap / 2f,
                rect.bottom - dp(8));

        endButton.set(
                center + gap / 2f,
                rect.bottom - height - dp(8),
                center + width + gap / 2f,
                rect.bottom - dp(8));

        drawPanel(canvas, answerButton, COLOR_GREEN);
        drawCenteredText(canvas, "ANSWER",
                answerButton.centerX(),
                answerButton.centerY() + dp(6),
                dp(12), COLOR_GREEN, true);

        drawPanel(canvas, endButton, COLOR_RED);
        drawCenteredText(canvas, "END",
                endButton.centerX(),
                endButton.centerY() + dp(6),
                dp(12), COLOR_RED, true);
    }

    private void drawPhoneIcon(Canvas canvas, float cx, float cy) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(7));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(COLOR_CYAN);

        RectF handset = new RectF(
                cx - dp(34), cy - dp(30),
                cx + dp(34), cy + dp(30));

        canvas.drawArc(handset, 130f, 100f, false, paint);
        canvas.drawLine(
                cx - dp(25), cy + dp(20),
                cx - dp(42), cy + dp(34),
                paint);
        canvas.drawLine(
                cx + dp(25), cy + dp(20),
                cx + dp(42), cy + dp(34),
                paint);
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

        String[] lines = text.split("\\n");
        if (lines.length == 1) {
            canvas.drawText(text, x, baseline, paint);
        } else {
            float lineGap = size * 1.25f;
            float y = baseline - (lines.length - 1) * lineGap / 2f;
            for (String line : lines) {
                canvas.drawText(line, x, y, paint);
                y += lineGap;
            }
        }

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

        for (int i = 0; i < tabButtons.length; i++) {
            if (tabButtons[i].contains(x, y)) {
                selectedTab = i;
                invalidate();
                performClick();
                return true;
            }
        }

        if (pairButton.contains(x, y)
                || answerButton.contains(x, y)
                || endButton.contains(x, y)) {
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

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
