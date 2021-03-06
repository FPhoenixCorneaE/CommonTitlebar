package com.fphoenixcorneae.toolbar

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.fphoenixcorneae.ext.*
import com.fphoenixcorneae.ext.view.isVisible
import com.fphoenixcorneae.ext.view.setTintColor
import com.fphoenixcorneae.util.ViewUtil
import com.fphoenixcorneae.util.statusbar.StatusBarUtil
import kotlin.math.max

/**
 * @desc 通用标题栏
 */
class CommonToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : RelativeLayout(context, attrs, defStyleAttr, defStyleRes), View.OnClickListener {
    private var viewStatusBarFill: View? = null // 状态栏填充视图

    /**
     * 获取标题栏底部横线
     */
    var bottomLine: View? = null // 分隔线视图
        private set
    var viewBottomShadow: View? = null // 底部阴影
        private set
    private var rlMain: RelativeLayout? = null // 主视图

    /**
     * 获取标题栏左边TextView，对应leftType = textView
     */
    var leftTextView: TextView? = null // 左边TextView
        private set

    /**
     * 获取标题栏左边ImageButton，对应leftType = imageButton
     */
    var leftImageButton: ImageButton? = null // 左边ImageButton
        private set

    /**
     * 获取左边自定义布局
     */
    var leftCustomView: View? = null
        private set

    /**
     * 获取标题栏右边TextView，对应rightType = textView
     */
    var rightTextView: TextView? = null // 右边TextView
        private set

    /**
     * 获取标题栏右边ImageButton，对应rightType = imageButton
     */
    var rightImageButton: ImageButton? = null // 右边ImageButton
        private set

    /**
     * 获取右边自定义布局
     */
    var rightCustomView: View? = null
        private set
    var centerLayout: LinearLayout? = null
        private set

    /**
     * 获取标题栏中间TextView，对应centerType = textView
     */
    var centerTextView: TextView? = null // 标题栏文字
        private set
    var centerSubTextView: TextView? = null // 副标题栏文字
        private set
    var progressCenter: ProgressBar? = null // 中间进度条,默认隐藏
        private set

    /**
     * 获取搜索框布局，对应centerType = searchView
     */
    var centerSearchView: RelativeLayout? = null // 中间搜索框布局
        private set

    /**
     * 获取搜索框内部输入框，对应centerType = searchView
     */
    var centerSearchEditText: EditText? = null
        private set
    var centerSearchLeftImageView: ImageView? = null
        private set

    /**
     * 获取搜索框右边图标ImageView，对应centerType = searchView
     */
    var centerSearchRightImageView: ImageView? = null
        private set

    /**
     * 获取中间自定义布局视图
     */
    var centerCustomView: View? = null // 中间自定义视图
        private set
    private var fillStatusBar: Boolean = true // 是否撑起状态栏, true时,标题栏浸入状态栏 = false
    var toolbarColor: Int = Color.WHITE // 标题栏背景颜色 = 0
        set(value) {
            field = value
            rlMain?.setBackgroundColor(value)
        }
    var toolbarHeight: Int = 0 // 标题栏高度 = 0
        set(value) {
            field = value
            rlMain?.layoutParams?.height = if (showBottomLine) {
                toolbarHeight - max(1, context.dp2px(0.4f))
            } else {
                toolbarHeight
            }
        }
    var statusBarColor: Int = Color.WHITE // 状态栏颜色 = 0
        set(value) {
            field = value
            viewStatusBarFill?.setBackgroundColor(value)
        }
    var statusBarMode: Int = 0 // 状态栏图片模式：0-暗色 非0-亮色
        set(value) {
            field = value
            val window = window ?: return
            if (value == 0) {
                StatusBarUtil.setDarkMode(window)
            } else {
                StatusBarUtil.setLightMode(window)
            }
        }
    var showBottomLine: Boolean = true // 是否显示底部分割线 = false
        set(value) {
            field = value
            bottomLine?.isVisible = value
        }
    var bottomLineColor: Int = 0 // 分割线颜色 = 0
        set(value) {
            field = value
            bottomLine?.setBackgroundColor(value)
        }
    var bottomShadowHeight: Float = 0f // 底部阴影高度 = 0f
        set(value) {
            field = value
            if (value > 0f) {
                viewBottomShadow?.layoutParams?.height = value.toInt()
            }
        }
    var leftType = TYPE_LEFT_NONE
        // 左边视图类型 = 0
        set(value) {
            field = value
            initMainLeftViews(context)
        }
    private var leftText: String? = null // 左边TextView文字
    private var leftTextColor = NO_ID // 左边TextView颜色 = NO_ID
    private var leftTextSize = 0f // 左边TextView文字大小 = 0f
    private var leftTextFontFamily = 0 // 左边TextView文字字体 = 0
    private var leftTextBold = false // 左边TextView文字是否加粗 = false
    private var leftDrawable = 0 // 左边TextView drawableLeft资源 = 0
    private var leftDrawablePadding = 5f // 左边TextView drawablePadding = 0f
    private var leftImageResource = R.drawable.common_toolbar_reback_selector // 左边图片资源 = 0
    private var leftImageTint = Color.BLACK
    private var leftCustomViewRes = 0 // 左边自定义视图布局资源 = 0
    var rightType = TYPE_RIGHT_NONE // 右边视图类型 = 0
        set(value) {
            field = value
            initMainRightViews(context)
        }
    private var rightText: String? = null // 右边TextView文字
    private var rightTextColor = NO_ID // 右边TextView颜色 = NO_ID
    private var rightTextSize = 0f // 右边TextView文字大小 = 0f
    private var rightTextFontFamily = 0 // 右边TextView文字字体 = 0
    private var rightTextBold = false // 右边TextView文字是否加粗 = false
    private var rightImageResource = 0 // 右边图片资源 = 0
    private var rightImageTint = Color.BLACK
    private var rightCustomViewRes = 0 // 右边自定义视图布局资源 = 0
    var centerType = TYPE_CENTER_NONE // 中间视图类型 = 0
        set(value) {
            field = value
            initMainCenterViews(context)
        }
    private var centerText: String? = null // 中间TextView文字
    private var centerTextColor = Color.parseColor("#333333") // 中间TextView字体颜色 = 0
    private var centerTextSize = 0f // 中间TextView字体大小 = 0f
    private var centerTextFontFamily = 0 // 中间TextView文字字体 = 0
    private var centerTextBold = true // 中间TextView文字是否加粗 = true
    private var centerTextMarquee = true // 中间TextView字体是否显示跑马灯效果 = false
    private var centerSubText: String? = null // 中间subTextView文字
    private var centerSubTextColor = Color.parseColor("#666666") // 中间subTextView字体颜色 = 0
    private var centerSubTextSize = 0f // 中间subTextView字体大小 = 0f
    private var centerSubTextFontFamily = 0 // 中间subTextView文字字体 = 0
    private var centerSubTextBold = false // 中间subTextView文字是否加粗 = true

