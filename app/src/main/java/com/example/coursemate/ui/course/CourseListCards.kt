package com.example.coursemate.ui.course

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.coursemate.model.User
import com.example.coursemate.utils.formatShortDateTime

@Composable
internal fun WelcomeCard(
    currentUser: User,
    courseCount: Int,
    openHomeworkCount: Int
) {
    Card(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "你好，${currentUser.username}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "当前已同步 $courseCount 门课程，还有 $openHomeworkCount 项待处理作业。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip(
                    icon = Icons.Outlined.Person,
                    text = currentUser.role.uppercase()
                )
                MetricChip(
                    icon = Icons.Outlined.Schedule,
                    text = "实时同步"
                )
            }
        }
    }
}

@Composable
internal fun FeaturedCourseCard(
    course: CourseSummaryUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
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
            Text(
                text = course.course.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = course.course.description.orEmpty().ifBlank { "暂无课程简介" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricChip(
                    icon = Icons.AutoMirrored.Outlined.Assignment,
                    text = "${course.homeworkCount} 个作业"
                )
                MetricChip(
                    icon = Icons.Outlined.Forum,
                    text = "${course.discussionCount} 条讨论"
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MetaRow(label = "教师", value = course.course.teacherName)
                MetaRow(
                    label = "最近截止",
                    value = course.nextDeadline?.let(::formatShortDateTime) ?: "暂无"
                )
                MetaRow(
                    label = "最新讨论",
                    value = course.latestDiscussionTitle ?: "还没有讨论"
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text("查看课程")
            }
        }
    }
}

@Composable
internal fun CompactCourseCard(
    course: CourseSummaryUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CourseVisual(
                courseName = course.course.name,
                teacherName = course.course.teacherName,
                accent = course.accent,
                modifier = Modifier.size(92.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = course.course.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = course.course.teacherName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricChip(
                        icon = Icons.AutoMirrored.Outlined.Assignment,
                        text = "${course.homeworkCount}"
                    )
                    MetricChip(
                        icon = Icons.Outlined.Forum,
                        text = "${course.discussionCount}"
                    )
                    MetricChip(
                        icon = Icons.Outlined.Campaign,
                        text = "${course.solvedDiscussionCount} 已解决"
                    )
                }
            }
        }
    }
}
