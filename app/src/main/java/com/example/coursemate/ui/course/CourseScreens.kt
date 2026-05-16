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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coursemate.CourseMateApplication
import com.example.coursemate.model.Course
import com.example.coursemate.ui.theme.CourseMateTheme
import com.example.coursemate.viewmodel.CourseUiState
import com.example.coursemate.viewmodel.CourseViewModel
import com.example.coursemate.viewmodel.CourseViewModelFactory

private data class CourseCardUiModel(
    val id: Int,
    val code: String,
    val title: String,
    val teacher: String,
    val term: String,
    val description: String,
    val tag: String,
    val progress: Float,
    val accent: CourseAccent
)

private enum class CourseAccent {
    Blue,
    Indigo,
    Teal
}

@Composable
fun CourseListRoute(
    modifier: Modifier = Modifier
) {
    val appContainer = (LocalContext.current.applicationContext as CourseMateApplication).appContainer
    val courseViewModel: CourseViewModel = viewModel(
        factory = CourseViewModelFactory(appContainer.courseRepository)
    )
    val uiState by courseViewModel.uiState.collectAsState()
    val courses = remember(uiState.courses) { uiState.courses.toCourseCards() }
    var selectedCourseId by rememberSaveable { mutableIntStateOf(NO_SELECTED_COURSE) }

    val selectedCourse = courses.firstOrNull { it.id == selectedCourseId }
    if (selectedCourse == null) {
        CourseListScreen(
            uiState = uiState,
            courses = courses,
            onCourseClick = { selectedCourseId = it.id },
            onRefresh = courseViewModel::refreshCourses,
            modifier = modifier
        )
    } else {
        CourseDetailScreen(
            course = selectedCourse,
            onBack = { selectedCourseId = NO_SELECTED_COURSE },
            modifier = modifier
        )
    }
}

@Composable
private fun CourseListScreen(
    uiState: CourseUiState,
    courses: List<CourseCardUiModel>,
    onCourseClick: (CourseCardUiModel) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                shape = RoundedCornerShape(18.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "新增课程")
            }
        },
        bottomBar = {
            CourseBottomBar(selected = "课程")
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CourseListHeader(
                    isLoading = uiState.isLoading,
                    errorMessage = uiState.errorMessage,
                    onRefresh = onRefresh
                )
            }
            if (courses.isNotEmpty()) {
                item {
                    FeaturedCourseCard(
                        course = courses.first(),
                        onClick = { onCourseClick(courses.first()) }
                    )
                }
            }
            items(courses.drop(1), key = { it.id }) { course ->
                CompactCourseCard(
                    course = course,
                    onClick = { onCourseClick(course) }
                )
            }
        }
    }
}

@Composable
private fun CourseListHeader(
    isLoading: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "我的课程",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = onRefresh) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Outlined.Search, contentDescription = "搜索课程")
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilterChipLike(text = "本学期", selected = true)
            FilterChipLike(text = "已结课", selected = false)
            FilterChipLike(text = "收藏", selected = false)
        }
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun FilterChipLike(
    text: String,
    selected: Boolean
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun FeaturedCourseCard(
    course: CourseCardUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            CourseVisual(
                course = course,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${course.teacher} · ${course.term}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                CourseTag(text = course.tag)
            }
            Spacer(modifier = Modifier.height(16.dp))
            CourseProgress(progress = course.progress, accent = course.accent)
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("进入课程")
            }
        }
    }
}

@Composable
private fun CompactCourseCard(
    course: CourseCardUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CourseVisual(
                course = course,
                modifier = Modifier.size(96.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${course.teacher} · ${course.term}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                CourseProgress(progress = course.progress, accent = course.accent, compact = true)
            }
        }
    }
}

@Composable
private fun CourseVisual(
    course: CourseCardUiModel,
    modifier: Modifier = Modifier
) {
    val colors = when (course.accent) {
        CourseAccent.Blue -> listOf(Color(0xFFD4E3FF), Color(0xFF7FB6F0))
        CourseAccent.Indigo -> listOf(Color(0xFFDEE0FF), Color(0xFF96A5FF))
        CourseAccent.Teal -> listOf(Color(0xFF8EF4E9), Color(0xFF3AAFA7))
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(colors)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.School,
            contentDescription = null,
            modifier = Modifier.size(42.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = course.code,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.88f))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun CourseTag(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CourseProgress(
    progress: Float,
    accent: CourseAccent,
    compact: Boolean = false
) {
    val color = when (accent) {
        CourseAccent.Blue -> MaterialTheme.colorScheme.primary
        CourseAccent.Indigo -> MaterialTheme.colorScheme.secondary
        CourseAccent.Teal -> MaterialTheme.colorScheme.tertiary
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (!compact) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "学习进度",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
            }
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compact) 6.dp else 8.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseDetailScreen(
    course: CourseCardUiModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CourseMate",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回课程列表")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.Search, contentDescription = "搜索课程内容")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Outlined.Forum, contentDescription = "新讨论")
            }
        },
        bottomBar = {
            CourseBottomBar(selected = "课程")
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
                CourseHero(course = course)
            }
            item {
                DetailTabs()
            }
            item {
                AnnouncementCard()
            }
            item {
                UpdateCard(
                    icon = Icons.Outlined.Description,
                    title = "新课件上传：${course.title} 课程资料",
                    body = "本周资料已更新，可在课件模块查看。",
                    action = "查看课件"
                )
            }
            item {
                UpdateCard(
                    icon = Icons.AutoMirrored.Outlined.Assignment,
                    title = "作业提醒",
                    body = "请关注课程作业的截止时间，按时完成提交。",
                    action = "查看作业"
                )
            }
            item {
                SyllabusCard()
            }
        }
    }
}

