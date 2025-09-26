package com.example.appsample1.component

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
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
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import com.example.appsample1.support.AppLoggerCS
import com.example.appsample1.toPx
import com.konneknative.R
import kotlin.math.abs

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
                // Start engine here if appropriate
                AppLoggerCS.debugLog(
                    "[FlutterEngineHelper][registerLifecycle][onActivityStarted] Activity started: ${activity.localClassName}"
                )
            }

            override fun onActivityStopped(activity: Activity) {
//                AppLoggerCS.debugLog(
//                    "[FlutterEngineHelper][registerLifecycle][onActivityStopped] Activity stopped: ${activity.localClassName}"
//                )
            }

            override fun onActivityDestroyed(activity: Activity) {
//                AppLoggerCS.debugLog(
//                    "[FlutterEngineHelper][registerLifecycle][onActivityDestroyed] Activity destroyed: ${activity.localClassName}"
//                )
            }

            // Required empty implementations
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
//                AppLoggerCS.debugLog(
//                    "[FlutterEngineHelper][registerLifecycle][onActivityCreated] Activity created: ${activity.localClassName}"
//                )
            }

            override fun onActivityResumed(activity: Activity) {
//                AppLoggerCS.debugLog(
//                    "[FlutterEngineHelper][registerLifecycle][onActivityResumed] Activity resumed: ${activity.localClassName}"
//                )
            }

            override fun onActivityPaused(activity: Activity) {
//                AppLoggerCS.debugLog(
//                    "[FlutterEngineHelper][registerLifecycle][onActivityPaused] Activity paused: ${activity.localClassName}"
//                )
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
//                AppLoggerCS.debugLog(
//                    "[FlutterEngineHelper][registerLifecycle][onActivitySaveInstanceState] Activity onActivitySaveInstanceState: ${activity.localClassName}"
//                )
            }
        })
    }

    fun addEmptyViewSpace(context: Context, linearLayout: LinearLayout, widthDp: Float, heightDp: Float) {
        val spacer = View(context)
        val layoutParams = LinearLayout.LayoutParams(
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, widthDp, context.resources.displayMetrics).toInt(),
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightDp, context.resources.displayMetrics).toInt()
        )
        spacer.layoutParams = layoutParams
        linearLayout.addView(spacer)
    }

    init {
        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )

        // Use horizontal LinearLayout to place image + text side by side
        container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
//            setPadding(24, 24, 24, 24)
            setPadding(12, 12, 12, 12)
            val drawableData =
                ContextCompat.getDrawable(context, R.drawable.ic_konnek) // Optional background
            val bitmap = (drawableData as BitmapDrawable).bitmap
            val scaledDrawable = BitmapDrawable(context.resources, bitmap).apply {
                gravity = android.view.Gravity.FILL
                setTileModeXY(null, null)
            }
            background = scaledDrawable
            gravity = Gravity.CENTER_VERTICAL
        }

        imageView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                130,
                130,
            ) // Customize size as needed
            scaleType = ImageView.ScaleType.FIT_CENTER
//            setPadding(12, 0, 12, 0) // space between icon and text
        }

        textView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(12.toPx(context), 0, 12, 0) // space between icon and text
            }
            setTextColor(Color.WHITE)
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            // text = "Open"
        }

        addEmptyViewSpace(context, container, 12f, 0f)
        container.addView(imageView)
        container.addView(textView)
        addView(container)

        setOnTouchListener(this)
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        val layoutParams = layoutParams as MarginLayoutParams
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downRawX = event.rawX
                downRawY = event.rawY
                dX = view.x - downRawX
                dY = view.y - downRawY
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val parent = parent as? View ?: return false
                val parentWidth = parent.width
                val parentHeight = parent.height
                val viewWidth = width
                val viewHeight = height

                var newX = event.rawX + dX
                var newY = event.rawY + dY

                newX = newX.coerceIn(
                    layoutParams.leftMargin.toFloat(),
                    (parentWidth - viewWidth - layoutParams.rightMargin).toFloat()
                )
                newY = newY.coerceIn(
                    layoutParams.topMargin.toFloat(),
                    (parentHeight - viewHeight - layoutParams.bottomMargin).toFloat()
                )

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
            // setBackgroundColor(Color.parseColor(colorHex))
            setBackgroundColor(colorHex.toColorInt())
        } catch (e: Exception) {
            // setBackgroundColor(Color.parseColor("#ffffffff"))
            setBackgroundColor("#FFFFFF".toColorInt())
        }

    }

    companion object {
        private const val CLICK_DRAG_TOLERANCE = 10f
    }

    fun scaleToFitWidth(bitmap: Bitmap, screenWidth: Int): Bitmap {
        val factor = screenWidth / bitmap.width.toFloat()
        return Bitmap.createScaledBitmap(
            bitmap,
            screenWidth,
            (bitmap.height * factor).toInt(),
            true
        )
    }
}
