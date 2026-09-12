/*
 * Copyright (c) 2023 FIRST
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode.Perception.Utilities;

import android.util.Size;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;

/**
 * This OpMode initializes a webcam and starts a video stream to the Robot Controller.
 * It demonstrates how to manually control the stream using the gamepad.
 * This version is configured for a 1280x800 resolution and MJPEG stream format
 * to achieve a higher frame rate. Note that not all webcams support 120 FPS
 * at this resolution. The FTC SDK will negotiate the closest available frame rate.
 *
 * SETUP:
 * 1. Connect a webcam to your robot's control hub that supports these settings.
 * 2. In the Robot Configuration on the Driver Station, add the webcam and name it "Webcam 1".
 *
 * USAGE:
 * 1. Initialize this OpMode from the Driver Station.
 * 2. A live video feed will appear on the Robot Controller screen (if you have one connected)
 * or can be viewed using `scrcpy`.
 * 3. To see the stream on the Driver Station, touch the three-dot menu button and
 * select "Camera Stream" while the OpMode is initializing.
 * 4. Press the START button on the Driver Station to begin the OpMode.
 * 5. Use the D-pad Down on gamepad 1 to stop the stream.
 * 6. Use the D-pad Up on gamepad 1 to resume the stream.
 */
@TeleOp(name = "Concept: Webcam Stream", group = "Concept")
public class SimpleWebcamStream extends LinearOpMode {

    /**
     * The variable to store our instance of the vision portal.
     */
    private VisionPortal visionPortal;

    @Override
    public void runOpMode() throws InterruptedException {

        // Initialize the VisionPortal with the webcam
        initVisionPortal();

        telemetry.addLine("Webcam Initialized.");
        telemetry.addLine("Press 'Camera Stream' on Driver Station to see the feed.");
        telemetry.addLine("Ready to start.");
        telemetry.update();

        // Wait for the user to press the START button on the Driver Station
        waitForStart();

        // The main loop of the OpMode
        while (opModeIsActive() && !isStopRequested()) {

            // The stream is now continuous. The gamepad controls have been removed.

            // --- Telemetry ---
            // Add telemetry to show the current state of the camera stream.
            telemetry.addData("Camera State", visionPortal.getCameraState());
            telemetry.addLine("\nStream is running continuously.");
            telemetry.update();
        }

        // Make sure to release camera resources when the OpMode is stopped.
        visionPortal.close();
    }

    /**
     * Initializes the VisionPortal for streaming.
     */
    private void initVisionPortal() {
        // Create a new VisionPortal Builder
        visionPortal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1")) // Get camera from hardware map
                //.setCameraResolution(new Size(1280, 800)) // Set camera resolution
                .setCameraResolution(new Size(640, 480)) // Set camera resolution
                // Setting the stream format to MJPEG is the primary way to achieve a high frame rate.
                // The SDK will automatically negotiate the highest possible FPS from the camera
                // based on the requested resolution and this stream format.
                .setStreamFormat(VisionPortal.StreamFormat.MJPEG) // Set stream format to MJPEG for higher FPS
                .enableLiveView(true) // Enable live view on Robot Controller screen
                .setAutoStopLiveView(true) // Automatically stop live view when OpMode is stopped
                .build(); // Build the VisionPortal
    }
}
