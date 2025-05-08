package page

import mui.system.useMediaQuery

fun useIsMobile(): Boolean =
    useMediaQuery("(max-width: 960px)")
