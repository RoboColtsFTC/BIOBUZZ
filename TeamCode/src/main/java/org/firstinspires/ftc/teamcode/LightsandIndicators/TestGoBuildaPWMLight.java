package org.firstinspires.ftc.teamcode.LightsandIndicators;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Actuation.Actuators.AngleServo;

@TeleOp(name="TestGoBuildaPWMLight", group="Test")
public class TestGoBuildaPWMLight extends LinearOpMode {
public  GoBuildaPWMLight PMWLight;
boolean rebounce1, rebounce2=false;
public double lightnumber=.44;

    @Override
    public void runOpMode() throws InterruptedException {
        PMWLight= new GoBuildaPWMLight(this.hardwareMap,"light");

        waitForStart();

        while (opModeIsActive()) {


            PMWLight.SetColor(lightnumber);

            telemetry.addData("Lightnumber",lightnumber);
            telemetry.update();

        }
    }
}
