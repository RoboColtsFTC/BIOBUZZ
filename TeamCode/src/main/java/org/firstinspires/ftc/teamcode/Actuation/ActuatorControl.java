package org.firstinspires.ftc.teamcode.Actuation;


import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.Actuation.Actuators.AngleServo;
import org.firstinspires.ftc.teamcode.Actuation.Actuators.ContinuousMotor;
import org.firstinspires.ftc.teamcode.Actuation.Actuators.DualMotor;
import org.firstinspires.ftc.teamcode.Actuation.Actuators.FeedControl;
import org.firstinspires.ftc.teamcode.Actuation.Actuators.SpindexerControl;
import org.firstinspires.ftc.teamcode.LightsandIndicators.GoBuildaPWMLight;
import org.firstinspires.ftc.teamcode.Perception.AprilTagData;
import org.firstinspires.ftc.teamcode.Perception.ColorDetector;

import java.util.Arrays;
import java.util.List;

// This class is used for initializing all the actuators used in the Decode Game.
// It allows them to be initialized once and shared between different classes
@Config
public class ActuatorControl {

    public static class Actuators{
        public FeedControl feedcontrol;
        public AngleServo FeedKicker;
        public AngleServo LaunchKicker;
        public ContinuousMotor IntakeMotor;
        public DualMotor LauncherMotor;
        public SpindexerControl spindexercontrol;

            }
    public Actuators actuators=new Actuators();

    public LoadSpindexer loadSpindexer;
    public LaunchGamePeace launchgamepeace;

    public LinearOpMode opmode;
    public HardwareMap hardwaremap;

    public static class Params{
        // Feed Kicker parameters
        public double FeedKicker_First=20;  //Calibrated to Robot 0
        public double FeedKicker_Second=160;  //Calibrated to Robot 115
        public double FeedKicker_MaxAngle=300;
        // LaunchKicker parameters
        public double LaunchKicker_First=103;  //calibrated to robot  103
        public double LaunchKicker_Second=0;   //calibrated to robot 0
        public double LaunchKicker_MaxAngle=300;

        // Intake Motor Parameters
        public double IntakeMotor_Power=1;
        public double Launchmmotor_velocity=.73;  // ticks per second

        // Feed control Parameters
        public double FeedControl_Power=.5;
    }

    public static GoBuildaPWMLight LockIndicator1,LockIndicator2, SpindexerStateIndicator2;

    public static List<ColorDetector.DetColor> colorPos= Arrays.asList(ColorDetector.DetColor.UNKNOWN, ColorDetector.DetColor.UNKNOWN, ColorDetector.DetColor.UNKNOWN);
    Params param=new Params();
    public static ControlState controlstate = ControlState.ready;
    public ActuatorControl(LinearOpMode opmode) {
        ControlState controlstate = ControlState.ready;
        this.opmode=opmode;
        this.hardwaremap= opmode.hardwareMap;
        actuators.feedcontrol=new FeedControl(hardwaremap, param.FeedControl_Power);
        actuators.FeedKicker= new AngleServo(hardwaremap,"FeedKicker",param.FeedKicker_First,param.FeedKicker_Second,param.FeedKicker_MaxAngle);
        actuators.LaunchKicker=new AngleServo(hardwaremap,"LaunchKicker",param.LaunchKicker_First,param.LaunchKicker_Second,param.LaunchKicker_MaxAngle);
        actuators.IntakeMotor = new ContinuousMotor(hardwaremap,"IntakeMotor",param.IntakeMotor_Power);
        actuators.LauncherMotor= new DualMotor(hardwaremap,"LauncherMotor1","LauncherMotor2",param.Launchmmotor_velocity);
        actuators.spindexercontrol=new SpindexerControl(hardwaremap,"Spindexer");
        loadSpindexer=new LoadSpindexer(this.opmode,actuators,colorPos);
        launchgamepeace=new LaunchGamePeace(this.opmode,actuators,colorPos);

        // Tuning
        actuators.IntakeMotor.SetReverse();
        actuators.feedcontrol.Reverse();

        LockIndicator1= new GoBuildaPWMLight(hardwaremap,"light1");
        LockIndicator2= new GoBuildaPWMLight(hardwaremap,"light2");
        SpindexerStateIndicator2= new GoBuildaPWMLight(hardwaremap,"light3");
    }

    public static enum ControlState{
        ready,
        loading,
        launching,
        tilting
    }


public void run() {

    loadSpindexer.run();
    launchgamepeace.run();



}

}

