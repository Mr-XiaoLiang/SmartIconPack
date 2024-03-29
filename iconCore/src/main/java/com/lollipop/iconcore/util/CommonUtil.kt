package com.lollipop.iconcore.util

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import java.io.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.Throwable as Throwable


/**
 * @author lollipop
 * @date 2020/6/9 23:17
 * 通用的全局工具方法
 */
object CommonUtil {

    /**
     * 全局的打印日志的关键字
     */
    var logTag = "LIcon"

    /**
     * 异步线程池
     */
    private val threadPool: Executor by lazy {
        Executors.newScheduledThreadPool(2)
    }

    /**
     * 主线程的handler
     */
    private val mainThread: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    /**
     * 异步任务
     */
    fun <T> doAsync(task: Task<T>) {
        threadPool.execute(task.runnable)
    }

    /**
     * 主线程
     */
    fun <T> onUI(task: Task<T>) {
        mainThread.post(task.runnable)
    }

    /**
     * 延迟任务
     */
    fun <T> delay(delay: Long, task: Task<T>) {
        mainThread.postDelayed(task.runnable, delay)
    }

    /**
     * 移除任务
     */
    fun <T> remove(task: Task<T>) {
        mainThread.removeCallbacks(task.runnable)
    }

    /**
     * 将一组对象打印合并为一个字符串
     */
    fun print(value: Array<out Any>): String {
        if (value.isEmpty()) {
            return ""
        }
        val iMax = value.size - 1
        val b = StringBuilder()
        var i = 0
        while (true) {
            b.append(value[i].toString())
            if (i == iMax) {
                return b.toString()
            }
            b.append(" ")
            i++
        }
    }

    /**
     * 包装的任务类
     * 包装的意义在于复用和移除任务
     * 由于Handler任务可能造成内存泄漏，因此在生命周期结束时，有必要移除任务
     * 由于主线程的Handler使用了全局的对象，移除不必要的任务显得更为重要
     * 因此包装了任务类，以任务类为对象来保留任务和移除任务
     */
    class Task<T>(
        private val target: T,
        private val err: ((Throwable) -> Unit) = {},
        private val run: T.() -> Unit
    ) {

        val runnable = Runnable {
            try {
                run(target)
            } catch (e: Throwable) {
                err(e)
            }
        }

        fun cancel() {
            remove(this)
        }

        fun run() {
            doAsync(this)
        }

        fun sync() {
            onUI(this)
        }

        fun delay(time: Long) {
            delay(time, this)
        }
    }

}

/**
 * 用于创建一个任务对象
 */
inline fun <reified T> T.task(
    noinline err: ((Throwable) -> Unit) = {},
    noinline run: T.() -> Unit
) = CommonUtil.Task(this, err, run)

/**
 * 异步任务
 */
inline fun <reified T> T.doAsync(
    noinline err: ((Throwable) -> Unit) = {},
    noinline run: T.() -> Unit
): CommonUtil.Task<T> {
    val task = task(err, run)
    CommonUtil.doAsync(task)
    return task
}

/**
 * 主线程
 */
inline fun <reified T> T.onUI(
    noinline err: ((Throwable) -> Unit) = {},
    noinline run: T.() -> Unit
): CommonUtil.Task<T> {
    val task = task(err, run)
    CommonUtil.onUI(task)
    return task
}

/**
 * 延迟任务
 */
inline fun <reified T> T.delay(
    delay: Long,
    noinline err: ((Throwable) -> Unit) = {},
    noinline run: T.() -> Unit
): CommonUtil.Task<T> {
    val task = task(err, run)
    CommonUtil.delay(delay, task)
    return task
}

/**
 * 一个全局的打印Log的方法
 */
inline fun <reified T : Any> T.log(vararg value: Any) {
    Log.d(CommonUtil.logTag, "${this.javaClass.simpleName} -> ${CommonUtil.print(value)}")
}


/**
 * 对一个输入框关闭键盘
 */
fun EditText.closeBoard() {
    //拿到InputMethodManager
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.let { imm ->
        //如果window上view获取焦点 && view不为空
        if (imm.isActive) {
            //拿到view的token 不为空
            windowToken?.let { token ->
                imm.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
    }
}

/**
 * 对一个activity关闭键盘
 */
fun Activity.closeBoard() {
    //拿到InputMethodManager
    (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.let { imm ->
        //如果window上view获取焦点 && view不为空
        if (imm.isActive) {
            currentFocus?.let { focus ->
                //拿到view的token 不为空
                focus.windowToken?.let { token ->
                    //表示软键盘窗口总是隐藏，除非开始时以SHOW_FORCED显示。
                    imm.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS)
                }
            }
        }
    }
}

/**
 * 对一个颜色值设置它的透明度
 * 只支持#AARRGGBB格式排列的颜色值
 */
fun Int.alpha(a: Int): Int {
    return this and 0xFFFFFF or ((a % 255) shl 24)
}

/**
 * 以浮点数的形式，以当前透明度为基础，
 * 调整颜色值的透明度
 */
fun Int.alpha(f: Float): Int {
    return this.alpha(((this shr 24) * f).toInt().range(0, 255))
}

/**
 * 将一个浮点数，以dip为单位转换为对应的像素值
 */
fun Float.toDip(context: Context): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this,
        context.resources.displayMetrics
    )
}

