import org.gradle.kotlin.dsl.kotlin

plugins {
    kotlin("jvm") version "2.4.0" apply false
    kotlin("plugin.spring") version "2.4.0" apply false

    id("org.springframework.boot") version "4.1.1" apply false
}