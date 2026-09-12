package org.firstinspires.ftc.teamcode.Actuation.TestCode;


import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.Actuation.Actuators.ContinuousMotor;
import org.firstinspires.ftc.teamcode.Actuation.Actuators.DualMotor;

@TeleOp(name = "TestDualMotor", group = "Test")
public class TestDualMotor extends LinearOpMode {

    DcMotor motor;
    double velocity = 0 ;
    double Increment = 10;
    double Maxvelocity = 28*5800*60;


public DualMotor continousmotor;


    @Override
    public void runOpMode() {

        continousmotor=new DualMotor(hardwareMap,"LauncherMotor1","LauncherMotor2",0);

        // Debounce Switches
        boolean dpad_up_Debounce = false;
        boolean dpad_down_Debounce = false;


        // Wait for the start button
        telemetry.addData(">", "Press Start to start Ball Launcher.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive() ) {

            if (gamepad2.dpad_up & !dpad_up_Debounce) {

                velocity += Increment;
            }
            dpad_up_Debounce=gamepad2.dpad_up; // Debouncing Game Controller

            if (gamepad2.dpad_down & !dpad_down_Debounce  ) {

                velocity -= Increment;
            }
            dpad_down_Debounce=gamepad2.dpad_down; // Debouncing Game Controller

            if (Math.abs(velocity)>Maxvelocity){
                if (velocity>0) {
                    velocity = Maxvelocity;
                }
                else {
                    velocity=-Maxvelocity;
                }

            }

            continousmotor.SetVelocity(velocity);
            continousmotor.StartMotor();

            telemetry.addData(">", "Press DPadUp to Increase Press DPadDown to Degrease.");
            telemetry.addData("Motor Power", "%5.2f",velocity);
            telemetry.addData("Motor Power Velocitym1", "%5.2f",continousmotor.GetVelocitym1());
            telemetry.addData("Motor Power Velocitym2", "%5.2f",continousmotor.GetVelocitym2());
            telemetry.addData(">", "Press Stop to end test." );
            telemetry.update();
        }


        }




    }



