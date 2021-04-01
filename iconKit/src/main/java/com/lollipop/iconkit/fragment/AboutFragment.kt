package com.lollipop.iconkit.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.iconcore.ui.BaseFragment
import com.lollipop.iconcore.util.MakerInfoManager
import com.lollipop.iconcore.util.OvalOutlineProvider
import com.lollipop.iconcore.util.lazyBind
import com.lollipop.iconkit.LIconKit
import com.lollipop.iconkit.R
import com.lollipop.iconkit.databinding.KitFragmentAboutBinding

/**
 * @author lollipop
 * @date 10/23/20 19:24
 */
class AboutFragment: BaseFragment() {

    private val viewBinding: KitFragmentAboutBinding by lazyBind()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        OvalOutlineProvider.bind(viewBinding.makerLogo)

        val makerInfoManager = MakerInfoManager(LIconKit.createMakerInfoProvider(view.context))
        viewBinding.makerLogo.setImageResource(makerInfoManager.icon)
        viewBinding.makerName.setText(makerInfoManager.name)
        viewBinding.makerSignature.setText(makerInfoManager.signature)
        viewBinding.headerImageView.setImageResource(makerInfoManager.background)

        val mottoArray = makerInfoManager.mottoArray
        if (mottoArray != 0) {
            val stringArray = resources.getStringArray(mottoArray)
            viewBinding.mottoList.layoutManager = LinearLayoutManager(
                view.context, RecyclerView.VERTICAL, false)
            viewBinding.mottoList.adapter = MottoAdapter(stringArray)
            viewBinding.mottoList.adapter?.notifyDataSetChanged()
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