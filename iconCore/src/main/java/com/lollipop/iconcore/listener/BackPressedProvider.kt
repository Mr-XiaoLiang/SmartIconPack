package com.lollipop.iconcore.listener

/**
 * @author lollipop
 * @date 2020/5/15 00:00
 * 返回事件提供者
 *
 * 这是返回事件的提供者接口
 * 表示这是一个返回事件的分发源，
 * 它将会收到或产生返回事件，允许监听并且拦截次事件。
 * 需要注意的是，返回事件的监听遵循先到先得的规定，
 * 如果有必要，请尽早注册，但是不应该在自身的责任之外监听或拦截事件，
 * 这可能造成不必要的逻辑冲突或内存泄漏
 *
 */
interface BackPressedProvider {

    /**
     * 添加一个返回事件监听器
     * 详情请见 {@link BackPressedListener#onBackPressed() }
     */
    fun addBackPressedListener(listener: BackPressedListener)

    /**
     * 移除一个返回事件监听器
     * 详情请见 {@link BackPressedListener#onBackPressed() }
     */
    fun removeBackPressedListener(listener: BackPressedListener)

}