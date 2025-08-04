package com.lieairecode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import atu.testrecorder.ATUTestRecorder;
import atu.testrecorder.exceptions.ATUTestRecorderException;
import io.github.bonigarcia.wdm.WebDriverManager;

public class TestStSauceLabs {
	
	private static WebDriver driver;
	private static ExtentReports extent;
	private static ATUTestRecorder recorder;
	private static com.aventstack.extentreports.ExtentTest test;
	private static Properties prop;
	private static WebDriverWait wait;
	private static final String VIDEO_LOCATION = "C:\\Users\\sliti\\eclipse-workspace\\CoursSeleniumWebDriverAvancee\\target\\Videos";
	public String methodName;

	@BeforeClass
	public String getActualDateTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yy_MM_dd_HH_mm_ss");
		return dateFormat.format(new Date());
	}
	
	public String getvideoFileName(String methodName) {
		return "Video_" + methodName + "_" + getActualDateTime();
	}

	private void autoRecorderStart() throws ATUTestRecorderException {
		recorder = new ATUTestRecorder(VIDEO_LOCATION, getvideoFileName(methodName), false);
		recorder.start();
	}

	@BeforeTest
	public void setupbeforeTest() throws IOException, ATUTestRecorderException {
		prop = new Properties();
		FileInputStream fis = new FileInputStream("src/test/resources/Data/data.properties");
		prop.load(fis);

		new File("target/Spark").mkdirs();

		String reportName = "target/Spark/SparkReport_" + getActualDateTime() + ".html";
		ExtentSparkReporter spark = new ExtentSparkReporter(reportName);
		spark.config().setTheme(Theme.DARK); // Choix du thème (DARK, STANDARD)
		spark.config().setDocumentTitle("SauceDemo Test Report"); // Titre du document
		spark.config().setReportName("Automated Test Suite Report"); // Nom du rapport
		spark.config().setTimeStampFormat("dd/MM/yyyy HH:mm:ss"); // Format de l'horodatage
		spark.config().setReportName("Asma's Report"); // Remplacez par votre nom
		spark.config().setDocumentTitle("Formation SpaceToon");
		// Optional: Add a footer or header
		extent = new ExtentReports();
		extent.attachReporter(spark);
		String browser = prop.getProperty("browser", "chrome");
		if (browser.equalsIgnoreCase("chrome")) {
			WebDriverManager.chromedriver().setup();
			driver = new ChromeDriver();
		} else if (browser.equalsIgnoreCase("firefox")) {
			WebDriverManager.firefoxdriver().setup();
			driver = new FirefoxDriver();
		} else {
			throw new IllegalArgumentException("Browser not supported: " + browser);
		}

		driver.get(prop.getProperty("baseUrl"));
		
		driver.manage().window().maximize();
		wait = new WebDriverWait(driver, Duration.ofSeconds(30));
	}

	private void autoRecorderFin() throws ATUTestRecorderException {
		if (recorder != null) {
			recorder.stop();
		}
	}

	private String getScreenshotPath(String imageName) throws IOException {
		String imageLocation = "C:\\Users\\sliti\\eclipse-workspace\\CoursSeleniumWebDriverAvancee\\target\\Screenshots\\";
		String actualImageName = imageLocation + imageName + "-" + getActualDateTime() + ".png";
		File sourceFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		File destFileImg = new File(actualImageName);
		FileUtils.copyFile(sourceFile, destFileImg);
		return actualImageName;
	}

	@BeforeMethod 

	public void setupbeforeMethode(ITestResult result) {
		this.methodName = result.getMethod().getMethodName();
		try {
			autoRecorderStart();
		} catch (ATUTestRecorderException e) {
			e.printStackTrace();
		}
	}

	@Test(priority = 1)
	public void failedLoginTest() throws IOException, ATUTestRecorderException {
		test = extent.createTest("Failed Login Test");
		try {
			WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
			username.sendKeys("fake_user");
			
			WebElement password = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
			password.sendKeys("wrong_password");
			WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));
			loginButton.click();
			WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.xpath("//*[contains(text(),'Epic sadface: Username and password')]")));
			Assert.assertTrue(errorMessage.isDisplayed(), "Error message not displayed!");
			test.pass("Login failed as expected with an incorrect username and password.");
		} catch (AssertionError e) {
			test.fail("Error message not displayed: " + e.getMessage());
			throw e;
		}
	}

	@Test(priority = 2)
	public void lockedOutUserTest() throws IOException, ATUTestRecorderException {
		test = extent.createTest("Locked Out User Test");
		try {
			WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
			username.clear();
			username.sendKeys("locked_out_user");
			WebElement password = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
			password.clear();
			password.sendKeys("secret_sauce");
			WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));
			loginButton.click();
			WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.xpath("//*[contains(text(),'Epic sadface: Sorry, this user has been locked out')]")));
			Assert.assertTrue(errorMessage.isDisplayed(), "Error message not displayed!");
			test.pass("Locked out user error displayed correctly.");
		} catch (AssertionError e) {
			test.fail("Error message not displayed: " + e.getMessage());
			throw e;
		}
	}

	@Test(priority = 3)
	public void validLoginTest() throws IOException, ATUTestRecorderException {
		test = extent.createTest("Valid Login Test");
		try {
			WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
			username.clear();
			username.sendKeys("standard_user");
			WebElement password = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
			password.clear();
			password.sendKeys("secret_sauce");
			WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));
			loginButton.click();
			WebElement productsPage = wait
					.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[text()='Products']")));
			Assert.assertTrue(productsPage.isDisplayed(), "Products page not displayed!");
			test.pass("Logged in successfully as a standard user.");
		} catch (AssertionError e) {
			test.fail("Login failed: " + e.getMessage());
			throw e;
		}
	}

	@Test(priority = 4)
	public void addToCartTest() throws IOException, ATUTestRecorderException {
		test = extent.createTest("Add to Cart Test");
		try {
			WebElement firstProduct = wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.xpath("/html/body/div/div/div/div[2]/div/div/div/div[1]/div[2]/div[2]/button")));
			firstProduct.click();
			WebElement cartBadge = wait
					.until(ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge")));
			Assert.assertEquals(cartBadge.getText(), "1", "Product not added to cart!");
			test.pass("Product successfully added to cart.");
		} catch (AssertionError e) {
			test.fail("Failed to add product to cart: " + e.getMessage());
			throw e;
		}
	}

	@Test(priority = 5)
	public void viewCartTest() throws IOException, ATUTestRecorderException {
		test = extent.createTest("View Cart Test");
		try {
			WebElement cartButton = wait
					.until(ExpectedConditions.elementToBeClickable(By.className("shopping_cart_link")));
			cartButton.click();
			WebElement cartTitle = wait
					.until(ExpectedConditions.visibilityOfElementLocated(By.id("cart_contents_container")));
			Assert.assertTrue(cartTitle.isDisplayed(), "Cart title not displayed!");
			test.pass("Successfully viewed the cart.");
		} catch (AssertionError e) {
			test.fail("Failed to view cart: " + e.getMessage());
			throw e;
		}
	}

	@Test(priority = 6)
	public void removeFromCartTest() throws IOException, ATUTestRecorderException {
		test = extent.createTest("Remove from Cart Test");
		try {
			// Assurez-vous que l'utilisateur est connecté avant de retirer un produit
			WebElement removeButton = wait
					.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".cart_button")));
			removeButton.click();
			wait.until(ExpectedConditions.invisibilityOf(removeButton));
			test.pass("Product successfully removed from cart.");
		} catch (AssertionError e) {
			test.fail("Failed to remove product from cart: " + e.getMessage());
			throw e;
		}
	}

	@Test(priority = 7)
	public void checkoutTest() throws IOException, ATUTestRecorderException {
		test = extent.createTest("Checkout Test");
		try {
			WebElement checkoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout")));
			checkoutButton.click();
			WebElement firstName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
			firstName.sendKeys(prop.getProperty("firstname"));
			WebElement lastName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("last-name")));
			lastName.sendKeys(prop.getProperty("lastname"));
			WebElement postalCode = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("postal-code")));
			postalCode.sendKeys(prop.getProperty("postalecode"));
			WebElement continueButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("continue")));
			continueButton.click();
			WebElement buttonFinish = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("finish")));
			buttonFinish.click();
			WebElement checkoutComplete = wait
					.until(ExpectedConditions.visibilityOfElementLocated(By.className("complete-header")));
			Assert.assertTrue(checkoutComplete.isDisplayed(), "Checkout was not successful!");
			Assert.assertEquals(checkoutComplete.getText(), "Thank you for your order!");
			test.pass("Checkout completed successfully.");
		} catch (AssertionError e) {
			test.fail("Checkout failed: " + e.getMessage());
			throw e;
		}
	}

	@Test(priority = 8)
	public void filterProductsTest() throws IOException, ATUTestRecorderException {
		test = extent.createTest("Filter Products Test");
		try {
			WebElement backToHome = driver.findElement(By.id("back-to-products"));
			backToHome.click();
			WebElement filterDropdown = wait
					.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
			filterDropdown.click();
			WebElement lowToHighOption = wait.until(ExpectedConditions
					.elementToBeClickable(By.xpath("//option[contains(text(),'Price (low to high)')]")));
			lowToHighOption.click();
			List<WebElement> productPrices = driver.findElements(By.className("inventory_item_price"));
			boolean isSorted = true;
			double previousPrice = 0.0;
			for (WebElement priceElement : productPrices) {
				double currentPrice = Double.parseDouble(priceElement.getText().replace("$", ""));
				if (currentPrice < previousPrice) {
					isSorted = false;
					break;
				}
				previousPrice = currentPrice;
			}
			Assert.assertTrue(isSorted, "Les produits ne sont pas triés par prix!");
			test.pass("Filtre des produits appliqué avec succès.");
		} catch (AssertionError e) {
			test.fail("Échec de l'application du filtre des produits: " + e.getMessage());
			throw e;
		}
	}

	@Test(priority = 9)
	public void addToCartInexistentProductTest() throws IOException, ATUTestRecorderException {
		test = extent.createTest("Add to Cart Inexistent Product Test");
		try {
			WebElement firstProduct = wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.xpath("/html/body/div/div/div/div[2]/div/div/div/div[1]/div[2]/div[2]/button[19]"))); // Ajustez
																												// le
																												// XPath
																												// pour
																												// un
																												// produit
																												// inexistant
			firstProduct.click();
			WebElement cartBadge = wait
					.until(ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge")));
			Assert.assertEquals(cartBadge.getText(), "1", "Product not added to cart!");
			test.pass("Product successfully added to cart.");
		} catch (AssertionError e) {
			test.fail("Failed to add product to cart: " + e.getMessage());
			throw e;
		}
	}

	@Test(priority = 10)
	public void logoutTest() throws IOException, ATUTestRecorderException {
		test = extent.createTest("Logout Test");
		try {
			WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
			menuButton.click();
			WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
			logoutButton.click();
			WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
			Assert.assertTrue(loginButton.isDisplayed(), "Login button not displayed after logout!");
			test.pass("Logged out successfully.");
		} catch (AssertionError e) {
			test.fail("Logout failed: " + e.getMessage());
			throw e;
		}
	}

	@AfterMethod
	public void tearDown(ITestResult result) throws IOException, ATUTestRecorderException {
		
		test.addScreenCaptureFromPath(getScreenshotPath(""));

		test.log(Status.INFO,
				"Vidéo ajoutée au rapport: <a href='" + VIDEO_LOCATION + "'>Cliquez ici pour voir la vidéo</a>");

		if (result.getStatus() == ITestResult.SUCCESS) {
			test.log(Status.PASS, "Test passed.");
		} else if (result.getStatus() == ITestResult.FAILURE) {
			test.log(Status.FAIL, "Test failed: " + result.getThrowable().getMessage());
		} else {
			test.log(Status.SKIP, "Test skipped.");
		}
		autoRecorderFin();
	}

	@AfterTest
	public void tearDownTest() throws ATUTestRecorderException {
	
		extent.flush();
		if (driver != null) {
			driver.quit();
		}
	}
}
