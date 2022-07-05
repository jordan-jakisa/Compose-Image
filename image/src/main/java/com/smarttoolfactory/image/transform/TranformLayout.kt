package com.smarttoolfactory.image.transform

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.gesture.PointerRequisite
import com.smarttoolfactory.gesture.detectPointerTransformGestures
import kotlin.math.abs

@Composable
fun TransformLayout(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    handleRadius: Dp = 15.dp,
    handlePlacement: HandlePlacement = HandlePlacement.Corner,
    onDown: (Transform) -> Unit = {},
    onMove: (Transform) -> Unit = {},
    onUp: (Transform) -> Unit = {},
    content: @Composable () -> Unit
) {

    MorphSubcomposeLayout(
        modifier = modifier
            .requiredSizeIn(
                minWidth = handleRadius * 2,
                minHeight = handleRadius * 2
            ),
        handleRadius = handleRadius.coerceAtLeast(12.dp),
        updatePhysicalSize = false,
        mainContent = {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        },
        dependentContent = { intSize: IntSize ->

            val dpSize = with(LocalDensity.current) {
                val rawWidth = intSize.width.toDp()
                val rawHeight = intSize.height.toDp()
                DpSize(rawWidth, rawHeight)
            }

            println("Dependent size: $intSize")
            TransformLayout(
                enabled = enabled,
                handleRadius = handleRadius,
                dpSize = dpSize,
                handlePlacement = handlePlacement,
                onDown = onDown,
                onMove = onMove,
                onUp = onUp,
                content = content
            )
        }
    )
}

@Composable
private fun TransformLayout(
    enabled: Boolean = true,
    handleRadius: Dp = 15.dp,
    dpSize: DpSize,
    transform: Transform = Transform(),
    handlePlacement: HandlePlacement,
    onDown: (Transform) -> Unit = {},
    onMove: (Transform) -> Unit = {},
    onUp: (Transform) -> Unit = {},
    content: @Composable () -> Unit
) {

    val touchRegionRadius: Float
    val minDimension: Float
    val size: Size

    with(LocalDensity.current) {
        touchRegionRadius = handleRadius.coerceAtLeast(12.dp).toPx()
        minDimension = (touchRegionRadius * if (handlePlacement == HandlePlacement.Corner) 4 else 6)

        size = DpSize(
            dpSize.width + handleRadius * 2,
            dpSize.height + handleRadius * 2
        ).toSize()
    }

    var outerTransform by remember {
        mutableStateOf(
            transform
        )
    }

    var innerTransform by remember {
        mutableStateOf(
            transform
        )
    }


    var rectDraw by remember {
        mutableStateOf(
            Rect(
                offset = Offset.Zero,
                size = size
            )
        )
    }

    val editModifier =
        Modifier
            .graphicsLayer {
                translationX = outerTransform.translationX
                translationY = outerTransform.translationY
                scaleX = outerTransform.scaleX
                scaleY = outerTransform.scaleY
                rotationZ = outerTransform.rotation
            }

            .transform(
                enabled = enabled,
                size = size,
                touchRegionRadius = touchRegionRadius,
                minDimension = minDimension,
                handlePlacement = handlePlacement,
                transform = outerTransform,
                onDown = { transformChange: Transform, rect: Rect ->
                    outerTransform = transformChange
                    rectDraw = rect
                    onDown(outerTransform)
                },
                onMove = { transformChange: Transform, rect: Rect ->
                    outerTransform = transformChange
                    rectDraw = rect
                    onMove(outerTransform)
                },
                onUp = { transformChange: Transform, rect: Rect ->
                    outerTransform = transformChange
                    rectDraw = rect
                    onUp(outerTransform)
                }
            )
            // Padding is required to keep touch position of handles inside composable
            // without padding only 1 quarter of corner handles are in composable
            // padding is scaled because scale change also changes padding dimensions
            .padding(
                horizontal = handleRadius / abs(outerTransform.scaleX),
                vertical = handleRadius / abs(outerTransform.scaleY)
            )
            .clipToBounds()
            .graphicsLayer {
                translationX = innerTransform.translationX
                translationY = innerTransform.translationY
                scaleX = innerTransform.scaleX
                scaleY = innerTransform.scaleY
            }
            .pointerInput(Unit) {
                detectPointerTransformGestures(
                    requisite = PointerRequisite.GreaterThan,
                    numberOfPointers = 1,
                    onGesture = { _,
                                  gesturePan: Offset,
                                  gestureZoom: Float,
                                  _,
                                  _,
                                  _ ->

                        val oldZoom = innerTransform.scaleX
                        val offset =
                            Offset(innerTransform.translationX, innerTransform.translationY)
                        val zoom = (oldZoom * gestureZoom).coerceIn(1f, 3f)
                        val newOffset = offset + gesturePan

                        val maxX = (size.width * (zoom - 1) / 2f)
                        val maxY = (size.height * (zoom - 1) / 2f)

                        innerTransform = innerTransform.copy(
                            translationX = newOffset.x.coerceIn(-maxX, maxX),
                            translationY = newOffset.y.coerceIn(-maxY, maxY),
                            scaleX = zoom,
                            scaleY = zoom
                        )
                    }
                )
            }

    TransformImpl(
        modifier = editModifier,
        enabled = enabled,
        dpSize = dpSize,
        handleRadius = handleRadius,
        touchRegionRadius = touchRegionRadius,
        rectDraw = rectDraw,
        handlePlacement = handlePlacement,
        content = content
    )
}

@Composable
private fun TransformImpl(
    modifier: Modifier,
    enabled: Boolean,
    dpSize: DpSize,
    handleRadius: Dp,
    touchRegionRadius: Float,
    rectDraw: Rect,
    handlePlacement: HandlePlacement,
    content: @Composable () -> Unit
) {
    Box(
        Modifier.size(
            width = dpSize.width + handleRadius * 2,
            height = dpSize.height + handleRadius * 2
        ),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }

        if (enabled) {
            HandleOverlay(
                modifier = Modifier.fillMaxSize(),
                radius = touchRegionRadius,
                handlePlacement = handlePlacement,
                rectDraw = rectDraw
            )
        }
    }
}

