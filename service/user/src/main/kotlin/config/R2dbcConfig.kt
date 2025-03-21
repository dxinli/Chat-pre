package iuo.zmua.user.config

import com.github.f4b6a3.ulid.UlidCreator
import iuo.zmua.user.entity.BaseEntity
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback
import reactor.core.publisher.Mono


@Configuration
class R2dbcConfig {

    @Bean
    fun ulidGeneratorCallback(): BeforeConvertCallback<BaseEntity> {
        return BeforeConvertCallback { entity, _ ->
            Mono.fromSupplier {
                entity.id = entity.id ?: UlidCreator.getUlid().toString()
                entity
            }
        }
    }
}