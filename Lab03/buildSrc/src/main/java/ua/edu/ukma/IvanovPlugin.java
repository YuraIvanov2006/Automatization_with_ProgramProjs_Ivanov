package ua.edu.ukma;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class IvanovPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getTasks().register("generateDocs", GenerateDocsTask.class);
        project.getTasks().register("checkDeps", CheckDepsTask.class);
    }
}