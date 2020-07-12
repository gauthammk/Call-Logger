package com.example.callpredictor;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

public class NewTagMeCalls extends AppCompatActivity {
    String[] callRecords, SMSRecords, freqContacts;
    String[] relationshipMap = {"Tag Here", "Friend", "Father", "Mother", "Spouse", "Child", "Sibling", "Colleague", "Other" };
    TextView titleTextView;
    Button nextButton;
    int index;
    int required;
    int arrayPointer = 0;
    String hashNum, UserName, TimeStamp;
    StringBuilder callRecordsCommaSeparated;
    HashMap<String, String> callHash = new HashMap<String, String>();
    HashMap<String, String> nameHash = new HashMap<String, String>();
    HashMap<String, String> smsHash = new HashMap<String, String>();
    HashMap<String, Integer> idHash = new HashMap<String, Integer>();
    int maxId;
    MainActivity.DynamicArray finalSmsArray = new MainActivity.DynamicArray();
    MainActivity.DynamicArray freqSms = new MainActivity.DynamicArray();
    MainActivity.DynamicArray taggedContacts = new MainActivity.DynamicArray();

   Object[] smsSet ;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_tag_me_calls);

        // initialising the fixed components
        titleTextView = findViewById(R.id.title);
        callRecordsCommaSeparated = new StringBuilder();
        LinearLayout mainContent = findViewById(R.id.mainContent);
        nextButton = findViewById(R.id.next);
        final Intent inte = getIntent();

        // getting the passed data from the previous activity
        Bundle extras = getIntent().getExtras();
        // getting the callRecords and SMSRecords array
        if(extras != null) {
            callRecords = extras.getStringArray("callRecords");
            SMSRecords = extras.getStringArray("SMSRecords");
            freqContacts = extras.getStringArray("freqContacts");
            nameHash = (HashMap<String, String>)inte.getSerializableExtra("nameHash");
            idHash = (HashMap<String, Integer>)inte.getSerializableExtra("idHash");
            maxId = extras.getInt("maxId");
            UserName = extras.getString("UserName");
            TimeStamp = extras.getString("TimeStamp");
            //csvName = extras.getString("csvName");
            System.out.println("Length of Call Record Array : " + callRecords.length);
            System.out.println("Length of SMS record Array : " + SMSRecords.length);
            System.out.println("Length of freqContact Array : " + freqContacts.length);
            System.out.println("Is nameHash empty ? "+ nameHash.isEmpty());

            System.out.println("---------------------SMS Records after filtering the common names ----------------");
            for(String SMS : SMSRecords){
                if(SMS != null && SMS != "null"){
                    //index = SMS.indexOf(",")+1;
                    index = 0;
                    hashNum = SMS.substring(index, index+8);
                    if(nameHash.containsKey(hashNum)){
                        finalSmsArray.add(SMS);
                        System.out.println(SMS);
                        if(smsHash.containsKey(hashNum) == false)
                            smsHash.put(hashNum, null);
                    }
                }
            }
            finalSmsArray.shrinkSize();
            System.out.print(finalSmsArray.array.length);
            titleTextView.setText(R.string.Call_title);
        }else{
            titleTextView.setText("No records found!");
        }

        // initialising user data components as arrays
        //final RadioButton[][] rb = new RadioButton[freqContacts.length][2];
        //RadioGroup[] rg = new RadioGroup[freqContacts.length];
        required = freqContacts.length <= 20 ? freqContacts.length : 20;
        final Spinner[] relationshipSpinner = new Spinner[freqContacts.length];
        //final EditText[] age = new EditText[freqContacts.length];
        final CheckBox[] hider = new CheckBox[freqContacts.length];

        // looping through the records and creating corresponding user data components
        for (int i = 0; i < freqContacts.length; i++) {

            // setting the layout to contain all the components in each record's display
            LinearLayout recordLayout = new LinearLayout(this);
            recordLayout.setOrientation(LinearLayout.VERTICAL);
            recordLayout.setGravity(Gravity.CENTER);
            // layout parameter to wrap_content on each of the user data components
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            // setting the hidden property
            hider[i] = new CheckBox(this);
            hider[i].setText("Hide");
            hider[i].setChecked(false);
            // setting the contact name
            TextView tv = new TextView(this);

            tv.setText(nameHash.get(freqContacts[i]));
            tv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
            tv.setTextSize(25);
            tv.setLayoutParams(layoutParams);
            tv.setPadding(20,20,20,20);

            // setting the radio buttons
            /*rg[i] = new RadioGroup(this);
            rg[i].setOrientation(RadioGroup.HORIZONTAL);
            rb[i][0] = new RadioButton(this);
            rb[i][0].setText("Male");
            rb[i][1] = new RadioButton(this);
            rb[i][1].setText("Female");
            //rb[i][1].setChecked(true);

            rg[i].addView(rb[i][0]);
            rg[i].addView(rb[i][1]);
            rg[i].setLayoutParams(layoutParams);
            rg[i].setPadding(20,20,20,20);

            // setting the age option
            age[i] = new EditText(this);
            age[i].setLayoutParams(layoutParams);
            age[i].setHint("AGE");
            age[i].setInputType(InputType.TYPE_CLASS_NUMBER);
            age[i].setFilters(new InputFilter[] { new InputFilter. LengthFilter(2) });
            age[i].setPadding(20,20,20,20);*/

            // setting the relationship spinner
            relationshipSpinner[i] = new Spinner(this);
            //setting the spinner options
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, relationshipMap);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            relationshipSpinner[i].setAdapter(adapter);
            relationshipSpinner[i].setGravity(Gravity.CENTER);
            relationshipSpinner[i].setPadding(20,20,20,20);
