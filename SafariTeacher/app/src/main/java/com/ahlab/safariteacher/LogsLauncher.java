package com.ahlab.safariteacher;

import android.os.Bundle;
import android.provider.ContactsContract;
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

public class LogsLauncher extends ActivityLauncher {

    ArrayList<Log> logsList;
    ListView listViewLogs;
    DatabaseReference dbLogs;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_logs);

        listViewLogs = findViewById(R.id.list);
        logsList = new ArrayList<>();

        dbLogs = FirebaseDatabase.getInstance().getReference("logs");
    }

    @Override
    protected void onStart() {
        super.onStart();

        dbLogs.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                logsList.clear();

                for(DataSnapshot imageSnapshot: dataSnapshot.getChildren()){
                    String dateTime = imageSnapshot.child("dateTime").getValue().toString();
                    String studentName = imageSnapshot.child("studentName").getValue().toString();
                    String qrName = imageSnapshot.child("qrName").getValue().toString();
                    String qrContent = imageSnapshot.child("qrContent").getValue().toString();
                    Log Log = new Log(dateTime, studentName, qrName, qrContent);
                    logsList.add(Log);
                }
                LogsListAdapter adapter = new LogsListAdapter(LogsLauncher.this, logsList);
                listViewLogs.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
