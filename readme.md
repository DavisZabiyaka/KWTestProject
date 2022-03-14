# Read Before Running Application

# Application is located in the src/test/java/application directory

Once you are in the StartApplication.java file, you must populate the directory and
file path properties that are blank and have comments next to them (i.e. HONDA_CSV_DIRECTORY)

There are 3 test cases (annotated with @Test), which essentially are to be treated as
different test cases to run

1. search - initial program that runs a search based on the csv file provided
2. searchImproved - runs an improved search for companies
3. searchNonExistingCompanies - runs a specific case for companies that don't seem to exist

Before running any test, go to the setUpClass method (annotated with the @BeforeClass annotation)
and make sure that the argument in the last line of code, readCsv(...) contains the correct name
of the file you want to read.

In order to run any of these tests, scroll to the test cases, right click with the mouse
and click 'run searchNonExistingCompanies' or whichever test case you want (the method to)
run tests might be IDE specific and different on the terminal/command line