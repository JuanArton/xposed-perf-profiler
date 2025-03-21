package com.juanarton.perfprofiler.ui.fragment.applist

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider
import autodispose2.autoDispose
import com.juanarton.perfprofiler.R
import com.juanarton.perfprofiler.core.adapter.AppListAdapter
import com.juanarton.perfprofiler.core.data.domain.model.AppItem
import com.juanarton.perfprofiler.core.data.domain.model.AppProfile
import com.juanarton.perfprofiler.core.util.Utils.destroyFragment
import com.juanarton.perfprofiler.core.util.Utils.fragmentBuilder
import com.juanarton.perfprofiler.core.util.Utils.getAppName
import com.juanarton.perfprofiler.databinding.FragmentAppListBinding
import com.juanarton.perfprofiler.ui.activity.main.MainActivity.Companion.APP_PROFILE
import com.juanarton.perfprofiler.ui.activity.profiledetail.DetailProfileActivity
import com.juanarton.perfprofiler.ui.fragment.dialog.ChoicesDialogFragment
import com.juanarton.perfprofiler.ui.fragment.dialog.DialogCallback
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.schedulers.Schedulers

@AndroidEntryPoint
class AppListFragment : Fragment() {

    private var _binding: FragmentAppListBinding? = null
    private val binding get() = _binding

    val appListViewModel: AppListViewModel by viewModels()

    private val appProfile: ArrayList<AppProfile> = arrayListOf()
    private val profileList: ArrayList<String> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAppListBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_add -> {
                        startActivity(Intent(requireContext(), DetailProfileActivity::class.java))
                        true
                    }
                    R.id.show_system -> {
                        appListViewModel.getInstalledApps(requireContext().packageManager, true)
                        true
                    }
                    R.id.hide_system -> {
                        appListViewModel.getInstalledApps(requireContext().packageManager, false)
                        true
                    }
                    else -> true
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        appListViewModel.getAppProfile()
        appListViewModel.appProfileList.observe(viewLifecycleOwner) {
            appProfile.clear()
            appProfile.addAll(it)
            appListViewModel.getInstalledApps(requireContext().packageManager, false)

            appListViewModel.getProfile()
        }

        appListViewModel.profileList.observe(viewLifecycleOwner) {
            profileList.clear()
            profileList.addAll(
                it.map { profile ->
                    profile.name
                }
            )

            binding?.apply {
                val item = it.map { profile ->
                    profile.name
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, item)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerDefaultProfile.adapter = adapter

                val index = it.indexOfFirst { profile -> profile.name == appListViewModel.getDefaultProfile() }

                if (index != -1) {
                    spinnerDefaultProfile.setSelection(index)
                }

                spinnerDefaultProfile.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val selectedItem = parent.getItemAtPosition(position).toString()
                        appListViewModel.setDefaultProfile(selectedItem)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }
        }

        binding?.apply {
            val typedValue = TypedValue()
            requireContext().theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
            val primaryColor = typedValue.data

            val listener: (Int, String, TextView, TextView) -> Unit = { index, packageId, appName, appProfile ->
                val notSet = arrayListOf("Not Set")
                val fragment = ChoicesDialogFragment(APP_PROFILE, index, profileList + notSet)
                fragmentBuilder(requireActivity(), fragment, android.R.id.content)

                fragment.setOnChoiceSelected(object : DialogCallback {
                    override fun onChoiceSelected(choiceId: String, index: Int, choice: String) {
                        if (choiceId == APP_PROFILE) {
                            if (choice == "Not Set") {
                                appListViewModel.deleteAppProfile(
                                    AppProfile(
                                        packageId, choice
                                    )
                                )
                                    .subscribeOn(Schedulers.io())
                                    .autoDispose(AndroidLifecycleScopeProvider.from(viewLifecycleOwner))
                                    .subscribe(
                                        {  },
                                        { error -> println("Gagal: ${error.message}") }
                                    )

                                appName.setTextColor(tvDefaultProfile.textColors)
                                appProfile.setTextColor(tvDefaultProfile.textColors)
                            } else {
                                appName.setTextColor(primaryColor)
                                appProfile.setTextColor(primaryColor)

                                appListViewModel.insertAppProfile(
                                    AppProfile(
                                        packageId, choice
                                    )
                                )
                                    .subscribeOn(Schedulers.io())
                                    .autoDispose(AndroidLifecycleScopeProvider.from(viewLifecycleOwner))
                                    .subscribe(
                                        {  },
                                        { error -> println("Gagal: ${error.message}") }
                                    )
                            }

                            appProfile.text = choice
                            destroyFragment(requireActivity(), fragment)
                        }
                    }
                })
            }

            val adapter = AppListAdapter(listener, appProfile)
            rvAppList.layoutManager = LinearLayoutManager(requireContext())
            rvAppList.adapter = adapter

            appListViewModel.appList.observe(viewLifecycleOwner) {
                val appList = it.map { app ->
                    AppItem(
                        getAppName(requireContext(), app),
                        app
                    )
                }

                adapter.setData(appList.sortedBy { app -> app.name })
            }
        }
    }

    override fun onResume() {
        super.onResume()

        appListViewModel.getProfile()
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}