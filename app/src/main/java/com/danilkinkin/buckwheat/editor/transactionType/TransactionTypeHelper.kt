package com.danilkinkin.buckwheat.editor.transactionType

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.danilkinkin.buckwheat.data.entities.TransactionType

object TransactionTypeHelper {
    var type by mutableStateOf(TransactionType.SPENT)

    fun getTransactionType(): TransactionType = type

    fun setTransactionType(newType: TransactionType) { type = newType }

    fun setDefaultTransactionType() { type = TransactionType.SPENT }

    fun rememberTransactionType(): TransactionType {
        return TransactionTypeHelper.type
    }
}