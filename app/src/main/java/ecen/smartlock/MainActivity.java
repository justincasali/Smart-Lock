package ecen.smartlock;

import android.app.Activity;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import android.content.Intent;
import android.view.View;

// Libraries for Accelerometer
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

// Import SPP Libary
import app.akexorcist.bluetotohspp.library.*;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    BluetoothSPP bt;
    SensorManager mSensorManager;

    TextView lockText;
    boolean lock = false;
    boolean block = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lockText = (TextView) findViewById(R.id.textView);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        bt = new BluetoothSPP(getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (bt.isBluetoothEnabled() && bt.isBluetoothAvailable()) {
            //bt.setupService();
            //bt.startService(BluetoothState.DEVICE_OTHER);
        }
        lockText.setText(R.string.unlocked);
        //bt.connect("00:15:FF:F3:23:78");
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                bt.connect(data);
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
            } else {
                // Do something if user doesn't choose any device (Pressed back)
            }
        }
    }

    // Button Code
    public void onClick(View view) {

        if (view.getId() == R.id.setup) {
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);


            bt.setBluetoothStateListener(new BluetoothSPP.BluetoothStateListener() {
                public void onServiceStateChanged(int state) {
                    if (state == BluetoothState.STATE_CONNECTED) {
                        lockText.setText("Connected");
                    }
                        // Do something when successfully connected
                    else if (state == BluetoothState.STATE_CONNECTING)
                        lockText.setText("Connecting");
                        // Do something while connecting
                    else if (state == BluetoothState.STATE_LISTEN) lockText.setText("Listen");
                        // Do something when device is waiting for connection
                    else if (state == BluetoothState.STATE_NONE) lockText.setText("None");
                    // Do something when device don't have any connection
                }
            });

        }

        if (view.getId() == R.id.toggle) {
            toggle();
        }

    }


    // Lock / Unlock the Lock
    public void toggle() {
        if (lock) {
            bt.send("0", false);
            lockText.setText(R.string.unlocked);
            lock = false;
        }
        else {
            bt.send("1", false);
            lockText.setText(R.string.locked);
            lock = true;
        }
    }

    // Sensor Code
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_LINEAR_ACCELERATION) return;
        float currentMean = (Math.abs(event.values[0]) + Math.abs(event.values[1]) + Math.abs(event.values[2])) / 3f;

        if (currentMean > 4 && !block) {
            block = true;
            toggle();
        }

        if (currentMean < 1) {
            block = false;
        }

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

}

