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

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CountdownManager : ViewModel() {

    var clockState =
        MutableLiveData<ClockStateInfo>(ClockStateInfo(10, 10, 10, 10, 10, 10, 10, 10, false))
    var counting = MutableLiveData<Boolean>(false)

    var countDownTimer: CountDownTimer? = null

    init {
        viewModelScope.launch {
            delay(2000)
            updateClockState(11, 11, 11, 11, animDuration = 2000)
            delay(3000)
            updateClockState(10, 10, 10, 10, animDuration = 2000)
        }
    }

    fun startCountdown(millis: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(remainingMillis: Long) {
                processMillis(remainingMillis)
            }

            override fun onFinish() {
                counting.value = false
            }
        }.start()
        counting.value = true
    }

    fun stopCountdown() {
        countDownTimer?.cancel()
        counting.value = false
    }

    fun processMillis(millis: Long) {
        val formatter: DateFormat = SimpleDateFormat("mm:ss", Locale.US)
        val timeText: String = formatter.format(Date(millis))
        val digit1 = timeText.get(0).toString()
        val digit2 = timeText.get(1).toString()
        val digit3 = timeText.get(3).toString()
        val digit4 = timeText.get(4).toString()
        updateClockState(digit1.toInt(), digit2.toInt(), digit3.toInt(), digit4.toInt())
    }

    fun updateClockState(
        state1: Int,
        state2: Int,
        state3: Int,
        state4: Int,
        animDuration: Int = 900
    ) {
        val currentClockState = clockState.value!!
        val newClockState = ClockStateInfo(
            o1 = currentClockState.n1,
            n1 = state1,
            o2 = currentClockState.n2,
            n2 = state2,
            o3 = currentClockState.n3,
            n3 = state3,
            o4 = currentClockState.o4,
            n4 = state4,
            animDuration = animDuration
        )
        clockState.value = newClockState
    }

    fun stabilizeAtState(state1: Int, state2: Int, state3: Int, state4: Int) {
        val state =
            ClockStateInfo(state1, state1, state2, state2, state3, state3, state4, state4, false)
        clockState.value = state
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }
}
