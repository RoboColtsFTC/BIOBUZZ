package org.firstinspires.ftc.teamcode.Actuation.Actuators;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.ServoImplEx;

@Config
public class SpindexerControl {
    public ServoImplEx servo;

    public static class Positions{
        public double Offset =0;
        public  double Position1 = 96;
        public  double Position2 = Position1+118;
        public double Position3 = Position2+115;
        public double Position4 = 60+93;
        public double Position5 = Position4+118;
        public double Position6 = Position5+114;
        public double MaxAngle= 360*1.7;

    }

    private static class normPositions{
        public double Offset =0;
        public double normPos1;
        public double normPos2;
        public double normPos3;
        public double normPos4;
        public double normPos5;
        public double normPos6;

    }
    public static enum CurrentState{
        posloadone,
        posloadTwo,
        posloadThree,
        posLaunchOne,
        posLaunchTwo,
        posLaunchThree
    }

    public static CurrentState curState;
    public static normPositions normPos = new normPositions();
    public static Positions Pos = new Positions();


public SpindexerControl(HardwareMap hardwareMap, String ServoName){
    servo = (ServoImplEx)hardwareMap.get(Servo.class, ServoName);
    servo.setPwmRange(new PwmControl.PwmRange(800, 2200));



    normPos.normPos1=(Pos.Position1+Pos.Offset)/Pos.MaxAngle;
    normPos.normPos2=(Pos.Position2+Pos.Offset)/Pos.MaxAngle;
    normPos.normPos3=(Pos.Position3+Pos.Offset)/Pos.MaxAngle;
    normPos.normPos4=(Pos.Position4+Pos.Offset)/Pos.MaxAngle;
    normPos.normPos5=(Pos.Position5+Pos.Offset)/Pos.MaxAngle;
    normPos.normPos6=(Pos.Position6+Pos.Offset)/Pos.MaxAngle;

   }


public void setPosition(int Position) {

    switch (Position) {
        case 1:
            servo.setPosition(normPos.normPos1);
            curState=CurrentState.posloadone;

            break;
        case 2:
            servo.setPosition(normPos.normPos2);
            curState=CurrentState.posloadTwo;
            break;
        case 3:
            servo.setPosition(normPos.normPos3);
            curState=CurrentState.posloadThree;
            break;
        case 4:
            servo.setPosition(normPos.normPos4);
            curState=CurrentState.posLaunchOne;
            break;
        case 5:
            servo.setPosition(normPos.normPos5);
            curState=CurrentState.posLaunchTwo;
            break;
        case 6:
            servo.setPosition(normPos.normPos6);
            curState=CurrentState.posLaunchThree;
            break;

        default:
            break;

    }
}
public void setPositions(Positions positions ){
    Pos=positions;
    normPos.normPos1=(Pos.Position1+Pos.Offset)/Pos.MaxAngle;
    normPos.normPos2=(Pos.Position2+Pos.Offset)/Pos.MaxAngle;
    normPos.normPos3=(Pos.Position3+Pos.Offset)/Pos.MaxAngle;
    normPos.normPos4=(Pos.Position4+Pos.Offset)/Pos.MaxAngle;
    normPos.normPos5=(Pos.Position5+Pos.Offset)/Pos.MaxAngle;
    normPos.normPos6=(Pos.Position6+Pos.Offset)/Pos.MaxAngle;

}

public CurrentState GetPosition(){

    return  curState;
}




}
