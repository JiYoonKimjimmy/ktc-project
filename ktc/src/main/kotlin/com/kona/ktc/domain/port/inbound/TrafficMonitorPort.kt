package com.kona.ktc.domain.port.inbound

/**
 * Port for monitoring traffic updates via SSE
 */
interface TrafficMonitorPort {
    /**
     * Register a client for traffic monitoring
     * @param clientId Unique identifier for the client
     */
    suspend fun registerClient(clientId: String)

    /**
     * Unregister a client from traffic monitoring
     * @param clientId Unique identifier for the client to unregister
     */
    suspend fun unregisterClient(clientId: String)

    /**
     * Get traffic updates for a client
     * @param clientId Unique identifier for the client
     * @return Flow of traffic updates
     */
    suspend fun getTrafficUpdates(clientId: String)
}
