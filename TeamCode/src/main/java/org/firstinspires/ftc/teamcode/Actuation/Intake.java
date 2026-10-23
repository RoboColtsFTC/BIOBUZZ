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



public ColorDetector colordetector;

public Actuators actuators;

public  List<DetColor> colorPos;

public static State Currentstate=State.Empty;



public LinearOpMode opmode;
private final ElapsedTime ControlFeedTimer = new ElapsedTime();
private final ElapsedTime KickerTimer = new ElapsedTime();
private static boolean auto;
    public Intake(LinearOpMode opmode, Actuators actuators, List<DetColor> colorPos){ // Cunstructor
       colordetector=new ColorDetector(opmode);
        this.colorPos =colorPos;
        Currentstate=State.Empty;
        this.opmode=opmode;
        this.actuators=actuators;
        ControlFeedTimer.reset();
        KickerTimer.reset();

    }



    public Action  Intake_auto(){
        return new Action(){
            @Override
            public boolean run(@NonNull TelemetryPacket telemetryPacket) {
                //autoload=true;
                auto=true;
                Intake_run();


                return auto;
            }



        };
    }

    public void run()
    {
        Intake_run();
        opmode.telemetry.addData("ColorPosition",colorPos);

    }

    public void Intake_run(){
        // TODO
        // Intake code goes here to set the motors.
        // We talked about having it activated to go foraward and reverse with two motors.
        // Left bumper top and bottom on drivers gampad.
        // please initialize two motors in Actuatorcontrol for this function.



    }



}
