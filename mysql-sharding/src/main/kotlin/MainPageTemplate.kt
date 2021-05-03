package ru.alexandershirokikh.uir

import io.ktor.html.*
import kotlinx.html.*

// Шаблон главной страницы
class MainPageTemplate(private val params: Map<String, List<Parameter>>) : Template<HTML> {

    override fun HTML.apply() {
        head {
            title { +"Параметры" }
            styleLink("/style.css")
        }
        body {
            div(classes = "container") {
                h3 { +"Текущие значения" }

                params.forEach { (server, paramsList) ->
                    div(classes = "serverOutput") {
                        h4 {
                            +"Сервер: $server"
                        }

                        if (paramsList.isEmpty()) {
                            p {
                                +"Список пуст"
                            }
                        } else {
                            ul(classes = "paramsList") {
                                for (param in paramsList) {
                                    li {
                                        a {
                                            href = "param/${param.id}"
                                            span(classes = "code") {
                                                +param.id.value.toString()
                                            }

                                            span(classes = "code") {
                                                +param.code
                                            }
                                            span(classes = "value") {
                                                +param.value
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}