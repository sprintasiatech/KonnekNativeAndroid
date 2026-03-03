package com.example.appsample1.component

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import com.example.appsample1.support.AppLoggerCS
import com.example.appsample1.toPx
import com.konneknative.R
import kotlin.math.abs
import androidx.core.graphics.scale

class MovableFloatingActionButton(context: Context) : FrameLayout(context), View.OnTouchListener {

    private var downRawX = 0f
    private var downRawY = 0f
    private var dX = 0f
    private var dY = 0f

    private val imageView: ImageView
    private val textView: TextView
    private val container: LinearLayout

    private fun registerLifecycle(context: Context) {
        val application = context.applicationContext as Application
        application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                AppLoggerCS.debugLog(
                    "[FlutterEngineHelper][registerLifecycle][onActivityStarted] Activity started: ${activity.localClassName}"
                )
            }

            override fun onActivityStopped(activity: Activity) {}
            override fun onActivityDestroyed(activity: Activity) {}
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        })
    }

    fun addEmptyViewSpace(
        context: Context,
        linearLayout: LinearLayout,
        widthDp: Float,
        heightDp: Float,
    ) {
        val spacer = View(context)
        spacer.layoutParams = LinearLayout.LayoutParams(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, widthDp, context.resources.displayMetrics
            ).toInt(),
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, heightDp, context.resources.displayMetrics
            ).toInt(),
        )
        linearLayout.addView(spacer)
    }

    init {
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

        container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(12, 12, 12, 12)
            gravity = Gravity.CENTER_VERTICAL
        }

        imageView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(130, 130)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        textView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                setMargins(12.toPx(context), 0, 12, 0)
            }
            setTextColor(Color.WHITE)
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
        }

        addEmptyViewSpace(context, container, 12f, 0f)
        container.addView(imageView)
        container.addView(textView)
        addView(container)

        setOnTouchListener(this)
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downRawX = event.rawX
                downRawY = event.rawY
                dX = view.x - downRawX
                dY = view.y - downRawY
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val parentView = parent as? View ?: return false

                val parentWidth = parentView.width
                val parentHeight = parentView.height
                val viewWidth = width
                val viewHeight = height

                if (parentWidth <= 0 || parentHeight <= 0) return true

                val layoutParams = layoutParams as? MarginLayoutParams

                val minX = layoutParams?.leftMargin?.toFloat() ?: 0f
                val minY = layoutParams?.topMargin?.toFloat() ?: 0f
                val maxX = (parentWidth - viewWidth - (layoutParams?.rightMargin ?: 0))
                    .toFloat()
                    .coerceAtLeast(minX)
                val maxY = (parentHeight - viewHeight - (layoutParams?.bottomMargin ?: 0))
                    .toFloat()
                    .coerceAtLeast(minY)

                val newX = (event.rawX + dX).coerceIn(minX, maxX)
                val newY = (event.rawY + dY).coerceIn(minY, maxY)

                animate().x(newX).y(newY).setDuration(0).start()
                return true
            }

            MotionEvent.ACTION_UP -> {
                val upDX = event.rawX - downRawX
                val upDY = event.rawY - downRawY
                return if (abs(upDX) < CLICK_DRAG_TOLERANCE && abs(upDY) < CLICK_DRAG_TOLERANCE) {
                    performClick()
                } else true
            }
        }
        return false
    }

    fun setButtonText(text: String) {
        textView.text = text
    }

    fun setButtonTextFontStyle(fontResId: Int) {
        textView.typeface = ResourcesCompat.getFont(context, fontResId)
    }

    fun setBackgroundImage(drawableData: Drawable?) {
        container.background = drawableData
    }

    fun setButtonIcon(resId: Int) {
        imageView.setImageResource(resId)
    }

    fun setButtonIcon2(drawableRes: Bitmap) {
        imageView.setImageBitmap(drawableRes)
    }

    fun setButtonIconVisibility(value: Boolean) {
        imageView.isVisible = value
    }

    fun setButtonTextColor(colorHex: String) {
        try {
            textView.setTextColor(colorHex.toColorInt())
        } catch (e: Exception) {
            textView.setTextColor("#000000".toColorInt())
        }
    }

    fun setTextColor(color: Int) {
        textView.setTextColor(color)
    }

    fun setBackgroundColorCustom(color: Int) {
        (getChildAt(0) as View).setBackgroundColor(color)
    }

    fun setButtonBackgroundColor(colorHex: String) {
        try {
            setBackgroundColor(colorHex.toColorInt())
        } catch (e: Exception) {
            setBackgroundColor("#FFFFFF".toColorInt())
        }
    }

    companion object {
        private const val CLICK_DRAG_TOLERANCE = 10f
    }

    fun scaleToFitWidth(bitmap: Bitmap, screenWidth: Int): Bitmap {
        val factor = screenWidth / bitmap.width.toFloat()
        return bitmap.scale(screenWidth, (bitmap.height * factor).toInt())
    }
}