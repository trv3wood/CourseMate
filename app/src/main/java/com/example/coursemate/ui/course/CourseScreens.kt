package com.example.coursemate.ui.course

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.coursemate.model.Course
import com.example.coursemate.model.Homework
import com.example.coursemate.model.Post
import com.example.coursemate.model.User
import com.example.coursemate.utils.parseDateTime
import com.example.coursemate.viewmodel.CourseUiState
import java.time.LocalDateTime

internal enum class CourseAccent {
    Blue,
    Indigo,
    Teal
}

internal data class CourseSummaryUiModel(
    val course: Course,
    val accent: CourseAccent,
    val homeworkCount: Int,
    val openHomeworkCount: Int,
    val discussionCount: Int,
    val solvedDiscussionCount: Int,
    val nextDeadline: String?,
    val latestDiscussionTitle: String?
)

@Composable
fun CourseListRoute(
    currentUser: User,
    uiState: CourseUiState,
    homework: List<Homework>,
    posts: List<Post>,
    onRefreshCourses: () -> Unit,
    onRefreshHomework: () -> Unit,
    onRefreshPosts: () -> Unit,
    onCreateCourse: (String, String?, String) -> Unit,
    onUpdateCourse: (Int, String?, String?, String?) -> Unit,
    onDeleteCourse: (Int) -> Unit,
    onCreatePost: (Int, String, String) -> Unit,
    onNavigateToHomeworkTab: () -> Unit,
    onNavigateToDiscussionTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summaries = remember(uiState.courses, homework, posts) {
        uiState.courses
            .sortedByDescending { parseDateTime(it.createdAt) }
            .mapIndexed { index, course ->
                course.toSummary(
                    accent = CourseAccent.entries[index % CourseAccent.entries.size],
                    homework = homework.filter { item -> item.courseId == course.id },
                    posts = posts.filter { item -> item.courseId == course.id }
                )
            }
    }
    var selectedCourseId by rememberSaveable { mutableStateOf<Int?>(null) }
    val selectedCourse = remember(selectedCourseId, summaries) {
        summaries.firstOrNull { it.course.id == selectedCourseId }
    }

    if (selectedCourse == null) {
        CourseListScreen(
            currentUser = currentUser,
            uiState = uiState,
            courses = summaries,
            onCourseClick = { selectedCourseId = it.course.id },
            onRefresh = {
                onRefreshCourses()
                onRefreshHomework()
                onRefreshPosts()
            },
            onCreateCourse = onCreateCourse,
            modifier = modifier
        )
    } else {
        CourseDetailScreen(
            course = selectedCourse,
            currentUser = currentUser,
            homework = homework.filter { it.courseId == selectedCourse.course.id },
            posts = posts.filter { it.courseId == selectedCourse.course.id },
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage,
            onBack = { selectedCourseId = null },
            onRefresh = {
                onRefreshCourses()
                onRefreshHomework()
                onRefreshPosts()
            },
            onUpdateCourse = { name, description, teacherName ->
                onUpdateCourse(selectedCourse.course.id, name, description, teacherName)
            },
            onDeleteCourse = {
                onDeleteCourse(selectedCourse.course.id)
                selectedCourseId = null
            },
            onCreatePost = { title, content ->
                onCreatePost(selectedCourse.course.id, title, content)
            },
            onNavigateToHomeworkTab = onNavigateToHomeworkTab,
            onNavigateToDiscussionTab = onNavigateToDiscussionTab,
            modifier = modifier
        )
    }
}

internal fun Course.toSummary(
    accent: CourseAccent,
    homework: List<Homework>,
    posts: List<Post>
): CourseSummaryUiModel {
    val nextDeadline = homework
        .mapNotNull { item -> item.deadline }
        .minByOrNull { value -> parseDateTime(value) ?: LocalDateTime.MAX }
    val latestPost = posts.maxByOrNull { post -> parseDateTime(post.createdAt) ?: LocalDateTime.MIN }

    return CourseSummaryUiModel(
        course = this,
        accent = accent,
        homeworkCount = homework.size,
        openHomeworkCount = homework.count { !it.status.equals("submitted", ignoreCase = true) },
        discussionCount = posts.size,
        solvedDiscussionCount = posts.count { it.solved },
        nextDeadline = nextDeadline,
        latestDiscussionTitle = latestPost?.title
    )
}
