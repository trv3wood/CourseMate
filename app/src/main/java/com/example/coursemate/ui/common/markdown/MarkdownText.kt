package com.example.coursemate.ui.common.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp

@Composable
internal fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge
) {
    val document = remember(markdown) { MarkdownDocumentParser.parse(markdown) }
    val uriHandler = LocalUriHandler.current
    val linkStyle = SpanStyle(
        color = MaterialTheme.colorScheme.primary,
        textDecoration = TextDecoration.Underline
    )
    val codeStyle = SpanStyle(
        fontFamily = FontFamily.Monospace,
        background = MaterialTheme.colorScheme.surfaceContainerHighest,
        color = MaterialTheme.colorScheme.onSurface
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        document.blocks.forEach { block ->
            MarkdownBlockView(
                block = block,
                baseStyle = style,
                linkStyle = linkStyle,
                codeStyle = codeStyle,
                onLinkClick = uriHandler::openUri
            )
        }
    }
}

@Composable
private fun MarkdownBlockView(
    block: MarkdownBlock,
    baseStyle: TextStyle,
    linkStyle: SpanStyle,
    codeStyle: SpanStyle,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    when (block) {
        is MarkdownBlock.Paragraph -> InlineMarkdownText(
            inlines = block.inlines,
            style = baseStyle,
            linkStyle = linkStyle,
            codeStyle = codeStyle,
            onLinkClick = onLinkClick,
            modifier = modifier
        )

        is MarkdownBlock.Heading -> InlineMarkdownText(
            inlines = block.inlines,
            style = headingStyle(block.level, baseStyle),
            linkStyle = linkStyle,
            codeStyle = codeStyle,
            onLinkClick = onLinkClick,
            modifier = modifier
        )

        is MarkdownBlock.Quote -> Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .align(Alignment.Top)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(999.dp)
                    )
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                block.blocks.forEach { child ->
                    MarkdownBlockView(
                        block = child,
                        baseStyle = baseStyle,
                        linkStyle = linkStyle,
                        codeStyle = codeStyle,
                        onLinkClick = onLinkClick
                    )
                }
            }
        }

        is MarkdownBlock.ListBlock -> Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            block.items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (block.ordered) "${block.startNumber + index}." else "\u2022",
                        style = baseStyle,
                        fontWeight = FontWeight.SemiBold
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item.blocks.forEach { child ->
                            MarkdownBlockView(
                                block = child,
                                baseStyle = baseStyle,
                                linkStyle = linkStyle,
                                codeStyle = codeStyle,
                                onLinkClick = onLinkClick
                            )
                        }
                    }
                }
            }
        }

        is MarkdownBlock.CodeBlock -> Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                block.info?.let { info ->
                    Text(
                        text = info,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = block.code,
                    style = baseStyle.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        MarkdownBlock.ThematicBreak -> HorizontalDivider()
    }
}

@Composable
private fun InlineMarkdownText(
    inlines: List<MarkdownInline>,
    style: TextStyle,
    linkStyle: SpanStyle,
    codeStyle: SpanStyle,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val annotated = remember(inlines, style, linkStyle, codeStyle) {
        buildAnnotatedString {
            appendInlines(
                inlines = inlines,
                linkStyle = linkStyle,
                codeStyle = codeStyle,
                onLinkClick = onLinkClick
            )
        }
    }

    Text(
        text = annotated,
        modifier = modifier.fillMaxWidth(),
        style = style
    )
}

private fun AnnotatedString.Builder.appendInlines(
    inlines: List<MarkdownInline>,
    linkStyle: SpanStyle,
    codeStyle: SpanStyle,
    onLinkClick: (String) -> Unit
) {
    inlines.forEach { inline ->
        when (inline) {
            is MarkdownInline.Text -> append(inline.value)
            MarkdownInline.SoftBreak -> append(' ')
            MarkdownInline.HardBreak -> append('\n')
            is MarkdownInline.Emphasis -> {
                pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                appendInlines(inline.children, linkStyle, codeStyle, onLinkClick)
                pop()
            }

            is MarkdownInline.Strong -> {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                appendInlines(inline.children, linkStyle, codeStyle, onLinkClick)
                pop()
            }

            is MarkdownInline.Code -> {
                pushStyle(codeStyle)
                append(inline.value)
                pop()
            }

            is MarkdownInline.Link -> {
                withLink(
                    LinkAnnotation.Url(
                        url = inline.destination,
                        styles = TextLinkStyles(style = linkStyle),
                        linkInteractionListener = LinkInteractionListener { link ->
                            onLinkClick((link as LinkAnnotation.Url).url)
                        }
                    )
                ) {
                    appendInlines(inline.children, linkStyle, codeStyle, onLinkClick)
                }
            }
        }
    }
}

@Composable
private fun headingStyle(level: Int, baseStyle: TextStyle): TextStyle {
    return when (level) {
        1 -> MaterialTheme.typography.headlineSmall
        2 -> MaterialTheme.typography.titleLarge
        3 -> MaterialTheme.typography.titleMedium
        else -> baseStyle.copy(fontWeight = FontWeight.SemiBold)
    }.copy(color = baseStyle.color)
}