/**
 * 将一个浮点数，以sp为单位转换为对应的像素值
 */
fun Float.toSp(context: Context): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP, this,
        context.resources.displayMetrics
    )
}

/**
 * 将一个浮点数，以dip为单位转换为对应的像素值
 */
fun Float.toDip(view: View): Float {
    return this.toDip(view.context)
}

/**
 * 将一个浮点数，以sp为单位转换为对应的像素值
 */
fun Float.toSp(view: View): Float {
    return this.toSp(view.context)
}

/**
 * 将一个整数，以dip为单位转换为对应的像素值
 */
fun Int.toDip(view: View): Float {
    return this.toFloat().toDip(view.context)
}

/**
 * 将一个整数，以dip为单位转换为对应的像素值
 */
fun Int.toDip(context: Context): Float {
    return this.toFloat().toDip(context)
}

/**
 * 将一个整数作为id来寻找对应的颜色值，
 * 如果找不到或者发生了异常，那么将会返回白色
 */
fun Int.findColor(context: Context): Int {
    try {
        return ContextCompat.getColor(context, this)
    } catch (e: Throwable) {
    }
    return Color.WHITE
}

/**
 * 将一个整数作为id来寻找对应的颜色值，
 * 如果找不到或者发生了异常，那么将会返回白色
 */
fun Int.findColor(view: View): Int {
    return this.findColor(view.context)
}

/**
 * 如果当前整数是0，那么获取回调函数中的值作为返回值
 * 否则返回当前
 */
fun Int.zeroTo(value: () -> Int): Int {
    return if (this == 0) {
        value()
    } else {
        this
    }
}

/**
 * 对一个浮点数做范围约束
 */
fun Float.range(min: Float, max: Float): Float {
    if (this < min) {
        return min
    }
    if (this > max) {
        return max
    }
    return this
}

/**
 * 将一个字符串转换为颜色值
 * 只接受1～8位0～F之间的字符
 */
fun String.parseColor(): Int {
    val value = this
    if (value.isEmpty()) {
        return 0
    }
    return when (value.length) {
        1 -> {
            val v = (value + value).toInt(16)
            Color.rgb(v, v, v)
        }
        2 -> {
            val v = value.toInt(16)
            Color.rgb(v, v, v)
        }
        3 -> {
            val r = value.substring(0, 1)
            val g = value.substring(1, 2)
            val b = value.substring(2, 3)
            Color.rgb(
                (r + r).toInt(16),
                (g + g).toInt(16),
                (b + b).toInt(16)
            )
        }
        4, 5 -> {
            val a = value.substring(0, 1)
            val r = value.substring(1, 2)
            val g = value.substring(2, 3)
            val b = value.substring(3, 4)
            Color.argb(
                (a + a).toInt(16),
                (r + r).toInt(16),
                (g + g).toInt(16),
                (b + b).toInt(16)
            )
        }
        6, 7 -> {
            val r = value.substring(0, 2).toInt(16)
            val g = value.substring(2, 4).toInt(16)
            val b = value.substring(4, 6).toInt(16)
            Color.rgb(r, g, b)
        }
        8 -> {
            val a = value.substring(0, 2).toInt(16)
            val r = value.substring(2, 4).toInt(16)
            val g = value.substring(4, 6).toInt(16)
            val b = value.substring(6, 8).toInt(16)
            Color.argb(a, r, g, b)
        }
        else -> {
            Color.WHITE
        }
    }
}

/**
 * 对一个输入框做回车事件监听
 */
inline fun <reified T : EditText> T.onActionDone(noinline run: T.() -> Unit) {
    this.imeOptions = EditorInfo.IME_ACTION_DONE
    this.setOnEditorActionListener { _, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_DONE
            || event.keyCode == KeyEvent.KEYCODE_ENTER
        ) {
            run.invoke(this)
            true
        } else {
            false
        }
    }
}

/**
 * 尝试将一个字符串转换为整形，
 * 如果发生了异常或者为空，那么将会返回默认值
 */
