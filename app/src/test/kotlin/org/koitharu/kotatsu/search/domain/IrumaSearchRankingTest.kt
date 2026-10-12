package org.koitharu.kotatsu.search.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class IrumaSearchRankingTest {

    @Test
    fun normalizesArabicAlefAndDiacritics() {
        assertEquals(IrumaSearchRanking.normalize("ابجد"), IrumaSearchRanking.normalize("أَبْجَد"))
    }

    @Test
    fun trimsCollapsesWhitespaceAndIgnoresCase() {
        assertEquals("iruma manga", IrumaSearchRanking.normalize("  IRUMA   Manga  "))
    }

    @Test
    fun editDistanceCountsSingleInsertion() {
        assertEquals(1, IrumaSearchRanking.editDistance("manga", "mangga"))
    }
}
