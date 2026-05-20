package org.example.lab01;

import java.io.*;
import java.lang.management.*;
import java.util.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet(name = "systemInfoServlet", value = "/system-info")
public class SystemInfoServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html; charset=UTF-8");

        // ---- Збираємо дані ----
        Runtime runtime = Runtime.getRuntime();
        OperatingSystemMXBean osMxBean = ManagementFactory.getOperatingSystemMXBean();

        long totalRamBytes = 0;
        long freeRamBytes  = 0;
        int  physCores     = 0;

        // com.sun.management.OperatingSystemMXBean — є у всіх JVM HotSpot / OpenJDK
        if (osMxBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunOs =
                    (com.sun.management.OperatingSystemMXBean) osMxBean;
            totalRamBytes = sunOs.getTotalMemorySize();
            freeRamBytes  = sunOs.getFreeMemorySize();
        }

        // Логічні процесори (потоки)
        int logicalCores = runtime.availableProcessors();

        // Спроба отримати кількість фізичних ядер через Runtime
        try {
            ProcessBuilder pb;
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win")) {
                pb = new ProcessBuilder("cmd", "/c",
                        "wmic cpu get NumberOfCores /value");
            } else {
                pb = new ProcessBuilder("sh", "-c",
                        "cat /sys/devices/system/cpu/cpu*/topology/core_id | sort -u | wc -l");
            }
            pb.redirectErrorStream(true);
            Process proc = pb.start();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("NumberOfCores=")) {
                    String val = line.replace("NumberOfCores=", "").trim();
                    if (!val.isEmpty()) {
                        physCores += Integer.parseInt(val);
                    }
                } else if (line.matches("\\d+") && !osName.contains("win")) {
                    physCores = Integer.parseInt(line);
                }
            }
            proc.waitFor();
        } catch (Exception ignored) {}

        if (physCores <= 0) physCores = logicalCores;

        // JVM heap
        long jvmTotalMem = runtime.totalMemory();
        long jvmFreeMem  = runtime.freeMemory();
        long jvmMaxMem   = runtime.maxMemory();
        long jvmUsedMem  = jvmTotalMem - jvmFreeMem;

        // CPU навантаження (0..1, або -1 якщо недоступно)
        double cpuLoad = osMxBean.getSystemLoadAverage(); // -1 на Windows
        String cpuLoadStr;
        if (osMxBean instanceof com.sun.management.OperatingSystemMXBean) {
            double load = ((com.sun.management.OperatingSystemMXBean) osMxBean)
                    .getCpuLoad();
            cpuLoadStr = load >= 0 ? String.format("%.1f%%", load * 100) : "N/A";
        } else {
            cpuLoadStr = cpuLoad >= 0 ? String.format("%.2f", cpuLoad) : "N/A";
        }

        // OS та JVM
        String osName    = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String osArch    = System.getProperty("os.arch");
        String javaVer   = System.getProperty("java.version");
        String jvmName   = System.getProperty("java.vm.name");
        String hostname  = "Unknown";
        try { hostname = java.net.InetAddress.getLocalHost().getHostName(); }
        catch (Exception ignored) {}

        // Час роботи сервера
        long uptimeMs    = ManagementFactory.getRuntimeMXBean().getUptime();
        long uptimeSec   = uptimeMs / 1000;
        String uptimeStr = String.format("%d г. %d хв. %d с.",
                uptimeSec / 3600, (uptimeSec % 3600) / 60, uptimeSec % 60);

        // Кількість потоків JVM
        int threadCount = ManagementFactory.getThreadMXBean().getThreadCount();

        // ---- Допоміжні функції ----
        // (форматування байтів передаємо як рядки у відповідних місцях)

        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"uk\">");
        out.println("<head>");
        out.println("  <meta charset=\"UTF-8\">");
        out.println("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        out.println("  <title>Інформація про сервер</title>");
        out.println("  <style>");
        out.println("    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600;700;900&family=JetBrains+Mono:wght@400;600&display=swap');");
        out.println("    *{margin:0;padding:0;box-sizing:border-box;}");
        out.println("    body{font-family:'Inter',sans-serif;min-height:100vh;");
        out.println("         background:linear-gradient(135deg,#0a0a1a,#0d1b2a,#0a1628);");
        out.println("         color:#e2e8f0;padding:40px 20px;}");
        out.println("    .page-header{text-align:center;margin-bottom:48px;}");
        out.println("    .page-header h1{font-size:2.2rem;font-weight:900;");
        out.println("         background:linear-gradient(90deg,#38bdf8,#818cf8,#a78bfa);");
        out.println("         -webkit-background-clip:text;-webkit-text-fill-color:transparent;");
        out.println("         background-clip:text;letter-spacing:-1px;}");
        out.println("    .page-header p{color:rgba(148,163,184,0.8);margin-top:8px;font-size:0.95rem;}");
        out.println("    .grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(320px,1fr));");
        out.println("          gap:24px;max-width:1100px;margin:0 auto;}");
        out.println("    .section{background:rgba(255,255,255,0.04);");
        out.println("             backdrop-filter:blur(12px);");
        out.println("             border:1px solid rgba(255,255,255,0.1);");
        out.println("             border-radius:16px;padding:28px;");
        out.println("             box-shadow:0 8px 32px rgba(0,0,0,0.3);");
        out.println("             transition:transform .3s,box-shadow .3s;}");
        out.println("    .section:hover{transform:translateY(-4px);");
        out.println("                   box-shadow:0 16px 48px rgba(0,0,0,0.4);}");
        out.println("    .section-title{display:flex;align-items:center;gap:10px;");
        out.println("                   font-size:0.75rem;font-weight:700;letter-spacing:.15em;");
        out.println("                   text-transform:uppercase;color:rgba(148,163,184,0.7);");
        out.println("                   margin-bottom:20px;}");
        out.println("    .icon{font-size:1.2rem;}");
        out.println("    .row{display:flex;justify-content:space-between;align-items:center;");
        out.println("         padding:10px 0;border-bottom:1px solid rgba(255,255,255,0.06);}");
        out.println("    .row:last-child{border-bottom:none;}");
        out.println("    .row-label{font-size:0.85rem;color:rgba(148,163,184,0.8);}");
        out.println("    .row-value{font-family:'JetBrains Mono',monospace;font-size:0.9rem;");
        out.println("               font-weight:600;color:#e2e8f0;text-align:right;max-width:60%;}");
        out.println("    .badge{display:inline-block;padding:3px 10px;border-radius:50px;");
        out.println("           font-size:0.8rem;font-weight:600;}");
        out.println("    .badge-blue{background:rgba(56,189,248,0.15);color:#38bdf8;");
        out.println("                border:1px solid rgba(56,189,248,0.3);}");
        out.println("    .badge-purple{background:rgba(167,139,250,0.15);color:#a78bfa;");
        out.println("                  border:1px solid rgba(167,139,250,0.3);}");
        out.println("    .badge-green{background:rgba(52,211,153,0.15);color:#34d399;");
        out.println("                 border:1px solid rgba(52,211,153,0.3);}");
        out.println("    .bar-wrap{width:100%;background:rgba(255,255,255,0.08);");
        out.println("              border-radius:6px;height:6px;margin-top:6px;}");
        out.println("    .bar{height:6px;border-radius:6px;");
        out.println("         background:linear-gradient(90deg,#38bdf8,#818cf8);}");
        out.println("    .full-row{grid-column:1/-1;}");
        out.println("    .back-link{display:inline-block;margin:32px auto 0;");
        out.println("               padding:12px 28px;");
        out.println("               background:linear-gradient(135deg,#38bdf833,#818cf833);");
        out.println("               border:1px solid rgba(56,189,248,0.4);");
        out.println("               border-radius:50px;color:#93c5fd;text-decoration:none;");
        out.println("               font-size:0.9rem;transition:all .3s;}");
        out.println("    .back-link:hover{background:linear-gradient(135deg,#38bdf855,#818cf855);");
        out.println("                     transform:translateY(-2px);}");
        out.println("    .center{text-align:center;}");
        out.println("  </style>");
        out.println("</head>");
        out.println("<body>");
        out.println("  <div class=\"page-header\">");
        out.println("    <h1>&#x1F5A5;&#xFE0F; Інформація про сервер</h1>");
        out.println("    <p>Деталі апаратного та програмного забезпечення хосту</p>");
        out.println("  </div>");
        out.println("  <div class=\"grid\">");

        // --- CPU ---
        out.println("  <div class=\"section\">");
        out.println("    <div class=\"section-title\"><span class=\"icon\">&#x26A1;</span>Процесор (CPU)</div>");
        row(out, "Логічних ядер / потоків",
                "<span class=\"badge badge-blue\">" + logicalCores + " threads</span>");
        row(out, "Фізичних ядер",
                "<span class=\"badge badge-purple\">" + physCores + " cores</span>");
        row(out, "Архітектура", osArch);
        row(out, "Завантаженість CPU", cpuLoadStr);
        out.println("  </div>");

        // --- RAM ---
        long usedRam = totalRamBytes - freeRamBytes;
        int ramPct   = totalRamBytes > 0 ? (int)(usedRam * 100 / totalRamBytes) : 0;
        out.println("  <div class=\"section\">");
        out.println("    <div class=\"section-title\"><span class=\"icon\">&#x1F4BE;</span>Оперативна пам'ять (RAM)</div>");
        row(out, "Усього RAM", formatBytes(totalRamBytes));
        row(out, "Вільно RAM",  formatBytes(freeRamBytes));
        row(out, "Використано RAM", formatBytes(usedRam)
                + " <span style=\"color:rgba(148,163,184,0.6);font-size:.8rem\">(" + ramPct + "%)</span>");
        out.println("    <div class=\"bar-wrap\"><div class=\"bar\" style=\"width:" + ramPct + "%\"></div></div>");
        out.println("  </div>");

        // --- OS ---
        out.println("  <div class=\"section\">");
        out.println("    <div class=\"section-title\"><span class=\"icon\">&#x1F4BB;</span>Операційна система</div>");
        row(out, "ОС", osName);
        row(out, "Версія ОС", osVersion);
        row(out, "Ім'я хосту", hostname);
        out.println("  </div>");

        // --- JVM ---
        int jvmPct = jvmMaxMem > 0 ? (int)(jvmUsedMem * 100 / jvmMaxMem) : 0;
        out.println("  <div class=\"section\">");
        out.println("    <div class=\"section-title\"><span class=\"icon\">&#x2615;</span>Java Virtual Machine</div>");
        row(out, "Java версія", "<span class=\"badge badge-green\">" + javaVer + "</span>");
        row(out, "JVM", jvmName);
        row(out, "Heap (max)",    formatBytes(jvmMaxMem));
        row(out, "Heap (виділено)", formatBytes(jvmTotalMem));
        row(out, "Heap (використано)", formatBytes(jvmUsedMem));
        out.println("    <div class=\"bar-wrap\"><div class=\"bar\" style=\"width:" + jvmPct + "%\"></div></div>");
        out.println("  </div>");

        // --- Runtime ---
        out.println("  <div class=\"section\">");
        out.println("    <div class=\"section-title\"><span class=\"icon\">&#x23F1;&#xFE0F;</span>Runtime</div>");
        row(out, "Uptime сервера", uptimeStr);
        row(out, "Потоків JVM", String.valueOf(threadCount));
        row(out, "Часова зона", TimeZone.getDefault().getID());
        row(out, "Мова системи", Locale.getDefault().toString());
        out.println("  </div>");

        out.println("  </div>"); // grid

        out.println("  <div class=\"center\">");
        out.println("    <a href=\"/Lab01/ivanov\" class=\"back-link\">&larr; Назад до сторінки студента</a>");
        out.println("  </div>");
        out.println("</body>");
        out.println("</html>");
    }

    private static void row(PrintWriter out, String label, String value) {
        out.println("    <div class=\"row\">");
        out.println("      <span class=\"row-label\">" + label + "</span>");
        out.println("      <span class=\"row-value\">" + value + "</span>");
        out.println("    </div>");
    }

    private static String formatBytes(long bytes) {
        if (bytes <= 0) return "N/A";
        if (bytes < 1024) return bytes + " B";
        double kb = bytes / 1024.0;
        if (kb < 1024) return String.format("%.1f KB", kb);
        double mb = kb / 1024.0;
        if (mb < 1024) return String.format("%.1f MB", mb);
        double gb = mb / 1024.0;
        return String.format("%.2f GB", gb);
    }
}
