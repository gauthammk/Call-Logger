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

//Tagging screen Activity
public class TagMeCalls extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    //declaration of view based components for manipulation
    TextView titleTextView;
    TextView recordTextView;
    Button nextButton;
    Spinner relationshipSpinner;
    RadioGroup genderRadioGroup;
    RadioButton genderRadioButton;
    EditText ageEditText;
    int arrayPointer = 0;
    String[] callRecords, SMSRecords;
    String[] relationshipMap = { "Friend", "Father", "Mother", "Spouse", "Child", "Sibling", "Colleague", "Other" };
    StringBuilder callRecordsCommaSeparated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_me_calls);
        //getting the passed data from the previous activity
        Bundle extras = getIntent().getExtras();
        //initialisation of view components
        titleTextView = findViewById(R.id.title);
        recordTextView = findViewById(R.id.record);
        nextButton = findViewById(R.id.next);
        relationshipSpinner = findViewById(R.id.relationship);
        genderRadioGroup = findViewById(R.id.gender);
        ageEditText = findViewById(R.id.age);
        callRecordsCommaSeparated = new StringBuilder();
        //getting the callRecords and SMSRecords array
        if(extras != null) {
            callRecords = extras.getStringArray("callRecords");
            SMSRecords = extras.getStringArray("SMSRecords");
            System.out.println("Length of Call Record Array : " + callRecords.length);
            System.out.println("Length of SMS record Array : " + SMSRecords.length);
            titleTextView.setText(R.string.CallInstructions);
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
        recordTextView.setText(callRecords[arrayPointer++]);
        //listening for next button click
        //my weird algorithm. The runtime of this is O(scary) but works well enough in practice.
        nextButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                //find selected gender from the genderRadioGroup
                genderRadioButton = findViewById(genderRadioGroup.getCheckedRadioButtonId());
                if(arrayPointer < callRecords.length){
                    //displaying call records one by one
                    callRecordsCommaSeparated.append(callRecords[arrayPointer- 1]).append(",").append(relationshipSpinner.getSelectedItem()).append(",").append(genderRadioButton.getText()).append(",").append(ageEditText.getText());
                    ageEditText.setText("");
                    recordTextView.setText(callRecords[arrayPointer++]);
                }else{
                    //last record is reached
                    callRecordsCommaSeparated.append(callRecords[arrayPointer- 1]).append(",").append(relationshipSpinner.getSelectedItem()).append(",").append(genderRadioButton.getText()).append(",").append(ageEditText.getText());
                    //go to TagMeSMS Activity
                    System.out.println(callRecordsCommaSeparated.toString());
                    Intent intent = new Intent(TagMeCalls.this, TagMeSMS.class);
                    intent.putExtra("SMSRecords", SMSRecords);
                    startActivity(intent);
                    //writing tagged call records to the csv file
                    try (FileOutputStream out = openFileOutput("Call_data.csv", Context.MODE_PRIVATE)) {
                        //saving the file onto the device
                        out.write((callRecordsCommaSeparated.toString()).getBytes());
                        //exporting the saved csv file
                        Context context = getApplicationContext();
                        File filelocation = new File(getFilesDir(), "Call_data.csv");
                        Uri path = FileProvider.getUriForFile(context, "com.example.callpredictor.fileprovider", filelocation);
                        Intent fileIntent = new Intent(Intent.ACTION_SEND);
                        fileIntent.setType("text/csv");
                        fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Call Data");
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
