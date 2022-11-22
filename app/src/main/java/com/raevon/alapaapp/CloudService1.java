package com.raevon.alapaapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class CloudService1 {
    private final Handler cloudHandler;
    private final BluetoothAdapter cloudAdapter;
    private BluetoothSocket cloudSocket;
    private BluetoothDevice cloudDevice;

    private ListenerThread cloudListener;
    private ConnectorThread cloudConnector;
    private ConnectedThread cloudConnected;
    private DisconnectThread cloudDisconnect;

    private InputStream InStream;
    private OutputStream OutStream;

    private Set<BluetoothDevice> cloudPairedDevices;
    private ArrayAdapter<String> cloudArrayAdapter;
    private final ArrayAdapter<String> cloudLogMonitor;
    private final ArrayList<String> cloudLogMessageList;

    private String cloudDeviceName;
    private String bluetooth_status;
    private String command;
    private String bread;

    public static boolean ReadDisconnectConfirmation;

    private boolean isReading;

    public static final UUID myUUID_SECURE
            = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID myUUID_INSECURE
            = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final String app_name = "IACL ARDUINO";

    private int connection_state;
    private int butler_state;
    private int rfcomm_state;
    private int clicks;

    // CONNECTIVITY_STATE
    public static final int NOT_CONNECTED = 0;
    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;
    public static final int DISCONNECT = 3;
    // BUTLER_STATE
    public static final int CONNECTOR_MODE = 4;
    public static final int LISTENER_MODE = 5;
    // RFCOMM_STATE
    public static final int RFCOMM_SECURE = 6;
    public static final int RFCOMM_INSECURE = 7;
    // UI_UPDATE
    public static final int RFCOMM_UI_UPDATE = 8;
    public static final int BUTLER_UI_UPDATE = 9;

    public CloudService1(
            Context context,
            Handler handler,
            ArrayList<String> arrayList_log_messages,
            ArrayAdapter<String> arrayAdapter_log_monitor,
            ArrayAdapter<String> arrayAdapter_paired_devices
            ) {
        cloudDevice = null;
        cloudSocket = null;
        cloudAdapter = BluetoothAdapter.getDefaultAdapter();
        cloudHandler = handler;
        cloudLogMessageList = arrayList_log_messages;
        cloudLogMonitor = arrayAdapter_log_monitor;
        cloudArrayAdapter = arrayAdapter_paired_devices;
        connection_state = NOT_CONNECTED;
        rfcomm_state = RFCOMM_SECURE;
        butler_state = CONNECTOR_MODE;
        clicks = 0;
        ReadDisconnectConfirmation = false;
    }

    public void toggleRfcomm() {
        if (rfcomm_state == RFCOMM_SECURE) {
            rfcomm_state = RFCOMM_INSECURE;
        }
        else if (rfcomm_state == RFCOMM_INSECURE) {
            rfcomm_state = RFCOMM_SECURE;
        }
        updateRFCOMM();
    }

    public void toggleButler() {
        if (butler_state == CONNECTOR_MODE) {
            butler_state = LISTENER_MODE;
        }
        else if (butler_state == LISTENER_MODE) {
            butler_state = CONNECTOR_MODE;
        }
        updateButler();
    }

    public void togglePairedDevicesList() {
        if (butler_state == CONNECTOR_MODE) {
            if (connection_state != CONNECTED) {
                updatePairedDevicesList();
            }
            else {
                cloudHandler.sendMessage(cloudHandler.obtainMessage
                        (Constants.MESSAGE_TOASTY, Constants.disconnect_first_notification));
            }
        }
        else if (butler_state == LISTENER_MODE) {
            cloudArrayAdapter.clear();
            bread = "Device on Listener Mode, please switch to Connector Mode.";
            cloudHandler.sendMessage(cloudHandler.obtainMessage(Constants.MESSAGE_TOASTY, bread));
        }

    }

    private void checkBluetoothState() {
        if (cloudAdapter == null) {
            bluetooth_status = Constants.bluetooth_unavailable;
        }
        else if (!cloudAdapter.isEnabled()) {
            bluetooth_status = Constants.bluetooth_isOFF;
        }
    }

    private void updateRFCOMM() {
        if (rfcomm_state == RFCOMM_SECURE) {
            if (butler_state == CONNECTOR_MODE) {
                String status_update = "Device Mode: Connector (Secure)";
                cloudHandler.sendMessage(cloudHandler.obtainMessage
                        (Constants.MESSAGE_UI_UPDATE, RFCOMM_UI_UPDATE, -1, status_update));
            }
            else if (butler_state == LISTENER_MODE) {
                String status_update = "Device Mode: Listener (Secure)";
                cloudHandler.sendMessage(cloudHandler.obtainMessage
                        (Constants.MESSAGE_UI_UPDATE, RFCOMM_UI_UPDATE, -1, status_update));
            }
            String rfcomm_update = "Device is using secure RFCOMM channels";
            cloudHandler.sendMessage(cloudHandler.obtainMessage
                    (Constants.MESSAGE_TOASTY, rfcomm_update));
        }
        else if (rfcomm_state == RFCOMM_INSECURE) {
            if (butler_state == CONNECTOR_MODE) {
                String status_update = "Device Mode: Connector (Insecure)";
                cloudHandler.sendMessage(cloudHandler.obtainMessage
                        (Constants.MESSAGE_UI_UPDATE, RFCOMM_UI_UPDATE, -1, status_update));
            }
            else if (butler_state == LISTENER_MODE) {
                String status_update = "Device Mode: Listener (Insecure)";
                cloudHandler.sendMessage(cloudHandler.obtainMessage
                        (Constants.MESSAGE_UI_UPDATE, RFCOMM_UI_UPDATE, -1, status_update));
            }
            String rfcomm_update = "Device is using insecure RFCOMM channels";
            cloudHandler.sendMessage(cloudHandler.obtainMessage
                    (Constants.MESSAGE_TOASTY, rfcomm_update));
        }
    }


    public void updateButler() {
        if (butler_state == CONNECTOR_MODE) {
            if (cloudListener != null) {
                closeCloudSocket();
                cloudListener = null;
            }
            if (rfcomm_state == RFCOMM_SECURE) {
                String status_update = "Device Mode: Connector (Secure)";
                cloudHandler.sendMessage(cloudHandler.obtainMessage
                        (Constants.MESSAGE_UI_UPDATE, BUTLER_UI_UPDATE, -1, status_update));
            }
            else if (rfcomm_state == RFCOMM_INSECURE) {
                String status_update = "Device Mode: Connector (Insecure)";
                cloudHandler.sendMessage(cloudHandler.obtainMessage
                        (Constants.MESSAGE_UI_UPDATE, BUTLER_UI_UPDATE, -1, status_update));
            }
        }
        else if (butler_state == LISTENER_MODE) {
            if (cloudConnector != null) {
                closeCloudSocket();
                cloudConnector = null;
            }
            if (rfcomm_state == RFCOMM_SECURE) {
                String status_update = "Device Mode: Listener (Secure)";
                cloudHandler.sendMessage(cloudHandler.obtainMessage
                        (Constants.MESSAGE_UI_UPDATE, BUTLER_UI_UPDATE, -1, status_update));
            }
            else if (rfcomm_state == RFCOMM_INSECURE) {
                String status_update = "Device Mode: Listener (Insecure)";
                cloudHandler.sendMessage(cloudHandler.obtainMessage
                        (Constants.MESSAGE_UI_UPDATE, BUTLER_UI_UPDATE, -1, status_update));
            }
            cloudListener = new ListenerThread();
            cloudListener.start();
        }
    }

    private void updatePairedDevicesList() {
        cloudArrayAdapter.clear();
        cloudPairedDevices = cloudAdapter.getBondedDevices();
        if (butler_state == CONNECTOR_MODE) {
            if (cloudPairedDevices.size() > 0) {
                for (BluetoothDevice device : cloudPairedDevices) {
                    cloudArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
            else {
                checkBluetoothState();
                if (bluetooth_status.equals(Constants.bluetooth_unavailable)) {
                    cloudHandler.sendMessage(cloudHandler.obtainMessage
                            (Constants.MESSAGE_TOASTY, bluetooth_status));
                }
                else if (bluetooth_status.equals(Constants.bluetooth_isOFF)) {
                    cloudHandler.sendMessage(cloudHandler.obtainMessage
                            (Constants.MESSAGE_TOASTY, bluetooth_status));
                }
                else {
                    cloudArrayAdapter.add(Constants.no_paired_devices);
                }
            }
        }
        else if (butler_state == LISTENER_MODE){
            checkBluetoothState();
            if (bluetooth_status.equals(Constants.bluetooth_unavailable)) {
                cloudHandler.sendMessage(cloudHandler.obtainMessage
                        (Constants.MESSAGE_TOASTY, bluetooth_status));
            }
            else if (bluetooth_status.equals(Constants.bluetooth_isOFF)) {
                cloudHandler.sendMessage(cloudHandler.obtainMessage
                        (Constants.MESSAGE_TOASTY, bluetooth_status));
            }
            else {
                cloudHandler.sendMessage(cloudHandler.obtainMessage
                        (Constants.MESSAGE_TOASTY, Constants.not_inConnector_mode));
            }
        }
    }

    public void adapterOnClick(View view) {
        if (butler_state == CONNECTOR_MODE) {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);
            String name = info.substring(0, info.length() - 17);

            cloudConnector = new ConnectorThread(name, address);
            cloudConnector.start();
        }
        else if (butler_state == LISTENER_MODE) {
            bread = "Device is on Listener Mode, please switch to Connector Mode.";
            cloudHandler.sendMessage(cloudHandler.obtainMessage
                    (Constants.MESSAGE_TOASTY, bread));
        }
    }

    public void updateLogMonitor(String command) {
        String logMessage;
        if (isReading) {
            logMessage = cloudDeviceName + ": " + command;
        }
        else {
            logMessage = "You: " + command;
        }
        if (cloudLogMonitor.getCount() > 7) {
            cloudLogMonitor.clear();
        }
        cloudLogMonitor.add(logMessage);
    }

    public void closeCloudSocket() {
        if (cloudSocket != null) {
            try {
                cloudSocket.close();
                cloudSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            cloudHandler.sendMessage(cloudHandler.obtainMessage
                    (Constants.MESSAGE_TOASTY, Constants.disconnected));
        }
        connection_state = NOT_CONNECTED;
    }

    private class ListenerThread extends Thread {
        private BluetoothServerSocket cloudServerSocket;

        public ListenerThread() {
            BluetoothServerSocket tmp = null;
            try {
                if (butler_state == LISTENER_MODE) {
                    if (rfcomm_state == RFCOMM_SECURE) {
                        tmp = cloudAdapter.listenUsingRfcommWithServiceRecord(app_name, myUUID_SECURE);
                    } else if (rfcomm_state == RFCOMM_INSECURE) {
                        tmp = cloudAdapter.listenUsingInsecureRfcommWithServiceRecord(app_name, myUUID_INSECURE);
                    }
                    cloudServerSocket = tmp;
                } else if (butler_state == CONNECTOR_MODE) {
                    cloudServerSocket = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (connection_state != CONNECTED) {
                try {
                    cloudSocket = cloudServerSocket.accept();
                    connection_state = CONNECTING;
                    cloudHandler.sendMessage(cloudHandler.obtainMessage
                            (Constants.MESSAGE_TOASTY, Constants.connecting));

                } catch (IOException e) {
                    e.printStackTrace();
                    cloudHandler.sendMessage(cloudHandler.obtainMessage
                            (Constants.MESSAGE_TOASTY, Constants.connection_failed));
                    break;
                }
                if (cloudSocket != null) {
                    try {
                        cloudServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    connection_state = CONNECTED;
                    cloudConnected = new ConnectedThread();
                    cloudConnected.start();
                    cloudDeviceName = cloudSocket.getRemoteDevice().getName();
                    String connected_to = "Connected To: " + cloudDeviceName;
                    cloudHandler.sendMessage(cloudHandler.obtainMessage
                            (Constants.MESSAGE_CONNECTION, CONNECTED, -1, connected_to));
                }
            }
        }
    }

    private class ConnectorThread extends Thread {
        private final String device_address;

        public ConnectorThread(String name, String address) {
            cloudDeviceName = name;
            device_address = address;
            cloudDevice = cloudAdapter.getRemoteDevice(device_address);
        }
        @Override
        public void run() {
            connection_state = CONNECTING;
            cloudHandler.sendMessage(cloudHandler.obtainMessage
                    (Constants.MESSAGE_TOASTY, Constants.connecting));
            try {
                if (rfcomm_state == RFCOMM_SECURE) {
                    cloudSocket = cloudDevice.createRfcommSocketToServiceRecord(myUUID_SECURE);
                } else if (rfcomm_state == RFCOMM_INSECURE) {
                    cloudSocket = cloudDevice.createInsecureRfcommSocketToServiceRecord(myUUID_INSECURE);
                }
            } catch (IOException e) {
                connection_state = NOT_CONNECTED;
                cloudHandler.sendMessage(cloudHandler.obtainMessage
                        (Constants.MESSAGE_TOASTY, Constants.connection_failed));
                e.printStackTrace();
            }
            try {
                cloudSocket.connect();
                connection_state = CONNECTED;
            } catch (IOException e) {
                try {
                    cloudSocket.close();
                    connection_state = NOT_CONNECTED;
                    cloudHandler.sendMessage(cloudHandler.obtainMessage
                            (Constants.MESSAGE_TOASTY, Constants.connection_failed));
                } catch (IOException e2) {
                    connection_state = NOT_CONNECTED;
                    cloudHandler.sendMessage(cloudHandler.obtainMessage
                            (Constants.MESSAGE_TOASTY, Constants.failed_socket_creation));
                }
            }
            if (connection_state == CONNECTED) {
                cloudConnected = new ConnectedThread();
                cloudConnected.start();
                String connected_to = "Connected To: " + cloudDeviceName;
                cloudHandler.sendMessage(cloudHandler.obtainMessage
                        (Constants.MESSAGE_CONNECTION, CONNECTED, -1, connected_to));
            }
        }
    }

    private class ConnectedThread extends Thread {
        public ConnectedThread() {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = cloudSocket.getInputStream();
                tmpOut = cloudSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            InStream = tmpIn;
            OutStream = tmpOut;
        }
        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (connection_state == CONNECTED) {
                try {
                    bytes = InStream.available();
                    if (bytes != 0) {
                        SystemClock.sleep(100);
                        bytes = InStream.available();
                        bytes = InStream.read(buffer, 0, bytes);
                        isReading = true;
                        command = new String(buffer, StandardCharsets.UTF_8);
                        if (command.contains("0")) {
                            ReadDisconnectConfirmation = true;
                            cloudHandler.sendMessage(cloudHandler.obtainMessage
                                    (Constants.MESSAGE_DISCONNECT_DEVICE, command));
                            connection_state = NOT_CONNECTED;
                        }
                        cloudHandler.sendMessage(cloudHandler.obtainMessage
                                (Constants.MESSAGE_READ, command));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    cloudHandler.sendMessage(cloudHandler.obtainMessage
                            (Constants.MESSAGE_TOASTY, Constants.connection_lost));
                    break;
                }
            }
        }
    }

    public static boolean getReceivedDisconnectConfirmation() {
        return ReadDisconnectConfirmation;
    }

    public static void setReceivedDisconnectConfirmation(boolean bool) {
        ReadDisconnectConfirmation = bool;
    }

    public void write(String command) {
        isReading = false;
        updateLogMonitor(command);
        byte[] bytes = command.getBytes(StandardCharsets.UTF_8);
        try {
            OutStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        cloudDisconnect = new DisconnectThread();
        cloudDisconnect.start();
    }

    private class DisconnectThread extends Thread {
        public DisconnectThread() {
            clicks++;
        }
        @Override
        public void run() {
            if (clicks == 1) {
                cloudHandler.sendMessage(cloudHandler.obtainMessage
                        (Constants.MESSAGE_TOASTY, Constants.click_notification_2_more));
            }
            else if (clicks == 2) {
                cloudHandler.sendMessage(cloudHandler.obtainMessage
                        (Constants.MESSAGE_TOASTY, Constants.click_notification_1_more));
            }
            else {
                cloudHandler.sendMessage(cloudHandler.obtainMessage
                        (Constants.MESSAGE_TOASTY, Constants.disconnecting));
                cloudHandler.sendMessage(cloudHandler.obtainMessage
                        (Constants.MESSAGE_CONNECTION, DISCONNECT, -1, -1));
                connection_state = NOT_CONNECTED;
                clicks = 0;
            }
        }
    }
}
