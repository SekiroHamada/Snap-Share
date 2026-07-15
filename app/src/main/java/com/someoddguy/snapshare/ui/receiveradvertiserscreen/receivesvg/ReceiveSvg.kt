package com.someoddguy.snapshare.ui.receiveradvertiserscreen.receivesvg

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.someoddguy.snapshare.R

@Composable
fun ReceiveSvg(){
    val infiniteTransition = rememberInfiniteTransition(label = "upArrow")
    val duration = 1200
    // 2. Define the animated value
    val rotationZ by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationZ"
    )


    Icon(
        painter = painterResource(id = R.drawable.atom_2_filled),
        contentDescription = "Send",
        modifier = Modifier
            .size(30.dp)
            .graphicsLayer{
                this.rotationZ = rotationZ
            },
        tint = colorResource(R.color.lightning)
    )
}