package com.lollipop.iconcore.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.lollipop.iconcore.listener.BackPressedListener
import com.lollipop.iconcore.listener.OnWindowInsetsListener
import com.lollipop.iconcore.provider.MainPageRenderer
import kotlinx.android.extensions.LayoutContainer

/**
 * @author lollipop
 * @date 10/23/20 19:01
 * 简易的Activity呈现类实现，它提供了基础的方法实现
 * 可以更加简单的实现Activity的布局
 */
open class SimpleActivityRenderer: MainPageRenderer,
        LayoutContainer, OnWindowInsetsListener, BackPressedListener {

    private var activityView: View? = null

    override val containerView: View?
        get() = activityView

    /**
     * 为activty设置内容体的View
     */
    protected fun setContentView(activity: IconPackActivity, view: View) {
        activityView = view
        activity.setContentView(activityView)
        activity.initRootGroup(view)
    }

    /**
     * 以ID的形式为activity设置内容体的view
     */
    protected fun setContentView(activity: IconPackActivity, resId: Int) {
        setContentView(activity,
            LayoutInflater.from(activity).inflate(resId, null))
    }

    /**
     * 为了弥补不在activity中，不能便捷寻找View的缺憾
     * 这里提供了方法，简化来这个过程
     */
    protected fun <T: View> find(id: Int): T? {
        return activityView?.findViewById(id)
    }

    override fun onCreate(target: IconPackActivity, savedInstanceState: Bundle?) {

    }

    override fun onStart(target: IconPackActivity) {

    }

    override fun onStop(target: IconPackActivity) {

    }

    override fun onResume(target: IconPackActivity) {

    }

    override fun onPause(target: IconPackActivity) {

    }

    override fun onRestart(target: IconPackActivity) {

    }

    override fun onDestroy(target: IconPackActivity) {

    }

    override fun onSaveInstanceState(target: IconPackActivity, outState: Bundle) {

    }

    override fun onRestoreInstanceState(target: IconPackActivity, savedInstanceState: Bundle) {

    }

    override fun onInsetsChange(root: View, left: Int, top: Int, right: Int, bottom: Int) {

    }

    override fun onBackPressed(): Boolean {
        return false
    }

}