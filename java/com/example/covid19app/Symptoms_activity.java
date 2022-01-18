package com.example.covid19app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;


public class Symptoms_activity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    DatabaseHelper db_main;
    Button upload_signs;
    Spinner spinner;
    ArrayAdapter<CharSequence> adapter;
    RatingBar rating_bar;
    Map<String, Float> symptom_value = new HashMap<String, Float>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms);

        db_main = new DatabaseHelper(Symptoms_activity.this);
        rating_bar = (RatingBar) findViewById(R.id.ratingBar);
        upload_signs = (Button) findViewById(R.id.upload_symptom);
        spinner = findViewById(R.id.symptoms_list);
        adapter = ArrayAdapter.createFromResource(this, R.array.list_symptoms, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        symptom_value.put("nausea", 0.0f);
        symptom_value.put("headache", 0.0f);
        symptom_value.put("diarrhea", 0.0f);
        symptom_value.put("sore_throat", 0.0f);
        symptom_value.put("fever", 0.0f);
        symptom_value.put("muscle_ache", 0.0f);
        symptom_value.put("loss_of_smell_or_taste", 0.0f);
        symptom_value.put("cough", 0.0f);
        symptom_value.put("shortness_of_breath", 0.0f);
        symptom_value.put("tired", 0.0f);

        rating_bar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                String spinner_item = spinner.getSelectedItem().toString();
                String column_name = get_colum_name(spinner_item);
                float number_of_stars = rating_bar.getRating();
                symptom_value.put(column_name, number_of_stars);
            }
        });
        upload_signs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db_main.update_symptoms(symptom_value);
                Toast.makeText(Symptoms_activity.this, "Symptoms have been submitted", Toast.LENGTH_LONG).show();
            }
        });
    }

    private String get_colum_name(String spinner_item) {
        switch (spinner_item){
            case "Nausea" : return "nausea";
            case "Headache" : return "headache";
            case "Diarrhea" : return "diarrhea";
            case "Sore Throat" : return "sore_throat";
            case "Fever" : return "fever";
            case "Muscle Ache" : return "muscle_ache";
            case "Loss of smell or taste" : return "loss_of_smell_or_taste";
            case "Cough" : return "cough";
            case "Shortness of breath" : return "shortness_of_breath";
            case "Feeling tired" : return "tired";
            default: return "nausea";
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}