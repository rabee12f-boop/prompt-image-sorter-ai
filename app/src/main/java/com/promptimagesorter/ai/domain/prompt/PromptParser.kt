package com.promptimagesorter.ai.domain.prompt

import com.promptimagesorter.ai.domain.model.PromptItem

/**
 * Parses a raw prompts text file into an ordered list of [PromptItem].
 *
 * Supported line formats for a scene header, matched in this order:
 *   "Scene 01 - text"      "Scene 01: text"      "Scene 01"  (header only,
 *   body on following lines until the next header)
 *   "01 - text"            "01. text"            "1 | text"
 *
 * If NO line in the whole file matches a scene-header pattern, the parser
 * falls back to "one non-empty line/paragraph = one scene, in file order"
 * (requirement #7, last bullet).
 *
 * This class deliberately does not touch image files or the filesystem —
 * it is a pure text -> data transform, which makes it trivial to unit test.
 */
class PromptParser {

    private val headerRegexes: List<Regex> = listOf(
        // "Scene 01 - text" / "Scene 01: text" / "Scene 01 text"
        Regex("""^\s*Scene\s*0*(\d+)\s*[-:.|]?\s*(.*)$""", RegexOption.IGNORE_CASE),
        // "01 - text" / "01. text" / "01: text"
        Regex("""^\s*0*(\d+)\s*[-.:|]\s*(.*)$"""),
    )

    fun parse(rawText: String): PromptParseResult {
        val lines = rawText
            .replace("\r\n", "\n")
            .replace('\r', '\n')
            .split("\n")

        val headerMatches = mutableListOf<Triple<Int, Int, String>>() // (lineIndex, sceneNumber, inlineText)
        for ((index, line) in lines.withIndex()) {
            if (line.isBlank()) continue
            val match = matchHeader(line) ?: continue
            headerMatches += Triple(index, match.first, match.second)
        }

        return if (headerMatches.isNotEmpty()) {
            parseWithHeaders(lines, headerMatches)
        } else {
            parseAsSequentialParagraphs(lines)
        }
    }

    private fun matchHeader(line: String): Pair<Int, String>? {
        for (regex in headerRegexes) {
            val m = regex.find(line) ?: continue
            val number = m.groupValues[1].toIntOrNull() ?: continue
            val inline = m.groupValues.getOrElse(2) { "" }.trim()
            return number to inline
        }
        return null
    }

    private fun parseWithHeaders(
        lines: List<String>,
        headerMatches: List<Triple<Int, Int, String>>,
    ): PromptParseResult {
        val items = mutableListOf<PromptItem>()
        val warnings = mutableListOf<String>()
        val seenSceneNumbers = mutableSetOf<Int>()

        for ((matchIndex, header) in headerMatches.withIndex()) {
            val (lineIndex, sceneNumber, inlineText) = header
            val bodyStart = lineIndex + 1
            val bodyEnd = if (matchIndex + 1 < headerMatches.size) {
                headerMatches[matchIndex + 1].first
            } else {
                lines.size
            }

            val bodyLines = lines.subList(bodyStart, bodyEnd)
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            val fullText = buildString {
                if (inlineText.isNotBlank()) appendLine(inlineText)
                bodyLines.forEach { appendLine(it) }
            }.trim()

            if (fullText.isBlank()) {
                warnings += "Scene $sceneNumber has no text (empty prompt)."
                continue
            }

            if (!seenSceneNumbers.add(sceneNumber)) {
                warnings += "Duplicate scene number $sceneNumber found — keeping the first occurrence."
                continue
            }

            items += PromptItem(
                sceneNumber = sceneNumber,
                originalText = fullText,
                cleanVisualText = cleanForEmbedding(fullText),
            )
        }

        return PromptParseResult(
            items = items.sortedBy { it.sceneNumber },
            usedFallbackSequentialMode = false,
            warnings = warnings,
        )
    }

    private fun parseAsSequentialParagraphs(lines: List<String>): PromptParseResult {
        // Group consecutive non-blank lines into paragraphs; blank line(s) separate scenes.
        val paragraphs = mutableListOf<String>()
        val current = StringBuilder()

        fun flush() {
            val text = current.toString().trim()
            if (text.isNotEmpty()) paragraphs += text
            current.clear()
        }

        for (line in lines) {
            if (line.isBlank()) {
                flush()
            } else {
                if (current.isNotEmpty()) current.append(' ')
                current.append(line.trim())
            }
        }
        flush()

        val items = paragraphs.mapIndexed { index, text ->
            PromptItem(
                sceneNumber = index + 1,
                originalText = text,
                cleanVisualText = cleanForEmbedding(text),
            )
        }

        val warnings = if (items.isEmpty()) {
            listOf("Prompt file appears to be empty or unreadable (invalid prompt format).")
        } else {
            emptyList()
        }

        return PromptParseResult(
            items = items,
            usedFallbackSequentialMode = true,
            warnings = warnings,
        )
    }

    /**
     * Light normalization only — no NLP yet. Collapses whitespace and drops
     * characters that don't help a future text-embedding model. Real
     * attribute extraction (character/environment/action/etc., see
     * requirement #8) is deferred to a later Prompt Analyzer.
     */
    private fun cleanForEmbedding(text: String): String =
        text.replace(Regex("\\s+"), " ").trim()
}

data class PromptParseResult(
    val items: List<PromptItem>,
    val usedFallbackSequentialMode: Boolean,
    val warnings: List<String>,
)
