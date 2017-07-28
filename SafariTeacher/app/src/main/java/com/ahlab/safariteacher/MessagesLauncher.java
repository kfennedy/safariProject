package com.ahlab.safariteacher;

import android.os.Bundle;

/**
 * Created by Katherine on 28/7/17.
 */

public class MessagesLauncher extends ActivityLauncher {

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_message);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
