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

        `when`(".pagesSize") {
            then("page size is correctly calcuated") { book.pagesSize shouldBe expectedPagesSize }
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

        `when`(".years") {
            then("should return the years with number of pages") {
                book.years shouldBe
                    mapOf(
                        // These numbers came from the original dump, by running the following query:
                        // select y.year_number, count(*)
                        // from PUBLIC.pc_page p
                        // join PUBLIC.pc_year y on y.year_id = p.year_id
                        // where y.book_id = 1
                        // group by y.year_number
                        // order by y.year_number asc
                        1493 to 2,
                        1494 to 5,
                        1500 to 18,
                        1501 to 12,
                        1503 to 1,
                        1504 to 4,
                        1505 to 3,
                        1508 to 1,
                        1513 to 2,
                        1516 to 11,
                        1519 to 4,
                        1520 to 1,
                        1523 to 1,
                        1525 to 8,
                        1527 to 1,
                        1528 to 3,
                        1530 to 4,
                        1531 to 8,
                        1532 to 16,
                        1533 to 1,
                        1534 to 7,
                        1535 to 14,
                        1536 to 1,
                        1537 to 8,
                        1539 to 7,
                        1540 to 13,
                        1542 to 5,
                        1545 to 3,
                        1546 to 8,
                        1548 to 11,
                        1549 to 11,
                        1550 to 11,
                        1551 to 15,
                        1552 to 8,
                        1553 to 1,
                        1554 to 9,
                        1555 to 3,
                        1556 to 3,
                        1557 to 4,
                        1559 to 11,
                        1560 to 9,
                        1561 to 1,
                        1562 to 1,
                        1564 to 1,
                        1565 to 8,
                        1566 to 4,
                        1568 to 3,
                        1569 to 5,
                        1570 to 4,
                        1571 to 6,
                        1572 to 1,
                        1573 to 11,
                        1574 to 2,
                        1575 to 5,
                        1576 to 24,
                        1577 to 11,
                        1578 to 13,
                        1579 to 3,
                        1580 to 13,
                        1581 to 4,
                        1582 to 6,
                        1584 to 28,
                        1585 to 33,
                        1587 to 13,
                        1588 to 13,
                        1589 to 4,
                        1590 to 12,
                    )
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
