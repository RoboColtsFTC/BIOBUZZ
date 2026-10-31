package org.firstinspires.ftc.teamcode.Actuation;

import static android.telephony.PhoneNumberUtils.WAIT;

import androidx.annotation.NonNull;

import org.firstinspires.ftc.teamcode.Actuation.ActuatorControl.Actuators;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import com.qualcomm.robotcore.util.ElapsedTime;


import org.firstinspires.ftc.teamcode.Perception.ColorDetector;


import java.util.Arrays;
import java.util.List;

@Config
public class LaunchGamePeace {

    public Actuators actuators;
    public LinearOpMode opmode;


    private final ElapsedTime LauncherRuntime = new ElapsedTime();
    private final ElapsedTime StopSEQDelay = new ElapsedTime();
    private final ElapsedTime StartSEQDelay = new ElapsedTime();

    private double LauncherRuntime_ms = 10000;
    private double StopSEQDelay_ms = 0;
    private double StartSEQDelay_ms = 0;

    public LaunchGamePeace(LinearOpMode opmode, Actuators actuators) {

        this.opmode = opmode;
        this.actuators = actuators;


    }

    public boolean autoLaunch = false;

    public double autoVelocity = 0;

    public enum State {
        START,
        STOP,
        WAIT,
        SEQCOMPLETE
    }

    public State state = State.SEQCOMPLETE;

    private State StartState = State.SEQCOMPLETE;
    private State StopState = State.SEQCOMPLETE;

    public Action Launch_Auto() {
        return new Action() {
            @Override
            public boolean run(@NonNull TelemetryPacket telemetryPacket) {

                autoLaunch = true;
                state = State.START;
                StartState = State.START;
                LauncherRuntime.reset();
                LaunchGamePeace_run();
                return autoLaunch;
            }


        };
    }

    public void run() {

        if (opmode.gamepad2.right_trigger_pressed) {
            state = State.START;
            StartState = State.START;

            LauncherRuntime.reset();

        }

        if (opmode.gamepad2.left_trigger_pressed ) {
            state = State.STOP;
            StopState= State.START;
        }
        LaunchGamePeace_run();
        opmode.telemetry.addLine(String.format("state = %s, Start State= %s, StopState= %s",state,StartState,StopState));
        opmode.telemetry.addLine(String.format("LauncherRuntime = %.0f, StartSEQDelay = %.0f, StopSEQDelay = %.0f",
                LauncherRuntime.milliseconds(),
                StartSEQDelay.milliseconds(),
                StopSEQDelay.milliseconds()
        ));
    }

    public void LaunchGamePeace_run() {
        // State Machine for Launcher
        switch (state) {
            case START:

                //LaunchGamePeace_Start();

                if (StartState==State.SEQCOMPLETE) {
                state = State.WAIT;
            }
                break;
            case WAIT:
                if (LauncherRuntime.milliseconds() > LauncherRuntime_ms) {
                    state = State.STOP;
                    StopState= State.START;
                }
                break;
            case STOP:

               // LaunchGamePeace_stop();

                break;
            default:
                state = State.SEQCOMPLETE;
                autoLaunch = false;
                break;
        }

    }

    public void LaunchGamePeace_Start() {
        switch (StartState) {
            case START:
//                actuators.LaunchGate.SetSecond();
               StartState = State.WAIT;
                StartSEQDelay.reset();

                break;
            case WAIT:
                // Allowing for time for servo to actuate.

                if (StartSEQDelay.milliseconds() > StartSEQDelay_ms) {
                    //actuators.FeedMotor.runReverse();

                    StartState = State.SEQCOMPLETE;
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
                actuators.FeedMotor.StopMotor();
                StopSEQDelay.reset();
                StopState = State.WAIT;

                break;
            case WAIT:
                // Allowing time for servo to stop turning before lifting the gate.
                if (StopSEQDelay.milliseconds() > StopSEQDelay_ms) {

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






