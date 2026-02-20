package com.danilkinkin.buckwheat.analytics

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.LocalBottomSheetScrollState
import com.danilkinkin.buckwheat.base.datePicker.CustomDateRangePicker
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.colorOnEditor
import com.danilkinkin.buckwheat.util.combineColors
import com.danilkinkin.buckwheat.util.prettyDate
import com.danilkinkin.buckwheat.util.toDate
import com.danilkinkin.buckwheat.wallet.DurationType
import java.time.LocalDate

@Composable
fun PeriodSelector(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    hasTransactions: Boolean = false,
    onPeriodSelected: (startDate: LocalDate, finishDate: LocalDate) -> Unit = { _, _ -> },
) {
    val localDensity = LocalDensity.current
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current

    var headerSize by remember { mutableStateOf(Size(0.dp, 0.dp)) }
    val scroll = with(localDensity) { scrollState.value.toDp() }

    var showDropdown by remember { mutableStateOf(false) }
    var pressOffset by remember { mutableStateOf(DpOffset.Zero) }
    var itemHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    var showDatePicker by remember { mutableStateOf(false) }

    var durationType by remember { mutableStateOf<DurationType?>(DurationType.THIS_MONTH) }
    var startDate by remember {
        mutableStateOf(LocalDate.now().withDayOfMonth(1))
    }
    var finishDate by remember {
        mutableStateOf(LocalDate.now())
    }

    fun onDurationTypeChanged(type: DurationType) {
        durationType = type
        if (type == DurationType.CUSTOM) {
            showDatePicker = true
        } else {
            durationType = type
            startDate = when (type) {
                DurationType.TODAY -> LocalDate.now()
                DurationType.THIS_MONTH -> LocalDate.now().withDayOfMonth(1)

                DurationType.LAST_30_DAYS -> LocalDate.now().minusDays(30)
                else -> LocalDate.now()
            }
            finishDate = LocalDate.now()
        }
        showDropdown = false
    }

    if (showDatePicker) {
        CustomDateRangePicker(
            startDate = startDate,
            finishDate = finishDate,
            onDatesSelected = { newStart, newFinish ->
                startDate = newStart
                finishDate = newFinish
            },
            onDismiss = { showDatePicker = false }
        )
    }

    LaunchedEffect(durationType, startDate, finishDate) {
        onPeriodSelected(startDate, finishDate)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = localBottomSheetScrollState.topPadding.coerceAtLeast(36.dp))
            .onGloballyPositioned {
                headerSize = Size(
                    width = with(localDensity) { it.size.width.toDp() },
                    height = with(localDensity) { it.size.height.toDp() }
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        val halfWidth = headerSize.width / 2
        val halfHeight = headerSize.height / 2

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .absoluteOffset(y = scroll * 0.25f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(36.dp))
            Box(modifier = modifier) {
                Text(
                    text = stringResource(
                        R.string.period_title,
                        durationType?.let {
                            when (it) {
                                DurationType.TODAY -> stringResource(R.string.today)
                                DurationType.THIS_MONTH -> stringResource(R.string.this_month)
                                DurationType.LAST_30_DAYS -> stringResource(R.string.last_30_days)
                                DurationType.CUSTOM -> stringResource(R.string.custom)
                            }
                        } ?: stringResource(R.string.custom)
                    ),
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .onSizeChanged { itemHeight = with(density) { it.height.toDp() } }
//                        .clickable { showDropdown = true }
                        .pointerInput(true) {
                            detectTapGestures (
                                onPress = {
                                    showDropdown = true
                                    pressOffset = DpOffset(it.x.toDp(), it.y.toDp())
                                }
                            )
                        }
                        .padding(8.dp) // Touch target

                )

                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { showDropdown = false },
//                    modifier = Modifier.align(Alignment.Center),
                    offset = pressOffset.copy(y = pressOffset.y - itemHeight)
                ) {
                    DurationType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = when (type) {
                                        DurationType.TODAY -> stringResource(R.string.today)
                                        DurationType.THIS_MONTH -> stringResource(R.string.this_month)
                                        DurationType.LAST_30_DAYS -> stringResource(R.string.last_30_days)
                                        DurationType.CUSTOM -> stringResource(R.string.custom)
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center,
                                )
                            },
                            onClick = {
                                onDurationTypeChanged(type)
                                showDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = pluralStringResource(
                    id = R.plurals.period_date_range,
                    count = if (startDate == finishDate) 1 else 2,
                    prettyDate(startDate.toDate(), "dd MMM, yyyy"),
                    prettyDate(finishDate.toDate(), "dd MMM, yyyy")
                ),
                style = MaterialTheme.typography.headlineMedium,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                color = colorOnEditor.copy(alpha = 0.6F),
                textAlign = TextAlign.Center,
                modifier = modifier
            )
            Spacer(Modifier.height(8.dp))
            if (!hasTransactions) {
                Text(
                    text = stringResource(R.string.period_summary_no_spends_title),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
            } else {
                Text(
                    text = stringResource(R.string.period_summary_title),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(64.dp))
        }

        val starColor = combineColors(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.surface,
            0.5f,
        )

        val angleStar1 by rememberInfiniteTransition("angleStar1").animateFloat(
            label = "angleStar1",
            initialValue = -20f,
            targetValue = 20f,
            animationSpec = infiniteRepeatable(tween(10000), RepeatMode.Reverse)
        )

        val angleStar2 by rememberInfiniteTransition("angleStar2").animateFloat(
            label = "angleStar2",
            initialValue = -50f,
            targetValue = 50f,
            animationSpec = infiniteRepeatable(tween(18000), RepeatMode.Reverse)
        )

        Icon(
            modifier = Modifier
                .requiredSize(256.dp)
                .absoluteOffset(x = halfWidth * 0.7f, y = -halfHeight * 0.6f + scroll * 0.35f)
                .rotate(angleStar1)
                .zIndex(-1f),
            painter = painterResource(R.drawable.shape_soft_star_1),
            tint = starColor,
            contentDescription = null,
        )
        Icon(
            modifier = Modifier
                .requiredSize(256.dp)
                .absoluteOffset(x = -halfWidth * 0.7f, y = halfHeight * 0.6f + scroll * 0.6f)
                .rotate(angleStar2)
                .zIndex(-1f),
            painter = painterResource(R.drawable.shape_soft_star_2),
            tint = starColor,
            contentDescription = null,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    BuckwheatTheme {
        PeriodSelector()
    }
}