package com.example.coursemate.ui.homework

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.coursemate.model.Course
import com.example.coursemate.model.Homework
import com.example.coursemate.utils.formatRelativeTime
import com.example.coursemate.utils.formatShortDateTime
import com.example.coursemate.utils.parseDateTime
import com.example.coursemate.viewmodel.HomeworkUiState
import java.time.Duration
import java.time.LocalDateTime

private enum class HomeworkFilter(val label: String) {
    Pending("待处理"),
    Submitted("已提交"),
    Expired("已过期"),
    All("全部")
}

private enum class HomeworkBucket {
    Pending,
    Submitted,
    Expired
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkRoute(
    uiState: HomeworkUiState,
    courses: List<Course>,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by rememberSaveable { mutableStateOf(HomeworkFilter.Pending) }
    val courseNameById = remember(courses) { courses.associateBy({ it.id }, { it.name }) }
    val urgentHomework = remember(uiState.homework) {
        uiState.homework
            .filter { classifyHomework(it) == HomeworkBucket.Pending }
            .filter { item ->
                val deadline = parseDateTime(item.deadline) ?: return@filter false
                Duration.between(LocalDateTime.now(), deadline).toDays() in 0..7
            }
            .sortedBy { parseDateTime(it.deadline) }
    }
    val filteredHomework = remember(uiState.homework, selectedFilter) {
        uiState.homework
            .filter { item ->
                when (selectedFilter) {
                    HomeworkFilter.Pending -> classifyHomework(item) == HomeworkBucket.Pending
                    HomeworkFilter.Submitted -> classifyHomework(item) == HomeworkBucket.Submitted
                    HomeworkFilter.Expired -> classifyHomework(item) == HomeworkBucket.Expired
                    HomeworkFilter.All -> true
                }
            }
            .sortedWith(
                compareBy<Homework> { parseDateTime(it.deadline) ?: LocalDateTime.MAX }
                    .thenByDescending { parseDateTime(it.createdAt) }
            )
    }
    val nonUrgentHomework = remember(filteredHomework, urgentHomework, selectedFilter) {
        if (selectedFilter != HomeworkFilter.Pending) {
            filteredHomework
        } else {
            val urgentIds = urgentHomework.map { it.id }.toSet()
            filteredHomework.filterNot { it.id in urgentIds }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("作业中心")
                        Text(
                            text = "${uiState.homework.size} 项作业",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Outlined.Refresh, contentDescription = "刷新作业")
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HomeworkFilter.entries.forEach { filter ->
                        FilterPill(
                            text = filter.label,
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter }
                        )
                    }
                }
            }
            if (uiState.errorMessage != null) {
                item {
                    MessageCard(
                        title = "作业同步失败",
                        message = uiState.errorMessage
                    )
                }
            }
            if (uiState.isLoading && uiState.homework.isEmpty()) {
                item {
                    LoadingCard(text = "正在同步作业")
                }
            } else if (uiState.homework.isEmpty()) {
                item {
                    EmptyCard(
                        title = "暂无作业",
                        message = "后端还没有布置任何作业。"
                    )
                }
            } else {
                if (selectedFilter == HomeworkFilter.Pending && urgentHomework.isNotEmpty()) {
                    item {
                        SectionTitle(text = "近期截止")
                    }
                    items(urgentHomework, key = { it.id }) { homework ->
                        HomeworkCard(
                            homework = homework,
                            courseName = courseNameById[homework.courseId] ?: "未知课程",
                            urgent = true
                        )
                    }
                    item {
                        SectionTitle(text = "全部待处理")
                    }
                }
                if (nonUrgentHomework.isEmpty()) {
                    item {
                        EmptyCard(
                            title = "当前筛选下没有作业",
                            message = "切换上方状态看看其他作业。"
                        )
                    }
                } else {
                    items(nonUrgentHomework, key = { it.id }) { homework ->
                        HomeworkCard(
                            homework = homework,
                            courseName = courseNameById[homework.courseId] ?: "未知课程"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeworkCard(
    homework: Homework,
    courseName: String,
    urgent: Boolean = false
) {
    val bucket = classifyHomework(homework)
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            if (urgent) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(112.dp)
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
private fun FilterPill(
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
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun MessageCard(
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
private fun EmptyCard(
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
                imageVector = Icons.Outlined.Assignment,
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
private fun LoadingCard(text: String) {
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

private fun classifyHomework(homework: Homework): HomeworkBucket {
    if (homework.status.equals("submitted", ignoreCase = true)) {
        return HomeworkBucket.Submitted
    }
    val deadline = parseDateTime(homework.deadline)
    return if (deadline != null && deadline.isBefore(LocalDateTime.now())) {
        HomeworkBucket.Expired
    } else {
        HomeworkBucket.Pending
    }
}
