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
import androidx.navigation.fragment.navArgs
import edu.vt.cs5254.dreamcatcher.databinding.FragmentDreamDetailBinding

class DreamDetailFragment: Fragment() {
    private var _binding: FragmentDreamDetailBinding?= null

    private val binding
        get() = checkNotNull(_binding){
            "FragmentDreamDetailBinding is null!!!"
        }

    private val args: DreamDetailFragmentArgs by navArgs()

    private val vm: DreamDetailViewModel by viewModels()
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

        binding.titleText.doOnTextChanged{text, _, _, _ ->
            vm.dream = vm.dream.copy(title = text.toString())
                .apply { entries = vm.dream.entries }
            updateView()
        }
        binding.deferredCheckbox.setOnClickListener {
            if (vm.dream.isDeferred){
                vm.dream.entries = vm.dream.entries.filter { it.kind != DreamEntryKind.DEFERRED }
            } else{
                vm.dream.entries += DreamEntry(kind = DreamEntryKind.DEFERRED, dreamId = vm.dream.id)
            }
            updateView()
        }
        binding.fulfilledCheckbox.setOnClickListener {
            if (vm.dream.isFulfilled){
                vm.dream.entries = vm.dream.entries.filter { it.kind != DreamEntryKind.FULFILLED }
            } else{
                vm.dream.entries += DreamEntry(kind = DreamEntryKind.FULFILLED, dreamId = vm.dream.id)
            }
            updateView()
        }
        //set click listeners (call updateView within each)

        updateView()
    }
    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
    private fun updateView(){

        binding.lastUpdatedText.text = String.format(vm.lastUpdateDateTime)
        if (binding.titleText.text.toString() != vm.dream.title) {
            binding.titleText.setText(vm.dream.title)
        }

        val buttonList = listOf(
            binding.entry0Button,
            binding.entry1Button,
            binding.entry2Button,
            binding.entry3Button,
            binding.entry4Button
        )
        buttonList.forEach{ it.visibility = View.GONE }

        buttonList.zip(vm.dream.entries)
            .forEach { (btn, entry) ->
                btn.configureForEntry(entry)
            }

        binding.deferredCheckbox.isChecked = vm.dream.isDeferred
        binding.fulfilledCheckbox.isChecked = vm.dream.isFulfilled

        binding.deferredCheckbox.isEnabled = !vm.dream.isFulfilled
        binding.fulfilledCheckbox.isEnabled = !vm.dream.isDeferred
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