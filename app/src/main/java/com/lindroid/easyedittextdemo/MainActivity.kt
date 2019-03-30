package com.lindroid.easyedittextdemo

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "EasyEditText"

class MainActivity : AppCompatActivity() {

    private val mContext:Context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        editText.apply {
            setOnTextChangeListener {
                Log.d(TAG, "输入的内容：$it")
            }
            setMaxLengthListener {
                Toast.makeText(mContext,"输入的内容过长",Toast.LENGTH_SHORT).show()
            }
        }

    }
}
