package org.firstinspires.ftc.teamcode.Actuation.TestCode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Actuation.Actuators.ContinuousServo;

@TeleOp(name="TestContinuousServo", group="Test")
public class TestContinuousServo  extends LinearOpMode {


    public ContinuousServo servo;
    @Override
    public void runOpMode() throws InterruptedException {
        servo= new ContinuousServo(hardwareMap,"Test",20);


        waitForStart();

        while (opModeIsActive()) {

            if (gamepad1.dpad_up) {
                servo.Start();
            }else if(gamepad1.dpad_down) {
                servo.Stop();
            }else if(gamepad1.dpad_left) {
                servo.Reverse();
            }else if(gamepad1.dpad_right) {
                servo.Forward();
            }
        }
    }
}
