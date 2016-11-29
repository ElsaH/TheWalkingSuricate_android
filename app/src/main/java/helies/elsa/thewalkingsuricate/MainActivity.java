package helies.elsa.thewalkingsuricate;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

import static java.lang.System.exit;

public class MainActivity extends Activity implements SensorEventListener {
    // Accéléromètre
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private float last_x, last_y, last_z;


    // Bluetooth
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //private static String address = "74:2F:68:B2:27:75";
    private static String address = "00:1A:7D:DA:71:13";
    //private static String address = "A0:88:69:6C:C6:C1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        CheckBTState();

    }

    @Override
    protected void onStart() {
        super.onStart();
        startGame();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
    }

    private String chooseBluetoothDevice(Set<BluetoothDevice> set){

        if(set.size() == 0) {
            exit(0);
        }

        Button validate = (Button) findViewById(R.id.validate);
        LinearLayout choix = (LinearLayout) findViewById(R.id.choices);
        RadioGroup group ;
        // https://www.mkyong.com/android/android-radio-buttons-example/

        final String[] choices = new String[set.size()];
        final String[] adresses = new String[set.size()];
        RadioButton[] list = new RadioButton[set.size()];

        final int[] choose = new int[1];
        final BluetoothDevice[] choosen = new BluetoothDevice[1];

        int i = 0;
        for (Object elem : set) {
            choices[i] = ((BluetoothDevice) elem).getName();
            adresses[i] = ((BluetoothDevice) elem).getAddress();


        }


        validate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "You selected "+ adresses[choose[0]], Toast.LENGTH_SHORT).show();
                Log.i("info", "Choix : " + choose[0] + " donc addresse : "+ adresses[choose[0]]);
                //choosen[0] = btAdapter.getRemoteDevice(adresses[choose[0]]);

            }
        });

        AlertDialog alert = popup.create();
        alert.show();
        setContentView(R.layout.positions);
        return adresses[choose[0]];
    }

    @Override
    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);


        /*
        Pour récupérer le device sans passer par l'adresse.
        */
        Set<BluetoothDevice> bondedDevices = btAdapter.getBondedDevices();
        String address = chooseBluetoothDevice(bondedDevices);

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
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        if (Math.abs(last_z - z) > 10 && Math.abs(last_y - y) > 10){
            // Détection d'une coupe
            sendMessage("COUPE\n");
            Toast.makeText(MainActivity.this, "Coupe", Toast.LENGTH_SHORT).show();
        }

        last_x = x;
        last_y = y;
        last_z = z;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
                Toast.makeText(MainActivity.this, "Arme1", Toast.LENGTH_SHORT).show();
                arme1.setEnabled(false);
                arme2.setEnabled(true);
                sendMessage("ARME1\n");
            }
        });

        arme2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Toast.makeText(MainActivity.this, "Arme2", Toast.LENGTH_SHORT).show();
                arme2.setEnabled(false);
                arme1.setEnabled(true);
                sendMessage("ARME2\n");
            }
        });

        trex.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Toast.makeText(MainActivity.this, "T-rex", Toast.LENGTH_SHORT).show();
                sendMessage("TREX\n");
            }
        });

        bombe.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Toast.makeText(MainActivity.this, "bombe", Toast.LENGTH_SHORT).show();
                sendMessage("BOMBE\n");
            }
        });
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

    private void CheckBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            AlertBox("Fatal Error", "Bluetooth Not supported. Aborting.");
        } else {
            if (!btAdapter.isEnabled()) {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
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
