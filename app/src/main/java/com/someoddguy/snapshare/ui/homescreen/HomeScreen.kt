package com.someoddguy.snapshare.ui.homescreen

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.someoddguy.snapshare.R
import com.someoddguy.snapshare.navigation.Routes
import com.someoddguy.snapshare.ui.filecard.FileCard

@Composable
@Preview(showSystemUi= true)
fun HomeScreen(navHostController: NavHostController){
    var selectedFileUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    // 2. Use the GetMultipleContents contract
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        // The result is automatically parsed into a List<Uri>
        if (uris.isNotEmpty()) {
            selectedFileUris = (selectedFileUris + uris).distinct()
        } else {
            selectedFileUris = uris
        }
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorResource(R.color.black),          // Sets the background to black
        contentColor = colorResource(R.color.white)    // Sets the default text color to white
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (selectedFileUris.isNotEmpty()) {
                Text(
                    text = "Selected Files (${selectedFileUris.size}):",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .align(Alignment.Start) // Align text to the left
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp), // Spacing between the cards and the button
                    contentPadding = PaddingValues(horizontal = 16.dp), // Padding at the start/end of the scroll
                    horizontalArrangement = Arrangement.spacedBy(12.dp) // Spacing between individual cards
                ) {
                    // This iterates through the List<Uri> and creates a FileCard for each
                    items(selectedFileUris) { uri ->
                        FileCard(
                            uri = uri,
                            onRemoveClick = { uriToRemove->
                                selectedFileUris=selectedFileUris.filter{it!=uriToRemove}
                            }
                        )
                    }
                }
            }
            Button(
                onClick = {
                    // 3. Launch the picker. The system UI will now allow long-pressing to select multiple.
                    filePickerLauncher.launch("*/*")
                }
            ) {
                Text("Select Files to Share")
            }

            // 4. Update the UI feedback to show the count
            if (selectedFileUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        navHostController.navigate(Routes.SearchBluetoothUsers){}// see if you can change it
                    }
                ){
                    Text(text="Send")
                }
            }
        }
    }
}