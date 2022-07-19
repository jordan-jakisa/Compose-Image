package com.smarttoolfactory.image.beforeafter

import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.*
import com.smarttoolfactory.gesture.detectMotionEvents
import com.smarttoolfactory.gesture.detectTransformGestures
import com.smarttoolfactory.image.ImageScope
import com.smarttoolfactory.image.R
import com.smarttoolfactory.image.getParentSize
import com.smarttoolfactory.image.getScaledBitmapRect
import com.smarttoolfactory.image.util.scale

/**
 * A composable that lays out and draws a given [beforeImage] and [afterImage] at given [order]
 * with specified [contentScale] and returns draw area and section of drawn bitmap.
 *
 * [BeforeAfterImageScope] extends [ImageScope] that returns draw area dimensions and image draw rect
 * and touch position of user on screen.
 *
 * @param beforeImage image that show initial progress
 * @param afterImage image that show final progress
 * @param enableProgressWithTouch flag to enable drag and change progress with touch
 * @param enableZoom when enabled images are zoomable and pannable
 * @param order order of images to be drawn
 * @param alignment determines where image will be aligned inside [BoxWithConstraints]
 * This is observable when bitmap image/width ratio differs from [Canvas] that draws [ImageBitmap]
 * @param contentDescription text used by accessibility services to describe what this image
 * represents. This should always be provided unless this image is used for decorative purposes,
 * and does not represent a meaningful action that a user can take. This text should be
 * localized, such as by using [androidx.compose.ui.res.stringResource] or similar
 * @param contentScale how image should be scaled inside Canvas to match parent dimensions.
 * [ContentScale.Fit] for instance maintains src ratio and scales image to fit inside the parent.
 */
@Composable
fun BeforeAfterImage(
    modifier: Modifier = Modifier,
    beforeImage: ImageBitmap,
    afterImage: ImageBitmap,
    enableProgressWithTouch: Boolean = true,
    enableZoom: Boolean = true,
    order: Order = Order.BeforeAfter,
    lineColor: Color = Color.White,
    @DrawableRes thumbResource: Int = R.drawable.baseline_swap_horiz_24,
    thumbSize: Dp = 36.dp,
    @FloatRange(from = 0.0, to = 100.0) thumbPositionPercent: Float = 85f,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
    contentDescription: String? = null,
) {

    val density = LocalDensity.current
    val thumbPosition = thumbPositionPercent.coerceIn(0f, 100f)

    BeforeAfterImage(
        modifier = modifier,
        beforeImage = beforeImage,
        afterImage = afterImage,
        enableProgressWithTouch = enableProgressWithTouch,
        enableZoom = enableZoom,
        order = order,
        contentScale = contentScale,
        alignment = alignment,
        contentDescription = contentDescription,
    ) {

        val handlePosition = position.x
        val posY: Int

        val realPos = handlePosition - with(density) {
            val thumbRadius = (thumbSize / 2)

            posY = ((imageHeight * thumbPosition / 100f - thumbRadius) - imageHeight / 2)
                .roundToPx()

            imageWidth.toPx() / 2
        }

        Canvas(modifier = Modifier.size(imageWidth, imageHeight)) {
            val canvasWidth = size.width
            val imagePosition = handlePosition.coerceIn(0f, canvasWidth)

            drawLine(
                lineColor,
                strokeWidth = 1.5.dp.toPx(),
                start = Offset(imagePosition, 0f),
                end = Offset(imagePosition, size.height)
            )
        }

        Icon(
            painter = painterResource(id = thumbResource),
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier
                .offset {
                    IntOffset(realPos.toInt(), posY)
                }
                .shadow(2.dp, CircleShape)
                .background(Color.White)
                .size(thumbSize)
                .padding(4.dp)
        )
    }
}

