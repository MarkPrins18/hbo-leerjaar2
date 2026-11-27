package nl.carsharing.app.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

val AppIcons.Key: ImageVector
    get() {
        if (_Key != null) return _Key!!
        _Key = ImageVector.Builder(
            name = "Key",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            addPath(
                pathData = addPathNodes("M21,10h-8.35C11.83,7.67 9.61,6 7,6c-3.31,0 -6,2.69 -6,6s2.69,6 6,6c2.61,0 4.83,-1.67 5.65,-4H13l2,2l2,-2l2,2l4,-4.04L21,10zM7,15c-1.65,0 -3,-1.35 -3,-3c0,-1.65 1.35,-3 3,-3s3,1.35 3,3C10,13.65 8.65,15 7,15z"),
                fill = SolidColor(Color.Black)
            )
        }.build()
        return _Key!!
    }

@Suppress("ObjectPropertyName")
private var _Key: ImageVector? = null
