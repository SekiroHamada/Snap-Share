package com.someoddguy.snapshare.ui.sendfilescreen.filecard

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.someoddguy.snapshare.R
import androidx.compose.material3.Icon

val fileTypeIcons = mapOf(
    "mp3" to R.drawable.ic_headphone,
    "mp4" to R.drawable.ic_video,
    "mkv" to R.drawable.ic_video,
    "jpg" to R.drawable.ic_file,
    "pdf" to R.drawable.ic_pdf
)
@Composable
fun FileCard(
    uri: Uri,
    onRemoveClick: (Uri) -> Unit
){

    val fileName = uri.lastPathSegment?:"Unknown File"
    val fileExtension = fileName.substringAfterLast('.', missingDelimiterValue = "").lowercase()
    val backgroundResId = fileTypeIcons[fileExtension]

    Box(
        modifier = Modifier
            .padding(top = 12.dp, end = 12.dp)
    ){
        Card(
            modifier = Modifier
                .height(120.dp)
                .width(110.dp)
                .border(
                    width = 2.dp,
                    color = colorResource(R.color.lightning),
                    shape = RoundedCornerShape(12.dp)
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(R.color.black),
                contentColor = colorResource(R.color.white)
            )
        ){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text =fileExtension,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        IconButton(
            onClick = { onRemoveClick(uri) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 5.dp, y = (-6).dp)
                .size(28.dp)
                .background(
                    color = Color.Transparent,
                    shape = CircleShape
                )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_cancel), // Replace with your SVG resource name
                contentDescription = "Remove file",
                tint = Color.White // This colors your SVG white to match your original text
            )
        }
//        Button(onClick = {onRemoveClick(uri)},
//            modifier = Modifier
//                .align(Alignment.TopEnd)
//                .offset(x=5.dp , y = (-6).dp)
//                .size(28.dp ),
//            shape = CircleShape,
//            contentPadding = PaddingValues(0.dp),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = colorResource(id=R.color.cancel_red),
//                contentColor = Color.White
//            )
//
//        ) {
//            Text(text="X");
//        }
    }

}