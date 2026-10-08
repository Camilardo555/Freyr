package com.freyr.app.ui.screens.group

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freyr.app.data.repository.FreyrRepository
import com.freyr.app.ui.theme.*
import com.freyr.app.ui.viewmodel.FreyrViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyGroupDetailScreen(
    groupId: String,
    viewModel: FreyrViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddDebt: (String) -> Unit,
    onNavigateToDebtDetail: (String) -> Unit,
    onNavigateToAddPayment: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager()

    val currentUser by viewModel.currentUser.collectAsState()
    val allGroups by viewModel.allGroups.collectAsState()
    val allDebts by viewModel.allDebts.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    val group = remember(allGroups, groupId) { allGroups.find { it.id == groupId } }

    var inviteInput by rememberSaveable { mutableStateOf("") }
    var inviteMessage by remember { mutableStateOf<String?>(null) }
    var isInviteSuccess by remember { mutableStateOf(false) }

    if (group == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Family Group not found")
        }
        return
    }

    val isCreator = group.creatorId == currentUser?.id
    val members = remember(group, allUsers) { allUsers.filter { group.memberIds.contains(it.id) } }
    val groupDebts = remember(allDebts, groupId) { allDebts.filter { it.familyGroupId == groupId } }

    val totalDebt = groupDebts.sumOf { it.amount }
    val totalPaid = groupDebts.sumOf { viewModel.getPaidForDebt(it.id) }
    val totalRemaining = (totalDebt - totalPaid).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group.name, fontWeight = FontWeight.Bold, color = FreyrPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FreyrPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FreyrBackground)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToAddDebt(group.id) },
                containerColor = FreyrPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Family Debt", fontWeight = FontWeight.Bold)
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
                // Group totals card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FreyrSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FreyrBorder))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("SHARED FAMILY FINANCES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FreyrTextMuted)
                            if (isCreator) {
                                Text("ADMIN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FreyrPrimary)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Total", fontSize = 11.sp, color = FreyrTextMuted)
                                Text(FreyrRepository.formatCurrency(totalDebt), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Column {
                                Text("Paid", fontSize = 11.sp, color = FreyrTextMuted)
                                Text(FreyrRepository.formatCurrency(totalPaid), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = FreyrStatusPaid)
                            }
                            Column {
                                Text("Remaining", fontSize = 11.sp, color = FreyrTextMuted)
                                Text(FreyrRepository.formatCurrency(totalRemaining), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = FreyrPrimary)
                            }
                        }
                    }
                }
            }

            // Members Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "MEMBERS (${members.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = FreyrTextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        members.forEach { m ->
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        m.fullName + if (m.id == group.creatorId) " (Admin)" else "",
                                        fontSize = 12.sp
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Invite Member Box (Creator can invite other users)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FreyrSecondaryLight),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FreyrBorder))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Invite Family Member", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FreyrPrimary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = inviteInput,
                                onValueChange = {
                                    inviteInput = it
                                    inviteMessage = null
                                },
                                placeholder = { Text("Username or email (e.g. carlos)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = FreyrSurface,
                                    unfocusedContainerColor = FreyrSurface
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = { focusManager.clearFocus() })
                            )

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    if (inviteInput.isBlank()) return@Button
                                    coroutineScope.launch {
                                        val res = viewModel.inviteUser(group.id, inviteInput.trim())
                                        if (res.isSuccess) {
                                            isInviteSuccess = true
                                            inviteMessage = "Invitation sent to $inviteInput!"
                                            inviteInput = ""
                                        } else {
                                            isInviteSuccess = false
                                            inviteMessage = res.exceptionOrNull()?.message ?: "Failed to send invitation"
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FreyrPrimary)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
                            }
                        }

                        if (inviteMessage != null) {
                            Text(
                                inviteMessage ?: "",
                                fontSize = 11.sp,
                                color = if (isInviteSuccess) FreyrStatusPaid else FreyrError,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Family Debts List
            item {
                Text(
                    "SHARED FAMILY DEBTS (${groupDebts.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = FreyrTextMuted,
                    fontWeight = FontWeight.Bold
                )
            }

            if (groupDebts.isEmpty()) {
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
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No family debts in this group yet.", color = FreyrTextSecondary)
                        }
                    }
                }
            } else {
                items(groupDebts.size) { index ->
                    val debt = groupDebts[index]
                    val paid = viewModel.getPaidForDebt(debt.id)
                    val remaining = viewModel.getRemainingForDebt(debt.id)
                    val isPaid = remaining == 0.0 || debt.status == "paid"
                    val progress = if (debt.amount > 0) (paid / debt.amount).toFloat().coerceIn(0f, 1f) else 0f

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDebtDetail(debt.id) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = FreyrSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FreyrBorder))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(debt.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = FreyrTextPrimary)
                                    Text(
                                        "Assigned to: ${viewModel.getUserName(debt.assignedUserId)}",
                                        fontSize = 12.sp,
                                        color = FreyrPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Surface(
                                    color = if (isPaid) FreyrStatusPaidBg else FreyrStatusPendingBg,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        if (isPaid) "Paid" else "Pending",
                                        color = if (isPaid) FreyrStatusPaid else FreyrPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
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
                                Text("Remaining: ${FreyrRepository.formatCurrency(remaining)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FreyrPrimary)
                                if (!isPaid) {
                                    Button(
                                        onClick = { onNavigateToAddPayment(debt.id) },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = FreyrPrimary),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("+ Pay", fontSize = 11.sp)
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
