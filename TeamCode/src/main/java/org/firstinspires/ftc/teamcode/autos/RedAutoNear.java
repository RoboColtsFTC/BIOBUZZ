package org.firstinspires.ftc.teamcode.autos;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.drivetrain.MecanumDrive;

@Autonomous(name="RedAutoNear", group="Robot")
public final class RedAutoNear extends LinearOpMode {
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
        Actions.runBlocking(drive.actionBuilder(beginPose)
                //   .turn(Math.toRadians(45))
                .strafeToLinearHeading(new Vector2d(15,0), Math.toRadians(0)) // move from start into shooting position
                .waitSeconds(5) // **** Add shooting here ****
//                .strafeToLinearHeading(new Vector2d(15,-24), Math.toRadians(0))
//                .strafeToLinearHeading(new Vector2d(85,-47), Math.toRadians(0))
//             .stopAndAdd(robot.actuatorcontrol.launchgamepeace.Launch_Auto(850))

                .build());



    }
}
//.afterTime(0, robot.actuatorcontrol.launchgamepeace.Launch_Auto())