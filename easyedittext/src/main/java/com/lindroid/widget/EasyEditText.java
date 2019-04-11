package com.lindroid.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import com.lindroid.view.R;

/**
 * @author Lin
 * @date 2019/4/11
 * @function 自定义EditText
 * @Description
 */
public class EasyEditText extends AppCompatEditText {
    private int clearIcon = R.drawable.ic_clear;
    private int displayIcon = R.drawable.ic_content_display;
    private int hideIcon = R.drawable.ic_content_hide;
    private boolean isShowClearButton = false;

    private boolean isEmpty = false;

    private TextWatcher textWatcher = null;

    private Drawable clearDrawable;

    private OnTextChangeListener textListener = null;

    public EasyEditText(Context context) {
        super(context);
    }

    public EasyEditText(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.editTextStyle);
    }

    public EasyEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EasyEditText, defStyleAttr, 0);
        clearIcon = ta.getResourceId(R.styleable.EasyEditText_clearContentIcon, clearIcon);
        displayIcon = ta.getResourceId(R.styleable.EasyEditText_displayContentIcon, displayIcon);
        hideIcon = ta.getResourceId(R.styleable.EasyEditText_hideContentIcon, hideIcon);
        isShowClearButton = ta.getBoolean(R.styleable.EasyEditText_showClearButton, isShowClearButton);
        ta.recycle();

        isEmpty = getText().toString().isEmpty();
    }


    private void setTextWatcher() {
        if (textWatcher != null) {
            return;
        }
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        addTextChangedListener(textWatcher);

    }

    public int getClearIcon() {
        return clearIcon;
    }

    public void setClearIcon(@DrawableRes int clearIcon) {
        this.clearIcon = clearIcon;
    }

    public int getDisplayIcon() {
        return displayIcon;
    }

    public void setDisplayIcon(@DrawableRes int displayIcon) {
        this.displayIcon = displayIcon;
    }

    public int getHideIcon() {
        return hideIcon;
    }

    public void setHideIcon(@DrawableRes int hideIcon) {
        this.hideIcon = hideIcon;
    }

    public boolean isShowClearButton() {
        return isShowClearButton;
    }

    public void setShowClearButton(boolean showClearButton) {
        isShowClearButton = showClearButton;
    }

    public interface OnTextChangeListener {
        void onChanged(CharSequence content);
    }

    /**
     * 设置文本改变监听事件
     */
    public void setTextChangeListener(OnTextChangeListener listener) {
        textListener = listener;
    }

    public interface OnMaxCharactersListener{

    }

}
