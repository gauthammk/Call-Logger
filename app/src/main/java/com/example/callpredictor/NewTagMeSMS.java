package com.example.callpredictor;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.io.FileOutputStream;

public class NewTagMeSMS extends AppCompatActivity {
    String[] SMSRecords;
    String[] relationshipMap = { "Friend", "Father", "Mother", "Spouse", "Child", "Sibling", "Colleague", "Other" };
    TextView titleTextView;
    Button nextButton;
    StringBuilder SMSRecordsCommaSeparated;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_tag_me_sms);

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
            titleTextView.setText(R.string.SMS_title);
        }else{
            System.out.println("No records found!");
        }

        // initialising user data components as arrays
        final RadioButton[][] rb = new RadioButton[SMSRecords.length][2];
        RadioGroup[] rg = new RadioGroup[SMSRecords.length];
        final Spinner[] relationshipSpinner = new Spinner[SMSRecords.length];
        final EditText[] age = new EditText[SMSRecords.length];
        final CheckBox[] hider = new CheckBox[SMSRecords.length];

        // looping through the records and creating corresponding user data components
        for (int i = 0; i < SMSRecords.length; i++) {

            // setting the layout to contain all the components in each record's display
            LinearLayout recordLayout = new LinearLayout(this);
            recordLayout.setOrientation(LinearLayout.VERTICAL);
            recordLayout.setGravity(Gravity.CENTER);
            // layout parameter to wrap_content on each of the user data components
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            // setting the hidden property
            hider[i] = new CheckBox(this);
            hider[i].setText("Hide");

            // setting the contact name
            TextView tv = new TextView(this);
            // not too sure about how to display this part
            tv.setText(SMSRecords[i].split(",")[3]);
            tv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
            tv.setTextSize(25);
            tv.setLayoutParams(layoutParams);
            tv.setPadding(20,20,20,20);

            // setting the radio buttons
            rg[i] = new RadioGroup(this);
            rg[i].setOrientation(RadioGroup.HORIZONTAL);
            rb[i][0] = new RadioButton(this);
            rb[i][0].setText("Male");
            rb[i][1] = new RadioButton(this);
            rb[i][1].setText("Female");
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
            age[i].setPadding(20,20,20,20);

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
            recordLayout.addView(age[i]);
            recordLayout.addView(rg[i]);
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
                // Adding the csv headers
                SMSRecordsCommaSeparated.append("ID, code, idk_what_this_is, Message, Date_Time, idk_what_this_is_again, Gender, Relationship, Age");
                // building the string to convert to csv
                for (int i = 0; i < SMSRecords.length; i++) {
                    boolean shouldBeHidden = hider[i].isChecked();
                    if (shouldBeHidden) {
                        System.out.print(SMSRecords[i].split(",")[0] + ": hidden");
                    }
                    if (rb[i][0].isChecked()) {
                        SMSRecordsCommaSeparated.append(SMSRecords[i] + ",Male," + relationshipSpinner[i].getSelectedItem() + "," + age[i].getText());
                    } else if (rb[i][1].isChecked()) {
                        SMSRecordsCommaSeparated.append(SMSRecords[i] + ",Female," + relationshipSpinner[i].getSelectedItem() + "," + age[i].getText());
                    } else {
                        SMSRecordsCommaSeparated.append(SMSRecords[i] + "," + relationshipSpinner[i].getSelectedItem() + "," + age[i].getText());
                    }
                }
                // printing out the csv string to the console
                System.out.println(SMSRecordsCommaSeparated.toString());
                // moving to the SMS screen
                Intent intent = new Intent(NewTagMeSMS.this, ThankYou.class);
                intent.putExtra("SMSRecords", SMSRecords);
                startActivity(intent);

                // writing tagged call records to the csv file
                try (FileOutputStream out = openFileOutput("SMS_data.csv", Context.MODE_PRIVATE)) {
                    // saving the file onto the device
                    out.write((SMSRecordsCommaSeparated.toString()).getBytes());
                    // exporting the saved csv file
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
