package helies.elsa.thewalkingsuricate;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class GameActivity extends Activity implements SensorEventListener {

    // Accéléromètre
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private float last_y, last_z;

    // Bluetooth
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //private static String address = "74:2F:68:B2:27:75"; // Elsa
    //private static String address = "00:1A:7D:DA:71:13"; // Léon
    public static String address = "A0:88:69:6C:C6:C1"; // Adrien

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        startGame();
    }

    @Override
    protected void onPause() {
        senSensorManager.unregisterListener(this);
        //sendMessage("PAUSE\n");
        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                AlertBox("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
            }
        }

        try     {
            btSocket.close();
        } catch (IOException e2) {
            AlertBox("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        /*
        Pour récupérer le device sans passer par l'adresse.
        */
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            AlertBox("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        btAdapter.cancelDiscovery();

        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                AlertBox("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }
        sendMessage("START\n");
    }

    @Override
    protected void onStop() {
        sendMessage("STOP\n");
        senSensorManager.unregisterListener(this);
        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                AlertBox("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
            }
        }

        try     {
            btSocket.close();
        } catch (IOException e2) {
            AlertBox("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }

        super.onStop();
    }

    private void startGame() {
        setContentView(R.layout.game);
        addListener();
    }

    private void addListener(){
        final Button arme1 = (Button) findViewById(R.id.arme1);
        final Button arme2 = (Button) findViewById(R.id.arme2);
        final Button trex = (Button) findViewById(R.id.trex);
        final Button bombe = (Button) findViewById(R.id.bombe);

        arme1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                arme1.setEnabled(false);
                arme2.setEnabled(true);
                sendMessage("ARME1\n");
            }
        });

        arme2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                arme2.setEnabled(false);
                arme1.setEnabled(true);
                sendMessage("ARME2\n");
            }
        });

        trex.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                sendMessage("TREX\n");
            }
        });

        bombe.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                sendMessage("BOMBE\n");
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float y = event.values[1];
        float z = event.values[2];

        if (Math.abs(last_z - z) > 8 && Math.abs(last_y - y) > 8){
            // Détection d'une coupe
            sendMessage("COUPE\n");
            //Toast.makeText(this, "Coupe", Toast.LENGTH_SHORT).show();
        }

        last_y = y;
        last_z = z;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void sendMessage(String message){
        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            AlertBox("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }
        byte[] msgBuffer = message.getBytes();
        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";
            AlertBox("Fatal Error", msg);
        }
    }

    public void AlertBox( String title, String message ){
        new AlertDialog.Builder(this)
                .setTitle( title )
                .setMessage( message + " Press OK to exit." )
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                    }
                }).show();
    }
}
