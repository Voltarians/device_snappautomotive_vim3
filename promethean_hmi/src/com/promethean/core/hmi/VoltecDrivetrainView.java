package com.promethean.core.hmi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.MotionEvent;

public final class VoltecDrivetrainView extends View {

    private static final float SUN_TEETH = 37f;
    private static final float RING_TEETH = 83f;
    private static final float PLANET_TEETH = 23f;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint clutchPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF clutchRect = new RectF();

    private final DifferentialBody differentialBody = new DifferentialBody();

    private long lastFrameNs;

    private float sunAngle;
    private float ringAngle;
    private float carrierAngle;
    private float planetSpinAngle;

    // Temporary demonstration state. These will be replaced by PCG-1 state.
    private float sunRpm = 2400f;
    private float ringRpm = 0f;

    private boolean c1Applied = true;
    private boolean c2Applied = false;
    private boolean c3Applied = false;

    private boolean engineDriving = false;
    private boolean engineGenerating = false;
    private boolean tractionControl = false;

    // Traction control is available throughout the drivetrain display.
    private boolean tractionControlEnabled = true;

    // Independent front-wheel brake / TC intervention states.
    private boolean upperWheelBraked = false;
    private boolean lowerWheelBraked = false;

    // Demonstration operating modes.
    private static final int MODE_EV_DRIVE = 0;
    private static final int MODE_REGEN = 1;
    private static final int MODE_ENGINE_GENERATION = 2;
    private static final int MODE_BLENDED_DRIVE = 3;
    private static final int MODE_NEUTRAL = 4;

    private int demoMode = MODE_EV_DRIVE;
    private String demoModeName = "EV DRIVE";

    public VoltecDrivetrainView(Context context) {
        super(context);
        init();
    }

    public VoltecDrivetrainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VoltecDrivetrainView(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
        init();
    }

    private void init() {
        paint.setTypeface(android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD));

        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(dp(3));
        stroke.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            demoMode = (demoMode + 1) % 5;
            applyDemoMode();
            invalidate();
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

