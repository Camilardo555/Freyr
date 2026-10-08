package com.freyr.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freyr.app.ui.theme.*
import com.freyr.app.ui.viewmodel.FreyrViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: FreyrViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager()

    var identifier by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = FreyrBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Branding
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(FreyrPrimary, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "F",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Freyr",
                style = MaterialTheme.typography.headlineLarge,
                color = FreyrPrimary
            )
            Text(
                "Minimalist Personal & Family Finances",
                style = MaterialTheme.typography.bodyMedium,
                color = FreyrTextSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = FreyrSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FreyrBorder))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Sign In",
                        style = MaterialTheme.typography.titleLarge,
                        color = FreyrPrimary
                    )

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
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // Email or Username field
                    OutlinedTextField(
                        value = identifier,
                        onValueChange = {
                            identifier = it
                            errorMessage = null
                        },
                        label = { Text("Email or Username") },
                        placeholder = { Text("juan or juan@example.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = FreyrPrimary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FreyrPrimary,
                            unfocusedBorderColor = FreyrBorder
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = FreyrPrimary)
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FreyrPrimary,
                            unfocusedBorderColor = FreyrBorder
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (identifier.isBlank()) {
                                errorMessage = "Please enter username or email."
                                return@Button
                            }
                            isLoading = true
                            coroutineScope.launch {
                                val res = viewModel.login(identifier, password)
                                isLoading = false
                                if (res.isSuccess) {
                                    onLoginSuccess()
                                } else {
                                    errorMessage = res.exceptionOrNull()?.message ?: "Login failed"
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FreyrPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Log In", fontWeight = FontWeight.Bold)
                        }
                    }

                    TextButton(
                        onClick = onNavigateToRegister,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            "Don't have an account? Create Account",
                            color = FreyrPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    HorizontalDivider(color = FreyrBorder)

                    // Quick Demo Accounts for effortless testing
                    Text(
                        "Quick Demo Accounts (Tap to test):",
                        style = MaterialTheme.typography.labelSmall,
                        color = FreyrTextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                identifier = "juan"
                                password = "password123"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Juan", fontSize = 12.sp, color = FreyrPrimary)
                        }
                        OutlinedButton(
                            onClick = {
                                identifier = "maria"
                                password = "password123"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("María", fontSize = 12.sp, color = FreyrPrimary)
                        }
                        OutlinedButton(
                            onClick = {
                                identifier = "carlos"
                                password = "password123"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Carlos", fontSize = 12.sp, color = FreyrPrimary)
                        }
                    }
                }
            }
        }
    }
}
