package com.freyr.app.ui.screens.debt

import androidx.compose.foundation.clickable
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
fun PersonalFinancesScreen(
    viewModel: FreyrViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddDebt: () -> Unit,
    onNavigateToDebtDetail: (String) -> Unit,
    onNavigateToAddPayment: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsState()
    val allDebts by viewModel.allDebts.collectAsState()

    var filter by remember { mutableStateOf("all") } // "all", "pending", "paid"

    val personalDebts = remember(allDebts, currentUser) {
        val uid = currentUser?.id ?: ""
        allDebts.filter { it.familyGroupId == null && (it.assignedUserId == uid || it.createdBy == uid) }
    }

    val filteredDebts = remember(personalDebts, filter) {
        personalDebts.filter { debt ->
            val remaining = viewModel.getRemainingForDebt(debt.id)
            val isPaid = remaining == 0.0 || debt.status == "paid"
            when (filter) {
                "pending" -> !isPaid
                "paid" -> isPaid
                else -> true
            }
        }
    }

    val totalDebt = personalDebts.sumOf { it.amount }
    val totalPaid = personalDebts.sumOf { viewModel.getPaidForDebt(it.id) }
    val totalRemaining = (totalDebt - totalPaid).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Finances", fontWeight = FontWeight.Bold, color = FreyrPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FreyrPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FreyrBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddDebt,
                containerColor = FreyrPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Debt")
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
                // Overview metrics card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FreyrSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FreyrBorder))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("PERSONAL DEBT SUMMARY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FreyrTextMuted)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Total", fontSize = 11.sp, color = FreyrTextSecondary)
                                Text(FreyrRepository.formatCurrency(totalDebt), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = FreyrTextPrimary)
                            }
                            Column {
                                Text("Paid", fontSize = 11.sp, color = FreyrTextSecondary)
                                Text(FreyrRepository.formatCurrency(totalPaid), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = FreyrStatusPaid)
                            }
                            Column {
                                Text("Remaining", fontSize = 11.sp, color = FreyrTextSecondary)
                                Text(FreyrRepository.formatCurrency(totalRemaining), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = FreyrPrimary)
                            }
                        }
                    }
                }
            }

            item {
                // Filter chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filter == "all",
                        onClick = { filter = "all" },
                        label = { Text("All (${personalDebts.size})") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = FreyrPrimary, selectedLabelColor = Color.White)
                    )
                    FilterChip(
                        selected = filter == "pending",
                        onClick = { filter = "pending" },
                        label = { Text("Pending") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = FreyrPrimary, selectedLabelColor = Color.White)
                    )
                    FilterChip(
                        selected = filter == "paid",
                        onClick = { filter = "paid" },
                        label = { Text("Paid") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = FreyrPrimary, selectedLabelColor = Color.White)
                    )
                }
            }

            if (filteredDebts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = FreyrSurface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FreyrBorder))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No personal debts found.", color = FreyrTextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = onNavigateToAddDebt) {
                                Text("+ Add your first debt", color = FreyrPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(filteredDebts.size) { index ->
                    val debt = filteredDebts[index]
                    val paid = viewModel.getPaidForDebt(debt.id)
                    val remaining = viewModel.getRemainingForDebt(debt.id)
                    val isPaid = remaining == 0.0 || debt.status == "paid"
                    val progress = if (debt.amount > 0) (paid / debt.amount).toFloat().coerceIn(0f, 1f) else 0f

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDebtDetail(debt.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = FreyrSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FreyrBorder))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(debt.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = FreyrTextPrimary)
                                    if (!debt.description.isNullOrBlank()) {
                                        Text(debt.description, fontSize = 12.sp, color = FreyrTextSecondary)
                                    }
                                }

                                Surface(
                                    color = if (isPaid) FreyrStatusPaidBg else FreyrStatusPendingBg,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        if (isPaid) "Paid" else "Pending",
                                        color = if (isPaid) FreyrStatusPaid else FreyrPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Figures (Prompt example: Laptop: Total $3,000,000, Paid $1,000,000, Remaining $2,000,000)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total: ${FreyrRepository.formatCurrency(debt.amount)}", fontSize = 12.sp, color = FreyrTextSecondary)
                                Text("Paid: ${FreyrRepository.formatCurrency(paid)}", fontSize = 12.sp, color = FreyrStatusPaid)
                                Text("Remaining: ${FreyrRepository.formatCurrency(remaining)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FreyrPrimary)
                            }

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = FreyrPrimary,
                                trackColor = FreyrSecondaryLight
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { onNavigateToDebtDetail(debt.id) }
                                ) {
                                    Text("History", fontSize = 12.sp, color = FreyrTextSecondary)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                viewModel.deleteDebt(debt.id)
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = FreyrTextMuted)
                                    }

                                    if (!isPaid) {
                                        Button(
                                            onClick = { onNavigateToAddPayment(debt.id) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = FreyrPrimary),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Text("+ Add Payment", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}
