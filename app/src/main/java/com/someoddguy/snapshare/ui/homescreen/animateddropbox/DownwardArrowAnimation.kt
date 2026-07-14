package com.someoddguy.snapshare.ui.homescreen.animateddropbox

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.someoddguy.snapshare.R

@Composable
fun DownwardArrow() {
    // 1. Create the infinite transition
    val infiniteTransition = rememberInfiniteTransition(label = "upArrow")

    // 2. Define the animated value
    val translateY by infiniteTransition.animateFloat(
        initialValue = 5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "translateY"
    )
    Box(modifier = Modifier
        .graphicsLayer{
            this.translationY = translateY
        }){
        Icon(
            painter = painterResource(id = R.drawable.ic_custom_send),
            contentDescription = "Send",
            modifier = Modifier.size(50.dp),
            tint = colorResource(R.color.lightning)


        )
    }

}