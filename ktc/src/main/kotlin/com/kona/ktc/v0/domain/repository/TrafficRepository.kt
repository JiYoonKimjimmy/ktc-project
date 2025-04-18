package com.kona.ktc.v0.domain.repository

import com.kona.ktc.v0.domain.model.TrafficToken
import com.kona.ktc.v0.domain.model.TrafficWaiting

interface TrafficRepository {
    suspend fun controlTraffic(token: TrafficToken): TrafficWaiting
} 