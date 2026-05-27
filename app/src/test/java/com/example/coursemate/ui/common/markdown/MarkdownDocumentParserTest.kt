package com.example.coursemate.ui.common.markdown

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownDocumentParserTest {
    @Test
    fun markdownToPlainPreviewStripsFormattingMarkers() {
        val markdown = """
            # 标题

            **加粗** 和 *斜体* 以及 `代码`

            - 第一项
            - 第二项
        """.trimIndent()

        assertEquals("标题 加粗 和 斜体 以及 代码 第一项 第二项", markdownToPlainPreview(markdown))
    }

    @Test
    fun parseBuildsBlocksAndInlineNodes() {
        val markdown = """
            ## Heading

            Paragraph with **bold**, *italic*, `code` and https://example.com

            > Quoted text

            1. First
            2. Second

            ```kotlin
            val answer = 42
            ```
        """.trimIndent()

        val document = MarkdownDocumentParser.parse(markdown)

        assertEquals(5, document.blocks.size)
        assertTrue(document.blocks[0] is MarkdownBlock.Heading)
        assertTrue(document.blocks[1] is MarkdownBlock.Paragraph)
        assertTrue(document.blocks[2] is MarkdownBlock.Quote)
        assertTrue(document.blocks[3] is MarkdownBlock.ListBlock)
        assertTrue(document.blocks[4] is MarkdownBlock.CodeBlock)

        val paragraph = document.blocks[1] as MarkdownBlock.Paragraph
        assertTrue(paragraph.inlines.any { it is MarkdownInline.Strong })
        assertTrue(paragraph.inlines.any { it is MarkdownInline.Emphasis })
        assertTrue(paragraph.inlines.any { it is MarkdownInline.Code })
        assertTrue(paragraph.inlines.any { it is MarkdownInline.Link })

        val listBlock = document.blocks[3] as MarkdownBlock.ListBlock
        assertTrue(listBlock.ordered)
        assertEquals(1, listBlock.startNumber)
        assertEquals(2, listBlock.items.size)
    }

    @Test
    fun unsupportedHtmlAndImageFallbackWithoutCrash() {
        val markdown = """
            Before <b>html</b>

            ![alt text](https://example.com/image.png)
        """.trimIndent()

        val document = MarkdownDocumentParser.parse(markdown)

        assertFalse(document.blocks.isEmpty())
        assertTrue(markdownToPlainPreview(markdown).contains("alt text"))
    }

    @Test
    fun markupOnlyInputReturnsEmptyPreview() {
        assertEquals("", markdownToPlainPreview("****"))
    }
}
