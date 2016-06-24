package net.kseek.camtestip;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.dynamsoft.ipcamera.R;

public class CamtestIP extends Activity {
    private CameraPreview preview;
    private CameraManager cameraManager;
    private boolean started = true;
    private SocketClient thread;
    private Button button;
    private String remoteIP;
    private int remotePort;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);


		SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key),
				Context.MODE_PRIVATE);
		remoteIP = sharedPref.getString(getString(R.string.ip), "192.168.1.160");
		remotePort = sharedPref.getInt(getString(R.string.port), 8880);

		button = (Button) findViewById(R.id.button_capture);
		button.setOnClickListener(
		    new View.OnClickListener() {
		        @Override
		        public void onClick(View v) {
		            // get an image from the camera
		          if (started) {
		        	  if (remoteIP == null) {
		        		  thread = new SocketClient(preview);
		        	  }
		        	  else {
		        		  thread = new SocketClient(preview, remoteIP, remotePort);
		        	  }
		              
		              started = false;
		              button.setText(R.string.stop);
		          }
		          else {
		              closeSocketClient();
		              reset();
		          }
		        }
		    }
		);
		cameraManager = new CameraManager(this);
        // Create our Preview view and set it as the content of our activity.
        preview = new CameraPreview(this, cameraManager.getCamera());
        FrameLayout flPreview = (FrameLayout) findViewById(R.id.camera_preview);
		flPreview.addView(preview);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ipcamera, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		int id = item.getItemId();
		switch (id) {
		case R.id.action_settings:
			setting();
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void setting() {
		LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.server_setting, null);
		EditText ipEdit = (EditText)textEntryView.findViewById(R.id.ip_edit);
		EditText portEdit = (EditText)textEntryView.findViewById(R.id.port_edit);

		ipEdit.setText(remoteIP);
		portEdit.setText("" + remotePort);

        AlertDialog dialog =  new AlertDialog.Builder(CamtestIP.this)
            .setIconAttribute(android.R.attr.alertDialogIcon)
            .setTitle(R.string.setting_title)
            .setView(textEntryView)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                	EditText ipEdit = (EditText)textEntryView.findViewById(R.id.ip_edit);
                	EditText portEdit = (EditText)textEntryView.findViewById(R.id.port_edit);

					remoteIP = ipEdit.getText().toString();
					remotePort = Integer.parseInt(portEdit.getText().toString());

					SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key),
							Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = sharedPref.edit();
					editor.clear();
					editor.putString(getString(R.string.ip), remoteIP);
					editor.putInt(getString(R.string.port), remotePort);
					editor.commit();

                	Toast.makeText(CamtestIP.this, "New address: " + remoteIP + ":" + remotePort, Toast.LENGTH_LONG).show();
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    /* User clicked cancel so do some stuff */
                }
            })
            .create();
        dialog.show();
	}
	
	@Override
    protected void onPause() {
        super.onPause();
        closeSocketClient();
        preview.onPause();
        cameraManager.onPause();              // release the camera immediately on pause event
        reset();
    }
	
	private void reset() {
		button.setText(R.string.start);
        started = true;
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		cameraManager.onResume();
		preview.setCamera(cameraManager.getCamera());
	}
	
	private void closeSocketClient() {
		if (thread == null)
			return;
		
		thread.interrupt();
        try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        thread = null;
	}
}
