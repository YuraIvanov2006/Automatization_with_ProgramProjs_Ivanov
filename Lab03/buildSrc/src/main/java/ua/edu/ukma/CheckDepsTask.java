package ua.edu.ukma;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public abstract class CheckDepsTask extends DefaultTask {
    @TaskAction
    public void run() {
        System.out.println("Configurations count: " + getProject().getConfigurations().size());
    }
}