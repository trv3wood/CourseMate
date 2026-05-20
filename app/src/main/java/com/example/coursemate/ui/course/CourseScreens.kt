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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.coursemate.model.Course
import com.example.coursemate.model.Homework
import com.example.coursemate.model.Post
import com.example.coursemate.model.User
import com.example.coursemate.utils.formatDateTime
import com.example.coursemate.utils.formatRelativeTime
import com.example.coursemate.utils.formatShortDateTime
import com.example.coursemate.utils.initialsOf
import com.example.coursemate.utils.parseDateTime
import com.example.coursemate.viewmodel.CourseUiState
import java.time.LocalDateTime

private enum class CourseAccent {
    Blue,
    Indigo,
    Teal
}

private enum class CourseDetailTab(val label: String) {
    Overview("概览"),
    Homework("作业"),
    Discussion("讨论"),
    Info("信息")
}

private data class CourseSummaryUiModel(
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
            onCreatePost = { title, content ->
                onCreatePost(selectedCourse.course.id, title, content)
            },
            onNavigateToHomeworkTab = onNavigateToHomeworkTab,
            onNavigateToDiscussionTab = onNavigateToDiscussionTab,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseListScreen(
    currentUser: User,
    uiState: CourseUiState,
    courses: List<CourseSummaryUiModel>,
    onCourseClick: (CourseSummaryUiModel) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CourseMate",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "课程中心",
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
                            Icon(Icons.Outlined.Refresh, contentDescription = "刷新课程")
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
                WelcomeCard(
                    currentUser = currentUser,
                    courseCount = courses.size,
                    openHomeworkCount = courses.sumOf { it.openHomeworkCount }
                )
            }
            if (uiState.errorMessage != null) {
                item {
                    InlineMessageCard(
                        title = "同步失败",
                        message = uiState.errorMessage
                    )
                }
            }
            if (uiState.isLoading && courses.isEmpty()) {
                item {
                    LoadingPlaceholder(text = "正在加载课程与课程动态")
                }
            } else if (courses.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "还没有课程",
                        message = "后端目前没有返回课程数据，稍后刷新或先在后台创建课程。"
                    )
                }
            } else {
                item {
                    FeaturedCourseCard(
                        course = courses.first(),
                        onClick = { onCourseClick(courses.first()) }
                    )
                }
                items(courses.drop(1), key = { it.course.id }) { course ->
                    CompactCourseCard(
                        course = course,
                        onClick = { onCourseClick(course) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseDetailScreen(
    course: CourseSummaryUiModel,
    currentUser: User,
    homework: List<Homework>,
    posts: List<Post>,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onCreatePost: (String, String) -> Unit,
    onNavigateToHomeworkTab: () -> Unit,
    onNavigateToDiscussionTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable(course.course.id) {
        mutableStateOf(CourseDetailTab.Overview)
    }
    var showComposer by rememberSaveable(course.course.id) { mutableStateOf(false) }

    if (showComposer) {
        CreatePostDialog(
            courseName = course.course.name,
            onDismiss = { showComposer = false },
            onConfirm = { title, content ->
                onCreatePost(title, content)
                showComposer = false
                selectedTab = CourseDetailTab.Discussion
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = course.course.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = course.course.teacherName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回课程列表")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Outlined.Refresh, contentDescription = "刷新课程详情")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showComposer = true },
                shape = RoundedCornerShape(18.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Outlined.AddComment, contentDescription = "新建讨论")
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
                CourseHero(
                    course = course,
                    currentUser = currentUser,
                    onOpenHomework = {
                        selectedTab = CourseDetailTab.Homework
                    },
                    onOpenDiscussion = {
                        selectedTab = CourseDetailTab.Discussion
                    }
                )
            }
            if (errorMessage != null) {
                item {
                    InlineMessageCard(
                        title = "同步失败",
                        message = errorMessage
                    )
                }
            }
            item {
                CourseDetailTabs(
                    selectedTab = selectedTab,
                    onSelect = { selectedTab = it }
                )
            }
            when (selectedTab) {
                CourseDetailTab.Overview -> {
                    if (homework.isNotEmpty()) {
                        item {
                            HomeworkHighlightCard(
                                homework = homework.sortedBy { parseDateTime(it.deadline) }.first(),
                                onOpenHomeworkTab = onNavigateToHomeworkTab
                            )
                        }
                    }
                    if (posts.isNotEmpty()) {
                        item {
                            DiscussionHighlightCard(
                                post = posts.sortedByDescending { parseDateTime(it.createdAt) }.first(),
                                onOpenDiscussionTab = onNavigateToDiscussionTab
                            )
                        }
                    }
                    if (homework.isEmpty() && posts.isEmpty()) {
                        item {
                            EmptyStateCard(
                                title = "这门课还很安静",
                                message = "目前没有作业和讨论，可以用右下角按钮发起第一条讨论。"
                            )
                        }
                    }
                    item {
                        OverviewInfoCard(course = course)
                    }
                }

                CourseDetailTab.Homework -> {
                    if (homework.isEmpty()) {
                        item {
                            EmptyStateCard(
                                title = "暂无作业",
                                message = "这门课还没有布置作业。"
                            )
                        }
                    } else {
                        items(
                            homework.sortedBy { parseDateTime(it.deadline) },
                            key = { it.id }
                        ) { item ->
                            HomeworkSummaryCard(
                                homework = item,
                                onOpenHomeworkTab = onNavigateToHomeworkTab
                            )
                        }
                    }
                }

                CourseDetailTab.Discussion -> {
                    if (posts.isEmpty()) {
                        item {
                            EmptyStateCard(
                                title = "暂无讨论",
                                message = "同学和老师还没有发起话题，你可以先提一个问题。"
                            )
                        }
                    } else {
                        items(
                            posts.sortedByDescending { parseDateTime(it.createdAt) },
                            key = { it.id }
                        ) { item ->
                            DiscussionSummaryCard(
                                post = item,
                                onOpenDiscussionTab = onNavigateToDiscussionTab
                            )
                        }
                    }
                }

                CourseDetailTab.Info -> {
                    item {
                        DetailInfoCard(course = course)
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeCard(
    currentUser: User,
    courseCount: Int,
    openHomeworkCount: Int
) {
    Card(
        shape = RoundedCornerShape(24.dp),
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
private fun FeaturedCourseCard(
    course: CourseSummaryUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    icon = Icons.Outlined.Assignment,
                    text = "${course.homeworkCount} 个作业"
                )
                MetricChip(
                    icon = Icons.Outlined.Forum,
                    text = "${course.discussionCount} 条讨论"
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MetaRow(
                    label = "教师",
                    value = course.course.teacherName
                )
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
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("查看课程")
            }
        }
    }
}

@Composable
private fun CompactCourseCard(
    course: CourseSummaryUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
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
                        icon = Icons.Outlined.Assignment,
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

@Composable
private fun CourseHero(
    course: CourseSummaryUiModel,
    currentUser: User,
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
                    icon = Icons.Outlined.Assignment,
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
private fun CourseDetailTabs(
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
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                )
            }
        }
    }
}

@Composable
private fun HomeworkHighlightCard(
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
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.error)
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
private fun DiscussionHighlightCard(
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
                        imageVector = Icons.Outlined.OpenInNew,
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
private fun OverviewInfoCard(course: CourseSummaryUiModel) {
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
private fun HomeworkSummaryCard(
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
private fun DiscussionSummaryCard(
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
private fun DetailInfoCard(course: CourseSummaryUiModel) {
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

@Composable
private fun CourseVisual(
    courseName: String,
    teacherName: String,
    accent: CourseAccent,
    modifier: Modifier = Modifier
) {
    val colors = when (accent) {
        CourseAccent.Blue -> listOf(Color(0xFFD4E3FF), Color(0xFF78AEE6))
        CourseAccent.Indigo -> listOf(Color(0xFFDEE0FF), Color(0xFF98A6F8))
        CourseAccent.Teal -> listOf(Color(0xFF8EF4E9), Color(0xFF4CB8AE))
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(colors))
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.BottomStart),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f)
            ) {
                Text(
                    text = initialsOf(courseName),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = courseName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = teacherName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f)
            )
        }
        Icon(
            imageVector = Icons.Outlined.School,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(42.dp),
            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f)
        )
    }
}

@Composable
private fun MetaRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private enum class PillTone {
    Info,
    Success,
    Warning
}

@Composable
private fun StatusPill(
    text: String,
    tone: PillTone
) {
    val colors = when (tone) {
        PillTone.Info -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        PillTone.Success -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f) to MaterialTheme.colorScheme.tertiary
        PillTone.Warning -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f) to MaterialTheme.colorScheme.error
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = colors.first,
        contentColor = colors.second
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun InlineMessageCard(
    title: String,
    message: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
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
private fun EmptyStateCard(
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
                imageVector = Icons.Outlined.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
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
private fun LoadingPlaceholder(text: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
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

@Composable
private fun CreatePostDialog(
    courseName: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }
    val isValid = title.isNotBlank() && content.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("发起课程讨论") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "课程：$courseName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
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
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title.trim(), content.trim()) },
                enabled = isValid
            ) {
                Text("发布")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

private fun Course.toSummary(
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
