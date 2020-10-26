package com.lollipop.iconcore.util

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor
import java.util.concurrent.Executors


/**
 * @author lollipop
 * @date 2020/6/9 23:17
 * 通用的全局工具方法
 */
object CommonUtil {
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

    fun print(value: Array<out Any>): String {
        if (value.isEmpty()) {
            return ""
        }
        val iMax = value.size - 1
        val b = StringBuilder()
        var i = 0
        while (true) {
            b.append(value[i].toString())
            if (i == iMax)  {
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
            private val run: T.() -> Unit) {

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
        noinline run: T.() -> Unit) = CommonUtil.Task(this, err, run)

/**
 * 异步任务
 */
inline fun <reified T> T.doAsync(
        noinline err: ((Throwable) -> Unit) = {},
        noinline run: T.() -> Unit): CommonUtil.Task<T> {
    val task = task(err, run)
    CommonUtil.doAsync(task)
    return task
}

/**
 * 主线程
 */
inline fun <reified T> T.onUI(
        noinline err: ((Throwable) -> Unit) = {},
        noinline run: T.() -> Unit): CommonUtil.Task<T> {
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
        noinline run: T.() -> Unit): CommonUtil.Task<T> {
    val task = task(err, run)
    CommonUtil.delay(delay, task)
    return task
}

inline fun <reified T: Any> T.log(vararg value: Any) {
    Log.d("LIconKit", "${this.javaClass.simpleName} -> ${CommonUtil.print(value)}")
}

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

fun Int.alpha(a: Int): Int {
    return this and 0xFFFFFF or ((a % 255) shl 24)
}

fun Int.alpha(f: Float): Int {
    val a = this shr 24
    return this.alpha(a)
}

fun Float.toDip(context: Context): Float {
    return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, this,
            context.resources.displayMetrics)
}

fun Float.toSp(context: Context): Float {
    return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, this,
            context.resources.displayMetrics)
}

fun Float.toDip(view: View): Float {
    return this.toDip(view.context)
}

fun Float.toSp(view: View): Float {
    return this.toSp(view.context)
}

fun Int.toDip(view: View): Float {
    return this.toFloat().toDip(view.context)
}

fun Int.toDip(context: Context): Float {
    return this.toFloat().toDip(context)
}

fun Int.findColor(context: Context): Int {
    try {
        return ContextCompat.getColor(context, this)
    } catch (e: Throwable) {
    }
    return Color.WHITE
}

fun Int.findColor(view: View): Int {
    return this.findColor(view.context)
}

fun Int.zeroTo(value: () -> Int): Int {
    return if (this == 0) {
        value()
    } else {
        this
    }
}

fun Float.range(min: Float, max: Float): Float {
    if (this < min) {
        return min
    }
    if (this > max) {
        return max
    }
    return this
}

fun String.parseColor(): Int {
    val value = this
    if (value.isEmpty()) {
        return 0
    }
    return when(value.length) {
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
            Color.rgb((r + r).toInt(16),
                    (g + g).toInt(16),
                    (b + b).toInt(16))
        }
        4, 5 -> {
            val a = value.substring(0, 1)
            val r = value.substring(1, 2)
            val g = value.substring(2, 3)
            val b = value.substring(3, 4)
            Color.argb((a + a).toInt(16),
                    (r + r).toInt(16),
                    (g + g).toInt(16),
                    (b + b).toInt(16))
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

inline fun <reified T: EditText> T.onActionDone(noinline run: T.() -> Unit) {
    this.setOnEditorActionListener { _, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_DONE
                || event.keyCode == KeyEvent.KEYCODE_ENTER) {
            run.invoke(this)
            true
        } else {
            false
        }
    }
}

fun String.tryInt(def: Int): Int {
    try {
        if (TextUtils.isEmpty(this)) {
            return def
        }
        return this.toInt()
    } catch (e: Throwable) { }
    return def
}

fun View.getColor(id: Int): Int {
    return ContextCompat.getColor(this.context, id)
}

fun Int.range(min: Int, max: Int): Int {
    if (this < min) {
        return min
    }
    if (this > max) {
        return max
    }
    return this
}

fun Context.findDrawableId(name: String): Int {
    var icon = findId(name, "drawable")
    if (icon != 0) {
        return icon
    }
    icon = findId(name, "mipmap")
    return icon
}

fun Context.findId(name: String, type: String): Int {
    return resources.getIdentifier(name, type, packageName)
}