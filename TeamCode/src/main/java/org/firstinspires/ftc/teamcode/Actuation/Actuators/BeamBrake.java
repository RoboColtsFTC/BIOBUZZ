package org.firstinspires.ftc.teamcode.Actuation.Actuators;

import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

public class BeamBrake {
    private DigitalChannel BeamBrake;
    private final ElapsedTime Hysteresis = new ElapsedTime();
    private double Hysteresis_ms =10;

    private enum State {
        detection,
        none
    }
    private State state = State.none;

    public BeamBrake(HardwareMap hardwareMap, String MotorName){
        BeamBrake = hardwareMap.get(DigitalChannel.class, "BeamBreak");
        BeamBrake.setMode(DigitalChannel.Mode.INPUT);
    }


    public boolean IsDetected(){

        if(BeamBrake.getState() & state==State.none){

            Hysteresis.reset();
            state=State.detection;
            return false;
        }else if (BeamBrake.getState() & state==State.detection & Hysteresis.milliseconds()>Hysteresis_ms){
            return true;

        } else {
            state=State.none;
            return false;
        }
    }

    public void SetHysteresis_ms(double time_ms){
        this.Hysteresis_ms = time_ms;
    }




}
