package br.ufpe.liber.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.NavigableMap
import java.util.Optional
import java.util.TreeMap

@Serializable
data class Book(
    val number: Int,
    val pages: List<Page> = listOf(),
) {
    @Transient
    private val pagesMap: NavigableMap<Key, Page> = TreeMap()

    init {
        pages.forEach {
            pagesMap[Key(it.number, it.year)] = it
        }
    }

    fun page(page: Int, year: Int): Optional<Page> = Optional.ofNullable(pagesMap[Key(page, year)])

    fun nextPage(page: Int, year: Int): Optional<Page> =
        Optional.ofNullable(pagesMap.higherEntry(Key(page, year))?.value)

    fun previousPage(page: Int, year: Int): Optional<Page> =
        Optional.ofNullable(pagesMap.lowerEntry(Key(page, year))?.value)

    private data class Key(val page: Int, val year: Int) : Comparable<Key> {
        override fun compareTo(other: Key): Int {
            val pageDiff = this.page.compareTo(other.page)
            return if (pageDiff == 0) this.year.compareTo(other.year) else pageDiff
        }
    }
}
