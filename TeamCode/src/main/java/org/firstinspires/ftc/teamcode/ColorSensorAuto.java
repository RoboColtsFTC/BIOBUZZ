package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Autonomous OpMode using a REV Color Sensor V3 to detect color.
 * Displays RGB and Alpha values and identifies the dominant color.
 */
@Autonomous(name = "Color Sensor: REV V3", group = "Sensor")
public class ColorSensorAuto extends LinearOpMode {

    @Override
    public void runOpMode() {
        // Initialize the color sensor from the hardware map
        ColorSensor colorSensor = hardwareMap.get(ColorSensor.class, "colorSensor");

        telemetry.setMsTransmissionInterval(100);
        telemetry.setDisplayFormat(Telemetry.DisplayFormat.MONOSPACE);

        waitForStart();

        while (opModeIsActive()) {
            // Read raw color values
            int red = colorSensor.red();
            int green = colorSensor.green();
            int blue = colorSensor.blue();
            int alpha = colorSensor.alpha();

            // Display raw values
            telemetry.addLine("REV Color Sensor V3 Readings:");
            telemetry.addLine(String.format("Red   = %3d", red));
            telemetry.addLine(String.format("Green = %3d", green));
            telemetry.addLine(String.format("Blue  = %3d", blue));
            telemetry.addLine(String.format("Alpha = %3d", alpha));

            String detectedColor;

            if (red < green && blue > green && blue > 4000) {
                detectedColor = "PURPLE";
            } else if (green > red && green > blue && green > 2500) {
                detectedColor = "GREEN";
            } else {
                detectedColor = "UNKNOWN";
            }



            telemetry.addLine("Detected Color: " + detectedColor);
            telemetry.update();

            sleep(50);
        }
    }
}
