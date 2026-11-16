# Launcher PID Tuning

How to tune the velocity control on the two launcher flywheels:
**Nectar** (`LaunchMotor 1`) and **Pollen** (`LaunchMotor 2`).

With velocity control on, the Control Hub runs a PID loop on each flywheel that holds it at
a target RPM. A tuned loop keeps the speed the same as the battery drops and brings the
flywheel back up to speed quickly after each ball goes through, so shots stay consistent.

---

## 1. Where the settings are

Everything is in FTC Dashboard and changes live, with the flywheels running.

| Dashboard entry | Setting | What it does |
|---|---|---|
| `ActuatorControl` | `LAUNCH_VELOCITY_CONTROL` | `true` = velocity (PID) control. `false` = old power control. |
| `ActuatorControl → param` | `Nectar_Target_RPM`, `Pollen_Target_RPM` | Speed each flywheel holds |
| | `Launch_Use_Custom_PIDF` | `false` = the hub's built-in PIDF. `true` = use the values below. |
| | `Nectar_P` `Nectar_I` `Nectar_D` `Nectar_F` | Nectar PIDF |
| | `Pollen_P` `Pollen_I` `Pollen_D` `Pollen_F` | Pollen PIDF |
| | `Launch_RampUp_RPMps`, `Launch_RampDown_RPMps` | How fast the target ramps up and down (RPM per second) |
| | `Launch_Ready_Fraction` | A shot waits until each flywheel is at this fraction of its target (0.95 = 95%) |
| | `Spinup_Timeout_ms` | Shoot anyway after this long, so a bad encoder can't block shooting |
| | `Launchmmotor_One_Power`, `Launchmmotor_Two_Power` | Power used when `LAUNCH_VELOCITY_CONTROL` is `false`, and for measuring top speed (step 3) |

**Dashboard changes are lost when the robot app restarts.** When you're done, copy the
values into `Actuation/ActuatorControl.java` (step 9).

---

## 2. Setup

1. **Charge the battery fully.** Tune on a fresh battery, then check on a used one.
2. **Put the robot on a stand or blocks**, and keep hands clear of the flywheels.
3. Connect a laptop to the robot's Wi-Fi and open `http://192.168.43.1:8080/dash`.
4. On the Driver Station, start **Red Teleop** or **Blue Teleop**. Tune in teleop: auto
   doesn't send telemetry to Dashboard.
5. In Dashboard, open the **Graph** panel and select these four lines:
   `Nectar vel`, `Nectar setpoint`, `Pollen vel`, `Pollen setpoint`.
   - `setpoint` is the speed being asked for.
   - `vel` is the speed measured by the encoder.
   - Tuning means making `vel` follow `setpoint` closely.
   - If `Nectar vel` shows as negative, read its size (ignore the minus sign).

**Gamepad 2 controls:** **A** = flywheels on, **B** = flywheels off,
**right trigger** = shoot.

---

## 3. Check the encoders first

Velocity control depends on the encoders. **If an encoder cable is unplugged, the hub
thinks the wheel is stopped and drives it at full power.**

1. Set `LAUNCH_VELOCITY_CONTROL` to `false` (power control).
2. Press **A**. Both `Nectar vel` and `Pollen vel` should rise to a few thousand RPM.
3. If either one stays at 0, fix that encoder cable before going on.

---

## 4. Find F (feedforward)

F does most of the work: it sends about the right power for the target speed straight away.
P and I then only fix the small error that's left.

**Measure the top speed of each flywheel**

1. Keep `LAUNCH_VELOCITY_CONTROL` = `false`.
2. Set `Launchmmotor_One_Power` = `1` and `Launchmmotor_Two_Power` = `1`.
3. Press **A**, wait about 3 seconds for the speed to settle, and write down `Nectar vel` and
   `Pollen vel`.
4. Press **B**. Set the powers back to `.73` and `.72`.

**Work out F for each flywheel**

```
ticks per second = RPM × 28 ÷ 60
F                = 32767 ÷ ticks per second
```

Example: full power measures 5000 RPM → 5000 × 28 ÷ 60 = 2333 ticks/sec →
F = 32767 ÷ 2333 = **14.0**

**Check F on its own**

1. Set `LAUNCH_VELOCITY_CONTROL` = `true` and `Launch_Use_Custom_PIDF` = `true`.
2. Set P, I and D to `0` for both flywheels, and set each F to your number.
3. Press **A** and watch the graph once it settles:

| What you see | Change |
|---|---|
| `vel` settles a little **under** `setpoint` (a few %) | This is right. Move on to P. |
| `vel` settles **over** `setpoint` | F too big: lower it by about 5% |
| `vel` settles **far under** `setpoint` (more than 10%) | F too small: raise it by about 5% |

