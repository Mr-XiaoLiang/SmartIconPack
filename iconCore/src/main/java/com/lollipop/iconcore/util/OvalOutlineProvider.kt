package com.lollipop.iconcore.util

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider

/**
 * @author lollipop
 * @date 10/26/20 18:12
 * 椭圆的View剪裁工具
 * 绑定到一个View上，使他被剪裁为椭圆形
 */
class OvalOutlineProvider: ViewOutlineProvider() {

    companion object {
        /**
         * 绑定到一个View上，使他被剪裁为椭圆形
         */
        fun bind(view: View?) {
            view?:return
            val outlineProvider = view.outlineProvider
            if (outlineProvider != null && outlineProvider is OvalOutlineProvider) {
                return
            }
            view.outlineProvider = OvalOutlineProvider()
            view.clipToOutline = true
        }
    }

    override fun getOutline(view: View?, outline: Outline?) {
        view?.let {
            outline?.setOval(0, 0, it.width, it.height)
        }
    }

}