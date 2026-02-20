package com.danilkinkin.buckwheat.analytics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.util.toDate
import java.time.LocalDate
import java.util.Date
import java.util.Locale

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import com.danilkinkin.buckwheat.data.entities.TransactionType
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.colorOnEditor
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*


@Composable
fun LineChartCard(
    modifier: Modifier = Modifier,
    spends: List<Transaction> = emptyList(),
    incomes: List<Transaction> = emptyList(),
    startDate: Date = LocalDate.now().withDayOfMonth(1).toDate(),
    finishDate: Date = LocalDate.now().toDate(),
    currency: ExtendCurrency,
) {
    val showIncomeLine = remember { mutableStateOf(true) }
    val showSpendsLine = remember { mutableStateOf(true) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Chart
            LineChartContent(
                spends = spends,
                incomes = incomes,
                startDate = startDate,
                finishDate = finishDate,
                showIncomeLine = showIncomeLine.value,
                showSpendsLine = showSpendsLine.value,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Toggle buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ToggleLineButton(
                    text = "Income",
                    selected = showIncomeLine.value,
                    color = colorOnEditor.copy(
                        red = (colorOnEditor.red * 0.9f + 0.1f * 0.1f),
                        green = (colorOnEditor.green * 0.9f + 0.1f * 24f),  // boost green
                        blue = (colorOnEditor.blue * 0.9f + 0.1f * 0.1f),
                    ),
                    onToggle = { showIncomeLine.value = !showIncomeLine.value }
                )
                ToggleLineButton(
                    text = "Spends",
                    selected = showSpendsLine.value,
                    color = colorOnEditor.copy(
                        red = (colorOnEditor.red * 0.9f + 0.1f * 24f),   // boost red
                        green = (colorOnEditor.green * 0.9f + 0.1f * 0.1f),
                        blue = (colorOnEditor.blue * 0.9f + 0.1f * 0.1f),
                    ),
                    onToggle = { showSpendsLine.value = !showSpendsLine.value }
                )
            }
        }
    }
}

