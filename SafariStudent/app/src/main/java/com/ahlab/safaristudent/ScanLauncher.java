package com.ahlab.safaristudent;

import android.os.Bundle;
import android.widget.ListView;

/**
 * Created by Katherine on 28/7/17.
 */

public class ScanLauncher extends ActivityLauncher {

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan);
    }
}
