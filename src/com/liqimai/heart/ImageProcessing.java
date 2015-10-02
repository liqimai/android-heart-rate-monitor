package com.liqimai.heart;

import java.util.concurrent.atomic.AtomicBoolean;



/**
 * This abstract class is used to process images.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public abstract class ImageProcessing {
	static double avgY;
	static double avgR;
	static double avgG;
	static double avgB;
	static double avgA;
	private static int heartRate;
    private static final AtomicBoolean processing = new AtomicBoolean(false);
    private static int averageIndex = 0;
    private static final int smoothRate = 0;
    private static final int averageArraySize = 6;
    private static final double[] averageArray = new double[averageArraySize];

    
//    private static int beatsIndex = 0;
//    private static final int beatsArraySize = 3;
//    private static final double[] beatsArray = new double[beatsArraySize];
    
    private static int beats = 0;
    
    private static int timeRecordIndex = 0;
    private static final int timeRecordSize = 6;
    private static final long[] timeRecord = new long[timeRecordSize]; 
    
//    private static long startTime = System.currentTimeMillis();
    
    
    
//    private static final int updateTime = 3;
    private static final double roundRate = 1;
    
    public static enum TYPE {
        GREEN, RED
    };

    private static TYPE currentType = TYPE.GREEN;
    private static double decodeYUV420SPtoAvg(byte[] yuv420sp, int width, int height) {
        if (yuv420sp == null) 
        	return 0;

        final int frameSize = width * height;

        int sumY = 0;
        int sumR = 0;
        int sumG = 0;
        int sumB = 0;
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & yuv420sp[yp]) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) r = 0;
                else if (r > 262143) r = 262143;
                if (g < 0) g = 0;
                else if (g > 262143) g = 262143;
                if (b < 0) b = 0;
                else if (b > 262143) b = 262143;

                sumY += y;
                sumR += (r>>10)&0xff;
                sumG += (g>>10)&0xff;
                sumB += (b>>10)&0xff;
            }
        }
        avgY = (int)((1.0*sumY/frameSize)*roundRate)/roundRate;
        avgR = (int)((1.0*sumR/frameSize)*roundRate)/roundRate;
        avgG = (int)((1.0*sumG/frameSize)*roundRate)/roundRate;
        avgB = (int)((1.0*sumB/frameSize)*roundRate)/roundRate;
        return avgG;
    }

    public static int getHeartRate(){
    	return heartRate;
    }
    public static TYPE getCurrent() {
        return currentType;
    }
    public static void onClose(){
    	for( int i = 0; i<timeRecord.length ; ++i){
    		timeRecord[i] = 0;
    	}
    	timeRecordIndex = 0;
    	beats = 0;
    	for( int i = 0; i<averageArray.length ; ++i){
    		averageArray[i] = 0;
    	}
    	averageIndex = 0;
    }
    public static void processImg(byte[] data, int width, int height) {
        if (!processing.compareAndSet(false, true)) 
        	return;

        double imgAvg = decodeYUV420SPtoAvg(data, width, height);
        // Log.i(TAG, "imgAvg="+imgAvg);
        if (imgAvg == 0 || imgAvg == 255) {
            processing.set(false);
            return;
        }
        int smoothCnt = 1;
        for(int i = 0; i < smoothRate; ++i){
        	if(averageArray[(averageIndex - i) % averageArraySize]>0){
        		imgAvg += averageArray[(averageIndex - i) % averageArraySize];
        		smoothCnt++;
        	}
        }
        imgAvg /= smoothCnt;
//        imgAvg = (int)(imgAvg*roundRate)/roundRate;
        double averageArrayAvg = 0;
        int averageArrayCnt = 0;
        for (int i = 0; i < averageArray.length; i++) {
            if (averageArray[i] > 0) {
                averageArrayAvg += averageArray[i];
                averageArrayCnt++;
            }
        }

        if (averageIndex == averageArraySize) averageIndex = 0;
        averageArray[averageIndex] = imgAvg;
        averageIndex++;
        
        avgA = (averageArrayCnt > 0) ? (averageArrayAvg / averageArrayCnt) : 0;
        avgA = (int)(avgA*roundRate)/roundRate;
        
        TYPE newType = currentType;
        if (imgAvg < avgA) {
            newType = TYPE.RED;
            if (newType != currentType) {
            	beats++;
                timeRecord[timeRecordIndex++] = System.currentTimeMillis();
                if (timeRecordIndex == timeRecordSize){
                	timeRecordIndex = 0;
                }
                // Log.d(TAG, "BEAT!! beats="+beats);
            }
        } else if (imgAvg > avgA) {
            newType = TYPE.GREEN;
        }


        // Transitioned from one state to another to the same
        if (newType != currentType) {
            currentType = newType;
        }
        double startTime,endTime;
        int beatCnt;
        if(beats > 1){
	        if(beats < timeRecordSize){
	        	startTime = timeRecord[0];
	        	endTime = timeRecord[timeRecordIndex-1];
	        	beatCnt = beats - 1;
	        }
	        else{
	        	startTime = timeRecord[timeRecordIndex];
	        	endTime = timeRecord[(timeRecordIndex-1+timeRecordSize)%timeRecordSize];
	        	beatCnt = timeRecordSize-1;
	        }
	    	heartRate = (int) (beatCnt * 60000 / (endTime - startTime));
        }
        processing.set(false);
    }
}