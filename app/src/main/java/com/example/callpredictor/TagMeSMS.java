package com.example.callpredictor;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;

public class TagMeSMS extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    //declaring view based components for manipulation
    TextView titleTextView;
    TextView recordTextView;
    Button nextButton;
    Spinner relationshipSpinner;
    RadioGroup genderRadioGroup;
    RadioButton genderRadioButton;
    EditText ageEditText;
    int arrayPointer = 0;
    String[] SMSRecords;
    String[] relationshipMap = { "Friend", "Father", "Mother", "Spouse", "Child", "Sibling", "Colleague", "Other" };
    StringBuilder SMSRecordsCommaSeparated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_me_sms);
        //getting the passed data from the previous activity
        Bundle extras = getIntent().getExtras();
        //initialisation of view components
        titleTextView = findViewById(R.id.title);
        recordTextView = findViewById(R.id.record);
        nextButton = findViewById(R.id.next);
        relationshipSpinner = findViewById(R.id.relationship);
        genderRadioGroup = findViewById(R.id.gender);
        ageEditText = findViewById(R.id.age);
        SMSRecordsCommaSeparated = new StringBuilder();
        //getting the SMSRecords array
        if(extras != null) {
            SMSRecords = extras.getStringArray("SMSRecords");
            titleTextView.setText(R.string.SMSInstructions);
        }else{
            System.out.println("No records found!");
        }
        //setting the spinner options
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, relationshipMap);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        relationshipSpinner.setAdapter(adapter);
        //listening for spinner item selections
        relationshipSpinner.setOnItemSelectedListener(this);
        //displaying the first record on the screen
        recordTextView.setText(SMSRecords[arrayPointer++]);
        //listening for next button click
        //my weird algorithm. The runtime of this is O(scary) but works well enough in practice.
        nextButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                //find selected gender from the genderRadioGroup
                genderRadioButton = findViewById(genderRadioGroup.getCheckedRadioButtonId());
                if( arrayPointer< SMSRecords.length){
                    //displaying call records one by one
                    SMSRecordsCommaSeparated.append(SMSRecords[arrayPointer - 1]).append(",").append(relationshipSpinner.getSelectedItem()).append(",").append(genderRadioButton.getText()).append(",").append(ageEditText.getText());
                    ageEditText.setText("");
                    recordTextView.setText(SMSRecords[arrayPointer++]);
                }else{
                    //last record is reached
                    SMSRecordsCommaSeparated.append(SMSRecords[arrayPointer - 1]).append(",").append(relationshipSpinner.getSelectedItem()).append(",").append(genderRadioButton.getText()).append(",").append(ageEditText.getText());
                    //go to TagMeSMS Activity
                    System.out.println(SMSRecordsCommaSeparated.toString());
                    Intent intent = new Intent(TagMeSMS.this, ThankYou.class);
                    startActivity(intent);
                    //writing tagged call records to the csv file
                    try (FileOutputStream out = openFileOutput("SMS_data.csv", Context.MODE_PRIVATE)) {
                        //saving the file into device
                        out.write((SMSRecordsCommaSeparated.toString()).getBytes());
                        //exporting the saved csv file
                        Context context = getApplicationContext();
                        File filelocation = new File(getFilesDir(), "SMS_data.csv");
                        Uri path = FileProvider.getUriForFile(context, "com.example.callpredictor.fileprovider", filelocation);
                        Intent fileIntent = new Intent(Intent.ACTION_SEND);
                        fileIntent.setType("text/csv");
                        fileIntent.putExtra(Intent.EXTRA_SUBJECT, "SMS Data");
                        fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        fileIntent.putExtra(Intent.EXTRA_STREAM, path);
                        startActivity(Intent.createChooser(fileIntent, "Send File"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        //displaying a toast to show the selected item from the spinner
        Toast.makeText(getApplicationContext(), "Selected Relationship: "+ relationshipMap[pos], Toast.LENGTH_SHORT).show();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}
