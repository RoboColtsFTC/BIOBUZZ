package org.firstinspires.ftc.teamcode.Actuation;

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


    List<ColorDetector.DetColor> colorPos;

    private final ElapsedTime LauncherMotorTimer = new ElapsedTime();
    private final ElapsedTime LoadGamePeaceTimer = new ElapsedTime();

    public LaunchGamePeace(LinearOpMode opmode, Actuators actuators, List<ColorDetector.DetColor> colorPos) {
        this.colorPos = colorPos;
        this.opmode = opmode;
        this.actuators = actuators;

        LauncherMotorTimer.reset();

    }

    public enum LauncherState {
        IDLE,
        MOTORSTARTUP,
        ACTIVELAUNCH,


    }

    public LauncherState launcherstate = LauncherState.IDLE;
    public List<Integer> LaunchOrder = Arrays.asList(6, 5, 4);  // Defalt sequnce
    public boolean autoLaunch = false;

    public double autoVelocity = 0;

    public Action Launch_Auto(double velocity) {
        return new Action() {
            @Override
            public boolean run(@NonNull TelemetryPacket telemetryPacket) {
                autoVelocity = velocity;
                autoLaunch = true;
                LoadSpindexer_run();

                return autoLaunch;
            }


        };
    }

    public void run() {
        LoadSpindexer_run();
    }

    public void LoadSpindexer_run() {


        switch (launcherstate) {

            case IDLE:


                if (opmode.gamepad2.x && ActuatorControl.controlstate == ActuatorControl.ControlState.ready) {

                    ActuatorControl.controlstate = ActuatorControl.ControlState.launching;
                    actuators.LauncherMotor.SetVelocity(1650); //120.7 12.53v

                    LauncherMotorTimer.reset();


                    LaunchOrder = Arrays.asList(6, 5, 4);

                    launcherstate = LauncherState.MOTORSTARTUP;


                } else if (autoLaunch) {
                    ActuatorControl.controlstate = ActuatorControl.ControlState.launching;
                    actuators.LauncherMotor.SetVelocity(autoVelocity); //120.7 12.53v.63

                    LauncherMotorTimer.reset();
                    LaunchOrder = Arrays.asList(6, 5, 4);
                    launcherstate = LauncherState.MOTORSTARTUP;


                }
                // Close Launching


                if (opmode.gamepad2.y && ActuatorControl.controlstate == ActuatorControl.ControlState.ready) {

                    ActuatorControl.controlstate = ActuatorControl.ControlState.launching;
                    actuators.LauncherMotor.SetVelocity(1400); //60.4 inch 12.59v works at 45.9

                    LauncherMotorTimer.reset();

                    LaunchOrder = Arrays.asList(6, 5, 4);

                    launcherstate = LauncherState.MOTORSTARTUP;

                }


                break;
            case MOTORSTARTUP:
                if (actuators.LauncherMotor.isMotorAtVelocity()) {

                    launcherstate = LauncherState.ACTIVELAUNCH;
                }
                break;
            case ACTIVELAUNCH:

                launchall();
                if (launchsequence == LaunchSequence.IDLE) {
                    launcherstate = LauncherState.IDLE;
                    autoLaunch = false;

                }

                break;

        }


    }


    public enum LaunchSequence {
        IDLE,
        LAUNCHPOSITION1,
        LAUNCHPOSITION2,
        LAUNCHPOSITION3

    }

    public LaunchSequence launchsequence = LaunchSequence.IDLE;

    public void launchall() {

        switch (launchsequence) {
            case IDLE:
                if (actuators.LauncherMotor.isMotorAtVelocity()) {
                    launchsequence = LaunchSequence.LAUNCHPOSITION1;
                }

                break;
            case LAUNCHPOSITION1:


                launch(LaunchOrder.get(0));

                if (loadgamepeace == LoadGamePeace.IDLE) {
                    launchsequence = LaunchSequence.LAUNCHPOSITION2;
                }
                break;
            case LAUNCHPOSITION2:


                launch(LaunchOrder.get(1));

                if (loadgamepeace == LoadGamePeace.IDLE) {
                    launchsequence = LaunchSequence.LAUNCHPOSITION3;
                }
                break;
            case LAUNCHPOSITION3:


                launch(LaunchOrder.get(2));

                if (loadgamepeace == LoadGamePeace.IDLE) {
                    launchsequence = LaunchSequence.IDLE;

                    ActuatorControl.controlstate = ActuatorControl.ControlState.ready;
                    LoadSpindexer.Currentstate = LoadSpindexer.State.Empty;
                    colorPos = Arrays.asList(ColorDetector.DetColor.UNKNOWN, ColorDetector.DetColor.UNKNOWN, ColorDetector.DetColor.UNKNOWN);

                }

                break;

        }

    }

    public enum LoadGamePeace {
        IDLE,
        SETPOSITION,
        ACTUATEKICKER,
        RETURNKICKERPOSITION

    }

    LoadGamePeace loadgamepeace = LoadGamePeace.IDLE;

    public void launch(int pos) {

        switch (loadgamepeace) {

            case IDLE:
                LoadGamePeaceTimer.reset();
                loadgamepeace = LoadGamePeace.SETPOSITION;
                break;

            case SETPOSITION:
                if (LoadGamePeaceTimer.milliseconds() >= 120) {
                    actuators.spindexercontrol.setPosition(pos);
                    LoadGamePeaceTimer.reset();
                    loadgamepeace = LoadGamePeace.ACTUATEKICKER;

                }


                break;
            case ACTUATEKICKER:
                if (LoadGamePeaceTimer.milliseconds() >= 120) {
                    actuators.LaunchKicker.SetSecond();
                    LoadGamePeaceTimer.reset();
                    loadgamepeace = LoadGamePeace.RETURNKICKERPOSITION;
                }
                break;
            case RETURNKICKERPOSITION:
                if (LoadGamePeaceTimer.milliseconds() >= 120) {
                    actuators.LaunchKicker.SetFirst();
                    LoadGamePeaceTimer.reset();
                    loadgamepeace = LoadGamePeace.IDLE;
                }
                break;


        }

    }




    }