/**
 * A composable that lays out and draws a given [beforeImage] and [afterImage] at given [order]
 * with specified [contentScale] and returns draw area and section of drawn bitmap.
 *
 * [BeforeAfterImageScope] extends [ImageScope] that returns draw area dimensions and image draw rect
 * and touch position of user on screen.
 *
 * @param beforeImage image that show initial progress
 * @param afterImage image that show final progress
 * @param enableProgressWithTouch flag to enable drag and change progress with touch
 * @param enableZoom when enabled images are zoomable and pannable
 * @param order order of images to be drawn
 * @param alignment determines where image will be aligned inside [BoxWithConstraints]
 * This is observable when bitmap image/width ratio differs from [Canvas] that draws [ImageBitmap]
 * @param contentDescription text used by accessibility services to describe what this image
 * represents. This should always be provided unless this image is used for decorative purposes,
 * and does not represent a meaningful action that a user can take. This text should be
 * localized, such as by using [androidx.compose.ui.res.stringResource] or similar
 * @param contentScale how image should be scaled inside Canvas to match parent dimensions.
 * [ContentScale.Fit] for instance maintains src ratio and scales image to fit inside the parent.
 * @param alpha Opacity to be applied to [beforeImage] from 0.0f to 1.0f representing
 * fully transparent to fully opaque respectively
 * @param colorFilter ColorFilter to apply to the [beforeImage] when drawn into the destination
 * @param filterQuality Sampling algorithm applied to the [beforeImage] when it is scaled and drawn
 * into the destination. The default is [FilterQuality.Low] which scales using a bilinear
 * sampling algorithm
 * @param content is a Composable that can be matched at exact position where [beforeImage] is drawn.
 * This is useful for drawing thumbs, cropping or another layout that should match position
 * with the image that is scaled is drawn
 */
@Composable
fun BeforeAfterImage(
    modifier: Modifier = Modifier,
    beforeImage: ImageBitmap,
    afterImage: ImageBitmap,
    enableProgressWithTouch: Boolean = true,
    enableZoom: Boolean = true,
    order: Order = Order.BeforeAfter,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    contentDescription: String? = null,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    content: @Composable BeforeAfterImageScope.() -> Unit
) {
    var progress by remember { mutableStateOf(50f) }

    BeforeAfterImage(
        modifier = modifier,
        beforeImage = beforeImage,
        afterImage = afterImage,
        progress = progress,
        onProgressChange = {
            progress = it
        },
        order = order,
        enableProgressWithTouch = enableProgressWithTouch,
        enableZoom = enableZoom,
        alignment = alignment,
        contentScale = contentScale,
        contentDescription = contentDescription,
        alpha = alpha,
        colorFilter = colorFilter,
        filterQuality = filterQuality,
        content = content
    )
}

/**
 * A composable that lays out and draws a given [beforeImage] and [afterImage] at given [order]
 * with specified [contentScale] and returns draw area and section of drawn bitmap.
  *
 * [BeforeAfterImageScope] extends [ImageScope] that returns draw area dimensions and image draw rect
 * and touch position of user on screen.
 *
 * @param beforeImage image that show initial progress
 * @param afterImage image that show final progress
 * @param progress current before/after or after/before image display ratio between [0f-100f]
 * @param onProgressChange callback to notify use about [progress] has changed
 * @param enableProgressWithTouch flag to enable drag and change progress with touch
 * @param enableZoom when enabled images are zoomable and pannable
 * @param order order of images to be drawn
 * @param alignment determines where image will be aligned inside [BoxWithConstraints]
 * This is observable when bitmap image/width ratio differs from [Canvas] that draws [ImageBitmap]
 * @param contentDescription text used by accessibility services to describe what this image
 * represents. This should always be provided unless this image is used for decorative purposes,
 * and does not represent a meaningful action that a user can take. This text should be
 * localized, such as by using [androidx.compose.ui.res.stringResource] or similar
 * @param contentScale how image should be scaled inside Canvas to match parent dimensions.
 * [ContentScale.Fit] for instance maintains src ratio and scales image to fit inside the parent.
 * @param alpha Opacity to be applied to [beforeImage] from 0.0f to 1.0f representing
 * fully transparent to fully opaque respectively
 * @param colorFilter ColorFilter to apply to the [beforeImage] when drawn into the destination
 * @param filterQuality Sampling algorithm applied to the [beforeImage] when it is scaled and drawn
 * into the destination. The default is [FilterQuality.Low] which scales using a bilinear
 * sampling algorithm
 * @param content is a Composable that can be matched at exact position where [beforeImage] is drawn.
 * This is useful for drawing thumbs, cropping or another layout that should match position
 * with the image that is scaled is drawn
 */
