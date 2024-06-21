package me.jinheng.cityullm.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.jinheng.cityullm.ChatRecord

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Welcome to CityU-MLab LLM Chat"
    }
    val text: LiveData<String> = _text

    private val _chatRecords = MutableLiveData<List<ChatRecord>>().apply {
        value = emptyList()
    }
    val chatRecords: LiveData<List<ChatRecord>> = _chatRecords

    fun addChatRecord(record: ChatRecord) {
        val updatedRecords = _chatRecords.value?.toMutableList() ?: mutableListOf()
        updatedRecords.add(record)
        _chatRecords.value = updatedRecords
    }
}