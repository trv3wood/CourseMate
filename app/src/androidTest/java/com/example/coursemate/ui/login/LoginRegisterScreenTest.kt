package com.example.coursemate.ui.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.coursemate.ui.theme.CourseMateTheme
import com.example.coursemate.viewmodel.AuthUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LoginRegisterScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsLoginFormByDefault() {
        setContent()

        composeRule.onNodeWithText("CourseMate").assertIsDisplayed()
        composeRule.onNodeWithText("学号邮箱 / 用户名").assertIsDisplayed()
        composeRule.onNodeWithText("密码").assertIsDisplayed()
        composeRule.onNodeWithTag(SUBMIT_BUTTON).assertIsDisplayed()
    }

    @Test
    fun validatesLoginInputBeforeSubmit() {
        var loginCount = 0
        setContent(onLogin = { _, _ -> loginCount++ })

        composeRule.onNodeWithTag(SUBMIT_BUTTON).performScrollTo().performClick()
        composeRule.onNodeWithText("请输入用户名").assertIsDisplayed()

        composeRule.onNodeWithTag(USERNAME_FIELD).performTextInput("student01")
        composeRule.onNodeWithTag(PASSWORD_FIELD).performTextInput("123")
        composeRule.onNodeWithTag(SUBMIT_BUTTON).performScrollTo().performClick()
        composeRule.onNodeWithText("密码至少需要 6 位").assertIsDisplayed()

        assertEquals(0, loginCount)
    }

    @Test
    fun submitsLoginWithTrimmedUsername() {
        var submittedUsername: String? = null
        var submittedPassword: String? = null
        setContent(
            onLogin = { username, password ->
                submittedUsername = username
                submittedPassword = password
            }
        )

        composeRule.onNodeWithTag(USERNAME_FIELD).performTextInput("  student01  ")
        composeRule.onNodeWithTag(PASSWORD_FIELD).performTextInput("secret1")
        composeRule.onNodeWithTag(SUBMIT_BUTTON).performScrollTo().performClick()

        assertEquals("student01", submittedUsername)
        assertEquals("secret1", submittedPassword)
    }

    @Test
    fun validatesRegisterPasswordConfirmationBeforeSubmit() {
        var registerCount = 0
        setContent(onRegister = { _, _, _, _ -> registerCount++ })

        composeRule.onNodeWithText("创建 CourseMate 账号").performScrollTo().performClick()
        composeRule.onNodeWithTag(USERNAME_FIELD).performTextInput("student01")
        composeRule.onNodeWithTag(EMAIL_FIELD).performTextInput("student01@example.com")
        composeRule.onNodeWithTag(PASSWORD_FIELD).performTextInput("secret1")
        composeRule.onNodeWithTag(CONFIRM_PASSWORD_FIELD).performTextInput("secret2")
        composeRule.onNodeWithTag(SUBMIT_BUTTON).performScrollTo().performClick()

        composeRule.onNodeWithText("两次输入的密码不一致").assertIsDisplayed()
        assertEquals(0, registerCount)
    }

    @Test
    fun submitsRegisterWithStudentRole() {
        var submitted: RegisterSubmit? = null
        setContent(
            onRegister = { username, email, password, role ->
                submitted = RegisterSubmit(username, email, password, role)
            }
        )

        composeRule.onNodeWithText("创建 CourseMate 账号").performScrollTo().performClick()
        composeRule.onNodeWithTag(USERNAME_FIELD).performTextInput("  student01  ")
        composeRule.onNodeWithTag(EMAIL_FIELD).performTextInput("  student01@example.com  ")
        composeRule.onNodeWithTag(PASSWORD_FIELD).performTextInput("secret1")
        composeRule.onNodeWithTag(CONFIRM_PASSWORD_FIELD).performTextInput("secret1")
        composeRule.onNodeWithTag(SUBMIT_BUTTON).performScrollTo().performClick()

        assertEquals(RegisterSubmit("student01", "student01@example.com", "secret1", "student"), submitted)
    }

    @Test
    fun disablesSubmitWhileLoading() {
        setContent(uiState = AuthUiState(isLoading = true))

        composeRule.onNodeWithTag(SUBMIT_BUTTON).performScrollTo().assertIsNotEnabled()
    }

    private fun setContent(
        uiState: AuthUiState = AuthUiState(),
        onLogin: (String, String) -> Unit = { _, _ -> },
        onRegister: (String, String, String, String) -> Unit = { _, _, _, _ -> }
    ) {
        composeRule.setContent {
            CourseMateTheme {
                LoginRegisterScreen(
                    uiState = uiState,
                    onLogin = onLogin,
                    onRegister = onRegister
                )
            }
        }
    }

    private data class RegisterSubmit(
        val username: String,
        val email: String,
        val password: String,
        val role: String
    )

    private companion object {
        const val USERNAME_FIELD = "auth_username_field"
        const val EMAIL_FIELD = "auth_email_field"
        const val PASSWORD_FIELD = "auth_password_field"
        const val CONFIRM_PASSWORD_FIELD = "auth_confirm_password_field"
        const val SUBMIT_BUTTON = "auth_submit_button"
    }
}
