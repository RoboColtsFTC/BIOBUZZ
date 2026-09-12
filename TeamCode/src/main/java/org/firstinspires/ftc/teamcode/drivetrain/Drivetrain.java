package org.firstinspires.ftc.teamcode.drivetrain;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Pose2d;
import com.arcrobotics.ftclib.controller.PIDController;
import com.arcrobotics.ftclib.geometry.Rotation2d;
import com.arcrobotics.ftclib.geometry.Translation2d;
import com.arcrobotics.ftclib.kinematics.wpilibkinematics.ChassisSpeeds;
import com.arcrobotics.ftclib.kinematics.wpilibkinematics.MecanumDriveKinematics;
import com.arcrobotics.ftclib.kinematics.wpilibkinematics.MecanumDriveWheelSpeeds;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import com.qualcomm.robotcore.hardware.Gamepad;


import org.firstinspires.ftc.teamcode.Actuation.ActuatorControl;
import org.firstinspires.ftc.teamcode.Perception.AprilTagData;

@Config
public class Drivetrain {
    MecanumDrive drive;
    public static Pose2d pos;
    LinearOpMode opMode;

    double width = .3429;  //current width .37232 meters center of wheel to center of wheel
    double length = .3429; //.304 meters axil to axil

    Translation2d m_frontLeftLocation = new Translation2d(width/2.0, length/2.0);
    Translation2d m_frontRightLocation = new Translation2d(width/2.0, -length/2.0);
    Translation2d m_backLeftLocation = new Translation2d(-width/2.0, length/2.0);
    Translation2d m_backRightLocation = new Translation2d(-width/2.0, -length/2.0);

    // Creating my kinematics object using the wheel locations.
    MecanumDriveKinematics m_kinematics = new MecanumDriveKinematics(
                    m_frontLeftLocation, m_frontRightLocation,
                    m_backLeftLocation, m_backRightLocation);

    Gamepad driver;
    public static double ApirlTagRotationGain=2;
    public boolean isfeildDrive =true;  // set true for feild orentated driving control.

    public static double headingAngle=0;
    public static Rotation2d headingAngleRotated;

    public static double AprilTagBearing=0;
    public FtcDashboard dashboard;


    public static double kp=.025;
    public static double ki=.0025;
    public static double kd=0;
    PIDController controller = new PIDController(kp,ki,kd);

    public double yticks;
    public double xticks;
    public AprilTagData TagData;
    private FtcDashboard da;

    public Drivetrain(LinearOpMode opMode, AprilTagData TagData){
        this.opMode = opMode;

        this.driver = opMode.gamepad1;
        this.TagData = TagData;
        drive = new MecanumDrive(this.opMode.hardwareMap, new Pose2d(0,0,0));


    }
    boolean tagDetectedAtleastonce =false;

    enum TurnToTagState{
        START,
        STOP
    }
    TurnToTagState turntotagstate=TurnToTagState.STOP;

    public void run(){
        ChassisSpeeds speeds;
        xticks=drive.localizer.getXticks();
        yticks=drive.localizer.getYticks();
        double thetaPower;
        //drive.localizer.setPose(new Pose2d(0,0,90));
        drive.localizer.update();
        pos = drive.localizer.getPose();  // updated for
        headingAngle=drive.localizer.getHeading();//Math.toDegrees(pos.heading.toDouble());
        headingAngleRotated= Rotation2d.fromDegrees(headingAngle+180);

        if(TagData.red) {
            AprilTagBearing = TagData.Red.Bearing;
        }else{
            AprilTagBearing = TagData.Blue.Bearing;
        }

        if(TagData.detectionState.isBlueGoalAprilTagDetected && !TagData.red) {
            ActuatorControl.LockIndicator1.SetColor(.5);
            ActuatorControl.LockIndicator2.SetColor(.5);
        }else if (TagData.detectionState.isRedGoalAprilTagDetected && TagData.red){
            ActuatorControl.LockIndicator1.SetColor(.5);
            ActuatorControl.LockIndicator2.SetColor(.5);
        }else{
            ActuatorControl.LockIndicator1.SetColor(0);
            ActuatorControl.LockIndicator2.SetColor(0);
        }

//        if(TagData.detectionState.isAnyTagDetected){
//            ActuatorControl.LockIndicator.SetColor(.5);
//            tagDetectedAtleastonce=true;
//        }else{
//            ActuatorControl.LockIndicator.SetColor(0);
//        }
//
        dashboard =  FtcDashboard.getInstance();
        TelemetryPacket packet = new TelemetryPacket();

        packet.put("Heading Angle",headingAngle);
        packet.put("Heading Angle Rotated",headingAngleRotated);
        packet.put("AprilTag heading",AprilTagBearing);
        packet.put("Pinpoint IMU Status",drive.localizer.GetIMUStatus());
        packet.put("isAnyTagDetected",TagData.detectionState.isAnyTagDetected);
        packet.put("isBlueGoalAprilTagDetected",TagData.detectionState.isBlueGoalAprilTagDetected);
        packet.put("isRedGoalAprilTagDetected",TagData.detectionState.isRedGoalAprilTagDetected);
        dashboard.sendTelemetryPacket(packet);

       if(driver.a) {
           thetaPower = controller.calculate(headingAngle, -45 + 180);
        } else if(driver.x){
           thetaPower = controller.calculate(headingAngle, 90);
       } else if(driver.b) {
           thetaPower = controller.calculate(headingAngle, -90);

       } else if ((driver.right_trigger>.5) &&  TagData.detectionState.isAnyTagDetected){
                thetaPower=drive.RotateTwardsGoal();
//               thetaPower = controller.calculate(headingAngle, AprilTagBearing) * ApirlTagRotationGain;
               opMode.telemetry.addData("IMU Reading", "%5.1f inches", AprilTagBearing);

       } else {






          thetaPower = -driver.right_stick_x * Math.PI;
       }


           //thetaPower = controller.calculate(Math.toDegrees(headingAngle, AprilTagBearing)* ApirlTagRotationGain;


        double maxSpeed = 1;


        if(isfeildDrive) {
            speeds = ChassisSpeeds.fromFieldRelativeSpeeds(
                    driver.left_stick_y * maxSpeed,
                    driver.left_stick_x * maxSpeed,
                    thetaPower,
                    headingAngleRotated
    );
        }else {  // used to verify correct orentation for feild drive.
                speeds = new ChassisSpeeds(
               driver.left_stick_y * maxSpeed,
               driver.left_stick_x * maxSpeed,
               thetaPower
      );
}
        opMode.telemetry.addData("Pinpoint IMU Status",drive.localizer.GetIMUStatus());
        opMode.telemetry.addData("heading",Math.toDegrees(pos.heading.toDouble()));
        opMode.telemetry.addData("heading+180",Math.toDegrees(pos.heading.toDouble())+180);
        opMode.telemetry.addData("rotatedHeading",headingAngleRotated);
        opMode.telemetry.addData("AprilTag heading",AprilTagBearing);


        MecanumDriveWheelSpeeds wheelSpeeds = m_kinematics.toWheelSpeeds(speeds);

        wheelSpeeds.normalize(maxSpeed);

        drive.setWheelPowers(new double[]{
                wheelSpeeds.frontLeftMetersPerSecond,
                wheelSpeeds.rearLeftMetersPerSecond,
                wheelSpeeds.rearRightMetersPerSecond,
                wheelSpeeds.frontRightMetersPerSecond
        });

       if(driver.back){

           drive.localizer.resetPinpointIMU();
           drive.localizer.setPose(new Pose2d(0,0,0));

       }
    }
}
