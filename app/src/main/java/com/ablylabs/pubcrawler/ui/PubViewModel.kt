package com.ablylabs.pubcrawler.ui

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.ablylabs.pubcrawler.pubs.Pub
import com.ablylabs.pubcrawler.realtime.*
import kotlinx.coroutines.launch

class PubViewModel(private val flowyPub: FlowyRealtimePub) : ViewModel() {
    private val _leaveResult = MutableLiveData<LeaveResult>()
    val leaveResult: LiveData<LeaveResult> = _leaveResult

    private val _joinResult = MutableLiveData<FlowJoinResult>()
    val joinResult: LiveData<FlowJoinResult> = _joinResult

    private val _acceptDrinkResult = MutableLiveData<AcceptDrinkResult>()
    val acceptDrinkResult: LiveData<AcceptDrinkResult> = _acceptDrinkResult

    private val _rejectDrinkResult = MutableLiveData<RejectDrinkResult>()
    val rejectDrinkResult: LiveData<RejectDrinkResult> = _rejectDrinkResult

    private val _messageSentResult = MutableLiveData<MessageSentResult>()
    val messageSentResult: LiveData<MessageSentResult> = _messageSentResult

    private val _offerDrinkResult = MutableLiveData<OfferSentResult>()
    val offerDrinkResult:LiveData<OfferSentResult> = _offerDrinkResult

    private val _offerResponse = MutableLiveData<DrinkOfferResponse>()
    val offerResponse:LiveData<DrinkOfferResponse> = _offerResponse

    private val _allPubGoers = MutableLiveData<List<PubGoer>>()
    val allPubGoers:LiveData<List<PubGoer>> = _allPubGoers

    fun leavePub(who: PubGoer, which: Pub) {
        viewModelScope.launch {
            _leaveResult.value = flowyPub.leave(who, which)
        }
    }

    fun joinPub(who: PubGoer, which: Pub) {
        viewModelScope.launch {
            _joinResult.value = flowyPub.join(which,who)
        }
    }

    fun acceptDrink(who: PubGoer, fromWhom: PubGoer) {
        viewModelScope.launch {
            _acceptDrinkResult.value = flowyPub.acceptDrink(who,fromWhom)
        }
    }

    fun rejectDrink(who: PubGoer, fromWhom: PubGoer) {
        viewModelScope.launch {
            _rejectDrinkResult.value = flowyPub.rejectDrink(who,fromWhom)
        }
    }

    fun sendTextMessage(who: PubGoer, toWhom: PubGoer, message:String) {
        viewModelScope.launch {
            _messageSentResult.value = flowyPub.sendTextMessage(who,toWhom,message)
        }
    }

    fun offerDrinkTo(who: PubGoer,toWhom: PubGoer){
        viewModelScope.launch {
            _offerDrinkResult.value = flowyPub.offerDrink(who,toWhom)
            if (_offerDrinkResult.value is OfferSentResult.Success){
                _offerResponse.value = flowyPub.registerToDrinkOfferResponse(offered = toWhom,offeree = who)
            }
        }
    }

    fun refreshPubgoers(pub:Pub) {
        viewModelScope.launch {
            _allPubGoers.value = flowyPub.allPubGoers(pub)
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