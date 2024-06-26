plugins {
    id("glyphs.dist-conventions")
    id("xyz.jpenilla.run-paper") version "2.2.3"
}

repositories {
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/nms/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
    maven("https://repo.essentialsx.net/releases/") // EssentialsDiscord
    maven("https://nexus.scarsz.me/content/groups/public/") // DiscordSRV
    maven("https://m2.dv8tion.net/releases") // JDA - Required by DiscordSRV
    maven("https://repo.unnamed.team/repository/unnamed-public/") // creative, command-flow
    mavenCentral()
}

dependencies {

    implementation(project(":creative-glyphs-api"))

    val serverApi = "io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT";

    // Required libraries
    compileOnly(serverApi)

    // Optional libraries
    compileOnly(libs.adventure.text.minimessage)

    // Internal libraries
    implementation("me.fixeddev:commandflow-universal:0.6.0") // command-flow
    implementation("me.fixeddev:commandflow-bukkit:0.6.0") // command-flow

    // Optional plugin hooks
    compileOnly("me.clip:placeholderapi:2.10.10")
    compileOnly(files("../lib/TownyChat-0.91.jar", "../lib/EzChat-3.0.3.jar"))
    compileOnly("net.essentialsx:EssentialsXDiscord:2.20.1")
    compileOnly("com.discordsrv:discordsrv:1.27.0") // DiscordSRV (shaded)
    compileOnly("io.github.miniplaceholders:miniplaceholders-api:2.1.0")
    compileOnly("de.hexaoxi:carbonchat-api:3.0.0-beta.25") // CarbonChat

    // Testing
    testImplementation(serverApi)
    testImplementation(libs.annotations)
}

tasks {
    runServer {
        downloadPlugins {
            modrinth("central", "1.3.0") // creative-central
            modrinth("carbon", "6dmNHzy8") // CarbonChat
            url("https://download.luckperms.net/1534/bukkit/loader/LuckPerms-Bukkit-5.4.121.jar") // LuckPerms
        }

        minecraftVersion("1.20.4")
    }
    shadowJar {
        val pkg = "team.unnamed.creativeglyphs.lib"
        relocate("org.ahocorasick", "$pkg.ahocorasick")
        relocate("me.fixeddev.commandflow", "$pkg.commandflow")
    }
}
