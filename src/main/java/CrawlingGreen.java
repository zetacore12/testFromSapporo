public class CrawlingGreen {
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

        saveCsv(filtered);
      
    } catch (Exception e) {
        logError("致命的エラー", e);
    }
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

  static List<Job> fetchJobs() throws IOException {
    Document doc = fetchDocument("https://www.green-japan.com/salary/400");

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
  
}
