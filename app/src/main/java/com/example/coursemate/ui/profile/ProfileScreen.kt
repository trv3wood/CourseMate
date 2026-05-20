package com.example.coursemate.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coursemate.model.User
import com.example.coursemate.utils.formatDateTime
import com.example.coursemate.utils.initialsOf
import com.example.coursemate.viewmodel.NotificationUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileRoute(
    currentUser: User,
    courseCount: Int,
    homeworkCount: Int,
    discussionCount: Int,
    notificationUiState: NotificationUiState,
    onRefreshNotifications: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("个人中心") },
                actions = {
                    IconButton(onClick = onRefreshNotifications) {
                        if (notificationUiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Outlined.Refresh, contentDescription = "刷新通知")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(999.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initialsOf(currentUser.username),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = currentUser.username,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = currentUser.email,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Text(
                                text = currentUser.role.uppercase(),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        label = "课程",
                        value = courseCount.toString(),
                        icon = Icons.Outlined.School,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "作业",
                        value = homeworkCount.toString(),
                        icon = Icons.AutoMirrored.Outlined.Assignment,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "讨论",
                        value = discussionCount.toString(),
                        icon = Icons.Outlined.Forum,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        InfoRow(label = "用户 ID", value = currentUser.id.toString())
                        InfoRow(label = "注册时间", value = formatDateTime(currentUser.createdAt))
                        InfoRow(label = "邮箱", value = currentUser.email)
                    }
                }
            }
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "通知",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = "${notificationUiState.notifications.size} 条",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (notificationUiState.errorMessage != null) {
                            Text(
                                text = notificationUiState.errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else if (notificationUiState.isLoading && notificationUiState.notifications.isEmpty()) {
                            Text(
                                text = "正在同步通知",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else if (notificationUiState.notifications.isEmpty()) {
                            Text(
                                text = "暂无通知",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            notificationUiState.notifications.forEach { notification ->
                                NotificationRow(
                                    title = notification.title,
                                    message = notification.message,
                                    type = notification.type
                                )
                            }
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = null
                    )
                    Text(
                        text = "退出登录",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(
    title: String,
    message: String,
    type: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        text = type,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
