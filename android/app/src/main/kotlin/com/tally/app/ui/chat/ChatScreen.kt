package com.tally.app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tally.app.data.model.ChatMessage
import com.tally.app.data.model.MessageRole
import com.tally.app.data.model.MessageStatus
import com.tally.app.ui.theme.*

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatScreen(vm: ChatViewModel = viewModel()) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(uiState.messages.size, uiState.messages.lastOrNull()?.content) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(TallyDarkBackground)
                .imePadding() // 键盘弹起时整个 Column 收缩，输入框自动上移
        ) {
            TopBar(
                onNewChat = vm::createNewChat,
                onToggleHistory = vm::toggleHistory,
                showHistory = uiState.showHistory
            )

            Box(modifier = Modifier.weight(1f)) {
                if (uiState.messages.isEmpty()) {
                    EmptyHomeView(
                        onQuickAction = { action ->
                            vm.onInputChange(action)
                            vm.sendMessage()
                        }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.messages, key = { it.id }) { message ->
                            MessageBubble(message)
                        }
                    }
                }
            }

            SimpleInputBar(
                text = uiState.inputText,
                isLoading = uiState.isLoading,
                onTextChange = vm::onInputChange,
                onSend = {
                    keyboardController?.hide()
                    vm.sendMessage()
                },
            )
        }

        if (uiState.showHistory) {
            HistorySidebar(
                sessions = uiState.sessions,
                currentSessionId = uiState.currentSessionId,
                onLoadSession = vm::loadSession,
                onDismiss = vm::toggleHistory
            )
        }
    }
}

