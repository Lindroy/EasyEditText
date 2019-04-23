package com.lindroid.view;

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
import android.util.Log;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

/**
 * @author Lin
 * @date 2019/4/11
 * @function 自定义EditText
 * @Description
 */
public class EasyEditText extends AppCompatEditText {
    private String TAG = "EasyTag";
    /**
     * 一键清空按钮图片Id
     */
    private int clearIcon = R.drawable.ic_eet_clear;
    /**
     * 内容显示为明文的图片Id
     */
    private int plainTextIcon = R.drawable.ic_eet_content_plain;
    /**
     * 内容显示为暗文的图片Id
     */
    private int cipherTextIcon = R.drawable.ic_eet_content_cipher;
    /**
     * 最大输入字符数，小于或等于0表示不做限制
     */
    private int maxCharacters = -1;
    /**
     * 超出最大字符输入数时的提示文字
     */
    @NonNull()
    private String maxCharsAlert;
    /**
     * 超出最大字符输入数时的提示文字，包含字数
     */
    @NonNull
    private String maxCharsAlertWithCount;
    /**
     * 超出最大字符输入数时是否弹出Toast
     */
    private boolean showMaxCharsAlertToast;

    /**
     * 是否显示清空按钮
     */
    private boolean isShowClearButton = false;
    /**
     * 是否显示设置密码可见的按钮，前提是不显示删除按钮
     */
    private boolean isShowPlainCipherToggle;
    /**
     * 是否显示明文
     */
    private boolean isDisplayContent = true;
    /**
     * 是否是密码输入模式
     */
    private boolean isPwdType = false;

    /**
     * 达到最大输入字符数时是否限制输入
     */
    private boolean isMaxCharsLimited;

    private boolean isEmpty = false;

    private TextWatcher textWatcher = null;

    private OnContentChangeListener contentListener = null;

    private BeforeTextChangeListener beforeListener = null;

    private OnTextChangeListener changeListener = null;

