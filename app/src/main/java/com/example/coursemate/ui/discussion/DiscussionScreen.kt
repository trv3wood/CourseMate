package com.example.coursemate.ui.discussion

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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.example.coursemate.model.Post
import com.example.coursemate.model.PostDetail
import com.example.coursemate.model.Reply
import com.example.coursemate.model.User
import com.example.coursemate.utils.canAcceptReply
import com.example.coursemate.utils.canManagePost
import com.example.coursemate.utils.canManageReply
import com.example.coursemate.utils.formatDateTime
import com.example.coursemate.utils.formatRelativeTime
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

@Composable
private fun PostCard(
    post: Post,
    courseName: String,
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
                TopicStatusChip(solved = post.solved)
            }
            Text(
                text = courseName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
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
                Text(
                    text = "作者 #${post.authorId}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PostDetailCard(
    post: Post,
    courseName: String,
    canManage: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
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
                TopicStatusChip(solved = post.solved)
                Text(
                    text = formatDateTime(post.createdAt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = post.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = courseName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (canManage) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("编辑话题")
                    }
                    TextButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("删除话题")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReplyCard(
    reply: Reply,
    canAccept: Boolean,
    canManage: Boolean,
    onAccept: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "用户 #${reply.authorId}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatRelativeTime(reply.createdAt),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (reply.isAccepted) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f),
                        contentColor = MaterialTheme.colorScheme.tertiary
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "已采纳",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            Text(
                text = reply.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (canAccept) {
                TextButton(
                    onClick = onAccept,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MarkEmailRead,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("采纳答案")
                }
            }
            if (canManage) {
                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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

@Composable
private fun ReplyComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "写下你的回复",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("回复内容") },
                minLines = 3
            )
            Button(
                onClick = onSend,
                enabled = value.isNotBlank(),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("发送回复")
            }
        }
    }
}

@Composable
private fun TopicStatusChip(solved: Boolean) {
    val (background, content) = if (solved) {
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f) to MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = background,
        contentColor = content
    ) {
        Text(
            text = if (solved) "已解决" else "待解答",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseFilterChip(
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
private fun CreateTopicDialog(
    courses: List<Course>,
    selectedCourseId: Int?,
    onDismiss: () -> Unit,
    onConfirm: (Int, String, String) -> Unit
) {
    var currentCourseId by rememberSaveable { mutableStateOf(selectedCourseId ?: courses.firstOrNull()?.id) }
    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }
    val canSubmit = currentCourseId != null && title.isNotBlank() && content.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建讨论话题") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (courses.isEmpty()) {
                    Text(
                        text = "当前没有课程，无法创建讨论。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        text = "选择课程",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        courses.forEach { course ->
                            CourseFilterChip(
                                text = course.name,
                                selected = currentCourseId == course.id,
                                onClick = { currentCourseId = course.id }
                            )
                        }
                    }
                }
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
                onClick = { onConfirm(currentCourseId!!, title.trim(), content.trim()) },
                enabled = canSubmit
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

@Composable
private fun EditPostDialog(
    initialTitle: String,
    initialContent: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf(initialTitle) }
    var content by rememberSaveable { mutableStateOf(initialContent) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑话题") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                enabled = title.isNotBlank() && content.isNotBlank()
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
private fun EditReplyDialog(
    initialContent: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var content by rememberSaveable { mutableStateOf(initialContent) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑回复") },
        text = {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("回复内容") },
                minLines = 4
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(content.trim()) },
                enabled = content.isNotBlank()
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
private fun InfoCard(
    title: String,
    message: String,
    error: Boolean = false
) {
    val containerColor = if (error) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (error) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Forum,
                contentDescription = null,
                tint = if (error) contentColor else MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = if (error) 1f else 0.72f)
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
