//import com.danilkinkin.buckwheat.editor.tagging.CustomTag
//import com.danilkinkin.buckwheat.editor.tagging.Tag

package com.danilkinkin.buckwheat.editor.transactionType

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.editor.EditStage
import com.danilkinkin.buckwheat.editor.EditorViewModel
import com.danilkinkin.buckwheat.editor.FocusController
import com.danilkinkin.buckwheat.util.observeLiveData
import com.danilkinkin.buckwheat.data.entities.TransactionType
import com.danilkinkin.buckwheat.R

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun TransactionTypeToolbar(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    editorViewModel: EditorViewModel = hiltViewModel(),
//    editorFocusController: FocusController
) {
    val localDensity = LocalDensity.current

    val tags by spendsViewModel.tags.observeAsState(emptyList())
    val currentComment by editorViewModel.currentComment.observeAsState("")

    var showAddComment by remember { mutableStateOf(false) }
    var transactionType by remember { mutableStateOf(TransactionType.SPENT) }
    var isExpanded by remember { mutableStateOf(false) }

    var isEdit by remember { mutableStateOf(false) }

    observeLiveData(editorViewModel.stage) {
        showAddComment = it === EditStage.EDIT_SPENT
    }
    AnimatedVisibility(visible = showAddComment) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
//            val width = maxWidth - 48.dp
//            val buttonWidth = 48.dp

            Surface(
                modifier = Modifier
                    .padding(start = 24.dp)
                    .animateContentSize(
                        animationSpec = spring(
                            stiffness = Spring.StiffnessLow,
                            dampingRatio = Spring.DampingRatioMediumBouncy
                        )
                    )
                    .then(
                        if (!isExpanded) Modifier.height(32.dp) else Modifier.wrapContentHeight()
                    ),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
//                shadowElevation = 4.dp // Optional: adds depth like a floating button
            ) {
                if (isExpanded) {
                    Column(
                        modifier = Modifier
                            .width(120.dp)
                            .padding(8.dp)
                    ) {
                        // Income Option
                        TextButton(
                            onClick = {
                                transactionType = TransactionType.INCOME
                                isExpanded = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.income) + " +",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Spent Option
                        TextButton(
                            onClick = {
                                transactionType = TransactionType.SPENT
                                isExpanded = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.spend) + " -",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .clickable { isExpanded = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (transactionType == TransactionType.INCOME) "+" else "-",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }

}