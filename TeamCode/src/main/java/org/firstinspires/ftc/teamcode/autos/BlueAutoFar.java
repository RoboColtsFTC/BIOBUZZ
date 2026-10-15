package org.firstinspires.ftc.teamcode.autos;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.drivetrain.MecanumDrive;

@Autonomous(name="BlueAutoFar", group="Robot")
public final class BlueAutoFar extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        Pose2d beginPose = new Pose2d(0, 0, Math.toRadians(0));
        MecanumDrive drive = new MecanumDrive(hardwareMap, beginPose);

       Robot robot = new Robot(this,false);

        waitForStart();
        /* -------------------------------------------------------------------------------------- */

       // beginPose = new Pose2d(0, 0, Math.toRadians(0));
        drive.localizer.setPose(beginPose);
        Actions.runBlocking(drive.actionBuilder(beginPose)            //  .strafeToLinearHeading(new Vector2d(0,-10), Math.toRadians(-90))
             //   .turn(Math.toRadians(45))
                .strafeToLinearHeading(new Vector2d(3,0), Math.toRadians(0)) // new TranslationalVelConstraint(10)
//                             //   .waitSeconds(1)
////                .stopAndAdd(robot.actuatorcontrol.launchgamepeace.Launch_Auto(850))
//                        .strafeToLinearHeading(new Vector2d(3,20), Math.toRadians(0)) // new TranslationalVelConstraint(10)
//                    //    .waitSeconds(1)
//                .strafeToLinearHeading(new Vector2d(23,25), Math.toRadians(0))// new TranslationalVelConstraint(10)
//                .strafeToLinearHeading(new Vector2d(-7,0), Math.toRadians(0))// new TranslationalVelConstraint(10)
//                .strafeToLinearHeading(new Vector2d(-32,-13), Math.toRadians(0))// new TranslationalVelConstraint(10)
//                .strafeToLinearHeading(new Vector2d(10,5), Math.toRadians(0))// new TranslationalVelConstraint(10)


                .build());



    }
}
//.afterTime(0, robot.actuatorcontrol.launchgamepeace.Launch_Auto())