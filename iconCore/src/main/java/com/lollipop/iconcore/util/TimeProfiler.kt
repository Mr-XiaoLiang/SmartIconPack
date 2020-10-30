package com.lollipop.iconcore.util

/**
 * @author lollipop
 * @date 10/30/20 13:44
 * 耗时分析工具
 */
class TimeProfiler(private val tag: String = CommonUtil.logTag) {

    private val timeList = ArrayList<Long>()

    fun punch() {
        timeList.add(System.currentTimeMillis())
    }

    fun printPoint() {
        val builder = StringBuilder()
        builder.append(tag)
        builder.append("[")
        for (index in timeList.indices) {
            if (index > 0) {
                builder.append(", ")
            }
            builder.append(timeList[index])
        }
        builder.append("]")
        log(builder.toString())
    }

    fun punchAndPrintInterval() {
        punch()
        printInterval()
    }

    fun printInterval() {
        val builder = StringBuilder()
        builder.append(tag)
        builder.append("[")
        if (timeList.size > 1) {
            for (index in 1 until timeList.size) {
                if (index > 1) {
                    builder.append(", ")
                }
                builder.append(timeList[index] - timeList[index - 1])
            }
        }
        builder.append("]")
        log(builder.toString())
    }

}