package org.firstinspires.ftc.teamcode.Actuation;


import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.teamcode.Actuation.ActuatorControl.Actuators;
import org.firstinspires.ftc.teamcode.Perception.ColorDetector;
import org.firstinspires.ftc.teamcode.Perception.ColorDetector.DetColor;



import java.util.List;
@Config
public class Intake {
public enum State {
    Empty,
    LoadOne,
    LoadTwo,
    LoadThree,
    Loaded

}





public Actuators actuators;



public LinearOpMode opmode;

private static boolean auto;

private boolean Forward_Debounce = false;
private boolean Reverse_Debounce= false;
private boolean Stop_Debounce = false;

    public Intake(LinearOpMode opmode, Actuators actuators){ // Cunstructor

        this.opmode=opmode;
        this.actuators=actuators;


    }



    public Action  Intake_auto(){
        return new Action(){
            @Override
            public boolean run(@NonNull TelemetryPacket telemetryPacket) {
                //autoload=true;
                auto=true;
                Intake_run_Forward();


                return auto;
            }



        };
    }

    public void run()
    {


        if (opmode.gamepad1.left_bumper& !Forward_Debounce) {

            Intake_run_Forward();
        }
        Forward_Debounce=opmode.gamepad1.left_bumper;


        if (opmode.gamepad1.left_trigger_pressed & ! Reverse_Debounce) {

            Intake_run_Reverse();
        }
        Reverse_Debounce=opmode.gamepad1.left_trigger_pressed;


        if ((opmode.gamepad1.right_bumper & !Stop_Debounce)) {//||actuators.BeamBrake.getState()

            Intake_run_Stop();
        }
        Stop_Debounce=opmode.gamepad1.right_bumper;
    }

    public void Intake_run_Forward(){

        actuators.IntakeMotor_One.runForward();
        actuators.FeedMotor.runForward();

    }
    public void Intake_run_Reverse() {
        actuators.IntakeMotor_One.runReverse();
    }

    public void Intake_run_Stop() {
        actuators.IntakeMotor_One.StopMotor();
        actuators.IntakeMotor_Two.StopMotor();
    }

}
