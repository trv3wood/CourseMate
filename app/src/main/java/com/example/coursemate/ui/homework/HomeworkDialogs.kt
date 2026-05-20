package com.example.coursemate.ui.homework

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.coursemate.model.Course
import com.example.coursemate.model.Homework
import com.example.coursemate.model.HomeworkSubmission
import com.example.coursemate.utils.formatShortDateTime
import com.example.coursemate.utils.parseDateTime

@Composable
internal fun HomeworkEditorDialog(
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
internal fun SubmissionEditorDialog(
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
internal fun SubmissionListDialog(
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
internal fun MySubmissionDialog(
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
internal fun ConfirmDeleteDialog(
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
