package org.firstinspires.ftc.teamcode.Actuation.Actuators;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Actuation.ActuatorControl;

import java.util.Objects;

public class ContinuousMotor {
    private final DcMotorEx motor;
    private boolean isrunning = false;
    private boolean isVelocityMode = false;

    // Targets for each mode. Velocity is in encoder ticks/sec (what motor.setVelocity expects).
    private double targetPower = 0;
    private double targetVelocity = 0;

    // The value currently being SENT to the motor.
    // Power mode -> 0..1, velocity mode -> ticks/sec.
    private double currentOutput = 0;

    // Power ramp: step size applied once per call (once per loop).
    private double RampUpStepSize = .2;          // power per call
    private double RampDownStepSize = .2;
    // Velocity ramp: rate in RPM per SECOND, so it ramps at the same speed however
    // fast the loop runs. 6000 RPM/s reaches 3600 RPM in 0.6 s.
    private double VelRampUp_RPMps = 6000;
    private double VelRampDown_RPMps = 3000;
    private final ElapsedTime rampTimer = new ElapsedTime();
    // Longest time step the velocity ramp will take in one call, so a long pause
    // between calls (or the first call) can't jump the setpoint.
    private static final double MAX_RAMP_DT = 0.1;   // seconds

    // Last PIDF sent with SetVelocityPIDF, so it is only sent to the hub when it changes.
    private double lastP = Double.NaN, lastI = Double.NaN, lastD = Double.NaN, lastF = Double.NaN;
    // PIDF read back from the hub, cached so telemetry doesn't read the hub every loop.
    private PIDFCoefficients cachedPIDF = null;

    private static final double TICKS_PER_REV = 28;       // bare motor encoder (matches GetVelocity)
    private static final double VELOCITY_TOLERANCE = 20;  // ticks/sec
    private static final double POWER_TOLERANCE = .001;

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

    // ---------------- Constructors ----------------

    public ContinuousMotor(HardwareMap hardwareMap, String MotorName, double power) {
        motor = hardwareMap.get(DcMotorEx.class, MotorName);
        SetEnconding(true);
        motor.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        this.targetPower = norm(power);
        this.isVelocityMode = false;
    }

    // inputmode "velocity" -> value is ticks/sec; anything else -> value is power.
    public ContinuousMotor(HardwareMap hardwareMap, String MotorName, double value, String inputmode) {
        motor = hardwareMap.get(DcMotorEx.class, MotorName);
        SetEnconding(true);
        motor.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        if (Objects.equals(inputmode, "velocity")) {
            this.targetVelocity = normVel(value);
            this.isVelocityMode = true;
        } else {
            this.targetPower = norm(value);
            this.isVelocityMode = false;
        }
    }

    // ---------------- Setpoints ----------------

    /** Sets the target power and switches to power mode. Takes effect on the next StartMotor/run call. */

    public void SetEnconding(boolean IsOn) {

        if(IsOn){
            motor.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        }else{
            motor.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        }

    }

    public void SetPower(double power) {
        targetPower = norm(power);
        switchMode(false);
    }

    /** Sets the target velocity in ticks/sec and switches to velocity mode. */
    public void SetVelocity(double ticksPerSec) {
        targetVelocity = normVel(ticksPerSec);
        switchMode(true);
    }

    /** Same as SetVelocity but in RPM (matches the units GetVelocity returns). */
    public void SetVelocityRPM(double rpm) {
        SetVelocity(rpm * TICKS_PER_REV / 60.0);
    }

    /** powerUp/powerDown: power per call. velUpRPMps/velDownRPMps: RPM per second. */
    public void SetRampSteps(double powerUp, double powerDown, double velUpRPMps, double velDownRPMps) {
        RampUpStepSize = powerUp;
        RampDownStepSize = powerDown;
        SetVelocityRamp(velUpRPMps, velDownRPMps);
    }

    /** Velocity ramp rates in RPM per second. 0 or less = no ramp (jump straight to the target). */
    public void SetVelocityRamp(double upRPMps, double downRPMps) {
        VelRampUp_RPMps = upRPMps;
        VelRampDown_RPMps = downRPMps;
    }

