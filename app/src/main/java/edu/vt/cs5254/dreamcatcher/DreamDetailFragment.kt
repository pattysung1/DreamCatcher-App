package edu.vt.cs5254.dreamcatcher

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.core.view.doOnLayout
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.io.File

//import java.text.DateFormat

class DreamDetailFragment : Fragment() {
    private var _binding: FragmentDreamDetailBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "FragmentDreamDetailBinding is null!!!"
        }

    private val args: DreamDetailFragmentArgs by navArgs()

    private val vm: DreamDetailViewModel by viewModels() {
        DreamDetailViewModelFactory(args.dreamId)
    }

    private val photoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { tookPicture ->
        Log.w("---DDF---", "Took Picture Result: $tookPicture")
        if (tookPicture) {
            binding.dreamPhoto.tag = null
            vm.dream.value?.let {
                updatePhoto(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDreamDetailBinding.inflate(inflater, container, false)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_dream_detail, menu)

                menu.findItem(R.id.take_photo_menu).isVisible = canResolve(
                    photoLauncher.contract.createIntent(
                        requireContext(),
                        Uri.EMPTY
                    )
                )
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.share_dream_menu -> {
                        vm.dream.value?.let { shareDream(it) }
                        true
                    }

                    R.id.take_photo_menu -> {
                        Log.w("---DDF---", "Take Photo Menu clicked!")

                        vm.dream.value?.let {
                            val photoFile = File(
                                requireActivity().filesDir,
                                it.photoFileName
                            )

                            val photoUri = FileProvider.getUriForFile(
                                requireContext(),
                                "edu.vt.cs5254.dreamcatcher.fileprovider",
                                photoFile
                            )

                            photoLauncher.launch(photoUri)
                        }
                        true
                    }

                    else -> false
                }
            }

        }, viewLifecycleOwner)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.w("---DDF---", "Received arg ${args.dreamId}")

        getItemTouchHelper().attachToRecyclerView(binding.dreamEntryRecycler)
        binding.dreamEntryRecycler.layoutManager = LinearLayoutManager(context)

        // addMenuProvider
        requireActivity().addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_dream_detail, menu)
                menu.findItem(R.id.take_photo_menu).isVisible = canResolve(
                    photoLauncher.contract.createIntent(
                        requireContext(),
                        Uri.EMPTY
                    )
                )
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.share_dream_menu -> {
                        vm.dream.value?.let { shareDream(it) }
                        true
                    }
                    R.id.take_photo_menu -> {
                        Log.w("---DDF---", "Take Photo Menu Clicked")

                        vm.dream.value?.let {
                            val photoFile = File(
                                requireActivity().filesDir,
                                it.photoFileName
                            )

                            val photoUri = FileProvider.getUriForFile(
                                requireContext(),
                                "edu.vt.cs5254.dreamcatcher.fileprovider",
                                photoFile
                            )

                            photoLauncher.launch(photoUri)
                        }

                        true
                    }
                    else -> false
                }
            }

        }, viewLifecycleOwner)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.dream.collect { dream ->
                    dream?.let {
                        binding.dreamEntryRecycler.adapter = DreamEntryAdapter(it.entries)
                        updateView(it)
                    }
                }
            }
        }

        binding.titleText.doOnTextChanged { text, _, _, _ ->
            vm.updateDream { oldDream ->
                oldDream.copy(title = text.toString())
                    .apply { entries = oldDream.entries }
            }
        }
        binding.deferredCheckbox.setOnClickListener {
            vm.updateDream { oldDream ->
                oldDream.copy()
                    .apply {
                        entries =
                            if (oldDream.isDeferred) {
                                oldDream.entries.filter { it.kind != DreamEntryKind.DEFERRED }
                            } else {
                                oldDream.entries + DreamEntry(
                                    kind = DreamEntryKind.DEFERRED,
                                    dreamId = id
                                )
                            }
                    }
            }
        }

        binding.fulfilledCheckbox.setOnClickListener {
            vm.updateDream { oldDream ->
                oldDream.copy()
                    .apply {
                        entries =
                            if (oldDream.isFulfilled) {
                                oldDream.entries.filter { it.kind != DreamEntryKind.FULFILLED }
                            } else {
                                oldDream.entries + DreamEntry(
                                    kind = DreamEntryKind.FULFILLED,
                                    dreamId = id
                                )
                            }
                    }
            }
        }
        // 设置一个 FragmentResultListener 来监听 ReflectionDialogFragment 返回的结果
        setFragmentResultListener(
            ReflectionDialogFragment.REQUEST_KEY
        ) { _, bundle ->
            val reflectText = bundle.getString(ReflectionDialogFragment.BUNDLE_KEY)
            vm.updateDream { oldDream ->
                oldDream.copy().apply {
                    entries = oldDream.entries + DreamEntry(
                        kind = DreamEntryKind.REFLECTION,
                        text = reflectText.toString(), dreamId = oldDream.id
                    )
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun updateView(dream: Dream) {


        binding.deferredCheckbox.isChecked = dream.isDeferred
        binding.fulfilledCheckbox.isChecked = dream.isFulfilled

        binding.deferredCheckbox.isEnabled = !dream.isFulfilled
        binding.fulfilledCheckbox.isEnabled = !dream.isDeferred

        val dataString = DateFormat.format("yyyy-MM-dd 'at' hh:mm:ss A", dream.lastUpdated)
        binding.lastUpdatedText.text = getString(R.string.last_updated, dataString)

        if (binding.titleText.text.toString() != dream.title) {
            binding.titleText.setText(dream.title)
        }

        updatePhoto(dream)

        binding.addReflectionButton?.setOnClickListener {
            // 打开 ReflectionDialogFragment 或者执行其他操作
            val reflectionDialogFragment = ReflectionDialogFragment()
            reflectionDialogFragment.show(
                parentFragmentManager,
                ReflectionDialogFragment::class.java.simpleName
            )
        }


        // Show or hide the FAB based on the dream's fulfillment status
        if (dream.isFulfilled) {
            binding.addReflectionButton?.hide()
        } else {
            binding.addReflectionButton?.show()
        }

        //Add this to update
        updatePhoto(dream)
    }

    private fun updatePhoto(dream: Dream) {
        Log.w("---DDF---", "Update Photo Called")
        with(binding.dreamPhoto) {
            if (tag != dream.photoFileName) {
                Log.w("---DDF---", "Update Required -- No Cache!")

                val photoFile = File(
                    requireActivity().filesDir,
                    dream.photoFileName
                )
                if (photoFile.exists()) {
                    Log.w("---DDF---", "Photo Exists")
                    doOnLayout { imgView ->
                        val bitmap = getScaledBitmap(
                            photoFile.path,
                            imgView.width,
                            imgView.height
                        )
                        setImageBitmap(bitmap)
                        tag = dream.photoFileName
                    }
                    binding.dreamPhoto.setOnClickListener {
                        findNavController().navigate(
                            DreamDetailFragmentDirections.showPhotoDetail(dream.photoFileName)
                        )
                    }
                    binding.dreamPhoto.isEnabled = true
                } else {
                    Log.w("---DDF---", "Photo Does NOT Exist")
                    setImageBitmap(null)
                    binding.dreamPhoto.isEnabled = false
                }
            } else {
                Log.w("---DDF---", "CACHE EXISTS!")
            }
        }
    }

    fun shareDream(dream: Dream) {
        val reportIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getDreamText(dream))
            putExtra(
                Intent.EXTRA_SUBJECT,
                "Subject"
            )
        }

        val chooserIntent = Intent.createChooser(
            reportIntent,
            "Subject"
        )
        startActivity(chooserIntent)
    }

    private fun getDreamText(dream: Dream): String {
        val title = dream.title + "\n"
        val date = String.format(
            DateFormat.format(getString(R.string.last_updated_share_format), dream.lastUpdated).toString()
        ) + "\n"
        val reflection = if (dream.entries.any { entry -> entry.kind == DreamEntryKind.REFLECTION }) {
            val sb = StringBuilder()
            sb.append("Reflections:").append("\n")
            dream.entries.filter { entry -> entry.kind == DreamEntryKind.REFLECTION}.forEach {
                sb.append(String.format(" * %s", it.text)).append("\n")
            }
            sb.toString()
        }else {
            ""
        }

        val lastLine = if (dream.entries.any { entry -> entry.kind == DreamEntryKind.DEFERRED || entry.kind == DreamEntryKind.FULFILLED }) {
            val kind = dream.entries.filter { entry -> entry.kind == DreamEntryKind.DEFERRED || entry.kind == DreamEntryKind.FULFILLED }
                .first().kind
            String.format("This dream has been %s.",
                if (kind == DreamEntryKind.DEFERRED) "Deferred"
                else "Fulfilled"
            )
        }else {
            ""
        }

        return title + date + reflection + lastLine
    }


    private fun canResolve(intent: Intent): Boolean {
//        @Suppress("deprecated")
//        return requireActivity().packageManager.resolveActivity(
//            intent,
//            PackageManager.MATCH_DEFAULT_ONLY
//        ) != null
        return requireActivity().packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null
    }

    private fun getItemTouchHelper(): ItemTouchHelper{
        return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,0){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deHolder = viewHolder as DreamEntryHolder
                val entryToDelete = deHolder.boundEntry
                vm.updateDream { oldDream ->
                    oldDream.copy()
                        .apply { entries = oldDream.entries.filterNot { it.id ==entryToDelete.id } }
                }
            }

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val deHolder = viewHolder as DreamEntryHolder
                val entryToSwipe = deHolder.boundEntry
                return if (entryToSwipe.kind == DreamEntryKind.REFLECTION){
                    ItemTouchHelper.LEFT
                } else{
                    0
                }
            }
        })
    }
}