package com.someoddguy.snapshare.ui.sendfilescreen.filecard

import android.net.Uri
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.someoddguy.snapshare.R



@Composable
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
                .width(110.dp)
                .border(
                    width = 12.dp,
                    color = colorResource(R.color.lightning)
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
                    text =uri.lastPathSegment ?: "Unknown File",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
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