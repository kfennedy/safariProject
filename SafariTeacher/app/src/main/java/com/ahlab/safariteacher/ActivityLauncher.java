package com.ahlab.safariteacher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ActivityLauncher extends Activity {

    Button buttonScannedLogs;
    Button buttonQRcontent;
    Button buttonTransMessage;
    Button buttonHelpAlerts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_teacher);

        buttonScannedLogs = (Button) findViewById(R.id.buttonScannedLogs);
        buttonScannedLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityLauncher.this, LogsLauncher.class);
                startActivity(intent);
            }
        });

        buttonQRcontent = (Button) findViewById(R.id.buttonQRcontent);
        buttonQRcontent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityLauncher.this, ContentLauncher.class);
                startActivity(intent);
            }
        });

        buttonTransMessage = (Button) findViewById(R.id.buttonTransMessage);
        buttonTransMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityLauncher.this, MessagesLauncher.class);
                startActivity(intent);
            }
        });

        buttonHelpAlerts = (Button) findViewById(R.id.buttonHelpAlerts);
        buttonHelpAlerts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityLauncher.this, AlertsLauncher.class);
                startActivity(intent);
            }
        });
    }
}
