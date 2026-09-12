package org.firstinspires.ftc.teamcode.Actuation.Actuators;


import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import java.util.Objects;

public class ContinuousMotor {
    private double velocity=0;
    private double Power = 0;
    private DcMotorEx motor;
    private boolean isrunning = false;


    public ContinuousMotor(HardwareMap hardwareMap, String MotorName, double power) {

        motor = hardwareMap.get(DcMotorEx.class, MotorName);
        motor.setDirection(DcMotorSimple.Direction.FORWARD);
        this.Power = norm(power);
    }
    // overloading constructor for velocity input as an option.
    public ContinuousMotor(HardwareMap hardwareMap, String MotorName ,double velocity, String inputmode) {

        if(Objects.equals(inputmode, "velocity")) {
            motor = hardwareMap.get(DcMotorEx.class, MotorName);
            motor.setDirection(DcMotorSimple.Direction.FORWARD);
            this.velocity = norm(velocity);
        }
    }
    public boolean isMotorAtVelocity(){
        return motor.getVelocity()==velocity;

    }
    public void SetVelocity(double velocity){
        this.velocity=velocity;
        motor.setVelocity(velocity);
    }
    public double GetVelocity(){
       return motor.getVelocity();
    }
    public void StartMotor() {
        motor.setPower(Power);
        isrunning = true;
    }

    public void StopMotor() {
        motor.setPower(0);
        isrunning = false;
    }

    public void SetPower(double power) {
        if (isrunning) {
            this.Power = norm(power);
            motor.setPower(norm(power));

        } else {
            this.Power = norm(power);
        }

    }

    public void SetForward(){
           motor.setDirection(DcMotorSimple.Direction.FORWARD);
}
    public void SetReverse(){
        motor.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    public double norm(double value) {

        if (Math.abs(value) > 1) {
            value = 1;
        } else if (Math.abs(value) < 0) {
            value = 0;
        }
        return value;


    }


}
