plugins {
    id("org.springframework.boot")
}

val flywayVersion = "10.20.0"

dependencies {
    implementation(project(":common"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation("com.pulumi:pulumi:1.8.0")
    implementation("com.pulumi:aws:7.11.0")

    implementation("software.amazon.awssdk:ec2:2.25.58")
    implementation("software.amazon.awssdk:sts:2.25.58")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-community-db-support:$flywayVersion")
    implementation("org.xerial:sqlite-jdbc:3.46.1.3")
    implementation("org.hibernate.orm:hibernate-community-dialects")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
}
