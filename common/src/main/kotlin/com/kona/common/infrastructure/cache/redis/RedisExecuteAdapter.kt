package com.kona.common.infrastructure.cache.redis

import org.springframework.data.redis.core.script.RedisScript

interface RedisExecuteAdapter {

    suspend fun execute(script: RedisScript<List<*>>, keys: List<String>, args: List<String>): List<*>

    suspend fun keys(pattern: String): List<String>

    suspend fun getValue(key: String): String?

    suspend fun setValue(key: String, value: String): Boolean

    suspend fun getSizeForZSet(key: String): Long

    suspend fun addValueForSet(key: String, value: String): Long

    suspend fun getValuesForSet(key: String): List<String>

}