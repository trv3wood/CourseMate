package com.example.coursemate.ui.common.markdown

internal data class MarkdownDocumentModel(
    val blocks: List<MarkdownBlock>
)

internal sealed interface MarkdownBlock {
    data class Paragraph(val inlines: List<MarkdownInline>) : MarkdownBlock

    data class Heading(
        val level: Int,
        val inlines: List<MarkdownInline>
    ) : MarkdownBlock

    data class Quote(val blocks: List<MarkdownBlock>) : MarkdownBlock

    data class ListBlock(
        val ordered: Boolean,
        val startNumber: Int,
        val items: List<ListItem>
    ) : MarkdownBlock

    data class CodeBlock(
        val code: String,
        val info: String?
    ) : MarkdownBlock

    data object ThematicBreak : MarkdownBlock
}

internal data class ListItem(
    val blocks: List<MarkdownBlock>
)

internal sealed interface MarkdownInline {
    data class Text(val value: String) : MarkdownInline

    data object SoftBreak : MarkdownInline

    data object HardBreak : MarkdownInline

    data class Emphasis(val children: List<MarkdownInline>) : MarkdownInline

    data class Strong(val children: List<MarkdownInline>) : MarkdownInline

    data class Code(val value: String) : MarkdownInline

    data class Link(
        val destination: String,
        val children: List<MarkdownInline>
    ) : MarkdownInline
}
