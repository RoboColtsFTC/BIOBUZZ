# RoboColts FTC — DECODE Team Code

This folder holds all of the RoboColts robot code for the **DECODE (2025–2026)** season.
Everything outside of `TeamCode/` (the `FtcRobotController/`, `MeepMeep/`, Gradle files,
and the root `README.md` release notes) is the stock *FIRST* Tech Challenge SDK and is
not modified by the team.

- **Package root:** `org.firstinspires.ftc.teamcode`
- **Robot control system:** Road Runner 1.0 (actions + trajectories), FTCLib (PID +
  mecanum kinematics), FTC VisionPortal (AprilTag), goBILDA Pinpoint odometry computer.
- **Game summary:** the robot intakes *artifacts* (GREEN and PURPLE balls), stores three
  of them in a rotating **spindexer**, reads each artifact's color with a triad of color
  sensors, and fires them from a dual-flywheel **launcher**. AprilTags on the goals are
  used for auto-aim and for reading the randomized *Obelisk* code (GPP / PGP / PPG).

---

## 1. High-level architecture

The competition code is organized around a single `Robot` object that owns three
subsystems. Every OpMode constructs a `Robot`, calls `waitForStart()`, then calls
`robot.runRobot()` once per loop.

```
LinearOpMode (BlueTeleop / RedTeleop / autos/*)
        │
        ▼
   Robot  ─────────────────────────────────────────────
        │ drivetrain        (Drivetrain)   field-centric mecanum + auto-aim
        │ actuatorcontrol   (ActuatorControl)  intake, spindexer, feed, launcher
        │ AprilTagPro       (AprilTag)      VisionPortal goal + Obelisk detection
        ▼
   runRobot() → drivetrain.run() + actuatorcontrol.run() + AprilTagPro.run()
```

`Robot` is toggled by boolean flags passed to its constructor, so the same class serves
TeleOp and Autonomous:

| Constructor argument | Meaning |
|---|---|
| `auto`            | `true` for Autonomous. Skips the TeleOp driver loop in `Drivetrain`. |
| `red`             | Alliance. Selects which goal AprilTag ID and bearing to track. |
| `useDrive`        | Build and run the `Drivetrain` subsystem. |
| `useBallLauncher` | Build and run `ActuatorControl` (intake / spindexer / launcher). |
| `useAprilTags`    | Build and run the `AprilTag` VisionPortal pipeline. |

There is a convenience constructor `Robot(opMode, red)` used by the autos — it is
equivalent to `Robot(opMode, auto=true, red, useDrive=false, useBallLauncher=true,
useAprilTags=true)` (the drivetrain in auto is driven by Road Runner's own
`MecanumDrive`, not by `Drivetrain`).

> **Note on shared state:** `Robot`, `ActuatorControl`, and the state-machine classes use
> a large number of `static` fields (`controlstate`, indicator lights, `colorPos`, the
> per-state enums). This means **only one `Robot` may be alive at a time** and the app
> should be fully restarted between runs if state looks stale.

---

## 2. Package map

| Package | Purpose |
|---|---|
| *(root)* `teamcode` | `Robot` wrapper + the two competition TeleOp OpModes + a few standalone bench-test OpModes. |
| `Actuation` | All game-piece handling: subsystem registry (`ActuatorControl`), load state machine (`LoadSpindexer`), launch state machine (`LaunchGamePeace`). |
| `Actuation.Actuators` | Thin, reusable hardware wrappers (servos, motors, spindexer). |
| `Actuation.TestCode` | One `@TeleOp` per actuator wrapper for bench tuning. Group `"Test"`. |
| `Perception` | AprilTag pipeline (`AprilTag`, `AprilTagData`) and the 3-sensor color voter (`ColorDetector`). |
| `Perception.Utilities` | Stock FTC camera-stream / frame-capture concept OpModes. |
| `LightsandIndicators` | `GoBuildaPWMLight` — a PWM-driven indicator light wrapper (driven as a `Servo`). |
| `drivetrain` | `Drivetrain` (team TeleOp driving) plus the Road Runner 1.0 quickstart (`MecanumDrive`, `PinpointLocalizer`, `messages/`, `tuning/`). |
| `drivetrain.PinPointIMU` | goBILDA Pinpoint driver + goBILDA's example OpModes. |
| `autos` | Eight Road Runner autonomous routines (near/far × reload/no-reload × red/blue). |

---

## 3. `Actuation` — game-piece subsystems

