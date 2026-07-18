package com.someoddguy.snapshare.ui.filetransferprogress.rhombusshape

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class RhombusShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            with(density){
                val width = 20.dp.toPx()
                val height = 25.dp.toPx()
                val slantOffset = width * 0.3f
                moveTo(slantOffset, 0f)
                lineTo(width, 0f)
                lineTo(width - slantOffset, height)
                lineTo(0f, height)
                close()
            }

        }
        return Outline.Generic(path)
    }
}