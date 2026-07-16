package com.someoddguy.snapshare.ui.sendfilescreen.filecard

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
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
import com.someoddguy.snapshare.globalcontext.GlobalContext


fun getMimeType(context: Context, uri: Uri): String {
    if (uri.scheme == "content") {
        return context.contentResolver.getType(uri) ?: "unknown"
    }

    val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase()) ?: "unknown"
}
@Composable
fun FileCard(
    uri: Uri,
    onRemoveClick: (Uri) -> Unit
){
    val context = GlobalContext.appContext
    val fileName = uri.lastPathSegment?:"Unknown File"
    val mimeType = getMimeType(context, uri)
    val backgroundResId = when {
        mimeType.startsWith("audio/") -> R.drawable.ic_headphone
        mimeType.startsWith("video/") -> R.drawable.ic_video
        mimeType == "application/pdf" -> R.drawable.ic_pdf
        mimeType.startsWith("image/") -> R.drawable.ic_image // Or R.drawable.ic_image if you have one
        else -> R.drawable.ic_file
    }
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
                    .paint(
                        painter = painterResource(id = backgroundResId),
                        contentScale = ContentScale.Inside
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text =fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(horizontal = 2.dp, vertical = 2.dp)
                        .offset(y = 40.dp)
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
                painter = painterResource(id = R.drawable.ic_cancel),
                contentDescription = "Remove file",
                tint = Color.White
            )
        }

    }

}