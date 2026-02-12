dependencies {
    // Common utilities and shared code
    
    // Spring Boot Redis support
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    
    // TestContainers for integration testing
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:postgresql:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    
    // Spring Boot test support
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc")
    
    // PostgreSQL driver
    testRuntimeOnly("org.postgresql:postgresql:42.7.1")
    
    // Property-based testing with jqwik
    testImplementation("net.jqwik:jqwik:1.8.2")
    
    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter")
}
