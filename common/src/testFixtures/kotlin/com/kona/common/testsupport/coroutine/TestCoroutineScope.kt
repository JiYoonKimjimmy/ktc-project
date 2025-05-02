package com.kona.common.testsupport.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class TestCoroutineScope {
    companion object {
        val defaultCoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    }
}