### `ActuatorControl`
Single initialization point for every actuator used in DECODE. Constructs each hardware
wrapper once and exposes them through the nested `Actuators` struct so the load and launch
state machines can share them. Also builds the three indicator lights and holds the
shared `colorPos` list (the detected color of each of the three stored artifacts).

`run()` simply ticks `loadSpindexer.run()` then `launchgamepeace.run()` every loop.

**`ActuatorControl.Params`** (live-editable via FTC Dashboard `@Config`) — key tuning values:

| Field | Meaning | Current value |
|---|---|---|
| `FeedKicker_First` / `_Second` | Feed-kicker servo angles (deg) | 20 / 160 |
| `LaunchKicker_First` / `_Second` | Launch-kicker servo angles (deg) | 103 / 0 |
| `IntakeMotor_Power` | Intake roller power | 1.0 |
| `FeedControl_Power` | Feed servo power | 0.5 |

**`ActuatorControl.ControlState`** — a top-level mutex shared by both state machines so
the robot only does one thing at a time: `ready`, `loading`, `launching`, `tilting`.

### `LoadSpindexer` — intake / indexing state machine
Fills the three spindexer slots one artifact at a time.

- Outer state (`LoadSpindexer.State`): `Empty → LoadOne → LoadTwo → LoadThree → Loaded`.
  Advanced by pressing **gamepad2.A** when idle (or automatically when an auto calls
  `LoadSpindexer_auto()`).
- Per-slot state (`GamePeaceLoadingState`): `StartIntake → Position → DetectColor →
  (kickball on slot 3) → Complete`.
- `DetectGamePeace` sub-machine runs `ColorDetector`; **gamepad2.B** is a manual
  "assume UNKNOWN and move on" override if a ball will not read.
- `ActuateFeed` / `ActuateFeedKicker` sequence the feed servos and kicker with
  `ElapsedTime` gates (mostly 120 ms steps, 500 ms for the kicker).
- Slot detection distance window: `1.35 cm … maxdist`, where `maxdist` is `4.8 cm` for
  slots 1–2 and `8 cm` for slot 3.
- Indicator light 3 (`SpindexerStateIndicator2`) shows load progress by color.

### `LaunchGamePeace` — launch state machine
Spins the flywheels up, then fires all three stored artifacts in order.

- `LauncherState`: `IDLE → MOTORSTARTUP → ACTIVELAUNCH`. Waits on
  `DualMotor.isMotorAtVelocity()` before firing.
- `LaunchSequence`: `LAUNCHPOSITION1 → 2 → 3`, driving `LoadGamePeace`
  (`SETPOSITION → ACTUATEKICKER → RETURNKICKERPOSITION`) for each shot with 120 ms gates.
- `LaunchOrder` defaults to spindexer positions `[6, 5, 4]`.
- Trigger from TeleOp:
  - **gamepad2.X** → far shot, flywheel velocity **1650** ticks/s.
  - **gamepad2.Y** → close shot, flywheel velocity **1400** ticks/s.
- Trigger from Autonomous: `Launch_Auto(velocity)` returns a Road Runner `Action`; the
  routine passes an explicit velocity (e.g. `1380` in `RedAutoNear`).
- On completion it resets `ControlState` to `ready`, `LoadSpindexer` to `Empty`, and
  clears `colorPos`.

### `Actuation.Actuators` — hardware wrappers

| Class | Hardware | Notes |
|---|---|---|
| `AngleServo` | `Servo` | Two calibrated angles (`First` / `Second`), stored in degrees, divided by `MaxAngle` (300°). Used for both kickers. |
| `ThreePositionServo` | `Servo` | Same idea with three angles + a runtime `offset`. General-purpose; not currently wired into the competition path. |
| `ContinuousMotor` | `DcMotorEx` | Power-mode intake motor with `norm()` clamping; overload accepts a velocity target. `SetReverse()` applied by `ActuatorControl`. |
| `DualMotor` | 2 × `DcMotorEx` | The launcher flywheels (`LauncherMotor1` forward, `LauncherMotor2` reversed). Velocity control; `isMotorAtVelocity()` gates the launch sequence. |
| `ContinuousServo` | `CRServo` | Direction + power wrapper with a `State` (running/idle) flag. |
| `FeedControl` | 2 × `ContinuousServo` (`FeedLeft` / `FeedRight`) | The belt/feed pair that moves artifacts into the spindexer. `Reverse()` applied at init. |
| `SpindexerControl` | `ServoImplEx` | The 6-index spindexer. PWM range widened to 800–2200 µs; positions 1–3 are load slots, 4–6 are launch slots. Angles defined in `Positions` (deg) over a `MaxAngle` of `360 × 1.7`. `@Config`. |

