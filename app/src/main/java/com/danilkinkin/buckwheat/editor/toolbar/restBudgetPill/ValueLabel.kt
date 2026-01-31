package com.danilkinkin.buckwheat.editor.toolbar.restBudgetPill

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.base.AnimatedNumber
import com.danilkinkin.buckwheat.util.HarmonizedColorPalette
import android.util.Log

@Composable
fun ValueLabel(
    harmonizedColor: HarmonizedColorPalette,
    restBudgetPillViewModel: RestBudgetPillViewModel = hiltViewModel(),
) {
    val balance by restBudgetPillViewModel.balance.observeAsState("")

    AnimatedContent(
        label = "Balance animated content",
        targetState = balance
    ) { targetBalance ->
        AnimatedNumber(
            value = targetBalance,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = MaterialTheme.typography.headlineMedium.fontSize
            ),
        )
    }
    Spacer(modifier = Modifier.width(16.dp))
}