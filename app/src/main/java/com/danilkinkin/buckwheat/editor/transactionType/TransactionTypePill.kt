package com.danilkinkin.buckwheat.editor.transactionType

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.entities.TransactionType
import com.danilkinkin.buckwheat.editor.transactionType.TransactionTypeHelper.rememberTransactionType


@Composable
fun TransactionTypePill(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val type = rememberTransactionType()

    Surface(
        modifier = modifier.then(
            if (!isExpanded) Modifier.height(32.dp) else Modifier.wrapContentHeight()
        ),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        if (isExpanded) {
            Column(
                modifier = Modifier
                    .width(120.dp)
                    .padding(8.dp)
            ) {
                TextButton(
                    onClick = {
                        TransactionTypeHelper.setTransactionType(TransactionType.INCOME)
                        onExpandedChange(false)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(
                        text = stringResource(R.string.income) + " +",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = {
                        TransactionTypeHelper.setTransactionType(TransactionType.SPENT)
                        onExpandedChange(false)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(
                        text = stringResource(R.string.spend) + " -",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .clickable { onExpandedChange(true) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (type == TransactionType.INCOME) "+" else "-",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
