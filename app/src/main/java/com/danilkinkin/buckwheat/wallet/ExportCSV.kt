package com.danilkinkin.buckwheat.wallet

import android.util.Log
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.danilkinkin.buckwheat.LocalWindowInsets
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.ButtonRow
import com.danilkinkin.buckwheat.base.Divider
import com.danilkinkin.buckwheat.base.LocalBottomSheetScrollState
import com.danilkinkin.buckwheat.base.datePicker.DatePicker
import com.danilkinkin.buckwheat.base.datePicker.model.CalendarSelectionMode
import com.danilkinkin.buckwheat.base.datePicker.model.CalendarState
import com.danilkinkin.buckwheat.base.datePicker.model.SelectedDate
import com.danilkinkin.buckwheat.ui.colorOnEditor
import com.danilkinkin.buckwheat.util.prettyDate
import com.danilkinkin.buckwheat.util.toDate
import java.time.LocalDate

const val EXPORT_CSV_SHEET = "export_csv"

enum class ExportType {
    TODAY,
    THIS_MONTH,
    LAST_30_DAYS,
    CUSTOM
}

@Composable
fun ExportCSV(
    activityResultRegistryOwner: ActivityResultRegistryOwner? = null,
    onClose: () -> Unit = {},
) {
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current

    var showExportOptions by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    var exportType by remember { mutableStateOf<ExportType?>(ExportType.THIS_MONTH) }
    var startDate by remember {
        mutableStateOf(LocalDate.now().withDayOfMonth(1))
    }
    var finishDate by remember {
        mutableStateOf(LocalDate.now())
    }

    val navigationBarHeight = LocalWindowInsets.current.calculateBottomPadding()
        .coerceAtLeast(16.dp)

    val exportCSVLaunch = rememberExportCSV(
        startDate = startDate,
        finishDate = finishDate,
        activityResultRegistryOwner = activityResultRegistryOwner
    )

    // --- Custom Date Range Picker Dialog ---
    if (showDatePicker) {
        val context = LocalContext.current

        // Initialize state
        val calendarState = remember {
            CalendarState(
                context = context,
                selectionMode = CalendarSelectionMode.RANGE,
                selectDate = startDate.toDate()
            ).apply {
                calendarUiState.value = calendarUiState.value.copy(
                    selectedStartDate = startDate,
                    selectedEndDate = finishDate,
                    disabledAfter = LocalDate.now()
                )
            }
        }

        Dialog(onDismissRequest = { showDatePicker = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.custom),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )

                    Divider()

                    Box(modifier = Modifier.weight(1f, fill = false)) {
                        DatePicker(
                            calendarState = calendarState,
                            onDayClicked = { clickedDate ->
                                val currentState = calendarState.calendarUiState.value
                                val start = currentState.selectedStartDate
                                val end = currentState.selectedEndDate

                                val newState = when (clickedDate) {
                                    end -> {
                                        currentState.changeHighlightedDate(SelectedDate.END)
                                    }
                                    start -> {
                                        currentState.changeHighlightedDate(SelectedDate.START)
                                    }
                                    else -> {
                                        currentState.setDateRange(clickedDate)
                                    }
                                }
                                calendarState.calendarUiState.value = newState
                            }
                        )
                    }

                    Divider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text(stringResource(android.R.string.cancel))
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val finalState = calendarState.calendarUiState.value
                                if (finalState.selectedStartDate != null) {
                                    startDate = finalState.selectedStartDate
                                    // If only one day selected, make start and end the same
                                    finishDate =
                                        finalState.selectedEndDate ?: finalState.selectedStartDate
                                    showDatePicker = false
                                }
                            },
                            enabled = calendarState.calendarUiState.value.selectedStartDate != null
                        ) {
                            Text(stringResource(android.R.string.ok))
                        }
                    }
                }
            }
        }
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
                        endCaption = exportType?.let {
                            when (it) {
                                ExportType.TODAY -> stringResource(R.string.today)
                                ExportType.THIS_MONTH -> stringResource(R.string.this_month)
                                ExportType.LAST_30_DAYS -> stringResource(R.string.last_30_days)
                                ExportType.CUSTOM -> stringResource(R.string.custom)
                            }
                        } ?: ""
                    )
                    DropdownMenu(
                        expanded = showExportOptions,
                        onDismissRequest = { showExportOptions = false },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        ExportType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = when (type) {
                                            ExportType.TODAY -> stringResource(R.string.today)
                                            ExportType.THIS_MONTH -> stringResource(R.string.this_month)
                                            ExportType.LAST_30_DAYS -> stringResource(R.string.last_30_days)
                                            ExportType.CUSTOM -> stringResource(R.string.custom)
                                        },
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                },
                                onClick = {
                                    exportType = type
                                    if (type == ExportType.CUSTOM) {
                                        showDatePicker = true
                                    } else {
                                        exportType = type
                                        startDate = when (type) {
                                            ExportType.TODAY -> LocalDate.now()
                                            ExportType.THIS_MONTH -> LocalDate.now()
                                                .withDayOfMonth(1)

                                            ExportType.LAST_30_DAYS -> LocalDate.now().minusDays(30)
                                            else -> LocalDate.now()
                                        }
                                        finishDate = LocalDate.now()
                                    }
                                    showExportOptions = false
                                }
                            )
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
                                R.string.export_to_csv_show_duration,
                                prettyDate(
                                    date = startDate.toDate(),
                                    pattern = "dd MMM, yyyy",
                                    simplifyIfToday = false
                                ),
                                prettyDate(
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