@Composable
fun BeforeAfterImage(
    modifier: Modifier = Modifier,
    beforeImage: ImageBitmap,
    afterImage: ImageBitmap,
    @FloatRange(from = 0.0, to = 100.0) progress: Float = 50f,
    onProgressChange: ((Float) -> Unit)? = null,
    enableProgressWithTouch: Boolean = true,
    enableZoom: Boolean = true,
    order: Order = Order.BeforeAfter,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    contentDescription: String? = null,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    content: @Composable BeforeAfterImageScope.() -> Unit = {}
) {
    val semantics = if (contentDescription != null) {
        Modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Image
        }
    } else {
        Modifier
    }

    BoxWithConstraints(
        modifier = modifier
            .then(semantics),
        contentAlignment = alignment,
    ) {

        val density = LocalDensity.current

        val bitmapWidth = beforeImage.width
        val bitmapHeight = beforeImage.height

        val parenSize = getParentSize(bitmapWidth, bitmapHeight)
        val boxWidth: Float = parenSize.width.toFloat()
        val boxHeight: Float = parenSize.height.toFloat()

        // Src is Bitmap, Dst is the container(Image) that Bitmap will be displayed
        val srcSize = Size(bitmapWidth.toFloat(), bitmapHeight.toFloat())
        val dstSize = Size(boxWidth, boxHeight)

        val scaleFactor = contentScale.computeScaleFactor(srcSize, dstSize)

        // Image is the container for bitmap that is located inside Box
        // image bounds can be smaller or bigger than its parent based on how it's scaled
        val imageWidth = bitmapWidth * scaleFactor.scaleX
        val imageHeight = bitmapHeight * scaleFactor.scaleY

        // Sales and interpolates from offset from dragging to user value in valueRange
        fun scaleToUserValue(offset: Float) =
            scale(0f, boxWidth, offset, 0f, 100f)

        // Scales user value using valueRange to position on x axis on screen
        fun scaleToOffset(userValue: Float) =
            scale(0f, 100f, userValue, 0f, boxWidth)

        var rawOffset by remember {
            mutableStateOf(
                Offset(
                    x = scaleToOffset(progress),
                    y = imageHeight.coerceAtMost(boxHeight) / 2f,
                )
            )
        }

        rawOffset = rawOffset.copy(x = scaleToOffset(progress))

        var isHandleTouched by remember { mutableStateOf(false) }

        var zoom by remember { mutableStateOf(1f) }
        var pan by remember { mutableStateOf(Offset.Zero) }

        val transformModifier = Modifier.pointerInput(Unit) {
            detectTransformGestures(
                onGesture = { _: Offset, panChange: Offset, zoomChange: Float, _, _, _ ->

                    zoom = (zoom * zoomChange).coerceIn(1f, 5f)

                    val maxX = (size.width * (zoom - 1) / 2f)
                    val maxY = (size.height * (zoom - 1) / 2f)

                    val newPan = pan + panChange.times(zoom)
                    pan = Offset(
                        newPan.x.coerceIn(-maxX, maxX),
                        newPan.y.coerceIn(-maxY, maxY)
                    )
                }
            )
        }

        val touchModifier = Modifier.pointerInput(Unit) {
            detectMotionEvents(
                onDown = {
                    val position = it.position
                    val xPos = position.x

                    isHandleTouched =
                        ((rawOffset.x - xPos) * (rawOffset.x - xPos) < 10000)
                },
                onMove = {
                    if (isHandleTouched) {
                        rawOffset = rawOffset.copy(x = it.position.x)
                        onProgressChange?.invoke(
                            scaleToUserValue(rawOffset.x)
                        )
                        it.consume()
                    }
                },
                onUp = {
                    isHandleTouched = false
                }
            )
        }

        val graphicsModifier = Modifier.graphicsLayer {
            this.scaleX = zoom
            this.scaleY = zoom
            this.translationX = pan.x
            this.translationY = pan.y
        }

        val imageModifier = Modifier
            .clipToBounds()
            .then(if (enableZoom) transformModifier else Modifier)
            .then(if (enableProgressWithTouch) touchModifier else Modifier)
            .then(graphicsModifier)


        val bitmapRect = getScaledBitmapRect(
            boxWidth = boxWidth.toInt(),
            boxHeight = boxHeight.toInt(),
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            bitmapWidth = bitmapWidth,
            bitmapHeight = bitmapHeight
        )

        // Dimensions of canvas that will draw this Bitmap
        val canvasWidthInDp: Dp
        val canvasHeightInDp: Dp

        with(density) {
            canvasWidthInDp = imageWidth.coerceAtMost(boxWidth).toDp()
            canvasHeightInDp = imageHeight.coerceAtMost(boxHeight).toDp()
        }

        ImageLayout(
            modifier = imageModifier,
            constraints = constraints,
            beforeImage = beforeImage,
            afterImage = afterImage,
            position = rawOffset,
            translateX = pan.x,
            zoom = zoom,
            bitmapRect = bitmapRect,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            canvasWidthInDp = canvasWidthInDp,
            canvasHeightInDp = canvasHeightInDp,
            order = order,
            alpha = alpha,
            colorFilter = colorFilter,
            filterQuality = filterQuality,
            content = content
        )
    }
}

