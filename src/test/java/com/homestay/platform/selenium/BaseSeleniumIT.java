package com.homestay.platform.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Map;

/**
 * Base class for the Selenium WebDriver suite (Task 9).
 *
 * By default each run starts the application on a random port with an in-memory database,
 * so the suite is self-contained on a laptop and inside Jenkins. To test an already deployed
 * instance instead, pass -Dapp.baseUrl=http://localhost:8080/homestay
 *
 * System properties:
 *   -Dbrowser=chrome|edge|firefox   (default chrome)
 *   -Dheadless=true|false           (default true)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ExtendWith(ScreenshotOnFailureExtension.class)
public abstract class BaseSeleniumIT {

    @LocalServerPort
    private int port;

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected String baseUrl;

    @BeforeEach
    void startBrowser() {
        String configured = System.getProperty("app.baseUrl", "");
        baseUrl = configured.isBlank() ? "http://localhost:" + port : configured.replaceAll("/$", "");
        driver = createDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void stopBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    public WebDriver getDriver() {
        return driver;
    }

    /**
     * Chrome/Edge show a "password found in a data breach" bubble after logging in with demo passwords
     * like host123. It steals focus and swallows the next click, so the password manager is switched off.
     */
    private static final Map<String, Object> NO_PASSWORD_MANAGER = Map.of(
            "credentials_enable_service", false,
            "profile.password_manager_enabled", false,
            "profile.password_manager_leak_detection", false);

    private static final String[] QUIET_BROWSER_ARGS = {
            "--disable-features=PasswordLeakDetection,PasswordManagerOnboarding,PasswordCheck",
            "--disable-save-password-bubble",
            "--disable-notifications",
            "--no-first-run",
            "--no-default-browser-check"};

    private static WebDriver createDriver() {
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "true"));
        String browser = System.getProperty("browser", "chrome").toLowerCase();
        switch (browser) {
            case "edge": {
                EdgeOptions o = new EdgeOptions();
                if (headless) o.addArguments("--headless=new");
                o.addArguments("--window-size=1366,900");
                o.addArguments(QUIET_BROWSER_ARGS);
                o.setExperimentalOption("prefs", NO_PASSWORD_MANAGER);
                return new EdgeDriver(o);
            }
            case "firefox": {
                FirefoxOptions o = new FirefoxOptions();
                if (headless) o.addArguments("-headless");
                return new FirefoxDriver(o);
            }
            default: {
                ChromeOptions o = new ChromeOptions();
                if (headless) o.addArguments("--headless=new");
                o.addArguments("--window-size=1366,900", "--no-sandbox", "--disable-dev-shm-usage");
                o.addArguments(QUIET_BROWSER_ARGS);
                o.setExperimentalOption("prefs", NO_PASSWORD_MANAGER);
                String chromeBinary = System.getProperty("chrome.binary", "");
                if (!chromeBinary.isBlank()) o.setBinary(chromeBinary);
                return new ChromeDriver(o);
            }
        }
    }

    // ------------------------------------------------------------------ helpers

    protected void open(String path) {
        driver.get(baseUrl + path);
    }

    /** Waits until the element is clickable, scrolls to it and clicks it (JavaScript click as a fallback). */
    protected void click(By locator) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", el);
        try {
            el.click();
        } catch (WebDriverException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }

    /** Clicks a button that submits a form and waits until the browser has loaded the next page. */
    protected void clickAndWaitForPage(By locator) {
        WebElement old = driver.findElement(By.tagName("html"));
        click(locator);
        try {
            wait.until(ExpectedConditions.stalenessOf(old));
        } catch (TimeoutException e) {
            // The click was swallowed: submit the button's form directly and wait again
            WebElement el = driver.findElement(locator);
            ((JavascriptExecutor) driver).executeScript("arguments[0].form.requestSubmit(arguments[0]);", el);
            wait.until(ExpectedConditions.stalenessOf(old));
        }
    }

    protected void loginAs(String username, String password) {
        open("/login");
        type(By.id("username"), username);
        type(By.id("password"), password);
        clickAndWaitForPage(By.id("login-btn"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("logout-btn")));
    }

    protected void logout() {
        clickAndWaitForPage(By.id("logout-btn"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("logout-msg")));
    }

    protected void type(By locator, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        el.clear();
        el.sendKeys(text);
    }

    /** Date inputs are locale dependent when typed, so set the ISO value directly. */
    protected void setDate(By locator, String isoDate) {
        WebElement el = driver.findElement(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", el, isoDate);
    }

    protected String text(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText().trim();
    }
}
