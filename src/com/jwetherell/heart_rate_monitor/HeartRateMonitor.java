package com.jwetherell.heart_rate_monitor;

import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
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
    static TextView textY = null;
    static TextView textR = null;
    static TextView textG = null;
    static TextView textB = null;
    static TextView textT = null;
    static TextView textA = null;
    static TextView textCnt = null;
    private static int frameCnt = 0;

    private static WakeLock wakeLock = null;
    




    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        preview = (SurfaceView) findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        image = findViewById(R.id.image);
        text = (TextView) findViewById(R.id.text);
        textY = (TextView) findViewById(R.id.textY);
        textR = (TextView) findViewById(R.id.textR);
        textG = (TextView) findViewById(R.id.textG);
        textB = (TextView) findViewById(R.id.textB);
        textT = (TextView) findViewById(R.id.textT);
        textA = (TextView) findViewById(R.id.textA);
        textCnt = (TextView) findViewById(R.id.textCnt);

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
        camera.setParameters(parameters);
        camera.setDisplayOrientation(90);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();

        wakeLock.release();

        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    private static PreviewCallback previewCallback = new PreviewCallback() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onPreviewFrame(byte[] data, Camera cam) {
        	frameCnt++;
            if (data == null) 
            	throw new NullPointerException();
            Camera.Size size = cam.getParameters().getPreviewSize();
            if (size == null) 
            	throw new NullPointerException();
            ImageProcessing.processImg(data, size.width, size.height);
            String Y = String.valueOf("Y:"+ImageProcessing.avgY);
            if(Y.length() > 7){
            	Y = Y.substring(0, 7);
            }
            textY.setText(Y);
            String R = String.valueOf("R:"+ImageProcessing.avgR);
            if(R.length() > 7){
            	R = R.substring(0, 7);
            }
            textR.setText(R);
            String G = String.valueOf("G:"+ImageProcessing.avgG);
            if(G.length() > 7){
            	G = G.substring(0, 7);
            }
            textG.setText(G);
            String B = String.valueOf("B:"+ImageProcessing.avgB);
            if(B.length() > 7){
            	B = B.substring(0, 7);
            }
            textB.setText(B);
            String A = String.valueOf("A:"+ImageProcessing.avgA);
            if(A.length() > 7){
            	A = A.substring(0, 7);
            }
            textT.setText(G);
            textA.setText(A);
            textCnt.setText(String.valueOf(frameCnt));
            text.setText(String.valueOf(ImageProcessing.getHeartRate()));
            image.postInvalidate();

        }
    };

    private static SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);
                camera.setPreviewCallback(previewCallback);
            } catch (Throwable t) {
                Log.e("PreviewDemo-surfaceCallback", "Exception in setPreviewDisplay()", t);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            Camera.Size size = getSmallestPreviewSize(width, height, parameters);
            if (size != null) {
                parameters.setPreviewSize(size.height, size.width);
                Log.d(TAG, "Using width=" + size.width + " height=" + size.height);
            }
            camera.setParameters(parameters);
            camera.startPreview();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Ignore
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
}
