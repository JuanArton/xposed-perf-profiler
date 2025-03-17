package com.juanarton.perfprofiler.core.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.juanarton.perfprofiler.R
import com.juanarton.perfprofiler.core.util.Utils.formatGpuStringMhz
import com.juanarton.perfprofiler.core.util.Utils.formatStringMhz
import com.juanarton.perfprofiler.databinding.ChoiceItemBinding
import com.juanarton.perfprofiler.ui.activity.main.MainActivity.Companion.APP_PROFILE
import com.juanarton.perfprofiler.ui.activity.profiledetail.DetailProfileActivity.Companion.CPU_GOV
import com.juanarton.perfprofiler.ui.activity.profiledetail.DetailProfileActivity.Companion.GPU_GOV
import com.juanarton.perfprofiler.ui.activity.profiledetail.DetailProfileActivity.Companion.GPU_MAX
import com.juanarton.perfprofiler.ui.activity.profiledetail.DetailProfileActivity.Companion.GPU_MIN

class ChoiceAdapter(
    private val onClick: (String) -> Unit,
    private val choiceId: String
) : RecyclerView.Adapter<ChoiceAdapter.ViewHolder>() {
    private var choiceList: ArrayList<String> = arrayListOf()

    fun setData(choiceList: List<String>?) {
        this.choiceList.apply {
            clear()
            choiceList?.let { addAll(it) }
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.choice_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val choice = choiceList[position]
        holder.bind(choice)
    }

    override fun getItemCount(): Int = choiceList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding = ChoiceItemBinding.bind(itemView)

        fun bind(choice: String) {
            binding.apply {
                tvChoiceValue.text = when (choiceId) {
                    CPU_GOV -> choice
                    GPU_GOV -> choice
                    GPU_MAX -> formatGpuStringMhz(choice)
                    GPU_MIN -> formatGpuStringMhz(choice)
                    APP_PROFILE -> choice
                    else -> { formatStringMhz(choice) }
                }

                choiceClickMask.setOnClickListener {
                    onClick(choiceList[adapterPosition])
                }
            }
        }
    }
}