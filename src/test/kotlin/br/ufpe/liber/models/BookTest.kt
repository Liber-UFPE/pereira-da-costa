package br.ufpe.liber.models

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeSortedWith
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.optional.shouldBePresent
import io.kotest.matchers.optional.shouldNotBePresent
import io.kotest.matchers.shouldBe
import io.micronaut.core.io.ResourceResolver
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.util.Optional

@MicronautTest
class BookTest(private val resourceResolver: ResourceResolver) : BehaviorSpec({
    given("Book loaded from JSON") {
        val book: Book =
            resourceResolver
                .getResourceAsStream("classpath:data/json/book-1.utf8.json")
                .map { inputStream ->
                    val json = inputStream.bufferedReader().use(BufferedReader::readText)
                    Json.decodeFromString<Book>(json)
                }
                .get()

        val expectedPagesSize = 497

        `when`("parsing the model") {
            then("it should have the correct number") { book.number shouldBe 1 }
            then("it should have the correct number of pages") { book.pages.size shouldBe expectedPagesSize }
            then("pages should be sorted by number") {
                book.pages shouldBeSortedWith { page1, page2 -> (page1.number - page2.number) }
            }
        }

        `when`(".page") {
            then("should return a page when it exists") {
                val page = book.pages.random()
                book.page(page = page.number, year = page.year) shouldBePresent {
                    it.number shouldBe page.number
                    it.year shouldBe page.year
                }
            }

            then("should return an empty optional when it does not exist") {
                // We know that there are no negative IDs
                book.page(page = -1, year = -1) shouldBe Optional.empty()
            }

            then("should return an empty optional when page number exists but year does not") {
                val page = book.pages.random() // to get a valid page number
                book.page(page = page.number, year = -1) shouldBe Optional.empty()
            }

            then("should return an empty optional when page year exists but number does not") {
                val page = book.pages.random() // to get a valid page number
                book.page(page = -1, year = page.year) shouldBe Optional.empty()
            }
        }

            }
        }

        `when`(".nextPage") {
            then("first page should have a next page") {
                val firstPage = book.pages.first()
                book.nextPage(firstPage.number, firstPage.year) shouldBePresent { nextPage ->
                    nextPage.year shouldBe firstPage.year
                    nextPage.number shouldBeGreaterThan firstPage.number
                }
            }

            then("last page should NOT have a next page") {
                val lastPage = book.pages.last()
                book.nextPage(lastPage.number, lastPage.year).shouldNotBePresent()
            }
        }

        `when`(".previousPage") {
            then("first page should NOT have a previous page") {
                val firstPage = book.pages.first()
                book.previousPage(firstPage.number, firstPage.year).shouldNotBePresent()
            }

            then("last page should have a previous page") {
                val lastPage = book.pages.last()
                book.previousPage(lastPage.number, lastPage.year) shouldBePresent { previousPage ->
                    previousPage.year shouldBe lastPage.year
                    previousPage.number shouldBeLessThan lastPage.number
                }
            }
        }
    }
})
