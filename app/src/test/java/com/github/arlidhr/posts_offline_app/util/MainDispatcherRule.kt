@file:OptIn(ExperimentalCoroutinesApi::class)

package com.github.arlidhr.posts_offline_app.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit Rule that replaces [Dispatchers.Main] with a [TestDispatcher] for the duration of each test.
 *
 * This ensures that coroutines launched in [androidx.lifecycle.viewModelScope] use
 * a controllable test dispatcher, enabling deterministic and synchronous test execution.
 *
 * [UnconfinedTestDispatcher] is the default: coroutines start eagerly without needing
 * `advanceUntilIdle()`, making state-assertion tests straightforward.
 *
 * For tests that involve time (e.g. debounce), pass the same [testDispatcher] instance
 * to [kotlinx.coroutines.test.runTest] so that [kotlinx.coroutines.test.advanceTimeBy]
 * shares the same virtual clock as `viewModelScope`.
 *
 * Usage:
 * ```kotlin
 * @get:Rule val mainDispatcherRule = MainDispatcherRule()
 *
 * @Test
 * fun `debounce fires after 300ms`() = runTest(mainDispatcherRule.testDispatcher) {
 *     viewModel.onSearchQueryChange("kotlin")
 *     advanceTimeBy(300L)
 *     verify { searchUseCase("kotlin") }
 * }
 * ```
 */
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