    private void applyDemoMode() {
        switch (demoMode) {
            case MODE_REGEN:
                demoModeName = "REGEN";
                sunRpm = -1800f;
                ringRpm = 0f;
                c1Applied = true;
                c2Applied = false;
                c3Applied = false;
                engineDriving = false;
                engineGenerating = false;
                tractionControl = false;
                break;

            case MODE_ENGINE_GENERATION:
                demoModeName = "ENGINE GENERATION";
                sunRpm = 1800f;
                ringRpm = 1200f;
                c1Applied = true;
                c2Applied = false;
                c3Applied = true;
                engineDriving = false;
                engineGenerating = true;
                tractionControl = false;
                break;

            case MODE_BLENDED_DRIVE:
                demoModeName = "BLENDED DRIVE";
                sunRpm = 2600f;
                ringRpm = 1600f;
                c1Applied = false;
                c2Applied = true;
                c3Applied = true;
                engineDriving = true;
                engineGenerating = false;
                tractionControl = false;
                break;

            case MODE_NEUTRAL:
                demoModeName = "NEUTRAL";
                sunRpm = 0f;
                ringRpm = 0f;
                c1Applied = false;
                c2Applied = false;
                c3Applied = false;
                engineDriving = false;
                engineGenerating = false;
                tractionControl = false;
                break;

            case MODE_EV_DRIVE:
            default:
                demoModeName = "EV DRIVE";
                sunRpm = 2400f;
                ringRpm = 0f;
                c1Applied = true;
                c2Applied = false;
                c3Applied = false;
                engineDriving = false;
                engineGenerating = false;
                tractionControl = false;
                break;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        lastFrameNs = System.nanoTime();
        postInvalidateOnAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long now = System.nanoTime();
        float dt = lastFrameNs == 0
                ? 0f
                : (now - lastFrameNs) / 1_000_000_000f;
        lastFrameNs = now;

        if (dt > 0.05f) dt = 0.05f;

        updateAnimation(dt);
        drawDrivetrain(canvas);

        postInvalidateOnAnimation();
    }

    private void updateAnimation(float dt) {
        float carrierRpm =
                (RING_TEETH * ringRpm + SUN_TEETH * sunRpm)
                        / (RING_TEETH + SUN_TEETH);

        float planetSpinRpm =
                -(sunRpm - carrierRpm) * SUN_TEETH / PLANET_TEETH;

        sunAngle = wrap(sunAngle + rpmToDegrees(sunRpm, dt));
        ringAngle = wrap(ringAngle + rpmToDegrees(ringRpm, dt));
        carrierAngle = wrap(carrierAngle + rpmToDegrees(carrierRpm, dt));
        planetSpinAngle =
                wrap(planetSpinAngle + rpmToDegrees(planetSpinRpm, dt));

    }

    private void drawDrivetrain(Canvas c) {
        float w = getWidth();
        float h = getHeight();

        if (w <= 0 || h <= 0) return;

        int active = Color.rgb(57, 220, 120);
        int cyan = Color.rgb(38, 205, 230);
        int red = Color.rgb(245, 75, 75);
        int white = Color.rgb(235, 242, 247);
        int muted = Color.rgb(120, 135, 145);
        int dark = Color.rgb(25, 35, 42);

        // Operating-mode power-flow colors.
        boolean evMode = demoMode == MODE_EV_DRIVE;
        int electricDriveColor = evMode ? active : cyan;
        int drivelineColor = evMode ? active : active;

        float cx = w * 0.40f;
        float cy = h * 0.43f;

        // Current carrier speed from planetary kinematics.
        float carrierRpm =
                (RING_TEETH * ringRpm + SUN_TEETH * sunRpm)
                        / (RING_TEETH + SUN_TEETH);

        // MODE / WARNING
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(dp(18));
        paint.setColor(tractionControl ? red : cyan);

        if (tractionControl) {
            c.drawText("TRACTION CONTROL", cx, dp(24), paint);
        } else {
            c.drawText("VOLTEC 4ET50 / MKA", cx, dp(24), paint);
        }

        // Demonstration mode indicator.
        paint.setColor(active);
        paint.setTextSize(dp(14));
        c.drawText(demoModeName, cx, dp(46), paint);

        paint.setColor(muted);
        paint.setTextSize(dp(9));
        c.drawText("TAP DRIVETRAIN TO CHANGE MODE", cx, dp(62), paint);

        // Traction-control status is always visible in this section.
        boolean tcIntervening = upperWheelBraked || lowerWheelBraked;

        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setTextSize(dp(10));
        paint.setColor(tcIntervening ? red : active);

        c.drawText(
                tcIntervening
                        ? "TRACTION CONTROL • ACTIVE"
                        : "TRACTION CONTROL • ON",
                w - dp(24), dp(62), paint);

        paint.setTextAlign(Paint.Align.CENTER);

        // ICE - simplified engine silhouette
        float engineX = w * 0.93f;

        // Align the ICE output shaft exactly with the drivetrain centerline.
        // The engine output is drawn at engineY + 38dp.
        float engineY = cy - dp(38);

        int engineColor = muted;
        if (engineDriving) engineColor = active;
        if (engineGenerating) engineColor = red;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(engineColor);

        RectF engineBlock = new RectF(
                engineX - dp(48), engineY,
                engineX + dp(48), engineY + dp(55));

        c.drawRoundRect(engineBlock, dp(8), dp(8), paint);

        RectF head = new RectF(
                engineX - dp(35), engineY - dp(17),
                engineX + dp(25), engineY + dp(5));

        c.drawRoundRect(head, dp(5), dp(5), paint);

        paint.setColor(white);
        paint.setTextSize(dp(12));
        c.drawText("ICE", engineX, engineY + dp(34), paint);

        // Motor A
        float motorAX = w * 0.67f;
        float motorAY = cy;

        // ENGINE -> C3 -> MOTOR A
        // C3 couples the engine only to Motor A.
        float c3PathY = cy;
        float engineShaftX = engineX - dp(48);
        float motorAShaftX = motorAX + dp(25);
        float c3PathX = (motorAShaftX + engineShaftX) / 2f;

        stroke.setColor(c3Applied
                ? (engineGenerating ? red : active)
                : muted);
        stroke.setStrokeWidth(dp(5));

        c.drawLine(motorAShaftX, c3PathY,
                c3PathX - dp(14), c3PathY, stroke);

        c.drawLine(c3PathX + dp(14), c3PathY,
                engineShaftX, c3PathY, stroke);

        // Coaxial electric machines.
        // Motor B = large outer machine.
        // Motor A = smaller inner machine.
        // Visual design matches approved two-ring core/windings SVG.

        int iron = Color.rgb(86, 97, 106);
        int copper = Color.rgb(217, 130, 43);

        int motorBColor = evMode ? active : cyan;
        int motorAColor = engineGenerating
                ? red
                : (evMode ? active : cyan);

        // --------------------------------------------------------
        // MOTOR B - outer ring
        // --------------------------------------------------------

        stroke.setStyle(Paint.Style.STROKE);

        // Outer housing.
        stroke.setColor(motorBColor);
        stroke.setStrokeWidth(dp(5));
        c.drawCircle(motorAX, motorAY, dp(58), stroke);

        // Iron core.
        stroke.setColor(iron);
        stroke.setStrokeWidth(dp(9));
        c.drawCircle(motorAX, motorAY, dp(49), stroke);

        // Copper windings around Motor B core.
        stroke.setColor(copper);
        stroke.setStrokeWidth(dp(4));
        stroke.setStrokeCap(Paint.Cap.ROUND);

        RectF motorBWindings = new RectF(
                motorAX - dp(49),
                motorAY - dp(49),
                motorAX + dp(49),
                motorAY + dp(49));

        for (int i = 0; i < 12; i++) {
            float startAngle = i * 30f + 4f;
            c.drawArc(motorBWindings,
                    startAngle,
                    22f,
                    false,
                    stroke);
        }

        // Inner boundary of Motor B.
        stroke.setColor(motorBColor);
        stroke.setStrokeWidth(dp(3));
        c.drawCircle(motorAX, motorAY, dp(38), stroke);

        // --------------------------------------------------------
        // MOTOR A - inner ring
        // --------------------------------------------------------

        stroke.setColor(motorAColor);
        stroke.setStrokeWidth(dp(4));
        c.drawCircle(motorAX, motorAY, dp(31), stroke);

        // Motor A iron core.
        stroke.setColor(iron);
        stroke.setStrokeWidth(dp(7));
        c.drawCircle(motorAX, motorAY, dp(25), stroke);

        // Motor A copper windings.
        stroke.setColor(copper);
        stroke.setStrokeWidth(dp(3));

        RectF motorAWindings = new RectF(
                motorAX - dp(25),
                motorAY - dp(25),
                motorAX + dp(25),
                motorAY + dp(25));

        for (int i = 0; i < 8; i++) {
            float startAngle = i * 45f + 5f;
            c.drawArc(motorAWindings,
                    startAngle,
                    32f,
                    false,
                    stroke);
        }

        // Inner Motor A boundary.
        stroke.setColor(motorAColor);
        stroke.setStrokeWidth(dp(2));
        c.drawCircle(motorAX, motorAY, dp(18), stroke);

        // Labels.
        paint.setTextAlign(Paint.Align.CENTER);

        paint.setColor(motorBColor);
        paint.setTextSize(dp(9));
        c.drawText("MOTOR B",
                motorAX,
                motorAY - dp(46),
                paint);

        paint.setColor(motorAColor);
        paint.setTextSize(dp(7));
        c.drawText("MOTOR A",
                motorAX,
                motorAY + dp(3),
                paint);

        // Planetary assembly geometry.
        float ringRadius = Math.min(w, h) * 0.19f;
        float sunRadius = ringRadius * 0.32f;
        float planetRadius = ringRadius * 0.23f;
        float orbitRadius = ringRadius * 0.55f;

        // MOTOR A -> C2 -> RING GEAR
        // This is a separate path from Motor B's permanent sun connection.
        float c2Y = cy - ringRadius - dp(34);
        float c2MotorX = motorAX;
        float c2RingX = cx + ringRadius * 0.68f;
        float c2X = (c2MotorX + c2RingX) / 2f;

        stroke.setColor(c2Applied ? active : muted);
        stroke.setStrokeWidth(dp(5));

        // Motor A rises to the C2 branch.
        c.drawLine(c2MotorX, cy - dp(25),
                c2MotorX, c2Y, stroke);

        // Motor A -> C2.
        c.drawLine(c2MotorX, c2Y,
                c2X + dp(14), c2Y, stroke);

        // C2 -> ring.
        c.drawLine(c2X - dp(14), c2Y,
                c2RingX, c2Y, stroke);

        // Drop into the ring gear.
        c.drawLine(c2RingX, c2Y,
                c2RingX, cy - ringRadius * 0.72f, stroke);

        // C2 uses the same opposing shoe design as C3.
        drawClutch(c, c2X, c2Y,
                "C2", c2Applied,
                engineGenerating ? red : active, muted);

        int ringColor = c1Applied
                ? (engineGenerating ? red : active)
                : (c2Applied ? active : white);

        float visualRingRadius = ringRadius * 0.88f;

        drawC1WagonBrake(
                c,
                cx,
                cy,
                visualRingRadius,
                c1Applied,
                engineGenerating ? red : active,
                muted);
        stroke.setColor(ringColor);

        // 83T internal ring gear - geometry matched to approved SVG.
        c.save();
        c.rotate(ringAngle, cx, cy);
        // Shrink the complete 83T ring proportionally around the same center.

        drawInternalGear(c, cx, cy,
                visualRingRadius,
                visualRingRadius * 0.925f,
                visualRingRadius * 0.855f,
                83,
                ringColor);
        c.restore();

        // 37T external sun gear - geometry matched to approved SVG.
        int sunColor = evMode ? active : cyan;

        c.save();
        c.rotate(sunAngle, cx, cy);
        drawExternalGear(c, cx, cy,
                sunRadius * 0.82f,
                sunRadius,
                sunRadius * 0.56f,
                37,
                sunColor);
        c.restore();

        // Carrier + planets
        // Draw the carrier first so it sits behind the planet gears.
        stroke.setColor(active);
        stroke.setStrokeWidth(dp(8));

        c.save();
        c.rotate(carrierAngle, cx, cy);

        for (int i = 0; i < 3; i++) {
            float orbit = i * 120f;
            double a = Math.toRadians(orbit);

            float px = cx + (float)Math.cos(a) * orbitRadius;
            float py = cy + (float)Math.sin(a) * orbitRadius;

            // Three-arm carrier.
            c.drawLine(cx, cy, px, py, stroke);
        }

        // Carrier hub.
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(active);
        c.drawCircle(cx, cy, dp(7), paint);

        // Draw planets on top of carrier arms.
        stroke.setStrokeWidth(dp(3));

        for (int i = 0; i < 3; i++) {
            float orbit = i * 120f;
            double a = Math.toRadians(orbit);

            float px = cx + (float)Math.cos(a) * orbitRadius;
            float py = cy + (float)Math.sin(a) * orbitRadius;

            c.save();
            c.rotate(planetSpinAngle, px, py);

            // 23T external planet gear - same thin style as approved SVG.
            drawExternalGear(c, px, py,
                    planetRadius * 0.80f,
                    planetRadius,
                    planetRadius * 0.55f,
                    23,
                    active);

            c.restore();
        }

        c.restore();

        paint.setColor(white);
        paint.setTextSize(dp(11));
        c.drawText("37T SUN", cx, cy + dp(5), paint);
        c.drawText("83T RING", cx,
                cy + ringRadius + dp(20), paint);

        // C3 engine coupling clutch
        drawClutch(c, c3PathX, c3PathY,
                "C3", c3Applied,
                engineGenerating ? red : active, muted);

        // Motor B is drawn concentrically inside Motor A.
        // Show its mechanical connection to the planetary sun.
        stroke.setColor(evMode ? active : cyan);
        stroke.setStrokeWidth(dp(5));

        // Nested Motor B -> sun connection.
        c.drawLine(cx + sunRadius, cy,
                motorAX - dp(48), motorAY, stroke);

        // Carrier -> final drive.
        // Differential and front wheels are arranged vertically on the left.
        // Each tire is 76dp wide x 40dp tall, so its long axis is
        // horizontal on the display.

        float diffX = w * 0.47f - ringRadius - dp(165);
        float diffY = cy;

        int outputColor = Math.abs(carrierRpm) > 1f
                ? (evMode ? active : active)
                : muted;

        // Final-drive shaft: planetary carrier -> differential.
        stroke.setColor(outputColor);
        stroke.setStrokeWidth(dp(7));
        c.drawLine(cx - ringRadius, cy,
                diffX + dp(31), diffY, stroke);

        // Approved vertical bevel-gear cutaway, with the ring gear removed.
        differentialBody.draw(c, diffX, diffY, dp(0.40f),
                Math.abs(carrierRpm) > 1f && !upperWheelBraked ? active : red,
                Math.abs(carrierRpm) > 1f && !lowerWheelBraked ? active : red,
                outputColor);

        // Front wheels stacked vertically on the left.
        float wheelX = diffX;
        float upperWheelY = diffY - dp(105);
        float lowerWheelY = diffY + dp(105);

        // Independent front half-shaft state.
        // GREEN = active / transmitting torque
        // RED   = inactive or traction-control braking
        boolean drivetrainMoving = Math.abs(carrierRpm) > 1f;

        int upperShaftColor =
                (drivetrainMoving && !upperWheelBraked) ? active : red;

        int lowerShaftColor =
                (drivetrainMoving && !lowerWheelBraked) ? active : red;

        stroke.setStrokeWidth(dp(7));

        // Upper half-shaft.
        stroke.setColor(upperShaftColor);
        c.drawLine(diffX, diffY - dp(31),
                wheelX, upperWheelY + dp(20), stroke);

        // Lower half-shaft.
        stroke.setColor(lowerShaftColor);
        c.drawLine(diffX, diffY + dp(31),
                wheelX, lowerWheelY - dp(20), stroke);

        // CV-joint markers follow each shaft state.
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(upperShaftColor);
        c.drawCircle(diffX, diffY - dp(42), dp(5), paint);

        paint.setColor(lowerShaftColor);
        c.drawCircle(diffX, diffY + dp(42), dp(5), paint);

        // Front wheels.
        drawWheel(c, wheelX, upperWheelY,
                upperWheelBraked, upperShaftColor, red, white);

        drawWheel(c, wheelX, lowerWheelY,
                lowerWheelBraked, lowerShaftColor, red, white);

        // Keep wheel/shaft labels outside the mechanism.
        paint.setTextSize(dp(8));
        paint.setColor(muted);

        paint.setTextAlign(Paint.Align.RIGHT);


        paint.setTextAlign(Paint.Align.LEFT);

        paint.setColor(white);
        paint.setTextSize(dp(11));

        paint.setColor(white);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(dp(12));

        // Keep telemetry completely clear of the drivetrain drawing.
        float infoX = w * 0.73f;
        float infoY = h * 0.70f;

        c.drawText("PLANETARY KINEMATICS", infoX, infoY, paint);

        paint.setColor(cyan);
        c.drawText(String.format("Sun       %5.0f rpm", sunRpm),
                infoX, infoY + dp(20), paint);

        c.drawText(String.format("Ring      %5.0f rpm", ringRpm),
                infoX, infoY + dp(38), paint);

        c.drawText(String.format("Carrier   %5.0f rpm", carrierRpm),
                infoX, infoY + dp(56), paint);

        int c1StatusColor = c1Applied
                ? (engineGenerating ? red : active)
                : muted;

        int c2StatusColor = c2Applied
                ? (engineGenerating ? red : active)
                : muted;

        int c3StatusColor = c3Applied
                ? (engineGenerating ? red : active)
                : muted;

        paint.setColor(c1StatusColor);
        c.drawText("C1  " + (c1Applied ? "APPLIED" : "RELEASED"),
                infoX, infoY + dp(80), paint);

        paint.setColor(c2StatusColor);
        c.drawText("C2  " + (c2Applied ? "APPLIED" : "RELEASED"),
                infoX, infoY + dp(98), paint);

        paint.setColor(c3StatusColor);
        c.drawText("C3  " + (c3Applied ? "APPLIED" : "RELEASED"),
                infoX, infoY + dp(116), paint);

        paint.setColor(muted);
        paint.setTextSize(dp(10));
        c.drawText(
                "83ωr + 37ωs = 120ωc",
                infoX, infoY + dp(140), paint);

        c.drawText(
                "SIMULATION • PCG-1 LIVE STATE NEXT",
                infoX, infoY + dp(158), paint);
    }

    private void drawMotor(Canvas c, float x, float y,
                           String label, int color) {

        stroke.setColor(color);
        stroke.setStrokeWidth(dp(5));
        c.drawCircle(x, y, dp(35), stroke);

        paint.setColor(color);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(dp(11));
        c.drawText(label, x, y + dp(4), paint);
    }


    private void drawC1WagonBrake(
            Canvas c,
            float cx,
            float cy,
            float ringRadius,
            boolean applied,
            int appliedColor,
            int releasedColor) {

        // Small external brake shoe centered 45 degrees up/right.
        final float startAngle = -74f;
        final float sweepAngle = 58f;

        int shoeColor = applied ? appliedColor : releasedColor;

        // C1 physically moves toward the ring when applied and away when released.
        float brakeOffset = applied ? 0f : dp(8);

        Paint brake = new Paint(Paint.ANTI_ALIAS_FLAG);
        brake.setStyle(Paint.Style.STROKE);
        brake.setStrokeCap(Paint.Cap.ROUND);

        // Friction surface rides directly against outside of ring gear.
        float frictionRadius = ringRadius + dp(2) + brakeOffset;
        RectF frictionRect = new RectF(
                cx - frictionRadius,
                cy - frictionRadius,
                cx + frictionRadius,
                cy + frictionRadius);

        brake.setColor(Color.rgb(215, 198, 165));
        brake.setStrokeWidth(dp(4));
        c.drawArc(frictionRect, startAngle, sweepAngle, false, brake);

        // Brake shoe body outside the friction material.
        float shoeRadius = ringRadius + dp(9) + brakeOffset;
        RectF shoeRect = new RectF(
                cx - shoeRadius,
                cy - shoeRadius,
                cx + shoeRadius,
                cy + shoeRadius);

        brake.setColor(shoeColor);
        brake.setStrokeWidth(dp(8));
        c.drawArc(shoeRect, startAngle, sweepAngle, false, brake);

        // Narrow reinforcement strap on outer edge.
        float strapRadius = ringRadius + dp(15) + brakeOffset;
        RectF strapRect = new RectF(
                cx - strapRadius,
                cy - strapRadius,
                cx + strapRadius,
                cy + strapRadius);

        brake.setColor(Color.rgb(92, 101, 108));
        brake.setStrokeWidth(dp(2));
        c.drawArc(strapRect, startAngle, sweepAngle, false, brake);

        // Rivets following the shoe.
        Paint rivet = new Paint(Paint.ANTI_ALIAS_FLAG);
        rivet.setStyle(Paint.Style.FILL);
        rivet.setColor(Color.rgb(40, 45, 48));

        double[] angles = {-66, -56, -45, -34, -24};

        for (double deg : angles) {
            double a = Math.toRadians(deg);
            float x = cx + shoeRadius * (float)Math.cos(a);
            float y = cy + shoeRadius * (float)Math.sin(a);
            c.drawCircle(x, y, dp(2.5f), rivet);
        }

        // C1 label just outside the center of the brake shoe.
        double labelAngle = Math.toRadians(-45);
        float labelRadius = ringRadius + dp(30) + brakeOffset;
        float labelX = cx + labelRadius * (float)Math.cos(labelAngle);
        float labelY = cy + labelRadius * (float)Math.sin(labelAngle);

        Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
        label.setColor(Color.WHITE);
        label.setTextAlign(Paint.Align.CENTER);
        label.setTextSize(dp(9));
        label.setFakeBoldText(true);

        c.drawText("C1", labelX, labelY + dp(3), label);
    }

    private void drawClutch(Canvas c, float x, float y,
                            String label, boolean applied,
                            int appliedColor, int releasedColor) {

        // Short vertical shoes from the approved opposing clutch SVG.
        // Applied faces touch; released faces separate by 8dp.
        int color = applied ? appliedColor : releasedColor;
        float shoeHeight = dp(32);
        float shoeWidth = shoeHeight * 21f / 72f;
        float gap = applied ? 0f : dp(8);
        float left = x - gap / 2f - shoeWidth;
        float right = x + gap / 2f;
        float top = y - shoeHeight / 2f;

        clutchPaint.setStyle(Paint.Style.STROKE);
        clutchPaint.setStrokeCap(Paint.Cap.ROUND);
        clutchPaint.setStrokeWidth(dp(5));
        clutchPaint.setColor(color);
        c.drawLine(x - dp(14), y, left, y, clutchPaint);
        c.drawLine(right + shoeWidth, y, x + dp(14), y, clutchPaint);

        drawClutchShoe(c, left, top, shoeWidth, shoeHeight,
                true, color, applied);
        drawClutchShoe(c, right, top, shoeWidth, shoeHeight,
                false, color, applied);

        clutchPaint.setStyle(Paint.Style.FILL);
        clutchPaint.setColor(Color.WHITE);
        clutchPaint.setTextAlign(Paint.Align.CENTER);
        clutchPaint.setTextSize(dp(10));
        clutchPaint.setFakeBoldText(true);
        c.drawText(label, x, top - dp(7), clutchPaint);
    }

    private void drawClutchShoe(Canvas c, float left, float top,
                                float width, float height,
                                boolean facesRight, int faceColor,
                                boolean applied) {
        float radius = height * 5f / 72f;
        float faceWidth = width * 13f / 21f;
        float faceLeft = facesRight ? left + width - faceWidth : left;
        int edgeColor = applied ? Color.rgb(106, 143, 121)
                : Color.rgb(120, 135, 145);

        clutchRect.set(left, top, left + width, top + height);
        clutchPaint.setStyle(Paint.Style.FILL);
        clutchPaint.setColor(applied ? Color.rgb(23, 59, 42)
                : Color.rgb(25, 35, 42));
        c.drawRoundRect(clutchRect, radius, radius, clutchPaint);
        clutchPaint.setStyle(Paint.Style.STROKE);
        clutchPaint.setStrokeWidth(dp(0.7f));
        clutchPaint.setColor(edgeColor);
        c.drawRoundRect(clutchRect, radius, radius, clutchPaint);

        clutchRect.set(faceLeft, top, faceLeft + faceWidth, top + height);
        clutchPaint.setStyle(Paint.Style.FILL);
        clutchPaint.setColor(faceColor);
        c.drawRoundRect(clutchRect, radius, radius, clutchPaint);
        clutchPaint.setStyle(Paint.Style.STROKE);
        clutchPaint.setStrokeWidth(dp(0.55f));
        clutchPaint.setColor(edgeColor);
        c.drawRoundRect(clutchRect, radius, radius, clutchPaint);

        // Reinforcement edge faces outward; friction faces face inward.
        float edgeX = facesRight ? left + width * 3f / 21f
                : left + width * 18f / 21f;
        clutchPaint.setStrokeWidth(dp(0.7f));
        clutchPaint.setColor(faceColor);
        c.drawLine(edgeX, top + height * 5f / 72f,
                edgeX, top + height * 67f / 72f, clutchPaint);

        clutchPaint.setStyle(Paint.Style.FILL);
        clutchPaint.setColor(Color.rgb(11, 22, 28));
        float rivetX = faceLeft + faceWidth / 2f;
        for (int i = 0; i < 3; i++) {
            float rivetY = top + height * (12f + 24f * i) / 72f;
            c.drawCircle(rivetX, rivetY, height * 2.3f / 72f, clutchPaint);
        }
    }

    private void drawWheel(Canvas c,
                           float x, float y,
                           boolean braking,
                           int active,
                           int brake,
                           int white) {

        // Stationary front-view tires with rubber shoulders and tread.
        float wheelW = dp(76);
        float wheelH = dp(40);

        RectF tire = new RectF(
                x - wheelW / 2f,
                y - wheelH / 2f,
                x + wheelW / 2f,
                y + wheelH / 2f);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(8, 12, 15));
        c.drawRoundRect(tire, dp(10), dp(10), paint);

        stroke.setStyle(Paint.Style.STROKE);
        stroke.setColor(Color.rgb(144, 157, 165));
        stroke.setStrokeWidth(dp(2));
        c.drawRoundRect(tire, dp(10), dp(10), stroke);

        // Inset tread face leaves a dark rubber shoulder around it.
        RectF tread = new RectF(
                tire.left + dp(5), tire.top + dp(5),
                tire.right - dp(5), tire.bottom - dp(5));
        paint.setColor(Color.rgb(38, 47, 53));
        c.drawRoundRect(tread, dp(6), dp(6), paint);

        // Two continuous grooves and alternating diagonal tread blocks.
        stroke.setColor(Color.rgb(9, 15, 19));
        stroke.setStrokeWidth(dp(2));
        c.drawLine(tread.left + dp(4), y - dp(5),
                tread.right - dp(4), y - dp(5), stroke);
        c.drawLine(tread.left + dp(4), y + dp(5),
                tread.right - dp(4), y + dp(5), stroke);
        for (int i = 0; i < 6; i++) {
            float blockX = tread.left + dp(7 + i * 10);
            c.drawLine(blockX, tread.top + dp(2),
                    blockX - dp(4), y - dp(5), stroke);
            c.drawLine(blockX - dp(4), y + dp(5),
                    blockX, tread.bottom - dp(2), stroke);
            c.drawLine(blockX - dp(2), y - dp(4),
                    blockX + dp(1), y + dp(4), stroke);
        }

        if (braking) {
            paint.setColor(brake);
            c.drawCircle(x + wheelW / 2f + dp(9),
                    y, dp(7), paint);

            paint.setColor(brake);
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTextSize(dp(10));
            c.drawText("TCS BRAKE",
                    x + wheelW / 2f + dp(20),
                    y + dp(4), paint);
        }
    }

