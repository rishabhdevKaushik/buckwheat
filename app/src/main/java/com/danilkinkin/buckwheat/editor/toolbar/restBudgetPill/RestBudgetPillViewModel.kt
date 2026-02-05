package com.danilkinkin.buckwheat.editor.toolbar.restBudgetPill

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danilkinkin.buckwheat.di.SpendsRepository
import com.danilkinkin.buckwheat.util.numberFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

enum class DaileBudgetState {
    NOT_SET,
    OVERDRAFT,
    BUDGET_END,
    NORMAL,
}

@HiltViewModel
class RestBudgetPillViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val spendsRepository: SpendsRepository,
) : ViewModel() {
    var state = MutableLiveData(DaileBudgetState.NOT_SET)
        private set
    var percentWithNewSpent = MutableLiveData(1f)
        private set
    var percentWithoutNewSpent = MutableLiveData(1f)
        private set
    var todayBudget = MutableLiveData("")
        private set
    var balance = MutableLiveData("")
        private set
    var newDailyBudget = MutableLiveData("")
        private set

    fun calculateValues(context: Context, currentSpent: BigDecimal) {
        val ths = this

        viewModelScope.launch {
            val balance = spendsRepository.getBalance().first()
            val currency = spendsRepository.getCurrency().first()

            if (balance <= BigDecimal.ZERO) {
                ths.balance.value = ""

                return@launch
            }

            val formattedBalance = numberFormat(
                context,
                balance.coerceAtLeast(BigDecimal.ZERO),
                currency = currency,
                trimDecimalPlaces = true,
            )

            ths.balance.value = formattedBalance
        }
    }
}