package com.lollipop.iconcore.listener

import android.view.View

/**
 * @author lollipop
 * @date 2020/5/13 23:03
 * 窗口缩进变化的监听器
 */
interface OnWindowInsetsListener {

    fun onInsetsChange(root: View, left: Int, top: Int, right: Int, bottom: Int)

}