package edu.vt.cs5254.dreamcatcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DreamListViewModel : ViewModel() {

    private val _dreams: MutableStateFlow<List<Dream>> = MutableStateFlow(emptyList())
    val dreams
        get() = _dreams.asStateFlow()

    private val repository = DreamRepository.get()

    suspend fun insertDream(dream: Dream){
        repository.insertDream(dream)
    }

    fun deleteDream(dream: Dream){
        viewModelScope.launch {
//            delay(3000)
            repository.deleteDream(dream)
        }
    }

    init {
        viewModelScope.launch {
            repository.getDreams().collect(){
                _dreams.value = it
            }
        }
    }

}