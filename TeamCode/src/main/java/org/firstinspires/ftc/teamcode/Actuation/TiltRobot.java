package org.firstinspires.ftc.teamcode.Actuation;

import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImplEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PwmControl;
public class TiltRobot {

    //public ServoImplEx servo;
    public Servo servo;

    public double incrementangle=1;

    public double Position1;

    public double Position2;

    public double StartingAngle=0;
    public double MaxAngle= 300;

    public double currentangle;



    public TiltRobot(HardwareMap hardwareMap, String ServoName,double StartingAngle, double Position1, double Position2){
        //servo = (ServoImplEx)hardwareMap.get(Servo.class, ServoName);
        servo = hardwareMap.get(Servo.class, ServoName);
        //servo.setPwmRange(new PwmControl.PwmRange(800, 2200));
        this.StartingAngle=StartingAngle;
        this.Position1=Position1;
        this.Position2=Position2;


        }

        public void tiltPosition1(){
            servo.setPosition(Position1/MaxAngle);

        }
        public void tiltPosition2(){
            servo.setPosition(Position2/MaxAngle);

        }
            public void ReturntoZero () {

                servo.setPosition(StartingAngle);


            }
            // todo add robot tilting code
        }
