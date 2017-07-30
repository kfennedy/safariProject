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

public class MappingLauncher extends ActivityLauncher{

    DatabaseReference dbMappings;
    ArrayList<Mapping> mappingsList;
    ListView listViewMappings;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_mappings);

        listViewMappings = findViewById(R.id.list);
        mappingsList = new ArrayList<>();
        dbMappings = FirebaseDatabase.getInstance().getReference("mappings");
    }

    @Override
    protected void onStart() {
        super.onStart();

        dbMappings.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mappingsList.clear();

                for(DataSnapshot imageSnapshot: dataSnapshot.getChildren()){
                    String qrName = imageSnapshot.getKey();
                    String qrContent = imageSnapshot.getValue().toString();
                    Mapping mapping = new Mapping(qrName, qrContent);
                    mappingsList.add(mapping);
                }
                MappingsListAdapter adapter = new MappingsListAdapter(MappingLauncher.this, mappingsList);
                listViewMappings.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
