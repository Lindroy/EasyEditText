package com.lindroid.easyedittextdemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class SampleActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)
       /* etMax.setMaxCharsListener {
            Toast.makeText(this, "最大字数为$it", Toast.LENGTH_SHORT).show()
        }*/
    }
}
