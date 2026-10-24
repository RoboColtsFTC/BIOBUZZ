package org.firstinspires.ftc.teamcode.Actuation;


import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.Actuation.Actuators.AngleServo;
import org.firstinspires.ftc.teamcode.Actuation.Actuators.ContinuousMotor;
import org.firstinspires.ftc.teamcode.Actuation.Actuators.ContinuousServo;
import org.firstinspires.ftc.teamcode.Actuation.Actuators.DualMotor;
import org.firstinspires.ftc.teamcode.Actuation.Actuators.FeedControl;
import org.firstinspires.ftc.teamcode.Actuation.Actuators.SpindexerControl;
import org.firstinspires.ftc.teamcode.LightsandIndicators.GoBuildaPWMLight;
import org.firstinspires.ftc.teamcode.Perception.ColorDetector;

import java.util.Arrays;
import java.util.List;

// This class is used for initializing all the actuators used in the Decode Game.
// It allows them to be initialized once and shared between different classes
@Config
public class ActuatorControl {

    public static class Actuators {

        public ContinuousMotor IntakeMotor_One;
        public ContinuousMotor IntakeMotor_Two;

        public ContinuousMotor LaunchMotor_One;
        public ContinuousMotor LaunchMotor_Two;

        public ContinuousServo GamePeaceFeed_One;
        public ContinuousServo GamePeaceFeed_Two;
        public AngleServo LaunchGate;

        public DigitalChannel BeamBrake;

    }

    public Actuators actuators = new Actuators();

    public Intake intake;
    public LaunchGamePeace launchgamepeace;

    public LinearOpMode opmode;
    public HardwareMap hardwaremap;


    public static class Params {

        // Launchgate parameters
        public double LaunchGate_First = 103;  //calibrated to robot  103
        public double LaunchGate_Second = 0;   //calibrated to robot 0
        public double LaunchGate_MaxAngle = 300;

        // Intake Motor Parameters
        public double IntakeMotor_Power = 1;
        public double Launchmmotor_One_Power = .73;  // ticks per second
        public double Launchmmotor_Two_Power = .73;

        // Feed control Parameters
        public double Feed_Power = 1;
    }

    public static GoBuildaPWMLight LockIndicator1, LockIndicator2, SpindexerStateIndicator2;

    Params param = new Params();


    public ActuatorControl(LinearOpMode opmode) {

        this.opmode = opmode;
        this.hardwaremap = opmode.hardwareMap;

        actuators.IntakeMotor_One = new ContinuousMotor(hardwaremap, "IntakeMotor 1", param.IntakeMotor_Power);
        actuators.IntakeMotor_Two = new ContinuousMotor(hardwaremap, "IntakeMotor 2", param.IntakeMotor_Power);

//        actuators.LaunchMotor_One = new ContinuousMotor(hardwaremap, "LaunchMotor 1", param.Launchmmotor_One_Power);
//        actuators.LaunchMotor_Two = new ContinuousMotor(hardwaremap, "LaunchMotor 2", param.Launchmmotor_Two_Power);

        actuators.GamePeaceFeed_One = new ContinuousServo(hardwaremap, "Feed 1", param.Feed_Power);
        actuators.GamePeaceFeed_Two = new ContinuousServo(hardwaremap, "Feed 2", param.Feed_Power);
       actuators.LaunchGate = new AngleServo(hardwaremap, "Gate", param.LaunchGate_First, param.LaunchGate_Second, param.LaunchGate_MaxAngle);
       actuators.BeamBrake = hardwaremap.get(DigitalChannel.class, "BeamBreak");
       actuators.BeamBrake.setMode(DigitalChannel.Mode.INPUT);

        intake = new Intake(this.opmode, actuators);
       launchgamepeace = new LaunchGamePeace(this.opmode, actuators);

//        LockIndicator1= new GoBuildaPWMLight(hardwaremap,"light1");
//        LockIndicator2= new GoBuildaPWMLight(hardwaremap,"light2");
//        SpindexerStateIndicator2= new GoBuildaPWMLight(hardwaremap,"light3");
    }




    public void run() {

        intake.run();
        launchgamepeace.run();

   }

}

