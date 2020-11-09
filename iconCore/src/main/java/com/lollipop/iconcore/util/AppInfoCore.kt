package com.lollipop.iconcore.util

import android.content.*
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.text.TextUtils
import com.lollipop.iconcore.ui.EmptyDrawable
import com.lollipop.iconcore.ui.IconHelper.Companion.fullName
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author lollipop
 * @date 10/30/20 14:23
 * 应用信息的核心类
 */
object AppInfoCore: BroadcastReceiver() {

    private const val LOCK_KEY = "AppInfoCore"

    /**
     * 应用原始信息
     */
    private val appResolveInfo = ArrayList<AppResolveInfo>()

    /**
     * 应用列表变化的回调函数集合
     */
    private val appChangeCallbackList = ArrayList<AppChangeListenerWrapper>()

    /**
     * 应用的加载完成集合
     */
    private val appLoadedCallbackList = LinkedList<AppLoadPendingTask>()

    /**
     * 应用是否加载完成
     */
    @Volatile
    var isLoaded = false
        private set

    /**
     * 应用数量
     */
    val appCount: Int
        get() {
            return appResolveInfo.size
        }

    @Volatile
    private var isLoading = false

    private val EMPTY_LABEL = ""

    private val EMPTY_ICON = EmptyDrawable()

    private var isRegisterReceiver = false

    fun init(context: Context, onLoaded: () -> Unit) {
        init(context, AppLoadPendingTaskWrapper(onLoaded))
    }

    /**
     * 初始化
     */
    fun init(context: Context, loadedCallback: AppLoadPendingTask?) {
        if (isLoaded) {
            loadedCallback?.onAppLoaded()
            while (appLoadedCallbackList.isNotEmpty()) {
                val c = appLoadedCallbackList.removeFirst()
                c.onAppLoaded()
            }
            return
        }
        if (loadedCallback != null) {
            appLoadedCallbackList.addLast(loadedCallback)
        }
        if (!isRegisterReceiver) {
            registerReceiver(context)
            isRegisterReceiver = true
        }
        reload(context)
    }

    private fun reload(context: Context) {
        if (isLoading) {
            return
        }
        isLoading = true
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val appList = pm.queryIntentActivities(mainIntent, 0)
        appResolveInfo.clear()
        for (info in appList) {
            appResolveInfo.add(AppResolveInfo(info))
        }
        isLoaded = true
        isLoading = false
        while (appLoadedCallbackList.isNotEmpty()) {
            val c = appLoadedCallbackList.removeFirst()
            c.onAppLoaded()
        }
    }

    @JvmSynthetic
    fun getLabel(context: Context, name: ComponentName): CharSequence {
        synchronized(LOCK_KEY) {
            for (info in appResolveInfo) {
                if (name.packageName == info.pkgName && name.className == info.clsName) {
                    if (TextUtils.isEmpty(info.label)) {
                        val label = info.resolveInfo.loadLabel(context.packageManager)
                        info.label = label
                    }
                    return info.label
                }
            }
            return EMPTY_LABEL
        }
    }

    @JvmSynthetic
    fun getLabel(context: Context, app: ResolveInfo): CharSequence {
        synchronized(LOCK_KEY) {
            for (info in appResolveInfo) {
                if (info.resolveInfo == app) {
                    if (TextUtils.isEmpty(info.label)) {
                        val label = info.resolveInfo.loadLabel(context.packageManager)
                        info.label = label
                    }
                    return info.label
                }
            }
            return EMPTY_LABEL
        }
    }

    @JvmSynthetic
    fun loadIcon(context: Context, name: ComponentName): Drawable {
        synchronized(LOCK_KEY) {
            for (info in appResolveInfo) {
                if (name.packageName == info.pkgName && name.className == info.clsName) {
                    return info.resolveInfo.loadIcon(context.packageManager)
                }
            }
            return EMPTY_ICON
        }
    }

    operator fun get(index: Int): ResolveInfo {
        return appResolveInfo[index].resolveInfo
    }

    fun forEach(run: (Int, ResolveInfo) -> Unit) {
        for (index in appResolveInfo.indices) {
            run(index, appResolveInfo[index].resolveInfo)
        }
    }

    fun addAppChangeListener(listener: AppChangeListener) {
        for (callback in appChangeCallbackList) {
            if (callback.callback == listener) {
                return
            }
        }
        appChangeCallbackList.add(AppChangeListenerWrapper(listener))
    }

    fun removeAppChangeListener(listener: AppChangeListener) {
        val iterator = appChangeCallbackList.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.callback == listener) {
                iterator.remove()
            }
        }
    }

    private class AppResolveInfo(
        val resolveInfo: ResolveInfo,
        var label: CharSequence = "") {
        val pkgName: String = resolveInfo.activityInfo.packageName
        val clsName = resolveInfo.activityInfo.name.fullName(pkgName)
    }

    interface AppLoadPendingTask {
        fun onAppLoaded()
    }

    interface AppChangeListener {
        fun onAppListChange()
    }

    private fun registerReceiver(context: Context) {
        context.applicationContext.registerReceiver(this, IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package");
        })
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        context?:return
        intent?:return
        when(intent.action) {
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REMOVED -> {
                for (callback in appChangeCallbackList) {
                    appLoadedCallbackList.addLast(callback)
                }
                reload(context)
            }
        }
    }

    private class AppChangeListenerWrapper(
        val callback: AppChangeListener): AppLoadPendingTask {
        override fun onAppLoaded() {
            callback.onAppListChange()
        }
    }

    private class AppLoadPendingTaskWrapper(
        private val onLoaded: () -> Unit): AppLoadPendingTask {
        override fun onAppLoaded() {
            onLoaded.invoke()
        }
    }

}