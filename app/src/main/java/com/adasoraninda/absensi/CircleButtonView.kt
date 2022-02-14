package com.adasoraninda.absensi

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.getResourceIdOrThrow

class CircleButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private var imageIndicator: ImageView? = null

    init {
        inflate(context, R.layout.layout_circle_button, this)
        initAttribute()

        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CircleButtonView,
            0, 0
        )

        try {
            val drawableId = typedArray.getResourceIdOrThrow(R.styleable.CircleButtonView_cbv_src)
            Log.d(TAG, "$drawableId")
            initView(drawableId)
        } finally {
            typedArray.recycle()
        }

    }

    private fun initView(@DrawableRes drawableId: Int) {
        imageIndicator = findViewById(R.id.image)
        val drawable = ContextCompat.getDrawable(context, drawableId)
        imageIndicator?.setImageDrawable(drawable)
    }

    private fun initAttribute() {
        isClickable = true
        isFocusable = true
        background = ContextCompat.getDrawable(context, R.drawable.bg_circle)
    }

    companion object {
        const val TAG = "CircleButtonView"
    }

}