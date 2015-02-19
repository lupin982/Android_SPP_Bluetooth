/*
 * Copyright 2014 Akexorcist
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.akexorcist.bluetoothspp;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;
import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.OnDataReceivedListener;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;



public class SearchDevice extends Activity {

    public static final int READ_STATUS_COMMAND = 100;
    public static final int UPDATE_TIME_COMMAND = 200;
    public static final int SET_TEMP_COMMAND = 300;
    public static final int SET_TIME_COMMAND = 400;

    BluetoothSPP bt;

    int lastSentCommand = -1;

    Button bReadStatus;
    Button bUpdateTime;
    Button bSetTemperature;
    Button bSetTime;

    TextView tvReadStatusResult;
    TextView tvUpdateTimeResult;
    TextView tvSetTempResult;
    TextView tvSetStartStopTimeResult;

    TextView tvHeaterValue;
    TextView tvLightValue;
    TextView tvTempValue;
    TextView tvHumidityValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_device);

        bt = new BluetoothSPP(this);

        // Read Status label
        tvHeaterValue = (TextView)findViewById(R.id.tvHeaterValue);
        tvLightValue = (TextView)findViewById(R.id.tvLightValue);
        tvTempValue = (TextView)findViewById(R.id.tvTempValue);
        tvHumidityValue = (TextView)findViewById(R.id.tvHumidityValue);

        tvReadStatusResult = (TextView)findViewById(R.id.tvReadStatusResult);
        tvUpdateTimeResult = (TextView)findViewById(R.id.tvUpdateTimeResult);
        tvSetTempResult = (TextView)findViewById(R.id.tvSetTempResult);
        tvSetStartStopTimeResult = (TextView)findViewById(R.id.tvSetStartStopTimeResult);

        if(!bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }


        // Receive callback
        bt.setOnDataReceivedListener(new OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {

                if (READ_STATUS_COMMAND == lastSentCommand) {
                    if (message.length() >= 6) {

                        if (0 == (int) message.charAt(0)) {
                            tvHeaterValue.setText("OFF");
                        } else {
                            tvHeaterValue.setText("ON");
                        }
                        if (0 == (int) message.charAt(1)) {
                            tvLightValue.setText("OFF");
                        } else {
                            tvLightValue.setText("ON");
                        }
                        int b = (int)message.charAt(2);
                        tvHumidityValue.setText(String.valueOf(b).toCharArray(), 0, String.valueOf(b).toCharArray().length);
                        //tvHumidityValue.setText(new char[]{message.charAt(2)}, 0, 1);
                        b = (int)message.charAt(3);
                        tvTempValue.setText(String.valueOf(b).toCharArray(), 0, String.valueOf(b).toCharArray().length);
                        //tvTempValue.setText(new char[]{message.charAt(3)}, 0, 1);

                        tvReadStatusResult.setText("OK");
                        tvUpdateTimeResult.setText("N.A.");
                        tvSetTempResult.setText("N.A.");
                        tvSetStartStopTimeResult.setText("N.A.");
                    }
                }
                else if(message.charAt(0) == '*') {
                    if (UPDATE_TIME_COMMAND == lastSentCommand) {
                        tvReadStatusResult.setText("N.A.");
                        tvUpdateTimeResult.setText("OK");
                        tvSetTempResult.setText("N.A.");
                        tvSetStartStopTimeResult.setText("N.A.");
                    }
                    else if (SET_TEMP_COMMAND == lastSentCommand) {
                        tvReadStatusResult.setText("N.A.");
                        tvUpdateTimeResult.setText("N.A.");
                        tvSetTempResult.setText("OK");
                        tvSetStartStopTimeResult.setText("N.A.");
                    }
                    else if (SET_TIME_COMMAND == lastSentCommand) {
                        tvReadStatusResult.setText("N.A.");
                        tvUpdateTimeResult.setText("N.A.");
                        tvSetTempResult.setText("N.A.");
                        tvSetStartStopTimeResult.setText("OK");
                    }
                    lastSentCommand = -1;
                }
                else
                {
                    tvReadStatusResult.setText("ERROR");
                    tvUpdateTimeResult.setText("N.A.");
                    tvSetTempResult.setText("N.A.");
                    tvSetStartStopTimeResult.setText("N.A.");
                }
                Log.i("Check", "Length : " + data.length);
                Log.i("Check", "Message : " + message);
            }

        });

        // Connect
        Button btnConnect = (Button)findViewById(R.id.bconnect);
        btnConnect.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(SearchDevice.this, DeviceList.class);
                    intent.putExtra("bluetooth_devices", "Bluetooth devices");
                    intent.putExtra("no_devices_found", "No device");
                    intent.putExtra("scanning", "Scanning");
                    intent.putExtra("scan_for_devices", "Search");
                    intent.putExtra("select_device", "Select");
                    intent.putExtra("layout_list", R.layout.device_layout_list);
                    intent.putExtra("layout_text", R.layout.device_layout_text);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        });

        // bt state listener
        bt.setBluetoothStateListener(new BluetoothSPP.BluetoothStateListener() {
            public void onServiceStateChanged(int state) {

                tvReadStatusResult.setText("N.A.");
                tvUpdateTimeResult.setText("N.A.");
                tvSetTempResult.setText("N.A.");
                tvSetStartStopTimeResult.setText("N.A.");

                lastSentCommand = -1;

                TextView tvConnected = (TextView)findViewById(R.id.tvConnected);
                if(state == BluetoothState.STATE_CONNECTED)
                {
                    tvConnected.setText("Connected");
                    Log.i("Check", "State : Connected");
                }
                else if(state == BluetoothState.STATE_CONNECTING)
                {
                    tvConnected.setText("Connecting");
                    Log.i("Check", "State : Connecting");
                }
                else if(state == BluetoothState.STATE_LISTEN)
                {
                    tvConnected.setText("Listen");
                    Log.i("Check", "State : Listen");
                }
                else if(state == BluetoothState.STATE_NONE)
                {
                    tvConnected.setText("None");
                    Log.i("Check", "State : None");
                }
            }

        });

        // Read Status
        bReadStatus = (Button)findViewById(R.id.bReadStatus);
        bReadStatus.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                byte[] sendCommand = new byte[]{0x23, 0x3};
                //sendCommand[0] = 0x23;
                //sendCommand[1] = 0x3;
                lastSentCommand = READ_STATUS_COMMAND;

                tvReadStatusResult.setText("N.A.");
                tvUpdateTimeResult.setText("N.A.");
                tvSetTempResult.setText("N.A.");
                tvSetStartStopTimeResult.setText("N.A.");

                bt.send(sendCommand, false);

            }
        });

        // Update Time
        bUpdateTime = (Button)findViewById(R.id.bUpdateTime);
        bUpdateTime.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                byte[] sendCommand = new byte[6];
                byte[] checksumCommand = new byte[3];
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                int minute = Calendar.getInstance().get(Calendar.MINUTE);
                int second = Calendar.getInstance().get(Calendar.SECOND);
                sendCommand[0] = 0x23;
                sendCommand[1] = 0x2;
                sendCommand[2] = (byte)hour;
                checksumCommand[0] = (byte)hour;
                sendCommand[3] = (byte)minute;
                checksumCommand[1] = (byte)minute;
                sendCommand[4] = (byte)second;
                checksumCommand[2] =(byte)second;
                sendCommand[5] = (byte)calculateChecksum(checksumCommand);

                tvReadStatusResult.setText("N.A.");
                tvUpdateTimeResult.setText("N.A.");
                tvSetTempResult.setText("N.A.");
                tvSetStartStopTimeResult.setText("N.A.");

                lastSentCommand = UPDATE_TIME_COMMAND;

                bt.send(sendCommand, false);
            }
        });

        // Set Temperature
        bSetTemperature = (Button)findViewById(R.id.bSetTemp);
        bSetTemperature.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                EditText etMinTemp = (EditText)findViewById(R.id.etMinTemp);
                EditText etMaxTemp = (EditText)findViewById(R.id.etMaxTemp);
                byte[] sendCommand = new byte[5];
                byte[] checksumCommand = new byte[2];
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                int minute = Calendar.getInstance().get(Calendar.MINUTE);
                int second = Calendar.getInstance().get(Calendar.SECOND);
                sendCommand[0] = 0x23;
                sendCommand[1] = 0x0;
                sendCommand[2] = (byte)(Integer.parseInt(etMaxTemp.getText().toString()));
                checksumCommand[0] = sendCommand[2];
                sendCommand[3] = (byte)(Integer.parseInt(etMinTemp.getText().toString()));
                checksumCommand[1] = sendCommand[3];
                sendCommand[4] = (byte)calculateChecksum(checksumCommand);

                tvReadStatusResult.setText("N.A.");
                tvUpdateTimeResult.setText("N.A.");
                tvSetTempResult.setText("N.A.");
                tvSetStartStopTimeResult.setText("N.A.");

                lastSentCommand = SET_TEMP_COMMAND;

                bt.send(sendCommand, false);
            }
        });

        // Set Time
        bSetTime = (Button)findViewById(R.id.bSetStartStopTime);
        bSetTime.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                EditText etStartTime = (EditText)findViewById(R.id.etStartTime);
                EditText etStopTime = (EditText)findViewById(R.id.etStopTime);
                byte[] sendCommand = new byte[9];
                byte[] checksumCommand = new byte[6];

                String[] startTime = etStartTime.getText().toString().split(":");
                String[] stopTime = etStopTime.getText().toString().split(":");

                if((startTime.length < 2) || (stopTime.length < 2))
                    return;

                sendCommand[0] = 0x23;
                sendCommand[1] = 0x1;
                // Start Time
                sendCommand[2] = (byte)(Integer.parseInt(startTime[0]));
                checksumCommand[0] = sendCommand[2];
                sendCommand[3] = (byte)(Integer.parseInt(startTime[1]));
                checksumCommand[1] = sendCommand[3];
                if(3 == startTime.length) {
                    sendCommand[4] = (byte) (Integer.parseInt(startTime[2]));
                }
                else
                {
                    sendCommand[4] = 0;
                }
                checksumCommand[2] = sendCommand[3];
                // Start Time
                sendCommand[5] = (byte)(Integer.parseInt(stopTime[0]));
                checksumCommand[3] = sendCommand[5];
                sendCommand[6] = (byte)(Integer.parseInt(stopTime[1]));
                checksumCommand[4] = sendCommand[6];
                if(3 == startTime.length) {
                    sendCommand[7] = (byte) (Integer.parseInt(stopTime[2]));
                }
                else
                {
                    sendCommand[7] = 0;
                }
                checksumCommand[5] = sendCommand[7];
                sendCommand[8] = (byte)calculateChecksum(checksumCommand);

                tvReadStatusResult.setText("N.A.");
                tvUpdateTimeResult.setText("N.A.");
                tvSetTempResult.setText("N.A.");
                tvSetStartStopTimeResult.setText("N.A.");

                lastSentCommand = SET_TIME_COMMAND;

                bt.send(sendCommand, false);
            }
        });

    }

    public void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }

    public void onStart() {
        super.onStart();
        if(!bt.isBluetoothEnabled()) {
            bt.enable();
        } else {
            if(!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                setup();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void setup() {
    }

    public long calculateChecksum(byte[] buf) {
        int length = buf.length;
        int i = 0;

        long sum = 0;
        long data;

        // Handle all pairs
        while (length > 1) {
            // Corrected to include @Andy's edits and various comments on Stack Overflow
            data = (((buf[i] << 8) & 0xFF00) | ((buf[i + 1]) & 0xFF));
            sum += data;
            // 1's complement carry bit correction in 16-bits (detecting sign extension)
            if ((sum & 0xFFFF0000) > 0) {
                sum = sum & 0xFFFF;
                sum += 1;
            }

            i += 2;
            length -= 2;
        }

        // Handle remaining byte in odd length buffers
        if (length > 0) {
            // Corrected to include @Andy's edits and various comments on Stack Overflow
            sum += (buf[i] << 8 & 0xFF00);
            // 1's complement carry bit correction in 16-bits (detecting sign extension)
            if ((sum & 0xFFFF0000) > 0) {
                sum = sum & 0xFFFF;
                sum += 1;
            }
        }

        // Final 1's complement value correction to 16-bits
        sum = ~sum;
        sum = sum & 0xFFFF;
        return sum;

    }
}
