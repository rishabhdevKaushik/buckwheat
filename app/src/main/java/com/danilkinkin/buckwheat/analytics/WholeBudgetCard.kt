package com.danilkinkin.buckwheat.analytics

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@Composable
fun WholeBudgetCard(
    modifier: Modifier = Modifier,
    balance: BigDecimal,
    currency: ExtendCurrency,
    currentMonth: Date,
    colors: CardColors = CardDefaults.cardColors(),
    bigVariant: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(vertical = 16.dp, horizontal = 24.dp),
) {
    val context = LocalContext.current

    StatCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        label = stringResource(R.string.whole_budget),
        value = numberFormat(
            context,
            balance,
            currency = currency,
        ),
        valueFontStyle = MaterialTheme.typography.displayMedium,
        valueFontSize = if (bigVariant) MaterialTheme.typography.headlineLarge.fontSize else MaterialTheme.typography.titleLarge.fontSize,
        colors = colors,
        content = {
            Spacer(modifier = Modifier.height(16.dp))
                    Column {
                        Text(
                            text = prettyDate(
                                currentMonth,
                                pattern = "MMM, yy",
                                simplifyIfToday = false,
                            ),
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = if (bigVariant) MaterialTheme.typography.bodySmall.fontSize else MaterialTheme.typography.labelSmall.fontSize,
                        )
                    }
        }
    )
}

@Composable
fun Cross(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.error,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        content()
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = this.size.width
            val height = this.size.height
            val offset = Offset(6f, 6f)
            val thickness = 6f

            drawLine(
                color = tint,
                start = Offset(offset.x, height - offset.y),
                end = Offset(width - offset.x, offset.y),
                strokeWidth = thickness,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun Arrow(
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    Canvas(modifier = modifier) {
        val width = this.size.width
        val height = this.size.height
        val heightHalf = height / 2

        val thickness = 6
        val thicknessHalf = thickness / 2

        val trianglePath = Path().let {
            it.moveTo(11f, heightHalf - thicknessHalf)
            it.lineTo(width - 22.4f, heightHalf - thicknessHalf)
            it.lineTo(width - 37.4f, heightHalf - 18)
            it.lineTo(width - 33, heightHalf - 22.4f)
            it.lineTo(width - 10.5f, heightHalf)
            it.lineTo(width - 33, heightHalf + 22.4f)
            it.lineTo(width - 37.4f, heightHalf + 18)
            it.lineTo(width - 22.4f, heightHalf + thicknessHalf)
            it.lineTo(width - 22.4f, heightHalf + thicknessHalf)
            it.lineTo(11f, heightHalf + thicknessHalf)

            it.close()

            it
        }

        drawPath(
            path = trianglePath,
            SolidColor(tint),
            style = Fill
        )
    }
}

//fun growByMiddleChildRowMeasurePolicy(localDensity: Density) =
//    MeasurePolicy { measurables, constraints ->
//        val minMiddleWidth = with(localDensity) { (24 + 32).dp.toPx().toInt() }
//
//        val first = measurables[0]
//            .measure(
//                constraints.copy(
//                    maxWidth = (constraints.maxWidth - minMiddleWidth) / 2
//                )
//            )
//        val last = measurables[2]
//            .measure(
//                constraints.copy(
//                    maxWidth = (constraints.maxWidth - minMiddleWidth) / 2
//                )
//            )
//
//        val height = listOf(first, last).minOf { it.height }
//
//        layout(constraints.maxWidth, height) {
//            first.placeRelative(0, 0, 0f)
//
//            val middleWidth =
//                (constraints.maxWidth - first.width - last.width).coerceAtLeast(minMiddleWidth)
//
//            val middle = measurables[1]
//                .measure(
//                    constraints.copy(
//                        maxWidth = middleWidth,
//                        minWidth = middleWidth,
//                    )
//                )
//
//            middle.placeRelative(first.width, 0, 0f)
//
//            last.placeRelative(constraints.maxWidth - last.width, 0, 0f)
//        }
//    }

@Preview
@Composable
private fun PreviewChart() {
    BuckwheatTheme {
        Box {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_forward),
                tint = Color.Green,
                contentDescription = null,
            )
            Arrow(
                modifier = Modifier
                    .height(24.dp)
                    .width(100.dp),
            )
        }
    }
}

@Preview
@Composable
private fun PreviewCross() {
    BuckwheatTheme {
        Cross {
            Text(text = "Hello")
        }
    }
}

@Preview
@Preview(name = "Night mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewEarlyFinish() {
    BuckwheatTheme {
        WholeBudgetCard(
            modifier = Modifier.height(IntrinsicSize.Min),
            balance = BigDecimal(60000),
            currency = ExtendCurrency.none(),
            currentMonth = LocalDate.now().minusDays(28).toDate(),
        )
    }
}

@Preview
@Preview(name = "Night mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    BuckwheatTheme {
        WholeBudgetCard(
            modifier = Modifier.height(IntrinsicSize.Min),
            balance = BigDecimal(60000),
            currency = ExtendCurrency.none(),
            currentMonth = LocalDate.now().minusDays(28).toDate(),
        )
    }
}

@Preview(name = "Small screen", widthDp = 190)
@Composable
private fun PreviewSmallScreen() {
    BuckwheatTheme {
        WholeBudgetCard(
            modifier = Modifier.height(IntrinsicSize.Min),
            balance = BigDecimal(60000),
            currency = ExtendCurrency.none(),
            currentMonth = LocalDate.now().minusDays(28).toDate(),
        )
    }
}

@Preview(name = "Small varinat", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewSmallVarinat() {
    BuckwheatTheme {
        WholeBudgetCard(
            modifier = Modifier.height(IntrinsicSize.Min),
            balance = BigDecimal(60000),
            currency = ExtendCurrency.none(),
            currentMonth = LocalDate.now().minusDays(28).toDate(),
            bigVariant = false,
        )
    }
}