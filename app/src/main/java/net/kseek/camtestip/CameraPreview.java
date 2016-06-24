package net.kseek.camtestip;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder holder;
    private Camera camera;
    private static final String TAG = "camera";
    private Size previewSize;
    private byte[] imageData;
    private LinkedList<byte[]> queue = new LinkedList<byte[]>();
    private static final int MAX_BUFFER = 15;
    private byte[] lastFrame = null;
    private int frameLength;

    public CameraPreview(Context context, Camera cam) {
        super(context);
        camera = cam;

        holder = getHolder();
        holder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        Parameters params = camera.getParameters();
        List<Size> sizes = params.getSupportedPreviewSizes();
        for (Size s : sizes) {
        	Log.i(TAG, "preview size = " + s.width + ", " + s.height);
        }
        
        params.setPreviewSize(640, 480); // set preview size. smaller is better
        camera.setParameters(params);
        
        previewSize = camera.getParameters().getPreviewSize();
        Log.i(TAG, "preview size = " + previewSize.width + ", " + previewSize.height);
        
        int format = camera.getParameters().getPreviewFormat();
        frameLength = previewSize.width * previewSize.height * ImageFormat.getBitsPerPixel(format) / 8;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {}

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        if (holder.getSurface() == null)
            return;

        try {
            camera.stopPreview();
            resetBuff();
        } catch (Exception e){}

        try {
            camera.setPreviewCallback(previewCallback);
            camera.setPreviewDisplay(holder);
            camera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
    
    public void setCamera(Camera cam) {
    	camera = cam;
    }
    
    public byte[] getImageBuffer() {
        synchronized (queue) {
			if (queue.size() > 0) {
				lastFrame = queue.poll();
			}
    	}
        
        return lastFrame;
    }
    
    private void resetBuff() {
        
        synchronized (queue) {
        	queue.clear();
        	lastFrame = null;
    	}
    }
    
    public int getPreviewLength() {
        return frameLength;
    }
    
    public int getPreviewWidth() {
    	return previewSize.width;
    }
    
    public int getPreviewHeight() {
    	return previewSize.height;
    }
    
    public void onPause() {
    	if (camera != null) {
    		camera.setPreviewCallback(null);
    		camera.stopPreview();
    	}
    	resetBuff();
    }
    
    private Camera.PreviewCallback previewCallback = new PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
        	synchronized (queue) {
    			if (queue.size() == MAX_BUFFER) {
    				queue.poll();
    			}
    			queue.add(data);
        	}
        }
    };
    
    private void saveYUV(byte[] byteArray) {

        YuvImage im = new YuvImage(byteArray, ImageFormat.NV21, previewSize.width, previewSize.height, null);
        Rect r = new Rect(0, 0, previewSize.width, previewSize.height);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        im.compressToJpeg(r, 100, baos);

        try {
            FileOutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/yuv.jpg");
            output.write(baos.toByteArray());
            output.flush();
            output.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {}
    }
    
    private void saveRAW(byte[] byteArray) {
        try {
            FileOutputStream file = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + "/test.yuv"));
            try {
                file.write(imageData);
                file.flush();
                file.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
