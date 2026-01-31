package com.danilkinkin.buckwheat.editor.transactionType

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.entities.TransactionType
import com.danilkinkin.buckwheat.editor.EditStage
import com.danilkinkin.buckwheat.editor.EditorViewModel
import com.danilkinkin.buckwheat.util.observeLiveData
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.animation.core.Spring

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun TransactionTypeToolbar(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    editorViewModel: EditorViewModel = hiltViewModel(),
) {
    var showAddComment by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    observeLiveData(editorViewModel.stage) {
        showAddComment = it === EditStage.EDIT_SPENT
    }

    AnimatedVisibility(visible = showAddComment) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            TransactionTypePill(
                modifier = Modifier
                    .padding(start = 24.dp)
                    .animateContentSize(
                        animationSpec = spring(
                            stiffness = Spring.StiffnessLow,
                            dampingRatio = Spring.DampingRatioMediumBouncy
                        )
                    ),
                isExpanded = isExpanded,
                onExpandedChange = { expanded ->
                    isExpanded = expanded
                }
            )
        }
    }
}
