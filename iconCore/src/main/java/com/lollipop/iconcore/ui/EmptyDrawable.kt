package com.lollipop.iconcore.ui

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

/**
 * @author lollipop
 * @date 11/1/20 17:49
 * 一个空的drawable
 */
class EmptyDrawable: Drawable() {

    override fun draw(canvas: Canvas) {
        // draw nothing
    }

    override fun setAlpha(alpha: Int) { }

    override fun setColorFilter(colorFilter: ColorFilter?) { }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }
}