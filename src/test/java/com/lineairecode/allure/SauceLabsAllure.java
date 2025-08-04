package com.lineairecode.allure;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.tika.io.IOUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;
import io.qameta.allure.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import atu.testrecorder.ATUTestRecorder;
import atu.testrecorder.exceptions.ATUTestRecorderException;

public class SauceLabsAllure {

	private static WebDriver driver;
	private static Properties prop;
	private static WebDriverWait wait;
	private static ATUTestRecorder recorder;
	private static final String VIDEO_LOCATION = "C:\\Users\\sliti\\eclipse-workspace\\CoursSeleniumWebDriverAvancee\\target\\AllureVideos";
	public String methodName;

	@BeforeClass
	public String getActualDateTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yy_MM_dd_HH_mm_ss");
		return dateFormat.format(new Date());
	}

	public String getVideoFileName(String methodName) {

		return "Video_" + methodName + "_" + getActualDateTime() + ".mov";
	}

	private void autoRecorderStart() throws ATUTestRecorderException {
		recorder = new ATUTestRecorder(VIDEO_LOCATION, getVideoFileName(methodName), false);
		recorder.start();
	}

	@BeforeTest
	@Description("Initialisation des configurations du test et du navigateur.")
	@Step("Configuration du navigateur et chargement des propriétés.")
	public void setupBeforeTest() throws IOException, ATUTestRecorderException {
		prop = new Properties();
		FileInputStream fis = new FileInputStream("src/test/resources/Data/data.properties");
		prop.load(fis);

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

	private void autoRecorderStop() throws ATUTestRecorderException {
		if (recorder != null) {
			recorder.stop();
		}
	}

	@Attachment(value = "Screenshot", type = "image/png")
	public byte[] saveScreenshotOnFailure(WebDriver driver) throws IOException {
		return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
	}

	@Attachment()
	public byte[] attachVideoToAllure(String VIDEO_LOCATION) throws IOException {
		File videoFile = new File(VIDEO_LOCATION);
		return FileUtils.readFileToByteArray(videoFile);
	}

	@BeforeMethod
	public void setupBeforeMethod(ITestResult result) {
		this.methodName = result.getMethod().getMethodName();
		try {
			autoRecorderStart();
		} catch (ATUTestRecorderException e) {
			e.printStackTrace();
		}
	}

	// Test 1 : Connexion échouée
	@Test(priority = 1)
	@Description("Test de connexion avec des informations erronées.")
	@Epic("Epic 1")
	@Feature("Connexion")
	@Story("Connexion échouée")
	@Severity(SeverityLevel.CRITICAL)
	public void failedLoginTest() throws IOException {
		try {
			WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
			username.sendKeys("fake_user");
			username.clear();
			WebElement password = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
			password.clear();
			password.sendKeys("wrong_password");
			WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));
			loginButton.click();
			WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.xpath("//*[contains(text(),'Epic sadface: Username and password')]")));
			Assert.assertTrue(errorMessage.isDisplayed(), "Error message not displayed!");
		} catch (AssertionError e) {
			saveScreenshotOnFailure(driver); // Capture screenshot in case of failure
			throw e;
		}
	}

	// Test 2 : Connexion réussie
	@Test(priority = 2)
	@Description("Test de connexion avec des informations valides.")
	@Epic("Epic 1")
	@Feature("Connexion")
	@Story("Connexion réussie")
	@Severity(SeverityLevel.BLOCKER)
	public void successfulLoginTest() throws IOException {
		WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
		username.clear();
		username.sendKeys("standard_user");
		WebElement password = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
		password.clear();
		password.sendKeys("secret_sauce");
		WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));
		loginButton.click();
		WebElement inventoryPage = wait
				.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
		Assert.assertTrue(inventoryPage.isDisplayed(), "Inventory page not displayed!");
	}

	// Test 3 : Vérifier la présence d'un article dans l'inventaire
	@Test(priority = 3)
	@Description("Vérification de la présence d'un article spécifique dans l'inventaire.")
	@Epic("Epic 2")
	@Feature("Inventaire")
	@Story("Vérification d'articles")
	@Severity(SeverityLevel.NORMAL)
	public void checkItemInInventoryTest() {
		WebElement item = wait
				.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[text()='Sauce Labs Backpack']")));
		Assert.assertTrue(item.isDisplayed(), "Sauce Labs Backpack not found in inventory!");
	}

	// Test 4 : Ajout d'un article au panier
	@Test(priority = 4)
	@Description("Test pour ajouter un article au panier.")
	@Epic("Epic 2")
	@Feature("Panier")
	@Story("Ajout d'article")
	@Severity(SeverityLevel.NORMAL)
	public void addItemToCartTest() {
		WebElement addToCartButton = wait
				.until(ExpectedConditions.elementToBeClickable(By.id("add-to-cart-sauce-labs-backpack")));
		addToCartButton.click();
		WebElement cartBadge = wait
				.until(ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge")));
		Assert.assertEquals(cartBadge.getText(), "1", "Cart badge count is incorrect!");
	}

	// Test 5 : Suppression d'un article du panier
	@Test(priority = 5)
	@Description("Test pour supprimer un article du panier.")
	@Epic("Epic 2")
	@Feature("Panier")
	@Story("Suppression d'article")
	@Severity(SeverityLevel.NORMAL)
	public void removeItemFromCartTest() {
		WebElement removeButton = wait
				.until(ExpectedConditions.elementToBeClickable(By.id("remove-sauce-labs-backpack")));
		removeButton.click();
		WebElement cartBadge = driver.findElement(By.className("shopping_cart_badge"));
		Assert.assertFalse(cartBadge.isDisplayed(), "Cart badge still visible after removing item!");
	}

	// Test 6 : Finaliser l'achat
	@Test(priority = 6)
	@Description("Test pour finaliser l'achat.")
	@Epic("Epic 3")
	@Feature("Achat")
	@Story("Finalisation de l'achat")
	@Severity(SeverityLevel.BLOCKER)
	public void checkoutTest() {
		WebElement cartButton = driver.findElement(By.className("shopping_cart_link"));
		cartButton.click();
		WebElement checkoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout")));
		checkoutButton.click();
		WebElement firstName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
		firstName.sendKeys("John");
		WebElement lastName = driver.findElement(By.id("last-name"));
		lastName.sendKeys("Doe");
		WebElement postalCode = driver.findElement(By.id("postal-code"));
		postalCode.sendKeys("12345");
		WebElement continueButton = driver.findElement(By.id("continue"));
		continueButton.click();
		WebElement finishButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("finish")));
		finishButton.click();
		WebElement completeHeader = wait
				.until(ExpectedConditions.visibilityOfElementLocated(By.className("complete-header")));
		Assert.assertEquals(completeHeader.getText(), "THANK YOU FOR YOUR ORDER",
				"Order completion message is incorrect!");
	}

	// Test 7 : Afficher les détails d'un article
	@Test(priority = 7)
	@Description("Test pour afficher les détails d'un article.")
	@Epic("Epic 2")
	@Feature("Détails de l'article")
	@Story("Affichage des détails")
	@Severity(SeverityLevel.MINOR)
	public void viewItemDetailsTest() {
		WebElement item = wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[text()='Sauce Labs Bike Light']")));
		item.click();
		WebElement itemName = wait
				.until(ExpectedConditions.visibilityOfElementLocated(By.className("inventory_details_name")));
		Assert.assertEquals(itemName.getText(), "Sauce Labs Bike Light", "Item details are incorrect!");
	}

	// Test 8 : Revenir à la page d'inventaire après avoir vu les détails
	@Test(priority = 8)
	@Description("Test pour revenir à la page d'inventaire après la consultation des détails d'un article.")
	@Epic("Epic 2")
	@Feature("Navigation")
	@Story("Retour à l'inventaire")
	@Severity(SeverityLevel.MINOR)
	public void backToInventoryTest() {
		WebElement backButton = wait
				.until(ExpectedConditions.elementToBeClickable(By.className("inventory_details_back_button")));
		backButton.click();
		WebElement inventoryPage = wait
				.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
		Assert.assertTrue(inventoryPage.isDisplayed(), "Failed to navigate back to inventory page!");
	}

	// Test 9 : Déconnexion de l'application
	@Test(priority = 9)
	@Description("Test pour déconnexion de l'application.")
	@Epic("Epic 4")
	@Feature("Déconnexion")
	@Story("Déconnexion")
	@Severity(SeverityLevel.CRITICAL)
	public void logoutTest() {
		WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
		menuButton.click();
		WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
		logoutLink.click();
		WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
		Assert.assertTrue(loginButton.isDisplayed(), "Login button not displayed after logout!");
	}

	// Test 10 : Trier les articles par prix
	@Test(priority = 10)
	@Description("Test pour trier les articles par prix croissant.")
	@Epic("Epic 2")
	@Feature("Tri")
	@Story("Tri par prix")
	@Severity(SeverityLevel.NORMAL)
	public void sortByPriceTest() {
		WebElement sortDropdown = wait
				.until(ExpectedConditions.visibilityOfElementLocated(By.className("product_sort_container")));
		sortDropdown.click();
		WebElement lowToHighOption = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//option[@value='lohi']")));
		lowToHighOption.click();
		WebElement firstItem = wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.xpath("//div[@class='inventory_item'][1]//div[@class='inventory_item_price']")));
		Assert.assertEquals(firstItem.getText(), "$7.99", "First item is not the cheapest after sorting by price!");
	}

	@AfterMethod
	public void cleanupAfterMethod(ITestResult result) {
	    try {
	        saveScreenshotOnFailure(driver);
	        autoRecorderStop();

	        String videoFilePath = getVideoFileName(methodName);
	        System.out.println("Path to video: " + videoFilePath);
	        
	        // Attendez un instant pour vous assurer que la vidéo est prête
	        Thread.sleep(1000);

	        attachVideoToAllure(videoFilePath);
	    } catch (IOException e) {
	        System.err.println("Erreur lors de l'attachement de la vidéo : " + e.getMessage());
	    } catch (ATUTestRecorderException e) {
	        System.err.println("Erreur lors de l'arrêt de l'enregistreur : " + e.getMessage());
	    } catch (InterruptedException e) {
	        Thread.currentThread().interrupt();
	        System.err.println("Le thread a été interrompu : " + e.getMessage());
	    }
	}
	@AfterTest
	@Description("Fermeture du navigateur.")
	public void teardown() {
		if (driver != null) {
			driver.quit();
		}
	}
}
