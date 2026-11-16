package org.firstinspires.ftc.teamcode.Actuation;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.teamcode.Actuation.ActuatorControl.Actuators;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Records the launch motor speeds during every shot to a CSV file on the Control Hub.
 *
 * A new file starts when a shot starts and closes TAIL_MS after the shot finishes,
 * so you can see the flywheels recover. Files are saved in /sdcard/FIRST/ShotLogs/,
 * named shot_YYYYMMDD_HHMMSS.csv. One row per loop.
 *
 * Columns:
 *   t_ms           time since the shot started
 *   state          shooter state: START (spin-up, then feeder ramp), WAIT (feeding),
 *                  REQUEUE (feeder backward), STOP (feeder ramping down), SEQCOMPLETE (tail)
 *   start_state    START = waiting for the flywheels to reach speed, WAIT = feeder starting
 *   nectar_rpm / pollen_rpm         measured flywheel speed (encoder)
 *   nectar_set_rpm / pollen_set_rpm speed being commanded (velocity control; 0 in power control).
 *                                   Compare with the measured speed to tune the PIDF.
 *   nectar_power / pollen_power     power being sent to the flywheels
 *   feed_power     power being sent to the feeder
 *   battery_v      battery voltage
 *
 * Copy the files off with Android Studio's Device Explorer or
 * "adb pull /sdcard/FIRST/ShotLogs", then open them in Excel or Google Sheets.
 */
@Config
public class ShotLogger {

    // Turn recording on or off (also from FTC Dashboard).
    public static boolean ENABLED = true;
    // Keep recording this long (ms) after the shot finishes, to see the flywheels recover.
    public static double TAIL_MS = 500;

    private final LinearOpMode opmode;
    private final Actuators actuators;
    private final VoltageSensor battery;

    private BufferedWriter writer = null;
    private String fileName = "";
    private final ElapsedTime shotTime = new ElapsedTime();
    private final ElapsedTime tailTime = new ElapsedTime();
    private boolean inTail = false;

    // Summary of the last shot, shown on telemetry.
    private double nectarMinFeeding = Double.MAX_VALUE;
    private double pollenMinFeeding = Double.MAX_VALUE;
    private String lastSummary = "no shot recorded yet";

    public ShotLogger(LinearOpMode opmode, Actuators actuators) {
        this.opmode = opmode;
        this.actuators = actuators;
        VoltageSensor sensor = null;
        try {
            sensor = opmode.hardwareMap.voltageSensor.iterator().next();
        } catch (Exception e) {
            // No voltage sensor found; the battery column will be blank.
        }
        this.battery = sensor;
    }

    /**
     * Call once every loop, after the shooter state machine has run.
     * shotActive is true while a shot is in progress.
     */
    public void update(boolean shotActive, LaunchGamePeace.State state, LaunchGamePeace.State startState) {
        if (!ENABLED) {
            close();
            return;
        }

        if (shotActive) {
            inTail = false;
            if (writer == null) {
                open();
            }
        } else if (writer != null) {
            if (!inTail) {
                inTail = true;
                tailTime.reset();
            } else if (tailTime.milliseconds() > TAIL_MS) {
                close();
            }
        }

        if (writer != null) {
            writeRow(state, startState);
        }

        opmode.telemetry.addData("Shot log", writer != null ? "recording " + fileName : lastSummary);
    }

    private void open() {
        try {
            File dir = new File(AppUtil.FIRST_FOLDER, "ShotLogs");
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IOException("could not create " + dir);
            }
            String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            fileName = "shot_" + stamp + ".csv";
            writer = new BufferedWriter(new FileWriter(new File(dir, fileName)));
            writer.write("t_ms,state,start_state,nectar_rpm,pollen_rpm,nectar_set_rpm,pollen_set_rpm,"
                    + "nectar_power,pollen_power,feed_power,battery_v\n");
            shotTime.reset();
            nectarMinFeeding = Double.MAX_VALUE;
            pollenMinFeeding = Double.MAX_VALUE;
        } catch (IOException e) {
            // Never let logging break shooting: turn it off and say why.
            writer = null;
            ENABLED = false;
            lastSummary = "logging off, error: " + e.getMessage();
        }
    }

    private void writeRow(LaunchGamePeace.State state, LaunchGamePeace.State startState) {
        double nectarRpm = Math.abs(actuators.LaunchMotor_One.GetVelocity());
        double pollenRpm = Math.abs(actuators.LaunchMotor_Two.GetVelocity());

        // Track the lowest speed while balls are going through (the dip each ball causes).
        boolean feeding = state == LaunchGamePeace.State.WAIT
                || (state == LaunchGamePeace.State.START && startState == LaunchGamePeace.State.WAIT);
        if (feeding) {
            nectarMinFeeding = Math.min(nectarMinFeeding, nectarRpm);
            pollenMinFeeding = Math.min(pollenMinFeeding, pollenRpm);
        }

        String volts = battery != null ? String.format(Locale.US, "%.2f", battery.getVoltage()) : "";
        String row = String.format(Locale.US, "%.0f,%s,%s,%.0f,%.0f,%.0f,%.0f,%.3f,%.3f,%.3f,%s\n",
                shotTime.milliseconds(),
                state,
                startState,
                nectarRpm,
                pollenRpm,
                actuators.LaunchMotor_One.GetSetpointRPM(),
                actuators.LaunchMotor_Two.GetSetpointRPM(),
                actuators.LaunchMotor_One.GetPower(),
                actuators.LaunchMotor_Two.GetPower(),
                actuators.FeedMotor.GetPower(),
                volts);
        try {
            writer.write(row);
            // Flush every row so the data is saved even if the OpMode is stopped mid-shot.
            writer.flush();
        } catch (IOException e) {
            close();
            ENABLED = false;
            lastSummary = "logging off, error: " + e.getMessage();
        }
    }

    private void close() {
        if (writer == null) {
            return;
        }
        try {
            writer.close();
        } catch (IOException e) {
            // Nothing more to do; the rows were already flushed.
        }
        writer = null;
        inTail = false;
        if (nectarMinFeeding == Double.MAX_VALUE) {
            lastSummary = fileName + " (no feeding recorded)";
        } else {
            lastSummary = String.format(Locale.US, "%s  min while feeding: Nectar %.0f, Pollen %.0f RPM",
                    fileName, nectarMinFeeding, pollenMinFeeding);
        }
    }
}
