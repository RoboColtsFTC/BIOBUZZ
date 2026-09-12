package org.firstinspires.ftc.teamcode.Perception;


import com.acmerobotics.dashboard.config.Config;

import org.firstinspires.ftc.teamcode.Perception.AprilTag.DetectionState;

public class AprilTagData {
    public DetectionState detectionState = new DetectionState();
    public goal Red;
    public goal Blue;
    public Code DetectedCode;

    public enum AprilCode{
            GPP,
            PGP,
            PPG
    }
    AprilCode ColorCode;
   public boolean red;  // True == Red, False == Blue

    public AprilTagData(){
        Red = new goal();
        Blue = new goal();
        DetectedCode = new Code();

    }
    public void SetRed(double Range,double Bearing) {
       Red.addValue(Range, Bearing);


    }

    public void SetBlue(double Range,double Bearing) {
        Blue.addValue(Range, Bearing);


    }

    public void SetCode(AprilCode code) {
        DetectedCode.SetCode(code);


    }

    public void SetDetectionState(DetectionState detectionState){
        this.detectionState=detectionState;

    }


   public class Code{
       public AprilCode CodeID;
       public boolean IsDetected = false;
       public void SetCode(AprilCode code){

           IsDetected = true;
           CodeID=code;

       }


    }

    public class goal{

        public long timestamp;
        public double Bearing;
        public double Range;
        public int windowsize=3;public goal(){



        }
        public void addValue( double Range,double Bearing){
            this.Bearing=Bearing;
            this.Range=Range;

        }
    }



}
