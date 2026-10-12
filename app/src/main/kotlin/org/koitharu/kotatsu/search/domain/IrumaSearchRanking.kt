package org.koitharu.kotatsu.search.domain

import org.koitharu.kotatsu.parsers.model.Manga
import java.text.Normalizer
import java.util.Locale

/**
 * Iruma Manga's app-owned search comparison and ranking policy.
 *
 * The original query is still passed to each source adapter. This code only
 * normalizes local comparisons and ranks actual results returned by a source.
 */
internal object IrumaSearchRanking {

    fun normalize(value: String?): String {
        if (value.isNullOrBlank()) return ""
        val compatibilityNormalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
            .lowercase(Locale.ROOT)
        val withoutMarks = compatibilityNormalized.filterNot { character ->
            character.code in 0x064B..0x065F ||
                character.code == 0x0670 ||
                character.code == 0x0640
        }
        return withoutMarks
            .replace(Regex("[أإآٱ]"), "ا")
            .replace('ى', 'ي')
            .replace('ؤ', 'و')
            .replace('ئ', 'ي')
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun score(manga: Manga, query: String): Int {
        val needle = normalize(query)
        if (needle.isEmpty()) return Int.MAX_VALUE

        val titles = listOfNotNull(manga.title, manga.altTitle)
            .map(::normalize)
            .filter(String::isNotEmpty)
        if (titles.any { it == needle }) return 0
        if (titles.any { it.startsWith(needle) }) return 1
        if (titles.any { needle in it }) return 2

        val tokens = needle.split(' ').filter { it.length > 1 }
        if (tokens.size > 1 && titles.any { title ->
                tokens.all(title::contains)
            }) {
            return 3
        }

        val distance = titles.minOfOrNull { title -> editDistance(needle, title) } ?: needle.length
        val typoAllowance = maxOf(1, needle.length / 4)
        return if (distance <= typoAllowance) 10 + distance else 100 + distance
    }

    fun isTitleMatch(manga: Manga, query: String): Boolean = score(manga, query) < 100

    internal fun editDistance(first: String, second: String): Int {
        if (first == second) return 0
        if (first.isEmpty()) return second.length
        if (second.isEmpty()) return first.length

        var previous = IntArray(second.length + 1) { it }
        for (i in first.indices) {
            val current = IntArray(second.length + 1)
            current[0] = i + 1
            for (j in second.indices) {
                val substitution = if (first[i] == second[j]) 0 else 1
                current[j + 1] = minOf(
                    current[j] + 1,
                    previous[j + 1] + 1,
                    previous[j] + substitution,
                )
            }
            previous = current
        }
        return previous[second.length]
    }
}
