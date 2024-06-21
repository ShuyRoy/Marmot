package me.jinheng.cityullm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.jinheng.cityullm.models.ModelOperation

class DownloadViewModel : ViewModel() {
    private val _downloadProgress = MutableLiveData<Int>()
    val downloadProgress: LiveData<Int> = _downloadProgress

    fun startDownload() {
        viewModelScope.launch(Dispatchers.IO) {
            ModelOperation.downloadModel("Llama-2-7B-Chat-GGUF", _downloadProgress)
        }
    }
}