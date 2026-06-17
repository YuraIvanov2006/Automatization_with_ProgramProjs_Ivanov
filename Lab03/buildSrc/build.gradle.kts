plugins {
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        create("ivanovPlugin") {
            id = "ua.edu.ukma.ivanov-plugin"
            implementationClass = "ua.edu.ukma.IvanovPlugin"
        }
    }
}