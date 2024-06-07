package br.ufpe.liber.controllers

import br.ufpe.liber.assets.AssetsResolver
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.DefaultHttpClientConfiguration
import io.micronaut.http.client.HttpClient
import io.micronaut.kotlin.context.createBean
import io.micronaut.kotlin.context.getBean
import io.micronaut.kotlin.http.argumentOf
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

@MicronautTest
class IndexControllerTest(private val server: EmbeddedServer, private val context: ApplicationContext) :
    BehaviorSpec({
        val client = context.createBean<HttpClient>(
            server.url,
            DefaultHttpClientConfiguration().apply { isExceptionOnErrorStatus = false },
        ).toBlocking()

        beforeSpec {
            context.getBean<AssetsResolver>()
        }

        given("IndexController") {
            `when`("navigating to pages") {
                forAll(
                    row("/", HttpStatus.OK),
                    row("/equipe", HttpStatus.OK),
                    row("/pereira-da-costa", HttpStatus.OK),
                    row("/anais-pernambucanos", HttpStatus.OK),
                    row("/poema", HttpStatus.OK),
                    row("/contato", HttpStatus.OK),
                    row("/does-not-exists", HttpStatus.NOT_FOUND),
                ) { path, expectedStatus ->
                    then("GET $path should return $expectedStatus") {
                        val request: HttpRequest<Unit> = HttpRequest.GET(path)
                        val response = client.exchange(request, argumentOf<KteWriteable>(), argumentOf<KteWriteable>())
                        response.status shouldBe expectedStatus
                    }

                    then("GET $path should return correct Content-Type") {
                        val request: HttpRequest<Unit> = HttpRequest.GET(path)
                        val response = client.exchange(request, argumentOf<KteWriteable>(), argumentOf<KteWriteable>())
                        response.header(HttpHeaders.CONTENT_TYPE) shouldBe "text/html; charset=utf-8"
                    }
                }
            }
        }
    })
