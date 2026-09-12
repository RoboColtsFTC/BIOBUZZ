package org.firstinspires.ftc.teamcode.Actuation.TestCode;
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Actuation.Actuators.ThreePositionServo;
@Config
@TeleOp(name="TestThreePositionServo", group="Test")
public class TestThreePositionServo extends LinearOpMode {
    public static double Position1 = 10;
    public static double Position2 =  50;
    public static double Position3 = 250;
    public static double MaxAngle= 300;
    public static double Offset =0;
    public ThreePositionServo servo;
    private FtcDashboard dashboard;
    @Override
    public void runOpMode() throws InterruptedException {
        servo= new ThreePositionServo(hardwareMap,"Test",Position1 ,Position2 ,Position3 , MaxAngle,Offset);
        TelemetryPacket packet = new TelemetryPacket();
        dashboard = FtcDashboard.getInstance();


        waitForStart();

        while (opModeIsActive()) {

            if (gamepad1.dpad_up) {
                servo.setPosition(1);
            }else if(gamepad1.dpad_down) {
                servo.setPosition(2);
            }else if(gamepad1.dpad_left) {
                servo.setPosition(3);
            }

            packet.put("Position 1 Angle", Position1);
            packet.put("Position 2 Angle", Position2);
            packet.put("Position 3 Angle", Position3);
            packet.put("Max Angle",MaxAngle);
            packet.put("Offset",Offset);
            packet.put("Current Servo Position Command", servo.servo.getPosition());
            dashboard.sendTelemetryPacket(packet);
            servo.setAngle(Position1,Position2,Position3);
            servo.setOffset(Offset);

            telemetry.addData("Servo Position", "Press D-Pad Up/Down/Left");
            telemetry.addData("P1 Angle", Position1);
            telemetry.addData("P2 Angle", Position2);
            telemetry.addData("P3 Angle", Position3);
            telemetry.addData("Max Angle",MaxAngle);
            telemetry.addData("Offset",Offset);
            telemetry.update();
        }
    }
}
