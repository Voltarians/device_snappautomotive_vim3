package com.promethean.core.hmi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Locale;

public final class HomeDashboardView extends View {
    public interface OnSectionSelectedListener {
        void onSectionSelected(String section);
    }

    private static final int BG_TOP = Color.rgb(2, 10, 19);
    private static final int BG_BOTTOM = Color.rgb(0, 2, 7);
    private static final int PANEL = Color.argb(230, 4, 17, 28);
    private static final int PANEL_EDGE = Color.rgb(0, 202, 255);
    private static final int CYAN = Color.rgb(54, 210, 255);
    private static final int BLUE = Color.rgb(22, 102, 255);
    private static final int GREEN = Color.rgb(49, 242, 102);
    private static final int AMBER = Color.rgb(255, 174, 54);
    private static final int TEXT = Color.rgb(242, 248, 255);
    private static final int MUTED = Color.rgb(147, 184, 215);

    private static final String[] NAV_LABELS = {
            "Home", "Audio", "Climate", "Vehicle", "Energy",
            "Phone", "Navigation", "Apps", "Settings"
    };
    private static final String[] NAV_SECTIONS = {
            "HOME", "RADIO", "CLIMATE", "VEHICLE", "ENERGY",
            "PHONE", "NAV", "APPS", "SETTINGS"
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Path carBody = new Path();

    private VehicleState state;
    private OnSectionSelectedListener listener;
    private int vehiclePaintColor = Color.rgb(178, 186, 194);

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
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(dp(1.3f));
        stroke.setColor(PANEL_EDGE);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void setOnSectionSelectedListener(OnSectionSelectedListener listener) {
        this.listener = listener;
    }

    public void setVehicleState(VehicleState state) {
        this.state = state;
        invalidate();
    }

    public void setVehiclePaintColor(int color) {
        this.vehiclePaintColor = color;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        final float w = getWidth();
        final float h = getHeight();
        if (w <= 0 || h <= 0) return;

        paint.setShader(new LinearGradient(0, 0, 0, h, BG_TOP, BG_BOTTOM, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, paint);
        paint.setShader(null);

        final boolean compact = getResources().getConfiguration().smallestScreenWidthDp < 720;
        final float pad = compact ? dp(9) : dp(14);
        final float topH = compact ? h * 0.105f : h * 0.095f;
        final float navH = compact ? h * 0.165f : h * 0.155f;
        final float contentTop = topH + pad * 0.35f;
        final float contentBottom = h - navH - pad * 0.35f;
        final float contentH = contentBottom - contentTop;

        drawHeader(c, w, topH, compact);

        final float sideW = compact ? w * 0.285f : w * 0.275f;
        final float gap = compact ? dp(7) : dp(11);

        RectF leftTop = new RectF(pad, contentTop, pad + sideW, contentTop + contentH * 0.58f);
        RectF leftBottom = new RectF(pad, leftTop.bottom + gap, pad + sideW, contentBottom);
        RectF rightTop = new RectF(w - pad - sideW, contentTop, w - pad, contentTop + contentH * 0.58f);
        RectF rightBottom = new RectF(w - pad - sideW, rightTop.bottom + gap, w - pad, contentBottom);
        RectF center = new RectF(leftTop.right + gap, contentTop, rightTop.left - gap, contentBottom);

        drawEnergyPanel(c, leftTop, compact);
        drawTwelveVoltPanel(c, leftBottom, compact);
        drawCenterGauge(c, center, compact);
        drawClimatePanel(c, rightTop, compact);
        drawVehicleStatusPanel(c, rightBottom, compact);
        drawBottomNav(c, w, h, navH, compact);
    }

    private void drawHeader(Canvas c, float w, float h, boolean compact) {
        paint.setColor(Color.rgb(1, 9, 16));
        c.drawRect(0, 0, w, h, paint);

        paint.setColor(BLUE);
        paint.setShadowLayer(dp(9), 0, 0, BLUE);
        c.drawRect(0, h - dp(2), w, h, paint);
        paint.clearShadowLayer();

        float titleSize = compact ? sp(18) : sp(24);
        drawText(c, "PROMETHEAN", dp(22), h * 0.57f, titleSize, TEXT, Paint.Align.LEFT, true);
        drawText(c, "CORE", compact ? dp(190) : dp(245), h * 0.57f, titleSize, CYAN, Paint.Align.LEFT, true);

        String time = new java.text.SimpleDateFormat("h:mm a", Locale.getDefault())
                .format(new java.util.Date());
        drawText(c, time, w * 0.64f, h * 0.57f, compact ? sp(14) : sp(18), TEXT, Paint.Align.CENTER, false);
        drawText(c, "Wi-Fi   BT   72°F", w - dp(24), h * 0.57f, compact ? sp(12) : sp(15), TEXT, Paint.Align.RIGHT, false);
    }

    private void drawEnergyPanel(Canvas c, RectF r, boolean compact) {
        drawPanel(c, r, "BATTERY / ENERGY", compact);
        float x = r.left + r.width() * 0.08f;
        float y = r.top + r.height() * 0.30f;
        float pct = state != null && state.hvSocPct > 0 ? state.hvSocPct : (state != null ? state.batteryPercent : 68);
        float range = state != null && state.liveElectricRangeMiles > 0
                ? state.liveElectricRangeMiles
                : (state != null ? state.electricRangeMiles : 24);
        drawText(c, String.format(Locale.US, "%.0f%%", pct), x, y, compact ? sp(30) : sp(38), TEXT, Paint.Align.LEFT, true);
        drawText(c, "EV Range", x, y + (compact ? dp(25) : dp(32)), compact ? sp(12) : sp(15), CYAN, Paint.Align.LEFT, false);
        drawText(c, String.format(Locale.US, "%.0f mi", range), x, y + (compact ? dp(46) : dp(58)), compact ? sp(18) : sp(23), TEXT, Paint.Align.LEFT, true);

        float barLeft = x;
        float barRight = r.right - r.width() * 0.08f;
        float barY = r.bottom - r.height() * 0.25f;
        drawSegmentBar(c, barLeft, barY, barRight - barLeft, compact ? dp(7) : dp(9), pct / 100f, CYAN, GREEN);

        float powerKw = state != null ? Math.abs(state.apmPowerW) / 1000f : 0f;
        drawText(c, String.format(Locale.US, "%.1f kW", powerKw), barLeft, r.bottom - dp(13), compact ? sp(13) : sp(16), TEXT, Paint.Align.LEFT, true);
        drawText(c, "Power", barLeft, r.bottom - dp(1), compact ? sp(9) : sp(11), MUTED, Paint.Align.LEFT, false);
        drawText(c, state != null && state.vehicleOn ? "READY" : "PARKED", barRight, r.bottom - dp(7), compact ? sp(11) : sp(13), GREEN, Paint.Align.RIGHT, true);
    }

    private void drawTwelveVoltPanel(Canvas c, RectF r, boolean compact) {
        drawPanel(c, r, "12V SYSTEM", compact);
        float v = state != null && state.system12VoltageV > 0 ? state.system12VoltageV : 12.6f;
        String status = state != null && state.pcgConnected ? "PCG-1 LIVE" : "STATUS";
        drawText(c, String.format(Locale.US, "%.1f V", v), r.left + r.width() * 0.12f, r.centerY(), compact ? sp(20) : sp(25), TEXT, Paint.Align.LEFT, true);
        drawText(c, status, r.left + r.width() * 0.12f, r.centerY() + dp(20), compact ? sp(10) : sp(12), v >= 12.0f ? GREEN : AMBER, Paint.Align.LEFT, true);

        String temp = state != null && state.hvTempMaxC > -100
                ? String.format(Locale.US, "%.0f°F", state.hvTempMaxC * 9f / 5f + 32f)
                : "--°F";
        drawText(c, temp, r.right - r.width() * 0.12f, r.centerY(), compact ? sp(18) : sp(22), TEXT, Paint.Align.RIGHT, true);
        drawText(c, "APM / HV TEMP", r.right - r.width() * 0.12f, r.centerY() + dp(20), compact ? sp(9) : sp(11), MUTED, Paint.Align.RIGHT, false);
    }

    private void drawCenterGauge(Canvas c, RectF r, boolean compact) {
        float cx = r.centerX();
        float cy = r.top + r.height() * 0.44f;
        float radius = Math.min(r.width() * 0.47f, r.height() * 0.46f);

        stroke.setStrokeWidth(compact ? dp(5) : dp(7));
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setColor(Color.rgb(21, 55, 88));
        c.drawArc(new RectF(cx - radius, cy - radius, cx + radius, cy + radius), 140, 260, false, stroke);
        stroke.setColor(CYAN);
        stroke.setShadowLayer(dp(9), 0, 0, BLUE);
        c.drawArc(new RectF(cx - radius, cy - radius, cx + radius, cy + radius), 140, 110, false, stroke);
        stroke.clearShadowLayer();
        stroke.setColor(AMBER);
        c.drawArc(new RectF(cx - radius, cy - radius, cx + radius, cy + radius), 310, 35, false, stroke);

        float speed = state != null ? state.liveVehicleSpeedMph : 35f;
        String gear = state != null && state.liveShiftPosition != null ? state.liveShiftPosition : "P";
        drawText(c, "MPH", cx, cy - radius * 0.48f, compact ? sp(11) : sp(14), TEXT, Paint.Align.CENTER, false);
        drawText(c, String.format(Locale.US, "%.0f", speed), cx, cy - radius * 0.07f, compact ? sp(38) : sp(52), TEXT, Paint.Align.CENTER, true);
        drawText(c, gear, cx, cy + radius * 0.22f, compact ? sp(21) : sp(28), BLUE, Paint.Align.CENTER, true);

        drawCar(c, new RectF(cx - radius * 0.70f, cy + radius * 0.12f, cx + radius * 0.70f, cy + radius * 0.83f));
        drawText(c, "CHEVROLET", cx, r.bottom - (compact ? dp(34) : dp(42)), compact ? sp(8) : sp(10), TEXT, Paint.Align.CENTER, true);
        drawText(c, "VOLT", cx, r.bottom - (compact ? dp(13) : dp(16)), compact ? sp(20) : sp(26), TEXT, Paint.Align.CENTER, true);

        drawText(c, "EV", cx - radius * 0.72f, cy - radius * 0.72f, compact ? sp(9) : sp(11), TEXT, Paint.Align.CENTER, true);
        drawText(c, "POWER", cx + radius * 0.76f, cy - radius * 0.72f, compact ? sp(9) : sp(11), TEXT, Paint.Align.CENTER, true);
        drawText(c, "CHARGE", cx - radius * 0.82f, cy + radius * 0.62f, compact ? sp(8) : sp(10), TEXT, Paint.Align.CENTER, true);
        drawText(c, "REGEN", cx + radius * 0.82f, cy + radius * 0.62f, compact ? sp(8) : sp(10), TEXT, Paint.Align.CENTER, true);
    }

    private void drawCar(Canvas c, RectF r) {
        float x = r.left, y = r.top, w = r.width(), h = r.height();
        carBody.reset();
        carBody.moveTo(x + w * 0.08f, y + h * 0.66f);
        carBody.lineTo(x + w * 0.20f, y + h * 0.42f);
        carBody.lineTo(x + w * 0.39f, y + h * 0.24f);
        carBody.lineTo(x + w * 0.68f, y + h * 0.25f);
        carBody.lineTo(x + w * 0.84f, y + h * 0.44f);
        carBody.lineTo(x + w * 0.94f, y + h * 0.65f);
        carBody.lineTo(x + w * 0.88f, y + h * 0.78f);
        carBody.lineTo(x + w * 0.14f, y + h * 0.78f);
        carBody.close();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(vehiclePaintColor);
        paint.setShadowLayer(dp(10), 0, dp(5), Color.argb(180, 0, 120, 255));
        c.drawPath(carBody, paint);
        paint.clearShadowLayer();

        paint.setColor(Color.rgb(13, 27, 40));
        Path glass = new Path();
        glass.moveTo(x + w * 0.30f, y + h * 0.43f);
        glass.lineTo(x + w * 0.42f, y + h * 0.28f);
        glass.lineTo(x + w * 0.66f, y + h * 0.29f);
        glass.lineTo(x + w * 0.78f, y + h * 0.44f);
        glass.close();
        c.drawPath(glass, paint);

        paint.setColor(Color.rgb(12, 15, 19));
        c.drawCircle(x + w * 0.27f, y + h * 0.76f, h * 0.16f, paint);
        c.drawCircle(x + w * 0.76f, y + h * 0.76f, h * 0.16f, paint);
        paint.setColor(Color.rgb(180, 198, 214));
        c.drawCircle(x + w * 0.27f, y + h * 0.76f, h * 0.08f, paint);
        c.drawCircle(x + w * 0.76f, y + h * 0.76f, h * 0.08f, paint);

        paint.setColor(CYAN);
        c.drawRect(x + w * 0.10f, y + h * 0.58f, x + w * 0.22f, y + h * 0.63f, paint);
    }

    private void drawClimatePanel(Canvas c, RectF r, boolean compact) {
        drawPanel(c, r, "CLIMATE", compact);
        float tempC = state != null ? state.cabinTemperatureC : Float.NaN;
        String temp = !Float.isNaN(tempC) && tempC > -50
                ? String.format(Locale.US, "%.0f°F", tempC * 9f / 5f + 32f)
                : "72°F";
        drawText(c, temp, r.left + r.width() * 0.08f, r.top + r.height() * 0.34f, compact ? sp(28) : sp(36), TEXT, Paint.Align.LEFT, true);
        drawText(c, state != null && state.climateAcActive ? "A/C ACTIVE" : "AUTO", r.left + r.width() * 0.08f, r.top + r.height() * 0.47f, compact ? sp(11) : sp(14), CYAN, Paint.Align.LEFT, true);

        float blower = state != null ? state.climateBlowerPct / 100f : 0.55f;
        drawSegmentBar(c, r.left + r.width() * 0.08f, r.bottom - r.height() * 0.25f,
                r.width() * 0.84f, compact ? dp(7) : dp(9), blower, CYAN, CYAN);
        drawText(c, "FRONT     REAR      A/C      AUTO", r.centerX(), r.bottom - dp(10),
                compact ? sp(8) : sp(10), TEXT, Paint.Align.CENTER, false);
    }

    private void drawVehicleStatusPanel(Canvas c, RectF r, boolean compact) {
        drawPanel(c, r, "VEHICLE STATUS", compact);
        String bus = state != null && state.physicalVehicleBusHealth != null
                ? state.physicalVehicleBusHealth : "SYSTEMS NORMAL";
        drawText(c, bus, r.centerX(), r.centerY() - dp(3), compact ? sp(12) : sp(15),
                state != null && state.pcgConnected ? GREEN : CYAN, Paint.Align.CENTER, true);
        String detail = state != null
                ? String.format(Locale.US, "%d vehicle buses • %s",
                    state.physicalVehicleBusesWithTraffic,
                    state.vehicleOn ? "READY" : "PARKED")
                : "4 tires • gateway ready";
        drawText(c, detail, r.centerX(), r.centerY() + dp(21), compact ? sp(9) : sp(11), MUTED, Paint.Align.CENTER, false);
    }

    private void drawBottomNav(Canvas c, float w, float h, float navH, boolean compact) {
        float top = h - navH;
        paint.setColor(Color.rgb(1, 7, 13));
        c.drawRect(0, top, w, h, paint);
        paint.setColor(BLUE);
        c.drawRect(0, top, w, top + dp(1.5f), paint);

        float gap = compact ? dp(4) : dp(6);
        float outer = compact ? dp(8) : dp(12);
        float bw = (w - outer * 2 - gap * (NAV_LABELS.length - 1)) / NAV_LABELS.length;
        for (int i = 0; i < NAV_LABELS.length; i++) {
            float l = outer + i * (bw + gap);
            RectF b = new RectF(l, top + gap, l + bw, h - gap);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(i == 0 ? Color.rgb(5, 40, 92) : Color.rgb(3, 14, 24));
            c.drawRoundRect(b, dp(7), dp(7), paint);
            stroke.setColor(i == 0 ? Color.rgb(40, 130, 255) : Color.rgb(17, 80, 112));
            stroke.setStrokeWidth(dp(1));
            c.drawRoundRect(b, dp(7), dp(7), stroke);
            if (i == 0) {
                stroke.setShadowLayer(dp(8), 0, 0, BLUE);
                c.drawRoundRect(b, dp(7), dp(7), stroke);
                stroke.clearShadowLayer();
            }

            drawNavGlyph(c, i, b.centerX(), b.top + b.height() * 0.38f, compact);
            drawText(c, NAV_LABELS[i], b.centerX(), b.bottom - (compact ? dp(10) : dp(12)),
                    compact ? sp(7.5f) : sp(9.5f), TEXT, Paint.Align.CENTER, false);
        }
    }

    private void drawNavGlyph(Canvas c, int index, float cx, float cy, boolean compact) {
        float s = compact ? dp(9) : dp(12);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(compact ? dp(1.5f) : dp(2));
        paint.setColor(index == 0 ? CYAN : TEXT);
        if (index == 0) {
            Path p = new Path();
            p.moveTo(cx - s, cy);
            p.lineTo(cx, cy - s);
            p.lineTo(cx + s, cy);
            p.lineTo(cx + s * 0.7f, cy);
            p.lineTo(cx + s * 0.7f, cy + s);
            p.lineTo(cx - s * 0.7f, cy + s);
            p.lineTo(cx - s * 0.7f, cy);
            p.close();
            c.drawPath(p, paint);
        } else if (index == 1) {
            c.drawCircle(cx - s * 0.35f, cy + s * 0.35f, s * 0.35f, paint);
            c.drawLine(cx, cy + s * 0.2f, cx, cy - s, paint);
            c.drawLine(cx, cy - s, cx + s * 0.8f, cy - s * 0.75f, paint);
        } else if (index == 2) {
            c.drawCircle(cx, cy, s * 0.28f, paint);
            for (int k = 0; k < 6; k++) {
                double a = k * Math.PI / 3.0;
                c.drawLine(cx, cy, cx + (float)Math.cos(a) * s, cy + (float)Math.sin(a) * s, paint);
            }
        } else if (index == 3) {
            c.drawRoundRect(new RectF(cx - s, cy - s * 0.45f, cx + s, cy + s * 0.55f), s * 0.2f, s * 0.2f, paint);
            c.drawCircle(cx - s * 0.55f, cy + s * 0.62f, s * 0.18f, paint);
            c.drawCircle(cx + s * 0.55f, cy + s * 0.62f, s * 0.18f, paint);
        } else if (index == 4) {
            for (int k = -1; k <= 1; k++) c.drawLine(cx + k * s * 0.55f, cy + s, cx + k * s * 0.55f, cy - s * (k + 2) / 3f, paint);
        } else if (index == 5) {
            c.drawArc(new RectF(cx - s, cy - s, cx + s, cy + s), 35, 110, false, paint);
        } else if (index == 6) {
            Path p = new Path();
            p.moveTo(cx, cy - s);
            p.lineTo(cx + s * 0.75f, cy + s);
            p.lineTo(cx, cy + s * 0.55f);
            p.lineTo(cx - s * 0.75f, cy + s);
            p.close();
            c.drawPath(p, paint);
        } else if (index == 7) {
            float d = s * 0.7f;
            for (int yy = -1; yy <= 1; yy += 2)
                for (int xx = -1; xx <= 1; xx += 2)
                    c.drawRect(cx + xx * d - s * 0.22f, cy + yy * d - s * 0.22f,
                            cx + xx * d + s * 0.22f, cy + yy * d + s * 0.22f, paint);
        } else {
            c.drawCircle(cx, cy, s * 0.55f, paint);
            c.drawCircle(cx, cy, s * 0.18f, paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawPanel(Canvas c, RectF r, String title, boolean compact) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(PANEL);
        c.drawRoundRect(r, dp(10), dp(10), paint);
        stroke.setColor(Color.rgb(13, 111, 150));
        stroke.setStrokeWidth(dp(1));
        c.drawRoundRect(r, dp(10), dp(10), stroke);
        drawText(c, title, r.left + r.width() * 0.06f, r.top + (compact ? dp(20) : dp(25)),
                compact ? sp(10.5f) : sp(13), Color.rgb(153, 217, 255), Paint.Align.LEFT, true);
    }

    private void drawSegmentBar(Canvas c, float x, float y, float width, float height,
                                float fraction, int startColor, int endColor) {
        int segments = 16;
        float gap = Math.max(dp(1), width * 0.008f);
        float sw = (width - gap * (segments - 1)) / segments;
        int active = Math.max(0, Math.min(segments, Math.round(fraction * segments)));
        for (int i = 0; i < segments; i++) {
            float t = i / (float)Math.max(1, segments - 1);
            int color = blend(startColor, endColor, t);
            paint.setColor(i < active ? color : Color.rgb(25, 47, 69));
            c.drawRect(x + i * (sw + gap), y, x + i * (sw + gap) + sw, y + height, paint);
        }
    }

    private static int blend(int a, int b, float t) {
        int ar = Color.red(a), ag = Color.green(a), ab = Color.blue(a);
        int br = Color.red(b), bg = Color.green(b), bb = Color.blue(b);
        return Color.rgb(
                Math.round(ar + (br - ar) * t),
                Math.round(ag + (bg - ag) * t),
                Math.round(ab + (bb - ab) * t));
    }

    private void drawText(Canvas c, String text, float x, float y, float size, int color,
                          Paint.Align align, boolean bold) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setTextSize(size);
        paint.setTextAlign(align);
        paint.setTypeface(bold ? android.graphics.Typeface.DEFAULT_BOLD : android.graphics.Typeface.DEFAULT);
        c.drawText(text, x, y, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) return true;
        float h = getHeight();
        boolean compact = getResources().getConfiguration().smallestScreenWidthDp < 720;
        float navH = compact ? h * 0.165f : h * 0.155f;
        if (event.getY() < h - navH) return true;

        float w = getWidth();
        float gap = compact ? dp(4) : dp(6);
        float outer = compact ? dp(8) : dp(12);
        float bw = (w - outer * 2 - gap * (NAV_LABELS.length - 1)) / NAV_LABELS.length;
        for (int i = 0; i < NAV_LABELS.length; i++) {
            float l = outer + i * (bw + gap);
            if (event.getX() >= l && event.getX() <= l + bw) {
                if (listener != null) listener.onSectionSelected(NAV_SECTIONS[i]);
                performClick();
                break;
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

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
