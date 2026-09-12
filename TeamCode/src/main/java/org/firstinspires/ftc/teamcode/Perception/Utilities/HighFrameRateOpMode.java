package org.firstinspires.ftc.teamcode.Perception.Utilities;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.VisionProcessor;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import android.util.Size;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "HighFrameRateOpMode", group = "Concept")
public class HighFrameRateOpMode extends LinearOpMode {


    public void runOpMode() {
        // Create an AprilTag processor (or your chosen processor)
        VisionProcessor aprilTag = new AprilTagProcessor.Builder().build();

        // Build the VisionPortal
        VisionPortal visionPortal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))


                // Set the streaming format to MJPEG for higher FPS
                .setStreamFormat(VisionPortal.StreamFormat.MJPEG)

                // Set the resolution to a size that supports 120 FPS
                //.setCameraResolution(new Size(1280, 800))
                .setCameraResolution(new Size(800, 600))

                // Disabling live view can free up resources for a higher frame rate
                .enableLiveView(false)


                .build();

        waitForStart();

        while (opModeIsActive()) {
            // Your normal OpMode code here
            telemetry.addData("FPS", visionPortal.getFps());
            telemetry.update();
        }
        visionPortal.close();
    }
}