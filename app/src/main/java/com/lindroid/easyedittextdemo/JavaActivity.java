package com.lindroid.easyedittextdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.lindroid.view.EasyEditText;

public class JavaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java);
        EasyEditText editText = findViewById(R.id.editText);
        editText.setDisplayIcon(R.drawable.ic_open);
        editText.setHideIcon(R.drawable.ic_close);
    }
}
