package com.someoddguy.snapshare.ui.searchdevicecard

import android.location.Address
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.someoddguy.snapshare.R

@Composable
@Preview
fun SearchDeviceCard(
    deviceName: String,
    deviceAddress: String,
    onClick: () -> Unit
){
    Box(
        modifier = Modifier
            .border(
                width = 2.dp,
                color = colorResource(R.color.white),
                shape = CircleShape
            )
            .padding(
                top = 12.dp,
                bottom = 12.dp,
                start= 12.dp,
                end = 12.dp)
            .clickable{onClick()},
        contentAlignment = Alignment.Center
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .border(
                        width = 2.dp,
                        color = colorResource(R.color.white),
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.lightning))
            ){

            }

            Spacer(
                modifier= Modifier
                    .width(30.dp)

            )
            Column(){
                Text(
                    text=deviceName,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color= colorResource(id= R.color.lightning)
                )
                Spacer(
                    modifier= Modifier
                        .height(5.dp)
                )
                Text(
                    text=deviceAddress,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color= colorResource(id= R.color.lightning)
                )
            }

        }
    }
}
