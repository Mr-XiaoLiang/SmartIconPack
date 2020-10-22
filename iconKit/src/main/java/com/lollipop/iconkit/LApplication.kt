package com.lollipop.iconkit

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.lollipop.iconcore.IconPackCore
import com.lollipop.iconcore.provider.MainPageProvider
import com.lollipop.iconcore.provider.MainPageRenderer
import com.lollipop.iconcore.ui.IconApplication

/**
 * @author lollipop
 * @date 10/22/20 16:29
 */
class LApplication: IconApplication() {

    override fun onCreate() {
        super.onCreate()
        IconPackCore.init(this, object: MainPageProvider {
            override fun createRenderer(): MainPageRenderer {
                return object : MainPageRenderer{
                    override fun onCreate(activity: Activity, savedInstanceState: Bundle?) {
                        activity.setContentView(View(activity).apply {
                            setBackgroundColor(Color.RED)
                        })
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

                    override fun onRestoreInstanceState(
                        activity: Activity,
                        savedInstanceState: Bundle
                    ) {
                    }

                }
            }
        })
    }

}