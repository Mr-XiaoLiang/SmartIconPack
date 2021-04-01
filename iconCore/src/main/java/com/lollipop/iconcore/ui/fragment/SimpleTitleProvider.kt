package com.lollipop.iconcore.ui.fragment

import android.content.res.Resources
import com.lollipop.iconcore.util.tryDo
import java.lang.ref.WeakReference

/**
 * @author lollipop
 * @date 4/1/21 23:02
 * 一个简单的标题提供器
 */
class SimpleTitleProvider(
    /**
     * 提供字符串的资源提供者
     */
    resources: Resources,
    /**
     * 标题的id数组
     */
    private val titleResArray: IntArray
) : TitleProvider {

    private val resourcesReference = WeakReference(resources)

    override fun getTitle(position: Int): CharSequence {
        if (position < 0 || position >= titleResArray.size) {
            return ""
        }
        tryDo {
            return resourcesReference.get()?.getString(titleResArray[position]) ?: ""
        }
        return ""
    }

}