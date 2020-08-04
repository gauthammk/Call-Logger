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
import java.util.ArrayList;
import java.util.HashMap;

public class NewTagMeSMS extends AppCompatActivity {
    String[] SMSRecords, freqSms;
    String[] relationshipMap = {"Tag Here", "Friend", "Work", "School/College", "Father", "Mother", "Spouse/Partner", "Sister", "Brother", "Daughter", "Son", "Relative", "Other" };
    TextView titleTextView;
    Button nextButton;
    int arrayPointer = 0;
    int required;
    int index;
    String hashNum, call_data, UserName, TimeStamp;
    StringBuilder SMSRecordsCommaSeparated;
    HashMap<String, String> nameHash = new HashMap<String, String>();
    HashMap<String, String> smsHash = new HashMap<String, String>();
    HashMap<String, Integer> idHash = new HashMap<String, Integer>();
    int maxId;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_tag_me_sms);

        Intent inte = getIntent();
        // initialising the fixed components
        titleTextView = findViewById(R.id.title);
        SMSRecordsCommaSeparated = new StringBuilder();
        LinearLayout mainContent = findViewById(R.id.mainContent);
        nextButton = findViewById(R.id.next);

        // getting the passed data from the previous activity
        Bundle extras = getIntent().getExtras();
        // getting the callRecords and SMSRecords array
        if(extras != null) {
            SMSRecords = extras.getStringArray("SMSRecords");
            freqSms = extras.getStringArray("freqSms");
            titleTextView.setText(R.string.SMS_title);
            nameHash = (HashMap<String, String>)inte.getSerializableExtra("nameHash");
            smsHash = (HashMap<String, String>)inte.getSerializableExtra("smsHash");
            call_data = extras.getString("call_data");
            idHash = (HashMap<String, Integer>)inte.getSerializableExtra("idHash");
            maxId = extras.getInt("maxId");
            UserName = extras.getString("UserName");
            TimeStamp = extras.getString("TimeStamp");
        }else{
            titleTextView.setText("No records found!");
        }

        // initialising user data components as arrays
        required = freqSms.length <= 15 ? freqSms.length : 15;
        //final RadioButton[][] rb = new RadioButton[freqSms.length][2];
        //RadioGroup[] rg = new RadioGroup[freqSms.length];
        final Spinner[] relationshipSpinner = new Spinner[freqSms.length];
        //final EditText[] age = new EditText[freqSms.length];
        final CheckBox[] hider = new CheckBox[freqSms.length];

        // looping through the records and creating corresponding user data components
        for (int i = 0; i < freqSms.length; i++) {

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
            // not too sure about how to display this part

            tv.setText(nameHash.get(freqSms[i]));
            tv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
            tv.setTextSize(25);
            tv.setLayoutParams(layoutParams);
            tv.setPadding(20,20,20,20);

            /*// setting the radio buttons
            rg[i] = new RadioGroup(this);
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

                int taggedContacts = 0;
                for (int i = 0; i < freqSms.length; i++) {
                    if (relationshipSpinner[i].getSelectedItem() != "Tag Here") {
                        taggedContacts++;
                    }
                }
                if (taggedContacts >= required) {
                    // Adding the csv headers
                    SMSRecordsCommaSeparated.append("ID, Name, Relationship, Message, Date_Time\n");
                    // building the string to convert to csv
                    for (int i = 0; i < freqSms.length; i++) {
                        boolean shouldBeHidden = hider[i].isChecked();
                        String s = "";
                        if (shouldBeHidden == false)
                            s += nameHash.get(freqSms[i]) + ", " + relationshipSpinner[i].getSelectedItem();
                        else
                            s += "null" + ", " + relationshipSpinner[i].getSelectedItem();
                    /*if (rb[i][0].isChecked()) {
                        s += ", Male, " + relationshipSpinner[i].getSelectedItem() + ", " + age[i].getText();
                    } else if (rb[i][1].isChecked()){
                        s += ", Female, " + relationshipSpinner[i].getSelectedItem() + ", " + age[i].getText();
                    }
                    else{
                        s += ", , " + relationshipSpinner[i].getSelectedItem() + ", " + age[i].getText();
                    }*/
                        smsHash.replace(freqSms[i], s);
                    }
                    while (arrayPointer < SMSRecords.length) {
                        //System.out.println("Array Pointer = "+arrayPointer);
                        //index = callRecords[arrayPointer].indexOf(",")+1;
                        //index = SMSRecords[arrayPointer].indexOf(",") + 1;
                        index = 0;
                        hashNum = SMSRecords[arrayPointer].substring(index, index + 8);
                        SMSRecordsCommaSeparated.append(idHash.get(hashNum)).append(", ").append(smsHash.get(hashNum)).append(",").append(SMSRecords[arrayPointer].substring(9));
                        arrayPointer++;
                    }
                    // printing out the csv string to the console
                    System.out.println("---------------------------------Sms Data Csv --------------------------------\n" + SMSRecordsCommaSeparated.toString());
                    // moving to the SMS screen
                    Intent tintent = new Intent(NewTagMeSMS.this, ThankYou.class);
                    tintent.putExtra("SMSRecords", SMSRecords);
                    startActivity(tintent);

                    // writing tagged call records to the csv file
                    Context context = getApplicationContext();

                    String smscsv = "SMS_data.csv";
                    String callcsv = "Call_data.csv";
                    String tactxt = "terms_and_conditions.txt";
                    String heading = "CONSENT FORM FOR COLLECTION OF PERSONAL DATA BY INSTITUTE\n\n";
                    String terms = heading + context.getResources().getString(R.string.terms) + "\n\nName : "+UserName+"\nTime Stamp : "+TimeStamp;
                    try{
                        FileOutputStream callout = openFileOutput(callcsv, Context.MODE_PRIVATE);
                        FileOutputStream smsout = openFileOutput(smscsv, Context.MODE_PRIVATE);
                        FileOutputStream tacout = openFileOutput(tactxt, Context.MODE_PRIVATE);
                        //saving the file into device
                        callout.write(call_data.getBytes());
                        smsout.write((SMSRecordsCommaSeparated.toString()).getBytes());
                        tacout.write(terms.getBytes());

                        //exporting the saved csv file

                        File callfilelocation = new File(getFilesDir(), callcsv);
                        File smsfilelocation = new File(getFilesDir(), smscsv);
                        File tacfilelocation = new File(getFilesDir(), tactxt);
                        Uri callpath = FileProvider.getUriForFile(context, "com.example.dataapp.fileprovider", callfilelocation);
                        Uri smspath = FileProvider.getUriForFile(context, "com.example.dataapp.fileprovider", smsfilelocation);
                        Uri tacpath = FileProvider.getUriForFile(context, "com.example.dataapp.fileprovider", tacfilelocation);
                        System.out.println("---------------------------" + callpath + "------------------------");
                        System.out.println("---------------------------" + smspath + "------------------------");
                        System.out.println("---------------------------" + tacpath + "------------------------");
                        String email = "bms.datacollection@gmail.com";
                        String subject = "Call Data and Sms Data";
                        String message = "Call Data Records and SMS Data Records";
                        final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                        emailIntent.setType("text/csv");
                        emailIntent.setType("vnd.android.cursor.dir/email");
                        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{email});
                        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
                        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
                        emailIntent.setPackage("com.google.android.gm");
                        if (callpath != null && smspath != null) {
                            ArrayList<Uri> uris = new ArrayList<Uri>();
                            uris.add(callpath);
                            uris.add(smspath);
                            uris.add(tacpath);
                            emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                        }
                        startActivity(Intent.createChooser(emailIntent, "Sending email..."));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
    private void setMargins (View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }
}