fun String.tryInt(def: Int): Int {
    try {
        if (TextUtils.isEmpty(this)) {
            return def
        }
        return this.toInt()
    } catch (e: Throwable) {
    }
    return def
}

/**
 * 以一个View为res来源获取指定id的颜色值
 */
fun View.getColor(id: Int): Int {
    return ContextCompat.getColor(this.context, id)
}

/**
 * 一个整形的范围约束
 */
fun Int.range(min: Int, max: Int): Int {
    if (this < min) {
        return min
    }
    if (this > max) {
        return max
    }
    return this
}

/**
 * 从Context中尝试通过名字获取一个drawable的id
 */
fun Context.findDrawableId(name: String): Int {
    var icon = findId(name, "drawable")
    if (icon != 0) {
        return icon
    }
    icon = findId(name, "mipmap")
    return icon
}

/**
 * 从Context中尝试通过名字获取一个指定类型的资源id
 */
fun Context.findId(name: String, type: String): Int {
    return resources.getIdentifier(name, type, packageName)
}

/**
 * 尝试通过一个id获取对应的资源名
 */
fun Context.findName(id: Int): String {
    return resources.getResourceName(id)
}

/**
 * 从context中获取当前应用的版本名称
 */
fun Context.versionName(): String {
    return packageManager.getPackageInfo(packageName, 0).versionName
}

/**
 * 从context中获取当前应用的版本名称
 */
fun Context.versionCode(): Long {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        return packageInfo.longVersionCode
    }
    return packageInfo.versionCode.toLong()
}

/**
 * 创建一个时间性能工具
 * 用于检测某些代码的运行时间
 */
inline fun <reified T : Any> T.timeProfiler(): TimeProfiler {
    val profiler = TimeProfiler(this.javaClass.simpleName)
    profiler.punch()
    return profiler
}

/**
 * 将一段文本写入一个文件中
 * 它属于IO操作，这是一个耗时的任务，
 * 需要在子线程中执行
 */
fun String.writeTo(file: File) {
    try {
        if (file.exists()) {
            file.delete()
        } else {
            file.parentFile?.mkdirs()
        }
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        val buffer = ByteArray(2048)
        try {
            inputStream = ByteArrayInputStream(this.toByteArray(Charsets.UTF_8))
            outputStream = FileOutputStream(file)
            var length = inputStream.read(buffer)
            while (length >= 0) {
                outputStream.write(buffer, 0, length)
                length = inputStream.read(buffer)
            }
            outputStream.flush()
        } catch (ee: Throwable) {
            ee.printStackTrace()
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}


inline fun <reified T : ViewBinding> Activity.lazyBind(): Lazy<T> = lazy { bind() }

inline fun <reified T : ViewBinding> Fragment.lazyBind(): Lazy<T> = lazy { bind() }

inline fun <reified T : ViewBinding> View.lazyBind(): Lazy<T> = lazy { bind() }

inline fun <reified T : ViewBinding> Activity.bind(): T {
    return this.layoutInflater.bind()
}

inline fun <reified T : ViewBinding> Fragment.bind(): T {
    return this.layoutInflater.bind()
}

inline fun <reified T : ViewBinding> View.bind(): T {
    return LayoutInflater.from(this.context).bind()
}

inline fun <reified T : ViewBinding> LayoutInflater.bind(): T {
    val layoutInflater: LayoutInflater = this
    val bindingClass = T::class.java
    val inflateMethod = bindingClass.getMethod("inflate", LayoutInflater::class.java)
    val invokeObj = inflateMethod.invoke(null, layoutInflater)
    if (invokeObj is T) {
        return invokeObj
    }
    throw InflateException("Cant inflate ViewBinding ${bindingClass.name}")
}

inline fun <reified T : ViewBinding> View.withThis(inflate: Boolean = false): Lazy<T> = lazy {
    val bindingClass = T::class.java
    val view: View = this
    if (view is ViewGroup && inflate) {
        val bindMethod = bindingClass.getMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.javaPrimitiveType
        )
        val bindObj = bindMethod.invoke(null, LayoutInflater.from(context), view, true)
        if (bindObj is T) {
            return@lazy bindObj
        }
    } else {
        val bindMethod = bindingClass.getMethod(
            "bind",
            View::class.java
        )
        val bindObj = bindMethod.invoke(null, view)
        if (bindObj is T) {
            return@lazy bindObj
        }
    }
    throw InflateException("Cant inflate ViewBinding ${bindingClass.name}")
}

inline fun <T : Any> T.tryDo(
    noinline error: ((Throwable) -> Unit)? = null,
    run: T.() -> Unit,
) {
    try {
        run()
    } catch (e: Throwable) {
        error?.invoke(e)
    }
}

