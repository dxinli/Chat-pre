package iuo.zmua.user.entity

import org.springframework.data.annotation.Id

// BaseEntity.kt
open class BaseEntity{
    @Id
    open var id: String? = null
}