    // Draw a thin external gear, matching the approved SVG style.
    private void drawExternalGear(Canvas c,
                                  float cx, float cy,
                                  float rootRadius,
                                  float tipRadius,
                                  float innerRadius,
                                  int teeth,
                                  int color) {

        android.graphics.Path path = new android.graphics.Path();

        int steps = teeth * 4;

        for (int i = 0; i < steps; i++) {
            double a = -Math.PI / 2.0
                    + (Math.PI * 2.0 * i / steps);

            int phase = i % 4;
            float r = (phase == 1 || phase == 2)
                    ? tipRadius
                    : rootRadius;

            float x = cx + (float)Math.cos(a) * r;
            float y = cy + (float)Math.sin(a) * r;

            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }

        path.close();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        c.drawPath(path, paint);

        // Cut-looking inner circumference using background color.
        paint.setColor(Color.rgb(25, 35, 42));
        c.drawCircle(cx, cy, innerRadius, paint);

        // Thin inner witness circumference.
        stroke.setColor(color);
        stroke.setStrokeWidth(dp(2));
        c.drawCircle(cx, cy, innerRadius + dp(2), stroke);
    }

    // Draw an internal ring gear, matching the approved thin 83T SVG.
    private void drawInternalGear(Canvas c,
                                  float cx, float cy,
                                  float outerRadius,
                                  float rootRadius,
                                  float tipRadius,
                                  int teeth,
                                  int color) {

        android.graphics.Path outer = new android.graphics.Path();
        outer.addCircle(cx, cy, outerRadius,
                android.graphics.Path.Direction.CW);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        c.drawPath(outer, paint);

        android.graphics.Path inner = new android.graphics.Path();

        int steps = teeth * 4;

        for (int i = 0; i < steps; i++) {
            double a = -Math.PI / 2.0
                    + (Math.PI * 2.0 * i / steps);

            int phase = i % 4;

            float r = (phase == 0 || phase == 3)
                    ? rootRadius
                    : tipRadius;

            float x = cx + (float)Math.cos(a) * r;
            float y = cy + (float)Math.sin(a) * r;

            if (i == 0) {
                inner.moveTo(x, y);
            } else {
                inner.lineTo(x, y);
            }
        }

        inner.close();

        // Remove the center visually using the drivetrain background color.
        paint.setColor(Color.rgb(25, 35, 42));
        c.drawPath(inner, paint);

        stroke.setColor(color);
        stroke.setStrokeWidth(dp(2));
        c.drawCircle(cx, cy, outerRadius - dp(3), stroke);
    }

    private float rpmToDegrees(float rpm, float dt) {
        return rpm * 6f * dt;
    }

    private float wrap(float angle) {
        angle %= 360f;
        if (angle < 0f) angle += 360f;
        return angle;
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
