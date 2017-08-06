package com.ahlab.safariteacher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ActivityLauncher extends Activity {

    Button buttonScannedLogs;
    Button buttonHelpAlerts;
    Button buttonQRcontent;
    Button buttonTransMessage;
    Button buttonSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_teacher);

        buttonScannedLogs = findViewById(R.id.buttonScannedLogs);
        buttonScannedLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityLauncher.this, LogsLauncher.class);
                startActivity(intent);
            }
        });

        buttonHelpAlerts = findViewById(R.id.buttonHelpAlerts);
        buttonHelpAlerts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityLauncher.this, AlertsLauncher.class);
                startActivity(intent);
            }
        });

        buttonQRcontent = findViewById(R.id.buttonQRcontent);
        buttonQRcontent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityLauncher.this, MappingLauncher.class);
                startActivity(intent);
            }
        });

        buttonTransMessage = findViewById(R.id.buttonTransMessage);
        buttonTransMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityLauncher.this, MessagesLauncher.class);
                startActivity(intent);
            }
        });

        buttonSettings = findViewById(R.id.buttonSettings);
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityLauncher.this, SettingsLauncher.class);
                startActivity(intent);
            }
        });
    }
}
