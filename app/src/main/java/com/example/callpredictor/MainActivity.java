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
    String[] freqContacts;
    Hashids hashids;
    HashMap<String, String> nameHash = new HashMap<String, String>();
    HashMap<String, Long> durationHash = new HashMap<String, Long>();
    HashMap<String, Integer> idHash = new HashMap<String, Integer>();
    int maxId = 1;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hashids = new Hashids("Samsung Thingy");
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_CALL_LOG)!= PackageManager.PERMISSION_GRANTED&&ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_SMS)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_CALL_LOG)&&ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_SMS)){
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_CALL_LOG,Manifest.permission.READ_SMS},1);

            }
            else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_CALL_LOG,Manifest.permission.READ_SMS },1);
            }
        }
        else
        {
            callRecords=getCallDetails();
            SMSRecords=getSMSData();
        }
        // move to the next screen and pass the call and SMS record arrays
        Button start = findViewById(R.id.start);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("----------------------------CSV NAME --------------------");
                Intent intent = new Intent(MainActivity.this, NewTagMeCalls.class);
                intent.putExtra("callRecords", callRecords);
                intent.putExtra("SMSRecords", SMSRecords);
                intent.putExtra("freqContacts", freqContacts);
                intent.putExtra("nameHash", nameHash);
                intent.putExtra("idHash", idHash);
                intent.putExtra("maxId", maxId);
                //intent.putExtra("csvName", csvName);
                startActivity(intent);
            }
        });

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode){
            case 1:{
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED&&grantResults[1]==PackageManager.PERMISSION_GRANTED){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        callRecords=getCallDetails();
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        SMSRecords=getSMSData();
                    }
                }
            }
            default:super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String[] getCallDetails() {
        DynamicArray callRecordArray = new DynamicArray();
        int k = 0;

        DynamicArray callrec = new DynamicArray();

        DynamicArray finalCallArray = new DynamicArray();
        String[] projection = new String[]{
                CACHED_NAME,
                NUMBER,
                TYPE,
                DATE,
                DURATION
        };

        try (@SuppressLint("MissingPermission") Cursor managedCursor = getApplicationContext().getContentResolver().query(Calls.CONTENT_URI, projection, null, null, null)) {
            if (managedCursor != null) {
                System.out.println("..........................NAMES.........................");
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
                    if (name != null && name != "null") {
                        //hash phone number
                        if (number.length() > 10) {
                            if(number.length() == 11)
                                number = number.substring(1);
                            else
                                number = number.substring(3);
                        }
                        long numberInt = Long.parseLong(number);
                        String hashedNumber = hashids.encode(numberInt);
                        //System.out.println(name + "   " + number);
                        if(hashedNumber != null){
                            callrec.add(hashedNumber);
                            if(durationHash.containsKey(hashedNumber) == false){
                                durationHash.put(hashedNumber, Long.parseLong(duration));
                            }
                            else{
                                long dur = durationHash.get(hashedNumber) + Long.parseLong(duration);
                                durationHash.replace(hashedNumber, dur);
                            }
                            if(nameHash.containsKey(hashedNumber) == false){
                                nameHash.put(hashedNumber, name);
                                //System.out.println("Inside :   " + name + "   " + number);
                            }
                            if(idHash.containsKey(hashedNumber) == false){
                                idHash.put(hashedNumber, maxId);
                                maxId += 1;
                            }
                        }
                        //callRecordArray.add("\n" + name + "," + hashedNumber + "," + dir + "," + dateString + "," + duration);
                        callRecordArray.add(hashedNumber + "," + dir + "," + dateString + "," + duration + "\n");
                        System.out.println(callRecordArray.array[callRecordArray.array.length-1]);
                    }
                }
            }
            callrec.shrinkSize();
            freqContacts = countFreq(callrec.array,callrec.array.length);
            for (String callRecord : callRecordArray.array) {
                //System.out.print(callRecord);
                if(callRecord != null && callRecord != "null"){
                    //int index = callRecord.indexOf(",")+1;
                    int index = 0;
                    String hashNum = callRecord.substring(index, index+8);
                    if(Arrays.asList(freqContacts).contains(hashNum)){
                        finalCallArray.add(callRecord);
                    }
                }
            }
            finalCallArray.shrinkSize();
            System.out.println("\n----------------CALL RECORD ARRAY-------------");
            System.out.println("Length of Call Record Array : " + finalCallArray.array.length);
            //int num = 1;
            for (String callRecord : finalCallArray.array) {
                System.out.println(callRecord);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return finalCallArray.array;
    }
    public static boolean mes_filter(String str) {
        //if(Pattern.matches("[a-z|A-Z]{2}-[a-z|A-Z]*", str))
        //return false;
        if(Pattern.matches("\\d{10}", str.substring(3)) || Pattern.matches("\\d{10}", str))
            return true;
        else
            return false;
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String[] getSMSData(){
        //String[] SMSRecordArray = new String[10];
        DynamicArray SMSRecordArray = new DynamicArray();
        int k = 0;
        String[] projection = new String[] { "_id", "address", "person", "body", "date", "type" };
        String INBOX = "content://sms/inbox";
        // String SENT = "content://sms/sent";
        // String DRAFT = "content://sms/draft";
        System.out.println("----------------SMS RECORD ARRAY-------------");
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
                    //Integer.parseInt(person) != 0 &&
                    //System.out.println("id = " + id + " Number = " + number + " Person = "+ person + " Body = " + body + " Date = " + dateString + " Type = " + type);
                    if (mes_filter(number)) {
                        //hash phone number
                        if (number.length() > 10) {
                            number = number.substring(3);
                        }
                        long numberInt = Long.parseLong(number);
                        String hashedNumber = hashids.encode(numberInt);

                        if(idHash.containsKey(hashedNumber) == false){
                            idHash.put(hashedNumber, maxId);
                            maxId += 1;
                        }

                        SMSRecordArray.add(hashedNumber + "," + body + "," + dateString + "," + type + "\n" );
                        System.out.println(" Hashed Number = " + hashedNumber + " Number = " + number + " Body = " + body + " Date = " + dateString + " Type = " + type);
                    }
                }//gives number of records
            }
            //add to the view
            SMSRecordArray.shrinkSize();

            System.out.println("----------------SMS RECORD ARRAY-------------");
            System.out.println("Length of Call SMS Array : " + SMSRecordArray.array.length);
            for (String SMSRecord : SMSRecordArray.array) {
                System.out.println(SMSRecord);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return SMSRecordArray.array;
    }
    public static class DynamicArray {

        public String array[];
        public int count;
        public int size;

        public DynamicArray() {
            array = new String[1];
            count = 0;
            size = 1;
        }

        public void add(String data) {
            if (count == size) {
                growSize();
            }
            array[count] = data;
            count++;
        }

        public void growSize() {

            String temp[] = null;
            if (count == size) {
                temp = new String[size * 2];
                {
                    for (int i = 0; i < size; i++) {
                        temp[i] = array[i];
                    }
                }
            }
            array = temp;
            size = size * 2;
        }
        public void shrinkSize() {
            String temp[] = null;
            if (count > 0) {
                temp = new String[count];
                for (int i = 0; i < count; i++) {
                    temp[i] = array[i];
                }
                size = count;
                array = temp;
            }
        }
    }

    public  String[] countFreq(String arr[], int n)
    {
        Map<String, Integer> mp = new HashMap<>();
        //String[] top40 = new String[40];
        DynamicArray top40 = new DynamicArray();
        int top = -1;
        int num, j;

        for (int i = 0; i < n; i++)
        {
            if (mp.containsKey(arr[i]))
            {
                mp.put(arr[i], mp.get(arr[i]) + 1);
            }
            else
            {
                mp.put(arr[i], 1);
            }
        }
        int si = mp.size();
        DynamicArray stack40 = new DynamicArray();
        DynamicArray tempsort = new DynamicArray();

        List<Map.Entry<String, Integer> > list =
                new LinkedList<Map.Entry<String, Integer> >(mp.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2)
            {
                return (o2.getValue()).compareTo(o1.getValue());
        }
        });

        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
            //System.out.println(aa.getKey()+ "   " + aa.getValue());
        }
        for (Map.Entry<String, Integer> entry : temp.entrySet())
        {
            stack40.add(entry.getKey());
        }
        stack40.shrinkSize();
        j = 0;
        int len = stack40.array.length;
        System.out.println("HashNumber   Freq   Duration");
        while(j < len){
            Map<String, Long> tm = new HashMap<>();
            int c = temp.get(stack40.array[j]);
            tm.put(stack40.array[j], durationHash.get(stack40.array[j]));
            j++;
            while(j < len && temp.get(stack40.array[j]) == c){
                tm.put(stack40.array[j], durationHash.get(stack40.array[j]));
                j++;
            }
            List<Map.Entry<String, Long> > templist =
                    new LinkedList<Map.Entry<String, Long> >(tm.entrySet());

            Collections.sort(templist, new Comparator<Map.Entry<String, Long> >() {
                public int compare(Map.Entry<String, Long> o1,
                                   Map.Entry<String, Long> o2)
                {
                    return (o2.getValue()).compareTo(o1.getValue());
                }
            });
            for (Map.Entry<String, Long> aa : templist) {
                String k = aa.getKey();
                tempsort.add(k);
                System.out.println(k + "       " + temp.get(k)+ "   " +aa.getValue());
            }
        }
        tempsort.shrinkSize();
        num = tempsort.array.length <= 25 ? tempsort.array.length : 25;
        for(int i = 0; i < num; i++)
            top40.add(tempsort.array[i]);
        top40.shrinkSize();
        return top40.array;
    }
}

