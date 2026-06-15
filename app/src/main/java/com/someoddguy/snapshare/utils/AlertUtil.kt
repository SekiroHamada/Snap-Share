package com.someoddguy.snapshare.utils // Make sure this matches your folder structure

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.provider.Settings

// Notice the "Activity." before the function name
fun Activity.showPermanentDenyAlert() = runOnUiThread {
    AlertDialog.Builder(this)
        .setTitle("Permission Required")
        .setMessage("This app needs specific permissions to function correctly. Please grant them in the app settings.")
        .setPositiveButton("Go to Settings") { dialog, _ ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
            dialog.dismiss()
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .setCancelable(false)
        .show()
}