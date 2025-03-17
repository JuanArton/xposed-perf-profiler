package com.juanarton.perfprofiler.ui.fragment.dialog

interface DialogCallback {
    fun onChoiceSelected(choiceId: String, index: Int, choice: String)
}