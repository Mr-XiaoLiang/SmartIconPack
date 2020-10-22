package com.lollipop.iconcore.provider

import android.app.Activity
import android.os.Bundle

/**
 * @author lollipop
 * @date 10/22/20 01:42
 * 主页的真正实现者
 */
interface MainPageRenderer {

    fun onCreate(activity: Activity, savedInstanceState: Bundle?)

    fun onStart(activity: Activity)

    fun onStop(activity: Activity)

    fun onResume(activity: Activity)

    fun onPause(activity: Activity)

    fun onRestart(activity: Activity)

    fun onDestroy(activity: Activity)

    fun onSaveInstanceState(activity: Activity, outState: Bundle)

    fun onRestoreInstanceState(activity: Activity, savedInstanceState: Bundle)

}