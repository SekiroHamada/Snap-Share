package com.someoddguy.snapshare

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.IntentCompat
import com.someoddguy.snapshare.filepackettransfer.SendFilePackets
import com.someoddguy.snapshare.navigation.NavigationSystem

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleShareIntent(intent)

        setContent {
            NavigationSystem()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent?) {
        intent?.let {
            when (it.action) {
                Intent.ACTION_SEND -> {
                    val uri = IntentCompat.getParcelableExtra(
                        it,
                        Intent.EXTRA_STREAM,
                        Uri::class.java
                    )

                    uri?.let { validUri ->
                        SendFilePackets.handleSelectedFiles(listOf(validUri))
                    }
                }
                Intent.ACTION_SEND_MULTIPLE -> {
                    val uris = IntentCompat.getParcelableArrayListExtra(
                        it,
                        Intent.EXTRA_STREAM,
                        Uri::class.java
                    )

                    uris?.let { validUris ->
                        SendFilePackets.handleSelectedFiles(validUris.toList())
                    }
                }
                else -> {
                    return
                }
            }
        }
    }
}