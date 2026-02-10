package com.danilkinkin.buckwheat.base.datePicker.model

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.danilkinkin.buckwheat.util.getNumberWeeks
import com.danilkinkin.buckwheat.util.toLocalDate
import java.time.LocalDate
import java.time.Period
import java.time.YearMonth
import java.util.*

class CalendarState(
    context: Context,
    selectionMode: CalendarSelectionMode = CalendarSelectionMode.SINGLE,
    selectDate: Date? = null,
    disableBeforeDate: Date? = null,
    disableAfterDate: Date? = null,
) {

    val calendarUiState = mutableStateOf(
        CalendarUiState(
        selectionMode = selectionMode,
        disabledBefore = disableBeforeDate?.toLocalDate(),
        disabledAfter = disableAfterDate?.toLocalDate(),
    )
    )
    val listMonths: List<Month>
    val initialMonthIndex: Int

    private val calendarStartDate: LocalDate = LocalDate.now().minusYears(10).withDayOfMonth(1)
    private val calendarEndDate: LocalDate = LocalDate.now().plusYears(2)
        .withMonth(12).withDayOfMonth(31)

    private val periodBetweenCalendarStartEnd: Period = Period.between(
        disableBeforeDate?.toLocalDate()?.withDayOfMonth(1) ?: calendarStartDate,
        disableAfterDate?.toLocalDate()?.withDayOfMonth(28) ?: calendarEndDate
    )

    init {
        val tempListMonths = mutableListOf<Month>()
        var startYearMonth = YearMonth.from(disableBeforeDate?.toLocalDate()?.withDayOfMonth(1) ?: calendarStartDate)
        var targetMonthIndex = 0

        for (numberMonth in 0..periodBetweenCalendarStartEnd.toTotalMonths()) {
            val ym = startYearMonth
            val numberWeeks = ym.getNumberWeeks(context)
            val listWeekItems = mutableListOf<Week>()
            for (week in 0 until numberWeeks) {
                listWeekItems.add(Week(number = week, yearMonth = ym))
            }
            val month = Month(ym, listWeekItems)
            tempListMonths.add(month)

            if (selectDate != null && targetMonthIndex == 0 &&
                !ym.isBefore(YearMonth.from(selectDate.toLocalDate())) &&
                ym <= YearMonth.from(selectDate.toLocalDate())) {
                targetMonthIndex = numberMonth.toInt()
            }

            startYearMonth = startYearMonth.plusMonths(1)
        }
        listMonths = tempListMonths.toList()
        initialMonthIndex = targetMonthIndex

        if (selectDate != null) setSelectedDay(selectDate.toLocalDate())
    }

    fun setSelectedDay(newDate: LocalDate) {
        if (calendarUiState.value.selectionMode == CalendarSelectionMode.RANGE) {
            calendarUiState.value = calendarUiState.value.setDates(LocalDate.now(), newDate)
        } else {
            calendarUiState.value = calendarUiState.value.setDate(newDate)
        }
    }

    companion object {
        const val DAYS_IN_WEEK = 7
    }
}
