package com.lollipop.iconcore.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.io.*
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
     * 崩溃标示信息
     */
    private const val CRASH_FLAG = "CRASH_FLAG"

    /**
     * 初始化崩溃信息的收集工具
     */
    fun init(context: Context,
             onAsync: Boolean = true,
             uploadCallback: ((File) -> Boolean)?) {
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
                val value = field.get(null)
                val infoValue = if (value is Array<*>) {
                    Arrays.toString(value)
                } else {
                    value?.toString()?:"null"
                }
                deviceInfo.addAttr(field.name, infoValue)
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
        updateCrashFlag()
        return true
    }

    private fun updateCrashFlag() {
        val dir = crashFileDir?:return
        val timeMillis = System.currentTimeMillis().toString()
        val flagFile = File(dir, CRASH_FLAG)
        timeMillis.writeTo(flagFile)
    }

    fun hasCrashFlag(context: Context): Boolean {
        return File(getCrashDir(context), CRASH_FLAG).exists()
    }

    fun resetCrashFlag(context: Context) {
        File(getCrashDir(context), CRASH_FLAG).delete()
    }

    private fun printError(error: Throwable): String {
        val out = ByteArrayOutputStream()
        val print = PrintWriter(out)
        error.printStackTrace(print)
        print.flush()
        return out.toString(Charsets.UTF_8.displayName())
    }

    private fun uploadCrash(uploadCallback: ((File) -> Boolean)?) {
        val callback = uploadCallback?:return
        val fileDir = crashFileDir?:return
        getCrashLog(fileDir, callback)
    }

    fun getCrashLog(context: Context, callback: (File) -> Unit): Boolean {
        return getCrashLog(getCrashDir(context)) {
            callback(it)
            false
        }
    }

    private fun getCrashLog(fileDir: File, callback: (File) -> Boolean): Boolean {
        val listFiles = fileDir.listFiles()?:return false
        val zipHelper = ZipHelper.zipTo(fileDir, "crash")
        if (listFiles.isEmpty()) {
            return false
        }
        var fileSize = 0
        for (file in listFiles) {
            if (!file.exists()) {
                continue
            }
            try {
                zipHelper.addFile(file)
                fileSize++
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        if (fileSize == 0) {
            return false
        }
        zipHelper.startUp {
            if (callback(it)) {
                it.delete()
            }
        }
        return true
    }

    /**
     * 清除所有的崩溃信息
     */
    fun clean(context: Context) {
        removeFile(getCrashDir(context))
    }

    private fun removeFile(dir: File) {
        val files = LinkedList<File>()
        files.add(dir)
        while (files.isNotEmpty()) {
            val first = files.removeFirst()
            if (first.isFile) {
                first.delete()
            } else if (first.isDirectory) {
                val listFiles = first.listFiles()
                if (listFiles != null && listFiles.isNotEmpty()) {
                    // 放回自身，等待子文件删除后删除自身
                    files.addFirst(first)
                    for (file in listFiles) {
                        // 将子文件放在最前，保证先删除子文件
                        files.addFirst(file)
                    }
                } else {
                    first.delete()
                }
            }
        }
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