@Composable
private fun ImageLayout(
    modifier: Modifier,
    constraints: Constraints,
    beforeImage: ImageBitmap,
    afterImage: ImageBitmap,
    position: Offset,
    translateX: Float,
    zoom: Float,
    bitmapRect: IntRect,
    imageWidth: Float,
    imageHeight: Float,
    canvasWidthInDp: Dp,
    canvasHeightInDp: Dp,
    order: Order,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    content: @Composable BeforeAfterImageScope.() -> Unit
) {

    val density = LocalDensity.current

    // Send rectangle of Bitmap drawn to Canvas as bitmapRect, content scale modes like
    // crop might crop image from center so Rect can be such as IntRect(250,250,500,500)

    // canvasWidthInDp, and  canvasHeightInDp are Canvas dimensions coerced to Box size
    // that covers Canvas
    val imageScopeImpl = BeforeAfterImageScopeImpl(
        density = density,
        constraints = constraints,
        imageWidth = canvasWidthInDp,
        imageHeight = canvasHeightInDp,
        rect = bitmapRect,
        position = position
    )

    // width and height params for translating draw position if scaled Image dimensions are
    // bigger than Canvas dimensions
    ImageImpl(
        modifier = modifier.size(canvasWidthInDp, canvasHeightInDp),
        beforeImage = beforeImage,
        afterImage = afterImage,
        position = position,
        translateX = translateX,
        zoom = zoom,
        alpha = alpha,
        width = imageWidth.toInt(),
        height = imageHeight.toInt(),
        order = order,
        colorFilter = colorFilter,
        filterQuality = filterQuality
    )

    imageScopeImpl.content()
}

@Composable
private fun ImageImpl(
    modifier: Modifier,
    beforeImage: ImageBitmap,
    afterImage: ImageBitmap,
    position: Offset,
    translateX: Float,
    zoom: Float,
    width: Int,
    height: Int,
    order: Order,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
) {
    val bitmapWidth = beforeImage.width
    val bitmapHeight = beforeImage.height

    Box {
        Canvas(modifier = modifier) {

            val canvasWidth = size.width
            val canvasHeight = size.height

            // First add translation for crop and other content scale
            // then get user touch position on any zoom level to get raw value
            val touchPosition =
                (width - canvasWidth) / 2f + (position.x / zoom)
                    .coerceIn(0f, canvasWidth)
                    .toInt()

            // Translate to left or down when Image size is bigger than this canvas.
            // ImageSize is bigger when scale modes like Crop is used which enlarges image
            // For instance 1000x1000 image can be 1000x2000 for a Canvas with 1000x1000
            // so top is translated -500 to draw center of ImageBitmap
            translate(
                top = (-height + canvasHeight) / 2f,
                left = (-width + canvasWidth) / 2f,
            ) {

                // This is pan limit set to limit panning inside image borders
                val maxX = (size.width * (zoom - 1) / 2f)
                // Get actual pan value
                val pan = (maxX - translateX) / zoom

                val srcOffsetX = ((pan + touchPosition) * bitmapWidth / width).toInt()
                val dstOffsetX = (pan + touchPosition).toInt()

                if (order == Order.BeforeAfter) {
                    drawImage(
                        beforeImage,
                        srcSize = IntSize(bitmapWidth, bitmapHeight),
                        dstSize = IntSize(width, height),
                        alpha = alpha,
                        colorFilter = colorFilter,
                        filterQuality = filterQuality
                    )
                    drawImage(
                        afterImage,
                        srcSize = IntSize(bitmapWidth, bitmapHeight),
                        srcOffset = IntOffset(srcOffsetX, 0),
                        dstSize = IntSize(width, height),
                        dstOffset = IntOffset(dstOffsetX, 0),
                        alpha = alpha,
                        colorFilter = colorFilter,
                        filterQuality = filterQuality
                    )
                } else {
                    drawImage(
                        afterImage,
                        srcSize = IntSize(bitmapWidth, bitmapHeight),
                        dstSize = IntSize(width, height),
                        alpha = alpha,
                        colorFilter = colorFilter,
                        filterQuality = filterQuality
                    )
                    drawImage(
                        beforeImage,
                        srcSize = IntSize(bitmapWidth, bitmapHeight),
                        srcOffset = IntOffset(srcOffsetX, 0),
                        dstSize = IntSize(width, height),
                        dstOffset = IntOffset(dstOffsetX, 0),
                        alpha = alpha,
                        colorFilter = colorFilter,
                        filterQuality = filterQuality
                    )
                }
            }
        }
    }
}

enum class Order {
    BeforeAfter, AfterBefore
}