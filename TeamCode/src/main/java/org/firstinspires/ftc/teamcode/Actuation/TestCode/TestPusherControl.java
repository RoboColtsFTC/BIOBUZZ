package org.firstinspires.ftc.teamcode.Testing;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.teamcode.Actuation.PusherControl;

/**
 * Simple TeleOp for testing and tuning the pusher servo.
 *
 * Controls:
 *  - Press A to trigger a push.
 *  - Use dpad_up / dpad_down to adjust push position.
 *  - Use dpad_right / dpad_left to adjust retract position.
 *  - Use bumpers to increase/decrease push duration.
 */
@TeleOp(name="Pusher Test", group="Testing")
public class TestPusherControl extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        PusherControl pusher = new PusherControl(hardwareMap);

        telemetry.addLine("Pusher Test Ready");
        telemetry.addLine("A: Push");
        telemetry.addLine("Dpad Up/Down: Adjust push pos");
        telemetry.addLine("Dpad Right/Left: Adjust retract pos");
        telemetry.addLine("Bumpers: Adjust push time");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // --- Manual adjustments ---
            if (gamepad1.dpad_up) {
                pusher.setForwardPos(pusher.getForwardPos() + 0.01);
                sleep(150);
            }
            if (gamepad1.dpad_down) {
                pusher.setForwardPos(pusher.getForwardPos() - 0.01);
                sleep(150);
            }
            if (gamepad1.dpad_right) {
                pusher.setRetractPos(pusher.getRetractPos() + 0.01);
                sleep(150);
            }
            if (gamepad1.dpad_left) {
                pusher.setRetractPos(pusher.getRetractPos() - 0.01);
                sleep(150);
            }

            // --- Adjust push duration ---
            if (gamepad1.right_bumper) {
                pusher.setPushDurationMs(pusher.getPushDurationMs() + 50);
                sleep(150);
            }
            if (gamepad1.left_bumper) {
                pusher.setPushDurationMs(Math.max(50, pusher.getPushDurationMs() - 50));
                sleep(150);
            }

            // --- Trigger push ---
            if (gamepad1.a && !pusher.isPushing()) {
                pusher.push();
            }

            pusher.update();

            telemetry.addData("Push pos", "%.2f", pusher.getForwardPos());
            telemetry.addData("Retract pos", "%.2f", pusher.getRetractPos());
            telemetry.addData("Duration (ms)", pusher.getPushDurationMs());
            telemetry.addData("Is Pushing", pusher.isPushing());
            telemetry.update();
        }
    }
}