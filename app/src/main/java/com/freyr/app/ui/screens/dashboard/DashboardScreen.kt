package com.freyr.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freyr.app.data.repository.FreyrRepository
import com.freyr.app.ui.theme.*
import com.freyr.app.ui.viewmodel.FreyrViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: FreyrViewModel,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToAddDebt: () -> Unit,
    onNavigateToCreateFamilyGroup: () -> Unit,
    onNavigateToAcceptInvitations: () -> Unit,
    onNavigateToPersonalFinances: () -> Unit,
    onNavigateToFamilyGroups: () -> Unit,
    onNavigateToDebtDetail: (String) -> Unit,
    onNavigateToAddPayment: (String) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val summary by viewModel.financialSummary.collectAsState()
    val allDebts by viewModel.allDebts.collectAsState()
    val allGroups by viewModel.allGroups.collectAsState()
    val allInvitations by viewModel.allInvitations.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    var showAccountSwitcher by remember { mutableStateOf(false) }

    val pendingInvitesCount = remember(allInvitations, currentUser) {
        val uid = currentUser?.id ?: ""
        allInvitations.count { it.invitedUserId == uid && it.status == "pending" }
    }

    val userGroupIds = remember(allGroups, currentUser) {
        val uid = currentUser?.id ?: ""
        allGroups.filter { it.memberIds.contains(uid) }.map { it.id }
    }

    val activeDebts = remember(allDebts, currentUser, userGroupIds) {
        val uid = currentUser?.id ?: ""
        allDebts.filter { debt ->
            val isRelevant = if (debt.familyGroupId == null) {
                debt.assignedUserId == uid || debt.createdBy == uid
            } else {
                userGroupIds.contains(debt.familyGroupId)
            }
            val remaining = viewModel.getRemainingForDebt(debt.id)
            isRelevant && remaining > 0 && debt.status == "pending"
        }.take(3)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(FreyrPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("F", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Text("Freyr", fontWeight = FontWeight.Bold, color = FreyrPrimary)
                    }
                },
                actions = {
                    // Profile button with avatar
                    IconButton(onClick = { showAccountSwitcher = true }) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(FreyrSecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser?.fullName?.take(2)?.uppercase() ?: "U",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = FreyrPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FreyrBackground)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = FreyrSurface, tonalElevation = 4.dp) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FreyrPrimary,
                        selectedTextColor = FreyrPrimary,
                        indicatorColor = FreyrSecondary
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToPersonalFinances,
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Personal") },
                    label = { Text("Personal") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToFamilyGroups,
                    icon = { Icon(Icons.Default.Group, contentDescription = "Family") },
                    label = { Text("Family") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToAcceptInvitations,
                    icon = {
                        BadgedBox(
                            badge = {
                                if (pendingInvitesCount > 0) {
                                    Badge(containerColor = FreyrPrimary) {
                                        Text("$pendingInvitesCount", color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Invites")
                        }
                    },
                    label = { Text("Invites") }
                )
            }
        },
        containerColor = FreyrBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Welcome banner
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Hello, ${currentUser?.fullName ?: "User"}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        "Here is your current financial posture.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // --- 4 MAIN DASHBOARD OPTIONS (Required by prompt) ---
            item {
                Text(
                    "MAIN ACTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = FreyrTextMuted,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. Edit Profile
                        MainActionCard(
                            title = "1. Edit Profile",
                            subtitle = "Credentials & bio",
                            icon = Icons.Default.Person,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToEditProfile
                        )

                        // 2. Add Debt (Primary CTA)
                        MainActionCard(
                            title = "2. Add Debt",
                            subtitle = "Personal or family",
                            icon = Icons.Default.AddCircle,
                            isPrimary = true,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToAddDebt
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 3. Create Family Group
                        MainActionCard(
                            title = "3. Create Group",
                            subtitle = "Family expense pool",
                            icon = Icons.Default.GroupAdd,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToCreateFamilyGroup
                        )

                        // 4. Accept Family Group Invitation
                        MainActionCard(
                            title = "4. Invitations",
                            subtitle = if (pendingInvitesCount > 0) "$pendingInvitesCount pending" else "View invites",
                            icon = Icons.Default.Mail,
                            badge = if (pendingInvitesCount > 0) pendingInvitesCount.toString() else null,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToAcceptInvitations
                        )
                    }
                }
            }

            // --- FINANCIAL ACTIVITY SUMMARY ---
            item {
                Text(
                    "FINANCIAL ACTIVITY SUMMARY",
                    style = MaterialTheme.typography.labelSmall,
                    color = FreyrTextMuted,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                // Highlight: Total Amount Pending
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FreyrPrimary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "TOTAL AMOUNT PENDING",
                            color = FreyrSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            FreyrRepository.formatCurrency(summary.totalPendingAmount),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        HorizontalDivider(color = FreyrPrimaryLight.copy(alpha = 0.5f))
                        Text(
                            "Outstanding balance across all your active debts",
                            color = FreyrSecondary.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryMetricCard(
                        title = "Pending Debts",
                        value = "${summary.pendingDebtsCount}",
                        subtitle = "Active balances",
                        valueColor = FreyrPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetricCard(
                        title = "Paid Debts",
                        value = "${summary.paidDebtsCount}",
                        subtitle = "Fully settled",
                        valueColor = FreyrStatusPaid,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryMetricCard(
                        title = "Personal Payments",
                        value = FreyrRepository.formatCurrency(summary.personalPaymentsTotal),
                        subtitle = "Paid by you",
                        valueColor = FreyrTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetricCard(
                        title = "Family Debts",
                        value = FreyrRepository.formatCurrency(summary.familyDebtsPendingTotal),
                        subtitle = "Group pending",
                        valueColor = FreyrTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // --- REQUIRING ATTENTION ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "PENDING DEBTS",
                        style = MaterialTheme.typography.labelSmall,
                        color = FreyrTextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "View all",
                        color = FreyrPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigateToPersonalFinances() }
                    )
                }
            }

            if (activeDebts.isEmpty()) {
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
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FreyrStatusPaid, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("All Clear!", fontWeight = FontWeight.Bold, color = FreyrTextPrimary)
                            Text("No pending debts requiring payment.", fontSize = 12.sp, color = FreyrTextSecondary)
                        }
                    }
                }
            } else {
                items(activeDebts.size) { index ->
                    val debt = activeDebts[index]
                    val paid = viewModel.getPaidForDebt(debt.id)
                    val remaining = viewModel.getRemainingForDebt(debt.id)
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
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(debt.name, fontWeight = FontWeight.Bold, color = FreyrTextPrimary)
                                    Text(
                                        if (debt.familyGroupId != null) "Family Debt" else "Personal Debt",
                                        fontSize = 11.sp,
                                        color = FreyrTextSecondary
                                    )
                                }
                                Button(
                                    onClick = { onNavigateToAddPayment(debt.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = FreyrPrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("+ Pay", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Paid: ${FreyrRepository.formatCurrency(paid)}", fontSize = 11.sp, color = FreyrStatusPaid)
                                Text("Remaining: ${FreyrRepository.formatCurrency(remaining)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FreyrPrimary)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // Account Switcher Dialog for quick test
    if (showAccountSwitcher) {
        AlertDialog(
            onDismissRequest = { showAccountSwitcher = false },
            title = { Text("Account Profile", fontWeight = FontWeight.Bold, color = FreyrPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Logged in as: ${currentUser?.fullName} (@${currentUser?.username})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text("Switch test account:", style = MaterialTheme.typography.labelSmall)
                    allUsers.forEach { user ->
                        TextButton(
                            onClick = {
                                viewModel.switchUser(user.id)
                                showAccountSwitcher = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(user.fullName, color = if (user.id == currentUser?.id) FreyrPrimary else FreyrTextPrimary)
                                Text("@${user.username}", fontSize = 11.sp, color = FreyrTextSecondary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showAccountSwitcher = false
                    viewModel.logout()
                }) {
                    Text("Log Out", color = FreyrError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccountSwitcher = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun MainActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isPrimary: Boolean = false,
    badge: String? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) FreyrPrimary else FreyrSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = if (isPrimary) null else CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FreyrBorder))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isPrimary) FreyrPrimaryDark else FreyrSecondaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (isPrimary) Color.White else FreyrPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (badge != null) {
                    Badge(containerColor = FreyrPrimary) {
                        Text(badge, color = Color.White)
                    }
                }
            }

            Column {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isPrimary) Color.White else FreyrTextPrimary
                )
                Text(
                    subtitle,
                    fontSize = 11.sp,
                    color = if (isPrimary) FreyrSecondary else FreyrTextSecondary
                )
            }
        }
    }
}

@Composable
fun SummaryMetricCard(
    title: String,
    value: String,
    subtitle: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FreyrSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FreyrBorder))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = FreyrTextMuted)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = valueColor)
            Text(subtitle, fontSize = 10.sp, color = FreyrTextSecondary)
        }
    }
}
