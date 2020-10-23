package com.lollipop.iconcore.listener

import com.lollipop.iconcore.listener.BackPressedListener

/**
 * @author lollipop
 * @date 2020/5/15 00:00
 * 返回事件提供者
 */
interface BackPressedProvider {

    fun addBackPressedListener(listener: BackPressedListener)

    fun removeBackPressedListener(listener: BackPressedListener)

}