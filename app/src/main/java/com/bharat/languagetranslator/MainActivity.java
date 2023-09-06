package com.bharat.languagetranslator;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    Spinner spinner1, spinner2;
    TextInputEditText sourceEdt;
    ImageView micIV;
    MaterialButton translatebtn;
    TextView translatedTV;
    String[] fromlanguages = {"From", "English", "Hindi", "Afrikaans", "French", "italian", "Russian", "Telugu", "Urdu", "Bengali", "Arabic"};
    String[] tolanguages = {"to", "English", "Hindi", "Afrikaans", "French", "italian", "Russian", "Telugu", "Urdu", "Bengali", "Arabic"};


    private static final int REQUEST_PERMISSION_CODE = 1;
    int languageCode, fromlanguageCode, tolanguagecode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinner1 = findViewById(R.id.spinner1);
        spinner2 = findViewById(R.id.spinner2);
        sourceEdt = findViewById(R.id.sourceEdt);
        micIV = findViewById(R.id.micIV);
        translatebtn = findViewById(R.id.translatebtn);
        translatedTV = findViewById(R.id.translatedTV);

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fromlanguageCode = getLanguageCode(fromlanguages[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        ArrayAdapter formAdapetr = new ArrayAdapter(this, R.layout.spinner_item, fromlanguages);
        formAdapetr.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner1.setAdapter(formAdapetr);


        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tolanguagecode = getLanguageCode(fromlanguages[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ArrayAdapter toAdapetr = new ArrayAdapter(this, R.layout.spinner_item, tolanguages);
        toAdapetr.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(toAdapetr);

        translatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                translatedTV.setText("");
                if (sourceEdt.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "please enter your text to translate", Toast.LENGTH_SHORT).show();

                } else if (fromlanguageCode == 0) {
                    Toast.makeText(MainActivity.this, "please select source language", Toast.LENGTH_SHORT).show();
                } else if (tolanguagecode == 0) {
                    Toast.makeText(MainActivity.this, "please select the language to make translation", Toast.LENGTH_SHORT).show();
                } else {
                    translatedText(fromlanguageCode, tolanguagecode, sourceEdt.getText().toString());
                }
            }
        });
        micIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to convert into text");
                try {
                    startActivityForResult(intent, REQUEST_PERMISSION_CODE);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "+e.getMessage()", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_PERMISSION_CODE){
            if (requestCode==RESULT_OK && data!=null){
                ArrayList<String> result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                sourceEdt.setText(result.get(0));
            }
        }

 }

    private void translatedText(int fromlanguageCode, int tolanguagecode, String source){

        translatedTV.setText("Downloading Modal..");


        options= new FirebaseTranslatorOptions.Builder().setSourceLanguage(fromlanguageCode).setTargetLanguage(tolanguagecode).build();
        FirebaseTranslator translator=FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions =new FirebaseModelDownloadConditions.Builder().build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                translatedTV.setText("Translating..");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        translatedTV.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Fail.to translate:"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "fail to download language Modal"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public int getLanguageCode(String language) {
        int languageCode = 0;
        FirebaseLanguageIdentification languageIdentifier =
                FirebaseNaturalLanguage.getInstance().getLanguageIdentification();
        languageIdentifier.identifyLanguage("text")
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@Nullable String languageCode) {
                                if (languageCode != "und") {
                                    Log.i(TAG, "Language: " + languageCode);
                                } else {
                                    Log.i(TAG, "Can't identify language.");
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
        switch (language) {
            case "English":
                languageCode = FirebaseTranslateLanguage.EN;
                break;
            case "Hindi":
                languageCode = FirebaseTranslateLanguage.HI;
                break;
            case "Afrikaans":
                languageCode = FirebaseTranslateLanguage.AF;
                break;
            case "French":
                languageCode = FirebaseTranslateLanguage.FR;
                break;
            case "italian":
                languageCode = FirebaseTranslateLanguage.IT;
                break;
            case "Russian":
                languageCode = FirebaseTranslateLanguage.RU;
                break;
            case "Telugu":
                languageCode = FirebaseTranslateLanguage.TE;
                break;
            case "Urdu":
                languageCode = FirebaseTranslateLanguage.UR;
                break;
            case "Bengali":
                languageCode = FirebaseTranslateLanguage.BN;
                break;
            case "Arabic":
                languageCode = FirebaseTranslateLanguage.AR;
                break;

            default:
                languageCode =0;
                break;
        }
        return languageCode;


    }
}