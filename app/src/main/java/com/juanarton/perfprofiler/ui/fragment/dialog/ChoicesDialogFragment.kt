package com.juanarton.perfprofiler.ui.fragment.dialog

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.recyclerview.widget.LinearLayoutManager
import com.juanarton.perfprofiler.R
import com.juanarton.perfprofiler.core.adapter.ChoiceAdapter
import com.juanarton.perfprofiler.databinding.FragmentChoicesDialogBinding

class ChoicesDialogFragment(
    private val choiceId: String = "",
    private val index: Int = 0,
    private val selectionList: List<String>?
) : Fragment() {

    private var _binding: FragmentChoicesDialogBinding? = null
    private val binding get() = _binding

    private var callback: DialogCallback? = null

    fun setOnChoiceSelected(callback: DialogCallback) {
        this.callback = callback
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChoicesDialogBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listener: (String) -> Unit = {
            callback?.onChoiceSelected(choiceId, index, it)
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            parentFragmentManager.beginTransaction().remove(this@ChoicesDialogFragment).commit()
        }

        val adapter = ChoiceAdapter(listener, choiceId)
        binding?.apply {

            root.setOnClickListener {
                parentFragmentManager.beginTransaction().remove(this@ChoicesDialogFragment).commit()
            }

            rvChoices.layoutManager = LinearLayoutManager(requireContext())
            rvChoices.adapter = adapter
            adapter.setData(selectionList)
        }
    }
}