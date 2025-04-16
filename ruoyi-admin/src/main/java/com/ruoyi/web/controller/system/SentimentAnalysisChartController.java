package com.ruoyi.web.controller.system;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@RestController
@RequestMapping("/pythonRun")
public class SentimentAnalysisChartController {

    @GetMapping("/SentimentAnalysisChartData")
    public ResponseEntity<String> runPythonScript() {
        try {
            // 指定 Python 文件路径
            String scriptPath = System.getProperty("user.dir") + "/pythonRun/pythonFile/SeparateData.py";

            // 通过 Runtime 执行 Python 脚本
            ProcessBuilder processBuilder = new ProcessBuilder("python", scriptPath);
            Process process = processBuilder.start();

            // 获取 Python 脚本输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            reader.close();

            // 返回 Python 输出结果给前端
            return ResponseEntity.ok(output.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error executing Python script.");
        }
    }
}
