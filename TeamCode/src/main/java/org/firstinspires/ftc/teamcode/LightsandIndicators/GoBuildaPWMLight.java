package org.firstinspires.ftc.teamcode.LightsandIndicators;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.hardwareMap;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;


public class  GoBuildaPWMLight {
    Servo PWMLight;


    public GoBuildaPWMLight(HardwareMap hardwaremap, String LightName){

        PWMLight=hardwaremap.get(Servo .class,LightName);


    }

    public void SetColor(double Value){

        PWMLight.setPosition(Value);
    }


}
