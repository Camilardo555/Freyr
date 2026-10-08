package com.freyr.app.ui.screens.group

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freyr.app.ui.theme.*
import com.freyr.app.ui.viewmodel.FreyrViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcceptInvitationsScreen(
    viewModel: FreyrViewModel,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsState()
    val allInvitations by viewModel.allInvitations.collectAsState()
    val allGroups by viewModel.allGroups.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    val pendingInvitations = remember(allInvitations, currentUser) {
        val uid = currentUser?.id ?: ""
        allInvitations.filter { it.invitedUserId == uid && it.status == "pending" }
    }

    // Sent invitations by groups created by currentUser
    val userCreatedGroupIds = remember(allGroups, currentUser) {
        val uid = currentUser?.id ?: ""
        allGroups.filter { it.creatorId == uid }.map { it.id }
    }
    val sentInvitations = remember(allInvitations, userCreatedGroupIds) {
        allInvitations.filter { userCreatedGroupIds.contains(it.familyGroupId) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Family Invitations", fontWeight = FontWeight.Bold, color = FreyrPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FreyrPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FreyrBackground)
            )
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

            item {
                Text(
                    "PENDING INVITATIONS FOR YOU (${pendingInvitations.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = FreyrTextMuted,
                    fontWeight = FontWeight.Bold
                )
            }

            if (pendingInvitations.isEmpty()) {
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
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Mail, contentDescription = null, tint = FreyrPrimary.copy(alpha = 0.5f), modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No pending invitations", fontWeight = FontWeight.Bold, color = FreyrTextPrimary)
                            Text("When a family member invites you to their group, you will see it here.", fontSize = 12.sp, color = FreyrTextSecondary)
                        }
                    }
                }
            } else {
                items(pendingInvitations.size) { index ->
                    val invite = pendingInvitations[index]
                    val group = allGroups.find { it.id == invite.familyGroupId }
                    val inviter = allUsers.find { it.id == invite.invitedByUserId }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = FreyrSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FreyrBorder))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(group?.name ?: "Family Group", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = FreyrTextPrimary)
                            Text(
                                "Invited by ${inviter?.fullName ?: "A member"} (@${inviter?.username ?: ""})",
                                fontSize = 12.sp,
                                color = FreyrTextSecondary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.respondToInvitation(invite.id, false)
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Decline", fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.respondToInvitation(invite.id, true)
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = FreyrPrimary),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Accept & Join", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Sent invitations
            if (sentInvitations.isNotEmpty()) {
                item {
                    Text(
                        "INVITATIONS SENT BY YOU (${sentInvitations.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = FreyrTextMuted,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                items(sentInvitations.size) { index ->
                    val invite = sentInvitations[index]
                    val targetUser = allUsers.find { it.id == invite.invitedUserId }
                    val group = allGroups.find { it.id == invite.familyGroupId }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = FreyrSurface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FreyrBorder))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(targetUser?.fullName ?: "User", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Group: ${group?.name}", fontSize = 11.sp, color = FreyrTextSecondary)
                            }
                            Surface(
                                color = when (invite.status) {
                                    "accepted" -> FreyrStatusPaidBg
                                    "declined" -> FreyrErrorBg
                                    else -> FreyrSecondaryLight
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    invite.status.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (invite.status) {
                                        "accepted" -> FreyrStatusPaid
                                        "declined" -> FreyrError
                                        else -> FreyrPrimary
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
