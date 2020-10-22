package com.lollipop.iconcore.throwable

import java.lang.RuntimeException

/**
 * @author lollipop
 * @date 10/22/20 01:46
 * 图标包异常
 */
class IconPackException(message: String = "unknown", throwable: Throwable? = null):
    RuntimeException(message, throwable)