@Composable
private fun TopBar(
    onNewChat: () -> Unit,
    onToggleHistory: () -> Unit,
    showHistory: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(48.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        IconButton(
            onClick = onToggleHistory,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "菜单",
                tint = if (showHistory) TallyGreen else TallyTextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = "Tally",
            style = MaterialTheme.typography.titleLarge,
            color = TallyTextPrimary,
        )

        IconButton(
            onClick = onNewChat,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "新对话",
                tint = TallyGreen,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun EmptyHomeView(
    onQuickAction: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "你好",
            style = MaterialTheme.typography.headlineMedium,
            color = TallyTextPrimary,
        )
        Text(
            text = "需要我为你做些什么？",
            style = MaterialTheme.typography.headlineMedium,
            color = TallyTextPrimary,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        QuickActionButton(
            icon = "📅",
            label = "查看日程",
            onClick = { onQuickAction("帮我查看今天的日程安排") }
        )
        Spacer(modifier = Modifier.height(12.dp))
        QuickActionButton(
            icon = "💰",
            label = "记录支出",
            onClick = { onQuickAction("我想记录一笔支出") }
        )
        Spacer(modifier = Modifier.height(12.dp))
        QuickActionButton(
            icon = "📊",
            label = "日程分析",
            onClick = { onQuickAction("分析我这周的日程安排") }
        )
        Spacer(modifier = Modifier.height(12.dp))
        QuickActionButton(
            icon = "💵",
            label = "费用统计",
            onClick = { onQuickAction("统计我这个月的消费情况") }
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(TallySurfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 20.sp)
        Text(
            text = label,
            color = TallyTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SimpleInputBar(
    text: String,
    isLoading: Boolean,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TallyDarkBackground)
            .padding(horizontal = 16.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(TallySurfaceVariant)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 使用 BasicTextField 替代 TextField 以获得更精确的控制
            val keyboardController = LocalSoftwareKeyboardController.current
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    color = TallyTextPrimary,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                ),
                decorationBox = { innerTextField ->
                    Box {
                        if (text.isEmpty()) {
                            Text(
                                text = "问问 Tally",
                                color = TallyTextSecondary,
                                fontSize = 15.sp,
                                lineHeight = 20.sp
                            )
                        }
                        innerTextField()
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send,
                ),
                keyboardActions = KeyboardActions(onSend = {
                    keyboardController?.hide()
                    onSend()
                }),
                singleLine = true,
                cursorBrush = androidx.compose.ui.graphics.SolidColor(TallyGreen),
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = TallyGreen,
                    strokeWidth = 2.dp
                )
            } else if (text.isNotBlank()) {
                IconButton(
                    onClick = onSend,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "发送",
                        tint = TallyGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HistorySidebar(
    sessions: List<com.tally.app.data.model.ChatSession>,
    currentSessionId: String?,
    onLoadSession: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(300.dp)
                .background(TallyDarkBackground)
                // pointerInput 消耗触摸事件，防止穿透到外层遮罩的 clickable(onDismiss)
                // 同时允许子组件（BasicTextField）正常接收事件获取焦点
                .pointerInput(Unit) { detectTapGestures { } }
                .statusBarsPadding()
                .padding(top = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = TallyTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(TallySurfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "搜索",
                    tint = TallyTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                // 搜索输入框 - 使用BasicTextField获得更好控制
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    textStyle = TextStyle(
                        color = TallyTextPrimary,
                        fontSize = 14.sp
                    ),
                    decorationBox = { innerTextField ->
                        Box {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "搜索对话",
                                    color = TallyTextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Search,
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    ),
                    singleLine = true,
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(TallyGreen),
                )

                // 点击搜索区域请求焦点
                LaunchedEffect(Unit) {
                    // 确保搜索框可聚焦
                }
            }

            Text(
                text = "对话",
                color = TallyTextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )

            val filteredSessions = sessions.filter {
                it.title.contains(searchQuery, ignoreCase = true)
            }

            if (filteredSessions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isEmpty()) "暂无历史对话" else "未找到匹配对话",
                        color = TallyTextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(filteredSessions, key = { it.id }) { session ->
                        val isSelected = session.id == currentSessionId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) TallyGreen.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { onLoadSession(session.id) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Create,
                                    contentDescription = null,
                                    tint = if (isSelected) TallyGreen else TallyTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = session.title,
                                    fontSize = 14.sp,
                                    color = TallyTextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                        }
                    }
                }
            }
        }
    }
}

private sealed class MarkdownElement {
    data class Text(val content: AnnotatedString) : MarkdownElement()
    data class CodeBlock(val language: String, val code: String) : MarkdownElement()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownElement()
}

private fun parseMarkdownElements(text: String): List<MarkdownElement> {
    val elements = mutableListOf<MarkdownElement>()
    var remaining = text

    while (remaining.isNotEmpty()) {
        when {
            remaining.startsWith("```") -> {
                val end = remaining.indexOf("```", 3)
                if (end != -1) {
                    val content = remaining.substring(3, end).trim()
                    val firstLine = content.takeWhile { it != '\n' }
                    val language = if (firstLine.isNotBlank() && !firstLine.contains('`')) firstLine else ""
                    val code = if (language.isNotEmpty()) content.drop(firstLine.length).trim() else content
                    elements.add(MarkdownElement.CodeBlock(language, code))
                    remaining = remaining.substring(end + 3)
                } else {
                    elements.add(MarkdownElement.Text(buildAnnotatedString { append("```") }))
                    remaining = remaining.drop(3)
                }
            }
            remaining.contains("|\n") && remaining.contains("|-") -> {
                val tableEnd = remaining.indexOf("\n\n").takeIf { it > 0 } ?: remaining.length
                val tableLines = remaining.substring(0, tableEnd).lines()
                    .filter { it.contains('|') }

                if (tableLines.size >= 2) {
                    val headers = tableLines[0].split('|').map { it.trim() }.filter { it.isNotEmpty() }
                    val rows = tableLines.drop(2).map { line ->
                        line.split('|').map { it.trim() }.filter { it.isNotEmpty() }
                    }.filter { it.isNotEmpty() }
                    elements.add(MarkdownElement.Table(headers, rows))
                    remaining = remaining.substring(tableEnd).trimStart()
                } else {
                    elements.add(MarkdownElement.Text(parseInlineMarkdown(remaining.takeWhile { it != '\n' })))
                    remaining = remaining.dropWhile { it != '\n' }.drop(1)
                }
            }
            else -> {
                val nextBlock = remaining.indexOf("```").takeIf { it > 0 } ?: remaining.length
                val blockText = remaining.substring(0, nextBlock)
                if (blockText.isNotEmpty()) {
                    elements.add(MarkdownElement.Text(parseInlineMarkdown(blockText)))
                }
                remaining = remaining.substring(nextBlock)
            }
        }
    }
    return elements
}

private fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                text[i] == '`' -> {
                    val end = text.indexOf('`', i + 1)
                    if (end != -1 && end > i + 1) {
                        withStyle(SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color(0xFF2D2D2D),
                            color = Color(0xFF4EC9B0)
                        )) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append('`')
                        i++
                    }
                }
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1 && end > i + 2) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append('*')
                        i++
                    }
                }
                text[i] == '*' && (i + 1 >= text.length || text[i + 1] != '*') -> {
                    val end = text.indexOf('*', i + 1)
                    if (end != -1 && end > i + 1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append('*')
                        i++
                    }
                }
                text.startsWith("~~", i) -> {
                    val end = text.indexOf("~~", i + 2)
                    if (end != -1 && end > i + 2) {
                        withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append('~')
                        i++
                    }
                }
                text[i] == '#' && (i == 0 || text[i - 1] == '\n') -> {
                    val end = text.indexOf('\n', i)
                    val line = if (end != -1) text.substring(i, end) else text.substring(i)
                    val level = line.takeWhile { it == '#' }.length.coerceAtMost(6)
                    val title = line.dropWhile { it == '#' }.trim()
                    withStyle(SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = when (level) {
                            1 -> 18.sp
                            2 -> 16.sp
                            else -> 15.sp
                        }
                    )) {
                        append(title)
                    }
                    if (end != -1) {
                        append('\n')
                        i = end + 1
                    } else {
                        i = text.length
                    }
                }
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == MessageRole.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp,
                    )
                )
                .background(if (isUser) TallyGreen else TallyCardBackground)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            if (message.status == MessageStatus.STREAMING && message.content.isEmpty()) {
                ThinkingIndicator()
            } else {
                val textColor = if (message.status == MessageStatus.ERROR) Color(0xFFFF6B6B)
                    else if (isUser) Color.Black else TallyTextPrimary

                if (!isUser && message.status != MessageStatus.ERROR) {
                    val elements = parseMarkdownElements(message.content)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        elements.forEach { element ->
                            when (element) {
                                is MarkdownElement.Text -> {
                                    BasicText(
                                        text = element.content,
                                        style = TextStyle(
                                            color = textColor,
                                            fontSize = 15.sp,
                                            lineHeight = 22.sp,
                                        )
                                    )
                                }
                                is MarkdownElement.CodeBlock -> {
                                    CodeBlock(element.language, element.code)
                                }
                                is MarkdownElement.Table -> {
                                    MarkdownTable(element.headers, element.rows)
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = message.content,
                        color = textColor,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeBlock(language: String, code: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        if (language.isNotEmpty()) {
            Text(
                text = language,
                fontSize = 11.sp,
                color = TallyTextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        Text(
            text = code,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = Color(0xFFD4D4D4),
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun MarkdownTable(headers: List<String>, rows: List<List<String>>) {
    val columnCount = headers.size
    val allRows = listOf(headers) + rows

    val columnWidths = (0 until columnCount).map { colIndex ->
        allRows.maxOf { row ->
            row.getOrElse(colIndex) { "" }.length
        }.coerceAtLeast(3)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF444444), RoundedCornerShape(8.dp))
    ) {
        allRows.forEachIndexed { rowIndex, row ->
            val bgColor = when {
                rowIndex == 0 -> Color(0xFF3A3A3A)
                rowIndex % 2 == 1 -> Color(0xFF2A2A2A)
                else -> Color(0xFF252525)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor),
                horizontalArrangement = Arrangement.Start
            ) {
                row.forEachIndexed { colIndex, cell ->
                    val width = columnWidths.getOrElse(colIndex) { 10 }
                    val displayText = cell.padEnd(width).take(width)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(0.5.dp, Color(0xFF444444))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = displayText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = if (rowIndex == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (rowIndex == 0) Color(0xFFDDDDDD) else TallyTextPrimary,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThinkingIndicator() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(TallyTextSecondary)
            )
        }
    }
}
