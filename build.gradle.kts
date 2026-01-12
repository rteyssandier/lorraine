plugins {
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.kotlin).apply(false)

    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.kotlin.compose.compiler).apply(false)
    alias(libs.plugins.kotlin.compose.hotreload).apply(false)
    alias(libs.plugins.kotlin.cocoapods).apply(false)
}
