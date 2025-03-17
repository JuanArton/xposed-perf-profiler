package com.juanarton.perfprofiler.core.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.juanarton.perfprofiler.R
import com.juanarton.perfprofiler.core.data.domain.model.Profile
import com.juanarton.perfprofiler.core.util.Utils.formatGpuStringMhz
import com.juanarton.perfprofiler.core.util.Utils.formatStringMhz
import com.juanarton.perfprofiler.databinding.ChoiceItemBinding
import com.juanarton.perfprofiler.databinding.ProfileItemBinding
import com.juanarton.perfprofiler.ui.activity.main.MainActivity.Companion.APP_PROFILE
import com.juanarton.perfprofiler.ui.activity.profiledetail.DetailProfileActivity.Companion.CPU_GOV
import com.juanarton.perfprofiler.ui.activity.profiledetail.DetailProfileActivity.Companion.GPU_GOV
import com.juanarton.perfprofiler.ui.activity.profiledetail.DetailProfileActivity.Companion.GPU_MAX
import com.juanarton.perfprofiler.ui.activity.profiledetail.DetailProfileActivity.Companion.GPU_MIN

class ProfileAdapter(
    private val editClick: (Profile) -> Unit,
    private val deleteClick: (Profile) -> Unit,
) : RecyclerView.Adapter<ProfileAdapter.ViewHolder>() {
    private var profileList: ArrayList<Profile> = arrayListOf()

    fun setData(profileList: List<Profile>?) {
        this.profileList.apply {
            clear()
            profileList?.let { addAll(it) }
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.profile_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val profile = profileList[position]
        holder.bind(profile)
    }

    override fun getItemCount(): Int = profileList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding = ProfileItemBinding.bind(itemView)

        fun bind(profile: Profile) {
            binding.apply {
                tvProfileValue.text = profile.name

                ibDelete.setOnClickListener {
                    deleteClick(profile)
                }

                ibEdit.setOnClickListener {
                    editClick(profile)
                }
            }
        }
    }
}