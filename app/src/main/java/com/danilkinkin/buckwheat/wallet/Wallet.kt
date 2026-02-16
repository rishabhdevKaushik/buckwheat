package com.danilkinkin.buckwheat.wallet

import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.LocalWindowInsets
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.ButtonRow
import com.danilkinkin.buckwheat.base.Divider
import com.danilkinkin.buckwheat.base.LocalBottomSheetScrollState
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.di.TUTORS
import com.danilkinkin.buckwheat.analytics.ANALYTICS_SHEET
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.*
import java.math.BigDecimal
import java.util.*
import android.util.Log


const val WALLET_SHEET = "wallet"

@Composable
fun Wallet(
    forceChange: Boolean = false,
    activityResultRegistryOwner: ActivityResultRegistryOwner? = null,
    appViewModel: AppViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current

    var balanceCache by remember { mutableStateOf(spendsViewModel.balance.value ?: BigDecimal.ZERO) }
    val balance by spendsViewModel.balance.observeAsState(BigDecimal.ZERO)
    val currency by spendsViewModel.currency.observeAsState()
    val spends by spendsViewModel.transactions.observeAsState()

    val navigationBarHeight = LocalWindowInsets.current.calculateBottomPadding()
        .coerceAtLeast(16.dp)


    var isEdit = forceChange

    val offset = with(LocalDensity.current) { 50.dp.toPx().toInt() }

    Surface(Modifier.padding(top = localBottomSheetScrollState.topPadding)) {
        Column {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (!forceChange && isEdit) {
                    IconButton(
                        onClick = { isEdit = false },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    Spacer(Modifier.size(48.dp))
                }
                Spacer(Modifier.weight(1F))
                Text(
                    text = if (isEdit) {
                        stringResource(R.string.wallet_edit_title)
                    } else {
                        stringResource(R.string.wallet_title)
                    },
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.weight(1F))
//                if (!isEdit) {
//                    IconButton(
//                        onClick = { isEdit = true },
//                    ) {
//                        Icon(
//                            painter = painterResource(R.drawable.ic_edit),
//                            contentDescription = null,
//                            modifier = Modifier.size(24.dp)
//                        )
//                    }
//                } else {
                    Spacer(Modifier.size(48.dp))
//                }
            }
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = navigationBarHeight)
            ) {
                AnimatedContent(
                    targetState = isEdit,
                    transitionSpec = {
                        if (targetState && !initialState) {
                            (slideInHorizontally(
                                tween(durationMillis = 150)
                            ) { offset } + fadeIn(
                                tween(durationMillis = 150)
                            )).togetherWith(slideOutHorizontally(
                                tween(durationMillis = 150)
                            ) { -offset } + fadeOut(
                                tween(durationMillis = 150)
                            ))
                        } else {
                            (slideInHorizontally(
                                tween(durationMillis = 150)
                            ) { -offset } + fadeIn(
                                tween(durationMillis = 150)
                            )).togetherWith(slideOutHorizontally(
                                tween(durationMillis = 150)
                            ) { offset } + fadeOut(
                                tween(durationMillis = 150)
                            ))
                        }.using(
                            SizeTransform(
                                clip = true,
                                sizeAnimationSpec = { _, _ -> tween(durationMillis = 350) }
                            )
                        )
                    }
                ) { targetIsEdit ->
                    if (targetIsEdit) {
                        BalanceConstructor(
                            forceChange = forceChange,
                            onChange = { newBalance ->
                                balanceCache = newBalance
                            }
                        )
                    } else {
                        BudgetSummary(
                            onEdit = {
                                isEdit = true
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        )
                    }
                }
                ButtonRow(
                    icon = painterResource(R.drawable.ic_currency),
                    text = stringResource(R.string.in_currency_label),
                    onClick = {
                        appViewModel.openSheet(PathState(CURRENCY_EDITOR))
                    },
                    endCaption = when (currency?.type) {
                        ExtendCurrency.Type.FROM_LIST -> "${
                            Currency.getInstance(
                                currency!!.value
                            ).displayName.titleCase()
                        } (${
                            Currency.getInstance(
                                currency!!.value
                            ).symbol
                        })"
                        ExtendCurrency.Type.CUSTOM -> currency!!.value!!
                        else -> ""
                    },
                )
                AnimatedVisibility(
                    visible = !isEdit,
                    enter = fadeIn(
                        tween(durationMillis = 350)
                    ) + expandVertically(
                        expandFrom = Alignment.Bottom,
                        animationSpec = tween(durationMillis = 350)
                    ),
                    exit = fadeOut(
                        tween(durationMillis = 350)
                    ) + shrinkVertically(
                        shrinkTowards = Alignment.Bottom,
                        animationSpec = tween(durationMillis = 350)
                    ),
                ) {
                    Column {
                        if (spends!!.isNotEmpty()) {
                            ButtonRow(
                                icon = painterResource(R.drawable.ic_analytics),
                                text = stringResource(R.string.view_analytics),
                                onClick = {
                                    appViewModel.openSheet(PathState(ANALYTICS_SHEET))
                                }
                            )
                        }


                        ButtonRow(
                            icon = painterResource(R.drawable.ic_file_download),
                            text = stringResource(R.string.export_to_csv),
//                            onClick = { exportCSVLaunch() }
                            onClick = { appViewModel.openSheet(PathState(EXPORT_CSV_SHEET)) }
                        )

                    }
                }
                AnimatedVisibility(
                    visible = isEdit,
                    enter = fadeIn(
                        tween(durationMillis = 350)
                    ) + expandVertically(
                        expandFrom = Alignment.Bottom,
                        animationSpec = tween(durationMillis = 350)
                    ),
                    exit = fadeOut(
                        tween(durationMillis = 350)
                    ) + shrinkVertically(
                        shrinkTowards = Alignment.Bottom,
                        animationSpec = tween(durationMillis = 350)
                    ),
                ) {
                    Column {
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
//                        Total(
//                            budget = budgetCache,
//                            restBudget = restBudget,
//                            days = days,
//                            currency = currency!!,
//                        )
                        Button(
                            onClick = {
                                spendsViewModel.changeDisplayCurrency(currency!!)

                                if (spends!!.isNotEmpty() && !forceChange) {
                                    spendsViewModel.setBalance(balanceCache)
                                } else {
                                    spendsViewModel.setBalance(balanceCache)
                                    appViewModel.activateTutorial(TUTORS.OPEN_WALLET)
                                }

                                onClose()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(60.dp)
                                .padding(horizontal = 16.dp),
                            enabled =  balanceCache > BigDecimal(0)
                        ) {
                            Text(
                                text = if (spends!!.isNotEmpty() && !forceChange) {
//                                    stringResource(R.string.change_budget)
                                    stringResource(R.string.change_balance)
                                } else {
                                    stringResource(R.string.apply)
                                },
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_forward),
                                contentDescription = null,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewWallet() {
    BuckwheatTheme {
        Wallet()
    }
}
