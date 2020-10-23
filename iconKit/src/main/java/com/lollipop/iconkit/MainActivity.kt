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

    override fun onCreate(target: Activity, savedInstanceState: Bundle?) {
        target.setContentView(R.layout.activity_main)
    }

    override fun onStart(target: Activity) {
        
    }

    override fun onStop(target: Activity) {
        
    }

    override fun onResume(target: Activity) {
        
    }

    override fun onPause(target: Activity) {
        
    }

    override fun onRestart(target: Activity) {
        
    }

    override fun onDestroy(target: Activity) {
        
    }

    override fun onSaveInstanceState(target: Activity, outState: Bundle) {
        
    }

    override fun onRestoreInstanceState(target: Activity, savedInstanceState: Bundle) {
        
    }

}