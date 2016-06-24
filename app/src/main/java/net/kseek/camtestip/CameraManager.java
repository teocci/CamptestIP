package net.kseek.camtestip;

import android.content.Context;
import android.hardware.Camera;
import android.widget.Toast;

public class CameraManager {
	private Camera camera;
	private Context context;

	
	public CameraManager(Context contxt) {
		context = contxt;
		// Create an instance of Camera
        camera = getCameraInstance();
	}

	public Camera getCamera() {
		return camera;
	}

	private void releaseCamera() {
		if (camera != null) {
			// release the camera for other applications
			camera.release();
			camera = null;
		}
	}
	
	public void onPause() {
		releaseCamera();
	}
	
	public void onResume() {
		if (camera == null) {
			camera = getCameraInstance();
		}
		
		Toast.makeText(context,
				"preview size = " +
					camera.getParameters().getPreviewSize().width + "x" +
					camera.getParameters().getPreviewSize().height,
				Toast.LENGTH_LONG).show();
	}
	
	/** A safe way to get an instance of the Camera object. */
	private static Camera getCameraInstance(){
	    Camera c = null;
	    try {
	        c = Camera.open(); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    return c; // returns null if camera is unavailable
	}
}
