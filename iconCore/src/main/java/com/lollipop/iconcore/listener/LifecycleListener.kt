package com.lollipop.iconcore.listener

import android.os.Bundle

/**
 * @author lollipop
 * @date 9/21/20 01:33
 */
interface LifecycleListener<T> {

    fun onCreate(target: T, savedInstanceState: Bundle?)

    fun onStart(target: T)

    fun onStop(target: T)

    fun onResume(target: T)

    fun onPause(target: T)

    fun onDestroy(target: T)

    fun onRestart(target: T)

}