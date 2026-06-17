package org.example.plugins;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import java.io.*;

@Mojo(name = "aggregate", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class SourceAggregatorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.sourceDirectory}", readonly = true)
    private File sourceDirectory;

    @Parameter(defaultValue = "${project.build.directory}/all-sources.txt")
    private File outputFile;

    public void execute() throws MojoExecutionException {
        getLog().info("Збираю весь Java код в один файл...");
        try {
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            aggregateFiles(sourceDirectory, writer);
            writer.close();
        } catch (Exception e) {
            throw new MojoExecutionException("Помилка збирання коду", e);
        }
    }

    private void aggregateFiles(File dir, BufferedWriter writer) throws IOException {
        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    aggregateFiles(file, writer);
                } else if (file.getName().endsWith(".java")) {
                    writer.write("\n// --- Файл: " + file.getName() + " --- \n");
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line + "\n");
                    }
                    reader.close();
                }
            }
        }
    }
}