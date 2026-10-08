package com.freyr.app.ui.screens.debt

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freyr.app.data.repository.FreyrRepository
import com.freyr.app.ui.theme.*
import com.freyr.app.ui.viewmodel.FreyrViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtDetailScreen(
    debtId: String,
    viewModel: FreyrViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddPayment: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val allDebts by viewModel.allDebts.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    val allGroups by viewModel.allGroups.collectAsState()

    val debt = remember(allDebts, debtId) { allDebts.find { it.id == debtId } }
    val payments = remember(allPayments, debtId) { allPayments.filter { it.debtId == debtId } }

    if (debt == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Debt Details", fontWeight = FontWeight.Bold, color = FreyrPrimary) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FreyrPrimary)
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Debt not found", color = FreyrTextSecondary)
            }
        }
        return
    }

    val totalPaid = payments.sumOf { it.amount }
    val remaining = (debt.amount - totalPaid).coerceAtLeast(0.0)
    val isPaid = remaining == 0.0 || debt.status == "paid"
    val group = allGroups.find { it.id == debt.familyGroupId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(debt.name, fontWeight = FontWeight.Bold, color = FreyrPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FreyrPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FreyrBackground)
            )
        },
        floatingActionButton = {
            if (!isPaid) {
                ExtendedFloatingActionButton(
                    onClick = { onNavigateToAddPayment(debt.id) },
                    containerColor = FreyrPrimary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Payment", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = FreyrBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FreyrSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FreyrBorder))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (debt.familyGroupId != null) "Family: ${group?.name ?: "Group"}" else "Personal Commitment",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FreyrPrimary
                            )
                            Surface(
                                color = if (isPaid) FreyrStatusPaidBg else FreyrStatusPendingBg,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    if (isPaid) "PAID IN FULL" else "PENDING",
                                    color = if (isPaid) FreyrStatusPaid else FreyrPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (!debt.description.isNullOrBlank()) {
                            Text(debt.description, fontSize = 13.sp, color = FreyrTextSecondary)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total", fontSize = 11.sp, color = FreyrTextMuted)
                                Text(FreyrRepository.formatCurrency(debt.amount), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Column {
                                Text("Paid", fontSize = 11.sp, color = FreyrTextMuted)
                                Text(FreyrRepository.formatCurrency(totalPaid), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = FreyrStatusPaid)
                            }
                            Column {
                                Text("Remaining", fontSize = 11.sp, color = FreyrTextMuted)
                                Text(FreyrRepository.formatCurrency(remaining), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = FreyrPrimary)
                            }
                        }

                        LinearProgressIndicator(
                            progress = { (totalPaid / debt.amount).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = FreyrPrimary,
                            trackColor = FreyrSecondaryLight
                        )

                        Text(
                            "Responsible: ${viewModel.getUserName(debt.assignedUserId)}",
                            fontSize = 12.sp,
                            color = FreyrTextSecondary
                        )
                    }
                }
            }

            item {
                Text(
                    "PAYMENT HISTORY (${payments.size} INSTALLMENTS)",
                    style = MaterialTheme.typography.labelSmall,
                    color = FreyrTextMuted,
                    fontWeight = FontWeight.Bold
                )
            }

            if (payments.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = FreyrSurface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FreyrBorder))
                    ) {
                        Text(
                            "No payments registered yet for this debt.",
                            fontSize = 13.sp,
                            color = FreyrTextSecondary,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            } else {
                items(payments.size) { index ->
                    val payment = payments[index]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = FreyrSurface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FreyrBorder))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    FreyrRepository.formatCurrency(payment.amount),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = FreyrStatusPaid
                                )
                                Text(
                                    "Paid by ${viewModel.getUserName(payment.userId)} · ${payment.date}",
                                    fontSize = 12.sp,
                                    color = FreyrTextSecondary
                                )
                                if (!payment.description.isNullOrBlank()) {
                                    Text(
                                        "\"${payment.description}\"",
                                        fontSize = 11.sp,
                                        color = FreyrTextMuted
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.deletePayment(payment.id, debt.id)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = FreyrTextMuted)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}
