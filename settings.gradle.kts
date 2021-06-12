enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://a8c-libs.s3.amazonaws.com/android") }
        mavenCentral()
        google()
    }
}
include(":WordPressUtils")

