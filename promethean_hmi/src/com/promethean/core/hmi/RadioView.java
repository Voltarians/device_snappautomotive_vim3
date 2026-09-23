package com.promethean.core.hmi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Locale;

public final class RadioView extends View {
    private static final int COLOR_BG = 0xFF070D10;
    private static final int COLOR_PANEL = 0xFF10181D;
    private static final int COLOR_STROKE = 0xFF27414A;
    private static final int COLOR_TEXT = 0xFFF2F7F8;
    private static final int COLOR_MUTED = 0xFF93A7AF;
    private static final int COLOR_CYAN = 0xFF4ED9F5;
    private static final int COLOR_GREEN = 0xFF69E7A7;

    private static final String[] SOURCES = {"FM", "AM", "XM", "BT"};

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int sourceIndex = 2;
    private float fmFrequency = 101.1f;
    private int amFrequency = 1010;
    private int xmChannel = 2;
    private int volume = 18;
    private int balance = 0;
    private int fade = 0;
    private int tuneKnobMode = 0; // 0=tune, 1=balance, 2=fade

    private final RectF tuneMinus = new RectF();
    private final RectF tunePlus = new RectF();
    private final RectF volumeMinus = new RectF();
    private final RectF volumePlus = new RectF();
    private final RectF soundModeButton = new RectF();
    private final RectF soundPad = new RectF();
    private final RectF[] sourceButtons = {
            new RectF(), new RectF(), new RectF(), new RectF()
    };
    private final RectF[] presetButtons = {
            new RectF(), new RectF(), new RectF(),
            new RectF(), new RectF(), new RectF()
    };

    public RadioView(Context context) {
        super(context);
        init();
    }

