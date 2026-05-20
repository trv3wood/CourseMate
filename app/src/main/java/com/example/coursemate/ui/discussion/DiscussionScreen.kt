package com.example.coursemate.ui.discussion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coursemate.model.Course
import com.example.coursemate.model.PostDetail
import com.example.coursemate.model.User
import com.example.coursemate.utils.canAcceptReply
import com.example.coursemate.utils.canManagePost
import com.example.coursemate.utils.canManageReply
import com.example.coursemate.utils.parseDateTime
import com.example.coursemate.viewmodel.DiscussionUiState
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussionRoute(
    currentUser: User,
    uiState: DiscussionUiState,
    courses: List<Course>,
    onRefreshPosts: () -> Unit,
    observePostDetail: (Int) -> Flow<PostDetail?>,
    onRefreshPostDetail: (Int) -> Unit,
    onCreatePost: (Int, String, String) -> Unit,
    onUpdatePost: (Int, String, String) -> Unit,
    onDeletePost: (Int) -> Unit,
    onReplyToPost: (Int, String) -> Unit,
    onUpdateReply: (Int, String, Int) -> Unit,
    onDeleteReply: (Int, Int) -> Unit,
    onAcceptReply: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val courseNameById = remember(courses) { courses.associateBy({ it.id }, { it.name }) }
    var selectedCourseId by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedPostId by rememberSaveable { mutableStateOf<Int?>(null) }
    var showComposer by rememberSaveable { mutableStateOf(false) }

    if (showComposer) {
        CreateTopicDialog(
            courses = courses,
            selectedCourseId = selectedCourseId ?: courses.firstOrNull()?.id,
            onDismiss = { showComposer = false },
            onConfirm = { courseId, title, content ->
                onCreatePost(courseId, title, content)
                showComposer = false
                selectedCourseId = courseId
            }
        )
    }

    if (selectedPostId == null) {
        val filteredPosts = remember(uiState.posts, selectedCourseId) {
            uiState.posts
                .filter { selectedCourseId == null || it.courseId == selectedCourseId }
                .sortedByDescending { parseDateTime(it.createdAt) }
        }
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("讨论中心")
                            Text(
                                text = "${filteredPosts.size} 条话题",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onRefreshPosts) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Outlined.Refresh, contentDescription = "刷新讨论")
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
                    Icon(Icons.Outlined.Add, contentDescription = "新建话题")
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
                        CourseFilterChip(
                            text = "全部课程",
                            selected = selectedCourseId == null,
                            onClick = { selectedCourseId = null }
                        )
                        courses.forEach { course ->
                            CourseFilterChip(
                                text = course.name,
                                selected = selectedCourseId == course.id,
                                onClick = { selectedCourseId = course.id }
                            )
                        }
                    }
                }
                if (uiState.errorMessage != null) {
                    item {
                        InfoCard(
                            title = "讨论同步失败",
                            message = uiState.errorMessage,
                            error = true
                        )
                    }
                }
                if (uiState.isLoading && uiState.posts.isEmpty()) {
                    item {
                        LoadingCard(text = "正在同步讨论话题")
                    }
                } else if (filteredPosts.isEmpty()) {
                    item {
                        InfoCard(
                            title = "暂无讨论",
                            message = if (courses.isEmpty()) {
                                "还没有课程，暂时无法发起讨论。"
                            } else {
                                "当前筛选下还没有话题，可以新建一个。"
                            }
                        )
                    }
                } else {
                    items(filteredPosts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            courseName = courseNameById[post.courseId] ?: "未知课程",
                            onClick = { selectedPostId = post.id }
                        )
                    }
                }
            }
        }
    } else {
        val detailFlow = remember(selectedPostId) { observePostDetail(selectedPostId!!) }
        val detail by detailFlow.collectAsState(initial = null)

        LaunchedEffect(selectedPostId) {
            selectedPostId?.let(onRefreshPostDetail)
        }

        DiscussionDetailScreen(
            currentUser = currentUser,
            postDetail = detail,
            courseName = detail?.post?.courseId?.let(courseNameById::get) ?: "讨论详情",
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage,
            onBack = { selectedPostId = null },
            onRefresh = { selectedPostId?.let(onRefreshPostDetail) },
            onReply = { content ->
                selectedPostId?.let { onReplyToPost(it, content) }
            },
            onUpdatePost = { title, content ->
                selectedPostId?.let { onUpdatePost(it, title, content) }
            },
            onDeletePost = {
                selectedPostId?.let(onDeletePost)
                selectedPostId = null
            },
            onUpdateReply = { replyId, content ->
                selectedPostId?.let { postId -> onUpdateReply(replyId, content, postId) }
            },
            onDeleteReply = { replyId ->
                selectedPostId?.let { postId -> onDeleteReply(replyId, postId) }
            },
            onAcceptReply = { replyId ->
                selectedPostId?.let { onAcceptReply(replyId, it) }
            },
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiscussionDetailScreen(
    currentUser: User,
    postDetail: PostDetail?,
    courseName: String,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onReply: (String) -> Unit,
    onUpdatePost: (String, String) -> Unit,
    onDeletePost: () -> Unit,
    onUpdateReply: (Int, String) -> Unit,
    onDeleteReply: (Int) -> Unit,
    onAcceptReply: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var replyContent by rememberSaveable(postDetail?.post?.id) { mutableStateOf("") }
    var editingPost by rememberSaveable(postDetail?.post?.id) { mutableStateOf(false) }
    var deletingPost by rememberSaveable(postDetail?.post?.id) { mutableStateOf(false) }
    var editingReplyId by rememberSaveable(postDetail?.post?.id) { mutableStateOf<Int?>(null) }
    var deletingReplyId by rememberSaveable(postDetail?.post?.id) { mutableStateOf<Int?>(null) }

    val editingReply = remember(postDetail, editingReplyId) {
        postDetail?.replies?.firstOrNull { it.id == editingReplyId }
    }
    val deletingReply = remember(postDetail, deletingReplyId) {
        postDetail?.replies?.firstOrNull { it.id == deletingReplyId }
    }

    if (editingPost && postDetail != null) {
        EditPostDialog(
            initialTitle = postDetail.post.title,
            initialContent = postDetail.post.content,
            onDismiss = { editingPost = false },
            onConfirm = { title, content ->
                onUpdatePost(title, content)
                editingPost = false
            }
        )
    }

    if (deletingPost && postDetail != null) {
        ConfirmDeleteDialog(
            title = "删除话题",
            message = "确定删除《${postDetail.post.title}》吗？",
            onDismiss = { deletingPost = false },
            onConfirm = {
                deletingPost = false
                onDeletePost()
            }
        )
    }

    if (editingReply != null) {
        EditReplyDialog(
            initialContent = editingReply.content,
            onDismiss = { editingReplyId = null },
            onConfirm = { content ->
                onUpdateReply(editingReply.id, content)
                editingReplyId = null
            }
        )
    }

    if (deletingReply != null) {
        ConfirmDeleteDialog(
            title = "删除回复",
            message = "确定删除这条回复吗？",
            onDismiss = { deletingReplyId = null },
            onConfirm = {
                onDeleteReply(deletingReply.id)
                deletingReplyId = null
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
                        Text("话题详情")
                        Text(
                            text = courseName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回讨论列表")
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
                            Icon(Icons.Outlined.Refresh, contentDescription = "刷新话题详情")
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
            if (errorMessage != null) {
                item {
                    InfoCard(
                        title = "详情同步失败",
                        message = errorMessage,
                        error = true
                    )
                }
            }
            if (isLoading && postDetail == null) {
                item {
                    LoadingCard(text = "正在加载话题详情")
                }
            } else if (postDetail == null) {
                item {
                    InfoCard(
                        title = "话题不存在",
                        message = "后端暂时没有返回这条话题详情。"
                    )
                }
            } else {
                item {
                    PostDetailCard(
                        post = postDetail.post,
                        courseName = courseName,
                        canManage = currentUser.canManagePost(postDetail.post),
                        onEdit = { editingPost = true },
                        onDelete = { deletingPost = true }
                    )
                }
                item {
                    Text(
                        text = "回复",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (postDetail.replies.isEmpty()) {
                    item {
                        InfoCard(
                            title = "还没有回复",
                            message = "你可以先给这条话题一个回答。"
                        )
                    }
                } else {
                    items(postDetail.replies, key = { it.id }) { reply ->
                        ReplyCard(
                            reply = reply,
                            canAccept = currentUser.canAcceptReply(postDetail.post) &&
                                !reply.isAccepted &&
                                !postDetail.post.solved,
                            canManage = currentUser.canManageReply(reply),
                            onAccept = { onAcceptReply(reply.id) },
                            onEdit = { editingReplyId = reply.id },
                            onDelete = { deletingReplyId = reply.id }
                        )
                    }
                }
                item {
                    ReplyComposer(
                        value = replyContent,
                        onValueChange = { replyContent = it },
                        onSend = {
                            onReply(replyContent.trim())
                            replyContent = ""
                        }
                    )
                }
            }
        }
    }
}
