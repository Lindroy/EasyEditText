package com.lindroid.easyedittextdemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_sample.*

private const val TAG = "EasyTag"

class SampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)
        etMax.setMaxCharsListener { maxChars, alertText ->
            Toast.makeText(this, "最大字数为$maxChars", Toast.LENGTH_SHORT).show()
        }
        etMax.setEmptyChangeListener { }
        etEmpty.setEmptyChangeListener { isEmpty ->
            if (isEmpty) {
                Toast.makeText(this, "内容已清空", Toast.LENGTH_SHORT).show()
            } else {
                Log.d(TAG, "输入框不为空")
            }
        }
        etWatcher.setOnContentChangeListener { content, count ->
            Log.d(TAG, "content:$content,count=$count")
        }

//        etCustomPwd.hideIcon = R.drawable.ic_close
//        etCustomPwd.displayIcon = R.drawable.ic_open
    }
}
