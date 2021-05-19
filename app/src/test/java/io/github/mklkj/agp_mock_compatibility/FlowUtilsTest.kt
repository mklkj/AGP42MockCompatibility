package io.github.mklkj.agp_mock_compatibility


import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FlowUtilsKtTest {

    private val testScope = TestCoroutineScope()

    @Test
    fun `fetch from two places with same remote data`() {
        val repo = mockk<TestRepo>()
        coEvery { repo.query() } returnsMany listOf(
            // initial data
            flowOf(listOf(1, 2, 3)),
            flowOf(listOf(1, 2, 3)),

            // for first
            flowOf(listOf(1, 2, 3)), // before save
            flowOf(listOf(2, 3, 4)), // after save

            // for second
            flowOf(listOf(2, 3, 4)), // before save
            flowOf(listOf(2, 3, 4)), // after save
        )
        coEvery { repo.fetch() } returnsMany listOf(
            listOf(2, 3, 4),
            listOf(2, 3, 4),
        )
        coEvery { repo.save(any(), any()) } just Runs

        // first
        networkBoundResource(
            showSavedOnLoading = false,
            query = { repo.query() },
            fetch = {
                val data = repo.fetch()
                delay(2_000)
                data
            },
            saveFetchResult = { old, new -> repo.save(old, new) }
        ).launchIn(testScope)

        testScope.advanceTimeBy(1_000)

        // second
        networkBoundResource(
            showSavedOnLoading = false,
            query = { repo.query() },
            fetch = {
                val data = repo.fetch()
                delay(2_000)
                data
            },
            saveFetchResult = { old, new -> repo.save(old, new) }
        ).launchIn(testScope)

        testScope.advanceTimeBy(3_000)

        coVerifyOrder {
            // from first
            repo.query()
            repo.fetch() // hang for 2 sec

            // wait 1 sec

            // from second
            repo.query()
            repo.fetch() // hang for 2 sec

            // from first
            repo.query()
            repo.save(withArg {
                assertEquals(listOf(1, 2, 3), it)
            }, any())
            repo.query()

            // from second
            repo.query()
            repo.save(withArg {
                assertEquals(listOf(2, 3, 4), it)
            }, any())
            repo.query()
        }
    }

    @Suppress("UNUSED_PARAMETER", "RedundantSuspendModifier")
    private class TestRepo {
        fun query() = flowOf<List<Int>>()
        suspend fun fetch() = listOf<Int>()
        suspend fun save(old: List<Int>, new: List<Int>) {}
    }
}