//            //listening for spinner item selections
//            relationshipSpinner.setOnItemSelectedListener(this);

            // adding the user data components to the recordLayout
            recordLayout.addView(hider[i]);
            recordLayout.addView(tv);
            //recordLayout.addView(age[i]);
            //recordLayout.addView(rg[i]);
            recordLayout.addView(relationshipSpinner[i]);
            mainContent.addView(recordLayout);

            // adding a nice border around the recordLayout and relationshipSpinner
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                recordLayout.setBackground(getDrawable(R.drawable.border));
                relationshipSpinner[i].setBackground(getDrawable(R.drawable.border));
            }
            // setting margins for breathing space
            setMargins(recordLayout, 10, 10,10,10);
        }

        // listening for next button click
        nextButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {

                // Check if the minimum number of records are tagged
                int taggedContactsnum = 0;
                for (int i = 0; i < freqContacts.length; i++) {
                    if (relationshipSpinner[i].getSelectedItem() != "Tag Here") {
                        taggedContactsnum++;
                        taggedContacts.add(freqContacts[i]);
                    }
                }
                taggedContacts.shrinkSize();
                System.out.println("--------------------------------------------------------Tagged Call Record Names---------------------------------------");
                for(int i=0; i < taggedContacts.array.length; i++){
                    System.out.println(nameHash.get(taggedContacts.array[i]));
                    callHash.put(taggedContacts.array[i], null);
                }
                if (taggedContactsnum >= required) {
                    // Adding the csv headers
                    callRecordsCommaSeparated.append("Id, Name, Relationship, CallType, Date_Time, Duration\n");
                    // creating the string to convert to csv
                    for (int i = 0; i < taggedContacts.array.length; i++) {
                        boolean shouldBeHidden = hider[i].isChecked();
                        String s = "";
                        if (shouldBeHidden == false)
                            s += nameHash.get(taggedContacts.array[i]) + ", " + relationshipSpinner[i].getSelectedItem();
                        else
                            s += "#####" + ", " + relationshipSpinner[i].getSelectedItem();
                    /*if (rb[i][0].isChecked()) {
                        s += ", Male, " + relationshipSpinner[i].getSelectedItem() + ", " + age[i].getText();
                    } else if (rb[i][1].isChecked()){
                        s += ",Female, " + relationshipSpinner[i].getSelectedItem() + ", " + age[i].getText();
                    }
                    else{
                        s += ", , " + relationshipSpinner[i].getSelectedItem() + ", " + age[i].getText();
                    }*/
                        callHash.replace(taggedContacts.array[i], s);
                    }
                    while (arrayPointer < callRecords.length) {
                        //System.out.println("Array Pointer = "+arrayPointer);
                        //index = callRecords[arrayPointer].indexOf(",")+1;
                        index = 0;
                        hashNum = callRecords[arrayPointer].substring(index, index + 8);
                        if(callHash.containsKey(hashNum))
                            callRecordsCommaSeparated.append(idHash.get(hashNum)).append(", ").append(callHash.get(hashNum)).append(", ").append(callRecords[arrayPointer].substring(9));
                        arrayPointer++;
                    }
                    for (int i = 0; i < taggedContacts.array.length; i++) {
                        if (smsHash.containsKey(taggedContacts.array[i])) {
                            smsHash.replace(taggedContacts.array[i], callHash.get(taggedContacts.array[i]));
                        }
                    }
                    smsSet = smsHash.keySet().toArray();
                    for (int i = 0; i < smsSet.length; i++) {
                        String s = smsSet[i].toString();
                        if (smsHash.get(s) == null)
                            freqSms.add(s);
                    }
                    freqSms.shrinkSize();
                    // Displaying the Enumeration

                    // printing out the csv string to the console
                    System.out.println("-------------------------------------------Call Data CSV -----------------------------------------------------------\n" + callRecordsCommaSeparated.toString());
                    // moving to the SMS screen
                    Intent nintent = new Intent(NewTagMeCalls.this, NewTagMeSMS.class);
                    nintent.putExtra("SMSRecords", finalSmsArray.array);
                    nintent.putExtra("freqSms", freqSms.array);
                    nintent.putExtra("nameHash", nameHash);
                    nintent.putExtra("smsHash", smsHash);
                    nintent.putExtra("idHash", idHash);
                    nintent.putExtra("maxId", maxId);
                    nintent.putExtra("call_data", callRecordsCommaSeparated.toString());
                    nintent.putExtra("UserName", UserName);
                    nintent.putExtra("TimeStamp", TimeStamp);
                    startActivity(nintent);
                    /*
                    // writing tagged call records to the csv file
                    String csv = "Call_data.csv";
                    try (FileOutputStream out = openFileOutput(csv, Context.MODE_PRIVATE)) {
                        //saving the file into device
                        out.write((callRecordsCommaSeparated.toString()).getBytes());
                        //exporting the saved csv file
                        Context context = getApplicationContext();
                        File filelocation = new File(getFilesDir(), csv);
                        Uri path = FileProvider.getUriForFile(context, "com.example.dataapp.fileprovider", filelocation);
                        String email = "bms.datacollection@gmail.com";
                        String subject = "Call Data";
                        String message = "CALL Data Records";
                        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.setType("text/csv");
                        emailIntent.setType("vnd.android.cursor.dir/email");
                        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{email});
                        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
                        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
                        emailIntent.setPackage("com.google.android.gm");
                        emailIntent.putExtra(Intent.EXTRA_STREAM, path);
                        startActivity(emailIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                }
                else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Please tag at least " + required + " contacts",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }
    /*public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        //displaying a toast to show the selected item from the spinner
        Toast.makeText(getApplicationContext(), "Selected Relationship: "+ relationshipMap[pos], Toast.LENGTH_SHORT).show();
        if(relationshipMap[pos] == "Father"){
            rb[i][0].setChecked(true);
        }
        if(relationshipMap[pos] == "Mother"){
            rb[i][1].setChecked(true);
        }
    }*/
    private void setMargins (View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }
}
