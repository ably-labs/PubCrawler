package com.ablylabs.pubcrawler.ui

import android.util.Log
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.ablylabs.pubcrawler.pubs.Pub
import com.ablylabs.pubcrawler.realtime.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private const val TAG = "PubViewModel"

class PubViewModel(private val flowyPub: FlowyRealtimePub) : ViewModel() {
    //following needs to be transformed from flows, they need not to be exposed like this
    private val _presenceActions = MutableLiveData<PubPresenceActions>()
    val presenceActions: LiveData<PubPresenceActions> = _presenceActions

    //
    //following needs to be transformed from flows, they need not to be exposed like this
    private val _pubActions = MutableLiveData<PubActions>()
    val pubActions: LiveData<PubActions> = _pubActions

    private val _leaveResult = MutableLiveData<LeaveResult>()
    val leaveResult: LiveData<LeaveResult> = _leaveResult

    private val _joinResult = MutableLiveData<JoinResult>()
    val joinResult: LiveData<JoinResult> = _joinResult

    private val _acceptDrinkResult = MutableLiveData<AcceptDrinkResult>()
    val acceptDrinkResult: LiveData<AcceptDrinkResult> = _acceptDrinkResult

    private val _rejectDrinkResult = MutableLiveData<RejectDrinkResult>()
    val rejectDrinkResult: LiveData<RejectDrinkResult> = _rejectDrinkResult

    private val _messageSentResult = MutableLiveData<MessageSentResult>()
    val messageSentResult: LiveData<MessageSentResult> = _messageSentResult

    private val _offerDrinkResult = MutableLiveData<OfferSentResult>()
    val offerDrinkResult: LiveData<OfferSentResult> = _offerDrinkResult

    private val _offerResponse = MutableLiveData<DrinkOfferResponse>()
    val offerResponse: LiveData<DrinkOfferResponse> = _offerResponse

    private val _allPubGoers = MutableLiveData<List<PubGoer>>()
    val allPubGoers: LiveData<List<PubGoer>> = _allPubGoers


    fun leavePub(who: PubGoer, which: Pub) {
        viewModelScope.launch {
            _leaveResult.value = flowyPub.leave(who, which)
            if (_leaveResult.value is LeaveResult) {
                _allPubGoers.value = flowyPub.allPubGoers(which)
            }
        }
    }

    fun joinPub(who: PubGoer, which: Pub) {
        viewModelScope.launch {
            _joinResult.value = flowyPub.join(which, who)
            _allPubGoers.value = flowyPub.allPubGoers(which)
            if (_joinResult.value is JoinResult.Success) {
                launch { buildActionFlowFor(which, who) }
                launch { buildPresenceFlow(which, who) }
            }
        }
    }

    private suspend fun buildPresenceFlow(
        which: Pub,
        who: PubGoer
    ) {
        flowyPub.buildPresenceFlow(pub = which).collect {
            Log.d(TAG, "buildPresenceFlow: presence flow collect")
            _presenceActions.value = it
            //also refresh users again
            _allPubGoers.value = flowyPub.allPubGoers(which)
            //rebuild action flow
            buildActionFlowFor(which, who)
        }
    }

    private suspend fun buildActionFlowFor(
        which: Pub,
        who: PubGoer
    ) {
        flowyPub.buildActionFlow(which, who).collect {
            _pubActions.value = it
        }
    }

    fun acceptDrink(who: PubGoer, fromWhom: PubGoer) {
        viewModelScope.launch {
            _acceptDrinkResult.value = flowyPub.acceptDrink(who, fromWhom)
        }
    }

    fun rejectDrink(who: PubGoer, fromWhom: PubGoer) {
        viewModelScope.launch {
            _rejectDrinkResult.value = flowyPub.rejectDrink(who, fromWhom)
        }
    }

    fun sendTextMessage(who: PubGoer, toWhom: PubGoer, message: String) {
        viewModelScope.launch {
            _messageSentResult.value = flowyPub.sendTextMessage(who, toWhom, message)
        }
    }

    fun offerDrinkTo(who: PubGoer, toWhom: PubGoer) {
        viewModelScope.launch {
            _offerDrinkResult.value = flowyPub.offerDrink(who, toWhom)
            if (_offerDrinkResult.value is OfferSentResult.Success) {
                _offerResponse.value =
                    flowyPub.registerToDrinkOfferResponse(offered = toWhom, offeree = who)
            }
        }
    }
}

class PubViewModelFactory(
    owner: SavedStateRegistryOwner,
    private val flowyPub: FlowyRealtimePub
) : AbstractSavedStateViewModelFactory(owner, null) {
    override fun <T : ViewModel> create(
        key: String, modelClass: Class<T>, handle: SavedStateHandle
    ): T {
        return PubViewModel(flowyPub) as T
    }
}