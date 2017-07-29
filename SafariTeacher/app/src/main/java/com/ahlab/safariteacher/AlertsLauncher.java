package com.ahlab.safariteacher;

import android.os.Bundle;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Katherine on 28/7/17.
 */

public class AlertsLauncher extends ActivityLauncher {

    ArrayList<Alert> alertsList;
    ListView listViewAlerts;
    DatabaseReference dbAlerts;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_alerts);

        listViewAlerts = findViewById(R.id.list);
        alertsList = new ArrayList<>();

        dbAlerts = FirebaseDatabase.getInstance().getReference("alerts");
    }

    @Override
    protected void onStart() {
        
        super.onStart();

        dbAlerts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                alertsList.clear();

                for(DataSnapshot imageSnapshot: dataSnapshot.getChildren()){
                    String dateTime = imageSnapshot.child("dateTime").getValue().toString();
                    String studentName = imageSnapshot.child("studentName").getValue().toString();
                    String alertType = imageSnapshot.child("alertType").getValue().toString();
                    String durationElapsed = imageSnapshot.child("durationElapsed").getValue().toString();
                    Alert alert = new Alert(dateTime, studentName, alertType, durationElapsed);
                    alertsList.add(alert);
                }
                AlertsListAdapter adapter = new AlertsListAdapter(AlertsLauncher.this, alertsList);
                listViewAlerts.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
