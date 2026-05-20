package com.example.coursemate.ui.course

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.coursemate.model.Homework
import com.example.coursemate.model.Post
import com.example.coursemate.model.User
import com.example.coursemate.utils.canManageCourses
import com.example.coursemate.utils.formatDateTime
import com.example.coursemate.utils.formatRelativeTime
import com.example.coursemate.utils.formatShortDateTime

@Composable
internal fun CourseHero(
    course: CourseSummaryUiModel,
    currentUser: User,
    onEditCourse: () -> Unit,
    onDeleteCourse: () -> Unit,
    onOpenHomework: () -> Unit,
    onOpenDiscussion: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            CourseVisual(
                courseName = course.course.name,
                teacherName = course.course.teacherName,
                accent = course.accent,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricChip(
                    icon = Icons.Outlined.Person,
                    text = course.course.teacherName
                )
                MetricChip(
                    icon = Icons.Outlined.CalendarMonth,
                    text = formatDateTime(course.course.createdAt)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = course.course.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = course.course.description.orEmpty().ifBlank { "暂无课程简介。" },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip(
                    icon = Icons.AutoMirrored.Outlined.Assignment,
                    text = "${course.homeworkCount} 个作业"
                )
                MetricChip(
                    icon = Icons.Outlined.Forum,
                    text = "${course.discussionCount} 条讨论"
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onOpenDiscussion,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("进入讨论")
                }
                TextButton(
                    onClick = onOpenHomework,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("查看作业")
                }
            }
            if (currentUser.canManageCourses()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onEditCourse) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("编辑课程")
                    }
                    TextButton(onClick = onDeleteCourse) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("删除课程")
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "当前登录：${currentUser.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
internal fun CourseDetailTabs(
    selectedTab: CourseDetailTab,
    onSelect: (CourseDetailTab) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        CourseDetailTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            Column(
                modifier = Modifier.clickable { onSelect(tab) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(3.dp)
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(999.dp)
                        )
                )
            }
        }
    }
}

@Composable
internal fun HomeworkHighlightCard(
    homework: Homework,
    onOpenHomeworkTab: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(96.dp)
                    .background(MaterialTheme.colorScheme.error, RoundedCornerShape(999.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "最近截止作业",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = homework.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = homework.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "截止：${formatShortDateTime(homework.deadline)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                TextButton(
                    onClick = onOpenHomeworkTab,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("前往作业中心")
                }
            }
        }
    }
}

@Composable
internal fun DiscussionHighlightCard(
    post: Post,
    onOpenDiscussionTab: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "最新讨论",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                StatusPill(
                    text = if (post.solved) "已解决" else "讨论中",
                    tone = if (post.solved) PillTone.Success else PillTone.Info
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = post.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatRelativeTime(post.createdAt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onOpenDiscussionTab) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("打开讨论")
                }
            }
        }
    }
}

@Composable
internal fun OverviewInfoCard(course: CourseSummaryUiModel) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "课程概况",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            MetaRow(label = "课程 ID", value = course.course.id.toString())
            MetaRow(label = "创建时间", value = formatDateTime(course.course.createdAt))
            MetaRow(label = "已解决讨论", value = "${course.solvedDiscussionCount} 条")
            MetaRow(label = "未完成作业", value = "${course.openHomeworkCount} 项")
        }
    }
}

@Composable
internal fun HomeworkSummaryCard(
    homework: Homework,
    onOpenHomeworkTab: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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
                StatusPill(
                    text = homework.status,
                    tone = if (homework.status.equals("submitted", ignoreCase = true)) {
                        PillTone.Success
                    } else {
                        PillTone.Warning
                    }
                )
            }
            Text(
                text = homework.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "截止：${formatShortDateTime(homework.deadline)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = onOpenHomeworkTab,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("在作业中心查看")
            }
        }
    }
}

@Composable
internal fun DiscussionSummaryCard(
    post: Post,
    onOpenDiscussionTab: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                StatusPill(
                    text = if (post.solved) "已解决" else "待解答",
                    tone = if (post.solved) PillTone.Success else PillTone.Info
                )
            }
            Text(
                text = post.content,
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
                Text(
                    text = formatRelativeTime(post.createdAt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onOpenDiscussionTab) {
                    Text("进入讨论中心")
                }
            }
        }
    }
}

@Composable
internal fun DetailInfoCard(course: CourseSummaryUiModel) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "课程信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            MetaRow(label = "课程名称", value = course.course.name)
            MetaRow(label = "任课教师", value = course.course.teacherName)
            MetaRow(label = "课程 ID", value = course.course.id.toString())
            MetaRow(label = "创建时间", value = formatDateTime(course.course.createdAt))
            MetaRow(label = "课程简介", value = course.course.description.orEmpty().ifBlank { "暂无" })
        }
    }
}
