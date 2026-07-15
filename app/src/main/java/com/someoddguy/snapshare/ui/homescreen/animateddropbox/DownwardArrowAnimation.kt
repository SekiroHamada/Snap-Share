package com.someoddguy.snapshare.ui.homescreen.animateddropbox

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.someoddguy.snapshare.R

@Preview
@Composable
fun DownwardArrow() {
    // 1. Create the infinite transition
    val infiniteTransition = rememberInfiniteTransition(label = "downArrow")
    val duration = 1200
    // 2. Define the animated value
    val translateY by infiniteTransition.animateFloat(
        initialValue = 60f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "translateY"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = duration,
                easing = CubicBezierEasing(
                    0.8f,
                    0.0f,
                    1.0f,
                    0.2f)
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Column(

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier =Modifier.offset(y = (-15).dp)
    ){
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_down),
            contentDescription = "Send",
            modifier = Modifier
                .size(30.dp)
                .graphicsLayer{
                    this.translationY = translateY
                    this.alpha = alpha
                },
            tint = colorResource(R.color.lightning)
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_tray),
            contentDescription = "Send",
            modifier = Modifier
                .size(50.dp),
            tint = colorResource(R.color.lightning)
        )
    }

}