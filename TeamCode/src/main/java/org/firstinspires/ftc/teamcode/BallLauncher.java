package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.telemetry;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Gamepad;

public class BallLauncher {

    private LinearOpMode opMode;
    private  Gamepad gamepad2;

    DcMotor motor;
    double Power = 0 ;
    double Increment = .01;
    double MaxPower = 1;
    boolean dpad_up_Debounce = false;
    boolean dpad_down_Debounce = false;

public BallLauncher(LinearOpMode opMode){

    this.opMode=opMode;
    this.gamepad2=opMode.gamepad2;
    motor = opMode.hardwareMap.get(DcMotorEx.class, "launcher_motor");
    Power=30;



}
public void run(){


        motor.setPower(Power);


}

public void UpdateTelemetry() {

    telemetry.addData("Ball Launcher Motor Power", "%5.2f",Power);
    telemetry.update();
}

}
