package com.juanarton.perfprofiler.ui.fragment.profilesetting

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.juanarton.perfprofiler.core.adapter.ProfileAdapter
import com.juanarton.perfprofiler.core.data.domain.model.Profile
import com.juanarton.perfprofiler.databinding.FragmentProfileSettingBinding
import com.juanarton.perfprofiler.ui.activity.profiledetail.DetailProfileActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileSettingFragment : Fragment() {

    private var _binding: FragmentProfileSettingBinding? = null
    private val binding get() = _binding

    private val profileSettingViewModel: ProfileSettingViewModel by viewModels()

    private val profileList: ArrayList<String> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileSettingBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileSettingViewModel.getProfile()
        profileSettingViewModel.profileList.observe(viewLifecycleOwner) {
            profileList.clear()
            profileList.addAll(
                it.map { profile ->
                    profile.name
                }
            )

            binding?.apply {
                val editListener: (Profile) -> Unit = { profile ->
                    val intent = Intent(requireContext(), DetailProfileActivity::class.java)
                    intent.putExtra("PROFILE", profile)
                    startActivity(intent)
                }

                val deleteListener: (Profile) -> Unit = { profile ->
                    profileSettingViewModel.deleteProfile(profile)
                    profileSettingViewModel.deleteAppProfileByProfile(profile.name)
                    profileSettingViewModel.getProfile()
                }

                val adapter = ProfileAdapter(editListener, deleteListener)
                rvProfile.layoutManager = LinearLayoutManager(requireContext())
                rvProfile.adapter = adapter
                adapter.setData(it)

                initSpinner(spinnerChargingProfile)
                initSpinner(spinnerOvh40Profile)
                initSpinner(spinnerOvh42Profile)
                initSpinner(spinnerOvh45Profile)
            }
        }
    }

    private fun initSpinner(spinner: AppCompatSpinner) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, profileList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        if (profileList.isNotEmpty()) {
            val index = when (spinner) {
                binding?.spinnerChargingProfile -> profileList.indexOf(profileSettingViewModel.getChargingProfile())
                binding?.spinnerOvh40Profile -> profileList.indexOf(profileSettingViewModel.getOvh40Profile())
                binding?.spinnerOvh42Profile -> profileList.indexOf(profileSettingViewModel.getOvh42Profile())
                binding?.spinnerOvh45Profile -> profileList.indexOf(profileSettingViewModel.getOvh45Profile())
                else -> -1
            }

            if (index in profileList.indices) {
                spinner.setSelection(index)
            }
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()

                when (spinner) {
                    binding?.spinnerChargingProfile -> profileSettingViewModel.setChargingProfile(selectedItem)
                    binding?.spinnerOvh40Profile -> profileSettingViewModel.setOvh40Profile(selectedItem)
                    binding?.spinnerOvh42Profile -> profileSettingViewModel.setOvh42Profile(selectedItem)
                    binding?.spinnerOvh45Profile -> profileSettingViewModel.setOvh45Profile(selectedItem)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun onResume() {
        super.onResume()

        profileSettingViewModel.getProfile()
        _binding = null
    }
}