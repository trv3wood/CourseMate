package com.example.coursemate.ui.homework

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.coursemate.model.Course
import com.example.coursemate.model.Homework
import com.example.coursemate.model.HomeworkSubmission
import com.example.coursemate.model.User
import com.example.coursemate.ui.common.PullRefreshContainer
import com.example.coursemate.utils.canManageHomework
import com.example.coursemate.utils.canSubmitHomework
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

internal enum class HomeworkBucket {
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
    val pendingCount = remember(uiState.homework, latestSubmissionByHomeworkId) {
        uiState.homework.count {
            classifyHomework(it, latestSubmissionByHomeworkId[it.id]) == HomeworkBucket.Pending
        }
    }
    val submittedCount = remember(uiState.homework, latestSubmissionByHomeworkId) {
        uiState.homework.count {
            classifyHomework(it, latestSubmissionByHomeworkId[it.id]) == HomeworkBucket.Submitted
        }
    }
    val expiredCount = remember(uiState.homework, latestSubmissionByHomeworkId) {
        uiState.homework.count {
            classifyHomework(it, latestSubmissionByHomeworkId[it.id]) == HomeworkBucket.Expired
        }
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
        PullRefreshContainer(
            isRefreshing = uiState.isLoading,
            onRefresh = onRefresh,
            modifier = Modifier.padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HomeworkFilter.entries.forEach { filter ->
                            FilterPill(
                                text = when (filter) {
                                    HomeworkFilter.Pending -> "${filter.label} $pendingCount"
                                    HomeworkFilter.Submitted -> "${filter.label} $submittedCount"
                                    HomeworkFilter.Expired -> "${filter.label} $expiredCount"
                                    HomeworkFilter.All -> "${filter.label} ${uiState.homework.size}"
                                },
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
}

internal fun classifyHomework(
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
