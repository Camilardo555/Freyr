package com.freyr.app.ui.screens.debt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freyr.app.ui.theme.*
import com.freyr.app.ui.viewmodel.FreyrViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtScreen(
    viewModel: FreyrViewModel,
    initialGroupId: String? = null,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager()

    val currentUser by viewModel.currentUser.collectAsState()
    val allGroups by viewModel.allGroups.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    val userGroups = remember(allGroups, currentUser) {
        val uid = currentUser?.id ?: ""
        allGroups.filter { it.memberIds.contains(uid) }
    }

    // Input States - using standard mutable state variables for full editability
    var name by rememberSaveable { mutableStateOf("") }
    var amountText by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    var isFamilyDebt by rememberSaveable {
        mutableStateOf(initialGroupId != null || (userGroups.isNotEmpty() && initialGroupId != null))
    }
    var selectedGroupId by rememberSaveable {
        mutableStateOf(initialGroupId ?: userGroups.firstOrNull()?.id ?: "")
    }
    var assignedUserId by rememberSaveable {
        mutableStateOf<String?>(null) // null = entire family
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val activeGroup = userGroups.find { it.id == selectedGroupId } ?: userGroups.firstOrNull()
    val groupMembers = remember(activeGroup, allUsers) {
        if (activeGroup != null) {
            allUsers.filter { activeGroup.memberIds.contains(it.id) }
        } else emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Debt",
                        fontWeight = FontWeight.Bold,
                        color = FreyrPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = FreyrPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FreyrBackground
                )
            )
        },
        containerColor = FreyrBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (errorMessage != null) {
                Surface(
                    color = FreyrErrorBg,
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FreyrError)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = FreyrError,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Scope Selector: Personal Debt vs Family Debt
            Text(
                "Debt Type",
                style = MaterialTheme.typography.labelLarge,
                color = FreyrTextSecondary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FreyrSecondaryLight, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        isFamilyDebt = false
                        assignedUserId = currentUser?.id
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isFamilyDebt) FreyrSurface else Color.Transparent,
                        contentColor = if (!isFamilyDebt) FreyrPrimary else FreyrTextSecondary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    elevation = if (!isFamilyDebt) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text("Personal Debt", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        if (userGroups.isEmpty()) {
                            errorMessage = "You must create or join a family group first."
                        } else {
                            isFamilyDebt = true
                            if (selectedGroupId.isEmpty()) {
                                selectedGroupId = userGroups.first().id
                            }
                            assignedUserId = null
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFamilyDebt) FreyrSurface else Color.Transparent,
                        contentColor = if (isFamilyDebt) FreyrPrimary else FreyrTextSecondary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    elevation = if (isFamilyDebt) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text("Family Debt", fontWeight = FontWeight.SemiBold)
                }
            }

            // 1. Debt Name Field - PROPERLY FOCUSABLE & EDITABLE
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Debt Name / Title *",
                    style = MaterialTheme.typography.labelLarge,
                    color = FreyrTextSecondary
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (errorMessage != null) errorMessage = null
                    },
                    placeholder = { Text("e.g. Laptop, Internet Bill, Groceries") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = FreyrSurface,
                        unfocusedContainerColor = FreyrSurface,
                        focusedBorderColor = FreyrPrimary,
                        unfocusedBorderColor = FreyrBorder,
                        focusedTextColor = FreyrTextPrimary,
                        unfocusedTextColor = FreyrTextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
            }

            // 2. Total Amount Field - PROPERLY FOCUSABLE WITH NUMERIC KEYBOARD
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Total Amount ($) *",
                    style = MaterialTheme.typography.labelLarge,
                    color = FreyrTextSecondary
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        // Allow digits and optional single decimal point
                        if (it.isEmpty() || it.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                            amountText = it
                            if (errorMessage != null) errorMessage = null
                        }
                    },
                    placeholder = { Text("e.g. 3000000 or 120000") },
                    prefix = { Text("$ ", fontWeight = FontWeight.Bold, color = FreyrPrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = FreyrSurface,
                        unfocusedContainerColor = FreyrSurface,
                        focusedBorderColor = FreyrPrimary,
                        unfocusedBorderColor = FreyrBorder,
                        focusedTextColor = FreyrTextPrimary,
                        unfocusedTextColor = FreyrTextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
            }

            // Family Group assignment options if Family Debt is selected
            if (isFamilyDebt) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FreyrSecondaryLight),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FreyrBorder))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Family Group Settings",
                            style = MaterialTheme.typography.titleMedium,
                            color = FreyrPrimary
                        )

                        // Select Group
                        Text("Group:", style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            userGroups.forEach { group ->
                                FilterChip(
                                    selected = selectedGroupId == group.id,
                                    onClick = { selectedGroupId = group.id },
                                    label = { Text(group.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = FreyrPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Select Assigned Member
                        Text("Responsible Member:", style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = assignedUserId == null,
                                onClick = { assignedUserId = null },
                                label = { Text("Entire Family") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FreyrPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )

                            groupMembers.forEach { member ->
                                FilterChip(
                                    selected = assignedUserId == member.id,
                                    onClick = { assignedUserId = member.id },
                                    label = { Text(member.fullName) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = FreyrPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 3. Description Note Field
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Description / Notes (Optional)",
                    style = MaterialTheme.typography.labelLarge,
                    color = FreyrTextSecondary
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("e.g. Monthly installment plan, 50/50 split") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = FreyrSurface,
                        unfocusedContainerColor = FreyrSurface,
                        focusedBorderColor = FreyrPrimary,
                        unfocusedBorderColor = FreyrBorder,
                        focusedTextColor = FreyrTextPrimary,
                        unfocusedTextColor = FreyrTextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    val trimmedName = name.trim()
                    if (trimmedName.isEmpty()) {
                        errorMessage = "Debt name cannot be empty."
                        return@Button
                    }

                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0.0) {
                        errorMessage = "Amount must be a valid positive number."
                        return@Button
                    }

                    isSaving = true
                    coroutineScope.launch {
                        val result = viewModel.addDebt(
                            name = trimmedName,
                            desc = description.trim().ifEmpty { null },
                            amt = amount,
                            assigned = if (isFamilyDebt) assignedUserId else currentUser?.id,
                            group = if (isFamilyDebt) selectedGroupId else null
                        )

                        isSaving = false
                        if (result.isSuccess) {
                            onNavigateBack()
                        } else {
                            errorMessage = result.exceptionOrNull()?.message ?: "Failed to save debt"
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FreyrPrimary,
                    contentColor = Color.White
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Save Debt",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
