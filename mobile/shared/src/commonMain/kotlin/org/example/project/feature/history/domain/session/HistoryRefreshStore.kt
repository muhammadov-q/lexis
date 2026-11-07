package org.example.project.feature.history.domain.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HistoryRefreshStore {
    private val _refreshVersion = MutableStateFlow(0L)
    val refreshVersion = _refreshVersion.asStateFlow()

    fun markHistoryDirty() {
        _refreshVersion.value += 1
    }
}

