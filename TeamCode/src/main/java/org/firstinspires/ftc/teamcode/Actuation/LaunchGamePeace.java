package org.firstinspires.ftc.teamcode.Actuation;

import androidx.annotation.NonNull;

import org.firstinspires.ftc.teamcode.Actuation.ActuatorControl.Actuators;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

@Config
public class LaunchGamePeace {

    public Actuators actuators;
    public LinearOpMode opmode;
    public ActuatorControl.Params param;

    private final ElapsedTime LauncherRuntime = new ElapsedTime();
    private final ElapsedTime StopSEQDelay = new ElapsedTime();
    private final ElapsedTime StartSEQDelay = new ElapsedTime();

    private double LauncherRuntime_ms = 4000;
    private double StopSEQDelay_ms = 0;
    private double StartSEQDelay_ms = 0;

    public LaunchGamePeace(LinearOpMode opmode, Actuators actuators, ActuatorControl.Params param) {
        this.opmode = opmode;
        this.actuators = actuators;
        this.param=param;
    }

    public boolean autoLaunch = false;

    public double autoVelocity = 0;

    public enum State {
        START,
        STOP,
        WAIT,
        SEQCOMPLETE
    }

    private State LaunchMotorState = State.SEQCOMPLETE;

    public State state = State.SEQCOMPLETE;

    private State StartState = State.SEQCOMPLETE;
    private State StopState = State.SEQCOMPLETE;

    public Action Launch_Auto() {
        return new Action() {
            private boolean initialized = false;

            @Override
            public boolean run(@NonNull TelemetryPacket telemetryPacket) {
                // Only kick off the sequence once. Road Runner calls run()
                // every loop, so resetting here every time would restart
                // the sequence forever and the action would never finish.
                if (!initialized) {
                    LaunchMotorState = State.START;
                    state = State.START;
                    StartState = State.START;
                    LauncherRuntime.reset();
                    initialized = true;
                    StartSEQDelay_ms=3000;

                }
                return LaunchGamePeace_run();
            }
        };
    }

    public void run() {

        if (opmode.gamepad2.right_trigger_pressed) {
            LaunchMotorState = State.START;
            state = State.START;
            StartState = State.START;
            LauncherRuntime.reset();
        }

        if (opmode.gamepad2.left_trigger_pressed) {
            state = State.STOP;
            StopState = State.START;
            LaunchMotorState = State.STOP;
        }

        LaunchGamePeace_run();
        LaunchMotors_run();

        opmode.telemetry.addData("Nectar vel", actuators.LaunchMotor_One.GetVelocity());
        opmode.telemetry.addData("Pollen vel", actuators.LaunchMotor_Two.GetVelocity());
        opmode.telemetry.addData("Feed vel", actuators.FeedMotor.GetVelocity());

        opmode.telemetry.addData("Nectar Pow", actuators.LaunchMotor_One.GetPower());
        opmode.telemetry.addData("Pollen Pow", actuators.LaunchMotor_Two.GetPower());
        opmode.telemetry.addData("Feed Pow", actuators.FeedMotor.GetPower());

//        opmode.telemetry.addLine(String.format("state = %s, Start State= %s, StopState= %s",
//                state, StartState, StopState));
//        opmode.telemetry.addLine(String.format("LauncherRuntime = %.0f, StartSEQDelay = %.0f, StopSEQDelay = %.0f",
//                LauncherRuntime.milliseconds(),
//                StartSEQDelay.milliseconds(),
//                StopSEQDelay.milliseconds()
//        ));
//
//        opmode.telemetry.addLine(String.format("LaunchMotorState = %s", LaunchMotorState));
    }

    public void LaunchMotors_run() {

        if (opmode.gamepad2.a) {
            LaunchMotorState = State.START;
        }

        if (opmode.gamepad2.b) {
            LaunchMotorState = State.STOP;
        }

        switch (LaunchMotorState) {
            case START: {
                // StartMotor() returns TRUE while still ramping, FALSE when done.
                // Call both every loop so neither motor gets skipped.
                boolean oneBusy = actuators.LaunchMotor_One.StartMotor();
                boolean twoBusy = actuators.LaunchMotor_Two.StartMotor();
                if (!oneBusy && !twoBusy) {
                    LaunchMotorState = State.WAIT;
                }
                break;
            }

            case WAIT:
                // Motors running; stay here until B is pressed
                break;

            case STOP: {
                // StopMotor() returns TRUE while still ramping down, FALSE when stopped.
                boolean oneBusy = actuators.LaunchMotor_One.StopMotor();
                boolean twoBusy = actuators.LaunchMotor_Two.StopMotor();

                if (!oneBusy && !twoBusy) {
                    LaunchMotorState = State.SEQCOMPLETE;
                }
                break;
            }

            case SEQCOMPLETE:
            default:
                break;
        }
    }

    public boolean LaunchGamePeace_run() {
        LaunchMotors_run();
        // State Machine for Launcher
        switch (state) {
            case START:
                LaunchGamePeace_Start();
                if (StartState == State.SEQCOMPLETE) {
                    state = State.WAIT;
                }
                return true;

            case WAIT:
                if (LauncherRuntime.milliseconds() > LauncherRuntime_ms) {
                    state = State.STOP;
                    StopState = State.START;
                }
                return true;

            case STOP:
                LaunchGamePeace_stop();
                return true;

            case SEQCOMPLETE:
                return false;

            default:
                state = State.SEQCOMPLETE;
                autoLaunch = false;
                return false;
        }
    }

    public void LaunchGamePeace_Start() {
        switch (StartState) {
            case START:
//                actuators.LaunchGate.SetSecond();

                boolean readyone = actuators.LaunchMotor_One.isMotorAtPower();
                boolean readytwo = actuators.LaunchMotor_Two.isMotorAtPower();
                if( readyone && readytwo) {
                    StartState = State.WAIT;
                    StartSEQDelay.reset();
                    StartSEQDelay.reset();
                }
                break;

            case WAIT:
                // Run feed motor; once it reports done, the sequence is complete
                if(StartSEQDelay.milliseconds()>StartSEQDelay_ms) {
                    actuators.FeedMotor.SetPower(param.Feed_Power_Launch);
                    if (!actuators.FeedMotor.runForward()) {
                        StartState = State.SEQCOMPLETE;
                    }
                }
                break;

            default:
                StartState = State.SEQCOMPLETE;
                break;
        }
    }

    public void LaunchGamePeace_stop() {
        switch (StopState) {
            case START:
                //LaunchMotorState = State.STOP;
                actuators.FeedMotor.StopMotor();
                StopSEQDelay.reset();
                StopState = State.WAIT;
                break;

            case WAIT:
                // Allowing time for motor to stop before finishing
                if (!actuators.FeedMotor.StopMotor()) {
                    StopState = State.SEQCOMPLETE;
                    state = State.SEQCOMPLETE;
                }
                break;

            default:
                StopState = State.SEQCOMPLETE;
                break;
        }
    }
}