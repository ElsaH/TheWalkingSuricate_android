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
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

import static android.R.id.input;

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
    public static String address ;

    private AudioRecord audioRecord;
    private final int sample = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);
    private final int size = AudioRecord.getMinBufferSize(sample, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

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
        sendMessage("PAUSE\n");
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

        final TextView waiting = (TextView) findViewById(R.id.waiting);
        waiting.setText("");

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, size);
        if(audioRecord == null) {
            Log.i("Fils de pute", "C'est nul");
        }

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


                audioRecord.startRecording();
                boolean grognement = false;
                waiting.setText("En attente de grognement !");

                while(!grognement){
                    short sData[] = new short[size];
                    audioRecord.read(sData, 0, size);

                    if(isGrognement(sData)) grognement = true;
                }

                Vibrator vibrate = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                vibrate.vibrate(500);

                waiting.setText("");
                audioRecord.stop();
                trex.setEnabled(false);
                sendMessage("TREX\n");
            }
        });

        bombe.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                bombe.setEnabled(false);
                sendMessage("BOMBE\n");
            }
        });
    }

    private void showAlertBox(){

    }

    private boolean isGrognement(short[] sData) {
        for(int i=0; i< sData.length; i++) {
            if(sData[i] > 12000) {
                Log.i("INFO", String.valueOf(sData[i]));
                return true;
            }
        }

        return false;
    }

    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

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
