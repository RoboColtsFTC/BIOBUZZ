package org.firstinspires.ftc.teamcode.Actuation;


import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.Actuation.Actuators.AngleServo;
import org.firstinspires.ftc.teamcode.Actuation.Actuators.ContinuousMotor;
import org.firstinspires.ftc.teamcode.Actuation.Actuators.ContinuousServo;
import org.firstinspires.ftc.teamcode.LightsandIndicators.GoBuildaPWMLight;

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
    // true = record each shot to the shot log, false = don't log.
    public static boolean SHOT_LOGGER_ENABLED = false;
    
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

    // Launcher control selector:
    //   true  = velocity control: the hub's PID holds each flywheel at its target RPM
    //           (Nectar_Target_RPM / Pollen_Target_RPM), so the speed doesn't drop as the
    //           battery drains and comes back faster after each ball.
    //   false = power control, as before (Launchmmotor_One_Power / Launchmmotor_Two_Power).
    // Can be changed in FTC Dashboard while the robot runs.
    public static boolean LAUNCH_VELOCITY_CONTROL = true;

    public static class Params {

        // Launchgate parameters
        public double LaunchGate_First = 103;  //calibrated to robot  103
        public double LaunchGate_Second = 0;   //calibrated to robot 0
        public double LaunchGate_MaxAngle = 300;

        // Intake Motor Parameters
        public double IntakeMotor_Power = 1;
        // ---- Launcher: Nectar = "LaunchMotor 1", Pollen = "LaunchMotor 2" ----
        // Everything below can be changed live in FTC Dashboard (ActuatorControl > param)
        // and takes effect on the next loop, so you can tune with the flywheels running.

        // Velocity control (LAUNCH_VELOCITY_CONTROL = true)
        // Target speeds in RPM. Starting values are close to what .73 / .72 power gave;
        // check them with the shot logs and adjust for distance.
        public double Nectar_Target_RPM = 3700;
        public double Pollen_Target_RPM = 3655;
        // A shot waits until each flywheel measures at least this fraction of its target.
        public double Launch_Ready_Fraction = 0.95;
        // How fast the target speed ramps, in RPM per second.
        public double Launch_RampUp_RPMps = 6000;
        public double Launch_RampDown_RPMps = 3000;
        // Velocity PIDF. While Launch_Use_Custom_PIDF is false the hub keeps its built-in
        // values (shown on telemetry as "Nectar PIDF" / "Pollen PIDF"). Set it true to use
        // the values below. Starting point: F = 32767 / (ticks/sec at full power),
        // P = F / 10, I = P / 10, D = 0. 13 assumes about 5000 RPM (2350 ticks/sec) at full power.
        public boolean Launch_Use_Custom_PIDF = true;
        public double Nectar_P = 1.3;
        public double Nectar_I = 0.13;
        public double Nectar_D = 0;
        public double Nectar_F = 13;
        public double Pollen_P = 36;
        public double Pollen_I = 0.6;
        public double Pollen_D = 0;
        public double Pollen_F = 12;

        // Power control (LAUNCH_VELOCITY_CONTROL = false), the old way
        public double Launchmmotor_One_Power = .73;
        public double Nectar_Ready_RPM = 3400;   // 95% of 3686 settled at .73 (2026-11-06, 11.8 V)
        public double Launchmmotor_Two_Power = .72;
        public double Pollen_Ready_RPM = 3657;   // 95% of 4286 settled at .85 (2026-11-06, 11.8 V)

        // Feed control Parameters

        public double Feed_Power_Load = .5;

        public double Feed_Power_Launch=.5;

        // Requeue shooter timing (ms). Only used when ORIGINAL_SHOOTER is false.
        // Feed_Launch_ms: how long the feeder pushes balls out. Set it just past the time
        // the last ball leaves, so the requeue starts right after. (The original code uses 4000.)
        public double Feed_Launch_ms = 1500;
        // Feed_Requeue_ms: how long the feeder then runs backward to pull back a stuck ball.
        public double Feed_Requeue_ms = 750;

        // Pre-set: at the start of every shot the feeder runs backward for Feed_Preset_ms,
        // then stops, so the balls start from the same spot each time. This runs while the
        // flywheels spin up. The feeder ramps (about 0.2 power per loop), so very short
        // times barely move the balls; start around 200 ms. 0 = off (the old behavior).
        public double Feed_Preset_ms = 200;
        public double Feed_Power_Preset = .3;

        // Spin-up check: a shot waits until each flywheel reaches this speed before feeding.
        // RPM, read from the "Nectar vel" / "Pollen vel" telemetry: spin up with A, let it
        // settle, and use about 95% of the reading. Update these whenever the powers change.
        // 0 = only wait for the power ramp (the old behavior).


        // Shoot anyway after this long (ms), so a bad encoder can't block shooting.
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