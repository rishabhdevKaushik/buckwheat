package com.danilkinkin.buckwheat.history

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
fun TotalPerMonth(
    spentPerMonth: BigDecimal,
    incomePerMonth: BigDecimal,
    currency: ExtendCurrency,
    currentMonth: Date,
    colors: CardColors = CardDefaults.cardColors(),
    bigVariant: Boolean = true,
) {
    val context = LocalContext.current

//            Spacer(modifier = Modifier.height(148.dp))
    Card(
        modifier = Modifier.padding(horizontal = 8.dp).padding(top = 16.dp),
        colors = colors,
        content = {


            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = numberFormat(
                            context,
                            spentPerMonth,
                            currency = currency,
                        ),
                        style = MaterialTheme.typography.displayMedium,
                        color = colors.contentColor.copy(
                            red = (colors.contentColor.red * 0.9f + 0.1f * 3f),   // boost red
                            green = (colors.contentColor.green * 0.9f + 0.1f * 0.1f),
                            blue = (colors.contentColor.blue * 0.9f + 0.1f * 0.1f),
                        )
                    )
                    Text(
                        text = numberFormat(
                            context,
                            incomePerMonth,
                            currency = currency,
                        ),
                        style = MaterialTheme.typography.displayMedium,
                        color = colors.contentColor.copy(
                            red = (colors.contentColor.red * 0.9f + 0.1f * 0.1f),
                            green = (colors.contentColor.green * 0.9f + 0.1f * 2f),  // boost green
                            blue = (colors.contentColor.blue * 0.9f + 0.1f * 0.1f),
                        )
                    )


                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp)
                        .offset(y = (-4).dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.spent),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.contentColor.copy(alpha = 0.6f)
                    )
                    Text(
                        text = stringResource(R.string.income),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.contentColor.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                Text(
                    text = prettyDate(
                        currentMonth,
                        pattern = "MMM yy",
                        simplifyIfToday = false,
                    ),
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = if (bigVariant) MaterialTheme.typography.bodySmall.fontSize else MaterialTheme.typography.labelSmall.fontSize,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    )
    Spacer(modifier = Modifier.height(8.dp))
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
        TotalPerMonth(
            spentPerMonth = BigDecimal(60000),
            incomePerMonth = BigDecimal(60000),
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
        TotalPerMonth(
            spentPerMonth = BigDecimal(60000),
            incomePerMonth = BigDecimal(60000),
            currency = ExtendCurrency.none(),
            currentMonth = LocalDate.now().minusDays(28).toDate(),
        )
    }
}

@Preview(name = "Small screen", widthDp = 190)
@Composable
private fun PreviewSmallScreen() {
    BuckwheatTheme {
        TotalPerMonth(
            spentPerMonth = BigDecimal(60000),
            incomePerMonth = BigDecimal(60000),
            currency = ExtendCurrency.none(),
            currentMonth = LocalDate.now().minusDays(28).toDate(),
        )
    }
}

@Preview(name = "Small varinat", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewSmallVarinat() {
    BuckwheatTheme {
        TotalPerMonth(
            spentPerMonth = BigDecimal(60000),
            incomePerMonth = BigDecimal(60000),
            currency = ExtendCurrency.none(),
            currentMonth = LocalDate.now().minusDays(28).toDate(),
            bigVariant = false,
        )
    }
}