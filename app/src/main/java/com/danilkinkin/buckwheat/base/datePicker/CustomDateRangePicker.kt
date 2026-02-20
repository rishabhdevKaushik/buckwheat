package com.danilkinkin.buckwheat.base.datePicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.Divider
import com.danilkinkin.buckwheat.base.datePicker.model.CalendarSelectionMode
import com.danilkinkin.buckwheat.base.datePicker.model.CalendarState
import com.danilkinkin.buckwheat.base.datePicker.model.SelectedDate
import com.danilkinkin.buckwheat.util.toDate
import java.time.LocalDate

@Composable
fun CustomDateRangePicker(
//    onDateSelected: (startDate: Long, endDate: Long) -> Unit,
    startDate: LocalDate,
    finishDate: LocalDate,
    onDatesSelected: (LocalDate, LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
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

    Dialog(onDismissRequest = onDismiss) {
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
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(android.R.string.cancel))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val finalState = calendarState.calendarUiState.value
                            if (finalState.selectedStartDate != null) {
                                val newStart = finalState.selectedStartDate
                                val newFinish = finalState.selectedEndDate ?: finalState.selectedStartDate
                                onDatesSelected(newStart, newFinish)
                                onDismiss()
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