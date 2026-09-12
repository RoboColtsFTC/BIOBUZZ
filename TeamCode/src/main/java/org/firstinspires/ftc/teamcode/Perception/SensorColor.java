/* Copyright (c) 2017-2020 FIRST. All rights reserved.
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
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode.Perception;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.SwitchableLight;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/*
 * This OpMode shows how to use a color sensor in a generic
 *
 */
@TeleOp(name = "Sensor: Color", group = "Sensor")
public class SensorColor extends LinearOpMode {

  /** The colorSensor field will contain a reference to our color sensor hardware object */
  NormalizedColorSensor colorSensor, colorSensor2,colorSensor3;
  public enum DetColor {
    PURPLE,
    GREEN,
    UNKNOWN

  }
  /** The relativeLayout field is used to aid in providing interesting visual feedback
   * in this sample application; you probably *don't* need this when you use a color sensor on your
   * robot. Note that you won't see anything change on the Driver Station, only on the Robot Controller. */
  View relativeLayout;
  final float[] hsvValues = new float[3];

  /*
   * The runOpMode() method is the root of this OpMode, as it is in all LinearOpModes.
   * Our implementation here, though is a bit unusual: we've decided to put all the actual work
   * in the runSample() method rather than directly in runOpMode() itself. The reason we do that is
   * that in this sample we're changing the background color of the robot controller screen as the
   * OpMode runs, and we want to be able to *guarantee* that we restore it to something reasonable
   * and palatable when the OpMode ends. The simplest way to do that is to use a try...finally
   * block around the main, core logic, and an easy way to make that all clear was to separate
   * the former from the latter in separate methods.
   */
  /*
   * The runOpMode() method is the root of this OpMode, as it is in all LinearOpModes.
   */
  @Override public void runOpMode() {
    // Get a reference to the RelativeLayout so we can later change the background
    // color of the Robot Controller app to match the hue detected by the RGB sensor.
    int relativeLayoutId = hardwareMap.appContext.getResources().getIdentifier("RelativeLayout", "id", hardwareMap.appContext.getPackageName());
    relativeLayout = ((Activity) hardwareMap.appContext).findViewById(relativeLayoutId);

    try {
      runSample(); // actually execute the sample
    } finally {
      // On the way out, *guarantee* that the background is reasonable. It doesn't actually start off
      // as pure white, but it's too much work to dig out what actually was used, and this is good
      // enough to at least make the screen reasonable again.
      // Set the panel back to the default color
      relativeLayout.post(new Runnable() {
        public void run() {
          relativeLayout.setBackgroundColor(Color.WHITE);
        }
      });
    }
  }

