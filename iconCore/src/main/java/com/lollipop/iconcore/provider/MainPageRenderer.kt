package com.lollipop.iconcore.provider

import android.app.Activity
import android.os.Bundle
import com.lollipop.iconcore.listener.LifecycleListener
import com.lollipop.iconcore.ui.IconPackActivity

/**
 * @author lollipop
 * @date 10/22/20 01:42
 * 主页的真正实现者
 */
interface MainPageRenderer: LifecycleListener<IconPackActivity> {

    fun onSaveInstanceState(activity: IconPackActivity, outState: Bundle)

    fun onRestoreInstanceState(activity: IconPackActivity, savedInstanceState: Bundle)

}