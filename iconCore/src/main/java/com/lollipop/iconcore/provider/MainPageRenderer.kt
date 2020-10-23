package com.lollipop.iconcore.provider

import android.app.Activity
import android.os.Bundle
import com.lollipop.iconcore.listener.LifecycleListener

/**
 * @author lollipop
 * @date 10/22/20 01:42
 * 主页的真正实现者
 */
interface MainPageRenderer: LifecycleListener<Activity> {

    fun onSaveInstanceState(activity: Activity, outState: Bundle)

    fun onRestoreInstanceState(activity: Activity, savedInstanceState: Bundle)

}