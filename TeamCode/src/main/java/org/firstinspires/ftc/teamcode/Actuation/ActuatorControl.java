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
        public ContinuousMotor FeedMotor;
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

    // Shooter code selector:
    //   true  = original shooter code
    //   false = new shooter code: after each shot the feeder reverses for
    //           Feed_Requeue_ms to pull back a ball stuck before the shooters,
    //           plus the fix for changing motor direction in the middle of a ramp.
    public static boolean ORIGINAL_SHOOTER = false;

    public static class Params {

        // Launchgate parameters
        public double LaunchGate_First = 103;  //calibrated to robot  103
        public double LaunchGate_Second = 0;   //calibrated to robot 0
        public double LaunchGate_MaxAngle = 300;

        // Intake Motor Parameters
        public double IntakeMotor_Power = 1;
        // Nectar
        public double Launchmmotor_One_Power = .73;  // ticks per second
        public double Nectar_Ready_RPM = 3400;   // 95% of 3686 settled at .73 (2026-11-06, 11.8 V)

        // Pollen
        public double Launchmmotor_Two_Power = .72;
        public double Pollen_Ready_RPM = 3300;   // 95% of 4286 settled at .85 (2026-11-06, 11.8 V)
        // Shoot anyway after this long (ms), so a bad encoder can't block shooting.

        // Feed control Parameters

        public double Feed_Power_Load = .5;

        public double Feed_Power_Launch=1;

        // Requeue shooter timing (ms). Only used when ORIGINAL_SHOOTER is false.
        // Feed_Launch_ms: how long the feeder pushes balls out. Set it just past the time
        // the last ball leaves, so the requeue starts right after. (The original code uses 4000.)
        public double Feed_Launch_ms = 1500;
        // Feed_Requeue_ms: how long the feeder then runs backward to pull back a stuck ball.
        public double Feed_Requeue_ms = 750;

        // Spin-up check: a shot waits until each flywheel reaches this speed before feeding.
        // RPM, read from the "Nectar vel" / "Pollen vel" telemetry: spin up with A, let it
        // settle, and use about 95% of the reading. Update these whenever the powers change.
        // 0 = only wait for the power ramp (the old behavior).


        public double Spinup_Timeout_ms = 2000;
    }

    public static GoBuildaPWMLight LockIndicator1, LockIndicator2, SpindexerStateIndicator2;

    public static Params param = new Params();


    public ActuatorControl(LinearOpMode opmode) {

        this.opmode = opmode;
        this.hardwaremap = opmode.hardwareMap;

        actuators.IntakeMotor_One = new ContinuousMotor(hardwaremap, "IntakeMotor", param.IntakeMotor_Power);
        actuators.FeedMotor = new ContinuousMotor(hardwaremap, "FeedMotor", param.Feed_Power_Load );
        // Intake and feeder run on plain power, as before 2026-11-05. In RUN_USING_ENCODER the
        // Hub's velocity PID drives them from the encoder, and the intake ran, slowed, then
        // went full reverse on its own. Only the launch motors need the encoder.
        actuators.IntakeMotor_One.SetEnconding(false);
        actuators.FeedMotor.SetEnconding(false);

        // Nectar
       actuators.LaunchMotor_One = new ContinuousMotor(hardwaremap, "LaunchMotor 1", param.Launchmmotor_One_Power);
        actuators.LaunchMotor_One.SetReverse();
        // Pollen
       actuators.LaunchMotor_Two = new ContinuousMotor(hardwaremap, "LaunchMotor 2", param.Launchmmotor_Two_Power);


       actuators.BeamBrake = hardwaremap.get(DigitalChannel.class, "BeamBreak");
       actuators.BeamBrake.setMode(DigitalChannel.Mode.INPUT);


       launchgamepeace = new LaunchGamePeace(this.opmode, actuators,param);
       intake = new Intake(this.opmode, actuators,launchgamepeace,param);

//        LockIndicator1= new GoBuildaPWMLight(hardwaremap,"light1");
//        LockIndicator2= new GoBuildaPWMLight(hardwaremap,"light2");
//        SpindexerStateIndicator2= new GoBuildaPWMLight(hardwaremap,"light3");
    }




    public void run() {

        intake.run();
        launchgamepeace.run();


   }

}

