package com.example.coursemate.ui.homework

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.coursemate.model.Homework
import com.example.coursemate.model.HomeworkSubmission
import com.example.coursemate.model.User
import com.example.coursemate.utils.canManageHomework
import com.example.coursemate.utils.canSubmitHomework
import com.example.coursemate.utils.canViewHomeworkSubmissions
import com.example.coursemate.utils.formatRelativeTime
import com.example.coursemate.utils.formatShortDateTime

@Composable
internal fun HomeworkCard(
    homework: Homework,
    courseName: String,
    mySubmission: HomeworkSubmission?,
    currentUser: User,
    onSubmit: () -> Unit,
    onViewMySubmission: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewSubmissions: () -> Unit,
    urgent: Boolean = false
) {
    val bucket = classifyHomework(homework, mySubmission)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            if (urgent) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(128.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.error)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = homework.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    HomeworkStatusChip(bucket = bucket)
                }
                Text(
                    text = courseName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = homework.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (urgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "截止 ${formatShortDateTime(homework.deadline)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (urgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = formatRelativeTime(homework.createdAt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (currentUser.canSubmitHomework()) {
                        TextButton(onClick = if (mySubmission == null) onSubmit else onViewMySubmission) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Send,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (mySubmission == null) "提交作业" else "查看提交")
                        }
                    }
                    if (currentUser.canViewHomeworkSubmissions()) {
                        TextButton(onClick = onViewSubmissions) {
                            Text("提交列表")
                        }
                    }
                    if (currentUser.canManageHomework()) {
                        TextButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("编辑")
                        }
                        TextButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("删除")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeworkStatusChip(bucket: HomeworkBucket) {
    val (background, content) = when (bucket) {
        HomeworkBucket.Pending -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        HomeworkBucket.Submitted -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f) to MaterialTheme.colorScheme.tertiary
        HomeworkBucket.Expired -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f) to MaterialTheme.colorScheme.error
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = background,
        contentColor = content
    ) {
        Text(
            text = when (bucket) {
                HomeworkBucket.Pending -> "待处理"
                HomeworkBucket.Submitted -> "已提交"
                HomeworkBucket.Expired -> "已过期"
            },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FilterPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
internal fun SummaryActionCard(
    title: String,
    message: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
internal fun MessageCard(
    title: String,
    message: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
internal fun EmptyCard(
    title: String,
    message: String
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Assignment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun LoadingCard(text: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
