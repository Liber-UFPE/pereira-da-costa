package br.ufpe.liber.controllers

import br.ufpe.liber.Templates
import br.ufpe.liber.services.BooksRepository
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.http.context.ServerRequestContext
import java.util.Optional

@Controller
@Produces(MediaType.TEXT_HTML)
class BooksController(private val templates: Templates, private val booksRepository: BooksRepository) : KteController {
    @Get("/livro/{bookNumber}/ano/{year}/pagina/{pageNumber}")
    fun show(bookNumber: Int, year: Int, pageNumber: Int, query: Optional<String>): HttpResponse<KteWriteable> {
        return booksRepository
            .get(bookNumber)
            .flatMap { book ->
                book.page(pageNumber, year).map { page ->
                    ok(templates.booksShow(book, page, query))
                }
            }
            .orElse(notFound(templates.notFound(currentRequestPath())))
    }

    private fun currentRequestPath(): String = ServerRequestContext.currentRequest<Any>()
        .map { it.path }
        .orElse("")
}
