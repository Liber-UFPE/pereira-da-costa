package br.ufpe.liber.controllers

import br.ufpe.liber.Templates
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces

@Controller("/")
@Produces(MediaType.TEXT_HTML)
class IndexController(private val templates: Templates) : KteController {
    @Get("/")
    fun index() = ok(templates.index())

    @Get("/equipe")
    fun staff() = ok(templates.equipe())

    @Get("/pereira-da-costa")
    fun who() = ok(templates.quemFoi())

    @Get("/anais-pernambucanos")
    fun ap() = ok(templates.anaisPernambucanos())

    @Get("/poema")
    fun poema() = ok(templates.poema())

    @Get("/contato")
    fun contato() = ok(templates.contato())
}
