package br.ufpe.liber.models

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe

class PageTest : BehaviorSpec({
    given("Page") {
        `when`(".textFormatted") {
            forAll(
                row("Uma frase.Sem espaços.", "Uma frase. Sem espaços."),
                row("Uma frase!Com exclamação.", "Uma frase! Com exclamação."),
                row("Uma frase?Com interrogação.", "Uma frase? Com interrogação."),
                row("Uma frase:Com dois pontos.", "Uma frase: Com dois pontos."),
                row("Próxima frase.É com acentos.", "Próxima frase. É com acentos."),
                row("Termina 1500.Com número", "Termina 1500. Com número"),
                row("Começa.1500 com número", "Começa. 1500 com número"),
                row("Termina \"aspas\".Com aspas", "Termina \"aspas\". Com aspas"),
                row("Começa com.\"Aspas\".", "Começa com. \"Aspas\"."),
                row("Não adiciona espaços. Já está correta.", "Não adiciona espaços. Já está correta."),
            ) { text, expected ->
                then("add spaces when necessary") {
                    Page(1, 1, text).formattedText() shouldBe expected
                }
            }
        }
    }
})
