package com.hs.documentprocessor;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DocumentProcessor {

    public static void main(String[] args) throws InterruptedException {
        Long startTime = System.currentTimeMillis();
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("user-agent= Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.131 Safari/537.36");
        chromeOptions.addArguments("window-size=1440,1060");
        chromeOptions.setHeadless(true);
//        chromeOptions.addArguments("headless");
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver");
        WebDriver driver = new ChromeDriver(chromeOptions);
        TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
        try {
            driver.get("http://10.57.0.166:8082/webrdp-web/_saas/_app/zyonline.app/index.db/portal_todo.page?open#");
            Thread.sleep(3000);
            Cookie cookie1 = new Cookie("CASPRIVACY",
                    "",
                    "10.57.0.166",
                    "/cas/",
                    null,
                    false);
            Cookie cookie2 = new Cookie("CASTGC",
                    "TGT-58195-9b9eKJIZNuvFRerY6thoGMROEOIuM2bI3m4YIh09hvFDw5zKEV-cas",
                    "10.57.0.166",
                    "/cas/",
                    null,
                    false);
            Cookie cookie3 = new Cookie("JSESSIONID",
                    "A0204A72627134F12AC2E68B4899AE5A",
                    "10.57.0.166",
                    "/cas/",
                    null,
                    false);
            Cookie cookie4 = new Cookie("JSESSIONID",
                    "EF01F1A44765ADECCA5FFB08E459254E",
                    "10.57.0.166",
                    "/webrdp-web",
                    null,
                    false);
            WebDriver.Options options = driver.manage();
            options.addCookie(cookie1);
            options.addCookie(cookie2);
            options.addCookie(cookie3);
            options.addCookie(cookie4);
            driver.navigate().refresh();
            Thread.sleep(3000);

            System.out.println("开始处理公文：");
            WebDriverWait wait = new WebDriverWait(driver, 10);
            int countFailure = 0;
            while (true) {
                if (countFailure > 10) {
                    System.out.println("处理失败公文数大于10，请检查脚本");
                    return;
                }
                // 获取当前剩余待处理公文数
                try {
                    byte[] imgByte = takesScreenshot.getScreenshotAs(OutputType.BYTES);
                    FileOutputStream fileOutputStream = new FileOutputStream(new File("step0.png"));
                    fileOutputStream.write(imgByte, 0, imgByte.length);
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"count_todo\"]")));
                    int countTodo = Integer.parseInt(driver.findElement(By.xpath("//*[@id=\"count_todo\"]")).getText());
                    if (countTodo == 0) {
                        return;
                    }
                    System.out.println("剩余待处理公文数：" + countTodo);
                } catch (Exception e) {
                    return;
                }

                try {
                    System.out.println("1. 选中第一条公文");
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"con_one_1\"]/li[1]/a")));
                    byte[] imgByte = takesScreenshot.getScreenshotAs(OutputType.BYTES);
                    FileOutputStream fileOutputStream = new FileOutputStream(new File("step1.png"));
                    fileOutputStream.write(imgByte, 0, imgByte.length);
                    driver.findElement(By.xpath("//*[@id=\"con_one_1\"]/li[1]/a")).click();
                    System.out.println("2. 进入公文页面");
                    // 等待页面动画
                    Thread.sleep(1000);
                    List<String> tabs = new ArrayList<>(driver.getWindowHandles());
                    driver.switchTo().window(tabs.get(1));
                    System.out.println("3. 点击提交按钮");
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("提交")));
                    driver.findElement(By.linkText("提交")).click();
                    System.out.println("4. 填写意见：阅");
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"ieadList\"]")));
                    // 等待加载动画
                    Thread.sleep(1000);
                    driver.findElement(By.xpath("//*[@id=\"handleWin\"]/div/div/div[2]/div/div[2]/div[1]/div[1]/table/tbody/tr[3]/td/table/tbody/tr[1]/td[2]/textarea")).sendKeys("阅");
                    Thread.sleep(500);
                    System.out.println("5. 提交意见");
                    driver.findElement(By.xpath("//*[@id=\"handleWin\"]/div/div/div[3]/button[2]")).click();
                    // 等待当前tab页关闭
                    while (true) {
                        Thread.sleep(1000);
                        try {
                            driver.findElement(By.xpath("//*[@id=\"handleWin\"]/div/div/div[3]/button[2]")).click();
                        } catch (Exception e) {
                            break;
                        }
                    }
                    System.out.println("6. 返回索引页面");
                    backToFirstTab(driver);
                } catch (Exception e) {
                    System.out.println(e.toString());
                    countFailure++;
                    backToFirstTab(driver);
                }
            }
        } finally {
            driver.quit();
            Long totalTime = System.currentTimeMillis() - startTime;
            System.out.println("公文处理完毕, 耗时：" + totalTime/1000 + "秒");
        }
    }

    private static void backToFirstTab(WebDriver driver) throws InterruptedException {
        List<String> tabsToClose = new ArrayList<>(driver.getWindowHandles());
        String firstTab = tabsToClose.remove(0);
        for (String tab : tabsToClose) {
            driver.switchTo().window(tab);
            driver.close();
            // 等待标签页关闭
            Thread.sleep(500);
        }
        // 切换至第一个标签页
        driver.switchTo().window(firstTab);
        Thread.sleep(1000);
    }

}
