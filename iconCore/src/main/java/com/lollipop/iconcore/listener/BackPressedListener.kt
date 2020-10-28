package com.lollipop.iconcore.listener

/**
 * @author lollipop
 * @date 2020/5/14 23:58
 * 返回按钮监听器
 */
interface BackPressedListener {
    /**
     * 当返回事件发生并且没有被拦截时，
     * 将会被分发并且触发此方法
     *
     * @return 当返回值为true时，表示处理本次事件，
     * 那么事件分发将不会继续下去，并且将不会执行额外的操作(比如关闭页面)
     * 此时，需要针对返回事件，做相应的响应。否则用户可能认为无响应。
     * 如果返回了false，那么表示此处不做处理，那么事件将会继续被分发，
     * 直到被处理或者被默认的方式处理
     */
    fun onBackPressed(): Boolean
}