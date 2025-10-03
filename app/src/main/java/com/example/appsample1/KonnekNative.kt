package com.example.appsample1

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.example.appsample1.component.MovableFloatingActionButton
import com.example.appsample1.support.base64ToBitmap
import androidx.core.graphics.toColorInt
import com.example.appsample1.FlutterEngineHelper.envInit
import com.example.appsample1.support.AppLoggerCS

object KonnekNative {
    internal var clientId: String = ""
    internal var clientSecret: String = ""
    internal var flavor: String = ""
    lateinit var triggerFloatingUIChanges: (Map<*, *>) -> Unit

    @JvmStatic
    fun initializeSDK(
        context: Context,
        id: String,
        secret: String,
        flavorData: String = "production",
    ) {
        clientId = id
        clientSecret = secret
        flavor = flavorData
        envInit(flavor)
        FlutterEngineHelper.ensureEngine(context.applicationContext)
        AppLoggerCS.useLogger = true
    }

    fun disposeEngine() {
        FlutterEngineHelper.disposeEngine()
    }

    @JvmStatic
    fun getFloatingButtonCustomize(context: Context, fontResId: Int?): FrameLayout {
        var bgColor = "#FFFFFF"
        var textColor = "#000000"
        var textButton = ""
        var iconButton = ""

        FlutterEngineHelper.callConfigViaNative(context)

        var btn = MovableFloatingActionButton(context)

        btn.apply {
            setBackgroundColor(bgColor.toColorInt())
            setPadding(
                20, 20, 20, 20
            )
            layoutParams = CoordinatorLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                70.toPx(context)
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                marginEnd = 10.toPx(context)
                bottomMargin = 10.toPx(context)
                minimumWidth = 195.toPx(context)
            }
            elevation = 16f
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 10.toPx(context).toFloat()
                setColor((bgColor).toColorInt()) // background color
            }
            setOnClickListener {
                FlutterEngineHelper.launchFlutter(context)
            }
        }

        triggerFloatingUIChanges = { datas ->
            if (datas["status"] != "true" || datas["status"] != true) {
                if (datas["button_color"] != null) {
                    bgColor = datas["button_color"] as String? ?: ""
                }
                if (datas["text_button_color"] != null) {
                    textColor = datas["text_button_color"] as String? ?: ""
                }
                if (datas["text_status"] == true) {
                    textButton = datas["text_button"] as String? ?: ""
                } else {
                    textButton = ""
                }
                iconButton = datas["ios_icon"] as String? ?: ""
                val bitmap: Bitmap?
                if (iconButton != ""){
                    bitmap = iconButton.base64ToBitmap()
                } else {
                    bitmap = null
                }

                btn.apply {
                    setButtonTextColor(textColor)
                    setButtonBackgroundColor(bgColor)
                    setButtonText(textButton)
                    setBackgroundImage(null)
                    if (fontResId != null) {
                        setButtonTextFontStyle(fontResId)
                    }
                    if (bitmap != null) {
                        setButtonIconVisibility(true)
                        bitmap.let { output ->
                            setButtonIcon2(output)
                        } ?: run {
                            // setButtonIcon(R.drawable.ic_konnek)
                        }
                    } else {
                        setButtonIconVisibility(false)
                    }
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 10.toPx(context).toFloat()
                        setColor(bgColor.toColorInt()) // background color
                    }
                }
            }
        }

        // Jika pakai constraint layout
        if (btn.layoutParams is ConstraintLayout.LayoutParams) {
            val layoutParams = btn.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
        }

        return btn
    }

    @JvmStatic
    fun getFloatingButton(context: Context): FrameLayout {
        var bgColor = "#FFFFFF"
        var textColor = "#000000"
        var textButton = ""
        var iconButton = ""

        FlutterEngineHelper.callConfigViaNative(context)

        var btn = MovableFloatingActionButton(context)

        btn.apply {
            setBackgroundColor(bgColor.toColorInt())
            setPadding(
                20, 20, 20, 20
            )
            layoutParams = CoordinatorLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                70.toPx(context)
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                marginEnd = 10.toPx(context)
                bottomMargin = 10.toPx(context)
                minimumWidth = 195.toPx(context)
            }
            elevation = 16f
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 10.toPx(context).toFloat()
                setColor((bgColor).toColorInt()) // background color
            }
            setOnClickListener {
                FlutterEngineHelper.launchFlutter(context)
            }
        }

        triggerFloatingUIChanges = { datas ->
            if (datas["status"] != "true" || datas["status"] != true) {
                if (datas["button_color"] != null) {
                    bgColor = datas["button_color"] as String? ?: ""
                }
                if (datas["text_button_color"] != null) {
                    textColor = datas["text_button_color"] as String? ?: ""
                }
                if (datas["text_status"] == true) {
                    textButton = datas["text_button"] as String? ?: ""
                } else {
                    textButton = ""
                }
                iconButton = datas["ios_icon"] as String? ?: ""
                val bitmap: Bitmap?
                if (iconButton != ""){
                    bitmap = iconButton.base64ToBitmap()
                } else {
                    bitmap = null
                }

                btn.apply {
                    setButtonTextColor(textColor)
                    setButtonBackgroundColor(bgColor)
                    setButtonText(textButton)
                    setBackgroundImage(null)
                    if (bitmap != null) {
                        setButtonIconVisibility(true)
                        bitmap.let { output ->
                            setButtonIcon2(output)
                        } ?: run {
                            // setButtonIcon(R.drawable.ic_konnek)
                        }
                    } else {
                        setButtonIconVisibility(false)
                    }
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 10.toPx(context).toFloat()
                        setColor(bgColor.toColorInt()) // background color
                    }
                }
            }
        }

        // Jika pakai constraint layout
        if (btn.layoutParams is ConstraintLayout.LayoutParams) {
            val layoutParams = btn.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
        }

        return btn
    }
}

fun Int.toPx(context: Context): Int =
    (this * context.resources.displayMetrics.density).toInt()