    private AfterTextChangeListener afterListener = null;

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
        plainTextIcon = ta.getResourceId(R.styleable.EasyEditText_plainTextIcon, plainTextIcon);
        cipherTextIcon = ta.getResourceId(R.styleable.EasyEditText_cipherTextIcon, cipherTextIcon);
        setShowClearButton(ta.getBoolean(R.styleable.EasyEditText_showClearButton, false));
        isShowPlainCipherToggle =
                ta.getBoolean(R.styleable.EasyEditText_showPlainCipherToggle, false);
        setMaxCharacters(ta.getInt(R.styleable.EasyEditText_maxCharacters, maxCharacters));
        isMaxCharsLimited = ta.getBoolean(R.styleable.EasyEditText_maxCharsLimited, true);
        maxCharsAlert = checkNull(ta.getString(R.styleable.EasyEditText_maxCharsAlert));
        maxCharsAlertWithCount = checkNull(ta.getString(R.styleable.EasyEditText_maxCharsAlert));
        showMaxCharsAlertToast = ta.getBoolean(R.styleable.EasyEditText_showMaxCharsAlertToast, false);
        ta.recycle();
        if (getText() != null) {
            isEmpty = getText().toString().isEmpty();
        }
        initContentToggle();
    }

    private String checkNull(String string) {
        if (string == null) {
            return "";
        }
        return string;
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
        if (isPwdType && !isShowClearButton && isShowPlainCipherToggle) {
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
                if (beforeListener != null) {
                    beforeListener.onBefore(s == null ? "" : s, start, count, after);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                CharSequence content = s == null ? "" : s;
                if (changeListener != null) {
                    changeListener.onChange(content, start, before, count);
                }
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
                    if (isMaxCharsLimited) {
                        setText(content.toString().substring(0, maxCharacters));
                        //光标移至最末端
                        assert getText() != null;
                        setSelection(getText().length());
                    }
                    String alertText;
                    if (!maxCharsAlertWithCount.isEmpty()) {
                        alertText = String.format(maxCharsAlertWithCount, maxCharacters);
                    } else if (!maxCharsAlert.isEmpty()) {
                        alertText = maxCharsAlert;
                    } else {
                        alertText = String.format(getContext().getString(R.string.eet_max_chars_alert_with_count), maxCharacters);
                    }
                    if (showMaxCharsAlertToast) {
                        Toast.makeText(getContext(), alertText, Toast.LENGTH_SHORT).show();
                    }
                    if (maxListener != null) {
                        maxListener.onMaxChars(maxCharacters, alertText);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (afterListener != null) {
                    afterListener.onAfter(s);
                }
                //监听字符输入
                if (contentListener != null) {
                    contentListener.onChanged(s.toString(), s.toString().length());
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
                } else if (isShowPlainCipherToggle) {
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
     * 输入框内容为空时隐藏一键清空按钮
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
            ivVisibility = ContextCompat.getDrawable(getContext(), plainTextIcon);
        } else {
            ivVisibility = ContextCompat.getDrawable(getContext(), cipherTextIcon);
        }
        setCompoundDrawablesWithIntrinsicBounds(null, null, ivVisibility, null);
    }

    /**
     * 获取右侧的图标
     */
    private Drawable getRightDrawable() {
        return getCompoundDrawables()[2];
    }

    /**
     * 右侧是否有图标
     */
    private boolean hasRightDrawable() {
        return getRightDrawable() != null;
    }

    /**
     * 移除右侧所有图标
     */
    private void removeDrawable() {
        setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
    }

    public int getClearIcon() {
        return clearIcon;
    }

    /**
     * 设置一键清空图标Id
     */
    public void setClearIcon(@DrawableRes int clearIcon) {
        this.clearIcon = clearIcon;
        setTextWatcher();
    }

    /**
     * 获取明文按钮图标资源Id
     */
    public int getPlainTextIcon() {
        return plainTextIcon;
    }

    /**
     * 设置明文按钮图标资源Id
     */
    public void setPlainTextIcon(@DrawableRes int plainTextIcon) {
        this.plainTextIcon = plainTextIcon;
        initContentToggle();
    }

    /**
     * 获取暗文按钮图标资源Id
     */
    public int getCipherTextIcon() {
        return cipherTextIcon;
    }

    /**
     * 设置暗文按钮图标资源Id
     */
    public void setCipherTextIcon(@DrawableRes int cipherTextIcon) {
        this.cipherTextIcon = cipherTextIcon;
        initContentToggle();
    }

    public int getMaxCharacters() {
        return maxCharacters;
    }

    /**
     * 设置最大输入字符数
     */
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

    /**
     * 设置是否显示一键清空按钮
     *
     * @param showClearButton:true时则显示
     */
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

    public String getMaxCharsAlert() {
        return maxCharsAlert;
    }

    public void setMaxCharsAlert(@NonNull String alert) {
        Log.e(TAG, "alert=" + alert);
        maxCharsAlert = alert;
    }

    public String getMaxCharsAlertWithCount() {
        return maxCharsAlertWithCount;
    }

    public void setMaxCharsAlertWithCount(String alertWithCount) {
        maxCharsAlertWithCount = alertWithCount;
    }

    public boolean isShowMaxCharsAlertToast() {
        return showMaxCharsAlertToast;
    }

    public void setShowMaxCharsAlertToast(boolean showToast) {
        this.showMaxCharsAlertToast = showToast;
    }

    public boolean isShowPlainCipherToggle() {
        return isShowPlainCipherToggle;
    }

    /**
     * 是否显示明暗文切换按钮
     * 需要满足两个前提：
     * 1、输入类型为密码类型；
     * 2、没有显示一键清空按钮。
     *
     * @param showPlainCipherToggle:true时显示按钮
     */
    public void setShowPlainCipherToggle(boolean showPlainCipherToggle) {
        if (isShowPlainCipherToggle == showPlainCipherToggle || isShowClearButton) {
            return;
        }
        isShowPlainCipherToggle = showPlainCipherToggle;
        if (isShowPlainCipherToggle && !hasRightDrawable()) {
            setVisibilityDrawable();
        } else {
            removeDrawable();
        }
    }

    public boolean isMaxCharsLimited() {
        return isMaxCharsLimited;
    }

    /**
     * 设置达到最大输入字数后是否限制输入
     *
     * @param maxCharsLimited:是否限制继续输入
     */
    public void setMaxCharsLimited(boolean maxCharsLimited) {
        isMaxCharsLimited = maxCharsLimited;
    }

    /**
     * 文本改变前的监听接口
     */
    public interface BeforeTextChangeListener {
        /**
         * @param s:改变前的字符
         * @param start:即将修改的位置
         * @param count:即将被修改的文字长度，新增则为0
         * @param after:被修改的文字长度，删除则为0
         */
        void onBefore(@NonNull CharSequence s, int start, int count, int after);
    }

    /**
     * 设置文本改变前的监听事件
     */
    public void setBeforeTextChangeListener(BeforeTextChangeListener listener) {
        beforeListener = listener;
        setTextWatcher();
    }

    /**
     * 文本改变时的监听接口
     */
    public interface OnTextChangeListener {
        /**
         * @param s:文本内容
         * @param start:有变动的字符序号
         * @param before:变动的字符长度，新增的话则为0
         * @param count:添加的字符长度，删除的话则为0
         */
        void onChange(@NonNull CharSequence s, int start, int before, int count);
    }

    /**
     * 设置文本改变监听事件
     */
    public void setOnTextChangeListener(OnTextChangeListener listener) {
        changeListener = listener;
        setTextWatcher();
    }

    /**
     * 文本改变后的监听接口
     */
    public interface AfterTextChangeListener {
        void onAfter(Editable s);
    }

    /**
     * 设置文本改变后的监听
     */
    public void setAfterTextChangeListener(AfterTextChangeListener listener) {
        afterListener = listener;
    }

    /**
     * 达到最大输入字数监听接口
     */
    public interface OnMaxCharactersListener {
        /**
         * @param maxChars:最大输入字符数
         * @param alertText:超过最大输入字符数时的提示文字
         */
        void onMaxChars(int maxChars, @NonNull String alertText);
    }

    /**
     * 设置最大字符数监听事件
     */
    public void setMaxCharsListener(OnMaxCharactersListener listener) {
        maxListener = listener;
    }

    /**
     * 监听内容是否为空接口
     */
    public interface OnEmptyChangeListener {
        /**
         * @param isEmpty: 输入框是否为空
         */
        void onEmpty(boolean isEmpty);
    }

    /**
     * 设置内容是否为空的的监听事件
     */
    public void setEmptyChangeListener(OnEmptyChangeListener listener) {
        emptyListener = listener;
    }

    /**
     * 内容变化监听接口
     */
    public interface OnContentChangeListener {
        /**
         * @param content:文本内容
         * @param count:当前的文本长度
         */
        void onChanged(@NonNull CharSequence content, int count);
    }

    public void setOnContentChangeListener(OnContentChangeListener listener) {
        contentListener = listener;
        setTextWatcher();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        changeListener = null;
        beforeListener = null;
        afterListener = null;
        contentListener = null;
        maxListener = null;
        emptyListener = null;
    }
}