    /**
     * Sets the Control/Expansion Hub's velocity PIDF for this motor (used in velocity mode).
     * Only sends to the hub when a value changes, so it is fine to call every loop.
     * Starting point: F = 32767 / (max ticks/sec at full power), P = F / 10, I = P / 10, D = 0.
     */
    public void SetVelocityPIDF(double p, double i, double d, double f) {
        if (p == lastP && i == lastI && d == lastD && f == lastF) {
            return;
        }
        motor.setVelocityPIDFCoefficients(p, i, d, f);
        cachedPIDF = null;   // read the new values back next time they are asked for
        lastP = p;
        lastI = i;
        lastD = d;
        lastF = f;
    }

    /** The velocity PIDF the hub is using. Read from the hub once, then again only after a change. */
    public PIDFCoefficients GetVelocityPIDF() {
        if (cachedPIDF == null) {
            cachedPIDF = motor.getPIDFCoefficients(DcMotorEx.RunMode.RUN_USING_ENCODER);
        }
        return cachedPIDF;
    }

    // ---------------- Feedback ----------------

    /** True when the MEASURED speed is within tolerance of the target velocity. */
    public boolean isMotorAtVelocity() {
        return Math.abs(Math.abs(motor.getVelocity()) - targetVelocity) < VELOCITY_TOLERANCE;
    }

    public boolean isMotorAtPower() {
        return Math.abs(motor.getPower() - targetPower) < POWER_TOLERANCE;
    }

    /**
     * True when the ramp has finished and the motor is being commanded at its target
     * (power or velocity). It does not say the wheel has reached that speed: use
     * GetVelocity for that.
     */
    public boolean IsAtTarget() {
        return isrunning && currentOutput == target();
    }

    /** Target velocity in RPM (velocity mode). */
    public double GetTargetVelocityRPM() {
        return targetVelocity * 60 / TICKS_PER_REV;
    }

    /**
     * The velocity being commanded right now in RPM, which moves toward the target during
     * a ramp. Returns 0 in power mode. Plot this against GetVelocity to tune the PIDF.
     */
    public double GetSetpointRPM() {
        return isVelocityMode ? currentOutput * 60 / TICKS_PER_REV : 0;
    }

    /** Measured velocity in RPM. */
    public double GetVelocity() {
        return (motor.getVelocity() * 60 / TICKS_PER_REV);
    }

    public double GetPower() {
        return motor.getPower();
    }

    public boolean IsRunning() {
        return isrunning;
    }

    public boolean IsVelocityMode() {
        return isVelocityMode;
    }

    // ---------------- Output helpers ----------------

    /** The ONLY place the motor is commanded. */
    private void applyOutput(double value) {
        currentOutput = value;
        if (isVelocityMode) {
            motor.setVelocity(currentOutput);
        } else {
            motor.setPower(currentOutput);
        }
    }

    private double target() {
        return isVelocityMode ? targetVelocity : targetPower;
    }

    // Ramp step for this call. Power: a fixed step per call. Velocity: rate x time since
    // the last step, converted from RPM to ticks/sec.
    private double upStep(double dt) {
        return isVelocityMode ? velStep(VelRampUp_RPMps, dt) : RampUpStepSize;
    }

    private double downStep(double dt) {
        return isVelocityMode ? velStep(VelRampDown_RPMps, dt) : RampDownStepSize;
    }

    private static double velStep(double rpmPerSec, double dt) {
        if (rpmPerSec <= 0) {
            return Double.MAX_VALUE;   // no ramp
        }
        return rpmPerSec * TICKS_PER_REV / 60.0 * dt;
    }

    /** Seconds since the last ramp step (capped), and restarts the timer. */
    private double rampDt() {
        double dt = Math.min(rampTimer.seconds(), MAX_RAMP_DT);
        rampTimer.reset();
        return dt;
    }

    /** Moves 'from' one step toward 'to' without overshooting. */
    private static double stepToward(double from, double to, double up, double down) {
        if (from < to) {
            return Math.min(from + up, to);
        }
        return Math.max(from - down, to);
    }

