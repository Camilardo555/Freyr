package com.freyr.app.ui.screens.debt

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
import com.freyr.app.data.repository.FreyrRepository
import com.freyr.app.ui.theme.*
import com.freyr.app.ui.viewmodel.FreyrViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentScreen(
    debtId: String,
    viewModel: FreyrViewModel,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager()

    val currentUser by viewModel.currentUser.collectAsState()
    val allDebts by viewModel.allDebts.collectAsState()
    val debt = remember(allDebts, debtId) { allDebts.find { it.id == debtId } }

    var amountText by rememberSaveable { mutableStateOf("") }
    var dateText by rememberSaveable {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    }
    var description by rememberSaveable { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    if (debt == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Debt not found")
        }
        return
    }

    val currentPaid = viewModel.getPaidForDebt(debt.id)
    val remaining = viewModel.getRemainingForDebt(debt.id)

    val enteredAmount = amountText.toDoubleOrNull() ?: 0.0
    val projectedRemaining = (remaining - enteredAmount).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register Payment", fontWeight = FontWeight.Bold, color = FreyrPrimary) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Debt status overview
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FreyrSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FreyrBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(debt.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = FreyrTextPrimary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Total Debt", fontSize = 11.sp, color = FreyrTextMuted)
                            Text(FreyrRepository.formatCurrency(debt.amount), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("Paid So Far", fontSize = 11.sp, color = FreyrTextMuted)
                            Text(FreyrRepository.formatCurrency(currentPaid), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = FreyrStatusPaid)
                        }
                        Column {
                            Text("Remaining", fontSize = 11.sp, color = FreyrTextMuted)
                            Text(FreyrRepository.formatCurrency(remaining), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = FreyrPrimary)
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Surface(
                    color = FreyrErrorBg,
                    shape = RoundedCornerShape(10.dp),
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

            // Payment Amount Input (Proper Focusable TextField with Numeric Keypad)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Payment Amount ($) *", style = MaterialTheme.typography.labelLarge, color = FreyrTextSecondary)
                    if (remaining > 0) {
                        TextButton(
                            onClick = { amountText = if (remaining % 1.0 == 0.0) remaining.toLong().toString() else remaining.toString() },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Pay Full Balance", fontSize = 12.sp, color = FreyrPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                            amountText = it
                            errorMessage = null
                        }
                    },
                    prefix = { Text("$ ", fontWeight = FontWeight.Bold, color = FreyrPrimary) },
                    placeholder = { Text("e.g. 500000") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = FreyrSurface,
                        unfocusedContainerColor = FreyrSurface,
                        focusedBorderColor = FreyrPrimary,
                        unfocusedBorderColor = FreyrBorder
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                if (enteredAmount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Projected remaining:", fontSize = 11.sp, color = FreyrTextSecondary)
                        Text(
                            FreyrRepository.formatCurrency(projectedRemaining) + if (enteredAmount >= remaining) " (Will mark as PAID)" else "",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FreyrPrimary
                        )
                    }
                }
            }

            // Date Input
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Payment Date", style = MaterialTheme.typography.labelLarge, color = FreyrTextSecondary)
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = FreyrSurface,
                        unfocusedContainerColor = FreyrSurface,
                        focusedBorderColor = FreyrPrimary,
                        unfocusedBorderColor = FreyrBorder
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
            }

            // Payer note
            Text(
                "Payment recorded by: ${currentUser?.fullName ?: "Current User"}",
                fontSize = 12.sp,
                color = FreyrTextSecondary
            )

            // Description / Note
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Note / Description (Optional)", style = MaterialTheme.typography.labelLarge, color = FreyrTextSecondary)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("e.g. 1st installment, Wire transfer") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = FreyrSurface,
                        unfocusedContainerColor = FreyrSurface,
                        focusedBorderColor = FreyrPrimary,
                        unfocusedBorderColor = FreyrBorder
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    val amt = amountText.toDoubleOrNull()
                    if (amt == null || amt <= 0.0) {
                        errorMessage = "Please enter a valid payment amount greater than 0."
                        return@Button
                    }
                    isSaving = true
                    coroutineScope.launch {
                        val res = viewModel.addPayment(
                            debtId = debt.id,
                            amt = amt,
                            date = dateText.trim().ifEmpty { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) },
                            desc = description.trim().ifEmpty { null }
                        )
                        isSaving = false
                        if (res.isSuccess) {
                            onNavigateBack()
                        } else {
                            errorMessage = res.exceptionOrNull()?.message ?: "Failed to record payment"
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FreyrPrimary, contentColor = Color.White)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Payment", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
