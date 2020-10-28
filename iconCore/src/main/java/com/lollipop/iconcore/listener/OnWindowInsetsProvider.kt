package com.lollipop.iconcore.listener

/**
 * @author lollipop
 * @date 2020/5/13 23:29
 * 窗口缩进的提供者
 *
 * 这是窗口缩进的提供者接口
 * 表示它具有窗口缩进信息的数据来源
 *
 */
interface OnWindowInsetsProvider {

    /**
     * 添加一个窗口缩进监听
     * 请见
     * @see {@link OnWindowInsetsListener}
     */
    fun addOnWindowInsetsProvider(listener: OnWindowInsetsListener)

    /**
     * 移除一个窗口缩进监听
     * 请见
     * @see {@link OnWindowInsetsListener}
     */
    fun removeOnWindowInsetsProvider(listener: OnWindowInsetsListener)

}