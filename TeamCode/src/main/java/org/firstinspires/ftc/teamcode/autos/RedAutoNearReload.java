package org.firstinspires.ftc.teamcode.autos;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.drivetrain.MecanumDrive;
@Disabled
@Autonomous(name="RedAutoNearReload", group="Robot")
public final class RedAutoNearReload extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        Pose2d beginPose = new Pose2d(0, 0, Math.toRadians(45));
        MecanumDrive drive = new MecanumDrive(hardwareMap, beginPose);

       Robot robot = new Robot(this,true);

        waitForStart();
        /* -------------------------------------------------------------------------------------- */

       // beginPose = new Pose2d(0, 0, Math.toRadians(0));
        drive.localizer.setPose(beginPose);
        Actions.runBlocking(drive.actionBuilder(beginPose)
                .strafeToLinearHeading(new Vector2d(-20,-20), Math.toRadians(45))
               // .stopAndAdd(robot.actuatorcontrol.launchgamepeace.Launch_Auto(1400))  //launch
                .strafeToLinearHeading(new Vector2d(-32,-28), Math.toRadians(180))  //move to intake position
               // .stopAndAdd(robot.actuatorcontrol.loadSpindexer.LoadSpindexer_auto())  //start intake action
                .strafeToLinearHeading(new Vector2d(-7,-28), Math.toRadians(180))  //intake
                    .waitSeconds(1)
                .strafeToLinearHeading(new Vector2d(-20,-20), Math.toRadians(45))  //back to launch position
               //.stopAndAdd(robot.actuatorcontrol.launchgamepeace.Launch_Auto(1400))  //launch
                .strafeToLinearHeading(new Vector2d(-20 ,5), Math.toRadians(0))  //end position

                // new TranslationalVelConstraint(10)
                .build());



    }
}
//.afterTime(0, robot.actuatorcontrol.launchgamepeace.Launch_Auto())