    /**
     * Switches between power and velocity control. If the motor is already moving,
     * currentOutput is converted to the new units so the ramp starts from the
     * motor's real state instead of jumping.
     */
    private void switchMode(boolean velocity) {
        if (velocity == isVelocityMode) {
            return;
        }
        isVelocityMode = velocity;
        if (velocity) {
            motor.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
            currentOutput = isrunning ? Math.abs(motor.getVelocity()) : 0;
        } else {
            currentOutput = isrunning ? Math.abs(motor.getPower()) : 0;
        }
    }

    // ---------------- Start / Stop ----------------

    /** Ramps to the target (power or velocity). Returns true while busy, false when done. */
    public boolean StartMotor() {
        switch (MotorState) {
            case IDLE:
                if (isrunning && currentOutput == target()) {
                    return false;   // already at target
                }
                MotorState = State.EXICUTING;
                rampTimer.reset();
                return true;

            case EXICUTING:
                double tgt = target();
                double dt = rampDt();
                double next = stepToward(currentOutput, tgt, upStep(dt), downStep(dt));
                applyOutput(next);
                if (next == tgt) {   // exact: stepToward clamps to the target
                    isrunning = true;
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

    /** Ramps down to 0 (power or velocity). Returns true while busy, false when done. */
    public boolean StopMotor() {
        switch (MotorState) {
            case IDLE:
                if (!isrunning && currentOutput == 0) {
                    return false;   // already stopped
                }
                MotorState = State.EXICUTING;
                rampTimer.reset();
                return true;

            case EXICUTING:
                double dt = rampDt();
                double next = stepToward(currentOutput, 0, upStep(dt), downStep(dt));
                applyOutput(next);
                if (next == 0) {
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

    // ---------------- Direction ----------------

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

    /** Convenience: set a velocity (ticks/sec) and run forward. Call every loop. */
    public boolean runForwardVelocity(double ticksPerSec) {
        SetVelocity(ticksPerSec);
        return runForward();
    }

    /** Convenience: set a velocity (ticks/sec) and run reverse. Call every loop. */
    public boolean runReverseVelocity(double ticksPerSec) {
        SetVelocity(ticksPerSec);
        return runReverse();
    }

    /**
     * Stop (if needed) -> change direction (if needed) -> ramp up.
     * Works the same in power or velocity mode because StartMotor/StopMotor handle both.
     * Returns true while busy, false when running in the requested direction at target.
     */
    private boolean runDirection(Direction wanted) {
        if (ActuatorControl.ORIGINAL_SHOOTER) {
            return runDirection_Original(wanted);
        } else {
            return runDirection_Requeue(wanted);
        }
    }

    private boolean runDirection_Original(Direction wanted) {
        switch (dirChangeState) {
            case IDLE:
                if (GetDirection() == wanted) {
                    if (isrunning && currentOutput == target()) {
                        return false;              // already running this way at target
                    }
                    dirChangeState = DirChangeState.START;   // also re-ramps if the setpoint changed
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
                dirChangeState = DirChangeState.START;
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

    /**
     * Same as runDirection_Original, plus one fix: if the requested direction changes
     * while the motor is ramping up (for example, shoot pressed during a requeue),
     * ramp down from where it is, change direction, and ramp up again.
     * Without this, the motor finishes ramping the wrong way and reports done.
     */
    private boolean runDirection_Requeue(Direction wanted) {
        switch (dirChangeState) {
            case IDLE:
                if (GetDirection() == wanted) {
                    if (isrunning && currentOutput == target()) {
                        return false;              // already running this way at target
                    }
                    dirChangeState = DirChangeState.START;   // also re-ramps if the setpoint changed
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
                dirChangeState = DirChangeState.START;
                return true;

            case START:
                if (GetDirection() != wanted) {
                    // Direction request changed mid-ramp. Ramp down from the
                    // current output (never flip at speed), then change direction.
                    MotorState = State.EXICUTING;
                    dirChangeState = DirChangeState.STOP;
                    return true;
                }
                if (StartMotor()) {
                    return true;
                }
                dirChangeState = DirChangeState.IDLE;
                return false;

            default:
                return false;
        }
    }

    // ---------------- Clamps ----------------

    // Power is 0..1; reverse is handled by direction, so negatives are not allowed.
    public double norm(double value) {
        return Math.max(0, Math.min(1, value));
    }

    // Velocity magnitude only; direction is handled separately.
    private double normVel(double value) {
        return Math.max(0, value);
    }
}