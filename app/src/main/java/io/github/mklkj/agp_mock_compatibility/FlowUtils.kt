package io.github.mklkj.agp_mock_compatibility

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

inline fun <ResultType, RequestType> networkBoundResource(
    mutex: Mutex = Mutex(),
    showSavedOnLoading: Boolean = true,
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend (ResultType) -> RequestType,
    crossinline saveFetchResult: suspend (old: ResultType, new: RequestType) -> Unit,
    crossinline onFetchFailed: (Throwable) -> Unit = { },
    crossinline shouldFetch: (ResultType) -> Boolean = { true },
    crossinline filterResult: (ResultType) -> ResultType = { it }
) = flow {
    emit(Resource.loading())

    val data = query().first()
    emitAll(if (shouldFetch(data)) {
        if (showSavedOnLoading) emit(Resource.loading(filterResult(data)))

        try {
            val newData = fetch(data)
            mutex.withLock { saveFetchResult(query().first(), newData) }
            query().map { Resource.success(filterResult(it)) }
        } catch (throwable: Throwable) {
            onFetchFailed(throwable)
            query().map { Resource.error(throwable, filterResult(it)) }
        }
    } else {
        query().map { Resource.success(filterResult(it)) }
    })
}
