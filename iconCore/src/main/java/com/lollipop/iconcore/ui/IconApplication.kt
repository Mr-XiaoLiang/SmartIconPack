package com.lollipop.iconcore.ui

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.lollipop.iconcore.provider.MainPageProvider

/**
 * @author lollipop
 * @date 10/22/20 01:27
 * 图标包的引用上下文，他主要用于绑定主页Activity的最终实现类
 */
open class IconApplication: Application(), Application.ActivityLifecycleCallbacks {

    private var mainPageProvider: MainPageProvider? = null

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
    }

    fun bindMainPageProvider(provider: MainPageProvider) {
        this.mainPageProvider = provider
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        if (activity is IconPackActivity) {
            mainPageProvider?.let {
                val renderer = it.createRenderer()
                activity.bindRenderer(renderer)
                renderer.onCreate(activity, bundle)
            }
        }
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }


}