package com.liqimai.heart;

import java.io.File;
import android.util.Log;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


/**
 * This class extends Activity to handle a picture preview, process the preview
 * for a red values and determine a heart beat.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class HeartRateMonitor extends Activity {

    private static final String TAG = "HeartRateMonitor";

    private static SurfaceView preview = null;
    private static SurfaceHolder previewHolder = null;
    private static Camera camera = null;
    private static View image = null;
    private static TextView text = null;
    private static TextView info = null;
    private static Button btn = null;

    private static WakeLock wakeLock = null;
    private static boolean processing = false;
    // private static PrintStream out;
    




    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        preview = (SurfaceView) findViewById(R.id.preview);
        previewHolder = preview.getHolder();
//        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        image = findViewById(R.id.image);
        text = (TextView) findViewById(R.id.text);
        info = (TextView) findViewById(R.id.info);
        btn = (Button) findViewById(R.id.btn);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        wakeLock.acquire();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();

        wakeLock.release();
        closeCamera();
    }

    private static PreviewCallback previewCallback = new PreviewCallback() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onPreviewFrame(byte[] data, Camera cam) {
//        	frameCnt++;
            if (data == null) 
            	throw new NullPointerException();
            Camera.Size size = cam.getParameters().getPreviewSize();
            if (size == null) 
            	throw new NullPointerException();
            ImageProcessing.processImg(data, size.width, size.height);
            // out.println(ImageProcessing.getAvgG() + " " + ImageProcessing.getTime());
            text.setText(//ImageProcessing.getHeartRate() + " " + DFT.getHeartRate() + " " + DFT.getConfidence() 
            		String.valueOf(
            				(int)(ImageProcessing.getHeartRate()*10)/10.0
            				) 
            		+ " " 
            		+ String.valueOf(
            				(int)(DFT.getHeartRate()*10)/10.0
            				) 
            		+ " " 
            		+ String.valueOf(
            				(int)(DFT.getConfidence()*10)/10.0
            				)
            		);
            if(DFT.getConfidence() > 4 && DFT.getConfidence() < 10){
            	android.util.Log.d("HeartRate", ImageProcessing.getHeartRate()+" "+DFT.getHeartRate()+" "+DFT.getConfidence());
            }
            image.postInvalidate();
        }
    };

    private static Camera.Size getSmallestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea < resultArea) result = size;
                }
            }
        }
        return result;
    }
    private static void openCamera(){
    	processing = true;
        camera = Camera.open();
		Camera.Parameters parameters = camera.getParameters();
        if(parameters.getMaxExposureCompensation() != parameters.getMinExposureCompensation()){
            parameters.setExposureCompensation(parameters.getMaxExposureCompensation());	
        }
        if(parameters.isAutoExposureLockSupported()){
        	parameters.setAutoExposureLock(true);
        }
        if(parameters.isAutoWhiteBalanceLockSupported()){
        	parameters.setAutoWhiteBalanceLock(true);
        }
        camera.setDisplayOrientation(90);
		parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        try {
            camera.setPreviewDisplay(previewHolder);
            camera.setPreviewCallback(previewCallback);
        } catch (Throwable t) {
            Log.e("PreviewDemo-surfaceCallback", "Exception in setPreviewDisplay()", t);
        }
        
        Camera.Size size = getSmallestPreviewSize(preview.getWidth(), preview.getHeight(), parameters);
        if (size != null) {
            parameters.setPreviewSize(size.height, size.width);
            Log.d(TAG, "Using width=" + size.width + " height=" + size.height);
        }
//        List<int[]> FPSRengelist = parameters.getSupportedPreviewFpsRange();
//        StringBuffer infotext = new StringBuffer();
//        infotext.append(FPSRengelist.size() + " \n");
//        for(int[] array : FPSRengelist){
//        	infotext.append(array.length + " " + array[Parameters.PREVIEW_FPS_MIN_INDEX] + " " + array[Parameters.PREVIEW_FPS_MAX_INDEX] + "\n");
//        }
//        info.setText(infotext);
//        parameters.setPreviewFpsRange(30000, 30000);
    	camera.setParameters(parameters);
        btn.setText("stop");
        
   //      if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
   //      	File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "HeartRate.txt");
   //      	FileOutputStream fileOutputStream;
			// try {
			// 	fileOutputStream = new FileOutputStream(file);
	  //       	out=new PrintStream(fileOutputStream);
			// } catch (FileNotFoundException e) {
			// 	// TODO Auto-generated catch block
	  //       	btn.setText("Can't write to the external storage.");
			// 	e.printStackTrace();
			// }
   //      }
   //      else{
   //      	btn.setText("Can't write to the external storage.");
   //      }
        DFT.initialize();
        camera.startPreview();
    }
    private static void closeCamera(){
		processing = false;
    	if(camera != null){
	        camera.stopPreview();
	        camera.setPreviewCallback(null);
	    	camera.release();
	    	camera = null;
	    	ImageProcessing.onClose();
    	}
    	// out.close();
		btn.setText("Start");
    }
    public void startMeasuring(View view) {
        // Do something in response to button
    	if(!processing){
	    	openCamera();
    	}
    	else{
    		closeCamera();
    	}
    }
}
