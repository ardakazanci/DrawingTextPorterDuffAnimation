package com.ardakazanci.drawingtextporterduffanimation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ardakazanci.drawingtextporterduffanimation.ui.theme.DrawingTextPorterDuffAnimationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrawingTextPorterDuffAnimationTheme {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    GhostTextAnimation()
                }
            }
        }
    }
}

@Composable
fun GhostTextAnimation() {
    val context = LocalContext.current
    val density = LocalDensity.current

    val textWidth = with(density) { 200.dp.toPx() }
    val animateWidth = with(density) { 170.dp.toPx() }

    val infiniteTransition = rememberInfiniteTransition()

    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = animateWidth,
        targetValue = -animateWidth,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "animatedOffset"
    )

    val animatedScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "animatedScale"
    )

    val animatedColor by infiniteTransition.animateColor(
        initialValue = Color.Black,
        targetValue = Color.Red,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "animatedColor"
    )

    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "rotateAngle"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {

        val ghostBitmap = remember {
            BitmapFactory.decodeResource(context.resources, R.drawable.ghost)
        }

        val resizedGhostBitmap = Bitmap.createScaledBitmap(
            ghostBitmap,
            (textWidth / 2).toInt(),
            (textWidth / 2).toInt(),
            true
        )

        Canvas(
            modifier = Modifier
                .width(400.dp)
                .height(400.dp)
                .graphicsLayer(
                    scaleX = animatedScale,
                    scaleY = animatedScale
                )
        ) {

            val textBitmap = Bitmap.createBitmap(
                size.width.toInt(),
                size.height.toInt(),
                Bitmap.Config.ARGB_8888
            )
            val textCanvas = android.graphics.Canvas(textBitmap)

            val textPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize = 48.sp.toPx()
                color = animatedColor.toArgb()
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.create("", android.graphics.Typeface.BOLD)
            }

            val textX = size.width / 2
            val textY = size.height / 2 - (textPaint.descent() + textPaint.ascent()) / 2

            textCanvas.drawText("GHOST", textX, textY, textPaint)


            val glowPaint = android.graphics.Paint().apply {
                color = animatedColor.copy(alpha = 0.5f).toArgb()
                maskFilter =
                    android.graphics.BlurMaskFilter(30f, android.graphics.BlurMaskFilter.Blur.OUTER)
            }

            textCanvas.drawText("GHOST", textX, textY, glowPaint)

            drawIntoCanvas { canvas ->

                val layerPaint = android.graphics.Paint()
                val saveLayerCount = canvas.nativeCanvas.saveLayer(null, layerPaint)
                val paint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.XOR)
                }
                val xPosition = animatedOffset
                val yPosition = size.height / 2 - resizedGhostBitmap.height / 2


                with(canvas.nativeCanvas) {
                    drawBitmap(textBitmap, 0f, 0f, null)
                    save()
                    rotate(
                        rotateAngle,
                        (xPosition + resizedGhostBitmap.width / 2),
                        (yPosition + resizedGhostBitmap.height / 2)
                    )
                    drawBitmap(resizedGhostBitmap, xPosition, yPosition, paint)
                    restore()
                    restoreToCount(saveLayerCount)
                }
            }
        }
    }
}