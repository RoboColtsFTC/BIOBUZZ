package org.firstinspires.ftc.teamcode.Actuation.TestCode;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.Actuation.TiltRobot;

import org.firstinspires.ftc.teamcode.Actuation.Actuators.ThreePositionServo;

@Config
@TeleOp(name="TestTiltServo", group="Test")
public class TestTiltServo extends LinearOpMode {
    public double Position1=90;

    public double Position2=45;

    public double StartingAngle=0;
    public TiltRobot servo;

    @Override
    public void runOpMode() throws InterruptedException {
        servo= new TiltRobot(hardwareMap,"TiltServo",StartingAngle,Position1 ,Position2 );
        TelemetryPacket packet = new TelemetryPacket();



        waitForStart();

        while (opModeIsActive()) {

            if (gamepad1.dpad_up) {
                servo.tiltPosition1();
            }else if(gamepad1.dpad_down) {
                servo.tiltPosition2();
            }else if(gamepad1.dpad_left) {
                servo.ReturntoZero();
            }



            telemetry.addData("Servo Position", "Press D-Pad Up/Down/Left");
            telemetry.addData("P1 Angle", servo.currentangle);
            telemetry.update();
        }
    }
}
