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

    public Action Intake_auto() {
        return new Action() {
            @Override
            public boolean run(@NonNull TelemetryPacket telemetryPacket) {
                // Road Runner keeps calling run() while it returns true.
                // Returns false once the intake is up to speed, ending the action.
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
        // Left trigger:  intake forward + feed reverse
        // Left bumper:   intake reverse + feed stop
        // Right bumper:  stop both
        boolean forwardBtn = opmode.gamepad1.left_trigger > 0.5;
        boolean reverseBtn = opmode.gamepad1.left_bumper;
        boolean stopBtn = opmode.gamepad1.right_bumper;

        // Rising edge + nothing else in progress
        if (forwardBtn && !Forward_Debounce && allIdle()) {
            ForwardState = State.RUN;
        }
        if (reverseBtn && !Reverse_Debounce && allIdle()) {
            ReverseState = State.RUN;
        }
        if (stopBtn && !Stop_Debounce && allIdle() || launchgamepeace.state == LaunchGamePeace.State.START) {
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
    // The feeder belongs to the shooter while a shot is running, so leave it alone then.
    // Two callers driving the feeder in opposite directions makes it flip back and forth.
    public boolean Intake_run_Forward() {
        boolean intakeBusy = actuators.IntakeMotor_One.runReverse();
        boolean feedBusy = false;
        if (launchgamepeace.state == LaunchGamePeace.State.SEQCOMPLETE) {
            feedBusy = actuators.FeedMotor.runReverse();
        }
        return intakeBusy || feedBusy;
    }

    public boolean Intake_run_Reverse() {
        boolean feedBusy = false;
        if (launchgamepeace.state == LaunchGamePeace.State.SEQCOMPLETE) {
            actuators.FeedMotor.SetPower(param.Feed_Power_Load);
            feedBusy = actuators.FeedMotor.StopMotor();
        }
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