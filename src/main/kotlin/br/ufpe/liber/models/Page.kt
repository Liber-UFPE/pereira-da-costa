package br.ufpe.liber.models

import kotlinx.serialization.Serializable

@Serializable
data class Page(
    val number: Int,
    val year: Int,
    val text: String,
) {
    companion object {
        private val SPLIT_PATTERN = "([\\p{Ll}\\d\"])([.,!?:])([\\p{Lu}\\d\"])".toRegex()
    }

    override fun toString(): String = "Page(number = $number, year = $year)"

    @Suppress("detekt:MagicNumber")
    fun formattedText(): String = text.replace(SPLIT_PATTERN) {
        "${it.groupValues[1]}${it.groupValues[2]} ${it.groupValues[3]}"
    }
}
