package helies.elsa.thewalkingsuricate;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by Elsa on 24/11/2016.
 */

public class BluetoothInterface {

        private BluetoothDevice device = null;
        private BluetoothSocket socket = null;
        private InputStream receiveStream = null;
        private OutputStream outputStream = null;
        private final String deviceName = "GALIA";
        private ReceiverThread receiverThread;
        private BluetoothAdapter bluetoothAdapter;
        Handler handler;

        public BluetoothInterface(Handler hstatus, Handler h) {
            handler = hstatus;
            receiverThread = new ReceiverThread(h);
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        public boolean hasBluetooth() {
            return (bluetoothAdapter != null);
        }

        public boolean findDevice() {
            // Liste des devices
            Set<BluetoothDevice> setpairedDevices = bluetoothAdapter.getBondedDevices();
            BluetoothDevice[] pairedDevices = (BluetoothDevice[]) setpairedDevices.toArray(new BluetoothDevice[setpairedDevices.size()]);

            for(int i=0;i<pairedDevices.length;i++) {
                if(pairedDevices[i].getName().contains(deviceName)) {
                    device = pairedDevices[i];
                    try {
                        // On récupère le socket de notre périphérique
                        socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                        receiveStream = socket.getInputStream();
                        outputStream = socket.getOutputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            }
            return false;
        }

        public void sendMessage(String s) throws IOException {
            outputStream.write(s.getBytes());
        }

        public void connect() {
            new Thread() {
                @Override public void run() {
                    try {
                        socket.connect();

                        Message msg = handler.obtainMessage();
                        msg.arg1 = 1;
                        handler.sendMessage(msg);

                        receiverThread.start();

                    } catch (IOException e) {
                        Log.v("N", "Connection Failed : "+e.getMessage());
                        e.printStackTrace();
                    }
                }
            }.start();
        }

        public void close() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public BluetoothDevice getDevice() {
            return device;
        }



        private class ReceiverThread extends Thread {
            Handler handler;
            ReceiverThread(Handler h) {
                handler = h;
            }

            @Override
            public void run() {
                while(true) {
                    try {
                        if(receiveStream.available() > 0) {

                            byte buffer[] = new byte[100];
                            int k = receiveStream.read(buffer, 0, 100);

                            if(k > 0) {
                                byte rawdata[] = new byte[k];
                                for(int i=0;i<k;i++)
                                    rawdata[i] = buffer[i];

                                String data = new String(rawdata);

                                Message msg = handler.obtainMessage();
                                Bundle b = new Bundle();
                                b.putString("receivedData", data);
                                msg.setData(b);
                                handler.sendMessage(msg);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

}
