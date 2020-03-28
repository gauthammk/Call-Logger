package com.example.callpredictor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog.Calls;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.hashids.Hashids;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.provider.CallLog.Calls.CACHED_NAME;
import static android.provider.CallLog.Calls.DATE;
import static android.provider.CallLog.Calls.DURATION;
import static android.provider.CallLog.Calls.INCOMING_TYPE;
import static android.provider.CallLog.Calls.MISSED_TYPE;
import static android.provider.CallLog.Calls.NUMBER;
import static android.provider.CallLog.Calls.OUTGOING_TYPE;
import static android.provider.CallLog.Calls.TYPE;


public class MainActivity extends AppCompatActivity {
    String[] SMSRecords;
    String[] callRecords;
    Hashids hashids;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hashids = new Hashids("Samsung Thingy");
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_SMS)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_SMS)){
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_SMS},1);

            }
            else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_SMS },1);
            }
        }
        else
        {
            System.out.println("Fetching SMS records.");
            SMSRecords = getSMSData();
        }

        if((ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_CALL_LOG)!= PackageManager.PERMISSION_GRANTED)){
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_CALL_LOG)){
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_CALL_LOG },1);

            }
            else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_CALL_LOG },1);
            }
        }
        else
        {
            System.out.println("Fetching call records.");
            callRecords = getCallDetails();
        }
        // move to the next screen and pass the call and SMS record arrays
        Button start = findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TagMeCalls.class);
                intent.putExtra("callRecords", callRecords);
                intent.putExtra("SMSRecords", SMSRecords);
                startActivity(intent);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String[] getCallDetails() {
        String[] callRecordArray = new String[10];
        int k = 0;

        String[] projection = new String[]{
                CACHED_NAME,
                NUMBER,
                TYPE,
                DATE,
                DURATION
        };

        try (@SuppressLint("MissingPermission") Cursor managedCursor = getApplicationContext().getContentResolver().query(Calls.CONTENT_URI, projection, null, null, null)) {
            if (managedCursor != null) {
                while (managedCursor.moveToNext() && k < 10) {
                    String name = managedCursor.getString(0);
                    String number = managedCursor.getString(1);
                    String type = managedCursor.getString(2);
                    String date = managedCursor.getString(3);
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH);
                    String dateString = formatter.format(new Date(Long.parseLong(date)));
                    String duration = managedCursor.getString(4);
                    String dir = null;
                    int dircode = Integer.parseInt(type);
                    switch (dircode) {
                        case OUTGOING_TYPE:
                            dir = "OUTGOING";
                            break;

                        case INCOMING_TYPE:
                            dir = "INCOMING";
                            break;

                        case MISSED_TYPE:
                            dir = "MISSED";
                            break;
                    }
                    // append to array if element does not exist already.
                    if (name != null) {
                        //hash phone number
                        if (number.length() > 10) {
                            number = number.substring(3);
                        }
                        long numberInt = Long.parseLong(number);
                        String hashedNumber = hashids.encode(numberInt);

                        callRecordArray[k++] = "\n" + name + "," + hashedNumber + "," + dir + "," + dateString + "," + duration;
                    }
                }
            }
            System.out.println("\n----------------CALL RECORD ARRAY-------------");
            System.out.println("Length of Call Record Array : " + callRecordArray.length);
            for (String callRecord : callRecordArray) {
                System.out.println(callRecord);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return callRecordArray;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String[] getSMSData(){
        String[] SMSRecordArray = new String[10];
        int k = 0;
        String[] projection = new String[] { "_id", "address", "person", "body", "date", "type" };
        String INBOX = "content://sms/inbox";
        // String SENT = "content://sms/sent";
        // String DRAFT = "content://sms/draft";

        try (Cursor managedCursor = getContentResolver().query(Uri.parse(INBOX), projection, null, null, null)) {
            if (managedCursor != null) {
                while (managedCursor.moveToNext() && k < 10) {
                    String id = managedCursor.getString(0);
                    String number = managedCursor.getString(1);
                    String person = managedCursor.getString(2);
                    String body = managedCursor.getString(3);
                    String date = managedCursor.getString(4);
                    String type = managedCursor.getString(5);
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH);
                    String dateString = formatter.format(new Date(Long.parseLong(date)));
                    //removing the commas from the SMS body so that it does not interfere with the csv file format
                    body = body.replace(',','.');
                    body = body.replace('\n',' ');
                    if (person != null) {
                        //hash phone number
                        if (number.length() > 10) {
                            number = number.substring(3);
                        }
                        long numberInt = Long.parseLong(number);
                        String hashedNumber = hashids.encode(numberInt);
                        SMSRecordArray[k++] = "\n" +  id + "," + hashedNumber + "," + person + "," + body + "," + dateString + "," + type;
                    }
                }//gives number of records
            }
            //add to the view
            System.out.println("----------------SMS RECORD ARRAY-------------");
            System.out.println("Length of Call SMS Array : " + SMSRecordArray.length);
            for (String SMSRecord : SMSRecordArray) {
                System.out.println(SMSRecord);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return SMSRecordArray;
    }
}

