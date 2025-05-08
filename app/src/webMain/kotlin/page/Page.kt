package page

import mui.system.Box
import mui.system.sx
import page.Sidebar
import react.FC
import web.cssom.*

val Page = FC {
    val isMobile = useIsMobile()

    Box {
        sx {
            display = Display.grid
            height = 100.pct
            gridTemplateRows = array(
                Sizes.Header.Height,
                Auto.auto,
                Length.maxContent,
            )
            gridTemplateColumns = array(
                Sizes.Sidebar.Width, Auto.auto,
            )
            gridTemplateAreas = GridTemplateAreas(
                arrayOf(Area.Header, Area.Header),
                arrayOf(if (isMobile) Area.Content else Area.Sidebar, Area.Content),
                arrayOf(Area.Footer, Area.Footer),
            )
        }

        Header()
        Sidebar()
        Content()
        Footer()
    }
}
