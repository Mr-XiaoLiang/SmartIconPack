package com.lollipop.iconcore.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author lollipop
 * @date 10/28/20 04:32
 * 保存Icon的工具
 */
class IconSaveHelper (
    private val iconWidth: Int,
    private val iconHeight: Int = iconWidth) {

    companion object {
        const val SUFFIX = ".png"
    }

    private val iconList = LinkedList<Icon>()
    private val fileList = ArrayList<File>()

    /**
     * 添加一个图标到队列中
     * 每个icon只能保存一次
     */
    fun add(context: Context, id: Int, name: String): IconSaveHelper {
        val drawable = ContextCompat.getDrawable(context, id)?:return this
        iconList.addLast(Icon(drawable, name))
        return this
    }

    /**
     * 添加一个drawable对象到队列中
     * 每个icon只能保存一次
     */
    fun add(drawable: Drawable, name: String): IconSaveHelper {
        val newDrawable = drawable.constantState?.newDrawable()?:drawable
        iconList.addLast(Icon(newDrawable, name))
        return this
    }

    /**
     * 检索队列中的icon，并且保存到指定的文件夹中
     * 每个icon只能保存一次
     * 注意，这是一个耗时操作，不建议在主线程中执行
     */
    fun saveTo(dir: File) {
        if (iconList.isEmpty()) {
            return
        }
        try {
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val bitmap = Bitmap.createBitmap(iconWidth, iconHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            while (iconList.isNotEmpty()) {
                val icon = iconList.removeFirst()
                var outputStream: OutputStream? = null
                try {
                    val iconFile = File(dir, icon.name + SUFFIX)
                    outputStream = FileOutputStream(iconFile)
                    val drawable = icon.drawable
                    drawable.setBounds(0, 0, iconWidth, iconHeight)
                    // 绘制前清理历史内容
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                    drawable.draw(canvas)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                    fileList.add(iconFile)
                } catch (e: Throwable) {
                    e.printStackTrace()
                } finally {
                    outputStream?.close()
                }
            }
            bitmap.recycle()
        } catch (ee: Throwable) {
            ee.printStackTrace()
        }
    }

    /**
     * 获取所有已保存的图片
     * 如果多次使用保存，那么每次新增的图片都会累加在其中
     */
    fun getFiles(): ArrayList<File> {
        return fileList
    }

    private data class Icon(val drawable: Drawable, val name: String)

}