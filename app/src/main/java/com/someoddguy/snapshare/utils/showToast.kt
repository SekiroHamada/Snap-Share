package com.someoddguy.snapshare.utils

import android.widget.Toast
import com.someoddguy.snapshare.globalcontext.GlobalContext // Make sure to import this
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun showToast(message: String, short: Boolean) {
    // Access the global context directly
    val context = GlobalContext.appContext
    if(short){
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }else{
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

}