---

## 5. Add P (proportional)

P pushes harder the further the speed is from the target. It does most of the work bringing
the flywheel back up after each ball.

1. Start with **P = F ÷ 10** (for example, F = 14 → P = 1.4).
2. Press **A**, then shoot a few balls with the **right trigger**. On the graph each ball
   makes a dip in `vel`. Watch how quickly it climbs back to `setpoint`.
3. Adjust:

| What you see | Change |
|---|---|
| Slow to come back after each ball | Raise P by about 25% |
| `vel` overshoots and wobbles up and down before settling | P too big: lower it by about 30% |
| Fast recovery, little or no overshoot | Done. Move on to I. |

Tune Nectar and Pollen separately. The flywheels can be different, so their best values can
be different too.

---

## 6. Add I (integral), only if needed

I removes an error that stays: when `vel` settles a little under `setpoint` and stays there.

1. If `vel` already settles right on `setpoint`, leave **I = 0**.
2. Otherwise start with **I = P ÷ 10**.
3. If `vel` starts overshooting or swinging slowly up and down, I is too big: halve it.

## 7. D (derivative)

Leave **D = 0**. Flywheels almost never need it, and it makes the noisy encoder reading
worse.

---

## 8. Ramps, ready check and target speeds

**Ramps**
- `Launch_RampUp_RPMps` (default 6000) controls how fast the setpoint rises. If the battery
  voltage drops a lot or the robot browns out when the flywheels start, lower it.
- `Launch_RampDown_RPMps` (default 3000) controls how fast it slows down.

**Ready check**
- A shot feeds once both flywheels reach `Launch_Ready_Fraction` × target (default 95%).
- Raise it (for example 0.97) if the first ball is shot short.
- Lower it if shots wait too long to start.

**Target speeds**
- Once the PID holds speed well, set `Nectar_Target_RPM` and `Pollen_Target_RPM` for
  accuracy.
- Shoot from your usual spots and adjust in steps of about 50 RPM.

---

## 9. Save the values

Copy your final numbers into `Actuation/ActuatorControl.java`, in the `Params` class:

```java
public double Nectar_Target_RPM = 3700;
public double Pollen_Target_RPM = 3500;
public boolean Launch_Use_Custom_PIDF = true;   // true once tuned
public double Nectar_P = 1.4;
public double Nectar_I = 0;
public double Nectar_D = 0;
public double Nectar_F = 14;
public double Pollen_P = 1.4;
// ... and the rest
```

Rebuild and deploy, then check that the robot behaves the same without Dashboard connected.
Dashboard isn't allowed during matches, so the code values are what you compete with.

---

## 10. Check with the shot logs

Every shot is saved to `/sdcard/FIRST/ShotLogs/shot_YYYYMMDD_HHMMSS.csv` on the Control Hub
(switch it on or off with `ShotLogger → ENABLED` in Dashboard).

1. Copy the files off the robot with Android Studio's **Device Explorer**, or from a terminal:
   ```bash
   adb pull /sdcard/FIRST/ShotLogs
   ```
2. Open a file in Excel or Google Sheets and make a line chart of `t_ms` against
   `nectar_rpm` and `nectar_set_rpm` (and the same for Pollen).
3. Rows where `state` is `WAIT` are while balls are going through. Look at how deep each dip
   is and how quickly it recovers.
4. After each shot, the Driver Station also shows the lowest RPM while feeding.

**Good tuning** means small dips that recover before the next ball, the same from shot to
shot, and the same on a full and a used battery (check the `battery_v` column).

---

## 11. Troubleshooting

| Problem | Likely cause | Fix |
|---|---|---|
| A flywheel jumps to full speed and stays there | Encoder unplugged, or reading 0 | Check the cable (section 3). Use `LAUNCH_VELOCITY_CONTROL` = `false` until it's fixed. |
| `vel` never gets near `setpoint` | Target above the motor's top speed, or F far too small | Lower the target below the top speed you measured, or raise F |
| Speed wobbles up and down all the time | P or I too big | Halve I, then lower P |
| Every shot waits `Spinup_Timeout_ms` before feeding | Flywheel never reaches the ready fraction | Lower `Launch_Ready_Fraction`, or fix F/I so it reaches the target |
| Shots got worse after tuning | Values only set in Dashboard and lost on restart | Copy them into `ActuatorControl.java` (section 9) |
| Telemetry shows the PIDF didn't change | `Launch_Use_Custom_PIDF` is still `false` | Set it to `true` |
| Need to go back to how it was before | | `LAUNCH_VELOCITY_CONTROL` = `false` uses the old power control |
