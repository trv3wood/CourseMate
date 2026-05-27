package com.example.coursemate.ui.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.coursemate.CourseMateApplication
import com.example.coursemate.ui.course.CourseListRoute
import com.example.coursemate.ui.discussion.DiscussionRoute
import com.example.coursemate.ui.homework.HomeworkRoute
import com.example.coursemate.ui.login.LoginRegisterScreen
import com.example.coursemate.ui.profile.ProfileRoute
import com.example.coursemate.utils.canViewHomeworkSubmissions
import com.example.coursemate.viewmodel.AuthViewModel
import com.example.coursemate.viewmodel.CourseViewModel
import com.example.coursemate.viewmodel.DiscussionViewModel
import com.example.coursemate.viewmodel.HomeworkViewModel
import com.example.coursemate.viewmodel.NotificationViewModel

private enum class MainDestination(
    val label: String,
    val icon: ImageVector
) {
    Courses("课程", Icons.Outlined.School),
    Homework("作业", Icons.AutoMirrored.Outlined.Assignment),
    Discussion("讨论", Icons.Outlined.Forum),
    Profile("个人", Icons.Outlined.Person)
}

@Composable
fun CourseMateApp(modifier: Modifier = Modifier) {
    val appContainer = (LocalContext.current.applicationContext as CourseMateApplication).appContainer
    val authViewModel: AuthViewModel = viewModel(
        factory = remember(appContainer) {
            viewModelFactory {
                initializer { AuthViewModel(appContainer.authRepository) }
            }
        }
    )
    val authState by authViewModel.uiState.collectAsState()
    var sessionRestoreAttempted by rememberSaveable(authState.token) { mutableStateOf(false) }

    LaunchedEffect(authState.token, authState.currentUser, sessionRestoreAttempted) {
        if (authState.token != null && authState.currentUser == null && !sessionRestoreAttempted) {
            sessionRestoreAttempted = true
            authViewModel.refreshCurrentUser()
        }
    }

    when {
        authState.currentUser != null -> {
            AuthenticatedHomeRoute(
                modifier = modifier,
                authViewModel = authViewModel
            )
        }

        authState.token != null && !sessionRestoreAttempted -> {
            FullScreenLoading(modifier = modifier)
        }

        authState.isLoading && authState.token != null && authState.currentUser == null -> {
            FullScreenLoading(modifier = modifier)
        }

        else -> {
            LoginRegisterScreen(
                uiState = authState,
                onLogin = authViewModel::login,
                onRegister = authViewModel::register,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun AuthenticatedHomeRoute(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val appContainer = (LocalContext.current.applicationContext as CourseMateApplication).appContainer
    val authState by authViewModel.uiState.collectAsState()
    val currentUser = authState.currentUser ?: return
    val courseViewModel: CourseViewModel = viewModel(
        factory = remember(appContainer) {
            viewModelFactory {
                initializer { CourseViewModel(appContainer.courseRepository) }
            }
        }
    )
    val homeworkViewModel: HomeworkViewModel = viewModel(
        factory = remember(appContainer) {
            viewModelFactory {
                initializer { HomeworkViewModel(appContainer.homeworkRepository) }
            }
        }
    )
    val discussionViewModel: DiscussionViewModel = viewModel(
        factory = remember(appContainer) {
            viewModelFactory {
                initializer { DiscussionViewModel(appContainer.postRepository) }
            }
        }
    )
    val notificationViewModel: NotificationViewModel = viewModel(
        factory = remember(appContainer) {
            viewModelFactory {
                initializer { NotificationViewModel(appContainer.notificationRepository) }
            }
        }
    )

    val courseState by courseViewModel.uiState.collectAsState()
    val homeworkState by homeworkViewModel.uiState.collectAsState()
    val discussionState by discussionViewModel.uiState.collectAsState()
    val notificationState by notificationViewModel.uiState.collectAsState()
    var destination by rememberSaveable { mutableStateOf(MainDestination.Courses) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            MainBottomBar(
                selected = destination,
                onSelect = { destination = it }
            )
        }
    ) { innerPadding ->
        when (destination) {
            MainDestination.Courses -> {
                CourseListRoute(
                    currentUser = currentUser,
                    uiState = courseState,
                    homework = homeworkState.homework,
                    posts = discussionState.posts,
                    onRefreshCourses = courseViewModel::refreshCourses,
                    onRefreshHomework = homeworkViewModel::refreshHomework,
                    onRefreshPosts = discussionViewModel::refreshPosts,
                    onCreateCourse = courseViewModel::createCourse,
                    onUpdateCourse = courseViewModel::updateCourse,
                    onDeleteCourse = courseViewModel::deleteCourse,
                    onCreatePost = discussionViewModel::createPost,
                    onNavigateToHomeworkTab = { destination = MainDestination.Homework },
                    onNavigateToDiscussionTab = { destination = MainDestination.Discussion },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            MainDestination.Homework -> {
                HomeworkRoute(
                    currentUser = currentUser,
                    uiState = homeworkState,
                    courses = courseState.courses,
                    onRefresh = homeworkViewModel::refreshHomework,
                    onCreateHomework = homeworkViewModel::createHomework,
                    onUpdateHomework = homeworkViewModel::updateHomework,
                    onDeleteHomework = homeworkViewModel::deleteHomework,
                    onSubmitHomework = { homeworkId, content, attachmentUrl ->
                        homeworkViewModel.submitHomework(
                            homeworkId = homeworkId,
                            content = content,
                            attachmentUrl = attachmentUrl,
                            refreshSubmissionList = currentUser.canViewHomeworkSubmissions()
                        )
                    },
                    onLoadHomeworkSubmissions = homeworkViewModel::loadHomeworkSubmissions,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            MainDestination.Discussion -> {
                DiscussionRoute(
                    currentUser = currentUser,
                    uiState = discussionState,
                    courses = courseState.courses,
                    onRefreshPosts = discussionViewModel::refreshPosts,
                    observePostDetail = discussionViewModel::observePostDetail,
                    onRefreshPostDetail = discussionViewModel::refreshPostDetail,
                    onCreatePost = discussionViewModel::createPost,
                    onUpdatePost = { postId, title, content ->
                        discussionViewModel.updatePost(postId, title = title, content = content)
                    },
                    onDeletePost = discussionViewModel::deletePost,
                    onReplyToPost = discussionViewModel::replyToPost,
                    onUpdateReply = { replyId, content, _ ->
                        discussionViewModel.updateReply(replyId, content)
                    },
                    onDeleteReply = { replyId, _ ->
                        discussionViewModel.deleteReply(replyId)
                    },
                    onAcceptReply = { replyId, postId ->
                        discussionViewModel.acceptReply(replyId, postId)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            MainDestination.Profile -> {
                ProfileRoute(
                    currentUser = currentUser,
                    courseCount = courseState.courses.size,
                    homeworkCount = homeworkState.homework.size,
                    discussionCount = discussionState.posts.size,
                    notificationUiState = notificationState,
                    onRefreshNotifications = notificationViewModel::refreshNotifications,
                    onCreateNotification = notificationViewModel::createNotification,
                    onLogout = authViewModel::logout,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainBottomBar(
    selected: MainDestination,
    onSelect: (MainDestination) -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            MainDestination.entries.forEach { destination ->
                val isSelected = destination == selected
                Surface(
                    onClick = { onSelect(destination) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(999.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .padding(bottom = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                                tint = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        Text(
                            text = destination.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FullScreenLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "正在连接 CourseMate",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