  protected void runSample() {
    float gain = 8;
      // Once per loop, we will update this hsvValues array. The first element (0) will contain the
      // hue, the second element (1) will contain the saturation, and the third element (2) will
      // contain the value. See http://web.archive.org/web/20190311170843/https://infohost.nmt.edu/tcc/help/pubs/colortheory/web/hsv.html
      // for an explanation of HSV color.

    colorSensor = hardwareMap.get(NormalizedColorSensor.class, "sensor_color1");
    colorSensor2 = hardwareMap.get(NormalizedColorSensor.class, "sensor_color2");
    colorSensor3 = hardwareMap.get(NormalizedColorSensor.class, "sensor_color3");
    // If possible, turn the light on in the beginning (it might already be on anyway,
    // we just make sure it is if we can).
    if (colorSensor instanceof SwitchableLight) {
      ((SwitchableLight)colorSensor).enableLight(true);
    }

    if (colorSensor2 instanceof SwitchableLight) {
      ((SwitchableLight)colorSensor2).enableLight(true);
    }

    if (colorSensor2 instanceof SwitchableLight) {
      ((SwitchableLight)colorSensor3).enableLight(true);
    }

    // Wait for the start button to be pressed.
    waitForStart();
    colorSensor.setGain(gain);
    colorSensor2.setGain(gain);
    colorSensor3.setGain(gain);

    // Loop until we are asked to stop
    while (opModeIsActive()) {

    DetectColor();

    }
  }
  public void DetectColor(){
    NormalizedRGBA colors = colorSensor.getNormalizedColors();
    NormalizedRGBA colors2 = colorSensor2.getNormalizedColors();
    NormalizedRGBA colors3 = colorSensor3.getNormalizedColors();

    /* Use telemetry to display feedback on the driver station. We show the red, green, and blue
     * normalized values from the sensor (in the range of 0 to 1), as well as the equivalent
     * HSV (hue, saturation and value) values. See http://web.archive.org/web/20190311170843/https://infohost.nmt.edu/tcc/help/pubs/colortheory/web/hsv.html
     * for an explanation of HSV color. */

    // Update the hsvValues array by passing it to Color.colorToHSV()
    Color.colorToHSV(colors.toColor(), hsvValues);
    relativeLayout.post(new Runnable() {
      public void run() {
        relativeLayout.setBackgroundColor(Color.HSVToColor(hsvValues));
      }
    });

    float normBlue, normGreen, normRed;
    float normBlue2, normGreen2, normRed2;
    float normBlue3, normGreen3, normRed3;
    DetColor detectedColor1;
    DetColor detectedColor2;
    DetColor detectedColor3;
    DetColor finalDetectedColor;
    /* If this color sensor also has a distance sensor, display the measured distance.
     * Note that the reported distance is only useful at very close range, and is impacted by
     * ambient light and surface reflectivity. */
    if (colorSensor instanceof DistanceSensor) {
      telemetry.addData("Distance (cm)", "%.3f", ((DistanceSensor) colorSensor).getDistance(DistanceUnit.CM));
    }
    normRed = colors.red / colors.alpha;
    normGreen = colors.green / colors.alpha;
    normBlue = colors.blue / colors.alpha;

    normRed2 = colors2.red / colors2.alpha;
    normGreen2 = colors2.green / colors2.alpha;
    normBlue2 = colors2.blue / colors2.alpha;

    normRed3 = colors3.red / colors3.alpha;
    normGreen3 = colors3.green / colors3.alpha;
    normBlue3 = colors3.blue / colors3.alpha;

    if (normRed < normGreen && normBlue > normGreen) {
      detectedColor1 = DetColor.PURPLE;
    } else if (normGreen > normRed && normGreen > normBlue && normGreen > .06) {
      detectedColor1 = DetColor.GREEN;
    } else {
      detectedColor1 = DetColor.UNKNOWN;
    }

    if (normRed2 < normGreen2 && normBlue2 > normGreen2) {
      detectedColor2 = DetColor.PURPLE;
    } else if (normGreen2 > normRed2 && normGreen2 > normBlue2 && normGreen2 > .06) {
      detectedColor2 = DetColor.GREEN;
    } else {
      detectedColor2 = DetColor.UNKNOWN;
    }
    if (normRed3< normGreen3 && normBlue3 > normGreen3) {
      detectedColor3 = DetColor.PURPLE;
    } else if (normGreen3 > normRed3 && normGreen3 > normBlue3 && normGreen3 > .06) {
      detectedColor3 = DetColor.GREEN;
    } else {
      detectedColor3= DetColor.UNKNOWN;
    }
    if (detectedColor1.equals(detectedColor2)) {
      finalDetectedColor = detectedColor1;
    } else if (detectedColor1.equals(detectedColor3)) {
      finalDetectedColor = detectedColor1;
    }else if (detectedColor2.equals(detectedColor3)){
      finalDetectedColor = detectedColor2;
    }
    else {
      finalDetectedColor = DetColor.UNKNOWN;
    }

    //telemetry.addData("NormRed", normRed);
    //telemetry.addData("NormGreen", normGreen);
    //telemetry.addData("NormBlue", normBlue);
    //telemetry.addData("NormRed2", normRed2);
    //telemetry.addData("NormGreen2", normGreen2);
    //telemetry.addData("NormBlue2", normBlue2);
    //telemetry.addData("gain", gain);
    telemetry.addLine("Detected Color1: " + detectedColor1);
    telemetry.addLine("Detected Color2: " + detectedColor2);
    telemetry.addLine("Final Detected Color: " + finalDetectedColor);
    telemetry.update();
  }
}