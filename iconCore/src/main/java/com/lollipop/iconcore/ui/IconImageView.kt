package com.lollipop.iconcore.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

/**
 * @author lollipop
 * @date 10/24/20 20:47
 * Icon的ImageView
 */
class IconImageView(context: Context, attr: AttributeSet?, defStyle: Int):
        AppCompatImageView(context, attr, defStyle), IconView {

    constructor(context: Context, attr: AttributeSet?): this(context, attr, 0)
    constructor(context: Context): this(context, null)

    override fun loadIcon(iconId: Int) {
        setImageResource(iconId)
    }

    override fun loadIcon(iconName: String) {
        setImageResource(resources.getIdentifier(iconName, "drawable", context.packageName))
    }

}