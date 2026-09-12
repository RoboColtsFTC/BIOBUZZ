//  Servo Class for setting two angles.
//


package org.firstinspires.ftc.teamcode.Actuation.Actuators;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class AngleServo {
    public Servo servo;
    public double First;
    public double Second;

    public double MaxAngle;  // Based on Servo's Max Range of Motion.


    public AngleServo(HardwareMap hardwareMap, String ServoName, double First, double Second, double MaxAngle) {
        servo = hardwareMap.get(Servo.class,ServoName);
        this.MaxAngle=MaxAngle;
        this.First=First;
        this.Second = Second;


    }

    public void SetFirst() {

          servo.setPosition(First/ MaxAngle);

    }

    public void SetSecond() {

            servo.setPosition(Second / MaxAngle);

    }
}
