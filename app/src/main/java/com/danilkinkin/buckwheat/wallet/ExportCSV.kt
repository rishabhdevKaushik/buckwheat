package com.danilkinkin.buckwheat.wallet

import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.LocalWindowInsets
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.ButtonRow
import com.danilkinkin.buckwheat.base.Divider
import com.danilkinkin.buckwheat.base.LocalBottomSheetScrollState
import com.danilkinkin.buckwheat.base.datePicker.CustomDateRangePicker
import com.danilkinkin.buckwheat.ui.colorOnEditor
import com.danilkinkin.buckwheat.util.prettyDate
import com.danilkinkin.buckwheat.util.toDate
import java.time.LocalDate

const val EXPORT_CSV_SHEET = "export_csv"

enum class DurationType {
    TODAY, THIS_MONTH, LAST_30_DAYS, CUSTOM
}

@Composable
fun ExportCSV(
    activityResultRegistryOwner: ActivityResultRegistryOwner? = null,
    onClose: () -> Unit = {},
) {
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current

    var showExportOptions by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }

    var durationType by remember { mutableStateOf<DurationType?>(DurationType.THIS_MONTH) }
    var startDate by remember {
        mutableStateOf(LocalDate.now().withDayOfMonth(1))
    }
    var finishDate by remember {
        mutableStateOf(LocalDate.now())
    }

    val navigationBarHeight =
        LocalWindowInsets.current.calculateBottomPadding().coerceAtLeast(16.dp)

    val exportCSVLaunch = rememberExportCSV(
        startDate = startDate,
        finishDate = finishDate,
        activityResultRegistryOwner = activityResultRegistryOwner
    )

    // --- Custom Date Range Picker Dialog ---
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

    Surface(Modifier.padding(top = localBottomSheetScrollState.topPadding)) {
        Column {
            Spacer(Modifier.size(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    stringResource(R.string.export_to_csv_title),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = navigationBarHeight)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    ButtonRow(
                        icon = painterResource(R.drawable.ic_calendar),
                        text = stringResource(R.string.export_duration),
                        onClick = { showExportOptions = true },
                        endCaption = durationType?.let {
                            when (it) {
                                DurationType.TODAY -> stringResource(R.string.today)
                                DurationType.THIS_MONTH -> stringResource(R.string.this_month)
                                DurationType.LAST_30_DAYS -> stringResource(R.string.last_30_days)
                                DurationType.CUSTOM -> stringResource(R.string.custom)
                            }
                        } ?: "")
                    DropdownMenu(
                        expanded = showExportOptions,
                        onDismissRequest = { showExportOptions = false },
//                        modifier = Modifier.align(Alignment.TopEnd)
                        offset = DpOffset((-20).dp, 0.dp)
                    ) {
                        DurationType.entries.forEach { type ->
                            DropdownMenuItem(text = {
                                Text(
                                    text = when (type) {
                                        DurationType.TODAY -> stringResource(R.string.today)
                                        DurationType.THIS_MONTH -> stringResource(R.string.this_month)
                                        DurationType.LAST_30_DAYS -> stringResource(R.string.last_30_days)
                                        DurationType.CUSTOM -> stringResource(R.string.custom)
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }, onClick = {
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
                                showExportOptions = false
                            })
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                ) {
                    Text(
                        text = if (startDate == finishDate) {
                            stringResource(
                                R.string.export_to_csv_show_duration_same_day,
                                prettyDate(date = startDate.toDate(), pattern = "dd MMM, yyyy")
                            )
                        } else {
                            stringResource(
                                R.string.export_to_csv_show_duration, prettyDate(
                                    date = startDate.toDate(),
                                    pattern = "dd MMM, yyyy",
                                    simplifyIfToday = false
                                ), prettyDate(
                                    date = finishDate.toDate(),
                                    pattern = "dd MMM, yyyy",
                                    simplifyIfToday = false
                                )
                            )
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorOnEditor.copy(alpha = 0.6F)
                    )
                }

                Column {
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { exportCSVLaunch() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(60.dp)
                            .padding(horizontal = 16.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.export),
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