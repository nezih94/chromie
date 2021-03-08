/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.animation.FloatEvaluator
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androiddevchallenge.ui.CountdownManager
import com.example.androiddevchallenge.ui.theme.MyTheme
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                MyApp()
            }
        }
    }
}

// Start building your app here!
@Composable
fun MyApp(countdownManager: CountdownManager = viewModel()) {
    Surface(
        color = Color(
            0xFFEEEEEE
        ),
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        val clockState: ClockStateInfo by countdownManager.clockState.observeAsState(initial = ClockStateInfo())
        var progress by remember { mutableStateOf(0f) }
        val counting by countdownManager.counting.observeAsState(initial = false)

        var selectedIndex by remember { mutableStateOf(-1) }
        var selectedMillis by remember { mutableStateOf(0L) }

        LaunchedEffect(key1 = clockState) {
            if (clockState.animate) {
                animate(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = clockState.animDuration,
                    )
                ) { value, velocity ->
                    progress = value
                }
                // workaround for cause-unknown anim glitch
                countdownManager.stabilizeAtState(
                    clockState.n1,
                    clockState.n2,
                    clockState.n3,
                    clockState.n4
                )
            } else {
                progress = 0f
            }
        }

        MillionTimes(clockState, progress)
        Column(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            Spacer(
                modifier = Modifier
                    .aspectRatio(0.833f)
                    .fillMaxWidth()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, true)
                    .padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                IconButton(
                    modifier = Modifier
                        .size(37.dp)
                        .background(color = Color.LightGray, shape = RoundedCornerShape(6.dp)),
                    onClick = { if (!counting) countdownManager.startCountdown(selectedMillis) else countdownManager.stopCountdown() }
                ) {
                    val imgRes: Int
                    if (counting) {
                        imgRes = R.drawable.ic_outline_stop_24
                    } else {
                        imgRes = R.drawable.ic_outline_play_arrow_24
                    }
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = ImageVector.vectorResource(id = imgRes),
                        contentDescription = null
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp),
                    content = {

                        for (i in 0 until 114) {
                            val millis: Long = 180000L + (i * 30000)
                            item {
                                val bgColor: Color
                                val strokeColor: Color
                                if (i == selectedIndex) {
                                    bgColor = Color.LightGray
                                    strokeColor = Color.Black
                                } else {
                                    bgColor = Color(0xFFE0E0E0)
                                    strokeColor = Color(0xFFE0E0E0)
                                }

                                Button(
                                    onClick = {
                                        selectedMillis = millis
                                        selectedIndex = i
                                        countdownManager.processMillis(millis)
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, strokeColor),
                                    colors = ButtonDefaults.buttonColors(backgroundColor = bgColor),
                                    enabled = !counting
                                ) {
                                    val formatter: DateFormat = SimpleDateFormat("mm:ss", Locale.US)
                                    val timeText: String = formatter.format(Date(millis))
                                    Text(text = timeText, color = Color.Black)
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MillionTimes(clockState: ClockStateInfo, progress: Float) {
    Column(
        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth()) {
            ClockDigit(
                clockState.o1, clockState.n1, progress, 0,
                Modifier
                    .weight(0.5f)
                    .aspectRatio(0.833f)
            )
            ClockDigit(
                clockState.o2, clockState.n2, progress, 1,
                Modifier
                    .weight(0.5f)
                    .aspectRatio(0.833f)
            )
        }
        Row {
            ClockDigit(
                clockState.o3, clockState.n3, progress, 2,
                Modifier
                    .weight(0.5f)
                    .aspectRatio(0.833f)
            )
            ClockDigit(
                clockState.o4, clockState.n4, progress, 3,
                Modifier
                    .weight(0.5f)
                    .aspectRatio(0.833f)
            )
        }
    }
}

val floatEvaluator = FloatEvaluator()

@Composable
fun ClockDigit(fromState: Int, toState: Int, progress: Float, clockIndex: Int, modifier: Modifier) {

    Canvas(
        modifier = modifier,
        onDraw = {
            val columnCount = 5
            val rowCount = 6
            val R = size.width / columnCount
            val radius = R / 2

            for (c in 0 until columnCount) {
                for (r in 0 until rowCount) {
                    val gridIndex: Int = c + (r * columnCount)
                    val clockCenter = Offset(x = (R * c) + radius, y = (R * r) + radius)
                    drawCircle(color = Color(0xFFEEEEEE), radius = radius, center = clockCenter)
                    drawCircle(
                        color = Color(0xFF5C5C5C),
                        radius = radius,
                        center = clockCenter,
                        style = Stroke(width = radius * 0.05f)
                    )

                    // akrep
                    rotate(
                        floatEvaluator.evaluate(
                            progress,
                            rotationData[gridIndex][fromState].first,
                            rotationData[gridIndex][toState].first
                        ),
                        pivot = clockCenter
                    ) {
                        val length = radius * 0.8f
                        val width = radius * 0.15f
                        drawLine(
                            color = Color.Black,
                            start = clockCenter,
                            end = Offset(clockCenter.x, clockCenter.y - length),
                            strokeWidth = width,
                            cap = StrokeCap.Round
                        )
                    }
                    // yelkovan
                    rotate(
                        floatEvaluator.evaluate(
                            progress,
                            rotationData[gridIndex][fromState].second,
                            rotationData[gridIndex][toState].second
                        ),
                        pivot = clockCenter
                    ) {
                        val lenght = radius * 0.9f
                        val width = radius * 0.15f
                        drawLine(
                            color = Color.Black,
                            start = clockCenter,
                            end = Offset(clockCenter.x, clockCenter.y - lenght),
                            strokeWidth = width,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    )
}

data class ClockStateInfo(
    var o1: Int = 0,
    var n1: Int = 0,
    var o2: Int = 0,
    var n2: Int = 0,
    var o3: Int = 0,
    var n3: Int = 0,
    var o4: Int = 0,
    var n4: Int = 0,
    var animate: Boolean = true,
    var animDuration: Int = 900
)

val rotationData = listOf(
    listOf(
        Pair(180f, 90f),
        Pair(225f, 225f),
        Pair(180f, 90f),
        Pair(180f, 90f),
        Pair(180f, 90f),
        Pair(180f, 90f),
        Pair(180f, 90f),
        Pair(180f, 90f),
        Pair(180f, 90f),
        Pair(180f, 90f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),
    listOf(
        Pair(270f, 90f),
        Pair(180f, 90f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(270f, 180f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),
    listOf(
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(225f, 225f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),
    listOf(
        Pair(270f, 90f),
        Pair(270f, 180f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(225f, 225f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),
    listOf(
        Pair(270f, 180f),
        Pair(225f, 225f),
        Pair(270f, 180f),
        Pair(270f, 180f),
        Pair(225f, 225f),
        Pair(270f, 180f),
        Pair(270f, 180f),
        Pair(270f, 180f),
        Pair(270f, 180f),
        Pair(270f, 180f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),

    listOf(
        Pair(180f, 0f),
        Pair(225f, 225f),
        Pair(0f, 90f),
        Pair(0f, 90f),
        Pair(180f, 0f),
        Pair(0f, 180f),
        Pair(0f, 180f),
        Pair(0f, 90f),
        Pair(0f, 180f),
        Pair(0f, 180f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),
    listOf(
        Pair(180f, 90f),
        Pair(0f, 90f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(180f, 0f),
        Pair(180f, 90f),
        Pair(180f, 90f),
        Pair(270f, 90f),
        Pair(180f, 90f),
        Pair(180f, 90f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),
    listOf(
        Pair(270f, 90f),
        Pair(270f, 180f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(225f, 225f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),
    listOf(
        Pair(270f, 180f),
        Pair(0f, 180f),
        Pair(270f, 180f),
        Pair(270f, 180f),
        Pair(225f, 225f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(270f, 225f),
        Pair(270f, 180f),
        Pair(270f, 180f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),
    listOf(
        Pair(180f, 0f),
        Pair(225f, 225f),
        Pair(0f, 180f),
        Pair(0f, 180f),
        Pair(225f, 225f),
        Pair(270f, 0f),
        Pair(270f, 0f),
        Pair(0f, 225f),
        Pair(0f, 180f),
        Pair(180f, 0f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),

    listOf(
        Pair(180f, 0f),
        Pair(225f, 225f),
        Pair(180f, 90f),
        Pair(180f, 90f),
        Pair(180f, 0f),
        Pair(0f, 180f),
        Pair(0f, 180f),
        Pair(225f, 225f),
        Pair(180f, 0f),
        Pair(0f, 180f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),
    listOf(
        Pair(180f, 0f),
        Pair(225f, 225f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(0f, 180f),
        Pair(0f, 90f),
        Pair(0f, 90f),
        Pair(225f, 225f),
        Pair(0f, 90f),
        Pair(0f, 90f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),
    listOf(
        Pair(225f, 225f),
        Pair(0f, 180f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(180f, 90f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(45f, 180f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),
    listOf(
        Pair(0f, 180f),
        Pair(0f, 180f),
        Pair(270f, 0f),
        Pair(270f, 0f),
        Pair(270f, 180f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(180f, 45f),
        Pair(270f, 0f),
        Pair(270f, 0f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),
    listOf(
        Pair(180f, 0f),
        Pair(225f, 225f),
        Pair(0f, 180f),
        Pair(180f, 0f),
        Pair(225f, 225f),
        Pair(270f, 180f),
        Pair(270f, 180f),
        Pair(225f, 225f),
        Pair(0f, 180f),
        Pair(0f, 180f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),

    listOf(
        Pair(180f, 0f),
        Pair(225f, 225f),
        Pair(0f, 180f),
        Pair(0f, 90f),
        Pair(180f, 0f),
        Pair(0f, 90f),
        Pair(0f, 180f),
        Pair(225f, 225f),
        Pair(0f, 180f),
        Pair(0f, 90f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),
    listOf(
        Pair(0f, 180f),
        Pair(225f, 225f),
        Pair(180f, 90f),
        Pair(270f, 90f),
        Pair(0f, 90f),
        Pair(270f, 90f),
        Pair(180f, 90f),
        Pair(225f, 225f),
        Pair(180f, 90f),
        Pair(270f, 90f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),
    listOf(
        Pair(225f, 225f),
        Pair(0f, 180f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(270f, 0f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(0f, 180f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),
    listOf(
        Pair(0f, 180f),
        Pair(0f, 180f),
        Pair(270f, 90f),
        Pair(270f, 180f),
        Pair(0f, 90f),
        Pair(270f, 180f),
        Pair(270f, 180f),
        Pair(0f, 180f),
        Pair(270f, 180f),
        Pair(270f, 180f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),
    listOf(
        Pair(180f, 0f),
        Pair(225f, 225f),
        Pair(270f, 0f),
        Pair(0f, 180f),
        Pair(270f, 180f),
        Pair(0f, 180f),
        Pair(0f, 180f),
        Pair(225f, 225f),
        Pair(180f, 0f),
        Pair(0f, 180f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),

    listOf(
        Pair(180f, 0f),
        Pair(225f, 225f),
        Pair(0f, 180f),
        Pair(180f, 90f),
        Pair(0f, 90f),
        Pair(180f, 90f),
        Pair(180f, 0f),
        Pair(225f, 225f),
        Pair(180f, 0f),
        Pair(90f, 180f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),
    listOf(
        Pair(90f, 0f),
        Pair(225f, 225f),
        Pair(0f, 90f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(0f, 90f),
        Pair(225f, 225f),
        Pair(0f, 90f),
        Pair(270f, 90f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),
    listOf(
        Pair(270f, 90f),
        Pair(0f, 180f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(270f, 180f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(0f, 180f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),
    listOf(
        Pair(270f, 0f),
        Pair(0f, 180f),
        Pair(270f, 90f),
        Pair(270f, 0f),
        Pair(180f, 90f),
        Pair(270f, 0f),
        Pair(270f, 0f),
        Pair(0f, 180f),
        Pair(270f, 0f),
        Pair(270f, 0f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),
    listOf(
        Pair(180f, 0f),
        Pair(225f, 225f),
        Pair(270f, 180f),
        Pair(0f, 180f),
        Pair(270f, 0f),
        Pair(0f, 180f),
        Pair(0f, 180f),
        Pair(225f, 225f),
        Pair(180f, 0f),
        Pair(0f, 180f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),

    listOf(
        Pair(90f, 0f),
        Pair(225f, 225f),
        Pair(0f, 90f),
        Pair(0f, 90f),
        Pair(225f, 225f),
        Pair(0f, 90f),
        Pair(0f, 90f),
        Pair(225f, 225f),
        Pair(0f, 90f),
        Pair(0f, 90f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),
    listOf(
        Pair(270f, 90f),
        Pair(225f, 225f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(225f, 225f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(225f, 225f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),
    listOf(
        Pair(270f, 90f),
        Pair(0f, 90f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(0f, 90f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(0f, 90f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),
    listOf(
        Pair(270f, 90f),
        Pair(0f, 270f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(270f, 0f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(0f, 270f),
        Pair(270f, 90f),
        Pair(270f, 90f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    ),
    listOf(
        Pair(270f, 0f),
        Pair(225f, 225f),
        Pair(270f, 0f),
        Pair(270f, 0f),
        Pair(225f, 225f),
        Pair(270f, 0f),
        Pair(270f, 0f),
        Pair(225f, 225f),
        Pair(270f, 0f),
        Pair(270f, 0f),
        Pair(225f, 45f),
        Pair(315f, 135f)
    )
)
