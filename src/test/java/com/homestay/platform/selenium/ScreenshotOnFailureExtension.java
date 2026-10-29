package com.homestay.platform.selenium;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Failure screenshot mechanism: runs right after a test method (before @AfterEach closes the
 * browser) and, if the test threw, saves a PNG to target/selenium-screenshots/.
 * Jenkins archives that folder as a build artefact.
 */
public class ScreenshotOnFailureExtension implements AfterTestExecutionCallback {

    static final Path DIR = Paths.get("target", "selenium-screenshots");

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isEmpty()) {
            return;
        }
        Object instance = context.getRequiredTestInstance();
        if (!(instance instanceof BaseSeleniumIT test) || test.getDriver() == null) {
            return;
        }
        WebDriver driver = test.getDriver();
        try {
            Files.createDirectories(DIR);
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            String name = context.getRequiredTestClass().getSimpleName() + "_"
                    + context.getRequiredTestMethod().getName() + "_" + stamp + ".png";
            Path src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE).toPath();
            Path target = DIR.resolve(name);
            Files.copy(src, target, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[selenium] Failure screenshot saved: " + target.toAbsolutePath()
                    + " (url: " + driver.getCurrentUrl() + ")");
        } catch (IOException | RuntimeException e) {
            System.err.println("[selenium] Could not save screenshot: " + e.getMessage());
        }
    }
}
