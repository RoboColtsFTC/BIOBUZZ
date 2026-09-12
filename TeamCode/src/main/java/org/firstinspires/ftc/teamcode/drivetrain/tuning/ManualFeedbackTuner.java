package org.firstinspires.ftc.teamcode.drivetrain.tuning;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.drivetrain.MecanumDrive;
import org.firstinspires.ftc.teamcode.drivetrain.PinpointLocalizer;

public final class ManualFeedbackTuner extends LinearOpMode {
    public static double DISTANCE = 64;

    @Override
    public void runOpMode() throws InterruptedException {
        MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));

//        if (PinpointLocalizer.PARAMS.perpXTicks == 0 && PinpointLocalizer.PARAMS.par0YTicks == 0 && PinpointLocalizer.PARAMS.par1YTicks == 1) {
//            throw new RuntimeException("Odometry wheel locations not set! Run AngularRampLogger to tune them.");
//        }

        waitForStart();

        while (opModeIsActive()) {
            Actions.runBlocking(
                drive.actionBuilder(new Pose2d(0, 0, 0))
                        .lineToX(DISTANCE)
                        .lineToX(0)
                        .build());
        }
    }
}
