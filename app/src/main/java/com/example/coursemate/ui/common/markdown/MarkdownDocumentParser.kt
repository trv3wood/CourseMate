package com.example.coursemate.ui.common.markdown

import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.Emphasis
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.HtmlBlock
import org.commonmark.node.HtmlInline
import org.commonmark.node.Image
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Link
import org.commonmark.node.ListItem as CommonMarkListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text
import org.commonmark.node.ThematicBreak
import org.commonmark.parser.Parser

internal object MarkdownDocumentParser {
    private val parser by lazy(LazyThreadSafetyMode.NONE) {
        Parser.builder()
            .extensions(listOf(AutolinkExtension.create()))
            .build()
    }

    fun parse(markdown: String): MarkdownDocumentModel {
        val document = parser.parse(markdown)
        return MarkdownDocumentModel(parseBlocks(document))
    }
}

internal fun markdownToPlainPreview(markdown: String): String {
    return MarkdownDocumentParser.parse(markdown)
        .blocks
        .joinToString(separator = " ") { it.toPlainText() }
        .replace(Regex("\\s+"), " ")
        .trim()
}

private fun parseBlocks(parent: Node): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    var current = parent.firstChild
    while (current != null) {
        parseBlock(current)?.let(blocks::add)
        current = current.next
    }
    return blocks
}

private fun parseBlock(node: Node): MarkdownBlock? = when (node) {
    is Paragraph -> MarkdownBlock.Paragraph(parseInlines(node))
    is Heading -> MarkdownBlock.Heading(node.level, parseInlines(node))
    is BlockQuote -> MarkdownBlock.Quote(parseBlocks(node))
    is BulletList -> MarkdownBlock.ListBlock(
        ordered = false,
        startNumber = 1,
        items = parseListItems(node)
    )

    is OrderedList -> MarkdownBlock.ListBlock(
        ordered = true,
        startNumber = node.markerStartNumber ?: 1,
        items = parseListItems(node)
    )

    is FencedCodeBlock -> MarkdownBlock.CodeBlock(
        code = node.literal.orEmpty().trimEnd(),
        info = node.info?.trim().orEmpty().ifBlank { null }
    )

    is IndentedCodeBlock -> MarkdownBlock.CodeBlock(
        code = node.literal.orEmpty().trimEnd(),
        info = null
    )

    is ThematicBreak -> MarkdownBlock.ThematicBreak
    is HtmlBlock -> htmlBlockFallback(node)
    else -> fallbackBlock(node)
}

private fun parseListItems(node: Node): List<ListItem> {
    val items = mutableListOf<ListItem>()
    var current = node.firstChild
    while (current != null) {
        if (current is CommonMarkListItem) {
            items += ListItem(parseBlocks(current))
        }
        current = current.next
    }
    return items
}

private fun parseInlines(parent: Node): List<MarkdownInline> {
    val inlines = mutableListOf<MarkdownInline>()
    var current = parent.firstChild
    while (current != null) {
        when (current) {
            is Text -> inlines += MarkdownInline.Text(current.literal.orEmpty())
            is SoftLineBreak -> inlines += MarkdownInline.SoftBreak
            is HardLineBreak -> inlines += MarkdownInline.HardBreak
            is Emphasis -> inlines += MarkdownInline.Emphasis(parseInlines(current))
            is StrongEmphasis -> inlines += MarkdownInline.Strong(parseInlines(current))
            is Code -> inlines += MarkdownInline.Code(current.literal.orEmpty())
            is Link -> inlines += MarkdownInline.Link(
                destination = current.destination.orEmpty(),
                children = parseInlines(current)
            )

            is Image -> inlines += MarkdownInline.Text(
                parseInlines(current).joinToString(separator = "") { it.toPlainText() }
            )

            is HtmlInline -> inlines += MarkdownInline.Text(current.literal.orEmpty())
            else -> {
                if (current.firstChild != null) {
                    inlines += parseInlines(current)
                }
            }
        }
        current = current.next
    }
    return inlines
}

private fun htmlBlockFallback(node: HtmlBlock): MarkdownBlock? {
    val literal = node.literal.orEmpty().trim()
    return if (literal.isBlank()) {
        null
    } else {
        MarkdownBlock.Paragraph(listOf(MarkdownInline.Text(literal)))
    }
}

private fun fallbackBlock(node: Node): MarkdownBlock? {
    val childBlocks = parseBlocks(node)
    if (childBlocks.isNotEmpty()) {
        return if (childBlocks.size == 1) {
            childBlocks.first()
        } else {
            MarkdownBlock.Quote(childBlocks)
        }
    }
    val inlineChildren = parseInlines(node)
    return inlineChildren.takeIf { it.isNotEmpty() }?.let(MarkdownBlock::Paragraph)
}

private fun MarkdownBlock.toPlainText(): String = when (this) {
    is MarkdownBlock.Paragraph -> inlines.joinToString(separator = "") { it.toPlainText() }
    is MarkdownBlock.Heading -> inlines.joinToString(separator = "") { it.toPlainText() }
    is MarkdownBlock.Quote -> blocks.joinToString(separator = " ") { it.toPlainText() }
    is MarkdownBlock.ListBlock -> items.joinToString(separator = " ") { item ->
        item.blocks.joinToString(separator = " ") { it.toPlainText() }
    }

    is MarkdownBlock.CodeBlock -> code
    MarkdownBlock.ThematicBreak -> ""
}

private fun MarkdownInline.toPlainText(): String = when (this) {
    is MarkdownInline.Text -> value
    MarkdownInline.SoftBreak,
    MarkdownInline.HardBreak -> " "

    is MarkdownInline.Emphasis -> children.joinToString(separator = "") { it.toPlainText() }
    is MarkdownInline.Strong -> children.joinToString(separator = "") { it.toPlainText() }
    is MarkdownInline.Code -> value
    is MarkdownInline.Link -> children.joinToString(separator = "") { it.toPlainText() }
}
