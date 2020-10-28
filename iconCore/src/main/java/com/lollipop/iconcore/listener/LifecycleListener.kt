package com.lollipop.iconcore.listener

import android.os.Bundle

/**
 * @author lollipop
 * @date 9/21/20 01:33
 *  这是一个通用的生命周期监听类，
 *  提供了基础的生命周期函数，并且由范型来表示被监听对象
 *  这表明，它未来可能诞生业务专属的监听衍生类
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