package org.firstinspires.ftc.teamcode.Perception;
import static java.lang.System.currentTimeMillis;
import java.util.LinkedList;
import java.util.Queue;

public class MovingAverage {
        private final Queue<Double> window;
        private final int windowSize;
        private double sum;
        private long PreviousTimestamp;
        public long DeltaT;
        public double Average=0;
        public double Value=0;

        public MovingAverage(int windowSize) {
            if (windowSize <= 0) {
                throw new IllegalArgumentException("Window size must be positive.");
            }
            this.windowSize = windowSize;
            this.window = new LinkedList<>();
            this.sum = 0.0;
            this.DeltaT=5;  // time before the buffer resets ms
        }  // end of MovingAverage Constructor

      // Returns the moving average and reset if data is old.

        public void addData(double newValue) {

            if ((currentTimeMillis()-PreviousTimestamp)>DeltaT){
                window.clear();
            }
            PreviousTimestamp = currentTimeMillis();
            // Add the new value to the sum and the window
            Value=newValue;
            sum += newValue;
            window.add(newValue);

            // If the window size is exceeded, remove the oldest value
            if (window.size() > windowSize) {
                sum -= window.remove();
            }

            // Calculate and return the average
            Average = sum / window.size();
        }


    }


