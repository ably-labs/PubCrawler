package com.ablylabs.pubcrawler.ui

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.ablylabs.pubcrawler.pubs.Pub
import com.ablylabs.pubcrawler.realtime.FlowyRealtimePub
import com.ablylabs.pubcrawler.realtime.LeaveResult
import com.ablylabs.pubcrawler.realtime.PubGoer
import kotlinx.coroutines.launch

class PubViewModel(private val flowyPub: FlowyRealtimePub) : ViewModel() {
    private val _leaveResult = MutableLiveData<LeaveResult>()
    val leaveResult: LiveData<LeaveResult> = _leaveResult
    fun leavePub(who: PubGoer, which: Pub) {
        viewModelScope.launch {
            _leaveResult.value = flowyPub.leave(who, which)
        }
    }
}

class PubViewModelFactory(
    owner: SavedStateRegistryOwner,
    private val flowyPub: FlowyRealtimePub
) : AbstractSavedStateViewModelFactory(owner,null) {
    override fun <T : ViewModel> create(
        key: String, modelClass: Class<T>, handle: SavedStateHandle
    ): T {
        return PubViewModel(flowyPub) as T
    }
}