@Composable
private fun ToggleLineButton(
    text: String,
    selected: Boolean,
    color: Color,
    onToggle: () -> Unit
) {
    TextButton(
        onClick = onToggle,
        colors = ButtonDefaults.textButtonColors(
            contentColor = color
        )
    ) {
        Text(
            text = text,
            color = if (selected) color
            else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LineChartContent(
    spends: List<Transaction>,
    incomes: List<Transaction>,
    startDate: Date,
    finishDate: Date,
    showIncomeLine: Boolean,
    showSpendsLine: Boolean,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val isNightMode = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES

    // Aggregate daily data
    val dailyData = remember(spends, incomes, startDate, finishDate) {
        aggregateDailyData(spends, incomes, startDate, finishDate)
    }

    val incomeColor: Color = colorOnEditor.copy(
        red = (colorOnEditor.red * 0.9f + 0.1f * 0.1f),
        green = (colorOnEditor.green * 0.9f + 0.1f * 24f),  // boost green
        blue = (colorOnEditor.blue * 0.9f + 0.1f * 0.1f),
        alpha = 0.6f
    )
    val spendsColor: Color = colorOnEditor.copy(
        red = (colorOnEditor.red * 0.9f + 0.1f * 24f),   // boost red
        green = (colorOnEditor.green * 0.9f + 0.1f * 0.1f),
        blue = (colorOnEditor.blue * 0.9f + 0.1f * 0.1f),
        alpha = 0.6f
    )
    val gridColor: Color = MaterialTheme.colorScheme.outlineVariant

    if (dailyData.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data for selected period",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Calculate scales
            val maxAmount = dailyData.maxOfOrNull { maxOf(it.income, it.spends) } ?: 0f
            val yScale = if (maxAmount > 0) canvasHeight * 0.8f / maxAmount else 1f
            val xScale = canvasWidth / (dailyData.size - 1)

            val baseline = canvasHeight * 0.85f

            // Draw grid lines
            drawGridLines(canvasWidth, canvasHeight, 4, gridColor)

            // Draw lines and points
            if (showIncomeLine && dailyData.any { it.income > 0 }) {
                drawPath(
                    path = generateLinePath(
                        dailyData.map { it.income },
                        baseline,
                        yScale,
                        xScale
                    ),
                    color = incomeColor,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            if (showSpendsLine && dailyData.any { it.spends > 0 }) {
                drawPath(
                    path = generateLinePath(
                        dailyData.map { it.spends },
                        baseline,
                        yScale,
                        xScale
                    ),
                    color = spendsColor,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    ),
                )
            }

            // Draw points
            dailyData.forEachIndexed { index, day ->
                val x = index * xScale

                if (showIncomeLine && day.income > 0) {
                    val y = baseline - day.income * yScale
                    drawCircle(
                        color = incomeColor,
                        radius = 3.dp.toPx(),
                        center = Offset(x, y)
                    )
                }

                if (showSpendsLine && day.spends > 0) {
                    val y = baseline - day.spends * yScale
                    drawCircle(
                        color = spendsColor,
                        radius = 3.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }

        // Date labels (bottom)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = dailyData.firstOrNull()?.date?.let {
                    SimpleDateFormat("MMM dd", Locale.getDefault()).format(it)
                } ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = dailyData.lastOrNull()?.date?.let {
                    SimpleDateFormat("MMM dd", Locale.getDefault()).format(it)
                } ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class DailyData(
    val date: Date,
    val income: Float,
    val spends: Float
)

private fun aggregateDailyData(
    spends: List<Transaction>,
    incomes: List<Transaction>,
    startDate: Date,
    finishDate: Date
): List<DailyData> {
    val calendar = Calendar.getInstance().apply { time = startDate }
    val endCalendar = Calendar.getInstance().apply { time = finishDate }

    val dailyData = mutableListOf<DailyData>()

    while (!calendar.after(endCalendar)) {
        val currentDate = calendar.time
        val dayStart = Calendar.getInstance().apply {
            time = currentDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dayEnd = Calendar.getInstance().apply {
            time = currentDate
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val daySpends = spends.filter { tx ->
            tx.date.after(dayStart.time) && tx.date.before(dayEnd.time)
        }
            .fold(0f) { acc, tx -> acc + tx.value.toFloat() }

        val dayIncomes = incomes
            .filter { tx ->
                tx.date.after(dayStart.time) && tx.date.before(dayEnd.time)
            }
            .fold(0f) { acc, tx -> acc + tx.value.toFloat() }

        dailyData.add(DailyData(currentDate, dayIncomes, daySpends))
        calendar.add(Calendar.DAY_OF_MONTH, 1)
    }

    return dailyData
}

private fun DrawScope.drawGridLines(width: Float, height: Float, lines: Int, gridColor: Color) {
    val strokeWidth = 1.dp.toPx()

    // Vertical grid lines
    repeat(5) { i ->
        val x = (width / 4f) * i
        drawLine(
            color = gridColor.copy(alpha = 0.3f),
            start = Offset(x, 0f),
            end = Offset(x, height * 0.9f),
            strokeWidth = strokeWidth
        )
    }

    // Horizontal grid lines
    repeat(lines) { i ->
        val y = (height * 0.9f / lines) * i
        drawLine(
            color = gridColor.copy(alpha = 0.3f),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = strokeWidth
        )
    }
}

private fun generateLinePath(
    values: List<Float>,
    baseline: Float,
    yScale: Float,
    xScale: Float
): Path {
    val path = Path()
    values.forEachIndexed { index, value ->
        val x = index * xScale
        val y = baseline - value * yScale
        if (index == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    return path
}


@Preview(name = "Default")
@Preview(name = "Default (Dark mode)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        LineChartCard(
            spends = listOf(
                Transaction(
                    type = TransactionType.SPENT,
                    value = BigDecimal(3),
                    date = LocalDate.now().minusDays(2).toDate()
                ),
                Transaction(
                    type = TransactionType.SPENT,
                    value = BigDecimal(5),
                    date = LocalDate.now().minusDays(1).toDate()
                ),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(8), date = Date()),
                Transaction(
                    type = TransactionType.SPENT,
                    value = BigDecimal(6),
                    date = LocalDate.now().plusDays(1).toDate()
                ),
                Transaction(
                    type = TransactionType.SPENT,
                    value = BigDecimal(8),
                    date = LocalDate.now().plusDays(2).toDate()
                ),
                Transaction(
                    type = TransactionType.SPENT,
                    value = BigDecimal(10),
                    date = LocalDate.now().plusDays(2).toDate()
                ),
                Transaction(
                    type = TransactionType.SPENT,
                    value = BigDecimal(12),
                    date = LocalDate.now().plusDays(2).toDate()
                ),
                Transaction(
                    type = TransactionType.SPENT,
                    value = BigDecimal(8),
                    date = LocalDate.now().plusDays(5).toDate()
                ),
                Transaction(
                    type = TransactionType.SPENT,
                    value = BigDecimal(82),
                    date = LocalDate.now().plusDays(11).toDate()
                ),
            ),
            incomes = listOf(
                Transaction(
                    type = TransactionType.INCOME,
                    value = BigDecimal(800),
                    date = LocalDate.now().minusDays(5).toDate()
                ),
                Transaction(
                    type = TransactionType.INCOME,
                    value = BigDecimal(10),
                    date = LocalDate.now().plusDays(2).toDate()
                ),
                Transaction(
                    type = TransactionType.INCOME,
                    value = BigDecimal(12),
                    date = LocalDate.now().plusDays(2).toDate()
                ),
            ),
            currency = ExtendCurrency.none(),
            startDate = LocalDate.now().minusDays(7).toDate(),
            finishDate = LocalDate.now().plusDays(27).toDate(),
        )
    }
}