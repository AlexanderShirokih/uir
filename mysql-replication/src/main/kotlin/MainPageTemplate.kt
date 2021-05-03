package ru.alexandershirokikh.uir

import io.ktor.html.*
import kotlinx.html.*

class MainPageTemplate(private val params: Map<String, List<Parameter>>) : Template<HTML> {

    override fun HTML.apply() {
        head {
            title { +"Параметры" }
            styleLink("style.css")
        }
        body {
            div(classes = "container") {
                div {
                    div(classes = "form") {
                        h3 { +"Добавление параметра" }
                        postForm {
                            action = "/param"
                            textInput {
                                id = "code"
                                name = "code"
                                placeholder = "Код"
                            }
                            br
                            textInput {
                                id = "value"
                                name = "value"
                                placeholder = "Значение"
                            }
                            br
                            submitInput { value = "Отправить" }
                        }
                    }
                }
                div {
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