    /**
     * 搜索输入框:是否可输入、提示文字、提示文字颜色、文字颜色、文字大小、背景图片、
     *           右边按钮类型  0: voice 1: delete = 0
     */
    private var centerSearchEditable = true
    private var centerSearchHintText: String? = resources.getString(R.string.toolbar_search_hint)
    private var centerSearchHintTextColor = Color.parseColor("#999999")
    private var centerSearchTextColor = Color.parseColor("#666666")
    private var centerSearchTextSize = 0f
    private var centerSearchIconTint = Color.WHITE
    private var centerSearchBgResource = R.drawable.common_toolbar_search_gray_shape
    private var centerSearchRightType = TYPE_CENTER_SEARCH_RIGHT_VOICE
    private var centerCustomViewRes = 0 // 中间自定义布局资源 = 0
    private var PADDING_5 = dp2px(5f)
    private var PADDING_16 = dp2px(16f)

    /**
     * 点击事件
     * @param v
     * @param action [MotionAction], 如ACTION_LEFT_TEXT
     * @param extra  中间为搜索框时,如果可输入,点击键盘的搜索按钮,会返回输入关键词
     */
    var onToolbarClickListener: ((v: View, action: Int, extra: String?) -> Unit)? = null

    /**
     * 标题栏中间布局双击事件监听
     */
    var onToolbarCenterDoubleClickListener: ((v: View) -> Unit)? = null

