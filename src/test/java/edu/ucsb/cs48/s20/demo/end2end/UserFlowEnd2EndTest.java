package edu.ucsb.cs48.s20.demo.end2end;

import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.*;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * This test runs a complete end-to-end test on the project as a single test.
 * Starts by logging in as a student, submitting a review, and reviewing 5 ideas
 *
 * If you are trying to duplicate this test in a project, make sure to copy the
 * html template under the test/resources/__files/ directory!
 */
@RunWith(SpringRunner.class)
// NOTE: properties="spring.datasource.name=XYZ" forces Spring Boot to run this test class with a fresh instance of the database.
// XYZ can be anything - just make it unique from other test classes for a fresh test db (loaded from data.sql)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties="spring.datasource.name=userflowend2endtest")
public class UserFlowEnd2EndTest {

    /**
     * Sets up custom environment variables for this test class. This is the information for our fake oauth provider.
     */
    static {
        System.setProperty("spring.security.oauth2.client.provider.wiremock.authorization-uri", "http://localhost:8077/oauth/authorize");
        System.setProperty("spring.security.oauth2.client.provider.wiremock.token-uri", "http://localhost:8077/oauth/token");
        System.setProperty("spring.security.oauth2.client.provider.wiremock.user-info-uri", "http://localhost:8077/userinfo");
        System.setProperty("spring.security.oauth2.client.provider.wiremock.user-name-attribute", "sub");
        System.setProperty("spring.security.oauth2.client.registration.wiremock.provider", "wiremock");
        System.setProperty("spring.security.oauth2.client.registration.wiremock.authorization-grant-type", "authorization_code");
        System.setProperty("spring.security.oauth2.client.registration.wiremock.scope", "email");
        System.setProperty("spring.security.oauth2.client.registration.wiremock.redirect-uri", "http://localhost:8080/login/oauth2/code/{registrationId}");
        System.setProperty("spring.security.oauth2.client.registration.wiremock.clientId", "wm");
        System.setProperty("spring.security.oauth2.client.registration.wiremock.clientSecret", "whatever");
    }

    private WebDriver webDriver;

    /**
     * Sets up the api endpoints for the custom mock oauth provider. We have to do this because we can't run automated
     * tests with real Google credentials
     */
    @Rule
    public WireMockRule mockOAuth2Provider = new WireMockRule(wireMockConfig()
            .port(8077)
            .extensions(new ResponseTemplateTransformer(true)));

    /**
     * Runs before the class.
     * Instantiates the WebDriverManager so we can correctly locate the system's ChromeDriver below
     */
    @BeforeClass
    public static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @Before
    public void setUp() {
        // Setup ChromeDriver (aided by the WebDriverManager)
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        webDriver = new ChromeDriver(options);

        // Configure mock oauth endpoints
        mockOAuth2Provider.stubFor(get(urlPathEqualTo("/favicon.ico")).willReturn(notFound()));
        mockOAuth2Provider.stubFor(get(urlPathMatching("/oauth/authorize?.*"))
                                   .willReturn(aResponse()
                                               .withStatus(200)
                                               .withHeader("Content-Type", "text/html")
                                               .withBodyFile("mocklogin.html")));
        mockOAuth2Provider.stubFor(post(urlPathEqualTo("/login"))
                                   .willReturn(temporaryRedirect("{{formData request.body 'form' urlDecode=true}}http://localhost:8080/login/oauth2/code/wiremock?code={{{randomValue length=30 type='ALPHANUMERIC'}}}&state={{{form.state}}}")));
        mockOAuth2Provider.stubFor(post(urlPathEqualTo("/oauth/token"))
                                   .willReturn(okJson("{\"token_type\": \"Bearer\",\"access_token\":\"{{randomValue length=20 type='ALPHANUMERIC'}}\"}")));
        mockOAuth2Provider.stubFor(get(urlPathEqualTo("/userinfo"))
                                   .willReturn(okJson("{\"sub\":\"my-id\",\"email\":\"joe@ucsb.edu\", \"hd\":\"ucsb.edu\", \"name\":\"Joe\", \"given_name\":\"Joe\", \"family_name\":\"Gaucho\"}")));
    }

    /**
     * Tear down the webDriver (the automated clicker)
     */
    @After
    public void reset() {
        if (webDriver != null) {
            webDriver.close();
        }
    }

    static String MEDIUM_TEXT = "This is a long bit of text. This will be reused in some text fields throughout the test.";

    @Test
    public void runUserFlowEnd2EndTestWithAuthentication() {
        // Navigate to login page
        webDriver.get("http://localhost:8080/oauth2/authorization/wiremock");
        // Make sure Spring redirected us to the right place
        assert(webDriver.getCurrentUrl()).startsWith("http://localhost:8077/oauth/authorize");
        webDriver.findElement(By.id("submit")).click();
        // Verify login was successful (and did not result in /error path)
        assert(webDriver.getCurrentUrl().equalsIgnoreCase("http://localhost:8080/"));

        // fill out idea form
        webDriver.findElement(By.id("ideaTitle")).sendKeys("This is a title for a fantastic idea");
        webDriver.findElement(By.id("details")).sendKeys(MEDIUM_TEXT);
        webDriver.findElement(By.id("submit")).click();

        // Make sure we got redirected to the review review page (lol)
        assert(webDriver.getCurrentUrl().equalsIgnoreCase("http://localhost:8080/reviews/perform"));

        // Enter 5 reviews
        for (int i = 0; i < 5; i++) {
            assert webDriver.findElement(By.id("reviewsNeeded")).getText().contains(String.valueOf(5-i)); //check user sees x remaining
            webDriver.findElement(By.id("radio5Label")).click();
            webDriver.findElement(By.className("form-control")).sendKeys(MEDIUM_TEXT);
            webDriver.findElement(By.id("submit")).click();
        }

        // User has inputted 5 reviews. Make sure the reviewsNeeded banner is gone, and the reviewsSufficient banner shows
        assert(webDriver.findElements(By.id("reviewsNeeded")).isEmpty());
        assert(webDriver.findElements(By.id("reviewsSufficient")).size() == 1);
    }

}
