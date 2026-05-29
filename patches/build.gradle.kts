group = "drobotk.revanced"

patches {
    about {
        name = "ReVanced Patches"
        description = "Patches for ReVanced"
        source = "git@github.com:drobotk/drobotk-rvp.git"
        author = "drobotk"
        contact = "drobotk@github.com"
        website = "https://github.com/drobotk/drobotk-rvp"
        license = "GNU General Public License v3.0"
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xcontext-parameters", "-Xexplicit-backing-fields")
    }
}

dependencies {
    compileOnly(libs.revanced.patches)
}

repositories {
    mavenLocal()
    mavenCentral()
    google()
    maven {
        name = "githubPackages"
        url = uri("https://maven.pkg.github.com/revanced/revanced")
        credentials(PasswordCredentials::class)
    }
}