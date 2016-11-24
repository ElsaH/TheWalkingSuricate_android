package helies.elsa.thewalkingsuricate;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends Activity implements SensorEventListener {
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 600;

    private boolean mInitialized;

    //private GameLoop game;
    private BluetoothInterface bt = null;

    private final float NOISE = (float) 2.0;

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            String data = msg.getData().getString("receivedData");
            Toast.makeText(MainActivity.this, "Msg: "+data, Toast.LENGTH_SHORT).show();
        }
    };

    final Handler handlerStatus = new Handler() {
        public void handleMessage(Message msg) {
            int co = msg.arg1;
            if(co == 1) {
                Toast.makeText(MainActivity.this, "Connected with "+bt.getDevice().getName(), Toast.LENGTH_SHORT).show();
            } else if (co == 2) {
                Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bt = new BluetoothInterface(handlerStatus, handler);
        if (!bt.hasBluetooth()) {
            Toast.makeText(MainActivity.this, "Protocole bluetooth non supporté", Toast.LENGTH_SHORT).show();
            finish();
            System.exit(0);
        }

        if (!bt.findDevice()) {
            Toast.makeText(MainActivity.this, "Micro-contrôleur non-trouvé", Toast.LENGTH_SHORT).show();
            finish();
            System.exit(0);
        }

        bt.connect();


        setContentView(R.layout.positions);
        mInitialized = false;

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        TextView etat = (TextView) findViewById(R.id.envoie);

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        if (Math.abs(last_z - z) > 10 && Math.abs(last_y - y) > 10){
            // Détection d'une coupe
            try {
                etat.setText("COUPE");
                bt.sendMessage("COUPE");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        last_x = x;
        last_y = y;
        last_z = z;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
