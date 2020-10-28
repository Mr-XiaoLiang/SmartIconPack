package com.lollipop.iconcore.listener

/**
 * @author lollipop
 * @date 2020/5/15 00:01
 * 返回事件提供者的辅助分发器
 *
 * 这是默认的返回事件提供者分发器
 * 它为分发者的角色提供了默认的实现，
 * 他会接受事件，并且将它分发出去
 *
 */
class BackPressedProviderHelper: BackPressedProvider, BackPressedListener {

    private val listenerList = ArrayList<BackPressedListener>()

    override fun addBackPressedListener(listener: BackPressedListener) {
        listenerList.add(listener)
    }

    override fun removeBackPressedListener(listener: BackPressedListener) {
        listenerList.remove(listener)
    }

    override fun onBackPressed(): Boolean {
        for (i in listenerList.indices) {
            if (listenerList[i].onBackPressed()) {
                return true
            }
        }
        return false
    }

    /**
     * 这是很有必要的销毁方法
     * 使用时请务必在生命周期中调用此方法，
     * 释放所有监听器，以避免不必要的内存泄漏
     */
    fun destroy() {
        listenerList.clear()
    }

}