### Standalone / experimental files in `Actuation`
These compile but are **not** part of the `Robot` path — bench tests and stubs:

- `BallPath` (`@TeleOp` "BallPath") — early intake test with `ContinuousServo` /
  `ContinuousMotor`. Contains commented-out scratch notes and a known brace mismatch in
  history; treat as scratch.
- `PusherControl` — self-contained timed push servo (`"Launch Prep Servo"`), superseded
  by the kicker approach.
- `TiltRobot` / `ReverseIntake` — `// TODO` stubs for a future robot-tilt / reverse-intake
  mechanism.

---

## 4. `Perception`

### `AprilTag` + `AprilTagData`
Wraps a `VisionPortal` + `AprilTagProcessor` on **`Webcam 1`** (640×480, MJPEG),
using `AprilTagGameDatabase.getDecodeTagLibrary()` and 640×480 lens intrinsics
`fx=fy=543.913, cx=321.431, cy=223.814`.

`AprilTag.run()` reads detections every loop and routes them by ID:

| Tag ID | Meaning | Effect |
|---|---|---|
| 20 | Blue goal | `AprilTagData.SetBlue(range, bearing)`, sets `isBlueGoalAprilTagDetected` |
| 24 | Red goal | `AprilTagData.SetRed(range, bearing)`, sets `isRedGoalAprilTagDetected` |
| 21 / 22 / 23 | Obelisk code GPP / PGP / PPG | `AprilTagData.SetCode(...)` |

`AprilTagData` is the shared blackboard between vision and the drivetrain/lights:
`Red`/`Blue` goal `Range`+`Bearing`, the detected `Code`, and a `DetectionState` struct
of booleans. `detectionState` is reset each frame; tags whose metadata name contains
`"Obelisk"` are excluded from the pose telemetry.

`AprilTagLocalization` is the stock FTC AprilTag-localization sample kept for reference
(`@TeleOp` "Perception: AprilTag Localization").

### `ColorDetector` — 3-sensor color voter
Reads **`sensor_color1/2/3`** (`NormalizedColorSensor`, also `DistanceSensor`,
gain 8, internal LEDs forced on).

- Per sensor: normalize R/G/B by alpha, then classify `PURPLE` (`R < G` and `B > G`),
  `GREEN` (`G` dominant and `G > 0.06`), else `UNKNOWN`.
- `GetColor()` returns the **majority vote** (any 2 of 3 agreeing) as
  `DetColor.{GREEN, PURPLE, UNKNOWN}`.
- `colordetected()` returns `true` only when a valid GREEN/PURPLE vote exists **and** at
  least one distance reading is inside `1.35 cm … maxdist` (a ball is actually present).
- `LoadSpindexer` sets `maxdist` per slot and calls these each loop.

`SensorColor` is the standalone `@TeleOp` ("Sensor: Color") version of the same voter,
used for tuning thresholds. `ColorSensorAuto` is a separate one-sensor REV V3 demo
(`@Autonomous` "Color Sensor: REV V3", hardware name `colorSensor`) — not used by the
competition robot.

### `MovingAverage`
Fixed-window running mean with a staleness timeout (`DeltaT` ms) that clears the window
if samples stop arriving. Available as a smoothing helper.

---

## 5. `drivetrain`

### `Drivetrain` — team TeleOp driving
Field-centric mecanum driving built on `MecanumDrive` (Road Runner) for hardware +
localization and **FTCLib** `MecanumDriveKinematics` for the field-relative math.
Chassis is treated as square: `width = length = 0.3429 m`.

Heading comes from the Pinpoint localizer; `headingAngleRotated = heading + 180°` is the
frame used for `ChassisSpeeds.fromFieldRelativeSpeeds(...)`.

**Driver controls (gamepad1):**

| Input | Action |
|---|---|
| Left stick | Translate (field-relative), `maxSpeed = 1` |
| Right stick X | Rotate (`−x · π` rad/s) |
| A | Snap heading to `−45° + 180°` |
| X | Snap heading to `90°` |
| B | Snap heading to `−90°` |
| Right trigger > 0.5 (with any tag visible) | **Auto-aim** — `MecanumDrive.RotateTwardsGoal()` turns toward the alliance goal tag's bearing |
| Back | Reset Pinpoint IMU + zero pose |

Heading-hold uses an FTCLib `PIDController` (`kp=.025, ki=.0025, kd=0`, `@Config`).
`isfeildDrive` can be set `false` to fall back to robot-relative driving for
orientation checks. Telemetry is pushed both to the Driver Station and to FTC Dashboard.

