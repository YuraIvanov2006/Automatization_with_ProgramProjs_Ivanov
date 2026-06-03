package ua.edu.ukma;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public abstract class GenerateDocsTask extends DefaultTask {
    @TaskAction
    public void run() throws IOException {
        File doc = getProject().file("report.txt");
        try (FileWriter w = new FileWriter(doc)) {
            w.write("Project: " + getProject().getName());
        }
    }
}