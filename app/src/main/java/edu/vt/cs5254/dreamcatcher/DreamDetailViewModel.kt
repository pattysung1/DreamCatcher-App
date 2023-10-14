package edu.vt.cs5254.dreamcatcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class DreamDetailViewModel(dreamId: UUID) : ViewModel() {

    private val _dream: MutableStateFlow<Dream?> = MutableStateFlow(null)
    val dream = _dream.asStateFlow()

    fun updateDream(onUpdate: (Dream)-> Dream){
        _dream.update { oldDream ->
            val newDream = oldDream?.let{ onUpdate(it)}?:return
            if(newDream == oldDream && newDream.entries == oldDream.entries){
                return
            }
            newDream.copy(lastUpdated = Date()).apply{ entries = newDream.entries}
        }
    }
    init {
        //lookup dream from database via dreamId...
        viewModelScope.launch {
            _dream.value = DreamRepository.get().getDream(dreamId)
        }
    }
}

class DreamDetailViewModelFactory(private val dreamId: UUID): ViewModelProvider.Factory{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DreamDetailViewModel(dreamId) as T
    }
}