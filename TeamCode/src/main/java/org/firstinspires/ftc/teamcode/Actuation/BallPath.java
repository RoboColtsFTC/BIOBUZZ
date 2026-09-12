package org.firstinspires.ftc.teamcode.Actuation;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Actuation.Actuators.ContinuousMotor; //forgot ; here
import org.firstinspires.ftc.teamcode.Actuation.Actuators.ContinuousServo;


@TeleOp(name="BallPath", group="Actuation")
public class BallPath extends LinearOpMode {

    public ContinuousServo servo0;
    public ContinuousServo servo1;
    public ContinuousMotor motor0;

    @Override
    public void runOpMode() throws InterruptedException {
        servo0 = new ContinuousServo(hardwareMap, "IntakeRight", 20);
        servo1 = new ContinuousServo(hardwareMap, "IntakeLeft", 20);
        motor0 = new ContinuousMotor(hardwareMap, "IntakeRoller", 20);

        telemetry.addLine("Push D-pad Up to Intake balls or D-pad Down to Expel balls");
        /*Wait for driver to say they're ready*/
        waitForStart();

        while (opModeIsActive()) {

            /*Intake roller(motor) and servos*/
            /*Intake ball*/
            if (gamepad1.dpad_up) {
                motor0.StartMotor();
                servo0.Start();
                servo0.Forward();
                servo1.Start();
                servo1.Reverse();
              /* public ContinuousMotor(HardwareMap hardwareMap, String MotorName, double power){

                    motor = hardwareMap.get(DcMotorEx.class, motor0);
                    motor.setDirection(DcMotorSimple.Direction.FORWARD);
                    this.Power = norm(power);
                } ?????? */
            }



                /*Expel ball*/
            else if (gamepad1.dpad_down) {
                    servo0.Start();
                    servo0.Reverse();
                    servo1.Start();
                    servo1.Forward();
                }
                /*Shoot ball*/


                /*Spin spinner*/

            }

        }
    }
//missing -> }


