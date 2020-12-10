package com.dabico.gseapp.github_service;

import com.dabico.gseapp.util.LongUtils;
import com.dabico.gseapp.util.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Duration;

@Service
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RepoHtmlPageSeleniumParserService {
    static final Logger logger = LoggerFactory.getLogger(RepoHtmlPageSeleniumParserService.class);

    static final String chromedriverFilePath;
    static final ChromeDriver driver;
    static final WebDriverWait webDriverWait;

    static {
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.contains("win")){
            chromedriverFilePath = "selenium/windows/chromedriver.exe";
        } else if (OS.contains("mac")){
            chromedriverFilePath = "selenium/macos/chromedriver";
        } else {
            chromedriverFilePath = "selenium/linux/chromedriver";
        }

        System.setProperty("webdriver.chrome.driver", chromedriverFilePath);
        System.setProperty("webdriver.chrome.silentOutput", "true");
        DesiredCapabilities capabilities = new DesiredCapabilities();
        ChromeDriverService service = new ChromeDriverService.Builder().usingDriverExecutable(new File(chromedriverFilePath)).build();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--headless");
        options.setExperimentalOption("useAutomationExtension", false);
        options.addArguments("start-maximized");
        options.addArguments("disable-infobars");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-dev-shm-usage");
        capabilities.setCapability(ChromeOptions.CAPABILITY,options);
        driver = new ChromeDriver(service,options);
        webDriverWait = new WebDriverWait(driver, Duration.ofSeconds(30), Duration.ofMillis(250));
    }

    public Long mineWatchersSelenium(String repoURL, int index){
        try {
            String watchersReg = String.format(RepoHtmlTags.watchersTemplateReg, index);
            String watchersAlt = String.format(RepoHtmlTags.watchersTemplateAlt, index);
            return LongUtils.getLongValue(StringUtils.removeFromEnd(getPageElement(repoURL, watchersReg, watchersAlt).getAttribute("aria-label"),35));
        } catch (TimeoutException ex) {
            logger.error("Number of watchers could not be mined at this time!");
            logger.error("Reason: Selenium could not locate the specified element");
            return null;
        }
    }

    public Long mineCommitsSelenium(String repoURL){
        return mineCommitsSelenium(0, repoURL);
    }

    public Long mineCommitsSelenium(int attempt, String repoURL){
        try {
            return LongUtils.getLongValue(getPageElement(repoURL, RepoHtmlTags.commitsReg, RepoHtmlTags.commitsAlt).getText());
        } catch (TimeoutException ex) {
            logger.error("Selenium could not locate number of commits!");
            logger.error("Retrying");
            if (attempt > 2){
                logger.error("Number of commits could not be mined at this time!");
                return null;
            } else {
                return mineCommitsSelenium(++attempt, repoURL);
            }
        }
    }

    public Long mineBranchesSelenium(String repoURL){
        return mineBranchesSelenium(0, repoURL);
    }

    public Long mineBranchesSelenium(int attempt, String repoURL){
        try {
            return LongUtils.getLongValue(getPageElement(repoURL, RepoHtmlTags.branchesReg, RepoHtmlTags.branchesAlt).getText());
        } catch (TimeoutException ex) {
            logger.error("Selenium could not locate number of branches!");
            logger.error("Retrying");
            if (attempt > 2){
                logger.error("Number of branches could not be mined at this time!");
                return null;
            } else {
                return mineBranchesSelenium(++attempt, repoURL);
            }
        }
    }

    public Long mineReleasesSelenium(String repoURL){
        return mineReleasesSelenium(0, repoURL);
    }

    public Long mineReleasesSelenium(int attempt, String repoURL){
        try {
            return LongUtils.getLongValue(getPageElement(repoURL, RepoHtmlTags.releasesReg, RepoHtmlTags.releasesAlt).getText());
        } catch (TimeoutException ex) {
            logger.error("Selenium could not locate number of releases!");
            logger.error("Retrying");
            if (attempt > 2){
                logger.error("Number of releases could not be mined at this time!");
                return null;
            } else {
                return mineReleasesSelenium(++attempt, repoURL);
            }
        }
    }

    public Long mineContributorsSelenium(int index, String repoURL){
        try {
            String contributorsReg = String.format(RepoHtmlTags.contribTemplateReg, index);
            String contributorsAlt = String.format(RepoHtmlTags.contribTemplateAlt, index);
            return LongUtils.getLongValue(getPageElement(repoURL, contributorsReg,contributorsAlt).getText());
        } catch (NumberFormatException ex){
            String linkReg = String.format(RepoHtmlTags.linkTemplateReg, index);
            String linkAlt = String.format(RepoHtmlTags.linkTemplateAlt, index);
            if (StringUtils.removeFromStart(ex.getMessage(),18).equals("\"5000+\"")){
                long contributors = 11 + LongUtils.getLongValue(StringUtils.removeFromStartAndEnd(getPageElement(repoURL, linkReg,linkAlt).getText(),2,13));
                return contributors;
            }
            return null;
        } catch (TimeoutException ex) {
            logger.error("Number of contributors could not be mined at this time!");
            logger.error("Reason: Selenium could not locate the specified element");
            return null;
        }
    }

    public WebElement getPageElement(String repoURL, String elementReg, String elementAlt){
        driver.get(repoURL);
        try {
            webDriverWait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(elementReg)));
            webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(elementReg)));
            return driver.findElementByCssSelector(elementReg);
        } catch (TimeoutException ex) {
            webDriverWait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(elementAlt)));
            webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(elementAlt)));
            return driver.findElementByCssSelector(elementAlt);
        }
    }

}
