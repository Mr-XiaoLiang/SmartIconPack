package com.lollipop.iconcore.provider

import android.os.Bundle
import com.lollipop.iconcore.listener.BackPressedListener
import com.lollipop.iconcore.listener.LifecycleListener
import com.lollipop.iconcore.listener.OnWindowInsetsListener
import com.lollipop.iconcore.ui.IconPackActivity

/**
 * @author lollipop
 * @date 10/22/20 01:42
 * 主页的真正实现者
 *
 * 它具备了基本的生命周期接口，
 * 以及窗口缩进事件分发接口
 * （需要调用initRootGroup注册事件或者手动实现），
 * 返回事件处理分发接口
 *
 */
interface MainPageRenderer: LifecycleListener<IconPackActivity>,
        OnWindowInsetsListener, BackPressedListener {

    fun onSaveInstanceState(target: IconPackActivity, outState: Bundle)

    fun onRestoreInstanceState(target: IconPackActivity, savedInstanceState: Bundle)

}