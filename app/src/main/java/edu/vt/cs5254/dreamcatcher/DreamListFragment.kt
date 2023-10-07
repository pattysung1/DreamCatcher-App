package edu.vt.cs5254.dreamcatcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import edu.vt.cs5254.dreamcatcher.databinding.FragmentDreamDetailBinding
import edu.vt.cs5254.dreamcatcher.databinding.FragmentDreamListBinding

class DreamListFragment : Fragment() {

    private var _binding: FragmentDreamListBinding?= null
    private val binding
        get() = checkNotNull(_binding){"FragmentDreamListBinding is null!!!"}
    private val vm: DreamListViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDreamListBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.dreamRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.dreamRecyclerView.adapter = DreamListAdapter(vm.dreams)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}