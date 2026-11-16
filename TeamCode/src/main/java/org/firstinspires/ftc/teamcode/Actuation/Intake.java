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
    public LaunchGamePeace launchgamepeace;
    public ActuatorControl.Params param;

    private boolean Forward_Debounce = false;
    private boolean Reverse_Debounce = false;
    private boolean Stop_Debounce = false;

    private enum State {
        IDLE,
        RUN
    }

    private State ForwardState = State.IDLE;
    private State ReverseState = State.IDLE;
    public State StopState = State.IDLE;

    public Intake(LinearOpMode opmode, Actuators actuators,LaunchGamePeace launchgamepeace,ActuatorControl.Params param) {
        this.opmode = opmode;
        this.actuators = actuators;
        this.launchgamepeace=launchgamepeace;
        this.param=param;
    }

    // Auto: intake ON. Runs the intake motor and runs the feeder backward at Feed_Power_Load
    // to pull balls in (the same as left trigger in teleop). The action finishes once both
    // motors are up to speed, and they keep running until Intake_auto_Stop() or a shot.
    // A shot runs the feeder forward and then stops it, so add Intake_auto() again after a
    // shot to keep collecting. Safe to call when the intake is already on.
    public Action Intake_auto() {
        return new Action() {
            @Override
            public boolean run(@NonNull TelemetryPacket telemetryPacket) {
                // Road Runner keeps calling run() while it returns true.
                // If a shot is still running (only possible if this runs in parallel with
                // a shot), the shooter owns the feeder: start only the intake motor and
                // wait for the shot to finish before reversing the feeder.
                if (launchgamepeace.state != LaunchGamePeace.State.SEQCOMPLETE) {
                    actuators.IntakeMotor_One.runReverse();
                    return true;
                }
                return Intake_run_Forward();
            }
        };
    }

    // Auto: intake OFF. Ramps the intake motor and the feeder down to stopped.
    // The action finishes once both have stopped. If a shot is running, only the intake
    // motor is stopped and the shooter stops the feeder when the shot ends.
    public Action Intake_auto_Stop() {
        return new Action() {
            @Override
            public boolean run(@NonNull TelemetryPacket telemetryPacket) {
                return Intake_run_Stop();
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
        // Left trigger:  intake forward + feed reverse
        // Left bumper:   intake reverse + feed stop
        // Right bumper:  stop both
        boolean forwardBtn = opmode.gamepad1.left_trigger > 0.5;
        boolean reverseBtn = opmode.gamepad1.left_bumper;
        boolean stopBtn = opmode.gamepad1.right_bumper;

        // While a shot is in progress the shooter owns the feeder: it runs it forward to
        // shoot, then reverse to requeue, then stops it. If the intake also commanded the
        // feeder here, the two would fight over its direction. So during a shot: cancel any
        // intake action, stop only the intake motor, ignore the intake buttons, and leave
        // the feeder alone. Press intake again after the shot to start loading.
        if (launchgamepeace.state != LaunchGamePeace.State.SEQCOMPLETE) {
            ForwardState = State.IDLE;
            ReverseState = State.IDLE;
            StopState = State.IDLE;
            actuators.IntakeMotor_One.StopMotor();
            Forward_Debounce = forwardBtn;
            Reverse_Debounce = reverseBtn;
            Stop_Debounce = stopBtn;
            opmode.telemetry.addData("Intake", "locked out: shot in progress");
            opmode.telemetry.addData("Intake running", actuators.IntakeMotor_One.IsRunning());
            opmode.telemetry.addData("Feed running", actuators.FeedMotor.IsRunning());
            return;
        }

        // Rising edge + nothing else in progress
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

        // The run methods return true while BUSY, so go IDLE when they return false
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

    // Call BOTH motors every loop. "a() || b()" would skip b() whenever a() is true.
    public boolean Intake_run_Forward() {
        // Set the load power every time. Without this the feeder kept the last power set,
        // which after a shot was Feed_Power_Launch (full power).
        actuators.FeedMotor.SetPower(param.Feed_Power_Load);
        boolean intakeBusy = actuators.IntakeMotor_One.runReverse();
        boolean feedBusy = actuators.FeedMotor.runReverse();
        return intakeBusy || feedBusy;
    }

    public boolean Intake_run_Reverse() {
        boolean feedBusy = actuators.FeedMotor.StopMotor();
        boolean intakeBusy = actuators.IntakeMotor_One.runForward();
        return feedBusy || intakeBusy;
    }

    public boolean Intake_run_Stop() {
        boolean intakeBusy = actuators.IntakeMotor_One.StopMotor();
        boolean feedBusy = false;
        // Only stop the feeder when no shot is in progress. The old check (state != START)
        // let this stop the feeder in the middle of a shot, once the shot left START.
        if (launchgamepeace.state == LaunchGamePeace.State.SEQCOMPLETE) {
           feedBusy = actuators.FeedMotor.StopMotor();
        }
        return intakeBusy || feedBusy;
    }
}