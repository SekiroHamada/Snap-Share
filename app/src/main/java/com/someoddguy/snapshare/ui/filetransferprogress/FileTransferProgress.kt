package com.someoddguy.snapshare.ui.filetransferprogress

import androidx.compose.runtime.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object FileTransferProgress {

    //tells total no of files
    private val _totalFiles = MutableStateFlow(0)
    val totalFiles : StateFlow<Int> = _totalFiles.asStateFlow()
    fun updateTotalFiles(no : Int){
        _totalFiles.value = no
    }

    //tells how many files got transferred
    private val _filesDone = MutableStateFlow(0)
    val filesDone: StateFlow<Int> = _filesDone.asStateFlow()
    fun updateFilesDone(){
        _filesDone.value += 1
    }
    //tells if initiate by sender or receiver
    private val _isReceiving = MutableStateFlow(false)
    val isReceiving : StateFlow<Boolean> = _isReceiving.asStateFlow()
    fun updateIsReceiving(bool : Boolean){
        _isReceiving.value = bool
    }

    //tells current file name
    private val _fileName = MutableStateFlow("")
    val fileName : StateFlow<String> = _fileName.asStateFlow()
    fun updateFileName(name : String){
        _fileName.value = name
    }

    //tells current file size in KB
    private val _fileSize = MutableStateFlow(0L)
    val fileSize : StateFlow<Long> = _fileSize.asStateFlow()
    fun updateFileSize(size : Long){
        _fileSize.value = size/1024
    }

    //tells if transfer is done or not
    private val _isDone = MutableStateFlow(false)
    val isDone: StateFlow<Boolean> = _isDone.asStateFlow()
    fun updateProgress(bool: Boolean){
        _isDone.value = bool
    }
}