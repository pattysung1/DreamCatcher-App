package edu.vt.cs5254.dreamcatcher

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import edu.vt.cs5254.dreamcatcher.databinding.FragmentDreamDetailBinding
import kotlinx.coroutines.flow.collect
import android.text.format.DateFormat
import kotlinx.coroutines.launch
//import java.text.DateFormat

class DreamDetailFragment: Fragment() {
    private var _binding: FragmentDreamDetailBinding?= null

    private val binding
        get() = checkNotNull(_binding){
            "FragmentDreamDetailBinding is null!!!"
        }

    private val args: DreamDetailFragmentArgs by navArgs()

    private val vm: DreamDetailViewModel by viewModels(){
        DreamDetailViewModelFactory(args.dreamId)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDreamDetailBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.w("---DDF---", "Received arg ${args.dreamId}")
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                vm.dream.collect{ dream->
                    dream?.let {updateView(it)}
                }
            }
        }

        binding.titleText.doOnTextChanged{text, _, _, _ ->
            vm.updateDream { oldDream ->
                oldDream.copy(title = text.toString())
                    .apply { entries = oldDream.entries }
            }
        }
        binding.deferredCheckbox.setOnClickListener {
            vm.updateDream { oldDream ->
                oldDream.copy()
                    .apply { entries =
                        if (oldDream.isDeferred){
                            oldDream.entries.filter { it.kind != DreamEntryKind.DEFERRED }
                        } else{
                            oldDream.entries + DreamEntry(kind = DreamEntryKind.DEFERRED, dreamId = id)
                        }
                    }
            }
        }

        binding.fulfilledCheckbox.setOnClickListener {
            vm.updateDream{ oldDream ->
                oldDream.copy()
                    .apply {entries =
                        if (oldDream.isFulfilled){
                            oldDream.entries.filter { it.kind != DreamEntryKind.FULFILLED }
                        } else{
                            oldDream.entries + DreamEntry(kind = DreamEntryKind.FULFILLED, dreamId = id)
                        }
                    }
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
    private fun updateView(dream: Dream){
        val buttonList = listOf(
            binding.entry0Button,
            binding.entry1Button,
            binding.entry2Button,
            binding.entry3Button,
            binding.entry4Button
        )
        buttonList.forEach{ it.visibility = View.GONE }

        buttonList.zip(dream.entries)
            .forEach { (btn, entry) ->
                btn.configureForEntry(entry)
            }

        binding.deferredCheckbox.isChecked = dream.isDeferred
        binding.fulfilledCheckbox.isChecked = dream.isFulfilled

        binding.deferredCheckbox.isEnabled = !dream.isFulfilled
        binding.fulfilledCheckbox.isEnabled = !dream.isDeferred

        val dataString = DateFormat.format("yyyy-MM-dd 'at' hh:mm:ss A", dream.lastUpdated)
        binding.lastUpdatedText.text = getString(R.string.last_updated, dataString)

        if(binding.titleText.text.toString() != dream.title){
            binding.titleText.setText(dream.title)
        }

        binding.addReflectionButton.setOnClickListener {
            // 打开 ReflectionDialogFragment 或者执行其他操作
            val reflectionDialogFragment = ReflectionDialogFragment()
            reflectionDialogFragment.show(parentFragmentManager, ReflectionDialogFragment::class.java.simpleName)
        }


        // Show or hide the FAB based on the dream's fulfillment status
        if (dream.isFulfilled) {
            binding.addReflectionButton.hide()
        } else {
            binding.addReflectionButton.show()
        }

        // Show or hide the FAB based on the dream's fulfillment status
        if (dream.isFulfilled) {
            binding.addReflectionButton.hide()
        } else {
            binding.addReflectionButton.show()
        }
    }

    private fun Button.configureForEntry(entry: DreamEntry){
        visibility = View.VISIBLE
        text = entry.kind.toString()
        when(entry.kind){
            DreamEntryKind.REFLECTION -> {
                setBackgroundWithContrastingText("#FFC988")
                isAllCaps = false
                text = entry.text
            }
            DreamEntryKind.CONCEIVED -> {
                setBackgroundWithContrastingText("navy")
            }
            DreamEntryKind.DEFERRED -> {
                setBackgroundWithContrastingText("#FA8978")
            }
            DreamEntryKind.FULFILLED -> {
                setBackgroundWithContrastingText("#86E3CE")
            }
        }
    }

}