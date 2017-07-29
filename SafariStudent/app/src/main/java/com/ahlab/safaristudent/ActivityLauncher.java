package com.ahlab.safaristudent;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import android.speech.tts.TextToSpeech.OnInitListener;


public class ActivityLauncher extends Activity implements OnInitListener, ZXingScannerView.ResultHandler{

    private ZXingScannerView zXingScannerView;
    private DatabaseReference dbMappings;
    private DatabaseReference dbLogs;
    private ZXingScannerView.ResultHandler resultHandler;
    public TextToSpeech tts;
    String dateTime;
    String qrName;
    String qrContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_student);

        dbLogs = FirebaseDatabase.getInstance().getReference("logs");
        tts = new TextToSpeech(getApplicationContext(), this);
    }

    public void scan(View view){
        zXingScannerView = new ZXingScannerView(getApplicationContext());
        setContentView(zXingScannerView);
        zXingScannerView.setResultHandler(this);
        zXingScannerView.startCamera();
        resultHandler = this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        zXingScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result result) {

        dateTime = getDateTimeNow();
        qrName = result.getText();
        Toast.makeText(getApplicationContext(), qrName, Toast.LENGTH_SHORT).show();

        dbMappings = FirebaseDatabase.getInstance().getReference("mappings");
        dbMappings.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() != null){
                    dbMappings.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            System.out.println(dataSnapshot.child(qrName));
                            qrContent = dataSnapshot.child(qrName).getValue().toString();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                Log log = new Log(dateTime, "katherine", qrName, qrContent);
                dbLogs.child(dateTime).setValue(log);

                if (qrContent != null){
                    String audioContent = "Katherine's content is " + qrContent;
                    tts.speak(audioContent, TextToSpeech.QUEUE_FLUSH, null);
                }
                zXingScannerView.resumeCameraPreview(resultHandler);
            }
        });
    }

    public String getDateTimeNow(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        String dateTime = dateFormat.format(cal.getTime()).replace('/','|');
        return dateTime;
    }

    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
        }
    }
}
