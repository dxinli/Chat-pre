package component

import react.*
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.button
import component.external.EmailShareButton
import component.external.EmailIcon
import component.external.TelegramShareButton
import component.external.TelegramIcon
import component.external.ReactPlayer
import emotion.react.css
import model.Video
import mui.material.iconClasses
import web.cssom.Display
import web.cssom.NamedColor
import web.cssom.Position
import web.cssom.px

external interface VideoPlayerProps : Props {
    var video: Video
    var onWatchedButtonPressed: (Video) -> Unit
    var unwatchedVideo: Boolean
}

val VideoPlayer = FC<VideoPlayerProps> { props ->
    div {
        css {
            position = Position.absolute
            top = 10.px
            right = 10.px
        }
        h3 {
            +"${props.video.speaker}: ${props.video.title}"
        }
        button {
            css {
                display = Display.block
                backgroundColor = if (props.unwatchedVideo) NamedColor.lightgreen else NamedColor.red
            }
            onClick = {
                props.onWatchedButtonPressed(props.video)
            }
            if (props.unwatchedVideo) {
                +"Mark as watched"
            } else {
                +"Mark as unwatched"
            }
        }
        EmailShareButton {
            url = props.video.videoUrl
            EmailIcon.create {  // 显式创建组件
                size = 32
                round = true
            }
        }
        TelegramShareButton {
            url = props.video.videoUrl
            TelegramIcon.create {
                size = 32
                round = true
            }
        }
        ReactPlayer {
            url = props.video.videoUrl
            controls = true
        }
    }
}
