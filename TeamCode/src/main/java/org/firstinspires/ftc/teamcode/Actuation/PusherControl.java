package org.firstinspires.ftc.teamcode.Actuation;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * Handles the push servo that pushes balls into the launcher.
 * Use this independently for tuning positions and timing.
 */
public class PusherControl {
    private final Servo pusherServo;
    private final ElapsedTime timer = new ElapsedTime();

    // === Tunable parameters ===
    private double forwardPos = 0.6;   // position that pushes ball forward
    private double retractPos = 0.0;   // resting position
    private long pushDurationMs = 300; // time to stay extended before retract

    private boolean isPushing = false;

    public PusherControl(HardwareMap hardwareMap) {
        pusherServo = hardwareMap.get(Servo.class, "Launch Prep Servo");
        pusherServo.setPosition(retractPos);
    }

    /** Extend servo to push position */
    public void push() {
        if (!isPushing) {
            pusherServo.setPosition(forwardPos);
            timer.reset();
            isPushing = true;
        }
    }

    /** Call periodically (e.g., in loop) to manage timing */
    public void update() {
        if (isPushing && timer.milliseconds() >= pushDurationMs) {
            pusherServo.setPosition(retractPos);
            isPushing = false;
        }
    }

    public boolean isPushing() {
        return isPushing;
    }

    public void setForwardPos(double pos) {
        forwardPos = pos;
    }

    public void setRetractPos(double pos) {
        retractPos = pos;
    }

    public void setPushDurationMs(long ms) {
        pushDurationMs = ms;
    }

    public double getForwardPos() {
        return forwardPos;
    }

    public double getRetractPos() {
        return retractPos;
    }

    public long getPushDurationMs() {
        return pushDurationMs;
    }
}
