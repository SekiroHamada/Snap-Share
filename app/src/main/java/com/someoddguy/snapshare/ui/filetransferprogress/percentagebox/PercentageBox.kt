package com.someoddguy.snapshare.ui.filetransferprogress.percentagebox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.someoddguy.snapshare.R
import com.someoddguy.snapshare.ui.filetransferprogress.rhombusshape.RhombusShape

@Preview
@Composable
fun PercentageBox(percentage : Float, threshold : Float){
    Box(
        modifier = Modifier
            .width(20.dp)
            .height(25.dp)
            .clip(shape = RhombusShape())
            .then(
                if(percentage >= threshold){
                    Modifier.background(colorResource(R.color.lightning))
                }else{
                    Modifier.background(colorResource(R.color.custom_gray))
                }
            )
    )
}