package nl.carsharing.app.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

val AppIcons.Straighten: ImageVector
    get() {
        if (_Straighten != null) return _Straighten!!
        _Straighten = ImageVector.Builder(
            name = "Straighten",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            addPath(
                pathData = addPathNodes("M21,6H3c-1.1,0 -2,0.9 -2,2v8c0,1.1 0.9,2 2,2h18c1.1,0 2,-0.9 2,-2V8c0,-1.1 -0.9,-2 -2,-2zM21,16H3V8h2v4h2V8h2v4h2V8h2v4h2V8h2v4h2V8h2v8z"),
                fill = SolidColor(Color.Black)
            )
        }.build()
        return _Straighten!!
    }

@Suppress("ObjectPropertyName")
private var _Straighten: ImageVector? = null
