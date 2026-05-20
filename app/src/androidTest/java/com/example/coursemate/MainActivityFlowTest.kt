package com.example.coursemate

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun registerAndNavigateAcrossMainTabs() {
        val suffix = System.currentTimeMillis().toString().takeLast(6)
        val username = "student$suffix"
        val email = "student$suffix@example.com"
        val password = "secret1"

        composeRule.onNodeWithText("创建 CourseMate 账号").performScrollTo().performClick()
        composeRule.onNodeWithTag(USERNAME_FIELD).performTextInput(username)
        composeRule.onNodeWithTag(EMAIL_FIELD).performTextInput(email)
        composeRule.onNodeWithTag(PASSWORD_FIELD).performTextInput(password)
        composeRule.onNodeWithTag(CONFIRM_PASSWORD_FIELD).performTextInput(password)
        composeRule.onNodeWithTag(SUBMIT_BUTTON).performScrollTo().performClick()

        composeRule.waitUntil(timeoutMillis = 20_000) {
            composeRule.onAllNodesWithText("课程中心").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("你好，$username").assertIsDisplayed()

        composeRule.onNodeWithText("作业").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("作业中心").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("讨论").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("讨论中心").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("个人").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("个人中心").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText(email).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private companion object {
        const val USERNAME_FIELD = "auth_username_field"
        const val EMAIL_FIELD = "auth_email_field"
        const val PASSWORD_FIELD = "auth_password_field"
        const val CONFIRM_PASSWORD_FIELD = "auth_confirm_password_field"
        const val SUBMIT_BUTTON = "auth_submit_button"
    }
}
