package com.lindroid.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
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

class EasyEditText : AppCompatEditText {
    private var icClearId = R.drawable.ic_clear
    private var icShowPwd = R.drawable.ic_pwd_visible
    private var icHidePwd = R.drawable.ic_pwd_invisible

    /**
     * 超过最大输入字符数时的提示文字
     */
    var maxToastText = ""

    /**
     * 最大输入字符数，-1表示不做限制
     */
    var maxInputLength = -1
        set(value) {
            field = value
            if (field != -1) {
                setTextWatcher()
            }
        }

    /**
     * 最小输入字符数，小于0表示不做限制
     */
    var minInputLength = 0
        set(value) {
            field = value
            if (field > 0) {
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
    var isShowPwdButton = false
        set(value) {
            field = value
            initPwdButton()
            /* if (!isShowClearButton && field) {
             } else {
                 removeDrawable()
             }*/
        }

    /**
     * 是否显示明文
     */
    private var isShowContent = true


    private var textWatcher: TextWatcher? = null

    private var textListener: ((s: CharSequence) -> Unit)? = null

    private var maxListener: (() -> Unit)? = null

    private var emptyListener: ((isEmpty: Boolean) -> Unit)? = null

    private var isEmpty: Boolean = false

    constructor(context: Context?) : this(context, null)
    //默认style为R.attr.editTextStyle才能获取焦点
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, android.R.attr.editTextStyle)

    @SuppressLint("Recycle")
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val typedArray = context?.obtainStyledAttributes(attrs, R.styleable.EasyEditText, defStyleAttr, 0)
        typedArray?.let {
            maxToastText = it.getString(R.styleable.EasyEditText_maxToastText) ?: ""
            maxInputLength = it.getInt(R.styleable.EasyEditText_maxInputLength, maxInputLength)
            minInputLength = it.getInt(R.styleable.EasyEditText_minInputLength, minInputLength)
            isShowClearButton = it.getBoolean(R.styleable.EasyEditText_showClearButton, isShowClearButton)
            isShowPwdButton = it.getBoolean(R.styleable.EasyEditText_showPwdButton, isShowPwdButton)
            icClearId = it.getResourceId(R.styleable.EasyEditText_clearAllContentIcon, icClearId)
            icShowPwd = it.getResourceId(R.styleable.EasyEditText_showContentIcon, icShowPwd)
            icHidePwd = it.getResourceId(R.styleable.EasyEditText_hideContentIcon, icHidePwd)
            it.recycle()
        }
        isEmpty = text.toString().isEmpty()
//        init()
    }

    private fun init() {
        if (maxInputLength > 0 || minInputLength > 0 || isShowClearButton) {
            setTextWatcher()
        }
        initPwdButton()
    }

    private fun initPwdButton() {
        if (!isShowClearButton && isShowPwdButton) {
            isShowContent = when (inputType) {
                EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD -> true
                (EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_PASSWORD) //文本密码
                    , (EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD) //数字密码
                    , (EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD) //网址形式的密码
                -> false
                else -> true
            }
            setPwdDrawable(true)
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
                if (maxInputLength > 0 && s.toString().length > maxInputLength) {
                    setText(s.toString().substring(0, maxInputLength))
                    //光标移至最末端
                    setSelection(text!!.length)
                    if (maxToastText.isNotEmpty()) {

                    }
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
            length() > 0 && compoundDrawables[2] == null ->
                setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(context, icClearId), null)
            length() <= 0 && compoundDrawables[2] != null ->
                setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }
    }

    /**
     * 绘制设置密码是否可以见的图标
     */
    private fun setPwdDrawable(isPwdVisible: Boolean) {
        val ivPwd = when (isPwdVisible) {
            true -> ContextCompat.getDrawable(context, icShowPwd)
            false -> ContextCompat.getDrawable(context, icHidePwd)
        }
        setCompoundDrawablesWithIntrinsicBounds(null, null, ivPwd, null)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            val eventX = event.rawX.toInt()
            val eventY = event.rawY.toInt()
            val rect = Rect()
            getGlobalVisibleRect(rect)
            rect.left = rect.right - 100
            if (rect.contains(eventX, eventY)) {
                when {
                    isShowClearButton -> {
                        setText("")
                    }
                    !isShowClearButton && isShowPwdButton -> {
                        isShowContent = !isShowContent
                        transformationMethod = if (isShowContent) {
                            HideReturnsTransformationMethod.getInstance()
                        } else {
                            PasswordTransformationMethod.getInstance()
                        }
                        setPwdDrawable(isShowContent)
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

    /**
     * 设置文本改变监听事件
     */
    fun setOnTextChangeListener(listener: (content: CharSequence) -> Unit) = this.apply {
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