package com.danilkinkin.buckwheat.analytics

import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.LocalWindowInsets
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.ButtonRow
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.entities.TransactionType
import com.danilkinkin.buckwheat.analytics.categoriesChart.CategoriesChartCard
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.toDate
import com.danilkinkin.buckwheat.util.toLocalDate
import com.danilkinkin.buckwheat.wallet.EXPORT_CSV_SHEET
import java.time.LocalDate

const val ANALYTICS_SHEET = "finishPeriod"

data class Size(val width: Dp, val height: Dp)

@Composable
fun Analytics(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    activityResultRegistryOwner: ActivityResultRegistryOwner? = null,
    onCreateNewPeriod: () -> Unit = {},
    onClose: () -> Unit = {},
) {
    val transactions by spendsViewModel.transactions.observeAsState(emptyList())
    val spends by spendsViewModel.spends.observeAsState(emptyList())
    val incomes by spendsViewModel.incomes.observeAsState(emptyList())
    val balance = spendsViewModel.balance.value!!
    var currentMonth = LocalDate.now().toDate()
    val scrollState = rememberScrollState()

    var selectedStartDate by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    var selectedFinishDate by remember { mutableStateOf(LocalDate.now()) }

    // Need to hide calendar after migration to transactions,
    // because after migration can't restore some transactions like INCOME & SET_DAILY_BUDGET
    val afterMigrationToTransactions =
        remember(transactions) { mutableStateOf(transactions.none { it.type == TransactionType.INCOME }) }


    val navigationBarHeight =
        LocalWindowInsets.current.calculateBottomPadding().coerceAtLeast(16.dp)

    // Filter spends and incomes by selected period
    val filteredSpends = remember(spends, selectedStartDate, selectedFinishDate) {
        spends.filter {
            it.date.toLocalDate().isAfter(selectedStartDate.minusDays(1)) &&
                    it.date.toLocalDate().isBefore(selectedFinishDate.plusDays(1))
        }
    }

    val filteredIncomes = remember(incomes, selectedStartDate, selectedFinishDate) {
        incomes.filter {
            it.date.toLocalDate().isAfter(selectedStartDate.minusDays(1)) &&
                    it.date.toLocalDate().isBefore(selectedFinishDate.plusDays(1))
        }
    }

    val filteredTransactions = remember(transactions, selectedStartDate, selectedFinishDate) {
        transactions.filter {
            it.date.toLocalDate().isAfter(selectedStartDate.minusDays(1)) &&
                    it.date.toLocalDate().isBefore(selectedFinishDate.plusDays(1))
        }
    }

    Surface(Modifier.height(IntrinsicSize.Min)) {
        Column {
            MiddlePeriodAnalyticsHeader(
                onClose = onClose,
            )
            Column(
                modifier = Modifier.verticalScroll(scrollState)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PeriodSelector(
                        scrollState = scrollState,
                        hasTransactions = filteredTransactions.isNotEmpty(),
                        onPeriodSelected = { startDate, finishDate ->
                            selectedStartDate = startDate
                            selectedFinishDate = finishDate
                        }
                    )
                    Column(Modifier.fillMaxWidth()) {
//                        TODO: Create charts here
                        LineChartCard(
                        spends = filteredSpends,
                        incomes = filteredIncomes,
                        startDate = selectedStartDate.toDate(),
                        finishDate = selectedFinishDate.toDate(),
                        currency = spendsViewModel.currency.value!!,
                        )
//                        WholeBudgetCard(
//                            balance = balance,
//                            currency = spendsViewModel.currency.value!!,
//                            currentMonth = currentMonth,
//                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        if (filteredSpends.isNotEmpty()) {
                            BalanceCard(modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min),
                            ) {
                                MinMaxSpentCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    isMin = true,
                                    spends = filteredSpends,
                                    isIncome = false,
                                    currency = spendsViewModel.currency.value!!,
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                MinMaxSpentCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    isMin = false,
                                    isIncome = false,
                                    spends = filteredSpends,
                                    currency = spendsViewModel.currency.value!!,
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min),
                            ) {
                                MinMaxSpentCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    isMin = true,
                                    isIncome = true,
                                    spends = filteredIncomes,
                                    currency = spendsViewModel.currency.value!!,
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                MinMaxSpentCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    isMin = false,
                                    isIncome = true,
                                    spends = filteredIncomes,
                                    currency = spendsViewModel.currency.value!!,
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            TransactionsCountCard(
                                modifier = Modifier.fillMaxWidth(),
                                count = filteredTransactions.size,
                            )
//                            if (!afterMigrationToTransactions.value) {
//                                Spacer(modifier = Modifier.height(36.dp))
//                                SpendsCalendar(
//                                    modifier = Modifier.zIndex(-1f),
//                                    balance = balance,
//                                    transactions = filteredTransactions,
//                                    currency = spendsViewModel.currency.value!!,
//                                )
//                            }
                            Spacer(modifier = Modifier.height(36.dp))
                            CategoriesChartCard(
                                modifier = Modifier.fillMaxWidth(),
                                transactions = filteredSpends,
                                currency = spendsViewModel.currency.value!!,
                                startDate = selectedStartDate,
                                finishDate = selectedFinishDate
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            CategoriesChartCard(
                                modifier = Modifier.fillMaxWidth(),
                                isIncome = true,
                                transactions = filteredIncomes,
                                currency = spendsViewModel.currency.value!!,
                                startDate = selectedStartDate,
                                finishDate = selectedFinishDate
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                if (spends.isNotEmpty()) {

                    ButtonRow(
                        icon = painterResource(R.drawable.ic_file_download),
                        text = stringResource(R.string.export_to_csv),
                        onClick = { appViewModel.openSheet(PathState(EXPORT_CSV_SHEET)) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

//                Spacer(
//                    Modifier
//                        .height(60.dp + navigationBarHeight)
//                        .fillMaxWidth()
//                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    BuckwheatTheme {
        Analytics()
    }
}
