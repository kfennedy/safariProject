package com.ahlab.safariteacher;

import android.os.Bundle;

/**
 * Created by Katherine on 28/7/17.
 */

public class SettingsLauncher extends ActivityLauncher {

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_settings);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