Indicator lights 1 & 2 (`LockIndicator1/2`) turn on when the correct alliance goal tag is
in view.

### Road Runner quickstart (mostly stock)
`MecanumDrive`, `Localizer`, `Drawing`, `messages/`, and `tuning/` are the Road Runner
1.0 quickstart. Team modifications:

- Drive motors: **`drive_LF`, `drive_LR`, `drive_RR`, `drive_RF`**.
- Localizer is `PinpointLocalizer` (goBILDA Pinpoint, device name **`pinpoint`**, both
  encoder directions `REVERSED`); the dead-wheel localizers are commented out.
- `setWheelPowers(double[])` added so `Drivetrain` can command wheels directly.
- `RotateTwardsGoal()` added — a P-controller (`kp=.1`) that returns a turn power toward
  `Robot.TagData` goal bearing for the auto-aim feature.
- `Localizer` interface extended with `GetIMUStatus()`, `resetPinpointIMU()`,
  `getHeading()`, `getXticks()`, `getYticks()`.

`drivetrain.PinPointIMU` contains goBILDA's `GoBildaPinpointDriver` and its example
OpModes (`DriveToPoint`, `SensorGoBildaPinpointExample`, `SensorPinpointDriveToPoint`).

---

## 6. `LightsandIndicators`

`GoBuildaPWMLight` wraps a goBILDA RGB indicator light as a `Servo` — `SetColor(double)`
writes a PWM position (0–1) that the light maps to a color. Three are configured:
`light1`, `light2` (goal-lock indicators, driven by `Drivetrain`) and `light3`
(spindexer load-state indicator, driven by `LoadSpindexer`).
`TestGoBuildaPWMLight` (`@TeleOp`, group "Test") sweeps the value for calibration.

---

## 7. `autos`

Eight Road Runner `@Autonomous` routines, group `"Robot"`:

| Name | Alliance | Start heading | Notes |
|---|---|---|---|
| `RedAutoNear` / `BlueAutoNear` | Red / Blue | 45° | Strafe to launch spot, `Launch_Auto`, park. Active. |
| `RedAutoFar` / `BlueAutoFar` | Red / Blue | — | Far starting position variant. Active. |
| `RedAutoNearReload` / `BlueAutoNearReload` | Red / Blue | — | Adds an intake/reload cycle. **`@Disabled`.** |
| `RedAutoFarReload` / `BlueAutoFarReload` | Red / Blue | 90° | Launch → drive to intake → reload → return → launch → park. **`@Disabled`** (launch/load `stopAndAdd` calls currently commented out). |

Pattern for every routine:

```java
Pose2d beginPose = new Pose2d(0, 0, Math.toRadians(startHeading));
MecanumDrive drive = new MecanumDrive(hardwareMap, beginPose);
Robot robot = new Robot(this, /*red=*/true);       // auto=true, launcher+tags on
waitForStart();
drive.localizer.setPose(beginPose);
Actions.runBlocking(drive.actionBuilder(beginPose)
        .strafeToLinearHeading(new Vector2d(x, y), Math.toRadians(h))
        .stopAndAdd(robot.actuatorcontrol.launchgamepeace.Launch_Auto(velocity))
        .build());
```

`robot.actuatorcontrol.loadSpindexer.LoadSpindexer_auto()` and
`launchgamepeace.Launch_Auto(velocity)` are the two `Action`s that bridge the state
machines into a trajectory. Use **MeepMeep** (`/MeepMeep`) to visualize paths before
deploying.

---

## 8. OpMode index

### Competition
| OpMode (`name`) | Group | Class |
|---|---|---|
| Blue Teleop | Linear OpMode | `BlueTeleop` — `Robot(this,false,false,true,true,true)` (drive + launcher + tags) |
| Red Teleop | Linear OpMode | `RedTeleop` — `Robot(this,false,true,true,false,false)` (drive only) |
| RedAutoNear / BlueAutoNear / RedAutoFar / BlueAutoFar | Robot | `autos/*` |

> The `*Reload` autos are `@Disabled`. `RedTeleop` is currently configured drive-only
> (`useBallLauncher=false, useAprilTags=false`) — flip those flags to match `BlueTeleop`
> when the launcher is needed on the red side.

