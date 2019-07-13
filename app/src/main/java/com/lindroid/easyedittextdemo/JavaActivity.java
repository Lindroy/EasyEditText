package com.lindroid.easyedittextdemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import com.lindroid.view.EasyEditText;

public class JavaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
//        etEmpty.setMaxCharsAlert(null);
        EasyEditText editText = findViewById(R.id.editText);
        /*
          监听输入框内容是否为空
         */
        editText.setEmptyChangeListener(new EasyEditText.OnEmptyChangeListener() {
            @Override
            public void onEmpty(boolean isEmpty) {
                //输入框内容从有到无或从无到有会回调此方法，如果已经有内容，再继续输入的话则不会调用此方法
            }
        });
        /*
          监听是否达到了最大输入字符数
         */
        editText.setMaxCharsListener(new EasyEditText.OnMaxCharactersListener() {
            @Override
            /*
             * @param maxChars:最大输入字符数
             * @param alertText:超过最大输入字符数时的提示文字
             */
            public void onMaxChars(int maxChars, @NonNull String alertText) {

            }
        });
        /*
          监听输入框内容变化
         */
        editText.setOnContentChangeListener(new EasyEditText.OnContentChangeListener() {
            @Override
            /*
             * @param content:文本内容
             * @param count:当前的文本长度
             */
            public void onChanged(@NonNull CharSequence content, int count) {

            }
        });

        /*
          设置文本改变前的监听事件
         */
        editText.setBeforeTextChangeListener(new EasyEditText.BeforeTextChangeListener() {
            @Override
            public void onBefore(@NonNull CharSequence s, int start, int count, int after) {

            }
        });
        /*
          设置文本改变监听事件
         */
        editText.setOnTextChangeListener(new EasyEditText.OnTextChangeListener() {
            @Override
            public void onChange(@NonNull CharSequence s, int start, int before, int count) {

            }
        });
        /*
          设置文本改变后的监听
         */
        editText.setAfterTextChangeListener(new EasyEditText.AfterTextChangeListener() {
            @Override
            public void onAfter(Editable s) {

            }
        });



    }
}
