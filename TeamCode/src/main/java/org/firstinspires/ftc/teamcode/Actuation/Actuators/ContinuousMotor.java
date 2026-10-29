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
    private boolean isVelocityMode=false;
    private double RampUpValue =0;
    private double RampUpStepSize=10;
    private double RampDownStepSize=10;

    public enum Direction {
        FORWARD,
        REVERSE,
        OTHER
    }

     private Direction direction;
    public ContinuousMotor(HardwareMap hardwareMap, String MotorName, double power) {

        motor = hardwareMap.get(DcMotorEx.class, MotorName);
        motor.setDirection(DcMotorSimple.Direction.FORWARD);
        motor.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        this.Power = norm(power);
        this.isVelocityMode=false;

    }
    // overloading constructor for velocity input as an option.
    public ContinuousMotor(HardwareMap hardwareMap, String MotorName ,double velocity, String inputmode) {

        if(Objects.equals(inputmode, "velocity")) {
            motor = hardwareMap.get(DcMotorEx.class, MotorName);
            motor.setDirection(DcMotorSimple.Direction.FORWARD);
            motor.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
            this.velocity = velocity;
            this.isVelocityMode=true;

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
        if(isVelocityMode){

            motor.setVelocity(velocity);
        }else {
            motor.setPower(Power);
        }
        isrunning = true;
    }

    public void StopMotor() {
        if(isVelocityMode){
            motor.setVelocity(0);
        }else {
            motor.setPower(0);
        }
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

    public boolean runForward(){
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
        return true;
    }

    public boolean runReverse(){
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
        return true;
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
