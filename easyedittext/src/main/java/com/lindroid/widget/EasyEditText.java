package com.lindroid.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;

import com.lindroid.view.R;

/**
 * @author Lin
 * @date 2019/4/11
 * @function 自定义EditText
 * @Description
 */
public class EasyEditText extends AppCompatEditText {
    private String TAG = "EasyTag";
    private int clearIcon = R.drawable.ic_clear;

    private int displayIcon = R.drawable.ic_content_display;

    private int hideIcon = R.drawable.ic_content_hide;

    /**
     * 最大输入字符数，小于或等于0表示不做限制
     */
    private int maxCharacters = -1;
    /**
     * 是否显示清空按钮
     */
    private boolean isShowClearButton = false;
    /**
     * 是否显示设置密码可见的按钮，前提是不显示删除按钮
     */
    private boolean isShowVisibilityToggle = false;
    /**
     * 是否显示明文
     */
    private boolean isDisplayContent = true;
    /**
     * 是否是密码输入模式
     */
    private boolean isPwdType = false;

    private boolean isEmpty = false;

    private TextWatcher textWatcher = null;

    private OnTextChangeListener textListener = null;

    private OnMaxCharactersListener maxListener = null;

    private OnEmptyChangeListener emptyListener = null;

    public EasyEditText(Context context) {
        this(context, null);
    }

