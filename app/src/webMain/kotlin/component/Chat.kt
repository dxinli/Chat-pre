package component

import emotion.css.css
import component.external.*
import react.FC
import react.Props
import react.dom.html.ReactHTML.div

external interface MessageProps : Props {
    var message: String
    var sender: String
    var time: String
}

val Message = FC<MessageProps> { _ ->
    div{
        Box{
            css{
                
            }
        }
    }
}