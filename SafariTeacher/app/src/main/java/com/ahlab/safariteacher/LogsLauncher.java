package com.ahlab.safariteacher;

import android.os.Bundle;
import android.widget.ListView;

/**
 * Created by Katherine on 28/7/17.
 */

public class LogsLauncher extends ActivityLauncher {

    ListView listViewLogs;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_logs);

        listViewLogs = findViewById(R.id.list);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
