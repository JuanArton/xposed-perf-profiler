package com.juanarton.perfprofiler.core.adapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.kyuubiran.ezxhelper.utils.runOnMainThread
import com.juanarton.perfprofiler.R
import com.juanarton.perfprofiler.core.data.domain.model.AppItem
import com.juanarton.perfprofiler.core.data.domain.model.AppProfile
import com.juanarton.perfprofiler.core.util.Utils.getAppIcon
import com.juanarton.perfprofiler.databinding.AppItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppListAdapter(
    private val onClick: (Int, String, TextView, TextView) -> Unit,
    private val appProfileList: List<AppProfile>
) : RecyclerView.Adapter<AppListAdapter.ViewHolder>(){
    private var appList: ArrayList<AppItem> = arrayListOf()

    fun setData(appList: List<AppItem>?) {
        this.appList.apply {
            clear()
            appList?.let {
                val matchedItems = it.filter { appItem ->
                    appProfileList.any { profile -> profile.packageId == appItem.packageId }
                }.sortedBy { app -> app.name }
                val unmatchedItems = it.filter { appItem ->
                    appProfileList.none { profile -> profile.packageId == appItem.packageId }
                }
                addAll(matchedItems + unmatchedItems)
            }
            notifyDataSetChanged()
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.app_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = appList[position]
        holder.bind(app)
    }

    override fun getItemCount(): Int = appList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding = AppItemBinding.bind(itemView)

        fun bind(appItem: AppItem) {
            binding.apply {
                CoroutineScope(Dispatchers.IO).launch {
                    val typedValue = TypedValue()
                    val theme = ivAppIcon.context.theme
                    theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
                    val primaryColor = typedValue.data

                    val appIcon = getAppIcon(ivAppIcon.context, appItem.packageId)
                    runOnMainThread {
                        ivAppIcon.setImageDrawable(appIcon)

                        tvAppName.text = appItem.name

                        val index = appProfileList.indexOfFirst { it.packageId == appItem.packageId }

                        if (index != -1) {
                            tvAppProfile.text = appProfileList[index].profile
                            tvAppName.setTextColor(primaryColor)
                            tvAppProfile.setTextColor(primaryColor)
                        }
                    }
                }

                appClickMask.setOnClickListener {
                    onClick(adapterPosition, appItem.packageId, tvAppName, tvAppProfile)
                }
            }
        }
    }
}