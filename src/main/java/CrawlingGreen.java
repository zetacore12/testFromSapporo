import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

public class CrawlingGreen {

    private static final String TARGET_URL = "https://www.green-japan.com/salary/400";
    private static final String CSV_PATH = "jobs.csv";

    public static void main(String[] args) {
        try {
            List<Job> jobs = fetchJobs();

            if (jobs.isEmpty()) {
                logError("求人が0件。HTML構造変更の可能性あり");
                return;
            }

            List<Job> filtered = filterJobs(jobs);

            logInfo("取得件数: " + jobs.size());
            logInfo("フィルタ後件数: " + filtered.size());

            List<Job> newJobs = filterNewJobs(filtered);

            logInfo("新着件数: " + newJobs.size());

            if (!newJobs.isEmpty()) {
                saveCsv(newJobs);
            }

        } catch (Exception e) {
            logError("致命的エラー", e);
        }
    }

    // =========================
    // 取得処理
    // =========================
    static List<Job> fetchJobs() throws IOException {
        Document doc = fetchDocument(TARGET_URL);

        Elements jobs = doc.select("div.job-offer-card");

        if (jobs.isEmpty()) {
            logError("job-offer-card が0件。セレクタ変更の可能性");
            saveDebugHtml(doc);
            return Collections.emptyList();
        }

        List<Job> result = new ArrayList<>();

        for (Element job : jobs) {
            try {
                String title = job.select(".job-title").text();
                String company = job.select(".company-name").text();
                String location = job.select(".location").text();
                String salary = job.select(".salary").text();
                String link = job.select("a").attr("abs:href");

                if (title.isEmpty() || link.isEmpty()) {
                    logError("必須項目欠損: " + job.text());
                    continue;
                }

                result.add(new Job(title, company, location, salary, link));

            } catch (Exception e) {
                logError("求人パース失敗: " + job.text(), e);
            }
        }

        return result;
    }

    static Document fetchDocument(String url) throws IOException {
        Connection.Response res = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(10000)
                .execute();

        if (res.statusCode() != 200) {
            throw new IOException("HTTPエラー: " + res.statusCode());
        }

        return res.parse();
    }

    // =========================
    // フィルタ
    // =========================
    static List<Job> filterJobs(List<Job> jobs) {
        return jobs.stream()
                .filter(j -> j.location.contains("札幌"))
                .filter(j -> j.title.contains("エンジニア") || j.title.contains("開発"))
                .toList();
    }

    // =========================
    // 新着判定
    // =========================
    static List<Job> filterNewJobs(List<Job> jobs) {
        Set<String> existingUrls = loadExistingUrls();

        List<Job> newJobs = new ArrayList<>();

        for (Job job : jobs) {
            if (!existingUrls.contains(job.link)) {
                newJobs.add(job);
            }
        }

        return newJobs;
    }

    static Set<String> loadExistingUrls() {
        Set<String> urls = new HashSet<>();

        try {
            if (!Files.exists(Path.of(CSV_PATH))) return urls;

            List<String> lines = Files.readAllLines(Path.of(CSV_PATH), StandardCharsets.UTF_8);

            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    urls.add(parts[4]);
                }
            }

        } catch (Exception e) {
            logError("既存CSV読み込み失敗", e);
        }

        return urls;
    }

    // =========================
    // 出力
    // =========================
    static void saveCsv(List<Job> jobs) {
        try (FileWriter fw = new FileWriter(CSV_PATH, true)) {
            for (Job j : jobs) {
                fw.write(String.join(",",
                        LocalDate.now().toString(),
                        escape(j.title),
                        escape(j.company),
                        escape(j.location),
                        escape(j.salary),
                        j.link
                ) + "\n");
            }
        } catch (IOException e) {
            logError("CSV書き込み失敗", e);
        }
    }

    static String escape(String s) {
        return s.replace(",", " ");
    }

    // =========================
    // デバッグHTML保存
    // =========================
    static void saveDebugHtml(Document doc) {
        try {
            Files.writeString(
                    Path.of("debug.html"),
                    doc.outerHtml(),
                    StandardCharsets.UTF_8
            );
            logInfo("debug.html に保存しました");
        } catch (IOException e) {
            logError("HTML保存失敗", e);
        }
    }

    // =========================
    // ログ
    // =========================
    static void logInfo(String msg) {
        System.out.println("[INFO] " + msg);
    }

    static void logError(String msg) {
        System.err.println("[ERROR] " + msg);
    }

    static void logError(String msg, Exception e) {
        System.err.println("[ERROR] " + msg);
        e.printStackTrace();
    }

// =========================
// モデル
// =========================
static class Job {
    String title;
    String company;
    String location;
    String salary;
    String link;

    Job(String title, String company, String location, String salary, String link) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.salary = salary;
        this.link = link;
    }
  }
}