    public RadioView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RadioView(Context context, AttributeSet attrs, int defStyleAttr) {
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

    public void applyVolumeRotaryDelta(int detents) {
        if (detents == 0) {
            return;
        }
        volume = clamp(volume + detents, 0, 40);
        invalidate();
    }

    public void applyTuneRotaryDelta(int detents) {
        if (detents == 0) {
            return;
        }
        if (tuneKnobMode == 1) {
            balance = clamp(balance + detents, -10, 10);
        } else if (tuneKnobMode == 2) {
            fade = clamp(fade + detents, -10, 10);
        } else {
            tune(detents);
        }
        invalidate();
    }

    public void cycleTuneRotaryMode() {
        tuneKnobMode = (tuneKnobMode + 1) % 3;
        invalidate();
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
        float topHeight = h * 0.48f;
        float nowPlayingWidth = w * 0.62f;

        RectF nowPlaying = new RectF(
                margin, margin,
                nowPlayingWidth, topHeight);

        RectF volumeCard = new RectF(
                nowPlayingWidth + gap, margin,
                w - margin, topHeight);

        drawNowPlaying(canvas, nowPlaying);
        drawVolumeCard(canvas, volumeCard);

        float sourceTop = topHeight + gap;
        float sourceHeight = dp(62);
        drawSources(canvas, margin, sourceTop, w - margin * 2f, sourceHeight);

        float bottomTop = sourceTop + sourceHeight + gap;
        float soundWidth = w * 0.34f;

        RectF presetArea = new RectF(
                margin, bottomTop,
                w - soundWidth - gap, h - margin);

        RectF soundArea = new RectF(
                w - soundWidth, bottomTop,
                w - margin, h - margin);

        drawPresets(canvas, presetArea);
        drawSound(canvas, soundArea);
    }

    private void drawNowPlaying(Canvas canvas, RectF rect) {
        drawPanel(canvas, rect, COLOR_STROKE);

        drawText(canvas, "NOW PLAYING",
                rect.left + dp(22), rect.top + dp(31),
                dp(11), COLOR_MUTED, true);

        drawText(canvas, SOURCES[sourceIndex],
                rect.left + dp(22), rect.top + dp(72),
                dp(22), COLOR_CYAN, true);

        drawText(canvas, stationText(),
                rect.left + dp(22), rect.centerY() + dp(8),
                dp(42), COLOR_TEXT, true);

        drawText(canvas, stationDetail(),
                rect.left + dp(22), rect.centerY() + dp(40),
                dp(14), COLOR_MUTED, false);

        float buttonW = dp(84);
        float buttonH = dp(50);
        tuneMinus.set(
                rect.right - buttonW * 2f - dp(32),
                rect.bottom - buttonH - dp(18),
                rect.right - buttonW - dp(24),
                rect.bottom - dp(18));
        tunePlus.set(
                rect.right - buttonW - dp(16),
                rect.bottom - buttonH - dp(18),
                rect.right - dp(8),
                rect.bottom - dp(18));

        drawStepButton(canvas, tuneMinus, "−");
        drawStepButton(canvas, tunePlus, "+");

        drawText(canvas, tuneModeLabel(),
                rect.left + dp(22), rect.bottom - dp(31),
                dp(11), COLOR_GREEN, true);
    }

    private void drawVolumeCard(Canvas canvas, RectF rect) {
        drawPanel(canvas, rect, COLOR_STROKE);

        float cx = rect.centerX();
        float cy = rect.top + rect.height() * 0.43f;
        float radius = Math.min(rect.width(), rect.height()) * 0.25f;

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(dp(8));
        paint.setColor(COLOR_STROKE);
        RectF ring = new RectF(
                cx - radius, cy - radius,
                cx + radius, cy + radius);
        canvas.drawArc(ring, 135f, 270f, false, paint);

        paint.setColor(COLOR_CYAN);
        canvas.drawArc(ring, 135f, 270f * (volume / 40f), false, paint);

        drawCenteredText(canvas, "VOLUME",
                cx, rect.top + dp(32),
                dp(11), COLOR_MUTED, true);
        drawCenteredText(canvas, Integer.toString(volume),
                cx, cy + dp(14),
                dp(38), COLOR_TEXT, true);

        float buttonW = dp(70);
        float buttonH = dp(46);
        volumeMinus.set(
                cx - buttonW - dp(8),
                rect.bottom - buttonH - dp(16),
                cx - dp(8),
                rect.bottom - dp(16));
        volumePlus.set(
                cx + dp(8),
                rect.bottom - buttonH - dp(16),
                cx + buttonW + dp(8),
                rect.bottom - dp(16));

        drawStepButton(canvas, volumeMinus, "−");
        drawStepButton(canvas, volumePlus, "+");
    }

    private void drawSources(Canvas canvas,
                             float left, float top,
                             float width, float height) {
        float gap = dp(10);
        float buttonW = (width - gap * 3f) / 4f;

        for (int i = 0; i < SOURCES.length; i++) {
            float x = left + i * (buttonW + gap);
            sourceButtons[i].set(x, top, x + buttonW, top + height);

            boolean selected = i == sourceIndex;
            drawPanel(canvas, sourceButtons[i],
                    selected ? COLOR_CYAN : COLOR_STROKE);
            drawCenteredText(canvas, SOURCES[i],
                    sourceButtons[i].centerX(),
                    sourceButtons[i].centerY() + dp(6),
                    dp(16),
                    selected ? COLOR_CYAN : COLOR_TEXT,
                    true);
        }
    }

    private void drawPresets(Canvas canvas, RectF rect) {
        drawPanel(canvas, rect, COLOR_STROKE);

        drawText(canvas, "PRESETS",
                rect.left + dp(18), rect.top + dp(26),
                dp(11), COLOR_MUTED, true);

        float pad = dp(18);
        float gap = dp(8);
        float top = rect.top + dp(38);
        float h = rect.height() - dp(52);
        float buttonW = (rect.width() - pad * 2f - gap * 2f) / 3f;
        float buttonH = (h - gap) / 2f;

        for (int i = 0; i < 6; i++) {
            int row = i / 3;
            int col = i % 3;
            float x = rect.left + pad + col * (buttonW + gap);
            float y = top + row * (buttonH + gap);

            presetButtons[i].set(x, y, x + buttonW, y + buttonH);
            drawPanel(canvas, presetButtons[i], COLOR_STROKE);

            drawCenteredText(canvas,
                    Integer.toString(i + 1),
                    presetButtons[i].centerX(),
                    presetButtons[i].centerY() + dp(7),
                    dp(19), COLOR_TEXT, true);
        }
    }

    private void drawSound(Canvas canvas, RectF rect) {
        drawPanel(canvas, rect, COLOR_STROKE);

        drawText(canvas, "SOUND",
                rect.left + dp(18), rect.top + dp(26),
                dp(11), COLOR_MUTED, true);

        soundModeButton.set(
                rect.right - dp(118), rect.top + dp(12),
                rect.right - dp(12), rect.top + dp(44));
        drawPanel(canvas, soundModeButton, COLOR_CYAN);
        drawCenteredText(canvas, tuneModeShort(),
                soundModeButton.centerX(),
                soundModeButton.centerY() + dp(5),
                dp(11), COLOR_CYAN, true);

        float pad = dp(26);
        soundPad.set(
                rect.left + pad,
                rect.top + dp(58),
                rect.right - pad,
                rect.bottom - dp(18));

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(COLOR_STROKE);
        canvas.drawRoundRect(soundPad, dp(12), dp(12), paint);
        canvas.drawLine(soundPad.centerX(), soundPad.top,
                soundPad.centerX(), soundPad.bottom, paint);
        canvas.drawLine(soundPad.left, soundPad.centerY(),
                soundPad.right, soundPad.centerY(), paint);

        float x = soundPad.centerX()
                + (balance / 10f) * soundPad.width() * 0.42f;
        float y = soundPad.centerY()
                + (fade / 10f) * soundPad.height() * 0.42f;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOR_CYAN);
        canvas.drawCircle(x, y, dp(9), paint);

        drawCenteredText(canvas, "FRONT",
                soundPad.centerX(), soundPad.top + dp(15),
                dp(9), COLOR_MUTED, true);
        drawCenteredText(canvas, "REAR",
                soundPad.centerX(), soundPad.bottom - dp(6),
                dp(9), COLOR_MUTED, true);

        drawText(canvas, "L",
                soundPad.left + dp(7), soundPad.centerY() + dp(4),
                dp(10), COLOR_MUTED, true);
        drawText(canvas, "R",
                soundPad.right - dp(15), soundPad.centerY() + dp(4),
                dp(10), COLOR_MUTED, true);
    }

