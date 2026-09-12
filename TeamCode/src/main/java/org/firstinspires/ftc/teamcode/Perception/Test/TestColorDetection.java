package org.firstinspires.ftc.teamcode.Perception.Test;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.hardwareMap;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;


import org.firstinspires.ftc.teamcode.Perception.ColorDetector;

@TeleOp(name = "TestColorDetection ", group = "Test")
public class TestColorDetection extends LinearOpMode {
    NormalizedColorSensor colorSensor, colorSensor2;
    public ColorDetector colordetector;

    @Override
    public void runOpMode() throws InterruptedException {
        colordetector = new ColorDetector(this);

        // Get a reference to the RelativeLayout so we can later change the background
        // color of the Robot Controller app to match the hue detected by the RGB sensor.
        waitForStart();
        boolean detectcolor;
        while (opModeIsActive()) {
            colordetector.run(); // actually execute the sample
            detectcolor = colordetector.colordetected();

        }
    }

    }



