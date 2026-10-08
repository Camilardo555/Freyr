package com.freyr.app.ui.screens.profile

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freyr.app.ui.theme.*
import com.freyr.app.ui.viewmodel.FreyrViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: FreyrViewModel,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager()

    val currentUser by viewModel.currentUser.collectAsState()

    var fullName by rememberSaveable { mutableStateOf(currentUser?.fullName ?: "") }
    var username by rememberSaveable { mutableStateOf(currentUser?.username ?: "") }
    var email by rememberSaveable { mutableStateOf(currentUser?.email ?: "") }
    var password by rememberSaveable { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            fullName = currentUser?.fullName ?: ""
            username = currentUser?.username ?: ""
            email = currentUser?.email ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold, color = FreyrPrimary) },
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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

            if (successMessage != null) {
                Surface(
                    color = FreyrStatusPaidBg,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = successMessage ?: "",
                        color = FreyrStatusPaid,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Full Name
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Full Name", style = MaterialTheme.typography.labelLarge, color = FreyrTextSecondary)
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it; errorMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
            }

            // Username
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Username", style = MaterialTheme.typography.labelLarge, color = FreyrTextSecondary)
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it; errorMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
            }

            // Email
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Email Address", style = MaterialTheme.typography.labelLarge, color = FreyrTextSecondary)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
            }

            // Password
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("New Password (Leave empty to keep current)", style = MaterialTheme.typography.labelLarge, color = FreyrTextSecondary)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    visualTransformation = PasswordVisualTransformation(),
                    placeholder = { Text("••••••••") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (fullName.isBlank()) {
                        errorMessage = "Full name cannot be empty."
                        return@Button
                    }
                    if (username.isBlank()) {
                        errorMessage = "Username cannot be empty."
                        return@Button
                    }
                    if (email.isBlank()) {
                        errorMessage = "Email cannot be empty."
                        return@Button
                    }

                    isSaving = true
                    coroutineScope.launch {
                        val res = viewModel.updateProfile(
                            fn = fullName.trim(),
                            un = username.trim(),
                            em = email.trim(),
                            pw = password.trim().ifEmpty { null }
                        )
                        isSaving = false
                        if (res.isSuccess) {
                            successMessage = "Profile updated successfully!"
                            errorMessage = null
                        } else {
                            errorMessage = res.exceptionOrNull()?.message ?: "Failed to update profile"
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
                    Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
