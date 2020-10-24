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
        if (iconId == 0) {
            setImageDrawable(null)
            return
        }
        setImageResource(iconId)
    }

    override fun loadIcon(iconName: String) {
        setImageResource(resources.getIdentifier(iconName, "drawable", context.packageName))
    }

    fun load(icon: IconHelper.IconInfo, def: Int = 0) {
        if (icon.resId == 0) {
            if (def != 0) {
                setImageResource(def)
            } else {
                setImageDrawable(null)
            }
            return
        }
        setImageResource(icon.resId)
    }

    fun load(icon: IconHelper.AppInfo, iconIndex: Int = 0) {
        if (icon.iconPack.isEmpty()) {
            setImageDrawable(icon.srcIcon)
            return
        }
        setImageResource(icon.iconPack[iconIndex])
    }

}