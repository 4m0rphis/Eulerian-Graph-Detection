plugins {
    id("java")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "univ.boussad.graphe.Main"
    }
}

group = "univ.boussad"
version = "1.0"