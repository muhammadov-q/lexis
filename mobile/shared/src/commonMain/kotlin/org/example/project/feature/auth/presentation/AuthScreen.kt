package org.example.project.feature.auth.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.project.common.presentation.ui.helpers.asString
import org.example.project.common.presentation.ui.theme.LexisColors
import org.example.project.common.presentation.ui.theme.LexisSpacing
import org.example.project.common.presentation.ui.theme.LexisTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import lexis.shared.generated.resources.Res
import lexis.shared.generated.resources.auth_action_continue_as_guest
import lexis.shared.generated.resources.auth_action_create_account
import lexis.shared.generated.resources.auth_action_creating_account
import lexis.shared.generated.resources.auth_action_sign_in
import lexis.shared.generated.resources.auth_action_signing_in
import lexis.shared.generated.resources.auth_branding
import lexis.shared.generated.resources.ic_lexis_logo
import lexis.shared.generated.resources.auth_create_subtitle
import lexis.shared.generated.resources.auth_create_title
import lexis.shared.generated.resources.auth_email_label
import lexis.shared.generated.resources.auth_error_password_min_length
import lexis.shared.generated.resources.auth_first_name_label
import lexis.shared.generated.resources.auth_have_account_sign_in
import lexis.shared.generated.resources.auth_last_name_label
import lexis.shared.generated.resources.auth_no_account_create
import lexis.shared.generated.resources.auth_password_hide
import lexis.shared.generated.resources.auth_password_label
import lexis.shared.generated.resources.auth_password_show
import lexis.shared.generated.resources.auth_sign_in_subtitle
import lexis.shared.generated.resources.auth_sign_in_title

@Composable
fun AuthScreen(
    initialMode: AuthMode,
    onGuestContinue: () -> Unit,
) {
    val viewModel: AuthViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.onEvent(AuthEvent.InitialModeSet(initialMode))
    }

    AuthScreenContent(
        state = state,
        onEvent = viewModel::onEvent,
        onGuestContinue = onGuestContinue,
    )
}

@Composable
private fun AuthScreenContent(
    state: AuthContract,
    onEvent: (AuthEvent) -> Unit,
    onGuestContinue: () -> Unit,
) {
    val isSignIn = state.mode == AuthMode.SignIn

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LexisColors.Background)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding()
            .padding(LexisSpacing.padding_24),
        verticalArrangement = Arrangement.spacedBy(
            LexisSpacing.padding_16,
            Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(Res.drawable.ic_lexis_logo),
            contentDescription = null,
            modifier = Modifier.size(LexisSpacing.padding_56),
        )
        Text(
            text = stringResource(Res.string.auth_branding),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = LexisSpacing.padding_4),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(LexisSpacing.padding_24),
                verticalArrangement = Arrangement.spacedBy(LexisSpacing.padding_12),
            ) {
                Text(
                    text = if (isSignIn) {
                        stringResource(Res.string.auth_sign_in_title)
                    } else {
                        stringResource(Res.string.auth_create_title)
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = if (isSignIn) {
                        stringResource(Res.string.auth_sign_in_subtitle)
                    } else {
                        stringResource(Res.string.auth_create_subtitle)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (!isSignIn) {
                    OutlinedTextField(
                        value = state.firstName,
                        onValueChange = { onEvent(AuthEvent.FirstNameChanged(it)) },
                        label = { Text(stringResource(Res.string.auth_first_name_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = state.lastName,
                        onValueChange = { onEvent(AuthEvent.LastNameChanged(it)) },
                        label = { Text(stringResource(Res.string.auth_last_name_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                OutlinedTextField(
                    value = state.email,
                    onValueChange = { onEvent(AuthEvent.EmailChanged(it)) },
                    label = { Text(stringResource(Res.string.auth_email_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors()
                        .copy(focusedLabelColor = LexisColors.Subtext)

                )

                OutlinedTextField(
                    value = state.password,
                    onValueChange = { onEvent(AuthEvent.PasswordChanged(it)) },
                    label = { Text(stringResource(Res.string.auth_password_label)) },
                    visualTransformation = if (state.isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        TextButton(onClick = { onEvent(AuthEvent.TogglePasswordVisibilityRequested) }) {
                            Text(
                                text = if (state.isPasswordVisible) {
                                    stringResource(Res.string.auth_password_hide)
                                } else {
                                    stringResource(Res.string.auth_password_show)
                                },
                                color = LexisColors.Subtext
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (!isSignIn) {
                    Text(
                        text = stringResource(Res.string.auth_error_password_min_length),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                state.error?.let {
                    Text(
                        text = it.asString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Button(
                    onClick = { onEvent(AuthEvent.SubmitRequested) },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            strokeWidth = LexisSpacing.padding_4,
                            modifier = Modifier.size(LexisSpacing.padding_16),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(modifier = Modifier.width(LexisSpacing.padding_8))
                    }
                    Text(
                        text = if (state.isLoading) {
                            if (isSignIn) {
                                stringResource(Res.string.auth_action_signing_in)
                            } else {
                                stringResource(Res.string.auth_action_creating_account)
                            }
                        } else {
                            if (isSignIn) {
                                stringResource(Res.string.auth_action_sign_in)
                            } else {
                                stringResource(Res.string.auth_action_create_account)
                            }
                        }
                    )
                }

                TextButton(
                    onClick = { onEvent(AuthEvent.ToggleModeRequested) },
                    enabled = !state.isLoading,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text(
                        text = if (isSignIn) {
                            stringResource(Res.string.auth_no_account_create)
                        } else {
                            stringResource(Res.string.auth_have_account_sign_in)
                        },
                        color = LexisColors.Subtext
                    )

                }

                OutlinedButton(
                    onClick = {
                        onGuestContinue()
                    },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(Res.string.auth_action_continue_as_guest))
                }
            }
        }

        Spacer(modifier = Modifier.height(LexisSpacing.padding_16))
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenPreview() {
    LexisTheme {
        AuthScreenContent(
            state = AuthContract(),
            onEvent = {},
            onGuestContinue = {},
        )
    }
}
