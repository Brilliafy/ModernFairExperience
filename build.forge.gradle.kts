import me.modmuss50.mpp.ReleaseType.STABLE

plugins {
    // Develops against MinecraftForge 1.20.1 (official mappings in dev, reobfuscated to SRG in production).
    id("net.neoforged.moddev.legacyforge") version "2.0.148"
    id("me.modmuss50.mod-publish-plugin") version "2.2.1"
}

// DO NOT set group = ...!
version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = "${property("mod.id") as String}-forge"

val modId: String = property("mod.id") as String
val modVersion: String = property("mod.version") as String
val requiredJava: JavaVersion = JavaVersion.VERSION_17 // MC 1.20.1

val compatibleVersions: List<String> = sc.properties.rawOrNull("mod", "mc_releases")
    ?.asList().orEmpty().map { it.toString() }

repositories {
    mavenCentral()
    maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
}

tasks {
    // The version that matches `stonecutter active` compiles the shared root `src`, so make
    // sure the guards are preprocessed for it before javac runs.
    named("compileJava") {
        dependsOn("stonecutterGenerate")
    }
}

dependencies {
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

    compileOnly("io.github.llamalad7:mixinextras-common:0.5.5")
    annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.5")
    add("jarJar", "io.github.llamalad7:mixinextras-forge:0.5.5")
}

legacyForge {
    version = "1.20.1-${property("deps.forge_loader")}"

    validateAccessTransformers = true

    parchment {
        minecraftVersion = "1.20.1"
        mappingsVersion = "2023.09.03"
    }

    runs {
        register("client") {
            client()
            gameDirectory = rootProject.file("run")
            jvmArguments.add("-Dmixin.debug.export=true")
        }
        register("server") {
            server()
            gameDirectory = rootProject.file("run")
        }
    }

    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
}

// Mixin annotation processor: generates refmaps for SRG production names.
mixin {
    add(sourceSets.main.get(), "mixins." + modId + ".refmap.json")
    config("mixins." + modId + ".json")
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks {
    processResources {
        fun MutableMap<String, String>.register(key: String, value: String) {
            inputs.property(key, value)
            set(key, value)
        }

        val props = buildMap {
            register("id", sc.properties["mod.id"])
            register("name", sc.properties["mod.name"])
            register("version", sc.properties["mod.version"])
            register("minecraft", sc.properties["mod.mc_compat"])
        }

        filesMatching("META-INF/mods.toml") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }

        // The Mixin annotation processor generates the real refmap for this jar.
        exclude("fabric.mod.json", "mixins." + modId + ".refmap.json", "META-INF/neoforge.mods.toml")
    }

    named<Jar>("jar") {
        manifest {
            attributes("MixinConfigs" to "mixins." + modId + ".json")
        }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        inputs.property("version", project.property("mod.version"))
        // The SRG-reobfuscated jar is the production artifact.
        from(named<org.gradle.jvm.tasks.Jar>("reobfJar").flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/" + modVersion))
    }
}

// Publishes this version+loader jar to Modrinth and CurseForge when a tag is pushed.
// `file` points at the production jar (SRG-reobfuscated for 1.20.1 Forge).
publishMods {
    file.set(tasks.named<org.gradle.jvm.tasks.Jar>("reobfJar").flatMap { it.archiveFile })
    displayName.set("${property("mod.name")} ${property("mod.version")} (forge ${sc.current.version})")
    version.set("${property("mod.version")}+${sc.current.version}-forge")
    changelog.set(providers.environmentVariable("CHANGELOG").orElse("See the GitHub release notes."))
    type.set(STABLE)
    modLoaders.add("forge")

    modrinth {
        projectId.set(sc.properties.get<String>("publish.modrinth_id"))
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        minecraftVersions.addAll(compatibleVersions)
    }

    curseforge {
        projectId.set(sc.properties.get<String>("publish.curseforge_id"))
        projectSlug.set(sc.properties.get<String>("publish.curseforge_slug"))
        accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
        minecraftVersions.addAll(compatibleVersions)
        client.set(true)
        server.set(true)
    }
}
