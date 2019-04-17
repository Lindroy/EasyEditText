package com.lindroid.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatEditText
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo

/**
 * @author Lin
 * @date 2019/4/11
 * @function 自定义EditText
 * @Description
 */
class EasyEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = android.R.attr.editTextStyle) : AppCompatEditText(context, attrs, defStyleAttr) {
    private val TAG = "EasyTag"
    /**
     * 一键清空按钮图片Id
     */
    private var clearIcon = R.drawable.ic_clear
    /**
     * 内容显示为明文的图片Id
     */
    private var displayIcon = R.drawable.ic_content_display
    /**
     * 内容显示为暗文的图片Id
     */
    private var hideIcon = R.drawable.ic_content_hide
    /**
     * 最大输入字符数，小于或等于0表示不做限制
     */
    /**
     * 设置最大输入字符数
     */
    var maxCharacters = -1
        set(maxCharacters) {
            if (this.maxCharacters == maxCharacters) {
                return
            }
            field = maxCharacters
            if (maxCharacters > 0) {
                setTextWatcher()
            }
        }
    /**
     * 超出最大字符输入数时的提示文字
     */
    var maxCharsAlert: String
    /**
     * 超出最大字符输入数时的提示文字，包含字数
     */
    var maxCharsAlertWithCount: String? = ""
    /**
     * 超出最大字符输入数时是否弹出Toast
     */
    var isShowMaxCharsAlertToast: Boolean = false

    /**
     * 是否显示清空按钮
     */
    /**
     * 设置是否显示一键清空按钮
     *
     * @param showClearButton:true时则显示
     */
    var isShowClearButton = false
        set(showClearButton) {
            if (this.isShowClearButton == showClearButton) {
                return
            }
            field = showClearButton
            if (this.isShowClearButton) {
                setTextWatcher()
            } else {
                removeDrawable()
            }
        }
    /**
     * 是否显示设置密码可见的按钮，前提是不显示删除按钮
     */
    private var isShowVisibilityToggle: Boolean = false
    /**
     * 是否显示明文
     */
    private var isDisplayContent = true
    /**
     * 是否是密码输入模式
     */
    private var isPwdType = false

    private var isEmpty = false

    private var textWatcher: TextWatcher? = null

    private var contentListener: OnContentChangeListener? = null

    private var beforeListener: BeforeTextChangeListener? = null

    private var changeListener: OnTextChangeListener? = null

    private var afterListener: AfterTextChangeListener? = null

    private var maxListener: OnMaxCharactersListener? = null

    private var emptyListener: OnEmptyChangeListener? = null

    /**
     * 获取右侧的图标
     */
    private val rightDrawable: Drawable?
        get() = compoundDrawables[2]

    init {
        checkInputType()
        val ta = context.obtainStyledAttributes(attrs, R.styleable.EasyEditText, defStyleAttr, 0)
        clearIcon = ta.getResourceId(R.styleable.EasyEditText_clearContentIcon, clearIcon)
        displayIcon = ta.getResourceId(R.styleable.EasyEditText_displayContentIcon, displayIcon)
        hideIcon = ta.getResourceId(R.styleable.EasyEditText_hideContentIcon, hideIcon)
        isShowClearButton = ta.getBoolean(R.styleable.EasyEditText_showClearButton, false)
        isShowVisibilityToggle = ta.getBoolean(R.styleable.EasyEditText_showVisibilityToggle, false)
        maxCharacters = ta.getInt(R.styleable.EasyEditText_maxCharacters, this.maxCharacters)
        maxCharsAlert = ta.getString(R.styleable.EasyEditText_maxCharsAlert)
        maxCharsAlertWithCount = ta.getString(R.styleable.EasyEditText_maxCharsAlertWithCount)
        Log.e(TAG, "maxCharsAlert=$maxCharsAlert")
        isShowMaxCharsAlertToast = ta.getBoolean(R.styleable.EasyEditText_showMaxCharsAlertToast, false)
        ta.recycle()
        if (text != null) {
            isEmpty = text!!.toString().isEmpty()
        }
        initContentToggle()

    }