    private String stationText() {
        switch (sourceIndex) {
            case 0:
                return String.format(Locale.US, "%.1f", fmFrequency);
            case 1:
                return Integer.toString(amFrequency);
            case 2:
                return "CH " + xmChannel;
            default:
                return "PHONE";
        }
    }

    private String stationDetail() {
        switch (sourceIndex) {
            case 0:
                return "FM Radio";
            case 1:
                return "AM Radio";
            case 2:
                return "SiriusXM Preview";
            default:
                return "Bluetooth Audio";
        }
    }

    private String tuneModeLabel() {
        if (tuneKnobMode == 1) {
            return "TUNE KNOB: BALANCE " + signed(balance);
        }
        if (tuneKnobMode == 2) {
            return "TUNE KNOB: FADE " + signed(fade);
        }
        return "TUNE KNOB: TUNING";
    }

    private String tuneModeShort() {
        if (tuneKnobMode == 1) {
            return "BALANCE";
        }
        if (tuneKnobMode == 2) {
            return "FADE";
        }
        return "TUNE";
    }

    private String signed(int value) {
        if (value > 0) {
            return "+" + value;
        }
        return Integer.toString(value);
    }

    private void tune(int detents) {
        switch (sourceIndex) {
            case 0:
                fmFrequency += detents * 0.2f;
                while (fmFrequency > 107.9f) {
                    fmFrequency -= 20.0f;
                }
                while (fmFrequency < 87.9f) {
                    fmFrequency += 20.0f;
                }
                fmFrequency = Math.round(fmFrequency * 10f) / 10f;
                break;
            case 1:
                amFrequency += detents * 10;
                if (amFrequency > 1710) {
                    amFrequency = 530;
                }
                if (amFrequency < 530) {
                    amFrequency = 1710;
                }
                break;
            case 2:
                xmChannel = clamp(xmChannel + detents, 1, 999);
                break;
            default:
                break;
        }
    }

    private void drawStepButton(Canvas canvas, RectF rect, String label) {
        drawPanel(canvas, rect, COLOR_STROKE);
        drawCenteredText(canvas, label,
                rect.centerX(), rect.centerY() + dp(11),
                dp(30), COLOR_CYAN, true);
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
                                  float x, float baseline,
                                  float size, int color,
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
                          float x, float baseline,
                          float size, int color,
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

        if (tuneMinus.contains(x, y)) {
            applyTuneRotaryDelta(-1);
        } else if (tunePlus.contains(x, y)) {
            applyTuneRotaryDelta(1);
        } else if (volumeMinus.contains(x, y)) {
            applyVolumeRotaryDelta(-1);
        } else if (volumePlus.contains(x, y)) {
            applyVolumeRotaryDelta(1);
        } else if (soundModeButton.contains(x, y)) {
            cycleTuneRotaryMode();
        } else if (soundPad.contains(x, y)) {
            balance = clamp(Math.round(
                    ((x - soundPad.centerX()) / (soundPad.width() * 0.42f)) * 10f),
                    -10, 10);
            fade = clamp(Math.round(
                    ((y - soundPad.centerY()) / (soundPad.height() * 0.42f)) * 10f),
                    -10, 10);
            invalidate();
        } else {
            for (int i = 0; i < sourceButtons.length; i++) {
                if (sourceButtons[i].contains(x, y)) {
                    sourceIndex = i;
                    tuneKnobMode = 0;
                    invalidate();
                    performClick();
                    return true;
                }
            }

            for (int i = 0; i < presetButtons.length; i++) {
                if (presetButtons[i].contains(x, y)) {
                    loadPreset(i);
                    invalidate();
                    performClick();
                    return true;
                }
            }
        }

        performClick();
        return true;
    }

    private void loadPreset(int index) {
        if (sourceIndex == 0) {
            fmFrequency = 88.1f + index * 3.1f;
        } else if (sourceIndex == 1) {
            amFrequency = 600 + index * 180;
        } else if (sourceIndex == 2) {
            xmChannel = 2 + index * 8;
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
