package com.raevon.alapaapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class Cloud extends AppCompatActivity {

    private CloudService1 myCloudService1;
    private CloudService2 myCloudService2;

    private ArrayAdapter<String> myArrayAdapter;
    private ArrayAdapter<String> myLogMonitorArray;
    private ArrayList<String> myLogMessages;
    private ArrayList<String> initial_commands;

    private Button
            living_room_button,
            dining_room_button,
            bed_room_button,
            bath_room1_button,
            bath_room2_button,
            onl_button,
            toal_button,
            show_paired_devices_button;

    private ImageView
            toggle_rfcomm_button,
            toggle_butler_button,
            developer_notes_button,
            disconnect_button;

    private TextView
            introduction_message_text,
            UI_status,
            paired_devices_text,
            connected_to_text,
            living_room_text,
            dining_room_text,
            bed_room_text,
            bath_room1_text,
            bath_room2_text,
            onl_text,
            toal_text,
            log_monitor_text;

    private ListView
            show_paired_devices_list,
            log_monitor_list;

    TextView[] myTextViews;
    Button[] myButtons;
    ImageView[] myImageViews;
    String[] myTexts;

    private String commandRead;

    private String string1, string2, string3, string4, string5, string6, string7, string8, string9, string10, string11;

    private boolean hasStarted;
    private boolean isConnected;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cloud_alapa_app);

        hasStarted = false;
        isConnected = false;

        // text views
        introduction_message_text = findViewById(R.id.introduction_message);
        UI_status = findViewById(R.id.UI_status);
        paired_devices_text = findViewById(R.id.paired_devices_text);
        connected_to_text = findViewById(R.id.connected_device_text);
        living_room_text = findViewById(R.id.living_room_text);
        dining_room_text = findViewById(R.id.dining_room);
        bed_room_text = findViewById(R.id.bed_room_text);
        bath_room1_text = findViewById(R.id.bath_room1_text);
        bath_room2_text = findViewById(R.id.bath_room2_text);
        onl_text = findViewById(R.id.outdoor_night_lights_text);
        toal_text = findViewById(R.id.turn_off_all_lights_text);
        log_monitor_text = findViewById(R.id.log_monitor_text);

        // buttons
        living_room_button = findViewById(R.id.living_room_button);
        dining_room_button = findViewById(R.id.dining_room_button);
        bed_room_button = findViewById(R.id.bed_room_button);
        bath_room1_button = findViewById(R.id.bath_room1_button);
        bath_room2_button = findViewById(R.id.bath_room2_button);
        onl_button = findViewById(R.id.outdoor_night_lights_button);
        toal_button = findViewById(R.id.turn_off_all_lights_button);
        show_paired_devices_button = findViewById(R.id.show_paired_devices_button);

        // image views buttons
        toggle_rfcomm_button = findViewById(R.id.rfcomm_button);
        toggle_butler_button = findViewById(R.id.butler_button);
        developer_notes_button = findViewById(R.id.developer_notes_button);
        disconnect_button = findViewById(R.id.disconnect_button);

        // list views
        show_paired_devices_list = findViewById(R.id.paired_devices_listview);
        log_monitor_list = findViewById(R.id.log_monitor);

        myArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        show_paired_devices_list.setAdapter(myArrayAdapter);

        myLogMessages = new ArrayList<>();
        myLogMonitorArray = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, myLogMessages);
        log_monitor_list.setAdapter(myLogMonitorArray);

        myTextViews = new TextView[]{
                introduction_message_text,
                paired_devices_text,
                connected_to_text,
                living_room_text,
                dining_room_text,
                bed_room_text,
                bath_room1_text,
                bath_room2_text,
                onl_text,
                toal_text,
                log_monitor_text
        };

        myButtons = new Button[]{
                living_room_button,
                dining_room_button,
                bed_room_button,
                bath_room1_button,
                bath_room2_button,
                onl_button,
                toal_button
        };

        myImageViews = new ImageView[]{
                toggle_rfcomm_button,
                toggle_butler_button,
                developer_notes_button,
                disconnect_button
        };

        string1 = getResources().getString(R.string.introduction_message);
        string2 = getResources().getString(R.string.paired_devices);
        string3 = getResources().getString(R.string.not_connected);
        string4 = getResources().getString(R.string.living_room);
        string5 = getResources().getString(R.string.dining_room);
        string6 = getResources().getString(R.string.bed_room);
        string7 = getResources().getString(R.string.bath_room1);
        string8 = getResources().getString(R.string.bath_room2);
        string9 = getResources().getString(R.string.outdoor_night_lights);
        string10 = getResources().getString(R.string.all_lights);
        string11 = getResources().getString(R.string.log_monitor);

        myTexts = new String[]{
                string1,
                string2,
                string3,
                string4,
                string5,
                string6,
                string7,
                string8,
                string9,
                string10,
                string11
        };

        myCloudService1 = new CloudService1(
                this,
                cloudHandler,
                myLogMessages,
                myLogMonitorArray,
                myArrayAdapter);

        myCloudService2 = new CloudService2(
                cloudHandler,
                myButtons);

        for (int i = 0; i < 7; i++) {
            int finalI = i;
            myButtons[i].setOnClickListener(v -> {
                myCloudService2.toggleButton(myButtons[finalI]);
            });
        }

        initial_commands = new ArrayList<>();

        show_paired_devices_button.setText(R.string.show_paired_devices_button);

        show_paired_devices_button.setOnClickListener(v -> {
            myCloudService1.togglePairedDevicesList();
        });

        show_paired_devices_list.setOnItemClickListener((parent, view, position, id) -> myCloudService1.adapterOnClick(view));

        for (int i = 0; i < 4; i++) {
            int finalI = i;
            myImageViews[i].setOnClickListener(v -> {
                switch (finalI) {
                    case 0:
                        myCloudService1.toggleRfcomm();
                        break;
                    case 1:
                        myCloudService1.toggleButler();
                        break;
                    case 2:
                        openDeveloperNotes();
                        break;
                    case 3:
                        myCloudService1.disconnect();
                        break;
                }
            });
        }

        setUpMyTexts();
        myCloudService1.updateButler();
        updateButtons();

    }

    private final Handler cloudHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_UI_UPDATE:
                    if (msg.arg1 == CloudService1.RFCOMM_UI_UPDATE) {
                        UI_status.setText(msg.obj.toString());
                    } else if (msg.arg1 == CloudService1.BUTLER_UI_UPDATE) {
                        UI_status.setText(msg.obj.toString());
                        toasty_msg(msg.obj.toString());
                    }
                    break;
                case Constants.MESSAGE_CONNECTION:
                    if (msg.arg1 == CloudService1.DISCONNECT) {
                        String command = "0";
                        myCloudService1.write(command);
                        isConnected = false;
                        connected_to_text.setText(string3);
                        updateButtons();
                        myCloudService1.closeCloudSocket();
                    } else if (msg.arg1 == CloudService1.CONNECTED) {
                        isConnected = true;
                        toasty_msg(msg.obj.toString());
                        connected_to_text.setText(msg.obj.toString());
                        updateButtons();
                    }
                    break;
                case Constants.MESSAGE_DISCONNECT_DEVICE:
                    if (CloudService1.getReceivedDisconnectConfirmation()) {
                        isConnected = false;
                        connected_to_text.setText(string3);
                        updateButtons();
                        CloudService1.setReceivedDisconnectConfirmation(false);
                        myCloudService1.updateLogMonitor(msg.obj.toString());
                        myCloudService1.closeCloudSocket();
                        myCloudService1.updateButler();
                    }
                    break;
                case Constants.MESSAGE_READ:
                    commandRead = msg.obj.toString();
                    myCloudService1.updateLogMonitor(commandRead);
                    break;
                case Constants.MESSAGE_WRITE:
                    String commandWrite = msg.obj.toString();
                    myCloudService1.write(commandWrite);
                    break;
                case Constants.MESSAGE_TOASTY:
                    String bread = msg.obj.toString();
                    toasty_msg(bread);
                    break;
            }
        }
    };

    public void setUpMyTexts() {
        for (int i = 0; i < 11; i++) {
            myTextViews[i].setText(myTexts[i]);
        }
    }

    public void updateButtons() {
        if (isConnected) {
            toggle_rfcomm_button.setClickable(false);
            toggle_butler_button.setClickable(false);
            show_paired_devices_button.setEnabled(false);
            for (int i = 0; i < 7; i++) {
                if (i == 6) {
                    myButtons[i].setText(R.string.isOff);
                } else {
                    myButtons[i].setText(R.string.toggle);
                }
                myButtons[i].setEnabled(true);
            }
            myImageViews[3].setClickable(true);
        } else {
            toggle_rfcomm_button.setClickable(true);
            toggle_butler_button.setClickable(true);
            show_paired_devices_button.setEnabled(true);
            for (int i = 0; i < 7; i++) {
                if (i == 6) {
                    myButtons[i].setText(R.string.isOff);
                } else {
                    myButtons[i].setText(R.string.toggle);
                }
                myButtons[i].setEnabled(false);
            }
            myImageViews[3].setClickable(false);
        }
    }

    public void openDeveloperNotes() {
        DeveloperNotes bonbonNotes = new DeveloperNotes();
        bonbonNotes.show(getSupportFragmentManager(), "developer_notes");
    }

    public void toasty_msg(String bread) {
        Toast.makeText(this, bread, Toast.LENGTH_SHORT).show();
    }

}