package com.lindroid.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatEditText
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo

/**
 * @author Lin
 * @date 2019/3/30
 * @function 自定义EditText
 * @Description
 */
const val TAG = "Tag"

class EasyEditText : AppCompatEditText {

    var clearIcon: Int = R.drawable.ic_clear
        set(@DrawableRes value) {
            field = value
            setTextWatcher()
        }

    var displayIcon = R.drawable.ic_content_display
        set(@DrawableRes value) {
            field = value
            initContentToggle()
        }

    var hideIcon = R.drawable.ic_content_hide
        set(@DrawableRes value) {
            field = value
            initContentToggle()
        }


    /**
     * 超过最大输入字符数时的提示文字
     */
    var maxToastText = ""

    /**
     * 最大输入字符数，-1表示不做限制
     */
    var maxCharacters = -1
        private set(value) {
            field = value
            if (field != -1) {
                setTextWatcher()
            }
        }

    /**
     * 是否显示清空按钮
     */
    var isShowClearButton = false
        set(value) {
            field = value
            if (field) {
                setTextWatcher()
            } else {
                removeDrawable()
            }
        }

    /**
     * 是否显示设置密码可见的按钮，前提是不显示删除按钮
     */
    var isShowVisibilityToggle = false
        set(value) {
            field = value
            if (!isShowClearButton) {
                if (field && rightDrawable == null) {
                    initContentToggle()
                } else if (!field) {
                    removeDrawable()
                }
            }
        }

    /**
     * 是否显示明文
     */
    private var isDisplayContent = true

    private var isPwdType = false

    private var textWatcher: TextWatcher? = null

    private var textListener: ((s: CharSequence) -> Unit)? = null

    private var maxListener: (() -> Unit)? = null

    private var emptyListener: ((isEmpty: Boolean) -> Unit)? = null

    private var isEmpty: Boolean = false

    /**
     * 右侧图标
     */
    private val rightDrawable: Drawable?
        get() = compoundDrawables[2]

    private var isInit = false

    constructor(context: Context?) : this(context, null)
    //默认style为R.attr.editTextStyle才能获取焦点
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, android.R.attr.editTextStyle)

    @SuppressLint("Recycle")
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initInputType()
        val typedArray = context?.obtainStyledAttributes(attrs, R.styleable.EasyEditText, defStyleAttr, 0)
        typedArray?.let {
            //            maxToastText = it.getString(R.styleable.EasyEditText_maxToastText) ?: ""
            isShowVisibilityToggle =
                    it.getBoolean(R.styleable.EasyEditText_showVisibilityToggle, isShowVisibilityToggle)
            isShowClearButton = it.getBoolean(R.styleable.EasyEditText_showClearButton, isShowClearButton)
            maxCharacters = it.getInt(R.styleable.EasyEditText_maxCharacters, maxCharacters)
            clearIcon = it.getResourceId(R.styleable.EasyEditText_clearContentIcon, clearIcon)
            displayIcon = it.getResourceId(R.styleable.EasyEditText_displayContentIcon, displayIcon)
            hideIcon = it.getResourceId(R.styleable.EasyEditText_hideContentIcon, hideIcon)

            it.recycle()
        }
        isInit = true
        isEmpty = text.toString().isEmpty()
        initContentToggle()
    }

    private fun initInputType() {
        when (inputType) {
            EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD -> {
                isPwdType = true
                isDisplayContent = true
            }
            (EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_PASSWORD) //文本密码
                , (EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD) //数字密码
                , (EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD) //网址形式的密码
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


    /**
     * 初始化明暗文切换按钮
     */
    private fun initContentToggle() {
        if (isInit && isPwdType && !isShowClearButton && isShowVisibilityToggle) {
            setPwdDrawable()
        }
    }

    private fun setTextWatcher() {
        if (textWatcher != null) {
            return
        }
        textWatcher = object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                if (isShowClearButton) {
                    setClearDrawable()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //监听字符输入
                textListener?.invoke(s ?: "")

                //监听是否为空
                when (s != null && s.isEmpty()) {
                    true -> {
                        isEmpty = true
                        emptyListener?.invoke(true)
                    }
                    false -> {
                        if (!isEmpty) {
                            return
                        }
                        isEmpty = false
                        emptyListener?.invoke(false)
                    }
                }
                //监听最大输入字符
                if (maxCharacters > 0 && s.toString().length > maxCharacters) {
                    setText(s.toString().substring(0, maxCharacters))
                    //光标移至最末端
                    setSelection(text!!.length)
                    maxListener?.invoke()
                }
            }
        }
        addTextChangedListener(textWatcher)
    }

    /**
     * 绘制删除图片
     */
    private fun setClearDrawable() {
        when {
            length() > 0 && rightDrawable == null ->
                setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(context, clearIcon), null)
            length() <= 0 && rightDrawable != null ->
                setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }
    }

    /**
     * 绘制设置密码是否可见的图标
     */
    private fun setPwdDrawable() {
        val ivPwd = when (isDisplayContent) {
            true -> ContextCompat.getDrawable(context, displayIcon)
            false -> ContextCompat.getDrawable(context, hideIcon)
        }
        setCompoundDrawablesWithIntrinsicBounds(null, null, ivPwd, null)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            val eventX = event.x.toInt()
            val eventY = event.y.toInt()
            val rect = Rect()
            getLocalVisibleRect(rect)
            rect.left = rect.right - 100
            if (rect.contains(eventX, eventY)) {
                when {
                    isShowClearButton -> {
                        setText("")
                    }
                    !isShowClearButton && isShowVisibilityToggle -> {
                        isDisplayContent = !isDisplayContent
                        transformationMethod = if (isDisplayContent) {
                            HideReturnsTransformationMethod.getInstance()
                        } else {
                            PasswordTransformationMethod.getInstance()
                        }
                        setPwdDrawable()
                        setSelection(length())
                    }
                }
            }

        }
        return super.onTouchEvent(event)
    }

    /**
     * 移除所有的图标
     */
    private fun removeDrawable() {
        setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        textListener = null
        maxListener = null
        emptyListener = null
    }

    /**
     * 设置文本改变监听事件
     */
    fun setTextChangeListener(listener: (content: CharSequence) -> Unit) = this.apply {
        textListener = listener
    }

    /**
     * 设置超过最长限定字符监听事件
     */
    fun setMaxLengthListener(listener: () -> Unit) = this.apply {
        maxListener = listener
    }

    /**
     * 设置输入框为空的监听
     */
    fun setEmptyChangeListener(listener: (isEmpty: Boolean) -> Unit) {
        emptyListener = listener
    }

}