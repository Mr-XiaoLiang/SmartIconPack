package com.lollipop.iconcore.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintWriter
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author lollipop
 * @date 11/7/20 11:06
 * Crash的收集类
 */
object CrashHandler {

    private const val CRASH_DIR_NAME = "lCrash"

    /**
     * 崩溃信息的收集监听器，它不能包含上下文信息
     * 因为可能造成内存泄漏
     */
    private val myExceptionHandler: ExceptionHandler by lazy {
        ExceptionHandler(::onCrash)
    }

    /**
     * 崩溃信息的根节点
     * 输出时，将会使用到它
     */
    private val crashRoot = XmlBuilder.create("Crash")

    /**
     * 崩溃信息的设备信息节点
     * 它只是一个子节点，单独出来的原因在于简化使用过程
     */
    private val deviceInfo = crashRoot.addChild("device")

    /**
     * 崩溃信息中的错误信息节点
     * 它只是一个子节点，单独出来的原因在于简化使用过程
     */
    private val errorInfo = crashRoot.addChild("msg")

    /**
     * 崩溃信息中的线程信息节点
     * 它只是一个子节点，单独出来的原因在于简化使用过程
     */
    private val threadInfo = crashRoot.addChild("thread")

    /**
     * 崩溃信息监听器
     * 可以在崩溃时，加入一些自己的信息
     */
    private var onCrashListener: OnCrashListener? = null

    /**
     * 崩溃信息的缓存目录
     */
    private var crashFileDir: File? = null

    /**
     * 崩溃信息的文件名
     */
    private const val CRASH_FILE_NAME = "yyyyMMddHHmmssSSS"

    /**
     * 初始化崩溃信息的收集工具
     */
    fun init(context: Context,
             uploadCallback: (File) -> Unit,
             onAsync: Boolean = true) {
        crashFileDir = getCrashDir(context)
        initDeviceInfo(context)
        //获取系统默认的UncaughtException处理器
        val handler = Thread.getDefaultUncaughtExceptionHandler()
        // 检查Handler，避免重复处理
        if (handler !is ExceptionHandler) {
            myExceptionHandler.setDefHandler(handler)
            //设置该CrashHandler为程序的默认处理器
            Thread.setDefaultUncaughtExceptionHandler(myExceptionHandler)
        }
        if (onAsync) {
            doAsync {
                uploadCrash(uploadCallback)
            }
        } else {
            uploadCrash(uploadCallback)
        }
    }

    /**
     * 获取崩溃信息的文件夹
     * 它存储于缓存文件夹，用户清理缓存时，将会清理崩溃日志
     * @param context 应用上下文
     */
    fun getCrashDir(context: Context): File {
        return File(context.cacheDir, CRASH_DIR_NAME)
    }

    private fun initDeviceInfo(context: Context) {
        deviceInfo.clean()
        // App的版本信息
        try {
            val packageName = context.packageName
            deviceInfo.addAttr("packageName", packageName)
            val pi = context.packageManager.getPackageInfo(
                packageName, PackageManager.GET_ACTIVITIES
            )
            if (pi != null) {
                val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pi.longVersionCode
                } else {
                    pi.versionCode
                }
                val name = pi.versionName ?: "null"
                deviceInfo.addAttr("appVersionName", name)
                deviceInfo.addAttr("appVersionCOde", code.toString())
            }
        } catch (e: Throwable) { }

        // 设备信息
        val fields: Array<Field> = Build::class.java.declaredFields
        for (field in fields) {
            try {
                field.isAccessible = true
                deviceInfo.addAttr(field.name, field.get(null)?.toString()?:"null")
            } catch (e: Exception) {
            }
        }
    }

    private fun onCrash(thread: Thread, error: Throwable): Boolean {
        try {
            threadInfo.clean()
            threadInfo.setText(thread.toString())
            errorInfo.clean()
            errorInfo.setText(printError(error))
            val otherInfo = onCrashListener?.onCrash(thread, error)
            if (otherInfo != null) {
                crashRoot.addChild(otherInfo)
            }
            return saveCrash()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return false
    }

    private fun saveCrash(): Boolean {
        val dir = crashFileDir?:return false
        val sdf = SimpleDateFormat(CRASH_FILE_NAME, Locale.getDefault())
        val name = sdf.format(Date(System.currentTimeMillis()))
        crashRoot.writeTo(File(dir, "$name.xml"))
        return true
    }

    private fun printError(error: Throwable): String {
        val out = ByteArrayOutputStream()
        val print = PrintWriter(out)
        error.printStackTrace(print)
        return out.toString(Charsets.UTF_8.displayName())
    }

    private fun uploadCrash(uploadCallback: (File) -> Unit) {
        val fileDir = crashFileDir?:return
        val listFiles = fileDir.listFiles()?:return
        val zipHelper = ZipHelper.zipTo(fileDir, "crash")
        for (file in listFiles) {
            if (!file.exists()) {
                continue
            }
            try {
                zipHelper.addFile(file)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        zipHelper.startUp {
            uploadCallback(it)
            it.delete()
        }
    }

    /**
     * 清除所有的崩溃信息
     */
    fun clean() {
        crashFileDir?.delete()
    }

    private class ExceptionHandler(
        private val onCrash: (Thread, Throwable) -> Boolean,
        private var defHandler: Thread.UncaughtExceptionHandler? = null
    ) :
        Thread.UncaughtExceptionHandler {

        fun setDefHandler(handler: Thread.UncaughtExceptionHandler?) {
            this.defHandler = handler
        }

        override fun uncaughtException(t: Thread, e: Throwable) {
            if (!onCrash.invoke(t, e)) {
                defHandler?.uncaughtException(t, e)
            } else {
                android.os.Process.killProcess(android.os.Process.myPid())
            }
        }
    }

    interface OnCrashListener {
        fun onCrash(thread: Thread, error: Throwable): XmlBuilder?
    }

}