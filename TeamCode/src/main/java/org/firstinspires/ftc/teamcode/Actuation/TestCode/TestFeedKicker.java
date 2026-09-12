package org.firstinspires.ftc.teamcode.Actuation.TestCode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Actuation.Actuators.AngleServo;

@TeleOp(name="TestFeedKicker", group="Test")
public class TestFeedKicker extends LinearOpMode {
public AngleServo servo;
    @Override
    public void runOpMode() throws InterruptedException {
        servo= new AngleServo(this.hardwareMap,"FeedKicker",20,130,300);

        waitForStart();

        while (opModeIsActive()) {

            if (gamepad1.dpad_up) {
                servo.SetFirst();
            }else if(gamepad1.dpad_down) {
                servo.SetSecond();
            }
        }
    }
}
