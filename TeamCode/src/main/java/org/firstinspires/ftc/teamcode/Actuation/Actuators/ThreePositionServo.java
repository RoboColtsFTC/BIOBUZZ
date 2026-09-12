package org.firstinspires.ftc.teamcode.Actuation.Actuators;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;


public class ThreePositionServo {
    public Servo servo;
    private double PositionOne;  //Degrees
    private double PositionTwo;  //Degrees
    private double PositionThree;  //Degrees
    private final double MaxAngle;  //Degrees

    private double PosOneNorm;
    private double PosTwoNorm;
    private double PosThreeNorm;

    private double offset=0; //Degrees

public ThreePositionServo(HardwareMap hardwareMap, String ServoName,double PositionOne, double PositionTwo,double PositionThree,double MaxAngle,double offset){
    servo = hardwareMap.get(Servo.class, ServoName);

    this.PositionOne=PositionOne;
    this.PositionTwo=PositionTwo;
    this.PositionThree=PositionThree;
    this.MaxAngle=MaxAngle;
    this.offset=offset;
    PosOneNorm=(PositionOne+offset)/MaxAngle;
    PosTwoNorm=(PositionTwo+offset)/MaxAngle;
    PosThreeNorm=(PositionThree+offset)/MaxAngle;
    servo.setPosition(PosOneNorm);


}

public void setPosition(int Position) {

    switch (Position) {
        case 1:
            servo.setPosition(PosOneNorm);
            break;
        case 2:
            servo.setPosition(PosTwoNorm);
            break;
        case 3:
            servo.setPosition(PosThreeNorm);
            break;
        default:
            break;

    }
}
public void setAngle(double PositionOne, double PositionTwo,double PositionThree){
    this.PositionOne=PositionOne;
    this.PositionTwo=PositionTwo;
    this.PositionThree=PositionThree;
    PosOneNorm=PositionOne/MaxAngle;
    PosTwoNorm=PositionTwo/MaxAngle;
    PosThreeNorm=PositionThree/MaxAngle;

}


    public void setOffset(double offset){  //offset is in degree
        this.offset=offset;
        PosOneNorm=(PositionOne+offset)/MaxAngle;
        PosTwoNorm=(PositionTwo+offset)/MaxAngle;
        PosThreeNorm=(PositionThree+offset)/MaxAngle;
    }

}