    public EasyEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public EasyEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        checkInputType();
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EasyEditText, defStyleAttr, 0);
        clearIcon = ta.getResourceId(R.styleable.EasyEditText_clearContentIcon, clearIcon);
        displayIcon = ta.getResourceId(R.styleable.EasyEditText_displayContentIcon, displayIcon);
        hideIcon = ta.getResourceId(R.styleable.EasyEditText_hideContentIcon, hideIcon);
        setShowClearButton( ta.getBoolean(R.styleable.EasyEditText_showClearButton, isShowClearButton));
        isShowVisibilityToggle =
                ta.getBoolean(R.styleable.EasyEditText_showVisibilityToggle, isShowVisibilityToggle);
        setMaxCharacters(ta.getInt(R.styleable.EasyEditText_maxCharacters, maxCharacters));
        ta.recycle();
        if (getText() != null) {
            isEmpty = getText().toString().isEmpty();
        }
        initContentToggle();
    }

    /**
     * 根据inputType判断是不是密码输入类型，从而决定显示明文或暗文
     */
    private void checkInputType() {
        switch (getInputType()) {
            case EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD: //可见的密码
                isPwdType = true;
                isDisplayContent = true;
                break;
            case EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD://文本密码
            case EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD://数字密码
            case EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD://网址形式的密码
                isPwdType = true;
                isDisplayContent = false;
                break;
            default:
                isPwdType = false;
                isDisplayContent = true;
                break;

        }
    }

    private void initContentToggle() {
        if (isPwdType && !isShowClearButton && isShowVisibilityToggle) {
            setVisibilityDrawable();
        }
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
                CharSequence content = s == null ? "" : s;
                //监听是否为空
                if (emptyListener != null) {
                    if (TextUtils.isEmpty(content)) {
                        isEmpty = true;
                    } else {
                        if (!isEmpty) {
                            return;
                        }
                        isEmpty = false;
                    }
                    emptyListener.onEmpty(isEmpty);
                }

                //监听最大输入字符
                if (maxCharacters > 0 && content.length() > maxCharacters) {
                    setText(content.toString().substring(0, maxCharacters));
                    //光标移至最末端
                    assert getText() != null;
                    setSelection(getText().length());
                    if (maxListener != null) {
                        maxListener.onMaxChars(maxCharacters);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //监听字符输入
                if (textListener != null) {
                    textListener.onChanged(s.toString(), s.toString().length());
                }
                if (isShowClearButton) {
                    setClearButton();
                }
            }
        };
        addTextChangedListener(textWatcher);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int eventX = (int) event.getX();
            int eventY = (int) event.getY();
            //设置响应点击的区域
            Rect rect = new Rect();
            getLocalVisibleRect(rect);
            rect.left = rect.right - 100;
            if (rect.contains(eventX, eventY)) {
                if (isShowClearButton) {
                    setText("");
                } else if (isShowVisibilityToggle) {
                    isDisplayContent = !isDisplayContent;
                    if (isDisplayContent) {
                        setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    } else {
                        setTransformationMethod(PasswordTransformationMethod.getInstance());
                    }
                    setVisibilityDrawable();
                    setSelection(length());
                }
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 设置一键清除按钮
     */
    private void setClearButton() {
        if (length() > 0 && !hasRightDrawable()) {
            setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(getContext(), clearIcon), null);
        } else if (length() <= 0 && hasRightDrawable()) {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

        }
    }

    /**
     * 切换内容是否可见的图标
     */
    private void setVisibilityDrawable() {
        Drawable ivVisibility;
        if (isDisplayContent) {
            ivVisibility = ContextCompat.getDrawable(getContext(), displayIcon);
        } else {
            ivVisibility = ContextCompat.getDrawable(getContext(), hideIcon);
        }
        setCompoundDrawablesWithIntrinsicBounds(null, null, ivVisibility, null);
    }

    private Drawable getRightDrawable() {
        return getCompoundDrawables()[2];
    }

    /**
     * 右侧是否有图标
     */
    private boolean hasRightDrawable() {
        return getRightDrawable() != null;
    }

    private void removeDrawable() {
        setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
    }


    public interface OnTextChangeListener {
        /**
         * @param content:文本内容
         * @param count:当前的文本长度
         */
        void onChanged(@NonNull CharSequence content, int count);
    }

    /**
     * 设置文本改变监听事件
     */
    public void setTextChangeListener(OnTextChangeListener listener) {
        textListener = listener;
        setTextWatcher();
    }

    public interface OnMaxCharactersListener {
        void onMaxChars(int maxChars);
    }

    public void setMaxCharsListener(OnMaxCharactersListener listener) {
        maxListener = listener;
    }

    public interface OnEmptyChangeListener {
        void onEmpty(boolean isEmpty);
    }

    public void setEmptyChangeListener(OnEmptyChangeListener listener) {
        emptyListener = listener;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        textListener = null;
        maxListener = null;
        emptyListener = null;
    }

    public int getClearIcon() {
        return clearIcon;
    }

    public void setClearIcon(@DrawableRes int clearIcon) {
        this.clearIcon = clearIcon;
        setTextWatcher();
    }

    public int getDisplayIcon() {
        return displayIcon;
    }

    public void setDisplayIcon(@DrawableRes int displayIcon) {
        this.displayIcon = displayIcon;
        initContentToggle();
    }

    public int getHideIcon() {
        return hideIcon;
    }

    public void setHideIcon(@DrawableRes int hideIcon) {
        this.hideIcon = hideIcon;
        initContentToggle();
    }

    public int getMaxCharacters() {
        return maxCharacters;
    }

    public void setMaxCharacters(int maxCharacters) {
        if (this.maxCharacters == maxCharacters) {
            return;
        }
        this.maxCharacters = maxCharacters;
        if (maxCharacters > 0) {
            setTextWatcher();
        }
    }

    public boolean isShowClearButton() {
        return isShowClearButton;
    }

    public void setShowClearButton(boolean showClearButton) {
        if (isShowClearButton == showClearButton) {
            return;
        }
        isShowClearButton = showClearButton;
        if (isShowClearButton) {
            setTextWatcher();
        } else {
            removeDrawable();
        }
    }

    public boolean isShowVisibilityToggle() {
        return isShowVisibilityToggle;
    }

    public void setShowVisibilityToggle(boolean showVisibilityToggle) {
        if (isShowVisibilityToggle == showVisibilityToggle || isShowClearButton) {
            return;
        }
        isShowVisibilityToggle = showVisibilityToggle;
        if (isShowVisibilityToggle && !hasRightDrawable()) {
            setVisibilityDrawable();
        } else {
            removeDrawable();
        }
    }
}
