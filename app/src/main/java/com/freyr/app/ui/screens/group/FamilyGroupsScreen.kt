package com.freyr.app.ui.screens.group

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freyr.app.data.repository.FreyrRepository
import com.freyr.app.ui.theme.*
import com.freyr.app.ui.viewmodel.FreyrViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyGroupsScreen(
    viewModel: FreyrViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCreateGroup: () -> Unit,
    onNavigateToGroupDetail: (String) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allGroups by viewModel.allGroups.collectAsState()
    val allDebts by viewModel.allDebts.collectAsState()

    val userGroups = remember(allGroups, currentUser) {
        val uid = currentUser?.id ?: ""
        allGroups.filter { it.memberIds.contains(uid) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Family Groups", fontWeight = FontWeight.Bold, color = FreyrPrimary) },
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
                onClick = onNavigateToCreateGroup,
                containerColor = FreyrPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Group")
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
            item { Spacer(modifier = Modifier.height(4.dp)) }

            if (userGroups.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = FreyrSurface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FreyrBorder))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = FreyrPrimary, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No Family Groups Yet", fontWeight = FontWeight.Bold, color = FreyrTextPrimary)
                            Text(
                                "Create a group to invite members and assign shared family debts.",
                                fontSize = 12.sp,
                                color = FreyrTextSecondary,
                                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                            )
                            Button(
                                onClick = onNavigateToCreateGroup,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FreyrPrimary)
                            ) {
                                Text("Create First Group")
                            }
                        }
                    }
                }
            } else {
                items(userGroups.size) { index ->
                    val group = userGroups[index]
                    val isCreator = group.creatorId == currentUser?.id
                    val groupDebts = allDebts.filter { it.familyGroupId == group.id }
                    val totalDebt = groupDebts.sumOf { it.amount }
                    val totalPaid = groupDebts.sumOf { viewModel.getPaidForDebt(it.id) }
                    val remaining = (totalDebt - totalPaid).coerceAtLeast(0.0)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToGroupDetail(group.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = FreyrSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                                Text(group.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = FreyrTextPrimary)
                                if (isCreator) {
                                    Surface(
                                        color = FreyrSecondaryLight,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            "Admin",
                                            color = FreyrPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                "${group.memberIds.size} members · ${groupDebts.size} shared debts",
                                fontSize = 12.sp,
                                color = FreyrTextSecondary
                            )

                            HorizontalDivider(color = FreyrBorder.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Group Total", fontSize = 11.sp, color = FreyrTextMuted)
                                    Text(FreyrRepository.formatCurrency(totalDebt), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Column {
                                    Text("Group Paid", fontSize = 11.sp, color = FreyrTextMuted)
                                    Text(FreyrRepository.formatCurrency(totalPaid), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = FreyrStatusPaid)
                                }
                                Column {
                                    Text("Remaining", fontSize = 11.sp, color = FreyrTextMuted)
                                    Text(FreyrRepository.formatCurrency(remaining), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = FreyrPrimary)
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
