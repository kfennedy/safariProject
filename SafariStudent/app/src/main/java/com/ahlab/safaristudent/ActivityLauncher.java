package com.ahlab.safaristudent;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import android.speech.tts.TextToSpeech.OnInitListener;


public class ActivityLauncher extends Activity implements OnInitListener, ZXingScannerView.ResultHandler{

    ZXingScannerView zXingScannerView;
    ZXingScannerView.ResultHandler resultHandler;
    DatabaseReference dbMessages;
    DatabaseReference dbMappings;
    DatabaseReference dbLogs;
    ArrayList<Message> messagesList;
    TextToSpeech tts;
    String dateTime;
    String studentName = "unknown";
    String qrName;
    String qrContent;
    String message0;
    String message1;
    Integer maxCount = 0;
    Integer delayTime = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_student);

        dbLogs = FirebaseDatabase.getInstance().getReference("logs");
        tts = new TextToSpeech(getApplicationContext(), this);
        messagesList = new ArrayList<>();
        pullMessagesFromCloud();
    }


    public void scan(){
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
        Log log = new Log(dateTime, studentName, qrName, qrContent);
        dbLogs.child(dateTime).setValue(log);
        Toast.makeText(getApplicationContext(), qrName, Toast.LENGTH_SHORT).show();
        maxCount += 1;

        if (maxCount <= messagesList.size()-2){
//            System.out.println("============maxCount=============== " + maxCount);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    String aha = messagesList.get(maxCount+1).getMessageContent();
                    tts.speak(aha, TextToSpeech.QUEUE_ADD, null);
//                    System.out.println("============message spoken=============== " + aha);
                }
            }, delayTime);
            processInteraction();
        } else {
//            System.out.println("============maxCount=============== " + maxCount);
            processInteraction();
        }

    }

    public void pullMessagesFromCloud(){
        messagesList.clear();
        dbMessages = FirebaseDatabase.getInstance().getReference("messages");
        dbMessages.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() != null){
                    dbMessages.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot data: dataSnapshot.getChildren()){
                                Message message = new Message(data.getKey(), data.getValue().toString());
                                messagesList.add(message);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
                }
                return Transaction.success(mutableData);
            }
            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                Toast.makeText(getApplicationContext(), "completes syncing", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void processInteraction(){
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

                if (qrName.equals("QR009") || qrName.equals("QR010")){
                    studentName = qrContent;
                    processMessage2();
                } else {
                    tts.speak(qrContent, TextToSpeech.QUEUE_ADD, null);

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            zXingScannerView.resumeCameraPreview(resultHandler);
                        }
                    }, delayTime);

                }
            }
        });
    }

    public void processMessage1(View view1){
        message0 = messagesList.get(0).getMessageContent();
        tts.speak(message0, TextToSpeech.QUEUE_FLUSH, null);
        scan();
    }

    // if QR009 - QR0010 is scanned
    public void processMessage2(){
        maxCount = 1;
        message1 = messagesList.get(1).getMessageContent();

        //TODO: need to customise the studentName from teacher's app
        String aha = "Hi " + studentName + ". " + message1;
        tts.speak(aha, TextToSpeech.QUEUE_FLUSH, null);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                zXingScannerView.resumeCameraPreview(resultHandler);
            }
        }, delayTime);
    }

    public String getDateTimeNow(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH);
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
