package org.firstinspires.ftc.teamcode.Actuation;

import androidx.annotation.NonNull;

import org.firstinspires.ftc.teamcode.Actuation.ActuatorControl.Actuators;
import org.firstinspires.ftc.teamcode.Actuation.Actuators.ContinuousMotor;

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
    private final ElapsedTime RequeueTimer = new ElapsedTime();
    private final ElapsedTime PresetTimer = new ElapsedTime();
    // True once the pre-set reverse is finished and the feeder has stopped.
    private boolean presetDone = true;

    private double LauncherRuntime_ms = 4000;
    private double StopSEQDelay_ms = 0;
    private double StartSEQDelay_ms = 0;

    // Records the launch motor speeds during each shot (see ShotLogger).
    private final ShotLogger shotLogger;

    public LaunchGamePeace(LinearOpMode opmode, Actuators actuators, ActuatorControl.Params param) {
        this.opmode = opmode;
        this.actuators = actuators;
        this.param=param;
        this.shotLogger = new ShotLogger(opmode, actuators);
    }

    public boolean autoLaunch = false;

    public double autoVelocity = 0;

    public enum State {
        START,
        STOP,
        WAIT,
        REQUEUE,
        PRESET,
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

    // Auto: spin up both flywheels. Finishes as soon as the power ramp is done (a few
    // loops), so the robot drives on while the wheels come up to speed. Turn the shooter
    // on at a waypoint before the shooting spot, and it is ready when the robot gets there.
    public Action Shooter_On_Auto() {
        return new Action() {
            @Override
            public boolean run(@NonNull TelemetryPacket telemetryPacket) {
                applyLaunchSetpoints();
                // Call both every loop so neither motor gets skipped.
                boolean oneBusy = actuators.LaunchMotor_One.StartMotor();
                boolean twoBusy = actuators.LaunchMotor_Two.StartMotor();
                if (!oneBusy && !twoBusy) {
                    LaunchMotorState = State.WAIT;   // flywheels running
                    return false;
                }
                return true;
            }
        };
    }

    // Auto: stop both flywheels.
    public Action Shooter_Off_Auto() {
        return new Action() {
            @Override
            public boolean run(@NonNull TelemetryPacket telemetryPacket) {
                applyLaunchSetpoints();
                boolean oneBusy = actuators.LaunchMotor_One.StopMotor();
                boolean twoBusy = actuators.LaunchMotor_Two.StopMotor();
                if (!oneBusy && !twoBusy) {
                    LaunchMotorState = State.SEQCOMPLETE;
                    return false;
                }
                return true;
            }
        };
    }

    // Auto: shoot by running the feeder. The feeder is what pushes the balls into the
    // flywheels. Waits for the ready RPM (or Spinup_Timeout_ms) first, and spins the
    // flywheels up if Shooter_On_Auto was not used. Unlike Launch_Auto there is no extra
    // 3 s delay, and the flywheels stay on afterward (Shooter_Off_Auto stops them).
    public Action Shoot_Auto() {
        return new Action() {
            private boolean initialized = false;

            @Override
            public boolean run(@NonNull TelemetryPacket telemetryPacket) {
                if (!initialized) {
                    LaunchMotorState = State.START;   // no change if the flywheels are already on
                    state = State.START;
                    StartState = State.START;
                    LauncherRuntime.reset();
                    StartSEQDelay_ms = 0;
                    initialized = true;
                }
                return LaunchGamePeace_run();
            }
        };
    }

    public void run() {

        // rightTriggerWasPressed() is true once per press. right_trigger_pressed is true the
        // whole time the trigger is held, which restarted the shot (and its timers) every loop.
        if (opmode.gamepad2.rightTriggerWasPressed()) {
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

        // LaunchGamePeace_run() already calls LaunchMotors_run(). It used to be called
        // here a second time, which ramped the flywheels twice per loop in teleop.
        LaunchGamePeace_run();

        opmode.telemetry.addData("Launch control", ActuatorControl.LAUNCH_VELOCITY_CONTROL ? "velocity" : "power");
        opmode.telemetry.addData("Nectar vel", actuators.LaunchMotor_One.GetVelocity());
        opmode.telemetry.addData("Pollen vel", actuators.LaunchMotor_Two.GetVelocity());
        if (ActuatorControl.LAUNCH_VELOCITY_CONTROL) {
            // Graph "vel" against "setpoint" in FTC Dashboard to tune the PIDF.
            opmode.telemetry.addData("Nectar setpoint", actuators.LaunchMotor_One.GetSetpointRPM());
            opmode.telemetry.addData("Pollen setpoint", actuators.LaunchMotor_Two.GetSetpointRPM());
            opmode.telemetry.addData("Nectar PIDF", actuators.LaunchMotor_One.GetVelocityPIDF());
            opmode.telemetry.addData("Pollen PIDF", actuators.LaunchMotor_Two.GetVelocityPIDF());
        }
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

    /**
     * Sends the launcher settings from param to both flywheels: velocity or power mode,
     * target, ramp rates and PIDF. Called every loop so Dashboard changes apply right away.
     * The motors only change speed when StartMotor runs (START or WAIT below).
     */
    private void applyLaunchSetpoints() {
        ContinuousMotor nectar = actuators.LaunchMotor_One;
        ContinuousMotor pollen = actuators.LaunchMotor_Two;
        if (ActuatorControl.LAUNCH_VELOCITY_CONTROL) {
            nectar.SetVelocityRPM(param.Nectar_Target_RPM);
            pollen.SetVelocityRPM(param.Pollen_Target_RPM);
            nectar.SetVelocityRamp(param.Launch_RampUp_RPMps, param.Launch_RampDown_RPMps);
            pollen.SetVelocityRamp(param.Launch_RampUp_RPMps, param.Launch_RampDown_RPMps);
            if (param.Launch_Use_Custom_PIDF) {
                nectar.SetVelocityPIDF(param.Nectar_P, param.Nectar_I, param.Nectar_D, param.Nectar_F);
                pollen.SetVelocityPIDF(param.Pollen_P, param.Pollen_I, param.Pollen_D, param.Pollen_F);
            }
        } else {
            nectar.SetPower(param.Launchmmotor_One_Power);
            pollen.SetPower(param.Launchmmotor_Two_Power);
        }
    }

    /** True when the flywheel's ramp is done and its measured speed is high enough to shoot. */
    private boolean launchReady(ContinuousMotor motor, double powerModeReadyRPM) {
        double readyRPM = ActuatorControl.LAUNCH_VELOCITY_CONTROL
                ? motor.GetTargetVelocityRPM() * param.Launch_Ready_Fraction
                : powerModeReadyRPM;
        return motor.IsAtTarget() && Math.abs(motor.GetVelocity()) >= readyRPM;
    }

    public void LaunchMotors_run() {
        applyLaunchSetpoints();

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
                // Motors running; stay here until B is pressed. StartMotor does nothing
                // while they are at target, and ramps to a new target if it was changed
                // (for example a new RPM set in Dashboard).
                actuators.LaunchMotor_One.StartMotor();
                actuators.LaunchMotor_Two.StartMotor();
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
        boolean busy;
        if (ActuatorControl.ORIGINAL_SHOOTER) {
            busy = LaunchGamePeace_run_Original();
        } else {
            busy = LaunchGamePeace_run_Requeue();
        }
        // Teleop and every auto shot come through here, so all shots get recorded.
        if (ActuatorControl.SHOT_LOGGER_ENABLED) {
            shotLogger.update(state != State.SEQCOMPLETE, state, StartState);
        }
        return busy;
    }

    // Original shooter sequence: shoot for LauncherRuntime_ms, then stop the feeder.
    public boolean LaunchGamePeace_run_Original() {
        LaunchMotors_run();
        // State Machine for Launcher
        switch (state) {
            case START:
                LaunchGamePeace_Start();
                if (StartState == State.SEQCOMPLETE) {
                    state = State.WAIT;
                    LauncherRuntime.reset();   // full feed time starts now, not at the trigger press
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

    // Requeue shooter sequence: same as the original, but after shooting the feeder
    // runs in reverse for Feed_Requeue_ms. That pulls a ball stuck between the feeder
    // and the shooters back into the robot. Pressing shoot during the requeue starts
    // a new shot (the feeder ramps down, changes direction, and ramps back up).
    public boolean LaunchGamePeace_run_Requeue() {
        LaunchMotors_run();
        // State Machine for Launcher
        switch (state) {
            case START:
                LaunchGamePeace_Start();
                if (StartState == State.SEQCOMPLETE) {
                    state = State.WAIT;
                    LauncherRuntime.reset();   // full feed time starts now, not at the trigger press
                }
                return true;

            case WAIT:
                if (LauncherRuntime.milliseconds() > param.Feed_Launch_ms) {
                    state = State.REQUEUE;
                    RequeueTimer.reset();
                }
                return true;

            case REQUEUE:
                // Run the feeder backward, the same way it runs to load balls
                actuators.FeedMotor.SetPower(param.Feed_Power_Load);
                actuators.FeedMotor.runReverse();
                if (RequeueTimer.milliseconds() > param.Feed_Requeue_ms) {
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
                // First loop of a shot: start the pre-set reverse (if turned on).
                // Teleop, Launch_Auto and Shoot_Auto all set StartState = START, so this
                // runs for every shot.
                PresetTimer.reset();
                presetDone = param.Feed_Preset_ms <= 0;
                StartState = State.PRESET;
                // fall through so the spin-up check also runs this loop

            case PRESET:
                // 1) Pre-set: run the feeder backward for Feed_Preset_ms, then ramp it to a
                //    full stop so the balls sit still against the same spot.
                if (!presetDone) {
                    if (PresetTimer.milliseconds() < param.Feed_Preset_ms) {
                        actuators.FeedMotor.SetPower(param.Feed_Power_Preset);
                        actuators.FeedMotor.runReverse();
                    } else if (!actuators.FeedMotor.StopMotor()) {
                        presetDone = true;
                    }
                }

                // 2) At the same time, wait until both flywheels are really at speed
                //    (measured by encoder), not just until the power ramp is done.
                //    LauncherRuntime was reset when shoot was pressed, so it times the
                //    spin-up for the timeout. The timeout never skips the pre-set.
                boolean readyone = launchReady(actuators.LaunchMotor_One, param.Nectar_Ready_RPM);
                boolean readytwo = launchReady(actuators.LaunchMotor_Two, param.Pollen_Ready_RPM);
                if (presetDone
                        && ((readyone && readytwo) || LauncherRuntime.milliseconds() > param.Spinup_Timeout_ms)) {
                    StartState = State.WAIT;
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