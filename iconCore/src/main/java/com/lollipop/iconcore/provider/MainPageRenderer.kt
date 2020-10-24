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
 */
interface MainPageRenderer: LifecycleListener<IconPackActivity>,
        OnWindowInsetsListener, BackPressedListener {

    fun onSaveInstanceState(target: IconPackActivity, outState: Bundle)

    fun onRestoreInstanceState(target: IconPackActivity, savedInstanceState: Bundle)

}