package com.github.arlidhr.posts_offline_app.networking

import kotlinx.coroutines.flow.Flow

/**
 * Interface to observe network connectivity changes.
 * Exposes a [Flow] that emits `true` when the device is online, `false` when offline.
 *
 * Designed for dependency inversion: the domain/presentation layers depend on this
 * interface, not the Android-specific implementation. This enables easy mocking in tests.
 */
interface ConnectivityObserver {
    val isConnected: Flow<Boolean>
}
