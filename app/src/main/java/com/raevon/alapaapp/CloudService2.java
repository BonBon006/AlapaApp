package com.raevon.alapaapp;

import android.os.Handler;
import android.widget.Button;

public class CloudService2 {
    private final Handler cloudHandler;
    private final Button[] cloudButtons;
    private final String[] toggle = {
            "1",   // living room
            "2",   // dining room
            "3",   // bed room
            "4",   // bath room 1
            "5",   // bath room 2
            "6",   // onl
            "7"    // toal
    };

    public CloudService2(
            Handler handler,
            Button[] buttons
    ) {
        cloudHandler = handler;
        cloudButtons = buttons;
    }

    public void toggleButton(Button button) {
        int i = 0;
        while (button != cloudButtons[i]) {
            i++;
        }
        cloudHandler.sendMessage(cloudHandler.obtainMessage
                (Constants.MESSAGE_WRITE, toggle[i]));
    }


}
