package org.example.plugins;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import java.io.*;

// Плагін запускається під час фази компіляції і генерує звіт
@Mojo(name = "generate-report", defaultPhase = LifecyclePhase.COMPILE)
public class TodoReportMojo extends AbstractMojo {

    // Вказуємо, де шукати вихідний код
    @Parameter(defaultValue = "${project.build.sourceDirectory}", readonly = true)
    private File sourceDirectory;

    // Вказуємо, де створити файл зі звітом
    @Parameter(defaultValue = "${project.build.directory}/todo-report.txt")
    private File reportFile;

    public void execute() throws MojoExecutionException {
        getLog().info("Шукаю невиконані завдання (TODO) у коді...");
        try {
            if (!reportFile.getParentFile().exists()) {
                reportFile.getParentFile().mkdirs();
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile));
            writer.write("Звіт по TODO завданням:\n=======================\n");
            scanForTodos(sourceDirectory, writer);
            writer.close();
            getLog().info("Звіт успішно збережено у: " + reportFile.getName());
        } catch (Exception e) {
            throw new MojoExecutionException("Помилка генерації звіту", e);
        }
    }

    // Метод, який рекурсивно обходить всі файли та шукає коментарі // TODO
    private void scanForTodos(File dir, BufferedWriter writer) throws IOException {
        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    scanForTodos(file, writer);
                } else if (file.getName().endsWith(".java")) {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String line;
                    int lineNum = 1;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("// TODO") || line.contains("// TODO:")) {
                            writer.write("Файл: " + file.getName() + " | Рядок " + lineNum + ": " + line.trim() + "\n");
                        }
                        lineNum++;
                    }
                    reader.close();
                }
            }
        }
    }
}