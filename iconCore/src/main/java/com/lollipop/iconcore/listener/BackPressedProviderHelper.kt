package com.lollipop.iconcore.listener

/**
 * @author lollipop
 * @date 2020/5/15 00:01
 * 返回事件提供者的辅助分发器
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

    fun destroy() {
        listenerList.clear()
    }

}