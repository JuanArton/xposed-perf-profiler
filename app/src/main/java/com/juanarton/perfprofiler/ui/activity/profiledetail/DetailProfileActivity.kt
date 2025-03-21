package com.juanarton.perfprofiler.ui.activity.profiledetail

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider
import autodispose2.autoDispose
import com.juanarton.perfprofiler.R
import com.juanarton.perfprofiler.core.data.domain.model.Profile
import com.juanarton.perfprofiler.core.util.CPUCluster
import com.juanarton.perfprofiler.core.util.Utils.destroyFragment
import com.juanarton.perfprofiler.core.util.Utils.formatGpuStringMhz
import com.juanarton.perfprofiler.core.util.Utils.formatStringMhz
import com.juanarton.perfprofiler.core.util.Utils.fragmentBuilder
import com.juanarton.perfprofiler.databinding.ActivityDetailProfileBinding
import com.juanarton.perfprofiler.ui.fragment.dialog.ChoicesDialogFragment
import com.juanarton.perfprofiler.ui.fragment.dialog.DialogCallback
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailProfileActivity : AppCompatActivity() {

    private var _binding: ActivityDetailProfileBinding? = null
    private val binding get() = _binding

    private val detailViewModel: DetailViewModel by viewModels()

    private val cpuMaxFreqs: MutableList<String> = mutableListOf()
    private val cpuMinFreqs: MutableList<String> = mutableListOf()
    private var cpuCurrentGov: MutableList<String> = mutableListOf()

    private var gpuMaxFreq: String = ""
    private var gpuMinFreq: String = ""
    private var gpuCurrentGov: String = ""

    private var isEdit = false

    companion object {
        const val CPU_MAX = "CPU_MAX"
        const val CPU_MIN = "CPU_MIN"
        const val CPU_GOV = "CPU_GOV"

        const val GPU_MAX = "GPU_MAX"
        const val GPU_MIN = "GPU_MIN"
        const val GPU_GOV = "GPU_GOV"
    }

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityDetailProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val profile = intent.getParcelableExtra("PROFILE", Profile::class.java)
        profile?.let {
            cpuMaxFreqs.add(it.c1MaxFreq)
            cpuMaxFreqs.add(it.c2MaxFreq)
            cpuMaxFreqs.add(it.c3MaxFreq)

            cpuMinFreqs.add(it.c1MinFreq)
            cpuMinFreqs.add(it.c2MinFreq)
            cpuMinFreqs.add(it.c3MinFreq)

            cpuCurrentGov.add(it.c1Governor)
            cpuCurrentGov.add(it.c2Governor)
            cpuCurrentGov.add(it.c3Governor)

            gpuMaxFreq = it.gpuMaxFreq
            gpuMinFreq = it.gpuMinFreq
            gpuCurrentGov = it.gpuGovernor

            binding?.etProfileName?.setText(it.name)

            isEdit = true
        }

        detailViewModel.getCpuFolders()
        detailViewModel.cpuFolders.observe(this) {
            it.forEachIndexed { index, policy ->
                initCpuClusterData(index, policy)
            }
        }

        initGpuClusterData()
    }

    private fun initCpuClusterData(policyIndex: Int, policy: String) {
        binding?.apply {
            lifecycleScope.launch {
                val cluster = CPUCluster.entries.find { it.code == policyIndex }

                val view = when (cluster) {
                    CPUCluster.Little -> clusterLittle
                    CPUCluster.Big -> clusterBig
                    CPUCluster.Prime -> clusterPrime
                    else -> clusterPrime
                }

                view.apply {
                    root.visibility = View.VISIBLE
                    tvCoreCluster.text = cluster.toString()
                    clusterFreqMax.tvItemTitle.text = getString(R.string.max_freq)
                    if (isEdit) {
                        clusterFreqMax.tvItemValue.text = cpuMaxFreqs[policyIndex]
                        clusterFreqMin.tvItemValue.text = cpuMinFreqs[policyIndex]
                        clusterGov.tvItemValue.text = cpuCurrentGov[policyIndex]
                    } else {
                        detailViewModel.getCpuMaxFreq(policy).let {
                            clusterFreqMax.tvItemValue.text = formatStringMhz(it)
                            cpuMaxFreqs.add(it)
                        }

                        clusterFreqMin.tvItemTitle.text = getString(R.string.min_freq)
                        detailViewModel.getCpuMinFreq(policy).let {
                            clusterFreqMin.tvItemValue.text = formatStringMhz(it)
                            cpuMinFreqs.add(it)
                        }

                        clusterGov.tvItemTitle.text = getString(R.string.cpu_governor)
                        detailViewModel.getCurrentCpuGovernor(policy).let {
                            clusterGov.tvItemValue.text = it
                            cpuCurrentGov.add(it)
                        }
                    }

                    val cpuFreqs = detailViewModel.getScalingAvailableFreq(policy).filter { it.isNotEmpty() }
                    val cpuGovs = detailViewModel.getScalingAvailableGov(policy).filter { it.isNotEmpty() }

                    view.clusterFreqMax.clickMask.setOnClickListener {
                        val fragment = ChoicesDialogFragment(CPU_MAX, policyIndex, cpuFreqs)
                        fragmentBuilder(this@DetailProfileActivity, fragment, android.R.id.content)
                        clickListener(fragment, clusterFreqMax.tvItemValue)
                    }

                    view.clusterFreqMin.clickMask.setOnClickListener {
                        val fragment = ChoicesDialogFragment(CPU_MIN, policyIndex, cpuFreqs)
                        fragmentBuilder(this@DetailProfileActivity, fragment, android.R.id.content)
                        clickListener(fragment, clusterFreqMin.tvItemValue)
                    }

                    view.clusterGov.clickMask.setOnClickListener {
                        val fragment = ChoicesDialogFragment(CPU_GOV, policyIndex, cpuGovs)
                        fragmentBuilder(this@DetailProfileActivity, fragment, android.R.id.content)
                        clickListener(fragment, clusterGov.tvItemValue)
                    }
                }
            }
        }
    }

    private fun initGpuClusterData() {
        lifecycleScope.launch {
            binding?.apply {
                clusterGpu.apply {
                    tvCoreCluster.text = getString(R.string.gpu)

                    if (isEdit) {
                        clusterFreqMax.tvItemValue.text = gpuMaxFreq
                        clusterFreqMin.tvItemValue.text = gpuMinFreq
                        clusterGov.tvItemValue.text = gpuCurrentGov
                    } else {
                        clusterFreqMax.tvItemTitle.text = getString(R.string.max_freq)
                        detailViewModel.getGpuMaxFreq().let {
                            clusterFreqMax.tvItemValue.text = formatGpuStringMhz(it)
                            gpuMaxFreq = it
                        }

                        clusterFreqMin.tvItemTitle.text = getString(R.string.min_freq)
                        detailViewModel.getGpuMinFreq().let {
                            clusterFreqMin.tvItemValue.text = formatGpuStringMhz(it)
                            gpuMinFreq = it
                        }

                        clusterGov.tvItemTitle.text = getString(R.string.cpu_governor)
                        detailViewModel.getCurrentGpuGovernor().let {
                            clusterGov.tvItemValue.text = it
                            gpuCurrentGov = it
                        }
                    }

                    val gpuFreqs = detailViewModel.getGpuFrequencies().filter { it.isNotEmpty() }
                    val gpuGovs = detailViewModel.getGpuGovernors().filter { it.isNotEmpty() }

                    clusterGpu.clusterFreqMax.clickMask.setOnClickListener {
                        val fragment = ChoicesDialogFragment(GPU_MAX, 0, gpuFreqs)
                        fragmentBuilder(this@DetailProfileActivity, fragment, android.R.id.content)
                        clickListener(fragment, clusterFreqMax.tvItemValue)
                    }

                    clusterGpu.clusterFreqMin.clickMask.setOnClickListener {
                        val fragment = ChoicesDialogFragment(GPU_MIN, 0, gpuFreqs)
                        fragmentBuilder(this@DetailProfileActivity, fragment, android.R.id.content)
                        clickListener(fragment, clusterFreqMin.tvItemValue)
                    }

                    clusterGpu.clusterGov.clickMask.setOnClickListener {
                        val fragment = ChoicesDialogFragment(GPU_GOV, 0, gpuGovs)
                        fragmentBuilder(this@DetailProfileActivity, fragment, android.R.id.content)
                        clickListener(fragment, clusterGov.tvItemValue)
                    }
                }
            }
        }
    }

    private fun clickListener(fragment: ChoicesDialogFragment, textView: TextView) {
        binding?.apply {
            fragment.setOnChoiceSelected(object : DialogCallback {
                override fun onChoiceSelected(
                    choiceId: String,
                    index: Int,
                    choice: String
                ) {
                    when (choiceId) {
                        CPU_MAX -> {
                            cpuMaxFreqs[index] = choice
                            destroyFragment(this@DetailProfileActivity, fragment)
                            textView.text = formatStringMhz(choice)
                        }
                        CPU_MIN -> {
                            cpuMinFreqs[index] = choice
                            destroyFragment(this@DetailProfileActivity, fragment)
                            textView.text = formatStringMhz(choice)
                        }
                        CPU_GOV -> {
                            cpuCurrentGov[index] = choice
                            destroyFragment(this@DetailProfileActivity, fragment)
                            textView.text = choice
                        }
                        GPU_MAX -> {
                            gpuMaxFreq = choice
                            destroyFragment(this@DetailProfileActivity, fragment)
                            textView.text = formatGpuStringMhz(choice)
                        }
                        GPU_MIN -> {
                            gpuMinFreq = choice
                            destroyFragment(this@DetailProfileActivity, fragment)
                            textView.text = formatGpuStringMhz(choice)
                        }
                        GPU_GOV -> {
                            gpuCurrentGov = choice
                            destroyFragment(this@DetailProfileActivity, fragment)
                            textView.text = choice
                        }
                    }
                }
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                binding?.apply {
                    if (etProfileName.text.toString().isNotEmpty()) {
                        detailViewModel.saveProfile(
                            Profile(
                                etProfileName.text.toString(),
                                cpuMaxFreqs[0], cpuMinFreqs[0], cpuCurrentGov[0],
                                cpuMaxFreqs[1], cpuMinFreqs[1], cpuCurrentGov[1],
                                cpuMaxFreqs[2], cpuMinFreqs[2], cpuCurrentGov[2],
                                gpuMaxFreq, gpuMinFreq, gpuCurrentGov
                            )
                        )
                            .subscribeOn(Schedulers.io())
                            .autoDispose(AndroidLifecycleScopeProvider.from(this@DetailProfileActivity))
                            .subscribe(
                                { finish() },
                                { error -> println("Gagal: ${error.message}") }
                            )
                    }
                    else {
                        Toast.makeText(
                            this@DetailProfileActivity,
                            getString(R.string.profile_name_cant_be_empty),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}