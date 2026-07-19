package com.someoddguy.snapshare.ui.sendfilescreen

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.someoddguy.snapshare.R
import com.someoddguy.snapshare.navigation.Routes
import com.someoddguy.snapshare.ui.sendfilescreen.filecard.FileCard
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.someoddguy.snapshare.filepackettransfer.SendFilePackets

@Preview
@Composable
fun SendFileScreen(
    navHostController: NavHostController = rememberNavController(),
    viewModel: SendFileViewModel = viewModel() // Instantiates or retrieves the ViewModel
) {
    // Observe the state from the ViewModel
    val selectedFileUris by viewModel.selectedFileUris.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        viewModel.addFiles(uris)
    }

    BackHandler() {
        SendFilePackets.clearFiles()
        navHostController.popBackStack()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_background), // Replace with your filename
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (selectedFileUris.isNotEmpty()) {
                Text(
                    text = "${selectedFileUris.size} Files Selected",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(selectedFileUris) { uri ->
                        FileCard(
                            uri = uri,
                            onRemoveClick = { uriToRemove ->
                                viewModel.removeSelectedFile(uriToRemove)
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    // 3. Launch the picker. The system UI will now allow long-pressing to select multiple.
                    filePickerLauncher.launch("*/*")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.custom_gray),
                    contentColor = colorResource(R.color.lightning)
                ),
                modifier = Modifier
                    .padding(
                        start = 20.dp,
                        top = 20.dp,
                        end = 20.dp,
                        bottom = 10.dp)
                    .width(270.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Select Files to Share", fontSize = 20.sp)
            }

            // 4. Update the UI feedback to show the count
            if (selectedFileUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        navHostController.navigate(Routes.SearchBluetoothUsers) {}
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.custom_gray),
                        contentColor = colorResource(R.color.lightning)
                    ),
                    modifier = Modifier
                        .padding(
                            start = 20.dp,
                            top = 20.dp,
                            end = 20.dp,
                            bottom = 10.dp)
                        .width(240.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Send", fontSize = 20.sp)
                }
            }
        }
    }
}

