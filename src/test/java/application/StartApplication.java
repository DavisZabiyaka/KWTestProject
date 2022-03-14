package application;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StartApplication {

    private static final String PITCHBOOK_URL = "https://pitchbook.com/profiles/search?q=";
    private static final String HONDA_CSV_DIRECTORY = ""; // Filepath of directory where csv file is located (i.e. "C:\\Users\\Max\\"
    private static final String HONDA_REPORTS_CSV_DIRECTORY = ""; // Filepath where you want your csv reports to be located
    private static final String FILE_NAME = ""; // Full name of your file, including the .csv
    private static final String COMMA_DELIMITED = ",";
    private static final String NO_URL = "NO_URL";

    private static final List<String> companies = new ArrayList<>();
    private static final List<String> companyUrls = new ArrayList<>();
    private static final List<String> noSuchCompaniesInPitchBook = new ArrayList<>();
    private static final List<String> noSuchCompaniesThoroughSearch = new ArrayList<>();
    private static final List<String> companiesInPitchBookWithNonMatchingUrls = new ArrayList<>();
    private static final List<String> missingCompanyUrlsInPitchBook = new ArrayList<>();
    private static final List<List<String>> companiesAndUrls = new ArrayList<>();

    private static final String NO_SUCH_COMPANIES_FILE   = "Companies not existing in PitchBook.csv";
    private static final String NO_SUCH_COMPANIES_THOROUGH_SEARCH_FILE = "More Thorough Search for Non-Existing Companies.csv";
    private static final String NON_MATCHING_URL_FILE    = "Non-matching URL Companies.csv";
    private static final String MISSING_COMPANY_URL_FILE = "Companies missing URL in PitchBook.csv";

    private static final String NO_SUCH_COMPANIES_HEADER                 = "Company Name";
    private static final String NO_SUCH_COMPANIES_THOROUGH_SEARCH_HEADER = "Company Name, NoOfSearches, Company Name Lowercase, NoOfSearches, Company Name First Word, NoOfSearches, Company Name First Word Lowercase, NoOfSearches";
    private static final String NON_MATCHING_URL_HEADER                  = "Company Name, Excel Url, PitchBook Url, Number of Search Items";
    private static final String MISSING_COMPANY_URL_HEADER               = "Company Name, Excel Url, Number of Search Items";

    private static final String NO_RESULTS_MESSAGE = "Sorry. We couldn't find the profile you're looking for.";


    private static final By searchInput           = By.className("search-input");
    private static final By searchButton          = By.className("btn-search");
    private static final By listItem              = By.xpath("/html/body/div[3]/div/div[1]/div/div/ul/li/a");
    private static final By sorryNoResultsMessage = By.xpath("/html/body/div[3]/div/div[1]/div/div/div/p[2]");
    private static final By websiteTitle          = By.xpath("/html/body/div[2]/div/div/div[2]/div[2]/div[1]/div[1]/div");
    private static final By pitchBookCompanyUrl   = By.xpath("/html/body/div[2]/div/div/div[2]/div[2]/div[1]/div[1]/a/span");



    private static final By profileListCss = By.cssSelector("body > div.container.main-padding > div > div.column.XL-9.M-12 > div > div > ul > li");
    private static final By resultsTitle = By.xpath("/html/body/div[3]/div/div[1]/div/h2");
    private static final By profileList  = By.className("profile-list");
    private static final By noResults    = By.className("no-result");

    private static WebDriver chromeDriver = null;

    private static final boolean IS_HEADLESS = false;

    @BeforeClass
    public static void setUpClass() throws IOException {
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(IS_HEADLESS);

        options.addArguments("enable-automation");
        options.addArguments("--no-sandbox");
        options.addArguments("--kiosk");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-browser-side-navigation");
        options.addArguments("--disable-extensions");

        WebDriverManager.chromedriver().setup();
        chromeDriver = new ChromeDriver(options);

        readCsv(NO_SUCH_COMPANIES_FILE);
    }

    @AfterClass
    public static void tearDownClass() {
        if (chromeDriver != null) {
            chromeDriver.close();
            chromeDriver.quit();
        }
    }

    /*public static void main(String[] args) {
        String urlPattern = "((https?|http?)(:?)((//?)|(\\\\?))((www.)?))";
        String urlPattern2 = "((https?|http?):((//?)|(\\\\?))((www.)?))";
        String url = "http://www.enspired-trading.com";
        String url2 = "www.enspired-trading.com";
        Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(url);
        int i = 0;
        while (m.find()) {
            url = url.replaceAll(m.group(i),"").trim();
            i++;
        }
        System.out.println(url);
        System.out.println(url2);
    }*/

    @Test
    public void search() throws InterruptedException, FileNotFoundException, URISyntaxException {
        for (int i = 0; i < /*companies.size()*/10; i++) {
            System.out.println(i + " of " + companies.size() + " companies...");
            String currentCompany = companies.get(i);
            String currentCompanyUrl = companyUrls.get(i);
            chromeDriver.get(PITCHBOOK_URL);
            waitUntilTitleContains("Profile Previews", 10);
            searchInput().sendKeys(currentCompany);
            searchButton().click();
            chromeDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            //waitUntilTitleContains("Profile Previews", 10);
            //waitUntilVisible(chromeDriver.findElement(resultsTitle), 10);
            //Thread.sleep(4000);
            //WebElement webElement = resultsTitle();
            //waitUntilElementContainsMatchingText(webElement, companies.get(i), 10);
            try {
                listItem().click();
                chromeDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                boolean hasWebsiteTitle = (websiteTitle().getText()).equals("Website");
                if (hasWebsiteTitle) {
                    String pitchBookCompanyUrl = (pitchBookCompanyUrl().getText());
                    boolean exactMatchUrls = pitchBookCompanyUrl.equals(currentCompanyUrl);
                    if (!exactMatchUrls) {
                        System.out.println(currentCompany + " has non-matching urls. Excel: " + currentCompanyUrl + ", PitchBook: " + pitchBookCompanyUrl);
                        companiesInPitchBookWithNonMatchingUrls.add(currentCompany + "," + currentCompanyUrl + "," + pitchBookCompanyUrl);
                    }
                } else if (!currentCompanyUrl.equals(NO_URL)) {
                    System.out.println("PitchBook is missing company url for " + currentCompany + ": " + currentCompanyUrl);
                    missingCompanyUrlsInPitchBook.add(currentCompany + "," + currentCompanyUrl);
                }
            } catch (NoSuchElementException e) {
                noSuchCompaniesInPitchBook.add(currentCompany);
            }
            //chromeDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            //waitUntilOneOfTwoElementsAreVisible(profileList(), noResults(), 10);
        }
        System.out.println("Finished all companies");
        // for no such companies in pitchbook
        generateCsv(HONDA_REPORTS_CSV_DIRECTORY + NO_SUCH_COMPANIES_FILE, NO_SUCH_COMPANIES_HEADER, noSuchCompaniesInPitchBook);
        // for companies found, but urls not matching
        generateCsv(HONDA_REPORTS_CSV_DIRECTORY + NON_MATCHING_URL_FILE, NON_MATCHING_URL_HEADER, companiesInPitchBookWithNonMatchingUrls);
        // for companies missing a url in Pitchbook
        generateCsv(HONDA_REPORTS_CSV_DIRECTORY + MISSING_COMPANY_URL_FILE, MISSING_COMPANY_URL_HEADER, missingCompanyUrlsInPitchBook);
    }

    @Test
    public void searchImproved() throws InterruptedException, FileNotFoundException, URISyntaxException {
        for (int i = 0; i < /*companies.size()*/30; i++) {
            System.out.println((i + 1) + " of " + companies.size() + " companies...");
            String currentCompany = companies.get(i);
            String currentCompanyUrl = companyUrls.get(i);
            chromeDriver.get(PITCHBOOK_URL);
            waitUntilTitleContains("Profile Previews", 10);
            searchInput().sendKeys(currentCompany);
            searchButton().click();
            chromeDriver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
            int sizeOfSearchList = profileListByCss().size();
            System.out.println(currentCompany + " search produces " + sizeOfSearchList + " result(s)");
            if (sizeOfSearchList > 0) {
                listItem().click();
                chromeDriver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
                boolean hasWebsiteTitle = (websiteTitle().getText()).equals("Website");
                if (hasWebsiteTitle) {
                    String pitchBookCompanyUrl = (pitchBookCompanyUrl().getText());
                    boolean exactMatchUrls = pitchBookCompanyUrl.equals(currentCompanyUrl);
                    if (!exactMatchUrls) {
                        System.out.println(currentCompany + " has non-matching urls. Excel: " + currentCompanyUrl + ", PitchBook: " + pitchBookCompanyUrl);
                        companiesInPitchBookWithNonMatchingUrls.add(currentCompany + "," + currentCompanyUrl + "," + pitchBookCompanyUrl + "," + sizeOfSearchList);
                    }
                } else if (!currentCompanyUrl.equals(NO_URL)) {
                    System.out.println("PitchBook is missing company url for " + currentCompany + ": " + currentCompanyUrl);
                    missingCompanyUrlsInPitchBook.add(currentCompany + "," + currentCompanyUrl + "," + sizeOfSearchList);
                }
            } else {
                noSuchCompaniesInPitchBook.add(currentCompany);
                continue;
            }
        }
        System.out.println("Finished all companies");
        // for no such companies in pitchbook
        generateCsv(HONDA_REPORTS_CSV_DIRECTORY + NO_SUCH_COMPANIES_FILE, NO_SUCH_COMPANIES_HEADER, noSuchCompaniesInPitchBook);
        // for companies found, but urls not matching
        generateCsv(HONDA_REPORTS_CSV_DIRECTORY + NON_MATCHING_URL_FILE, NON_MATCHING_URL_HEADER, companiesInPitchBookWithNonMatchingUrls);
        // for companies missing a url in Pitchbook
        generateCsv(HONDA_REPORTS_CSV_DIRECTORY + MISSING_COMPANY_URL_FILE, MISSING_COMPANY_URL_HEADER, missingCompanyUrlsInPitchBook);
    }

    private int numberOfSearchResults(String searchInput) {
        searchInput().sendKeys(searchInput);
        searchButton().click();
        chromeDriver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
        int sizeOfList = profileListByCss().size();
        searchInput().clear();
        System.out.println("Current search " + searchInput + " produces " + sizeOfList + " result(s)");
        return sizeOfList;
    }

    @Test
    public void searchNoneExistingCompanies() throws InterruptedException, FileNotFoundException {
        for (int i = 0; i < companies.size(); i++) {
            String currentCompany                          = null;

            int sizeOfListCurrentCompany                   = 0;
            int sizeOfListCurrentCompanyLowerCase          = 0;
            int sizeOfListCurrentCompanyFirstWord          = 0;
            int sizeOfListCurrentCompanyFirstWordLowerCase = 0;
            String currentCompanyLowerCase                 = null;
            String currentCompanyFirstWord                 = null;
            String currentCompanyFirstWordLowerCase        = null;
            System.out.println((i + 1) + " of " + companies.size() + " companies...");
            currentCompany = companies.get(i);
            currentCompanyLowerCase = currentCompany.toLowerCase();
            currentCompanyFirstWord = currentCompany.split(" ").length > 1 ? currentCompany.split(" ")[0] : null;
            currentCompanyFirstWordLowerCase = currentCompanyFirstWord != null ? currentCompanyFirstWord.toLowerCase() : null;
            //String currentCompanyUrl = companyUrls.get(i);
            chromeDriver.get(PITCHBOOK_URL);
            waitUntilTitleContains("Profile Previews", 10);
            // Start checking Exact company name
            sizeOfListCurrentCompany = numberOfSearchResults(currentCompany);
            sizeOfListCurrentCompanyLowerCase = numberOfSearchResults(currentCompanyLowerCase);
            if (currentCompanyFirstWord != null) {
                sizeOfListCurrentCompanyFirstWord = numberOfSearchResults(currentCompanyFirstWord);
                sizeOfListCurrentCompanyFirstWordLowerCase = numberOfSearchResults(currentCompanyFirstWordLowerCase);

            }
            noSuchCompaniesThoroughSearch.add(
                    currentCompany + ","
                            + sizeOfListCurrentCompany + ","
                            + currentCompanyLowerCase + ","
                            + sizeOfListCurrentCompanyLowerCase + ","
                            + currentCompanyFirstWord + ","
                            + sizeOfListCurrentCompanyFirstWord + ","
                            + currentCompanyFirstWordLowerCase + ","
                            + sizeOfListCurrentCompanyFirstWordLowerCase
            );
        }
        System.out.println("Finished all companies");
        generateCsv(HONDA_REPORTS_CSV_DIRECTORY + NO_SUCH_COMPANIES_THOROUGH_SEARCH_FILE, NO_SUCH_COMPANIES_THOROUGH_SEARCH_HEADER, noSuchCompaniesThoroughSearch);
    }

    private static List<List<String>> readCsv(String fileName) throws IOException {
        try
                (
                        FileReader fileReader = new FileReader(HONDA_CSV_DIRECTORY + fileName);
                        BufferedReader bufferedReader = new BufferedReader(fileReader);
                ) {
            String line = bufferedReader.readLine();
            while (line != null) {
                String[] values = line.split(COMMA_DELIMITED);
                if (values.length > 2) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < values.length - 1; i++) {
                        if (i > 0) {
                            stringBuilder.append("," + values[i]);
                        } else {
                            stringBuilder.append(values[i]);
                        }
                    }
                    companies.add(stringBuilder.toString());
                    companyUrls.add(values[values.length - 1]);
                    stringBuilder = null;
                } else if (values.length == 1) {
                    //System.out.println("Company: " + values[0] + ", url: No Url");
                    companies.add(values[0]);
                    companyUrls.add(NO_URL);
                } else {
                    //System.out.println("Company: " + values[0] + ", url: " + values[1]);
                    companies.add(values[0]);
                    companyUrls.add(values[1]);
                }
                line = bufferedReader.readLine();
            }
            companiesAndUrls.add(companies);
            companiesAndUrls.add(companyUrls);
            return companiesAndUrls;
        }
    }

    private static List<String> readCsvOnlyCompanies(String fileName) throws IOException {
        try
                (
                        FileReader fileReader = new FileReader(HONDA_CSV_DIRECTORY + fileName);
                        BufferedReader bufferedReader = new BufferedReader(fileReader);
                ) {
            String line = bufferedReader.readLine();
            while (line != null) {
                String[] values = line.split(COMMA_DELIMITED);
                if (values.length > 2) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < values.length - 1; i++) {
                        if (i > 0) {
                            stringBuilder.append("," + values[i]);
                        } else {
                            stringBuilder.append(values[i]);
                        }
                    }
                    companies.add(stringBuilder.toString());
                    //companyUrls.add(values[values.length - 1]);
                    stringBuilder = null;
                } else if (values.length == 1) {
                    //System.out.println("Company: " + values[0] + ", url: No Url");
                    companies.add(values[0]);
                    //companyUrls.add(NO_URL);
                } else {
                    //System.out.println("Company: " + values[0] + ", url: " + values[1]);
                    companies.add(values[0]);
                    //companyUrls.add(values[1]);
                }
                line = bufferedReader.readLine();
            }
            /*companiesAndUrls.add(companies);
            companiesAndUrls.add(companyUrls);*/
            return companies;
        }
    }

    private static void generateCsv(String fileName, String header, List<String> data) throws FileNotFoundException {
        File outputFile = new File(fileName);
        try
                (
                        PrintWriter printWriter = new PrintWriter(outputFile);
                ) {
            printWriter.write(header + "\n");
            for (String datum: data) {
                printWriter.write(datum + "\n");
            }
        }
    }

    /*private static String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }*/

    private static String stripUrl(String url) {
        String urlPattern = "((https?|http?):((//?)|(\\\\?))((www.)?))";
        Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(url);
        int i = 0;
        while (m.find()) {
            url = url.replaceAll(m.group(i),"").trim();
            i++;
        }
        return url;
    }

    private WebElement searchInput() { return chromeDriver.findElement(searchInput); }

    private WebElement searchButton() { return chromeDriver.findElement(searchButton); }

    private List<WebElement> sorryNoResultsMessage() { return chromeDriver.findElements(sorryNoResultsMessage); }

    private WebElement listItem() { return chromeDriver.findElement(listItem); }

    private WebElement websiteTitle() { return chromeDriver.findElement(websiteTitle); }

    private WebElement pitchBookCompanyUrl() { return chromeDriver.findElement(pitchBookCompanyUrl); }

    private WebElement resultsTitle() { return chromeDriver.findElement(resultsTitle); }

    private WebElement profileList() { return chromeDriver.findElement(profileList); }

    private List<WebElement> profileListByCss() { return chromeDriver.findElements(profileListCss); }

    private WebElement noResults() { return chromeDriver.findElement(noResults); }

    private static void waitUntilTitleContains(String titlePattern, int maximumNumberOfSeconds) {
        new WebDriverWait(chromeDriver, maximumNumberOfSeconds).until(ExpectedConditions.titleContains(titlePattern));
    }

    private static void waitUntilElementContainsMatchingText(WebElement element, String textPattern, int maximumNumberOfSeconds) {
        new WebDriverWait(chromeDriver, maximumNumberOfSeconds).until(ExpectedConditions.textToBePresentInElement(element, textPattern));
    }

    private static void waitUntilVisible(WebElement element, int maximumNumberOfSeconds) {
        new WebDriverWait(chromeDriver, maximumNumberOfSeconds).until(ExpectedConditions.visibilityOf(element));
    }

    /*private static void waitUntilOneOfTwoElementsAreVisible(WebElement element1, WebElement element2, int maximumNumberOfSeconds) {
        new WebDriverWait(chromeDriver, maximumNumberOfSeconds).until(ExpectedConditions.or(ExpectedConditions.visibilityOf(element1), ExpectedConditions.visibilityOf(element2)));
    }*/
}
