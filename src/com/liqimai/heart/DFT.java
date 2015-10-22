package com.liqimai.heart;

public class DFT{
    static final int MAX_FREQUENCY = 250;
    static final int SAMPLE_SIZE = 300;
    static final int SMOOTH_RATE = 20;
    static final Complex c0 = new Complex(0, 0);
    static double FPS = 22.756;
    static private Complex[][] A = new Complex[MAX_FREQUENCY][SAMPLE_SIZE - SMOOTH_RATE/2];
    static private int Aj = 0;

    static private int xIndex = 0;
    static private double x[] = new double[SAMPLE_SIZE];
    static private long[] timeRecord = new long[SAMPLE_SIZE];
    static private Complex[] X = new Complex[MAX_FREQUENCY];
    static private double[] base = new double[SAMPLE_SIZE];
    static private double HeartRate;
    static private double confidence;
    static {
        for (int i = 0; i < A.length; ++i) {
            for (int j = 0; j < A[i].length; ++j) {
                double theta = (-2*Math.PI*1000/FPS*0.00001)*j*i;
                A[i][j] = new Complex(Math.cos(theta), Math.sin(theta));
            }
            X[i] = new Complex(0, 0);
        }
    }
    static void newData(double value){
        double oldValue = x[xIndex%SAMPLE_SIZE];
        x[xIndex%SAMPLE_SIZE] = value;
        timeRecord[xIndex%SAMPLE_SIZE] = System.currentTimeMillis();
        double acturalFPS;
        if(xIndex < SAMPLE_SIZE){
        	acturalFPS = 1000.0 * xIndex / (timeRecord[xIndex] - timeRecord[0]);
        }
        else{
        	acturalFPS = 1000.0 * SAMPLE_SIZE / (timeRecord[xIndex % SAMPLE_SIZE] - timeRecord[(xIndex+1) % SAMPLE_SIZE]);
        }
        int processDataIndex = (xIndex%SAMPLE_SIZE - SMOOTH_RATE/2 + SAMPLE_SIZE) % SAMPLE_SIZE;
        base[processDataIndex] = 0;
        if(xIndex >= SMOOTH_RATE){
            //smooth
            for (int i = -SMOOTH_RATE/2; i < SMOOTH_RATE/2; ++i) {
                base[processDataIndex] += 
                    x[(xIndex%SAMPLE_SIZE - SMOOTH_RATE/2 + i + SAMPLE_SIZE) % SAMPLE_SIZE];
            }
            base[processDataIndex] /= SMOOTH_RATE;
            
            //update dft
            for (int i = 0; i < MAX_FREQUENCY; ++i) {
                // if ( xIndex == 202) {
                //     System.err.println("oldValue="+oldValue);
                //     System.err.println("base="+base[xIndex%SAMPLE_SIZE]);
                // }
                X[i] = 
                X[i].plus(
                        A[i][Aj].times(x[processDataIndex] - base[processDataIndex])
                    );
            }
        }
        if (xIndex >= SAMPLE_SIZE + SMOOTH_RATE/2) {
            for (int i = 0; i < MAX_FREQUENCY; ++i) {
                // if ( xIndex == 202) {
                //     System.err.println("oldValue="+oldValue);
                //     System.err.println("base="+base[xIndex%SAMPLE_SIZE]);
                // }
                X[i] = 
                X[i].minus(
                        A[i][(Aj+1)%A[i].length].times(oldValue-base[xIndex%SAMPLE_SIZE])
                );
            }
        }

        Aj = (Aj + 1) % A[0].length;
        xIndex = xIndex + 1;
        // if (xIndex == SAMPLE_SIZE) {
        //     for (int i = 0; i < SAMPLE_SIZE; ++i) {
        //         System.out.println(base[i]);
        //     }
        // }
        double firstPeak = 0;
        double secondPeak = 0;
        for(int i = 1; i < MAX_FREQUENCY-1; ++i){
            if(X[i].abs() >= X[i-1].abs() 
                && X[i].abs() >= X[i+1].abs() 
                && X[i].abs() > firstPeak){
                secondPeak = firstPeak;
                firstPeak = X[i].abs();
                HeartRate = i*0.6*acturalFPS/FPS;
            }
        }
        confidence = firstPeak/secondPeak;
    }    
    static public void setFPS(int FPS){
    	DFT.FPS = FPS;
    }
    static public double getHeartRate(){
        return HeartRate;
    }
    static public double getConfidence(){
        return confidence;
    }
    static public void initialize(){
        Aj = 0;
        xIndex = 0;
        for (int i  = 0; i < SAMPLE_SIZE; ++i){
        	x[i] = 0;
        	base[i] = 0;
        }
        for (int i  = 0; i < MAX_FREQUENCY; ++i){
        	X[i] = c0;
        }
        HeartRate = 0;
        confidence = 0;
    }
    // static public void newData(double value){
    //     x[xIndex] = value;
    //     xIndex = xIndex + 1;
    //     if(xIndex == SAMPLE_SIZE){
    //         xIndex -= SAMPLE_SIZE;
    //         full = true;


