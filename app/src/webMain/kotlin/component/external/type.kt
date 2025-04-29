package component.external

import react.Props

external interface ReactPlayerProps : Props {
    var url: String
    var controls: Boolean
}

external interface ShareButtonProps : Props {
    var url: String
}

external interface IconProps : Props {
    var size: Int
    var round: Boolean
}

external interface BoxProps : Props {
    var component: String
    var sx: Any
}