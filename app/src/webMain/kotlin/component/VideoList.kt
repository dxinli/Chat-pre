package component

import model.Video
import react.*
import react.dom.html.ReactHTML.p

val VideoList = FC<VideoListProps>{ props->

    for (video in props.videos) {
        p {
            key = video.id.toString()
            onClick = { props.onSelectVideo(video) }
            if(video == props.selectedVideo){
                + "▶"
            }
            +"${video.speaker}: ${video.title}"
        }
    }
}

external interface VideoListProps : Props {
    var videos: List<Video>
    var selectedVideo: Video?
    var onSelectVideo: (Video) -> Unit
}
