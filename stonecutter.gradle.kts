plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.20.1-fabric"

stonecutter parameters {
    val (version, loader) = current.project.split('-', limit = 2)

    properties {
        tags(version, loader)
    }
}
