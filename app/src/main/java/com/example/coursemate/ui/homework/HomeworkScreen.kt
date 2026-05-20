package com.example.coursemate.ui.homework

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.coursemate.model.Course
import com.example.coursemate.model.Homework
import com.example.coursemate.model.HomeworkSubmission
import com.example.coursemate.model.User
import com.example.coursemate.utils.canManageHomework
import com.example.coursemate.utils.canSubmitHomework
import com.example.coursemate.utils.canViewHomeworkSubmissions
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
    currentUser: User,
    uiState: HomeworkUiState,
    courses: List<Course>,
    onRefresh: () -> Unit,
    onCreateHomework: (Int, String, String, String?, String) -> Unit,
    onUpdateHomework: (Int, String?, String?, String?, String?) -> Unit,
    onDeleteHomework: (Int) -> Unit,
    onSubmitHomework: (Int, String, String?) -> Unit,
    onLoadHomeworkSubmissions: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by rememberSaveable { mutableStateOf(HomeworkFilter.Pending) }
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var editingHomeworkId by rememberSaveable { mutableStateOf<Int?>(null) }
    var deletingHomeworkId by rememberSaveable { mutableStateOf<Int?>(null) }
    var submitHomeworkId by rememberSaveable { mutableStateOf<Int?>(null) }
    var viewingHomeworkId by rememberSaveable { mutableStateOf<Int?>(null) }
    var showMySubmissions by rememberSaveable { mutableStateOf(false) }

    val courseNameById = remember(courses) { courses.associateBy({ it.id }, { it.name }) }
    val latestSubmissionByHomeworkId = remember(uiState.mySubmissions) {
        uiState.mySubmissions
            .groupBy { it.homeworkId }
            .mapValues { (_, submissions) ->
                submissions.maxByOrNull { parseDateTime(it.submittedAt) ?: LocalDateTime.MIN }
            }
    }
    val urgentHomework = remember(uiState.homework, latestSubmissionByHomeworkId) {
        uiState.homework
            .filter {
                classifyHomework(it, latestSubmissionByHomeworkId[it.id]) == HomeworkBucket.Pending
            }
            .filter { item ->
                val deadline = parseDateTime(item.deadline) ?: return@filter false
                Duration.between(LocalDateTime.now(), deadline).toDays() in 0..7
            }
            .sortedBy { parseDateTime(it.deadline) }
    }
    val filteredHomework = remember(uiState.homework, selectedFilter, latestSubmissionByHomeworkId) {
        uiState.homework
            .filter { item ->
                when (selectedFilter) {
                    HomeworkFilter.Pending -> {
                        classifyHomework(item, latestSubmissionByHomeworkId[item.id]) == HomeworkBucket.Pending
                    }
                    HomeworkFilter.Submitted -> {
                        classifyHomework(item, latestSubmissionByHomeworkId[item.id]) == HomeworkBucket.Submitted
                    }
                    HomeworkFilter.Expired -> {
                        classifyHomework(item, latestSubmissionByHomeworkId[item.id]) == HomeworkBucket.Expired
                    }
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
    val editingHomework = remember(editingHomeworkId, uiState.homework) {
        uiState.homework.firstOrNull { it.id == editingHomeworkId }
    }
    val deletingHomework = remember(deletingHomeworkId, uiState.homework) {
        uiState.homework.firstOrNull { it.id == deletingHomeworkId }
    }
    val submittingHomework = remember(submitHomeworkId, uiState.homework) {
        uiState.homework.firstOrNull { it.id == submitHomeworkId }
    }
    val viewingHomework = remember(viewingHomeworkId, uiState.homework) {
        uiState.homework.firstOrNull { it.id == viewingHomeworkId }
    }

    if (showCreateDialog) {
        HomeworkEditorDialog(
            courses = courses,
            title = "发布作业",
            onDismiss = { showCreateDialog = false },
            onConfirm = { courseId, title, content, deadline, status ->
                onCreateHomework(courseId, title, content, deadline, status)
                showCreateDialog = false
            }
        )
    }

    if (editingHomework != null) {
        HomeworkEditorDialog(
            courses = courses,
            title = "编辑作业",
            initialHomework = editingHomework,
            onDismiss = { editingHomeworkId = null },
            onConfirm = { _, title, content, deadline, status ->
                onUpdateHomework(editingHomework.id, title, content, deadline, status)
                editingHomeworkId = null
            }
        )
    }

    if (deletingHomework != null) {
        ConfirmDeleteDialog(
            title = "删除作业",
            message = "确定删除《${deletingHomework.title}》吗？这个操作不能撤销。",
            onDismiss = { deletingHomeworkId = null },
            onConfirm = {
                onDeleteHomework(deletingHomework.id)
                deletingHomeworkId = null
            }
        )
    }

    if (submittingHomework != null) {
        val existingSubmission = latestSubmissionByHomeworkId[submittingHomework.id]
        SubmissionEditorDialog(
            homework = submittingHomework,
            existingSubmission = existingSubmission,
            onDismiss = { submitHomeworkId = null },
            onConfirm = { content, attachmentUrl ->
                onSubmitHomework(submittingHomework.id, content, attachmentUrl)
                submitHomeworkId = null
            }
        )
    }

    if (viewingHomework != null) {
        val submissions = uiState.submissionsByHomeworkId[viewingHomework.id].orEmpty()
        SubmissionListDialog(
            title = viewingHomework.title,
            submissions = submissions,
            courseName = courseNameById[viewingHomework.courseId] ?: "未知课程",
            onDismiss = { viewingHomeworkId = null }
        )
    }

    if (showMySubmissions) {
        MySubmissionDialog(
            submissions = uiState.mySubmissions,
            homeworkTitleById = uiState.homework.associateBy({ it.id }, { it.title }),
            onDismiss = { showMySubmissions = false }
        )
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
        },
        floatingActionButton = {
            if (currentUser.canManageHomework()) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    shape = RoundedCornerShape(18.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "发布作业")
                }
            }
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
            if (currentUser.canSubmitHomework()) {
                item {
                    SummaryActionCard(
                        title = "我的提交记录",
                        message = if (uiState.mySubmissions.isEmpty()) {
                            "你还没有提交任何作业。"
                        } else {
                            "已记录 ${uiState.mySubmissions.size} 次作业提交，点击查看详情。"
                        },
                        icon = Icons.Outlined.FolderOpen,
                        onClick = { showMySubmissions = true }
                    )
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
                        message = if (currentUser.canManageHomework()) {
                            "后端还没有布置任何作业，可以点击右下角先发布一项。"
                        } else {
                            "后端还没有布置任何作业。"
                        }
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
                            mySubmission = latestSubmissionByHomeworkId[homework.id],
                            currentUser = currentUser,
                            urgent = true,
                            onSubmit = { submitHomeworkId = homework.id },
                            onViewMySubmission = { submitHomeworkId = homework.id },
                            onEdit = { editingHomeworkId = homework.id },
                            onDelete = { deletingHomeworkId = homework.id },
                            onViewSubmissions = {
                                onLoadHomeworkSubmissions(homework.id)
                                viewingHomeworkId = homework.id
                            }
                        )
                    }
                    item {
                        SectionTitle(text = "全部作业")
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
                            courseName = courseNameById[homework.courseId] ?: "未知课程",
                            mySubmission = latestSubmissionByHomeworkId[homework.id],
                            currentUser = currentUser,
                            onSubmit = { submitHomeworkId = homework.id },
                            onViewMySubmission = { submitHomeworkId = homework.id },
                            onEdit = { editingHomeworkId = homework.id },
                            onDelete = { deletingHomeworkId = homework.id },
                            onViewSubmissions = {
                                onLoadHomeworkSubmissions(homework.id)
                                viewingHomeworkId = homework.id
                            }
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
                                imageVector = Icons.Outlined.Send,
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
private fun SummaryActionCard(
    title: String,
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
private fun HomeworkEditorDialog(
    courses: List<Course>,
    title: String,
    initialHomework: Homework? = null,
    onDismiss: () -> Unit,
    onConfirm: (courseId: Int, title: String, content: String, deadline: String?, status: String) -> Unit
) {
    var selectedCourseId by rememberSaveable { mutableStateOf(initialHomework?.courseId ?: courses.firstOrNull()?.id) }
    var homeworkTitle by rememberSaveable { mutableStateOf(initialHomework?.title.orEmpty()) }
    var content by rememberSaveable { mutableStateOf(initialHomework?.content.orEmpty()) }
    var deadline by rememberSaveable { mutableStateOf(initialHomework?.deadline.orEmpty()) }
    var status by rememberSaveable { mutableStateOf(initialHomework?.status ?: "open") }
    val canSubmit = selectedCourseId != null && homeworkTitle.isNotBlank() && content.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (courses.isEmpty()) {
                    Text(
                        text = "当前没有课程，无法发布作业。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        text = "所属课程",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        courses.forEach { course ->
                            FilterPill(
                                text = course.name,
                                selected = selectedCourseId == course.id,
                                onClick = { selectedCourseId = course.id }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = homeworkTitle,
                    onValueChange = { homeworkTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("标题") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("内容") },
                    minLines = 4
                )
                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("截止时间") },
                    supportingText = { Text("使用 2026-06-01T23:59:00 这种格式，留空则不设置") }
                )
                OutlinedTextField(
                    value = status,
                    onValueChange = { status = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("状态") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        selectedCourseId!!,
                        homeworkTitle.trim(),
                        content.trim(),
                        deadline.trim().ifBlank { null },
                        status.trim().ifBlank { "open" }
                    )
                },
                enabled = canSubmit
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun SubmissionEditorDialog(
    homework: Homework,
    existingSubmission: HomeworkSubmission?,
    onDismiss: () -> Unit,
    onConfirm: (content: String, attachmentUrl: String?) -> Unit
) {
    var content by rememberSaveable { mutableStateOf(existingSubmission?.content.orEmpty()) }
    var attachmentUrl by rememberSaveable { mutableStateOf(existingSubmission?.attachmentUrl.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingSubmission == null) "提交作业" else "查看 / 重新提交") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = homework.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "截止 ${formatShortDateTime(homework.deadline)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (existingSubmission != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        Text(
                            text = "最近一次提交：${formatShortDateTime(existingSubmission.submittedAt)}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("提交内容") },
                    minLines = 4
                )
                OutlinedTextField(
                    value = attachmentUrl,
                    onValueChange = { attachmentUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("附件链接") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(content.trim(), attachmentUrl.trim().ifBlank { null }) },
                enabled = content.isNotBlank()
            ) {
                Text(if (existingSubmission == null) "提交" else "重新提交")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
private fun SubmissionListDialog(
    title: String,
    submissions: List<HomeworkSubmission>,
    courseName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("提交记录") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = courseName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (submissions.isEmpty()) {
                    Text(
                        text = "还没有学生提交这项作业。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    submissions.sortedByDescending { parseDateTime(it.submittedAt) }.forEach { submission ->
                        SubmissionRow(
                            title = "学生 #${submission.studentId}",
                            content = submission.content,
                            footer = "${submission.status} · ${formatShortDateTime(submission.submittedAt)}"
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
private fun MySubmissionDialog(
    submissions: List<HomeworkSubmission>,
    homeworkTitleById: Map<Int, String>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("我的提交") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (submissions.isEmpty()) {
                    Text(
                        text = "暂无提交记录。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    submissions.sortedByDescending { parseDateTime(it.submittedAt) }.forEach { submission ->
                        SubmissionRow(
                            title = homeworkTitleById[submission.homeworkId] ?: "作业 #${submission.homeworkId}",
                            content = submission.content,
                            footer = "${submission.status} · ${formatShortDateTime(submission.submittedAt)}"
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
private fun SubmissionRow(
    title: String,
    content: String,
    footer: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = footer,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("删除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
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

private fun classifyHomework(
    homework: Homework,
    mySubmission: HomeworkSubmission?
): HomeworkBucket {
    if (mySubmission != null) {
        return HomeworkBucket.Submitted
    }
    val deadline = parseDateTime(homework.deadline)
    return if (deadline != null && deadline.isBefore(LocalDateTime.now())) {
        HomeworkBucket.Expired
    } else {
        HomeworkBucket.Pending
    }
}
