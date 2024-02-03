package com.specknet.pdiotapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.specknet.pdiotapp.R;
import com.specknet.pdiotapp.utils.DatabaseHelper;
import com.specknet.pdiotapp.utils.DatePickerFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryDisplayActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final String LOGTAG = "HistoryDisplayActivity";
    private TextInputEditText editTextDate;
    private TextInputEditText displayActivity;
    private TextView decriptionLabel;
    private TextView displayActivities;

    private TextView historyPerformedInfo;
    private Calendar cal = Calendar.getInstance();
    private String date = "";
    DatabaseHelper db = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_display);

        // Initialize views
        editTextDate = findViewById(R.id.editTextChosenDate);
        decriptionLabel = findViewById(R.id.decriptionHistoryLabel);
        displayActivity = findViewById(R.id.displayActivities);

        // Set an OnClickListener for the date picker button
        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOGTAG, "date clicked");
                DatePickerFragment newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        displayActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Display Activities
                Log.d("GETTING-DATE", date);
                String date = editTextDate.getText().toString();
                if(date != ""){
                    List<String> predictions = db.readData(date);

                    StringBuilder sb = new StringBuilder();
                    for(String pred: predictions){
                        sb.append(pred);
                        sb.append("\n");
                    }
                    runOnUiThread(() -> {
                        decriptionLabel.setText("Activities performed on " + date);
                        displayActivities = findViewById(R.id.historyPerformedInfo);
                        displayActivities.setText(sb);
                    });
                }
                else {
                    Toast.makeText(HistoryDisplayActivity.this, "Please Select a Date!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Set the chosen date in the TextInputEditText using SimpleDateFormat
        cal.set(year, month, day);

        // Use SimpleDateFormat to format the date for display
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = dateFormat.format(cal.getTime());

        editTextDate.setText(formattedDate);
        date = formattedDate;
    }

    public void processDate(int year, int month, int day) {
        Log.d(LOGTAG, "processDate");

        cal.set(year, month, day);

        // Use SimpleDateFormat to format the date for display
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = dateFormat.format(cal.getTime());

        // Log the formatted date
        Log.d(LOGTAG, "Formatted Date: " + formattedDate);

        // Update the EditText with the formatted date
        editTextDate.setText(formattedDate);

        // Log the result after setting the text
        Log.d(LOGTAG, "EditText Date: " + editTextDate.getText().toString());
    }
}
