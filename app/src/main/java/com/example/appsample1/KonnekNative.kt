package com.example.appsample1

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.example.appsample1.component.MovableFloatingActionButton
import com.example.appsample1.support.base64ToBitmap
import androidx.core.graphics.toColorInt
import com.example.appsample1.FlutterEngineHelper.envInit
import com.example.appsample1.support.AppLoggerCS

data class FloatingButtonConfig(
    val buttonColor: String = "#FFFFFF",
    val textButtonColor: String = "#000000",
    val textButton: String = "",
    val iconBase64: String = "",
    val isTextVisible: Boolean = false,
)

object KonnekNative {
    internal var clientId: String = ""
    internal var clientSecret: String = ""
    internal var flavor: String = ""

    var triggerFloatingUIChanges: ((Map<*, *>) -> Unit)? = null

    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Initialize the SDK
     *
     * @param context   Application context
     * @param id        Client ID provided by Konnek
     * @param secret    Client secret provided by Konnek
     * @param flavorData Environment string: "production" | "staging" (default: "production")
     */
    @JvmStatic
    @JvmOverloads
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
        AppLoggerCS.useLogger = false
    }

    fun disposeEngine() {
        FlutterEngineHelper.disposeEngine()
    }

    /**
     * Direct to Konnek Chats function
     *
     * Bypassing Floating Button navigation direct to Konnek Chats
     *
     * without using getFloatingButton
     */
    @JvmStatic
    fun openChat(context: Context) {
        FlutterEngineHelper.launchFlutter(context);
    }

    /**
     * Returns floating action button component.
     *
     * [fontResId] is optional — pass null (or omit) for default font.
     *
     * Usage (Kotlin):
     * ```kotlin
     * val fab = KonnekNative.getFloatingButton(this)
     * val fabCustom = KonnekNative.getFloatingButton(this, fontResId = R.font.zurich_sans)
     * root.addView(fab)
     * ```
     *
     * Usage (Java):
     * ```java
     * FrameLayout fab = KonnekNative.getFloatingButton(this, null);
     * root.addView(fab);
     * ```
     *
     * @param context   Activity or Fragment context
     * @param fontResId Optional font resource ID for button label
     */
    @JvmStatic
    @JvmOverloads
    fun getFloatingButton(
        context: Context,
        fontResId: Int? = null,
    ): FrameLayout {
        var config = FloatingButtonConfig()

        FlutterEngineHelper.callConfigViaNative(context)

        val btn = MovableFloatingActionButton(context).apply {
            applyConfig(context, config, fontResId)
            setOnClickListener {
                FlutterEngineHelper.launchFlutter(context)
            }
            minimumWidth = 195.toPx(context)
        }

        triggerFloatingUIChanges = { rawData ->
            val parsed = rawData.toFloatingButtonConfig()

            mainHandler.post {
                config = parsed
                btn.applyConfig(context, config, fontResId)
            }
        }

        btn.layoutParams = buildLayoutParams(context)

        return btn
    }

    /**
     * @deprecated Use [getFloatingButton] with optional [fontResId] instead.
     */
    @Deprecated(
        message = "Use getFloatingButton(context, fontResId) instead.",
        replaceWith = ReplaceWith("getFloatingButton(context, fontResId)"),
    )
    @JvmStatic
    fun getFloatingButtonCustomize(context: Context, fontResId: Int?): FrameLayout =
        getFloatingButton(context, fontResId)

    private fun buildLayoutParams(context: Context): CoordinatorLayout.LayoutParams =
        CoordinatorLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            70.toPx(context),
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            marginEnd = 10.toPx(context)
            bottomMargin = 10.toPx(context)
        }
}

private fun MovableFloatingActionButton.applyConfig(
    context: Context,
    config: FloatingButtonConfig,
    fontResId: Int?,
) {
    setButtonTextColor(config.textButtonColor)
    setButtonBackgroundColor(config.buttonColor)
    setButtonText(if (config.isTextVisible) config.textButton else "")
    setBackgroundImage(null)

    fontResId?.let { setButtonTextFontStyle(it) }

    val bitmap: Bitmap? = config.iconBase64
        .takeIf { it.isNotEmpty() }
        ?.base64ToBitmap()

    if (bitmap != null) {
        setButtonIconVisibility(true)
        setButtonIcon2(bitmap)
    } else {
        setButtonIconVisibility(false)
    }

    background = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 10.toPx(context).toFloat()
        setColor(config.buttonColor.toColorInt())
    }

    if (layoutParams is ConstraintLayout.LayoutParams) {
        (layoutParams as ConstraintLayout.LayoutParams).apply {
            bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
        }
    }
}

private fun Map<*, *>.toFloatingButtonConfig(): FloatingButtonConfig {
    val isActive = this["status"] == "true" || this["status"] == true
    if (!isActive) return FloatingButtonConfig()

    return FloatingButtonConfig(
        buttonColor = this["button_color"] as? String ?: "#FFFFFF",
        textButtonColor = this["text_button_color"] as? String ?: "#000000",
        textButton = this["text_button"] as? String ?: "",
        iconBase64 = this["ios_icon"] as? String ?: "",
        isTextVisible = this["text_status"] == true,
    )
}

fun Int.toPx(context: Context): Int =
    (this * context.resources.displayMetrics.density).toInt()

