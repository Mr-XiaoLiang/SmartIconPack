package com.lollipop.iconkit

import com.lollipop.iconcore.IconPackCore
import com.lollipop.iconcore.provider.MainPageProvider
import com.lollipop.iconcore.provider.MainPageRenderer
import com.lollipop.iconcore.ui.IconApplication

/**
 * @author lollipop
 * @date 10/22/20 16:29
 */
open class LApplication: IconApplication(), MainPageProvider {

    override fun onCreate() {
        super.onCreate()
        IconPackCore.init(this)
    }

    override fun createRenderer(): MainPageRenderer {
        return MainActivity()
    }

}