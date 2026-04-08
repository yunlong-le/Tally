package com.tally.app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tally.app.R
import com.tally.app.data.model.ChatMessage
import com.tally.app.data.model.MessageRole
import com.tally.app.data.model.MessageStatus
import com.tally.app.ui.theme.TallyBorder
import com.tally.app.ui.theme.TallyCardBackground
import com.tally.app.ui.theme.TallyDarkBackground
import com.tally.app.ui.theme.TallyGreen
import com.tally.app.ui.theme.TallyTextPrimary
import com.tally.app.ui.theme.TallyTextSecondary

@Composable
fun ChatScreen(vm: ChatViewModel = viewModel()) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // 新消息时自动滚到底部
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
        ) {
            // 顶部标题栏
            TopBar(
                onNewChat = vm::createNewChat,
                onToggleHistory = vm::toggleHistory,
                showHistory = uiState.showHistory
            )

            // 消息列表
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    MessageBubble(message)
                }
            }

            // 输入栏
            InputBar(
                text = uiState.inputText,
                isLoading = uiState.isLoading,
                onTextChange = vm::onInputChange,
                onSend = vm::sendMessage,
            )
        }

        // 历史侧边栏（类似 ChatGPT App）
        if (uiState.showHistory) {
            HistorySidebar(
                sessions = uiState.sessions,
                currentSessionId = uiState.currentSessionId,
                onLoadSession = vm::loadSession,
                onDeleteSession = vm::deleteSession,
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
            .height(48.dp) // 固定紧凑高度
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 历史按钮
        IconButton(
            onClick = onToggleHistory,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "历史对话",
                tint = if (showHistory) TallyGreen else TallyTextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }

        Text(
            text = "Tally 对位",
            color = TallyTextPrimary,
            fontSize = 16.sp,
        )

        // 新对话按钮
        IconButton(
            onClick = onNewChat,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "新对话",
                tint = TallyGreen,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * Markdown 渲染数据类
 */
private sealed class MarkdownElement {
    data class Text(val content: AnnotatedString) : MarkdownElement()
    data class CodeBlock(val language: String, val code: String) : MarkdownElement()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownElement()
}

/**
 * Markdown 解析器
 * 支持：粗体、斜体、代码、删除线、标题、代码块、表格
 */
private fun parseMarkdownElements(text: String): List<MarkdownElement> {
    val elements = mutableListOf<MarkdownElement>()
    var remaining = text

    while (remaining.isNotEmpty()) {
        when {
            // 代码块 ```
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
            // 表格（简化检测：包含 | 的行）
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

/**
 * 行内 Markdown 解析
 */
private fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                // 行内代码 `
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
                // 粗体 **
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
                // 斜体 * (但不是 **)
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
                // 删除线 ~~
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
                // 标题 #
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

/**
 * 渲染表格（参考 Gemini 样式，带边框线）
 */
@Composable
private fun MarkdownTable(headers: List<String>, rows: List<List<String>>) {
    val columnCount = headers.size
    val allRows = listOf(headers) + rows

    // 计算每列最大宽度（基于字符数）
    val columnWidths = (0 until columnCount).map { colIndex ->
        allRows.maxOf { row ->
            row.getOrElse(colIndex) { "" }.length
        }.coerceAtLeast(3) // 最小宽度 3
    }

    // 表格外边框
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF444444), RoundedCornerShape(8.dp))
    ) {
        allRows.forEachIndexed { rowIndex, row ->
            // 行背景色 - 表头更深
            val bgColor = when {
                rowIndex == 0 -> Color(0xFF3A3A3A) // 表头深色背景
                rowIndex % 2 == 1 -> Color(0xFF2A2A2A) // 奇数行
                else -> Color(0xFF252525) // 偶数行交替色
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .then(
                        if (rowIndex == 0) {
                            Modifier.clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        } else if (rowIndex == allRows.size - 1) {
                            Modifier.clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                        } else Modifier
                    ),
                horizontalArrangement = Arrangement.Start
            ) {
                row.forEachIndexed { colIndex, cell ->
                    val width = columnWidths.getOrElse(colIndex) { 10 }
                    // 右填充空格以对齐
                    val displayText = cell.padEnd(width).take(width)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = 0.5.dp,
                                color = Color(0xFF444444),
                                shape = if (colIndex == 0 && rowIndex == 0) {
                                    RoundedCornerShape(topStart = 8.dp)
                                } else if (colIndex == row.size - 1 && rowIndex == 0) {
                                    RoundedCornerShape(topEnd = 8.dp)
                                } else RectangleShape
                            )
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

            // 表头下方加粗分隔线
            if (rowIndex == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Color(0xFF555555))
                )
            }
        }
    }
}

/**
 * 渲染代码块
 */
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
                // 空 AI 消息 → 显示加载点
                ThinkingIndicator()
            } else {
                val textColor = if (message.status == MessageStatus.ERROR) Color(0xFFFF6B6B)
                    else if (isUser) Color.Black else TallyTextPrimary

                // AI 消息使用增强 Markdown 渲染
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
private fun HistorySidebar(
    sessions: List<com.tally.app.data.model.ChatSession>,
    currentSessionId: String?,
    onLoadSession: (String) -> Unit,
    onDeleteSession: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // 遮罩层（点击关闭）
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss)
    ) {
        // 侧边栏
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(280.dp)
                .background(TallyCardBackground)
                .clickable(enabled = false) { } // 阻止点击穿透
                .padding(top = 48.dp)
        ) {
            Text(
                text = "历史对话",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TallyTextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            if (sessions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无历史对话",
                        color = TallyTextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(sessions, key = { it.id }) { session ->
                        val isSelected = session.id == currentSessionId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) TallyGreen.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { onLoadSession(session.id) }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = session.title,
                                fontSize = 14.sp,
                                color = if (isSelected) TallyGreen else TallyTextPrimary,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )

                            // 删除按钮
                            IconButton(
                                onClick = { onDeleteSession(session.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "删除",
                                    tint = TallyTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
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

@Composable
private fun InputBar(
    text: String,
    isLoading: Boolean,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TallyCardBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("说点什么…", color = TallyTextSecondary, fontSize = 15.sp) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = TallyBorder,
                unfocusedContainerColor = TallyBorder,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = TallyTextPrimary,
                unfocusedTextColor = TallyTextPrimary,
                cursorColor = TallyGreen,
            ),
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Send,
            ),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            maxLines = 4,
            singleLine = false,
        )

        // 发送按钮 / 加载指示
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (text.isNotBlank() && !isLoading) TallyGreen else TallyBorder),
            contentAlignment = Alignment.Center,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = TallyGreen,
                    strokeWidth = 2.dp,
                )
            } else {
                IconButton(
                    onClick = onSend,
                    enabled = text.isNotBlank(),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_send),
                        contentDescription = "发送",
                        tint = if (text.isNotBlank()) Color.Black else TallyTextSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
