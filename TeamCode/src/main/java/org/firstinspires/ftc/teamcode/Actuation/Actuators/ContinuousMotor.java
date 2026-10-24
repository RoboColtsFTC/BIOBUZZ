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

    public enum Direction {
        FORWARD,
        REVERSE,
        OTHER
    }

     private Direction direction;
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
    public Direction GetDirection(){

        if (motor.getDirection() == DcMotorEx.Direction.FORWARD) {
               return Direction.FORWARD;
        } else if (motor.getDirection() == DcMotorEx.Direction.REVERSE) {
               return Direction.REVERSE;
        }else{
            return Direction.OTHER;
        }
    }



    public void SetForward(){
           motor.setDirection(DcMotorSimple.Direction.FORWARD);
}
    public void SetReverse(){
        motor.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    public void runForward(){
        if (GetDirection()==Direction.FORWARD){
            this.StartMotor();
        } else{
            if (isrunning){
                this.StopMotor();
                motor.setDirection(DcMotorSimple.Direction.FORWARD);
                this.StartMotor();
            }else{
                motor.setDirection(DcMotorSimple.Direction.FORWARD);
                this.StartMotor();
            }

        }
    }

    public void runReverse(){
        if (GetDirection()==Direction.REVERSE){
            this.StartMotor();
        } else{
            if (isrunning){
                this.StopMotor();
                motor.setDirection(DcMotorSimple.Direction.REVERSE);
                this.StartMotor();
            }else{
                motor.setDirection(DcMotorSimple.Direction.REVERSE);
                this.StartMotor();
            }

        }
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
