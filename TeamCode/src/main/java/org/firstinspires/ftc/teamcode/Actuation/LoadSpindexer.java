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
public class LoadSpindexer {
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
    public LoadSpindexer(LinearOpMode opmode,Actuators actuators,List<DetColor> colorPos){ // Cunstructor
       colordetector=new ColorDetector(opmode);
        this.colorPos =colorPos;
        Currentstate=State.Empty;
        this.opmode=opmode;
        this.actuators=actuators;
        ControlFeedTimer.reset();
        KickerTimer.reset();

    }



    public Action  LoadSpindexer_auto(){
        return new Action(){
            @Override
            public boolean run(@NonNull TelemetryPacket telemetryPacket) {
                //autoload=true;
                auto=true;
                load_spindexer_run();


                return auto;
            }



        };
    }

    public void run()
    {
        load_spindexer_run();
        opmode.telemetry.addData("ColorPosition",colorPos);

    }

    public void load_spindexer_run(){

        colordetector.run();
        // State machine to load Spindexer
        if(Currentstate==State.Loaded){
            ActuatorControl.SpindexerStateIndicator2.SetColor(.5);
        }
        switch(Currentstate) {
            case Empty:
                if(opmode.gamepad2.a && (ActuatorControl.controlstate==ActuatorControl.ControlState.ready)) {
                    ActuatorControl.controlstate=ActuatorControl.ControlState.loading;
                    Currentstate = State.LoadOne;
                }else if (auto){
                    ActuatorControl.controlstate=ActuatorControl.ControlState.loading;
                    Currentstate = State.LoadOne;
                }


                    ActuatorControl.SpindexerStateIndicator2.SetColor(0);

                break;
            case LoadOne:
                LoadGamePeace( 1);

                if(gamepeaceloadingstate==GamePeaceLoadingState.Complete){
                    gamepeaceloadingstate=GamePeaceLoadingState.IDLE;
                    Currentstate=State.LoadTwo;
                }
                ActuatorControl.SpindexerStateIndicator2.SetColor(.338);  //yellow
                break;
            case LoadTwo:
                LoadGamePeace( 2);

                if(gamepeaceloadingstate==GamePeaceLoadingState.Complete){
                    gamepeaceloadingstate=GamePeaceLoadingState.IDLE;
                    Currentstate=State.LoadThree;
                }
                ActuatorControl.SpindexerStateIndicator2.SetColor(.28);  //yellow
                break;
            case LoadThree:
                LoadGamePeace( 3);

                if(gamepeaceloadingstate==GamePeaceLoadingState.Complete){
                    gamepeaceloadingstate=GamePeaceLoadingState.IDLE;
                    Currentstate=State.Loaded;
                    actuators.feedcontrol.StopFeed();
                    actuators.IntakeMotor.StopMotor();
                    ActuatorControl.controlstate = ActuatorControl.ControlState.ready;
                    auto=false;
                    opmode.telemetry.addData("colororder",colorPos);

                }

                break;
            }

    }
    public enum GamePeaceLoadingState{
        IDLE,
        StartIntake,
        Position,
        DetectColor,
        kickball,
        Complete
    }

    public static GamePeaceLoadingState gamepeaceloadingstate=GamePeaceLoadingState.IDLE;
    public void LoadGamePeace(int SpindexPos)  {

        // Cycle time controls t
        switch(gamepeaceloadingstate) {
            case IDLE:
                  gamepeaceloadingstate = GamePeaceLoadingState.StartIntake;
                  break;
            case StartIntake:
                actuators.IntakeMotor.StartMotor();
                actuators.feedcontrol.startFeed();
                gamepeaceloadingstate = GamePeaceLoadingState.Position;
                break;
            case Position:

                if (SpindexPos == 1 ) {
                    actuators.spindexercontrol.setPosition(SpindexPos);
                    gamepeaceloadingstate=GamePeaceLoadingState.DetectColor;

                } else {
                    ActuateFeed(SpindexPos);

                    if(feedstate==FeedState.COMPLETE) {
                        feedstate=FeedState.IDLE;
                        gamepeaceloadingstate = GamePeaceLoadingState.DetectColor;
                    }

                }

                break;
            case DetectColor:
                DetectGamePeace(SpindexPos);

                if(detectgamepeace==DetectGamePeace.complete){
                    detectgamepeace=DetectGamePeace.IDLE;
                    if (Currentstate==State.LoadThree){
                        gamepeaceloadingstate = GamePeaceLoadingState.kickball;
                    }else{
                        gamepeaceloadingstate=GamePeaceLoadingState.Complete;
                    }
                }

                break;
            case kickball:
                ActuateFeedKicker();

                if (kickerstate == KickerState.COMPLETE){
                    kickerstate = KickerState.IDLE;
                    gamepeaceloadingstate=GamePeaceLoadingState.Complete;
                }

                break;
            case Complete:
                // used to prevent circular states.

                break;


        }

    }



