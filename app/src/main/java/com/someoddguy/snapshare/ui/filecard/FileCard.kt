package com.someoddguy.snapshare.ui.filecard

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.net.URI
import com.someoddguy.snapshare.R




@Composable
@Preview()
fun FileCard(
    uri: Uri,
    onRemoveClick: (Uri) -> Unit
){
    Box(
        modifier = Modifier
            .padding(top = 12.dp, end = 12.dp)
    ){
        Card(
            modifier = Modifier
                .height(120.dp)
                .width(110.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Displays the end of the Uri string as a placeholder for the filename
                Text(
                    text =uri.lastPathSegment ?: "Unknown File",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis // Adds "..." if the name is too long
                )

            }
        }
        Button(onClick = {onRemoveClick(uri)},
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x=5.dp , y = (-6).dp)
                .size(28.dp ),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id=R.color.cancel_red),
                contentColor = Color.White
            )

        ) {
            Text(text="X");
        }
    }

}