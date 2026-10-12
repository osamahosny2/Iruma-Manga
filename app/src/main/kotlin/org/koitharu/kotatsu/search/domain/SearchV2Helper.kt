package org.koitharu.kotatsu.search.domain

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.koitharu.kotatsu.core.model.isNsfw
import org.koitharu.kotatsu.core.parser.MangaDataRepository
import org.koitharu.kotatsu.core.parser.MangaRepository
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.core.util.ext.contains
import org.koitharu.kotatsu.core.util.ext.printStackTraceDebug
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaListFilter
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.model.SortOrder
import org.koitharu.kotatsu.parsers.util.runCatchingCancellable

class SearchV2Helper @AssistedInject constructor(
    @Assisted private val source: MangaSource,
    private val mangaRepositoryFactory: MangaRepository.Factory,
    private val dataRepository: MangaDataRepository,
    private val settings: AppSettings,
) {

    suspend operator fun invoke(query: String, kind: SearchKind): SearchResults? {
        if (settings.isNsfwContentDisabled && source.isNsfw()) {
            return null
        }
        val cleanedQuery = query.trim().replace(Regex("\s+"), " ")
        val repository = mangaRepositoryFactory.create(source)
        val listFilter = repository.getFilter(cleanedQuery, kind) ?: return null
        val sortOrder = repository.getSortOrder(kind)
        val list = repository.getList(0, sortOrder, listFilter)
        if (list.isEmpty()) {
            return null
        }
        val result = list.toMutableList()
        result.postFilter(cleanedQuery, kind)
        result.sortByRelevance(cleanedQuery, kind)
        return SearchResults(listFilter = listFilter, sortOrder = sortOrder, manga = result)
    }

    private suspend fun MangaRepository.getFilter(query: String, kind: SearchKind): MangaListFilter? = when (kind) {
        SearchKind.SIMPLE,
        SearchKind.TITLE,
        -> if (filterCapabilities.isSearchSupported) {
            MangaListFilter(query = query)
        } else {
            null
        }

        SearchKind.AUTHOR -> if (filterCapabilities.isAuthorSearchSupported) {
            MangaListFilter(author = query)
        } else if (filterCapabilities.isSearchSupported) {
            MangaListFilter(query = query)
        } else {
            null
        }

        SearchKind.TAG -> {
            val tags = this@SearchV2Helper.dataRepository.findTags(this.source) + runCatchingCancellable {
                this@getFilter.getFilterOptions().availableTags
            }.onFailure { e ->
                e.printStackTraceDebug()
            }.getOrDefault(emptySet())
            val tag = tags.find { x -> x.title.equals(query, ignoreCase = true) }
            if (tag != null) {
                MangaListFilter(tags = setOf(tag))
            } else {
                null
            }
        }
    }

    private fun MutableList<Manga>.postFilter(query: String, kind: SearchKind) {
        if (settings.isNsfwContentDisabled) {
            removeAll { it.isNsfw() }
        }
        when (kind) {
            SearchKind.TITLE -> {
                // Preserve source results if strict local matching would otherwise hide everything.
                val closeMatches = filter { manga -> IrumaSearchRanking.isTitleMatch(manga, query) }
                if (closeMatches.isNotEmpty()) {
                    clear()
                    addAll(closeMatches)
                }
            }

            SearchKind.AUTHOR -> retainAll { manga ->
                manga.authors.isEmpty() || manga.authors.contains(query, ignoreCase = true)
            }

            SearchKind.SIMPLE,
            SearchKind.TAG,
            -> Unit
        }
    }

    private fun MutableList<Manga>.sortByRelevance(query: String, kind: SearchKind) {
        when (kind) {
            SearchKind.SIMPLE,
            SearchKind.TITLE,
            -> sortBy { manga -> IrumaSearchRanking.score(manga, query) }

            SearchKind.AUTHOR -> sortByDescending { manga ->
                manga.authors.contains(query, ignoreCase = true)
            }

            SearchKind.TAG -> sortByDescending { manga ->
                manga.tags.any { tag -> tag.title.equals(query, ignoreCase = true) }
            }
        }
    }

    private fun MangaRepository.getSortOrder(kind: SearchKind): SortOrder {
        val preferred: SortOrder = when (kind) {
            SearchKind.SIMPLE,
            SearchKind.TITLE,
            SearchKind.AUTHOR,
            -> SortOrder.RELEVANCE

            SearchKind.TAG -> SortOrder.POPULARITY
        }
        return if (preferred in sortOrders) {
            preferred
        } else {
            defaultSortOrder
        }
    }

    @AssistedFactory
    interface Factory {

        fun create(source: MangaSource): SearchV2Helper
    }
}
