@file:JsModule("react-player")
@file:JsNonModule

package component.external

import react.*

@JsName("ReactPlayer")
external val ReactPlayer: ComponentClass<ReactPlayerProps>

external interface ReactPlayerProps : Props {
    var url: String
    var controls: Boolean
}
