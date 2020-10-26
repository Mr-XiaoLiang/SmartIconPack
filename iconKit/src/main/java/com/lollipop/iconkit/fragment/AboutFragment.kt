package com.lollipop.iconkit.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.iconcore.util.MakerInfoManager
import com.lollipop.iconcore.util.OvalOutlineProvider
import com.lollipop.iconkit.LIconKit
import com.lollipop.iconkit.R
import kotlinx.android.synthetic.main.kit_fragment_about.*

/**
 * @author lollipop
 * @date 10/23/20 19:24
 */
class AboutFragment: BaseTabFragment() {
    override val tabIcon: Int
        get() = R.drawable.ic_baseline_person_24
    override val tabTitle: Int
        get() = R.string.about
    override val tabColorId: Int
        get() = R.color.tabAboutSelectedColor
    override val layoutId: Int
        get() = R.layout.kit_fragment_about

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        OvalOutlineProvider.bind(makerLogo)

        val makerInfoManager = MakerInfoManager(LIconKit.createMakerInfoProvider(view.context))
        makerLogo.setImageResource(makerInfoManager.icon)
        makerName.setText(makerInfoManager.name)
        makerSignature.setText(makerInfoManager.signature)
        headerImageView.setImageResource(makerInfoManager.background)

        val mottoArray = makerInfoManager.mottoArray
        if (mottoArray != 0) {
            val stringArray = resources.getStringArray(mottoArray)
            mottoList.layoutManager = LinearLayoutManager(
                view.context, RecyclerView.VERTICAL, false)
            mottoList.adapter = MottoAdapter(stringArray)
            mottoList.adapter?.notifyDataSetChanged()
        }

    }

    private class MottoAdapter(val values: Array<String>): RecyclerView.Adapter<MottoHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MottoHolder {
            return MottoHolder.create(parent)
        }

        override fun onBindViewHolder(holder: MottoHolder, position: Int) {
            holder.bind(values[position])
        }

        override fun getItemCount(): Int {
            return values.size
        }

    }

    private class MottoHolder private constructor(view: View): RecyclerView.ViewHolder(view) {
        companion object {
            fun create(group: ViewGroup): MottoHolder {
                return MottoHolder(
                    LayoutInflater.from(group.context)
                        .inflate(R.layout.kit_item_motto, group, false))
            }
        }

        private val mottoView: TextView = itemView.findViewById(R.id.mottoValueView)

        fun bind(value: String) {
            mottoView.text = value
        }

    }

}