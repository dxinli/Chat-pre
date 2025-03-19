package buildSrc.convention.utils

import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.provider.Provider

fun VersionCatalog.spec(name: String, action: (dep: Provider<MinimalExternalModuleDependency>)->Unit) {
    findLibrary(name).ifPresent { dependency ->
        action(dependency)
    }
}