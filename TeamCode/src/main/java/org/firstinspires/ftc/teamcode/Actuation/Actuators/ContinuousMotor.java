package org.firstinspires.ftc.teamcode.Actuation.Actuators;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import java.util.Objects;

public class ContinuousMotor {
    private double velocity = 0;
    private DcMotorEx motor;
    private boolean isrunning = false;
    private boolean isVelocityMode = false;

    // FIX: keep the TARGET power and the power currently APPLIED separate,
    // so the ramp doesn't overwrite its own target.
    private double targetPower = 0;
    private double currentPower = 0;

    private double RampUpStepSize = .2;
    private double RampDownStepSize = .2;
    private static final double VELOCITY_TOLERANCE = 20; // ticks/sec

    public enum Direction {
        FORWARD,
        REVERSE,
        OTHER
    }

    private enum State {
        IDLE,
        EXICUTING,
        COMPLETE
    }

    private enum DirChangeState {
        IDLE,
        STOP,
        CHANGEDIR,
        START
    }

    private DirChangeState dirChangeState = DirChangeState.IDLE;
    private State MotorState = State.IDLE;

    public ContinuousMotor(HardwareMap hardwareMap, String MotorName, double power) {
        motor = hardwareMap.get(DcMotorEx.class, MotorName);
        motor.setDirection(DcMotorSimple.Direction.FORWARD);
        motor.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        this.targetPower = norm(power);
        this.isVelocityMode = false;
    }

    // overloading constructor for velocity input as an option.
    public ContinuousMotor(HardwareMap hardwareMap, String MotorName, double velocity, String inputmode) {
        // FIX: always get the motor, otherwise it stays null if the string doesn't match
        motor = hardwareMap.get(DcMotorEx.class, MotorName);
        motor.setDirection(DcMotorSimple.Direction.FORWARD);
        motor.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        if (Objects.equals(inputmode, "velocity")) {
            this.velocity = velocity;
            this.isVelocityMode = true;
        } else {
            this.targetPower = norm(velocity);
        }
    }

    public boolean isMotorAtVelocity() {
        // FIX: doubles are never exactly equal; use a tolerance
        return Math.abs(motor.getVelocity() - velocity) < VELOCITY_TOLERANCE;
    }

    public void SetVelocity(double velocity) {
        this.velocity = velocity;
        motor.setVelocity(velocity);
    }

    public double GetVelocity() {
        return motor.getVelocity();
    }

    public boolean IsRunning() {
        return isrunning;
    }

    // Sends power to the hardware. This is the ONLY place motor.setPower is called.
    private void applyPower(double power) {
        currentPower = norm(power);
        motor.setPower(currentPower);
    }

    /** Ramps up to targetPower. Returns true while busy, false when done. */
    public boolean StartMotor() {
        switch (MotorState) {
            case IDLE:
                // FIX: already running at target -> nothing to do
                if (isrunning && currentPower == targetPower) {
                    return false;
                }
                MotorState = State.EXICUTING;
                return true;

            case EXICUTING:
                double next = Math.min(currentPower + RampUpStepSize, targetPower);
                applyPower(next);
                if (next >= targetPower) {
                    isrunning = true;          // FIX: this was never set
                    MotorState = State.COMPLETE;
                }
                return true;

            case COMPLETE:
                MotorState = State.IDLE;
                return false;

            default:
                return false;
        }
    }

    /** Ramps down to 0. Returns true while busy, false when done. */
    public boolean StopMotor() {
        switch (MotorState) {
            case IDLE:
                // FIX: already stopped -> report done instead of starting a new ramp
                if (!isrunning && currentPower == 0) {
                    return false;
                }
                MotorState = State.EXICUTING;
                return true;

            case EXICUTING:
                double next = Math.max(currentPower - RampDownStepSize, 0);
                applyPower(next);
                if (next <= 0) {
                    isrunning = false;
                    MotorState = State.COMPLETE;
                }
                return true;

            case COMPLETE:
                MotorState = State.IDLE;
                return false;

            default:
                return false;
        }
    }

    /** Changes the target power. Applied immediately if the motor is already running. */
    public void SetPower(double power) {
        targetPower = norm(power);
        if (isrunning) {
            applyPower(targetPower);
        }
    }

    public Direction GetDirection() {
        if (motor.getDirection() == DcMotorSimple.Direction.FORWARD) {
            return Direction.FORWARD;
        } else if (motor.getDirection() == DcMotorSimple.Direction.REVERSE) {
            return Direction.REVERSE;
        } else {
            return Direction.OTHER;
        }
    }

    public void SetForward() {
        motor.setDirection(DcMotorSimple.Direction.FORWARD);
    }

    public void SetReverse() {
        motor.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    public boolean runForward() {
        return runDirection(Direction.FORWARD);
    }

    public boolean runReverse() {
        return runDirection(Direction.REVERSE);
    }

    /**
     * Stop (if needed) -> change direction (if needed) -> ramp up.
     * Returns true while busy, false when the motor is running in the requested direction.
     */
    private boolean runDirection(Direction wanted) {
        switch (dirChangeState) {
            case IDLE:
                if (GetDirection() == wanted) {
                    if (isrunning) {
                        return false;              // already running this way
                    }
                    dirChangeState = DirChangeState.START;
                } else if (isrunning) {
                    dirChangeState = DirChangeState.STOP;
                } else {
                    dirChangeState = DirChangeState.CHANGEDIR;
                }
                return true;

            case STOP:
                if (!StopMotor()) {
                    dirChangeState = DirChangeState.CHANGEDIR;
                }
                return true;

            case CHANGEDIR:
                if (wanted == Direction.FORWARD) {
                    SetForward();
                } else {
                    SetReverse();
                }
                dirChangeState = DirChangeState.START;   // FIX: was stuck here forever
                return true;

            case START:
                if (StartMotor()) {
                    return true;
                }
                dirChangeState = DirChangeState.IDLE;
                return false;

            default:
                return false;
        }
    }

    // FIX: clamp to [-1, 1] (old version turned negatives into +1)
    public double norm(double value) {
        return Math.max(-1, Math.min(1, value));
    }
}
