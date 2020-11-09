package com.lollipop.iconkit.dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.iconcore.ui.IconHelper
import com.lollipop.iconcore.ui.IconImageView
import com.lollipop.iconkit.R
import com.lollipop.iconkit.dialog.base.InnerDialogProvider

/**
 * @author lollipop
 * @date 11/9/20 22:44
 * 新的Icon数据的对话框
 */
class NewIconDialog(private val data: List<Any>): InnerDialogProvider() {

    override val layoutId: Int
        get() = R.layout.kit_dialog_new_icon

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        val listView: RecyclerView = view.findViewById(R.id.iconList)
        val adapter = NewIconAdapter(data)
        val column = view.resources.getInteger(R.integer.app_list_column)
        listView.layoutManager = GridLayoutManager(
            view.context, column, RecyclerView.VERTICAL, false)
        listView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private class NewIconAdapter(private val data: List<Any>): RecyclerView.Adapter<NewIconHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewIconHolder {
            return NewIconHolder.create(parent)
        }

        override fun onBindViewHolder(holder: NewIconHolder, position: Int) {
            val any = data[position]
            if (any is IconHelper.AppInfo) {
                holder.bind(any)
            } else if (any is IconHelper.IconInfo) {
                holder.bind(any)
            }
        }

        override fun getItemCount(): Int {
            return data.size
        }

    }

    private class NewIconHolder private constructor(
        view: View): RecyclerView.ViewHolder(view) {

        companion object {
            fun create(group: ViewGroup): NewIconHolder {
                return NewIconHolder(
                    LayoutInflater.from(group.context)
                        .inflate(R.layout.kit_item_icon, group, false))
            }
        }

        private val iconView: IconImageView = itemView.findViewById(R.id.iconView)
        private val nameView: TextView = itemView.findViewById(R.id.nameView)

        fun bind(iconInfo: IconHelper.IconInfo) {
            iconView.load(iconInfo)
            nameView.text = iconInfo.name
        }

        fun bind(appInfo: IconHelper.AppInfo) {
            iconView.load(appInfo)
            nameView.text = appInfo.getLabel(nameView.context)
        }

    }

}