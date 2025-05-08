package router

import react.router.useLocation

fun useShowcaseIdParam(): String =
    useLocation().pathname.substringAfterLast("/")
