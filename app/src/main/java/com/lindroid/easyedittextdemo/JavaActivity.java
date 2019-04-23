package com.lindroid.easyedittextdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.lindroid.view.EasyEditText;

public class JavaActivity extends AppCompatActivity {
    private EasyEditText etWatcher;
    private EasyEditText etEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        etWatcher = findViewById(R.id.etWatcher);
        etEmpty = findViewById(R.id.etEmpty);
//        etEmpty.setMaxCharsAlert(null);


    }
}