    public enum DetectGamePeace{
        IDLE,

        DetectGamePeace,
        ManualOveride,
        RecordColor,
        complete
    }
    public static DetectGamePeace detectgamepeace=DetectGamePeace.IDLE;


    public void DetectGamePeace(int SpindexPos){

        switch(detectgamepeace){
            case IDLE:


                    detectgamepeace=DetectGamePeace.DetectGamePeace;

                break;
            case DetectGamePeace:
                if (Currentstate == State.LoadThree) {
                    colordetector.maxdist = 8;
                }else{
                    colordetector.maxdist = 4.8;
                }
                if ( colordetector.colordetected()){
                    detectgamepeace=DetectGamePeace.RecordColor;
                } else if(opmode.gamepad2.b) {
                    detectgamepeace=DetectGamePeace.ManualOveride;
                }

                if(opmode.gamepad2.b) {
                    detectgamepeace=DetectGamePeace.ManualOveride;
               }

                break;
            case ManualOveride:
                colorPos.set(SpindexPos-1,DetColor.UNKNOWN);
                detectgamepeace=DetectGamePeace.complete;

                break;
            case RecordColor:
                colorPos.set(SpindexPos-1,colordetector.GetColor());
                detectgamepeace=DetectGamePeace.complete;

                break;
            case complete:

                // state used to avoid circular logic
                break;

        }

    }



    public enum KickerState{
        IDLE,
        KICK,
        RETURNTOPOSITION,
        COMPLETE
    }


    public static KickerState kickerstate = KickerState.IDLE;
    
    public void ActuateFeedKicker(){
        switch(kickerstate){
            case IDLE:

                    KickerTimer.reset();
                    kickerstate = KickerState.KICK;

                break;
            case KICK:
                 actuators.FeedKicker.SetSecond();
            
                if(KickerTimer.milliseconds()>=500){

                    KickerTimer.reset();
                    kickerstate = KickerState.RETURNTOPOSITION;
                }
                break;
            case RETURNTOPOSITION:
                    actuators.FeedKicker.SetFirst();
                    if(KickerTimer.milliseconds()>=500){
                        KickerTimer.reset();
                        kickerstate = KickerState.COMPLETE;
                }
                
                break;
            case COMPLETE:
                // state used to avoid circular state logic
                break;
                           
        }

    }
    
public enum FeedState{
    IDLE,
    STOPFEED,
    CHANGEPOSITION,
    STARTFEED,
    COMPLETE
}
    
public static FeedState feedstate=FeedState.IDLE;
    public void ActuateFeed(int SpindexPos){
        switch(feedstate){
            case IDLE:

                    ControlFeedTimer.reset();
                    feedstate = FeedState.STOPFEED;

                break;
            case STOPFEED:
                actuators.feedcontrol.StopFeed();
                if(ControlFeedTimer.milliseconds()>=0){

                    ControlFeedTimer.reset();
                    feedstate=FeedState.CHANGEPOSITION;
                }
                break;
            case CHANGEPOSITION:
              
              actuators.spindexercontrol.setPosition(SpindexPos);

              if(ControlFeedTimer.milliseconds()>=120){
                    ControlFeedTimer.reset();
                    feedstate=FeedState.STARTFEED;
              }
                 break;
            case STARTFEED:
                 actuators.feedcontrol.startFeed();
                 feedstate=FeedState.COMPLETE;

                break;
            case COMPLETE:
                // state used to avoid circular state logic
                break;
                            
        }

    }



}
