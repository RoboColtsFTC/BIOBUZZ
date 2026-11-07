package org.firstinspires.ftc.teamcode.autos;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.drivetrain.MecanumDrive;

@Autonomous(name="RedAutoLaunch", group="Robot")
public final class RedAutoLaunch extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        Pose2d beginPose = new Pose2d(0, 0, Math.toRadians(0));
        MecanumDrive drive = new MecanumDrive(hardwareMap, beginPose);
        // orientation for robot is intake in front (shooter in back)
        // left wheels aline with front wall and left side of 3rd square from left
        Robot robot = new Robot(this,true);

        waitForStart();
        /* -------------------------------------------------------------------------------------- */

        // beginPose = new Pose2d(0, 0, Math.toRadians(0));
        drive.localizer.setPose(beginPose);
        Actions.runBlocking(drive.actionBuilder(beginPose)            //  .strafeToLinearHeading(new Vector2d(0,-10), Math.toRadians(-90))
                //   .turn(Math.toRadians(45))
                //.strafeToLinearHeading(new Vector2d(15,0), Math.toRadians(0)) // move from start into shooting position
                .stopAndAdd(robot.actuatorcontrol.launchgamepeace.Launch_Auto())
//               .stopAndAdd(robot.actuatorcontrol.intake.Intake_auto())
//               .waitSeconds(3)
//                .strafeToLinearHeading(new Vector2d(38,0), Math.toRadians(0))
                .strafeToLinearHeading(new Vector2d(34,0), Math.toRadians(0))
                .strafeToLinearHeading(new Vector2d(38,-85), Math.toRadians(0))
                .strafeToLinearHeading(new Vector2d(42,-85), Math.toRadians(0))
                //              .strafeToLinearHeading(new Vector2d(85,47), Math.toRadians(0))
                //             .stopAndAdd(robot.actuatorcontrol.intake.Intake_auto())  //start intake action

                .build());

    }
}
//.afterTime(0, robot.actuatorcontrol.launchgamepeace.Launch_Auto())