@Composable
private fun CourseHero(course: CourseCardUiModel) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            CourseVisual(
                course = course,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CourseTag(text = course.code)
                CourseTag(text = course.tag)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = course.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = course.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "${course.teacher} · ${course.term}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            CourseProgress(progress = course.progress, accent = course.accent)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("进入讨论")
                }
                TextButton(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("查看进度")
                }
            }
        }
    }
}

@Composable
private fun DetailTabs() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        listOf("动态", "课件", "作业", "成员").forEachIndexed { index, text ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (index == 0) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (index == 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (index == 0) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                )
            }
        }
    }
}

@Composable
private fun AnnouncementCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
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
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Campaign,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "课程重要公告",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "本周课程安排已更新，请同学们课前查看资料并完成预习。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "2小时前 · 教师发布",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UpdateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
    action: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = action,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SyllabusCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "教学大纲",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            SyllabusRow(done = true, title = "第1周：课程基础", subtitle = "已完成")
            SyllabusRow(done = true, title = "第2周：核心概念", subtitle = "进行中")
            SyllabusRow(done = false, title = "第3周：综合实践", subtitle = "尚未开始")
        }
    }
}

@Composable
private fun SyllabusRow(
    done: Boolean,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.padding(bottom = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = if (done) Icons.Outlined.CheckCircle else Icons.Outlined.School,
            contentDescription = null,
            tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CourseBottomBar(selected: String) {
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
            BottomItem(icon = Icons.Outlined.Home, label = "首页", selected = selected == "首页")
            BottomItem(icon = Icons.Outlined.School, label = "课程", selected = selected == "课程")
            BottomItem(icon = Icons.Outlined.Forum, label = "讨论", selected = selected == "讨论")
            BottomItem(icon = Icons.Outlined.Person, label = "个人", selected = selected == "个人")
        }
    }
}

@Composable
private fun BottomItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

private fun List<Course>.toCourseCards(): List<CourseCardUiModel> {
    if (isEmpty()) {
        return sampleCourses
    }
    val accents = listOf(CourseAccent.Blue, CourseAccent.Indigo, CourseAccent.Teal)
    val progress = listOf(0.75f, 0.45f, 0.9f, 0.15f)
    return mapIndexed { index, course ->
        CourseCardUiModel(
            id = course.id,
            code = "C${course.id.toString().padStart(3, '0')}",
            title = course.name,
            teacher = course.teacherName,
            term = "本学期",
            description = course.description ?: "课程资料、作业和讨论将在这里集中呈现。",
            tag = if (index % 2 == 0) "必修" else "选修",
            progress = progress[index % progress.size],
            accent = accents[index % accents.size]
        )
    }
}

private val sampleCourses = listOf(
    CourseCardUiModel(
        id = -1,
        code = "CS301",
        title = "计算机网络",
        teacher = "王教授",
        term = "2026春季学期",
        description = "围绕网络体系结构、路由协议和应用层协议展开，配合实验理解真实网络系统。",
        tag = "必修",
        progress = 0.75f,
        accent = CourseAccent.Blue
    ),
    CourseCardUiModel(
        id = -2,
        code = "MA202",
        title = "高等数学 (II)",
        teacher = "张老师",
        term = "2026春季学期",
        description = "覆盖多元函数、级数与常微分方程，为后续专业课程建立数学基础。",
        tag = "基础课",
        progress = 0.45f,
        accent = CourseAccent.Indigo
    ),
    CourseCardUiModel(
        id = -3,
        code = "SE101",
        title = "软件工程导论",
        teacher = "李教授",
        term = "2026春季学期",
        description = "学习软件生命周期、需求分析、协作开发与工程实践。",
        tag = "项目课",
        progress = 0.9f,
        accent = CourseAccent.Teal
    ),
    CourseCardUiModel(
        id = -4,
        code = "AI100",
        title = "人工智能基础",
        teacher = "陈教授",
        term = "2026春季学期",
        description = "介绍搜索、机器学习基础和智能系统应用。",
        tag = "选修",
        progress = 0.15f,
        accent = CourseAccent.Blue
    )
)

private const val NO_SELECTED_COURSE = Int.MIN_VALUE

@Preview(showBackground = true, widthDp = 390, heightDp = 884)
@Composable
private fun CourseListScreenPreview() {
    CourseMateTheme {
        CourseListScreen(
            uiState = CourseUiState(),
            courses = sampleCourses,
            onCourseClick = {},
            onRefresh = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 884)
@Composable
private fun CourseDetailScreenPreview() {
    CourseMateTheme {
        CourseDetailScreen(
            course = sampleCourses.first(),
            onBack = {}
        )
    }
}
