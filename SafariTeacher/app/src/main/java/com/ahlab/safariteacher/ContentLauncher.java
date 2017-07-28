package com.ahlab.safariteacher;

import android.os.Bundle;
import android.widget.ListView;

/**
 * Created by Katherine on 28/7/17.
 */

public class ContentLauncher extends ActivityLauncher {

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_content);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
