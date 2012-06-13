package fApps;

import java.io.*;
import java.util.List;

import fApps.androidSDK.R;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class AndroidSDKActivity extends Activity implements LocationListener, SensorEventListener {
	/** Called when the activity is first created. */
	private String TAG = "ANDROID_SDK_PROTOTYPE";

	private LocationManager locationManager;
	private SensorManager sensorManager;
	
	//for accelerometer values
	private TextView outputOrTv1;
	private TextView outputOrTv2;
	private TextView outputOrTv3;
	 
	//for orientation values
	private TextView outputX2;
	private TextView outputY2;
	private TextView outputZ2;
	
	//for GPS values
	private TextView outputLon;
	private TextView outputLat;
	
	//for Logging
	String gpsLog;
	String orientLog;
	String gyroLog;
	
	//for Logs
	private TextView logs;
	
	
	
	//new getOrientation
	private float[] mags;
	private float[] accels;
	private float[] gyro;
    private static final int matrix_size = 16;
    float[] RE = new float[matrix_size];
    float[] outR = new float[matrix_size];
    float[] I = new float[matrix_size];
    float[] values = new float[3]; 

    double[] azimuth;
    double[] pitch;
    double[] roll;
    int bufsize;
    int bufpoint;
    
   double bigX;
   double bigY;
   double bigZ;
    
    
    //Gradchar
    private final char deg=176;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0, this);
		

	    //TV for AcclsMang
	    outputX2 = (TextView) findViewById(R.id.TextView4);
	    outputY2 = (TextView) findViewById(R.id.TextView5);
	    outputZ2 = (TextView) findViewById(R.id.TextView6);
	    //TV for Orientation
		outputOrTv1 = (TextView) findViewById(R.id.TvOr1);
		outputOrTv2 = (TextView) findViewById(R.id.TvOr2);
		outputOrTv3 = (TextView) findViewById(R.id.TvOr3);
		//TV for GPS
	    outputLon = (TextView) findViewById(R.id.TextView7);
	    outputLat = (TextView) findViewById(R.id.TextView8);	   
	    
	    bufsize=20;
	    bufpoint=0;
	    azimuth = new double[bufsize];
	    pitch= new double[bufsize];
	    roll = new double[bufsize];

	    
	    
	    checkExternalMedia();
	    
	   /*List<Sensor> l = sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
	   String str = null;
		   for(int i=0;i<l.size();i++)
		   {
			   str = str+l.get(i).getName();
		   }
		   outputOrTv1.setText(str);*/
	    }
	    

	@Override
	protected void onResume() {
	    super.onResume();
	    sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), sensorManager.SENSOR_DELAY_NORMAL);
	    sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sensorManager.SENSOR_DELAY_GAME);
	    sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), sensorManager.SENSOR_DELAY_GAME);
	    sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), sensorManager.SENSOR_DELAY_GAME);
		
	}
	 protected void onStop() {
		    super.onStop();
		    sensorManager.unregisterListener(this);
		    this.writeToSDFile(gpsLog+orientLog+gyroLog);
		 }
	

	@Override
	protected void onPause() {
		super.onPause();
		/*
		sensorManager.unregisterListener(this);*/
	}

	@Override
	public void onLocationChanged(Location location) {
		
		if (location != null) {
			outputLon.setText("Lon: " + location.getLongitude());
			outputLat.setText("Lat: " + location.getLatitude());
			gpsLog=gpsLog+System.currentTimeMillis()+";"+"long"+";"+location.getLongitude()+"\n";
			gpsLog=gpsLog+System.currentTimeMillis()+";"+"lang"+";"+location.getLatitude()+"\n";
		}

	}
	@Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            switch (event.sensor.getType()){
            
            
            	case Sensor.TYPE_MAGNETIC_FIELD:
                mags = event.values.clone();
                break;
            	case Sensor.TYPE_ACCELEROMETER:
                accels = event.values.clone();
                break;     
            	case Sensor.TYPE_GYROSCOPE:
            		gyro = event.values.clone();
            		
		
            	break;
            }
            
            if (mags != null && accels != null) 
            {
                SensorManager.getRotationMatrix(RE, I, accels, mags);

                // Correct if screen is in Landscape
                SensorManager.remapCoordinateSystem(RE, SensorManager.AXIS_X,SensorManager.AXIS_Z, outR);

                SensorManager.getOrientation(outR, values);

                azimuth[bufpoint] = Math.toDegrees(values[0]);
                pitch[bufpoint] = Math.toDegrees(values[1]);
                roll[bufpoint] = Math.toDegrees(values[2]);
                if(bufpoint == (bufsize-1))
                {
                java.util.Arrays.sort(azimuth);
                java.util.Arrays.sort(pitch);
                java.util.Arrays.sort(roll);
                double tmpAzimuth=0;
                double tmpPitch=0;
                double tmpRoll=0;
               
                outputX2.setText(Double.toString(Math.round(azimuth[(azimuth.length/2)-1]))+deg);
                outputY2.setText(Double.toString(Math.round(pitch[(pitch.length/2)-1]))+deg);
                outputZ2.setText(Double.toString(Math.round(roll[(roll.length/2)-1]))+deg);
                
                orientLog=orientLog+System.currentTimeMillis()+";"+"Azimuth"+";"+tmpAzimuth+"\n";
                orientLog=orientLog+System.currentTimeMillis()+";"+"Pitch"+";"+tmpPitch+"\n";
                orientLog=orientLog+System.currentTimeMillis()+";"+"Roll"+";"+tmpRoll+"\n";
                bufpoint=0;
                }
                else
                {
                	bufpoint++;
                }
                mags=null;
                accels=null;
            }
            if(gyro != null)
            {
            	if(Math.round(Math.toDegrees(gyro[0]))!=0)
            		outputOrTv1.setText(Double.toString(Math.round(Math.toDegrees(gyro[0])))+deg);
            	if(Math.round(Math.toDegrees(gyro[1]))!=0)
            		outputOrTv2.setText(Double.toString(Math.round(Math.toDegrees(gyro[1])))+deg);
            	if(Math.round(Math.toDegrees(gyro[2]))!=0)
            		outputOrTv3.setText(Double.toString(Math.round(Math.toDegrees(gyro[2])))+deg);
                gyro=null;
            }
        }

	}

	private void checkExternalMedia(){
	    boolean mExternalStorageAvailable = false;
	    boolean mExternalStorageWriteable = false;
	    String state = Environment.getExternalStorageState();

	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        // Can read and write the media
	        mExternalStorageAvailable = mExternalStorageWriteable = true;
	    } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        // Can only read the media
	        mExternalStorageAvailable = true;
	        mExternalStorageWriteable = false;
	    } else {
	        // Can't read or write
	        mExternalStorageAvailable = mExternalStorageWriteable = false;
	    }
	}
	
	private void writeToSDFile(String data){

	    // Find the root of the external storage.
	    // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal

	    File root = android.os.Environment.getExternalStorageDirectory(); 

	    // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

	    File dir = new File (root.getAbsolutePath() + "/download");
	    dir.mkdirs();
	    File file = new File(dir, "myData.txt");

	    try {
	        FileOutputStream f = new FileOutputStream(file);
	        PrintWriter pw = new PrintWriter(f);
	        pw.println(data);
	        pw.flush();
	        pw.close();
	        f.close();
	        logs.setText("File written");
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	        Log.i(TAG, "******* File not found. Did you" +
	                " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
	        logs.setText("File not written");
	    } catch (IOException e) {
	    	logs.setText("File written and no idea why..");
	        e.printStackTrace();
	    }   
	    
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
}