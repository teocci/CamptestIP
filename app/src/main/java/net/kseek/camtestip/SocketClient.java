package net.kseek.camtestip;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketClient extends Thread {
	private Socket socket;
	private CameraPreview cameraPreview;
	private static final String TAG = "socket";
	private String remoteIP;
	private int remotePort;
	
	public SocketClient(CameraPreview preview, String ip, int port) {
	    cameraPreview = preview;
	    remoteIP = ip;
	    remotePort = port;
		start();
	}
	
	public SocketClient(CameraPreview preview) {
	    cameraPreview = preview;
		start();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		
		try {

			int timeOut = 10000; // in milliseconds

			socket = new Socket();
			socket.connect(new InetSocketAddress(remoteIP, remotePort), timeOut);
			BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
			BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream());
			
			JsonObject jsonObj = new JsonObject();
            jsonObj.addProperty("type", "data");
            jsonObj.addProperty("length", cameraPreview.getPreviewLength());
            jsonObj.addProperty("width", cameraPreview.getPreviewWidth());
            jsonObj.addProperty("height", cameraPreview.getPreviewHeight());
            
			byte[] buff = new byte[256];
			int len = 0;
            String msg = null;
            outputStream.write(jsonObj.toString().getBytes());
            outputStream.flush();
                        
            while ((len = inputStream.read(buff)) != -1) {
                msg = new String(buff, 0, len);
                
                // JSON analysis
                JsonParser parser = new JsonParser();
                boolean isJSON = true;
                JsonElement element = null;
                try {
                    element =  parser.parse(msg);
                }
                catch (JsonParseException e) {
                    Log.e(TAG, "exception: " + e);
                    isJSON = false;
                }
                if (isJSON && element != null) {
                    JsonObject obj = element.getAsJsonObject();
                    element = obj.get("state");
                    if (element != null && element.getAsString().equals("ok")) {
                        // send data
                        while (true) {
                            outputStream.write(cameraPreview.getImageBuffer());
                            outputStream.flush();
                            
                            if (Thread.currentThread().isInterrupted())
                                break;
                        }
                        
                        break;
                    }
                }
                else {
                    break;
                }
            }

			outputStream.close();
			inputStream.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			Log.e(TAG, e.toString());
		} 
		finally {
			try {
				socket.close();
				socket = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void close() {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
