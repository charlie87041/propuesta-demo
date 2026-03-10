dependencies {
    implementation(project(":common"))
    implementation(project(":catalog-module"))
    implementation(project(":cart-module"))
    implementation(project(":order-module"))
    implementation(project(":payment-module"))
    implementation(project(":admin-module"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.postgresql:postgresql")
}
