/**
 * Gradle Toolchain Configuration
 *
 * This project requires Java 21 as specified in design.md.
 * If Java 21 is not available, Gradle will attempt to download it automatically.
 */

import org.gradle.api.JavaVersion

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}
