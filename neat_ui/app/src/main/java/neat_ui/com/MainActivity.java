package neat_ui.com;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.content.Intent;

import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    private final static int REQUEST_ENABLE_BT = 1;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    static ConnectedThread mConnectedThread;
    private static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private String deviceName = "ev3dev";
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket mmSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Vibrator vibe = (Vibrator) MainActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
        final MediaPlayer mp_manual = MediaPlayer.create(this, R.raw.pipes);
        final MediaPlayer mp_follow = MediaPlayer.create(this, R.raw.flick);
        final MediaPlayer mp_come = MediaPlayer.create(this, R.raw.trill);
        final MediaPlayer mp_lift = MediaPlayer.create(this, R.raw.coconuts);
        boolean isFollowing = false;
        boolean isComing = false;
        boolean isConnected = false;

        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "This device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
            return;
        }

        checkWhetherConnected(isConnected);
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        pairDevices();

        final Button button_manual = findViewById(R.id.manual_mode);

        button_manual.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {
                mp_manual.start();
                vibe.vibrate(100);
                goToManualMovementActivity();
            }

        });

        Button button_follow = findViewById(R.id.follow);

        if(isFollowing) {
            button_follow.setEnabled(false);
            button_follow.setBackgroundResource(R.drawable.disabled_round_corners);
        } else {
            button_follow.setEnabled(true);
            button_follow.setOnClickListener(new View.OnClickListener() {

                @Override

                public void onClick(View v) {
                    mp_follow.start();
                    vibe.vibrate(100);
                }

            });
        }

        Button button_come = findViewById(R.id.come_to_me);

        if(isComing) {
            button_come.setEnabled(false);
            button_come.setBackgroundResource(R.drawable.disabled_round_corners);
        } else {
            button_come.setEnabled(true);
            button_come.setOnClickListener(new View.OnClickListener() {

                @Override

                public void onClick(View v) {
                    mp_come.start();
                    vibe.vibrate(100);
                }

            });
        }

        Button button_lift = findViewById(R.id.lift_controls);

        button_lift.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {
                mp_lift.start();
                vibe.vibrate(100);
                goToLiftMovementActivity();
            }

        });
    }

    private void goToManualMovementActivity() {

        Intent intent = new Intent(this, ManualMovementActivity.class);

        startActivity(intent);

    }

    private void goToLiftMovementActivity() {

        Intent intent = new Intent(this, LiftMovementActivity.class);

        startActivity(intent);

    }

    private void checkWhetherConnected(boolean isConnected) {
        if(!isConnected) {
            showConnectionPopup();
        }
    }

    private void showConnectionPopup() {
        DialogFragment newFragment = new ConnectionPopUp();
        newFragment.show(getSupportFragmentManager(), "connect pop-up");
    }

    public void pairDevices() {
        mmDevice = null;
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(deviceName)) {
                    mmDevice = device;
                    Toast.makeText(getApplicationContext(), "Paired with your N.E.A.T.",
                            Toast.LENGTH_LONG).show();
                    break;
                }
            }
        }
        if (mmDevice == null) {
            Toast.makeText(getApplicationContext(), "Not paired with your N.E.A.T.",
                    Toast.LENGTH_LONG).show();
            return;
            // perhaps add in ability to discover here
        }

        ConnectThread connect = new ConnectThread(mmDevice, mUUID);
        connect.start();
    }

    public class ConnectThread extends Thread {

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run(){
            //cancelDiscovery just in case since it reduces likelihood of a successful connection
            mBluetoothAdapter.cancelDiscovery();
            //Creating the socket.
            BluetoothSocket tmp = null;
            try {
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
            //socket created, try to connect
            try {
                mmSocket.connect();
                System.out.println("Connected");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Can't connect to socket");
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    System.out.println("socket did not close");
                    e2.printStackTrace();
                }
                return;
            }
            //start a new thread for communications
            connected(mmSocket);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void connected(BluetoothSocket mmSocket) {
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];  // buffer store for the stream

            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    //do something with this
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }


        public void write(String message) {
            byte[] bytes = message.getBytes(Charset.defaultCharset());
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}