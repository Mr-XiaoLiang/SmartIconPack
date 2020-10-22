package com.lollipop.iconkit

import android.app.Activity
import android.os.Bundle
import com.lollipop.iconcore.provider.MainPageRenderer

/**
 * @author lollipop
 * @date 10/22/20 20:34
 * 主页
 */
class MainActivity: MainPageRenderer{

    override fun onCreate(activity: Activity, savedInstanceState: Bundle?) {
        activity.setContentView(R.layout.activity_main)
    }

    override fun onStart(activity: Activity) {
        
    }

    override fun onStop(activity: Activity) {
        
    }

    override fun onResume(activity: Activity) {
        
    }

    override fun onPause(activity: Activity) {
        
    }

    override fun onRestart(activity: Activity) {
        
    }

    override fun onDestroy(activity: Activity) {
        
    }

    override fun onSaveInstanceState(activity: Activity, outState: Bundle) {
        
    }

    override fun onRestoreInstanceState(activity: Activity, savedInstanceState: Bundle) {
        
    }

}