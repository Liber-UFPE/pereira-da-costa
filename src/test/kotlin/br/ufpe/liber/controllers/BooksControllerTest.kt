package br.ufpe.liber.controllers

import br.ufpe.liber.asString
import br.ufpe.liber.assets.AssetsResolver
import br.ufpe.liber.get
import br.ufpe.liber.search.TextHighlighter
import br.ufpe.liber.services.BooksRepository
import br.ufpe.liber.views.Markdown
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldContainIgnoringCase
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.DefaultHttpClientConfiguration
import io.micronaut.http.client.HttpClient
import io.micronaut.kotlin.context.getBean
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

@MicronautTest
class BooksControllerTest(
    private val server: EmbeddedServer,
    private val context: ApplicationContext,
) : BehaviorSpec({
    val client = context
        .createBean(
            HttpClient::class.java,
            server.url,
            DefaultHttpClientConfiguration().apply { isExceptionOnErrorStatus = false },
        )
        .toBlocking()

    val bookRepository = context.getBean<BooksRepository>()

    beforeSpec {
        context.getBean<AssetsResolver>()
        context.getBean<TextHighlighter>()
    }

    given("#show page") {
        `when`("accessing a page") {
            val book = bookRepository.listAll().random()
            val page = book.pages.random()

            val response = client.get("/livro/${book.number}/ano/${page.year}/pagina/${page.number}")

            then("returns HTTP 200") {
                response.status() shouldBe HttpStatus.OK
            }

            then("shows page's content") {
                val pageHtml = Markdown.toHtml(page.formattedText()).asString()
                response.body() shouldContain pageHtml
            }
        }

        `when`("'query' parameter is part of the query string") {
            val book = bookRepository.listAll().first()
            val page = book.pages.first()
            val query = "descobertas"

            // Get the page with a query
            val response = client.get("/livro/${book.number}/ano/${page.year}/pagina/${page.number}?query=$query")

            then("returns HTTP 200") {
                response.status() shouldBe HttpStatus.OK
            }

            then("should highlight query") {
                response.body() shouldContainIgnoringCase "<mark>$query</mark>"
            }
        }

        `when`("trying to access a page that does not exist") {
            then("returns HTTP 404") {
                val book = bookRepository.listAll().random()
                val lastPage = book.pages.last()
                val nonExistingPageId = lastPage.number + 100

                val response = client.get("/livro/${book.number}/ano/${lastPage.year}/pagina/$nonExistingPageId")
                response.status() shouldBe HttpStatus.NOT_FOUND
            }
        }
    }
})
