package ru.alexandershirokikh.uir

import io.ktor.html.*
import kotlinx.html.*

// Шаблон страницы просмотра параметра
class DetailsPageTemplate : Template<HTML> {
    val item: Placeholder<FlowContent> = Placeholder()

    override fun HTML.apply() {
        head {
            title { +"Параметры" }
            styleLink("/style.css")
        }
        body {
            div(classes = "container-details") {
                h3 { +"Просмотр элемента" }

                insert(item)
            }
            footer {
                a {
                    href = "/"
                    +"Назад"
                }
            }
        }
    }
}