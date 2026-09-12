package org.firstinspires.ftc.teamcode.Perception;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.hardwareMap;
import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.opMode;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.SwitchableLight;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;


public class ColorDetector{
    public NormalizedColorSensor colorSensor, colorSensor2,colorSensor3;
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
    HardwareMap hardwareMap;
    //ColorSensor colorSensor;

    DetColor finalDetectedColor = DetColor.UNKNOWN;
    public LinearOpMode opmode;
    public double maxdist = 5.00;

    public ColorDetector(@NonNull LinearOpMode opmode) {
        this.opmode=opmode;
        this.hardwareMap = opmode.hardwareMap;


        // Get a reference to the RelativeLayout so we can later change the background
        // color of the Robot Controller app to match the hue detected by the RGB sensor.
        int relativeLayoutId = hardwareMap.appContext.getResources().getIdentifier("RelativeLayout", "id", hardwareMap.appContext.getPackageName());
        relativeLayout = ((Activity) hardwareMap.appContext).findViewById(relativeLayoutId);
        colorSensor = hardwareMap.get(NormalizedColorSensor.class, "sensor_color1");
        colorSensor2 = hardwareMap.get(NormalizedColorSensor.class, "sensor_color2");
        colorSensor3 = hardwareMap.get(NormalizedColorSensor.class, "sensor_color3");
//
//        try {
//            runSample(); // actually execute the sample
//        } finally {
//            // On the way out, *guarantee* that the background is reasonable. It doesn't actually start off
//            // as pure white, but it's too much work to dig out what actually was used, and this is good
//            // enough to at least make the screen reasonable again.
//            // Set the panel back to the default color
//            relativeLayout.post(new Runnable() {
//                public void run() {
//                    relativeLayout.setBackgroundColor(Color.WHITE);
//                }
//            });
//        }
    }

    public void run() {
        float gain = 8;
        // Once per loop, we will update this hsvValues array. The first element (0) will contain the
        // hue, the second element (1) will contain the saturation, and the third element (2) will
        // contain the value. See http://web.archive.org/web/20190311170843/https://infohost.nmt.edu/tcc/help/pubs/colortheory/web/hsv.html
        // for an explanation of HSV color.

//        colorSensor = hardwareMap.get(NormalizedColorSensor.class, "sensor_color1");
//        colorSensor2 = hardwareMap.get(NormalizedColorSensor.class, "sensor_color2");

        // If possible, turn the light on in the beginning (it might already be on anyway,
        // we just make sure it is if we can).
        if (colorSensor instanceof SwitchableLight) {
            ((SwitchableLight)colorSensor).enableLight(true);
        }

        if (colorSensor2 instanceof SwitchableLight) {
            ((SwitchableLight)colorSensor2).enableLight(true);
        }

        if (colorSensor3 instanceof SwitchableLight) {
            ((SwitchableLight)colorSensor3).enableLight(true);
        }

        // Wait for the start button to be pressed.

        colorSensor.setGain(gain);
        colorSensor2.setGain(gain);
        colorSensor3.setGain(gain);

        // Loop until we are asked to stop


        GetColor();

    }

    public DetColor GetColor() {
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
        /* If this color sensor also has a distance sensor, display the measured distance.
         * Note that the reported distance is only useful at very close range, and is impacted by
         * ambient light and surface reflectivity. */
        if (colorSensor instanceof DistanceSensor) {
           // opmode.telemetry.addData("Distance (cm)", "%.3f", ((DistanceSensor) colorSensor).getDistance(DistanceUnit.CM));
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
        /* updated from (normRed > .04 && normGreen > .11 && normBlue > .09) */
        } else if (normGreen > normRed && normGreen > normBlue && normGreen > .06) {
            detectedColor1 = DetColor.GREEN;
        } else {
            detectedColor1 = DetColor.UNKNOWN;
        }

        if (normRed2 < normGreen2 && normBlue2 > normGreen2) {
            detectedColor2 = DetColor.PURPLE;
        /* updated from normRed2 > .04 && normGreen > .11 && normBlue2 > .09 */
        } else if (normGreen2 > normRed2 && normGreen2 > normBlue2 && normGreen2 > .06) {
            detectedColor2 = DetColor.GREEN;
        } else {
            detectedColor2 = DetColor.UNKNOWN;
        }

        if (normRed3< normGreen3 && normBlue3 > normGreen3) {
            detectedColor3 = DetColor.PURPLE;
        /* normRed3 > .04 && normGreen3 > .11 && normBlue3 > .09 */
        } else if (normGreen3 > normRed3 && normGreen3 > normBlue3 && normGreen3 > .06) {
            detectedColor3 = DetColor.GREEN;
        } else {
            detectedColor3 = DetColor.UNKNOWN;
        }

        if (((detectedColor1 == DetColor.GREEN) && (detectedColor2 == DetColor.GREEN)) || ((detectedColor2 == DetColor.GREEN) && (detectedColor3 == DetColor.GREEN)) || ((detectedColor1 == DetColor.GREEN) && (detectedColor3 == DetColor.GREEN))) {
            finalDetectedColor = DetColor.GREEN;
        } else if (((detectedColor1 == DetColor.PURPLE) && (detectedColor2 == DetColor.PURPLE)) || ((detectedColor2 == DetColor.PURPLE) && (detectedColor3 == DetColor.PURPLE)) || ((detectedColor1 == DetColor.PURPLE) && (detectedColor3 == DetColor.PURPLE))) {
            finalDetectedColor = DetColor.PURPLE;
        } else {
            finalDetectedColor = DetColor.UNKNOWN;
        }

//        opMode.telemetry.addData("NormRed", normRed);
//        opMode.addData("NormGreen", normGreen);
//        opMode.addData("NormBlue", normBlue);
//        opMode.addData("NormRed2", normRed2);
//        telemetry.addData("NormGreen2", normGreen2);
//        telemetry.addData("NormBlue2", normBlue2);
//        telemetry.addData("gain", gain);
//        telemetry.addLine("Detected Color1: " + detectedColor1);
//        telemetry.addLine("Detected Color2: " + detectedColor2);
//        opmode.telemetry.addLine("Final Detected Color: " + finalDetectedColor);
//        opmode.telemetry.update();
        return finalDetectedColor;
    }



    public boolean colordetected() {
        boolean detected =false;
        if (
                ((((DistanceSensor) colorSensor).getDistance(DistanceUnit.CM) > 1.35) ||
                        (((DistanceSensor) colorSensor2).getDistance(DistanceUnit.CM) > 1.35) ||
                        (((DistanceSensor) colorSensor3).getDistance(DistanceUnit.CM) > 1.35))
                && ((((DistanceSensor) colorSensor).getDistance(DistanceUnit.CM) < maxdist) ||
                        (((DistanceSensor) colorSensor2).getDistance(DistanceUnit.CM) < maxdist)||
                        (((DistanceSensor) colorSensor3).getDistance(DistanceUnit.CM) < maxdist)))
        {
            switch(finalDetectedColor){
                case GREEN:
                    detected =true;
                    break;
                case PURPLE:
                    detected =true;
                    break;
                default:
                    detected =false;
                    break;
            }
        } else {
            detected = false;
        }
        opmode.telemetry.addData("Distance (cm)", "%.3f", ((DistanceSensor) colorSensor).getDistance(DistanceUnit.CM));
        opmode.telemetry.addLine("Final Detected Color: " + finalDetectedColor);
        opmode.telemetry.addData("Detected Boolean: ", detected);
        //opmode.telemetry.update();
        return detected;
    }


}


