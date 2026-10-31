package org.firstinspires.ftc.teamcode.Actuation;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Actuation.ActuatorControl.Actuators;

@Config
public class Intake {

    public Actuators actuators;
    public LinearOpMode opmode;

    private boolean Forward_Debounce = false;
    private boolean Reverse_Debounce = false;
    private boolean Stop_Debounce = false;

    private enum State {
        IDLE,
        RUN
    }

    private State ForwardState = State.IDLE;
    private State ReverseState = State.IDLE;
    private State StopState = State.IDLE;

    public Intake(LinearOpMode opmode, Actuators actuators) {
        this.opmode = opmode;
        this.actuators = actuators;
    }

    public Action Intake_auto() {
        return new Action() {
            @Override
            public boolean run(@NonNull TelemetryPacket telemetryPacket) {
                // FIX: Road Runner keeps calling run() while it returns true.
                // Return "still busy" so the action ends once the intake is up to speed.
                return Intake_run_Forward();
            }
        };
    }

    private boolean allIdle() {
        return ForwardState == State.IDLE
                && ReverseState == State.IDLE
                && StopState == State.IDLE;
    }

    /** Call this once every loop. */
    public void run() {
        boolean forwardBtn = opmode.gamepad1.left_bumper;
        boolean reverseBtn = opmode.gamepad1.left_trigger > 0.5;
        boolean stopBtn = opmode.gamepad1.right_bumper;

        // Rising edge + nothing else in progress  (FIX: && not ||)
        if (forwardBtn && !Forward_Debounce && allIdle()) {
            ForwardState = State.RUN;
        }
        if (reverseBtn && !Reverse_Debounce && allIdle()) {
            ReverseState = State.RUN;
        }
        if (stopBtn && !Stop_Debounce && allIdle()) {
            StopState = State.RUN;
        }
        Forward_Debounce = forwardBtn;
        Reverse_Debounce = reverseBtn;
        Stop_Debounce = stopBtn;

        // FIX: the run methods return true while BUSY, so go IDLE when they return false
        if (ForwardState == State.RUN && !Intake_run_Forward()) {
            ForwardState = State.IDLE;
        }
        if (ReverseState == State.RUN && !Intake_run_Reverse()) {
            ReverseState = State.IDLE;
        }
        if (StopState == State.RUN && !Intake_run_Stop()) {
            StopState = State.IDLE;
        }

        opmode.telemetry.addData("ForwardState", ForwardState);
        opmode.telemetry.addData("ReverseState", ReverseState);
        opmode.telemetry.addData("StopState", StopState);
        opmode.telemetry.addData("Intake running", actuators.IntakeMotor_One.IsRunning());
        opmode.telemetry.addData("Feed running", actuators.FeedMotor.IsRunning());
    }

    // FIX: call BOTH motors every loop. "a() || b()" skips b() whenever a() is true.
    public boolean Intake_run_Forward() {
        boolean intakeBusy = actuators.IntakeMotor_One.runForward();
        boolean feedBusy = actuators.FeedMotor.runForward();
        return intakeBusy || feedBusy;
    }

    public boolean Intake_run_Reverse() {
        boolean feedBusy = actuators.FeedMotor.StopMotor();
        boolean intakeBusy = actuators.IntakeMotor_One.runReverse();
        return feedBusy || intakeBusy;
    }

    public boolean Intake_run_Stop() {
        boolean intakeBusy = actuators.IntakeMotor_One.StopMotor();
        boolean feedBusy = actuators.FeedMotor.StopMotor();
        return intakeBusy || feedBusy;
    }
}
