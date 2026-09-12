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
        Pose2d beginPose = new Pose2d(0, 0, Math.toRadians(-90));
        MecanumDrive drive = new MecanumDrive(hardwareMap, beginPose);

       Robot robot = new Robot(this,false);

        waitForStart();
        /* -------------------------------------------------------------------------------------- */

       // beginPose = new Pose2d(0, 0, Math.toRadians(0));
        drive.localizer.setPose(beginPose);
        Actions.runBlocking(drive.actionBuilder(beginPose)
                .strafeToLinearHeading(new Vector2d(0,-10), Math.toRadians(-90))
                .turn(Math.toRadians(25))
                .stopAndAdd(robot.actuatorcontrol.launchgamepeace.Launch_Auto(1550))
                .strafeToLinearHeading(new Vector2d(5 ,-38), Math.toRadians(0))// new TranslationalVelConstraint(10)

                .build());



    }
}
//.afterTime(0, robot.actuatorcontrol.launchgamepeace.Launch_Auto())