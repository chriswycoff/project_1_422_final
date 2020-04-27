package com.example.map_test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class Facts extends AppCompatActivity {

    private TextView factTextView;
    int fact_number = 0;
    int num_facts = 4;
    String[] facts_array = {
            "Diseases can make anyone sick regardless " +
                    "of their race or ethnicity."
            , "For most people, the immediate risk of becoming seriously " +
            "ill from the virus that causes COVID-19 is thought to be low."
            , "Wash your hands often with soap and water for at least 20 seconds, " +
            "especially after blowing your nose, coughing, or sneezing; " +
            "going to the bathroom; and before eating or preparing food.",
            "You are helping fight Covid-19 " +
            "by providing " +
            "anonymous geolocation data. Thank You! "};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facts);

        factTextView = findViewById(R.id.factTextView);

    }


    public void updateButtonNextFactOnClick(View view){
        try{
            factTextView.setText(facts_array[fact_number]);
            fact_number += 1;

            if (fact_number >= num_facts){
                fact_number = 0;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void updateButtonThreeOnClick(View view){
        try{
            finish();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