    //     }
    //     smooth();
    //     dft();
    //     double MaxAmplitude = 0;
    //     for(int i = 0; i < MAX_FREQUENCY; ++i){
    //         if(X[i].abs() > MaxAmplitude){
    //             MaxAmplitude = X[i].abs();
    //             HeartRate = i*0.6;
    //         }
    //     }
    // }
    // static private void smooth(){
    //     if(full){
    //         for (int i = 0; i < SAMPLE_SIZE; ++i) {
    //             base[i] = 0;
    //             for (int j = -SMOOTH_RATE/2; j < SMOOTH_RATE/2; ++j) {
    //                 if (i+j < xIndex && i>= xIndex) {
    //                     base[i] += x[xIndex];
    //                 }
    //                 else if (i+j >= xIndex && i<xIndex) {
    //                     base[i] += x[(xIndex-1+SAMPLE_SIZE)%SAMPLE_SIZE];
    //                 }
    //                 else{
    //                     base[i] += x[(i+j+SAMPLE_SIZE)%SAMPLE_SIZE];
    //                 }
    //             }
    //             base[i] = base[i]/SMOOTH_RATE;
    //         }
    //     }
    //     else{
    //         for (int i = 0; i < xIndex; ++i) {
    //             base[i] = 0;
    //             for (int j = -SMOOTH_RATE/2; j < SMOOTH_RATE/2; ++j) {
    //                 if (i+j <0  && i>= 0) {
    //                     base[i] += x[0];
    //                 }
    //                 else if (i+j >= xIndex && i<xIndex) {
    //                     base[i] += x[(xIndex-1+SAMPLE_SIZE)%SAMPLE_SIZE];
    //                 }
    //                 else{
    //                     base[i] += x[(i+j+SAMPLE_SIZE)%SAMPLE_SIZE];
    //                 }
    //             }
    //             base[i] = base[i]/SMOOTH_RATE;
    //         }
    //     }
    // }
    // static private void dft(){
    //     for (int i = 0; i < A.length; ++i) {
    //         X[i] = new Complex(0, 0);
    //         for (int j = 0; j < A[i].length; ++j) {
    //             X[i] = X[i].plus(A[i][j].times(x[j]-base[j]));
    //         }
    //     }   
    // }
//    public static void main(String[] args) {
//        try{
//            Scanner fin =
//                    new Scanner (
//                            new DataInputStream(
//                                    new FileInputStream(
//                                            new File("y.txt"))));
////             while(fin.hasNext()){
////                 newData(fin.nextDouble());
////             }
////            for (int i = 0; fin.hasNext(); ++i ) {
////                x[i] = fin.nextDouble();
////            }
////            smooth();
////            dft();
////            for(int i = 0; i < SAMPLE_SIZE; ++i){
////                System.out.println(base[i]);
////            }            
////            System.out.println();
//
//
//            while(fin.hasNext()){
//            // for(int i = 0; i < 400; ++i) {
//                newData(fin.nextDouble());
//                System.out.println(getConfidence());
//                // System.out.println(HeartRate); 
//                // for(int i = 0; i < MAX_FREQUENCY; ++i){
//                //    System.out.print(X[i].abs() + " ");
//                // }
//                // System.out.println();
//            }
//            // for (int i = 0; i < SAMPLE_SIZE; ++i) {
//            //     System.out.println(base[i]);
//            // }
//        }
//        catch(FileNotFoundException e){
//            System.err.println(e);
//        }
//    }
}