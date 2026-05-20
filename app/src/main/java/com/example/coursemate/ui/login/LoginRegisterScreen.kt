package com.example.coursemate.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coursemate.CourseMateApplication
import com.example.coursemate.viewmodel.AuthUiState
import com.example.coursemate.viewmodel.AuthViewModel
import com.example.coursemate.viewmodel.AuthViewModelFactory

internal enum class AuthMode {
    Login,
    Register
}

internal enum class RegisterRole(
    val value: String,
    val label: String
) {
    Student("student", "学生"),
    Teacher("teacher", "教师"),
    Admin("admin", "管理员")
}

@Composable
fun LoginRegisterRoute(
    modifier: Modifier = Modifier
) {
    val appContainer = (LocalContext.current.applicationContext as CourseMateApplication).appContainer
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(appContainer.authRepository)
    )
    val uiState by authViewModel.uiState.collectAsState()

    LoginRegisterScreen(
        uiState = uiState,
        onLogin = authViewModel::login,
        onRegister = authViewModel::register,
        modifier = modifier
    )
}

@Composable
fun LoginRegisterScreen(
    uiState: AuthUiState,
    onLogin: (username: String, password: String) -> Unit,
    onRegister: (username: String, email: String, password: String, role: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var mode by rememberSaveable { mutableStateOf(AuthMode.Login) }
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var selectedRole by rememberSaveable { mutableStateOf(RegisterRole.Student) }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

    val isRegister = mode == AuthMode.Register
    val errorMessage = localError ?: uiState.errorMessage
    val successMessage = uiState.currentUser?.let { "欢迎回来，${it.username}" }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(42.dp))
            CourseMateLogo()
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "CourseMate",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "欢迎来到 CourseMate",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            AuthModeTabs(
                selectedMode = mode,
                onModeSelected = {
                    mode = it
                    localError = null
                }
            )
            Spacer(modifier = Modifier.height(18.dp))

            if (isRegister) {
                CourseMateTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        localError = null
                    },
                    label = "用户名",
                    leadingIcon = {
                        Icon(Icons.Outlined.Person, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.testTag("auth_username_field")
                )
                Spacer(modifier = Modifier.height(12.dp))
                CourseMateTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        localError = null
                    },
                    label = "邮箱",
                    leadingIcon = {
                        Icon(Icons.Outlined.Mail, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.testTag("auth_email_field")
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "注册身份",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RegisterRole.entries.forEach { role ->
                        RoleChip(
                            text = role.label,
                            selected = selectedRole == role,
                            onClick = {
                                selectedRole = role
                                localError = null
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                CourseMateTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        localError = null
                    },
                    label = "学号邮箱 / 用户名",
                    leadingIcon = {
                        Icon(Icons.Outlined.Mail, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.testTag("auth_username_field")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            CourseMateTextField(
                value = password,
                onValueChange = {
                    password = it
                    localError = null
                },
                label = "密码",
                leadingIcon = {
                    Icon(Icons.Outlined.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Outlined.VisibilityOff
                            } else {
                                Icons.Outlined.Visibility
                            },
                            contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if (isRegister) ImeAction.Next else ImeAction.Done
                ),
                modifier = Modifier.testTag("auth_password_field")
            )

            if (isRegister) {
                Spacer(modifier = Modifier.height(12.dp))
                CourseMateTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        localError = null
                    },
                    label = "确认密码",
                    leadingIcon = {
                        Icon(Icons.Outlined.Lock, contentDescription = null)
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.testTag("auth_confirm_password_field")
                )
            }

            if (errorMessage != null || successMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage ?: successMessage.orEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (errorMessage != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.tertiary
                    },
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    localError = validateAuthInput(
                        mode = mode,
                        username = username,
                        email = email,
                        password = password,
                        confirmPassword = confirmPassword
                    )
                    if (localError == null) {
                        if (isRegister) {
                            onRegister(username.trim(), email.trim(), password, selectedRole.value)
                        } else {
                            onLogin(username.trim(), password)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("auth_submit_button"),
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = if (isRegister) "注册" else "登录",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = if (isRegister) "已有账号" else "还没有账号",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(5.dp))
            TextButton(
                onClick = {
                    mode = if (isRegister) AuthMode.Login else AuthMode.Register
                    localError = null
                }
            ) {
                Text(text = if (isRegister) "返回登录" else "创建 CourseMate 账号")
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "登录即代表同意 用户协议 和 隐私政策",
                modifier = Modifier.padding(bottom = 18.dp, top = 24.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun validateAuthInput(
    mode: AuthMode,
    username: String,
    email: String,
    password: String,
    confirmPassword: String
): String? {
    if (username.isBlank()) {
        return "请输入用户名"
    }
    if (username.length < 3) {
        return "用户名至少长度为3"
    }
    if (mode == AuthMode.Register && email.isBlank()) {
        return "请输入邮箱"
    }
    if (password.length < 6) {
        return "密码至少需要 6 位"
    }
    if (mode == AuthMode.Register && password != confirmPassword) {
        return "两次输入的密码不一致"
    }
    return null
}
