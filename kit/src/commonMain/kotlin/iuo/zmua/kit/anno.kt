package iuo.zmua.kit

@Target(AnnotationTarget.CLASS,AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class RSocketApi(val path: String)