### Bench-test / utility
| OpMode | Group | Purpose |
|---|---|---|
| `Actuation:Intake Ball` | Actuation | Manual intake motor power ramp (`intake_motor`). |
| `BallPath` | Actuation | Early intake-path experiment. |
| `ServoTest` | Linear OpMode | Sweep a generic servo (`Servo0`). |
| `TestSpindexerControl`, `TestFeedControl`, `TestAngleServo`, `TestDualMotor`, `TestContinousMotor`, `TestContinuousServo`, `TestThreePositionServo`, `TestFeedKicker`, `TestTiltServo`, `TestIntakeMotor`, `TestLauncherMotor`, `TestPusherControl`, `ActuoatorControlTest` | Test | One per actuator wrapper. |
| `TestGoBuildaPWMLight` | Test | Indicator-light calibration. |
| `TestColorDetection` | Test | `ColorDetector` bring-up. |
| `Sensor: Color` / `Color Sensor: REV V3` | Sensor | Color-sensor tuning demos. |
| `Perception: AprilTag Localization`, `Utility: Camera Frame Capture`, `Concept: Webcam Stream`, `HighFrameRateOpMode` | Perception / Utility / Concept | Stock FTC vision samples. |
| `goBILDA® PinPoint Odometry Example`, `Pinpoint Navigation Example` | Pinpoint | Stock goBILDA samples. |
| Road Runner tuning OpModes | `tuning` | `LocalizationTest`, `ManualFeedbackTuner`, `SplineTest`, `TuningOpModes`. |

---

## 9. Robot configuration (hardware map)

Names the code expects in the active Robot Controller configuration:

**Control/Expansion Hub — motors**
| Config name | Device | Used by |
|---|---|---|
| `drive_LF` `drive_LR` `drive_RR` `drive_RF` | Mecanum drive motors | `MecanumDrive` |
| `IntakeMotor` | Intake roller | `ActuatorControl` → `ContinuousMotor` (reversed) |
| `LauncherMotor1` `LauncherMotor2` | Flywheels | `ActuatorControl` → `DualMotor` |
| `intake_motor` / `launcher_motor` | *(standalone bench OpModes only)* | `IntakeBall` / `BallLauncher` |

**Servos / CR servos**
| Config name | Device | Used by |
|---|---|---|
| `Spindexer` | `ServoImplEx`, PWM 800–2200 | `SpindexerControl` |
| `FeedKicker` `LaunchKicker` | Servo (angle) | `AngleServo` |
| `FeedLeft` `FeedRight` | CR servo | `FeedControl` |
| `light1` `light2` `light3` | goBILDA PWM light (as Servo) | `GoBuildaPWMLight` |
| `IntakeLeft` `IntakeRight` `IntakeRoller` | *(bench: `BallPath`)* | — |
| `Launch Prep Servo` | *(bench: `PusherControl`)* | — |
| `Servo0` | *(bench: `ServoTest`)* | — |

**Sensors**
| Config name | Device | Used by |
|---|---|---|
| `sensor_color1` `sensor_color2` `sensor_color3` | REV color/distance sensors | `ColorDetector`, `SensorColor` |
| `colorSensor` | REV Color Sensor V3 | `ColorSensorAuto` (demo only) |
| `Webcam 1` | USB webcam | `AprilTag`, `AprilTagLocalization` |
| `pinpoint` | goBILDA Pinpoint odometry computer | `PinpointLocalizer` |

---

## 10. Tuning & dashboards

- Classes annotated `@Config` expose their `public static` fields live in
  **FTC Dashboard** (`http://192.168.43.1:8080/dash` on the field network): `Robot`,
  `ActuatorControl`, `Drivetrain`, `MecanumDrive`, `SpindexerControl`, `LaunchGamePeace`,
  `LoadSpindexer`, and most test OpModes.
- Flywheel launch velocities (`1650` far / `1400` close, `1380` in `RedAutoNear`) were
  calibrated at ~12.5 V — re-check after a battery or flywheel change.
- Spindexer slot angles, kicker angles, and color thresholds/distance windows are the
  values most likely to drift between events; the `Test*` OpModes exist to re-tune each
  in isolation.

---

## 11. Known rough edges

- Heavy use of `static` mutable state — restart the app between runs.
- `BallPath`, `PusherControl`, `TiltRobot`, `ReverseIntake` are experimental/stub and not
  on the competition path.
- `RedTeleop` currently runs drive-only; the `*Reload` autos are `@Disabled` with their
  launch/load actions commented out.
- Spelling in the codebase is inconsistent (`GamePeace`/game-piece, `Twards`/toward,
  `feild`/field, `Builda`/goBILDA) — kept as-is here so identifiers stay searchable.
