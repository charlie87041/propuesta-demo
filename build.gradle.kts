plugins {
    id("java")
    id("org.springframework.boot") version "3.2.0" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
  
}

allprojects {
    group = "com.cookiesstore"
    version = "0.1.0"
    
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")
    if (name != "application") {
        apply(plugin = "java-library")
    }

    configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.2.0")
        }
    }
    
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
    
    dependencies {
        implementation("org.springframework.boot:spring-boot-starter")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.junit.jupiter:junit-jupiter")
    }
    
    tasks.test {
        useJUnitPlatform()
        jvmArgs("-Dspring.profiles.active=test")
    }

    tasks.named<JavaCompile>("compileJava") {
        options.release = 21
    }
}

// Apply Spring Boot only to the application module
project(":application") {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
}