    @SuppressLint("ObsoleteSdkInt")
    private fun loadAttributes(
        context: Context,
        attrs: AttributeSet?,
    ) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.CommonToolbar)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // notice 未引入沉浸式标题栏之前,隐藏标题栏撑起布局
            fillStatusBar = array.getBoolean(R.styleable.CommonToolbar_fillStatusBar, true)
        }
        toolbarColor = array.getColor(R.styleable.CommonToolbar_toolbarColor, Color.WHITE)
        toolbarHeight = array.getDimension(
            R.styleable.CommonToolbar_toolbarHeight,
            context.dpToPx(44f)
        ).toInt()
        statusBarColor =
            array.getColor(R.styleable.CommonToolbar_statusBarColor, Color.WHITE)
        statusBarMode = array.getInt(R.styleable.CommonToolbar_statusBarMode, 0)
        showBottomLine = array.getBoolean(R.styleable.CommonToolbar_showBottomLine, true)
        bottomLineColor = array.getColor(
            R.styleable.CommonToolbar_bottomLineColor,
            Color.parseColor("#eeeeee")
        )
        bottomShadowHeight = array.getDimension(
            R.styleable.CommonToolbar_bottomShadowHeight,
            0f
        )
        leftType = array.getInt(
            R.styleable.CommonToolbar_leftType,
            TYPE_LEFT_NONE
        )
        when (leftType) {
            TYPE_LEFT_TEXT_VIEW -> {
                leftText = array.getString(R.styleable.CommonToolbar_leftText)
                leftTextColor = array.getColor(
                    R.styleable.CommonToolbar_leftTextColor,
                    NO_ID
                )
                leftTextSize = array.getDimension(
                    R.styleable.CommonToolbar_leftTextSize,
                    context.spToPx(16f)
                )
                leftTextFontFamily = array.getResourceId(
                    R.styleable.CommonToolbar_leftTextFontFamily,
                    0
                )
                leftTextBold = array.getBoolean(
                    R.styleable.CommonToolbar_leftTextBold,
                    false
                )
                leftDrawable = array.getResourceId(R.styleable.CommonToolbar_leftDrawable, 0)
                leftDrawablePadding =
                    array.getDimension(R.styleable.CommonToolbar_leftDrawablePadding, 5f)
            }
            TYPE_LEFT_IMAGE_BUTTON -> {
                leftImageResource = array.getResourceId(
                    R.styleable.CommonToolbar_leftImageResource,
                    R.drawable.common_toolbar_reback_selector
                )
                leftImageTint = array.getColor(
                    R.styleable.CommonToolbar_leftImageTint,
                    Color.BLACK
                )
            }
            TYPE_LEFT_CUSTOM_VIEW -> {
                leftCustomViewRes =
                    array.getResourceId(R.styleable.CommonToolbar_leftCustomView, 0)
            }
        }
        rightType = array.getInt(
            R.styleable.CommonToolbar_rightType,
            TYPE_RIGHT_NONE
        )
        when (rightType) {
            TYPE_RIGHT_TEXT_VIEW -> {
                rightText = array.getString(R.styleable.CommonToolbar_rightText)
                rightTextColor = array.getColor(
                    R.styleable.CommonToolbar_rightTextColor,
                    NO_ID
                )
                rightTextSize = array.getDimension(
                    R.styleable.CommonToolbar_rightTextSize,
                    context.spToPx(16f)
                )
                rightTextFontFamily = array.getResourceId(
                    R.styleable.CommonToolbar_rightTextFontFamily,
                    0
                )
                rightTextBold = array.getBoolean(
                    R.styleable.CommonToolbar_rightTextBold,
                    false
                )
            }
            TYPE_RIGHT_IMAGE_BUTTON -> {
                rightImageResource =
                    array.getResourceId(R.styleable.CommonToolbar_rightImageResource, 0)
                rightImageTint = array.getColor(
                    R.styleable.CommonToolbar_rightImageTint,
                    Color.BLACK
                )
            }
            TYPE_RIGHT_CUSTOM_VIEW -> {
                rightCustomViewRes =
                    array.getResourceId(R.styleable.CommonToolbar_rightCustomView, 0)
            }
        }
        centerType = array.getInt(
            R.styleable.CommonToolbar_centerType,
            TYPE_CENTER_NONE
        )
        when (centerType) {
            TYPE_CENTER_TEXT_VIEW -> {
                centerText = array.getString(R.styleable.CommonToolbar_centerText)
                // 如果当前上下文对象是Activity，就获取Activity的标题
                if (centerText.isNullOrBlank() && getContext() is Activity) {
                    // 获取清单文件中的 android:label 属性值
                    val label = (getContext() as Activity).title
                    if (label.isNullOrBlank().not()) {
                        try {
                            val packageManager = getContext().packageManager
                            val packageInfo =
                                packageManager.getPackageInfo(getContext().packageName, 0)
                            // 如果当前 Activity 没有设置 android:label 属性，则默认会返回 APP 名称，则需要过滤掉
                            if (label.toString() != packageInfo.applicationInfo.loadLabel(
                                    packageManager
                                ).toString()
                            ) {
                                // 设置标题
                                centerText = label.toString()
                            }
                        } catch (ignored: PackageManager.NameNotFoundException) {
                        }
                    }
                }
                centerTextColor = array.getColor(
                    R.styleable.CommonToolbar_centerTextColor,
                    Color.parseColor("#333333")
                )
                centerTextSize = array.getDimension(
                    R.styleable.CommonToolbar_centerTextSize,
                    context.spToPx(18f)
                )
                centerTextFontFamily = array.getResourceId(
                    R.styleable.CommonToolbar_centerTextFontFamily,
                    0
                )
                centerTextBold = array.getBoolean(
                    R.styleable.CommonToolbar_centerTextBold,
                    true
                )
                centerTextMarquee =
                    array.getBoolean(R.styleable.CommonToolbar_centerTextMarquee, true)
                centerSubText = array.getString(R.styleable.CommonToolbar_centerSubText)
                centerSubTextColor = array.getColor(
                    R.styleable.CommonToolbar_centerSubTextColor,
                    Color.parseColor("#666666")
                )
                centerSubTextSize = array.getDimension(
                    R.styleable.CommonToolbar_centerSubTextSize,
                    context.spToPx(11f)
                )
                centerSubTextFontFamily = array.getResourceId(
                    R.styleable.CommonToolbar_centerSubTextFontFamily,
                    0
                )
                centerSubTextBold = array.getBoolean(
                    R.styleable.CommonToolbar_centerSubTextBold,
                    false
                )
            }
            TYPE_CENTER_SEARCH_VIEW -> {
                centerSearchEditable =
                    array.getBoolean(R.styleable.CommonToolbar_centerSearchEditable, true)
                centerSearchHintText =
                    array.getString(R.styleable.CommonToolbar_centerSearchHintText)
                centerSearchHintTextColor =
                    array.getColor(
                        R.styleable.CommonToolbar_centerSearchHintTextColor,
                        Color.parseColor("#999999")
                    )
                centerSearchTextColor =
                    array.getColor(
                        R.styleable.CommonToolbar_centerSearchTextColor,
                        Color.parseColor("#666666")
                    )
                centerSearchTextSize =
                    array.getDimension(
                        R.styleable.CommonToolbar_centerSearchTextSize,
                        context.spToPx(14f)
                    )
                centerSearchIconTint = array.getColor(
                    R.styleable.CommonToolbar_centerSearchIconTint,
                    Color.WHITE
                )
                centerSearchBgResource = array.getResourceId(
                    R.styleable.CommonToolbar_centerSearchBg,
                    R.drawable.common_toolbar_search_gray_shape
                )
                centerSearchRightType = array.getInt(
                    R.styleable.CommonToolbar_centerSearchRightType,
                    TYPE_CENTER_SEARCH_RIGHT_VOICE
                )
            }
            TYPE_CENTER_CUSTOM_VIEW -> {
                centerCustomViewRes =
                    array.getResourceId(R.styleable.CommonToolbar_centerCustomView, 0)
            }
        }
        array.recycle()
    }

    /**
     * 初始化全局视图
     *
     * @param context 上下文
     */
    private fun initGlobalViews(context: Context) {
        val globalParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        layoutParams = globalParams
        val transparentStatusBar = StatusBarUtil.supportTransparentStatusBar()
        // 构建标题栏填充视图
        if (fillStatusBar && transparentStatusBar) {
            val statusBarHeight = StatusBarUtil.getStatusBarHeight(context)
            viewStatusBarFill = View(context)
            viewStatusBarFill!!.id = ViewUtil.generateViewId()
            viewStatusBarFill!!.setBackgroundColor(statusBarColor)
            val statusBarParams = LayoutParams(MATCH_PARENT, statusBarHeight)
            statusBarParams.addRule(ALIGN_PARENT_TOP)
            addView(viewStatusBarFill, statusBarParams)
        }
        // 构建主视图
        rlMain = RelativeLayout(context)
        rlMain!!.id = ViewUtil.generateViewId()
        rlMain!!.setBackgroundColor(toolbarColor)
        val mainParams = LayoutParams(MATCH_PARENT, toolbarHeight)
        if (fillStatusBar && transparentStatusBar) {
            mainParams.addRule(BELOW, viewStatusBarFill!!.id)
        } else {
            mainParams.addRule(ALIGN_PARENT_TOP)
        }
        // 计算主布局高度
        mainParams.height = if (showBottomLine) {
            toolbarHeight - max(1, context.dp2px(0.4f))
        } else {
            toolbarHeight
        }
        addView(rlMain, mainParams)
        // 构建分割线视图
        if (showBottomLine) {
            // 已设置显示标题栏分隔线,5.0以下机型,显示分隔线
            bottomLine = View(context)
            bottomLine!!.setBackgroundColor(bottomLineColor)
            val bottomLineParams = LayoutParams(
                MATCH_PARENT,
                max(1, context.dp2px(0.4f))
            )
            bottomLineParams.addRule(BELOW, rlMain!!.id)
            addView(bottomLine, bottomLineParams)
        }
        if (bottomShadowHeight >= 0f) {
            viewBottomShadow = View(context)
            viewBottomShadow!!.setBackgroundResource(R.drawable.common_toolbar_bottom_shadow)
            val bottomShadowParams = LayoutParams(
                MATCH_PARENT,
                context.dp2px(bottomShadowHeight)
            )
            bottomShadowParams.addRule(BELOW, rlMain!!.id)
            addView(viewBottomShadow, bottomShadowParams)
        }
    }

    /**
     * 初始化主视图左边部分
     * -- add: adaptive RTL
     *
     * @param context 上下文
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun initMainLeftViews(context: Context) {
        val leftInnerParams = LayoutParams(WRAP_CONTENT, MATCH_PARENT)
        leftInnerParams.addRule(ALIGN_PARENT_START)
        leftInnerParams.addRule(CENTER_VERTICAL)
        when (leftType) {
            TYPE_LEFT_TEXT_VIEW -> { // 初始化左边TextView
                leftTextView = TextView(context)
                leftTextView!!.id = ViewUtil.generateViewId()
                leftTextView!!.text = leftText
                when {
                    leftTextColor != NO_ID -> {
                        leftTextView!!.setTextColor(leftTextColor)
                    }
                    else -> {
                        leftTextView!!.setTextColor(
                            ContextCompat.getColorStateList(
                                context,
                                R.color.common_toolbar_text_selector
                            )
                        )
                    }
                }
                leftTextView!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, leftTextSize)
                leftTextView!!.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                leftTextView!!.isSingleLine = true
                // 字体
                if (!isInEditMode && leftTextFontFamily != 0) {
                    leftTextView!!.typeface = ResourcesCompat.getFont(context, leftTextFontFamily)
                }
                // 字体加粗
                leftTextView!!.paint.isFakeBoldText = leftTextBold
                leftTextView!!.setOnClickListener(this)
                // 设置DrawableLeft及DrawablePadding
                if (leftDrawable != 0) {
                    leftTextView!!.compoundDrawablePadding = leftDrawablePadding.toInt()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        leftTextView!!.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            leftDrawable,
                            0,
                            0,
                            0
                        )
                    } else {
                        leftTextView!!.setCompoundDrawablesWithIntrinsicBounds(
                            leftDrawable,
                            0,
                            0,
                            0
                        )
                    }
                }
                leftTextView!!.setPadding(PADDING_16, 0, PADDING_16, 0)
                rlMain!!.addView(leftTextView, leftInnerParams)
            }
            TYPE_LEFT_IMAGE_BUTTON -> { // 初始化左边ImageButton
                leftImageButton = ImageButton(context)
                leftImageButton!!.id = ViewUtil.generateViewId()
                leftImageButton!!.setBackgroundColor(Color.TRANSPARENT)
                leftImageButton!!.setImageResource(leftImageResource)
                leftImageButton!!.setTintColor(leftImageTint)
                leftImageButton!!.setPadding(PADDING_16, 0, PADDING_16, 0)
                leftImageButton!!.setOnClickListener(this)
                rlMain!!.addView(leftImageButton, leftInnerParams)
            }
            TYPE_LEFT_CUSTOM_VIEW -> { // 初始化自定义View
                leftCustomView =
                    LayoutInflater.from(context).inflate(leftCustomViewRes, rlMain, false)
                leftCustomView?.apply {
                    if (id == View.NO_ID) {
                        id = ViewUtil.generateViewId()
                    }
                }
                rlMain!!.addView(leftCustomView, leftInnerParams)
            }
        }
    }

    /**
     * 初始化主视图右边部分
     * -- add: adaptive RTL
     *
     * @param context 上下文
     */
    private fun initMainRightViews(context: Context) {
        val rightInnerParams = LayoutParams(WRAP_CONTENT, MATCH_PARENT)
        rightInnerParams.addRule(ALIGN_PARENT_END)
        rightInnerParams.addRule(CENTER_VERTICAL)
        when (rightType) {
            TYPE_RIGHT_TEXT_VIEW -> { // 初始化右边TextView
                rightTextView = TextView(context)
                rightTextView!!.id = ViewUtil.generateViewId()
                rightTextView!!.text = rightText
                when {
                    rightTextColor != NO_ID -> {
                        rightTextView!!.setTextColor(rightTextColor)
                    }
                    else -> {
                        rightTextView!!.setTextColor(
                            ContextCompat.getColorStateList(
                                context,
                                R.color.common_toolbar_text_selector
                            )
                        )
                    }
                }
                rightTextView!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, rightTextSize)
                rightTextView!!.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                rightTextView!!.isSingleLine = true
                // 字体
                if (!isInEditMode && rightTextFontFamily != 0) {
                    rightTextView!!.typeface = ResourcesCompat.getFont(context, rightTextFontFamily)
                }
                // 字体加粗
                rightTextView!!.paint.isFakeBoldText = rightTextBold
                rightTextView!!.setPadding(PADDING_16, 0, PADDING_16, 0)
                rightTextView!!.setOnClickListener(this)
                rlMain!!.addView(rightTextView, rightInnerParams)
            }
            TYPE_RIGHT_IMAGE_BUTTON -> { // 初始化右边ImageBtn
                rightImageButton = ImageButton(context)
                rightImageButton!!.id = ViewUtil.generateViewId()
                rightImageButton!!.setImageResource(rightImageResource)
                rightImageButton!!.setTintColor(rightImageTint)
                rightImageButton!!.setBackgroundColor(Color.TRANSPARENT)
                rightImageButton!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
                rightImageButton!!.setPadding(PADDING_16, 0, PADDING_16, 0)
                rightImageButton!!.setOnClickListener(this)
                rlMain!!.addView(rightImageButton, rightInnerParams)
            }
            TYPE_RIGHT_CUSTOM_VIEW -> { // 初始化自定义view
                rightCustomView =
                    LayoutInflater.from(context).inflate(rightCustomViewRes, rlMain, false)
                rightCustomView?.apply {
                    if (id == View.NO_ID) {
                        id = ViewUtil.generateViewId()
                    }
                }
                rlMain!!.addView(rightCustomView, rightInnerParams)
            }
        }
    }

    /**
     * 初始化主视图中间部分
     *
     * @param context 上下文
     */
    private fun initMainCenterViews(context: Context) {
        when (centerType) {
            TYPE_CENTER_TEXT_VIEW -> {
                // 初始化中间子布局
                centerLayout = LinearLayout(context)
                centerLayout!!.id = ViewUtil.generateViewId()
                centerLayout!!.gravity = Gravity.CENTER
                centerLayout!!.orientation = LinearLayout.VERTICAL
                centerLayout!!.setOnClickListener(this)
                val centerParams = LayoutParams(WRAP_CONTENT, MATCH_PARENT)
                centerParams.marginStart = PADDING_16
                centerParams.marginEnd = PADDING_16
                centerParams.addRule(CENTER_IN_PARENT)
                rlMain!!.addView(centerLayout, centerParams)
                // 初始化标题栏TextView
                centerTextView = TextView(context)
                centerTextView!!.text = centerText
                centerTextView!!.setTextColor(centerTextColor)
                centerTextView!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, centerTextSize)
                centerTextView!!.gravity = Gravity.CENTER
                centerTextView!!.isSingleLine = true
                // 字体
                if (!isInEditMode && centerTextFontFamily != 0) {
                    centerTextView!!.typeface =
                        ResourcesCompat.getFont(context, centerTextFontFamily)
                }
                // 字体加粗
                centerTextView!!.paint.isFakeBoldText = centerTextBold
                // 设置跑马灯效果
                centerTextView!!.maxWidth = (context.screenWidth * 3 / 5.0).toInt()
                if (centerTextMarquee) {
                    centerTextView!!.ellipsize = TextUtils.TruncateAt.MARQUEE
                    centerTextView!!.marqueeRepeatLimit = -1
                    centerTextView!!.requestFocus()
                    centerTextView!!.isSelected = true
                }
                val centerTextParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                centerLayout!!.addView(centerTextView, centerTextParams)
                // 初始化进度条, 显示于标题栏左边
                progressCenter = ProgressBar(context)
                progressCenter!!.indeterminateDrawable =
                    ContextCompat.getDrawable(context, R.drawable.common_toolbar_progress_draw)
                progressCenter!!.visibility = View.GONE
                val progressWidth = context.dp2px(18f)
                val progressParams = LayoutParams(progressWidth, progressWidth)
                progressParams.addRule(CENTER_VERTICAL)
                progressParams.addRule(START_OF, centerLayout!!.id)
                rlMain!!.addView(progressCenter, progressParams)
                // 初始化副标题栏
                centerSubTextView = TextView(context)
                centerSubTextView!!.text = centerSubText
                centerSubTextView!!.setTextColor(centerSubTextColor)
                centerSubTextView!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, centerSubTextSize)
                centerSubTextView!!.gravity = Gravity.CENTER
                centerSubTextView!!.isSingleLine = true
                // 字体
                if (!isInEditMode && centerSubTextFontFamily != 0) {
                    centerSubTextView!!.typeface =
                        ResourcesCompat.getFont(context, centerSubTextFontFamily)
                }
                // 字体加粗
                centerSubTextView!!.paint.isFakeBoldText = centerSubTextBold
                if (TextUtils.isEmpty(centerSubText)) {
                    centerSubTextView!!.visibility = View.GONE
                }
                val centerSubTextParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                centerLayout!!.addView(centerSubTextView, centerSubTextParams)
            }
            TYPE_CENTER_SEARCH_VIEW -> {
                // 初始化通用搜索框
                centerSearchView = RelativeLayout(context)
                centerSearchView!!.setBackgroundResource(centerSearchBgResource)
                val centerParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
                // 设置边距
                centerParams.topMargin = context.dp2px(7f)
                centerParams.bottomMargin = context.dp2px(7f)
                // 根据左边的布局类型来设置边距,布局依赖规则
                when (leftType) {
                    TYPE_LEFT_TEXT_VIEW -> {
                        centerParams.addRule(END_OF, leftTextView!!.id)
                        centerParams.marginStart = PADDING_5
                    }
                    TYPE_LEFT_IMAGE_BUTTON -> {
                        centerParams.addRule(END_OF, leftImageButton!!.id)
                        centerParams.marginStart = PADDING_5
                    }
                    TYPE_LEFT_CUSTOM_VIEW -> {
                        centerParams.addRule(END_OF, leftCustomView!!.id)
                        centerParams.marginStart = PADDING_5
                    }
                    else -> {
                        centerParams.marginStart = PADDING_16
                    }
                }
                // 根据右边的布局类型来设置边距,布局依赖规则
                when (rightType) {
                    TYPE_RIGHT_TEXT_VIEW -> {
                        centerParams.addRule(START_OF, rightTextView!!.id)
                        centerParams.marginEnd = PADDING_5
                    }
                    TYPE_RIGHT_IMAGE_BUTTON -> {
                        centerParams.addRule(START_OF, rightImageButton!!.id)
                        centerParams.marginEnd = PADDING_5
                    }
                    TYPE_RIGHT_CUSTOM_VIEW -> {
                        centerParams.addRule(START_OF, rightCustomView!!.id)
                        centerParams.marginEnd = PADDING_5
                    }
                    else -> {
                        centerParams.marginEnd = PADDING_16
                    }
                }
                rlMain!!.addView(centerSearchView, centerParams)
                // 初始化搜索框搜索ImageView
                centerSearchLeftImageView = ImageView(context)
                centerSearchLeftImageView!!.id = ViewUtil.generateViewId()
                centerSearchLeftImageView!!.setOnClickListener(this)
                val searchIconWidth = context.dp2px(15f)
                val searchParams = LayoutParams(searchIconWidth, searchIconWidth)
                searchParams.addRule(CENTER_VERTICAL)
                searchParams.addRule(ALIGN_PARENT_START)
                searchParams.marginStart = PADDING_16
                centerSearchView!!.addView(centerSearchLeftImageView, searchParams)
                centerSearchLeftImageView!!.setImageResource(R.drawable.common_toolbar_search_normal)
                centerSearchLeftImageView!!.setTintColor(centerSearchIconTint)
                // 初始化搜索框语音ImageView
                centerSearchRightImageView = ImageView(context)
                centerSearchRightImageView!!.id = ViewUtil.generateViewId()
                centerSearchRightImageView!!.setOnClickListener(this)
                val voiceParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                voiceParams.addRule(CENTER_VERTICAL)
                voiceParams.addRule(ALIGN_PARENT_END)
                voiceParams.marginEnd = PADDING_16
                centerSearchView!!.addView(centerSearchRightImageView, voiceParams)
                if (centerSearchRightType == TYPE_CENTER_SEARCH_RIGHT_VOICE) {
                    centerSearchRightImageView!!.setImageResource(R.drawable.common_toolbar_voice)
                    centerSearchRightImageView!!.setTintColor(centerSearchIconTint)
                } else {
                    centerSearchRightImageView!!.setImageResource(R.drawable.common_toolbar_delete_normal)
                    centerSearchRightImageView!!.visibility = View.GONE
                }
                // 初始化文字输入框
                centerSearchEditText = EditText(context)
                centerSearchEditText!!.setBackgroundColor(Color.TRANSPARENT)
                centerSearchEditText!!.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                centerSearchEditText!!.hint = centerSearchHintText
                centerSearchEditText!!.setHintTextColor(centerSearchHintTextColor)
                centerSearchEditText!!.setTextColor(centerSearchTextColor)
                centerSearchEditText!!.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    centerSearchTextSize
                )
                centerSearchEditText!!.setPadding(PADDING_5, 0, PADDING_5, 0)
                if (!centerSearchEditable) {
                    centerSearchEditText!!.isCursorVisible = false
                    centerSearchEditText!!.clearFocus()
                    centerSearchEditText!!.isFocusable = false
                    centerSearchEditText!!.setOnClickListener(this)
                }
                centerSearchEditText!!.isCursorVisible = false
                centerSearchEditText!!.isSingleLine = true
                centerSearchEditText!!.ellipsize = TextUtils.TruncateAt.END
                centerSearchEditText!!.imeOptions = EditorInfo.IME_ACTION_SEARCH
                centerSearchEditText!!.addTextChangedListener(centerSearchWatcher)
                centerSearchEditText!!.onFocusChangeListener = focusChangeListener
                centerSearchEditText!!.setOnEditorActionListener(editorActionListener)
                centerSearchEditText!!.setOnClickListener {
                    centerSearchEditText!!.isCursorVisible = true
                }
                val searchHintParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
                searchHintParams.addRule(END_OF, centerSearchLeftImageView!!.id)
                searchHintParams.addRule(START_OF, centerSearchRightImageView!!.id)
                searchHintParams.addRule(CENTER_VERTICAL)
                searchHintParams.marginStart = PADDING_5
                searchHintParams.marginEnd = PADDING_5
                centerSearchView!!.addView(centerSearchEditText, searchHintParams)
            }
            TYPE_CENTER_CUSTOM_VIEW -> { // 初始化中间自定义布局
                centerCustomView =
                    LayoutInflater.from(context).inflate(centerCustomViewRes, rlMain, false)
                centerCustomView?.apply {
                    if (id == View.NO_ID) {
                        id = ViewUtil.generateViewId()
                    }
                }
                val centerCustomParams = LayoutParams(WRAP_CONTENT, MATCH_PARENT)
                centerCustomParams.marginStart = PADDING_16
                centerCustomParams.marginEnd = PADDING_16
                centerCustomParams.addRule(CENTER_IN_PARENT)
                rlMain!!.addView(centerCustomView, centerCustomParams)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isInEditMode) {
            setUpImmersionToolbar()
        }
    }

    private fun setUpImmersionToolbar() {
        val window = window ?: return
        // 设置状态栏背景透明
        StatusBarUtil.transparentStatusBar(window)
        // 设置图标主题
        if (statusBarMode == 0) {
            StatusBarUtil.setDarkMode(window)
        } else {
            StatusBarUtil.setLightMode(window)
        }
    }

    private val window: Window?
        get() {
            val activity: Activity = when (val context = context) {
                is Activity -> {
                    context
                }
                else -> {
                    (context as ContextWrapper).baseContext as Activity
                }
            }
            return activity.window
        }

    private val centerSearchWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(
            s: CharSequence,
            start: Int,
            count: Int,
            after: Int,
        ) {
        }

        override fun onTextChanged(
            s: CharSequence,
            start: Int,
            before: Int,
            count: Int,
        ) {
        }

        override fun afterTextChanged(s: Editable) =
            when (centerSearchRightType) {
                TYPE_CENTER_SEARCH_RIGHT_VOICE -> {
                    when {
                        TextUtils.isEmpty(s) -> {
                            centerSearchRightImageView!!.setImageResource(R.drawable.common_toolbar_voice)
                        }
                        else -> {
                            centerSearchRightImageView!!.setImageResource(R.drawable.common_toolbar_delete_normal)
                        }
                    }
                }
                else -> {
                    when {
                        TextUtils.isEmpty(s) -> {
                            centerSearchRightImageView!!.visibility = View.GONE
                        }
                        else -> {
                            centerSearchRightImageView!!.visibility = View.VISIBLE
                        }
                    }
                }
            }
    }
    private val focusChangeListener = OnFocusChangeListener { _, hasFocus ->
        if (centerSearchRightType == TYPE_CENTER_SEARCH_RIGHT_DELETE) {
            val input = centerSearchEditText!!.text.toString()
            when {
                hasFocus && !TextUtils.isEmpty(input) -> {
                    centerSearchRightImageView!!.visibility = View.VISIBLE
                }
                else -> {
                    centerSearchRightImageView!!.visibility = View.GONE
                }
            }
        }
    }
    private val editorActionListener = OnEditorActionListener { v, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            onToolbarClickListener?.invoke(
                v,
                MotionAction.ACTION_SEARCH_SUBMIT,
                centerSearchEditText!!.text.toString()
            )
        }
        false
    }

    /**
     * 双击事件中，上次被点击时间
     */
    private var lastClickMillis: Long = 0

    override fun onClick(v: View) {
        when (v) {
            centerSearchRightImageView -> {
                centerSearchEditText!!.setText("")
            }
        }
        onToolbarClickListener?.apply {
            when (v) {
                centerLayout -> {
                    onToolbarCenterDoubleClickListener?.let {
                        val currentClickMillis = System.currentTimeMillis()
                        if (currentClickMillis - lastClickMillis < 500) {
                            it.invoke(v)
                        }
                        lastClickMillis = currentClickMillis
                    }
                }
                leftTextView -> {
                    invoke(v, MotionAction.ACTION_LEFT_TEXT, null)
                }
                leftImageButton -> {
                    invoke(v, MotionAction.ACTION_LEFT_BUTTON, null)
                }
                rightTextView -> {
                    invoke(v, MotionAction.ACTION_RIGHT_TEXT, null)
                }
                rightImageButton -> {
                    invoke(v, MotionAction.ACTION_RIGHT_BUTTON, null)
                }
                centerSearchEditText, centerSearchLeftImageView -> {
                    invoke(v, MotionAction.ACTION_SEARCH, null)
                }
                centerSearchRightImageView -> {
                    centerSearchEditText!!.setText("")
                    if (centerSearchRightType == TYPE_CENTER_SEARCH_RIGHT_VOICE) {
                        // 语音按钮被点击
                        invoke(v, MotionAction.ACTION_SEARCH_VOICE, null)
                    } else {
                        // 删除按钮被点击
                        invoke(v, MotionAction.ACTION_SEARCH_DELETE, null)
                    }
                }
                centerTextView -> {
                    invoke(v, MotionAction.ACTION_CENTER_TEXT, null)
                }
            }
        }
    }

    /**
     * 设置背景颜色
     *
     * @param color
     */
    override fun setBackgroundColor(color: Int) {
        viewStatusBarFill?.setBackgroundColor(color)
        rlMain?.setBackgroundColor(color)
    }

    /**
     * 设置背景图片
     *
     * @param resource
     */
    override fun setBackgroundResource(resource: Int) {
        setBackgroundColor(Color.TRANSPARENT)
        super.setBackgroundResource(resource)
    }

    /**
     * 是否填充状态栏
     *
     * @param show
     */
    fun showStatusBar(show: Boolean) {
        viewStatusBarFill?.visibility = when {
            show -> View.VISIBLE
            else -> View.GONE
        }
    }

    /**
     * 切换状态栏模式
     */
    fun toggleStatusBarMode() {
        val window = window ?: return
        StatusBarUtil.transparentStatusBar(window)
        when (statusBarMode) {
            0 -> {
                statusBarMode = 1
                StatusBarUtil.setLightMode(window)
            }
            else -> {
                statusBarMode = 0
                StatusBarUtil.setDarkMode(window)
            }
        }
    }

    /**
     * @param leftView
     */
    fun setLeftView(leftView: View) {
        if (leftView.id == View.NO_ID) {
            leftView.id = ViewUtil.generateViewId()
        }
        val leftInnerParams = LayoutParams(WRAP_CONTENT, MATCH_PARENT)
        leftInnerParams.addRule(ALIGN_PARENT_START)
        leftInnerParams.addRule(CENTER_VERTICAL)
        rlMain?.addView(leftView, leftInnerParams)
    }

    /**
     * @param centerView
     */
    fun setCenterView(centerView: View) {
        if (centerView.id == View.NO_ID) {
            centerView.id = ViewUtil.generateViewId()
        }
        val centerInnerParams = LayoutParams(WRAP_CONTENT, MATCH_PARENT)
        centerInnerParams.addRule(CENTER_IN_PARENT)
        centerInnerParams.addRule(CENTER_VERTICAL)
        rlMain?.addView(centerView, centerInnerParams)
    }

    /**
     * @param rightView
     */
    fun setRightView(rightView: View) {
        if (rightView.id == View.NO_ID) {
            rightView.id = ViewUtil.generateViewId()
        }
        val rightInnerParams = LayoutParams(WRAP_CONTENT, MATCH_PARENT)
        rightInnerParams.addRule(ALIGN_PARENT_END)
        rightInnerParams.addRule(CENTER_VERTICAL)
        rlMain?.addView(rightView, rightInnerParams)
    }

    /**
     * 显示中间进度条
     */
    fun showCenterProgress() {
        progressCenter?.visibility = View.VISIBLE
    }

    /**
     * 隐藏中间进度条
     */
    fun dismissCenterProgress() {
        progressCenter?.visibility = View.GONE
    }

    /**
     * 显示或隐藏输入法,centerType="searchView"模式下有效
     */
    fun showSoftInputKeyboard(show: Boolean) {
        centerSearchEditText?.run {
            when {
                centerSearchEditable && show -> {
                    isFocusable = true
                    isFocusableInTouchMode = true
                    requestFocus()
                    openKeyboard()
                }
                else -> {
                    closeKeyboard()
                }
            }
        }
    }

    /**
     * 设置搜索框右边图标
     *
     * @param res
     */
    fun setSearchRightImageResource(res: Int) {
        centerSearchRightImageView?.setImageResource(res)
    }

    /**
     * 获取SearchView输入结果
     */
    val searchKey: String
        get() = centerSearchEditText?.text?.toString() ?: ""

    @Target(
        AnnotationTarget.FIELD,
        AnnotationTarget.ANNOTATION_CLASS,
        AnnotationTarget.CLASS,
        AnnotationTarget.VALUE_PARAMETER
    )
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class MotionAction {
        companion object {
            /**
             * 左边TextView被点击
             */
            var ACTION_LEFT_TEXT = 1

            /**
             * 左边ImageBtn被点击
             */
            var ACTION_LEFT_BUTTON = 2

            /**
             * 右边TextView被点击
             */
            var ACTION_RIGHT_TEXT = 3

            /**
             * 右边ImageBtn被点击
             */
            var ACTION_RIGHT_BUTTON = 4

            /**
             * 搜索框被点击,搜索框不可输入的状态下会被触发
             */
            var ACTION_SEARCH = 5

            /**
             * 搜索框输入状态下,键盘提交触发
             */
            var ACTION_SEARCH_SUBMIT = 6

            /**
             * 语音按钮被点击
             */
            var ACTION_SEARCH_VOICE = 7

            /**
             * 搜索删除按钮被点击
             */
            var ACTION_SEARCH_DELETE = 8

            /**
             * 中间文字点击
             */
            var ACTION_CENTER_TEXT = 9
        }
    }

    companion object {
        private const val MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT
        private const val WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT
        const val TYPE_LEFT_NONE = 0
        const val TYPE_LEFT_TEXT_VIEW = 1
        const val TYPE_LEFT_IMAGE_BUTTON = 2
        const val TYPE_LEFT_CUSTOM_VIEW = 3
        const val TYPE_RIGHT_NONE = 0
        const val TYPE_RIGHT_TEXT_VIEW = 1
        const val TYPE_RIGHT_IMAGE_BUTTON = 2
        const val TYPE_RIGHT_CUSTOM_VIEW = 3
        const val TYPE_CENTER_NONE = 0
        const val TYPE_CENTER_TEXT_VIEW = 1
        const val TYPE_CENTER_SEARCH_VIEW = 2
        const val TYPE_CENTER_CUSTOM_VIEW = 3
        const val TYPE_CENTER_SEARCH_RIGHT_VOICE = 0
        const val TYPE_CENTER_SEARCH_RIGHT_DELETE = 1
    }

    init {
        loadAttributes(context, attrs)
        initGlobalViews(context)
    }
}