    /**
     * 根据inputType判断是不是密码输入类型，从而决定显示明文或暗文
     */
    private fun checkInputType() {
        when (inputType) {
            EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD //可见的密码
            -> {
                isPwdType = true
                isDisplayContent = true
            }
            EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_PASSWORD//文本密码
                , EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD//数字密码
                , EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD//网址形式的密码
            -> {
                isPwdType = true
                isDisplayContent = false
            }
            else -> {
                isPwdType = false
                isDisplayContent = true
            }
        }
    }

    private fun initContentToggle() {
        if (isPwdType && !this.isShowClearButton && isShowVisibilityToggle) {
            setVisibilityDrawable()
        }
    }

    private fun setTextWatcher() {
        if (textWatcher != null) {
            return
        }
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (beforeListener != null) {
                    beforeListener!!.onBefore(s ?: "", start, count, after)
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val content = s ?: ""
                if (changeListener != null) {
                    changeListener!!.onChange(content, start, before, count)
                }
                //监听是否为空
                if (emptyListener != null) {
                    if (TextUtils.isEmpty(content)) {
                        isEmpty = true
                    } else {
                        if (!isEmpty) {
                            return
                        }
                        isEmpty = false
                    }
                    emptyListener!!.onEmpty(isEmpty)
                }

                //监听最大输入字符
                if (maxCharacters > 0 && content.length > maxCharacters) {
                    setText(content.toString().substring(0, maxCharacters))
                    //光标移至最末端
                    assert(text != null)
                    setSelection(text!!.length)
                    if (maxListener != null) {
                        maxListener!!.onMaxChars(maxCharacters)
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {
                if (afterListener != null) {
                    afterListener!!.onAfter(s)
                }
                //监听字符输入
                if (contentListener != null) {
                    contentListener!!.onChanged(s.toString(), s.toString().length)
                }
                if (isShowClearButton) {
                    setClearButton()
                }
            }
        }
        addTextChangedListener(textWatcher)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val eventX = event.x.toInt()
            val eventY = event.y.toInt()
            //设置响应点击的区域
            val rect = Rect()
            getLocalVisibleRect(rect)
            rect.left = rect.right - 100
            if (rect.contains(eventX, eventY)) {
                if (this.isShowClearButton) {
                    setText("")
                } else if (isShowVisibilityToggle) {
                    isDisplayContent = !isDisplayContent
                    if (isDisplayContent) {
                        transformationMethod = HideReturnsTransformationMethod.getInstance()
                    } else {
                        transformationMethod = PasswordTransformationMethod.getInstance()
                    }
                    setVisibilityDrawable()
                    setSelection(length())
                }
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * 设置一键清除按钮
     * 输入框内容为空时隐藏一键清空按钮
     */
    private fun setClearButton() {
        if (length() > 0 && !hasRightDrawable()) {
            setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(context, clearIcon), null)
        } else if (length() <= 0 && hasRightDrawable()) {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }
    }

    /**
     * 切换内容是否可见的图标
     */
    private fun setVisibilityDrawable() {
        val ivVisibility: Drawable?
        if (isDisplayContent) {
            ivVisibility = ContextCompat.getDrawable(context, displayIcon)
        } else {
            ivVisibility = ContextCompat.getDrawable(context, hideIcon)
        }
        setCompoundDrawablesWithIntrinsicBounds(null, null, ivVisibility, null)
    }

    /**
     * 右侧是否有图标
     */
    private fun hasRightDrawable(): Boolean {
        return rightDrawable != null
    }

    /**
     * 移除右侧所有图标
     */
    private fun removeDrawable() {
        setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
    }

    fun getClearIcon(): Int {
        return clearIcon
    }

    /**
     * 设置一键清空图标Id
     */
    fun setClearIcon(@DrawableRes clearIcon: Int) {
        this.clearIcon = clearIcon
        setTextWatcher()
    }

    /**
     * 获取明文按钮图标资源Id
     */
    fun getDisplayIcon(): Int {
        return displayIcon
    }

    /**
     * 设置明文按钮图标资源Id
     */
    fun setDisplayIcon(@DrawableRes displayIcon: Int) {
        this.displayIcon = displayIcon
        initContentToggle()
    }

    /**
     * 获取暗文按钮图标资源Id
     */
    fun getHideIcon(): Int {
        return hideIcon
    }

    /**
     * 设置暗文按钮图标资源Id
     */
    fun setHideIcon(@DrawableRes hideIcon: Int) {
        this.hideIcon = hideIcon
        initContentToggle()
    }

    fun isShowVisibilityToggle(): Boolean {
        return isShowVisibilityToggle
    }

    /**
     * 是否显示明暗文切换按钮
     * 需要满足两个前提：
     * 1、输入类型为密码类型；
     * 2、没有显示一键清空按钮。
     *
     * @param showVisibilityToggle:true时显示按钮
     */
    fun setShowVisibilityToggle(showVisibilityToggle: Boolean) {
        if (isShowVisibilityToggle == showVisibilityToggle || this.isShowClearButton) {
            return
        }
        isShowVisibilityToggle = showVisibilityToggle
        if (isShowVisibilityToggle && !hasRightDrawable()) {
            setVisibilityDrawable()
        } else {
            removeDrawable()
        }
    }


    /**
     * 文本改变前的监听接口
     */
    interface BeforeTextChangeListener {
        /**
         * @param s:改变前的字符
         * @param start:即将修改的位置
         * @param count:即将被修改的文字长度，新增则为0
         * @param after:被修改的文字长度，删除则为0
         */
        fun onBefore(s: CharSequence, start: Int, count: Int, after: Int)
    }

    /**
     * 设置文本改变前的监听事件
     */
    fun setBeforeTextChangeListener(listener: BeforeTextChangeListener) {
        beforeListener = listener
        setTextWatcher()
    }

    /**
     * 文本改变时的监听接口
     */
    interface OnTextChangeListener {
        /**
         * @param s:文本内容
         * @param start:有变动的字符序号
         * @param before:变动的字符长度，新增的话则为0
         * @param count:添加的字符长度，删除的话则为0
         */
        fun onChange(s: CharSequence, start: Int, before: Int, count: Int)
    }

    /**
     * 设置文本改变监听事件
     */
    fun setOnTextChangeListener(listener: OnTextChangeListener) {
        changeListener = listener
        setTextWatcher()
    }

    /**
     * 文本改变后的监听接口
     */
    interface AfterTextChangeListener {
        fun onAfter(s: Editable)
    }

    /**
     * 设置文本改变后的监听
     */
    fun setAfterTextChangeListener(listener: AfterTextChangeListener) {
        afterListener = listener
    }

    /**
     * 达到最大输入字数监听接口
     */
    interface OnMaxCharactersListener {
        fun onMaxChars(maxChars: Int)
    }

    /**
     * 设置最大字符数监听事件
     */
    fun setMaxCharsListener(listener: OnMaxCharactersListener) {
        maxListener = listener
    }

    /**
     * 监听内容是否为空接口
     */
    interface OnEmptyChangeListener {
        /**
         * @param isEmpty: 输入框是否为空
         */
        fun onEmpty(isEmpty: Boolean)
    }

    /**
     * 设置内容是否为空的的监听事件
     */
    fun setEmptyChangeListener(listener: OnEmptyChangeListener) {
        emptyListener = listener
    }

    /**
     * 内容变化监听接口
     */
    interface OnContentChangeListener {
        /**
         * @param content:文本内容
         * @param count:当前的文本长度
         */
        fun onChanged(content: CharSequence, count: Int)
    }

    fun setOnContentChangeListener(listener: OnContentChangeListener) {
        contentListener = listener
        setTextWatcher()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        changeListener = null
        beforeListener = null
        afterListener = null
        contentListener = null
        maxListener = null
        emptyListener = null
    }
}
