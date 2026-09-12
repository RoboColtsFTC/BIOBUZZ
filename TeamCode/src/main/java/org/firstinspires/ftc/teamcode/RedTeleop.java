package org.firstinspires.ftc.teamcode;


import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
@Config
@TeleOp(name="Red Teleop", group="Linear OpMode")
public class RedTeleop extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        Robot robot = new Robot(this, false, true, true,false,false);

        waitForStart();

        while (opModeIsActive()) {
            robot.runRobot();
            telemetry.update();
        }
    }
}
