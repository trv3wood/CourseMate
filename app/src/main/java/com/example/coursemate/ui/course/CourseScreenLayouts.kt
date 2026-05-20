package com.example.coursemate.ui.course

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.coursemate.model.Homework
import com.example.coursemate.model.Post
import com.example.coursemate.model.User
import com.example.coursemate.utils.canManageCourses
import com.example.coursemate.utils.parseDateTime
import com.example.coursemate.viewmodel.CourseUiState

internal enum class CourseDetailTab(val label: String) {
    Overview("概览"),
    Homework("作业"),
    Discussion("讨论"),
    Info("信息")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CourseListScreen(
    currentUser: User,
    uiState: CourseUiState,
    courses: List<CourseSummaryUiModel>,
    onCourseClick: (CourseSummaryUiModel) -> Unit,
    onRefresh: () -> Unit,
    onCreateCourse: (String, String?, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }

    if (showCreateDialog) {
        CourseEditorDialog(
            title = "创建课程",
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, description, teacherName ->
                onCreateCourse(name, description, teacherName)
                showCreateDialog = false
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (currentUser.canManageCourses()) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    shape = RoundedCornerShape(18.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "创建课程")
                }
            }
        },
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
internal fun CourseDetailScreen(
    course: CourseSummaryUiModel,
    currentUser: User,
    homework: List<Homework>,
    posts: List<Post>,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onUpdateCourse: (String?, String?, String?) -> Unit,
    onDeleteCourse: () -> Unit,
    onCreatePost: (String, String) -> Unit,
    onNavigateToHomeworkTab: () -> Unit,
    onNavigateToDiscussionTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable(course.course.id) {
        mutableStateOf(CourseDetailTab.Overview)
    }
    var showComposer by rememberSaveable(course.course.id) { mutableStateOf(false) }
    var showEditCourseDialog by rememberSaveable(course.course.id) { mutableStateOf(false) }
    var showDeleteCourseDialog by rememberSaveable(course.course.id) { mutableStateOf(false) }

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

    if (showEditCourseDialog) {
        CourseEditorDialog(
            title = "编辑课程",
            initialCourse = course.course,
            onDismiss = { showEditCourseDialog = false },
            onConfirm = { name, description, teacherName ->
                onUpdateCourse(name, description, teacherName)
                showEditCourseDialog = false
            }
        )
    }

    if (showDeleteCourseDialog) {
        ConfirmCourseDeleteDialog(
            courseName = course.course.name,
            onDismiss = { showDeleteCourseDialog = false },
            onConfirm = {
                showDeleteCourseDialog = false
                onDeleteCourse()
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
                    onEditCourse = { showEditCourseDialog = true },
                    onDeleteCourse = { showDeleteCourseDialog = true },
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
