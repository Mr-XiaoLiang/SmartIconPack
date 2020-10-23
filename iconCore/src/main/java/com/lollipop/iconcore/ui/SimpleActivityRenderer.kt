package com.lollipop.iconcore.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.lollipop.iconcore.provider.MainPageRenderer
import kotlinx.android.extensions.LayoutContainer

/**
 * @author lollipop
 * @date 10/23/20 19:01
 */
open class SimpleActivityRenderer: MainPageRenderer, LayoutContainer {

    private var activityView: View? = null

    override val containerView: View?
        get() = activityView

    protected fun setContentView(activity: IconPackActivity, view: View) {
        activityView = view
        activity.setContentView(activityView)
    }

    protected fun setContentView(activity: IconPackActivity, resId: Int) {
        setContentView(activity,
            LayoutInflater.from(activity).inflate(resId, null))
    }

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

}