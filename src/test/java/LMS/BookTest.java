package LMS;
// JUnit5
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

// Mockito
import static org.mockito.Mockito.*;

// Java standard
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.lang.*;
import java.util.GregorianCalendar;

public class BookTest {

    static private String pathToResources = "src/test/resources/";
    private final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    private uiUtils helper = new uiUtils(pathToResources, originalOut, outStream);

    // Dummies
    private Borrower dummyBorrower;
    private Book bookInTest;
    private Staff dummyStaff;
    private Loan dummyLoan;
    private HoldRequest dummyHoldRequest;

    @BeforeEach
    public void setupStream() {
        System.setOut(new PrintStream(outStream));

        dummyBorrower = new Borrower(0, "dummyBorrower", "", 0);
        bookInTest = new Book(0, "TestBook", "Software Engineering",
                "Somebody", false);
        dummyStaff = new Staff(0, "dummyStaff", "dummyAddress", 0, 0.0);
        dummyLoan = new Loan(dummyBorrower, bookInTest, dummyStaff,
                null, new Date(), null, false);
        dummyHoldRequest = new HoldRequest(dummyBorrower, bookInTest, new Date());

    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(originalIn);
        bookInTest.setIDCount(0);
    }


    @ParameterizedTest(name="BorroBookwer constructor tests: ID:{0}, " +
            "Title: {1}, Subject: {2}, Author: {3}, Issued: {4}")
    @CsvSource({
            "-4, book-4, subject-4, author-4, false",
            "-3, book-3, , author-3, false",
            "-2, book-2, null, author-2, false",
            "-1, book-1, subject-1, , false",
            "0, book0, subject0, null, false",
            "1, book1, subject1, author1, true",
            "2, book2, subject2, author2, true",
            "3, book3, subject3, author3, false"
    })
    public void bookConstructor(
            int id, String title, String subject, String author, boolean issued
    ) {
        Book currBook = new Book(id, title, subject, author, issued);

        assertTrue(currBook.getHoldRequests().isEmpty());
        assertEquals(title, currBook.getTitle());
        assertEquals(subject, currBook.getSubject());
        assertEquals(author, currBook.getAuthor());
        assertEquals(issued, currBook.getIssuedStatus());
        assertEquals(id, currBook.getID());
    }

    @DisplayName("Book constructor: repeated id")
    @Test
    public void bookConstructorRepeatedID() {
        Book repeatedBook = new Book(0, "tiile", "subject", "authors", false);
        assertNotEquals(bookInTest.getID(),
                repeatedBook.getID(), "Should not allow books to have the same id");
    }

    @DisplayName("Book setIDCount: Normal")
    @Test
    public void booksetIDCountNormal() {
        bookInTest.setIDCount(5);
        Book newBook = new Book(-1, "","", "", false);
        assertEquals(newBook.getID(), 6);
    }

    @DisplayName("addHoldRequest: Normal")
    @Test
    public void addHoldRequestNormal() {
        HoldRequest[] addedHoldReqquest = new HoldRequest[10];
        ArrayList<HoldRequest> allHoldReqquest;
        bookInTest.setIssuedStatus(true);
        
        assert(bookInTest.getHoldRequests().isEmpty());
        for(int i = 0; i < 10; i++ ) {
            HoldRequest hr = new HoldRequest(dummyBorrower, bookInTest, new Date());
            bookInTest.addHoldRequest(hr);
            addedHoldReqquest[i] = hr;
        }
        allHoldReqquest = bookInTest.getHoldRequests();
        for (int i = 0; i < 10; i++) {
            assertEquals(addedHoldReqquest[i], allHoldReqquest.get(i));
        }

    }

    @DisplayName("addHoldRequest: Not issued")
    @Test
    public void addHoldRequestNotIssued() {
        bookInTest.setIssuedStatus(false);
        assertThrows(Exception.class, ()->{
           bookInTest.addHoldRequest(dummyHoldRequest); 
        }, "Should not add hold request when not issued");
    }

    @DisplayName("addHoldRequest: Different Book")
    @Test
    public void addHoldRequestDifferentBook() {
        Book diffBook = new Book(-1, "Another Book", "Another Subject", 
                "Another Author", true);
        HoldRequest hr = new HoldRequest(dummyBorrower, diffBook, new Date());
        int before, after;
        
        before = bookInTest.getHoldRequests().size();
        bookInTest.setIssuedStatus(true);
        bookInTest.addHoldRequest(hr);
        after = bookInTest.getHoldRequests().size();
        assertEquals(before, after, "Hold request for another book should not matter");
    }

    @DisplayName("addHoldRequest: Null hold request")
    @Test
    public void addHoldRequestNullRequest() {
        int before, after;
        before = bookInTest.getHoldRequests().size();
        bookInTest.setIssuedStatus(true);
        bookInTest.addHoldRequest(null);
        after = bookInTest.getHoldRequests().size();
        assertEquals(before, after, "Null request should not matter");
    }

    @DisplayName("removeHoldRequest: Normal")
    @Test
    public void removeHoldRequestNormal() {
        bookInTest.setIssuedStatus(true);
        bookInTest.addHoldRequest(dummyHoldRequest);
        bookInTest.removeHoldRequest();
        assertTrue(bookInTest.getHoldRequests().isEmpty());
    }

    @DisplayName("removeHoldRequest: Empty list")
    @Test
    public void removeHoldRequestEmptyList() {
        bookInTest.removeHoldRequest();
        assertTrue(bookInTest.getHoldRequests().isEmpty());
    }


    @DisplayName("makeHoldRequest: Normal")
    @Test
    public void makeHoldRequestNormal() {
        bookInTest.makeHoldRequest(dummyBorrower);
        ArrayList<HoldRequest> hr = bookInTest.getHoldRequests();
        assertEquals(hr.get(0).borrower, dummyBorrower);
    }

    @DisplayName("makeHoldRequest: Already borrowed")
    @Test
    public void makeHoldRequestBorrowed() {
        int before, after;
        Loan loanForThis = new Loan(dummyBorrower, bookInTest, dummyStaff,
                null, new Date(), null, false);
        dummyBorrower.addBorrowedBook(loanForThis);

        before = bookInTest.getHoldRequests().size();
        bookInTest.makeHoldRequest(dummyBorrower);
        after = bookInTest.getHoldRequests().size();
        assertEquals(before, after);
    }

    @DisplayName("makeHoldRequest: Already held")
    @Test
    public void makeHoldRequestHeld() {
        int before, after;
        HoldRequest hr = new HoldRequest(dummyBorrower, bookInTest, new Date());
        dummyBorrower.addHoldRequest(hr);

        bookInTest.makeHoldRequest(dummyBorrower);
        before = bookInTest.getHoldRequests().size();
        bookInTest.makeHoldRequest(dummyBorrower);
        after = bookInTest.getHoldRequests().size();
        assertEquals(before, after);
    }

    @DisplayName("printHoldRequests: Empty list")
    @Test
    public void printHoldRequestsEmpty() {
        String expected, actual;
        expected = helper.cleanString("\nNo Hold Requests.");

        bookInTest.printHoldRequests();
        actual = helper.cleanString(outStream.toString());

        assertEquals(expected, actual);
    }

    @DisplayName("printHoldRequests: Normal")
    @Test
    public void printHoldRequestNormal() {
        String expected, actual;
        bookInTest.makeHoldRequest(dummyBorrower);
        bookInTest.printHoldRequests();

        expected = helper.cleanString("\nNo Hold Requests.");
        actual = helper.cleanString(outStream.toString());

        assertNotEquals(expected, actual);
    }

    @ParameterizedTest(name="changeBookInfo: {0}")
    @CsvSource({
            "testBook/changeBookInfoNoChange.txt",
            "testBook/changeBookInfoAuthor.txt",
            "testBook/changeBookInfoSubject.txt",
            "testBook/changeBookInfoTitle.txt",
            "testBook/changeBookInfoAuthorSubject.txt",
            "testBook/changeBookInfoAuthorTitle.txt",
            "testBook/changeBookInfoSubjectTitle.txt",
            "testBook/changeBookInfoAuthorSubjectTitle.txt"
    })
    public void changeBookInfo(String resourceFile) {
        String inputContent = helper.readFromResource(resourceFile);
        ByteArrayInputStream inStream = new ByteArrayInputStream(inputContent.getBytes());
        System.setIn(inStream);

        Book equivalentBook = updateInfoParseResource(inputContent, bookInTest);
        try {
            bookInTest.changeBookInfo();
        }
        catch (Exception e) {
            helper.printInTest(e.getMessage());
        }

        assertEquals(equivalentBook.getTitle(),bookInTest.getTitle(), "Title should match");
        assertEquals(equivalentBook.getSubject(), bookInTest.getSubject(), "Subject should match");
        assertEquals(equivalentBook.getAuthor(), bookInTest.getAuthor(),"Author should match");
    }

    @DisplayName("issueBook: Successful issue")
    @Test
    public void issueBookSuccess() {
        Library mockLib = mock(Library.class);

        bookInTest.issueBook(dummyBorrower, dummyStaff);
        // verify(mockLib).addLoan(any());  // Assert loan recorded in the library -  Doesn't work with singleton
        assertFalse(dummyBorrower.getBorrowedBooks().isEmpty()); // Assert borrower has the book in their record
        assertTrue(bookInTest.getIssuedStatus());

    }

    @DisplayName("issueBook: add hold request of book - no older request")
    @Test
    public void issueBookAddHoldRequestNoOldHR() {
        ByteArrayInputStream inStream = new ByteArrayInputStream("y\n".getBytes());
        System.setIn(inStream);
        bookInTest.setIssuedStatus(true);
        bookInTest.issueBook(dummyBorrower, dummyStaff);
        assertEquals(1, bookInTest.getHoldRequests().size());
    }

    @ParameterizedTest(name="issueBook: issued, new Borrower request HR: {0}")
    @CsvSource({
            "n",
            "y"
    })
    public void issueBookIssued(String userInput) {
        int expected;
        HoldRequest oldHoldRequest = new HoldRequest(dummyBorrower, bookInTest,
                new GregorianCalendar(2019, Calendar.OCTOBER, 31).getTime());
        Borrower newBorrower = new Borrower(-1, "New Borrower", "New Address", 0);

        ByteArrayInputStream inStream = new ByteArrayInputStream((userInput + "\n").getBytes());
        System.setIn(inStream);

        bookInTest.setIssuedStatus(true);
        bookInTest.addHoldRequest(oldHoldRequest); // Will be reomved
        bookInTest.issueBook(newBorrower, dummyStaff);

        if (userInput.equals("y"))
            expected = 1;
        else
            expected = 0;

        helper.printInTest(outStream.toString());
        // assertEquals(expected, newBorrower.getOnHoldBooks().size());
        assertEquals(expected, bookInTest.getHoldRequests().size());
    }


    @ParameterizedTest(name="issueBook: issue=false, other borrower has HR, new Borrower request HR: {0}")
    @CsvSource({
            "n",
            "y"
    })
    public void issueBookOtherHoldRequestExists(String userInput) {
        Borrower newBorrower = new Borrower(-1, "New Borrower", "New Address", 0);
        int expected = 0;

        ByteArrayInputStream inStream = new ByteArrayInputStream((userInput + "\n").getBytes());
        System.setIn(inStream);

        bookInTest.addHoldRequest(dummyHoldRequest);
        bookInTest.issueBook(newBorrower, dummyStaff);

        if (userInput.equals("y"))
            expected = 1;
        else
            expected = 0;
        assertEquals(expected, newBorrower.getOnHoldBooks().size());
    }

    @DisplayName("issueBook: Not issued, and the borrower has HR earlier")
    @Test
    public void issueBookHadHoldRequestEarlier() {
        Borrower newBorrower = new Borrower(-1, "New Borrower", "New Address", 0);

        bookInTest.addHoldRequest(dummyHoldRequest);
        bookInTest.issueBook(dummyBorrower, dummyStaff);
        assertEquals(0, bookInTest.getHoldRequests().size());
        assertEquals(1, dummyBorrower.getBorrowedBooks().size());
        assertTrue(bookInTest.getIssuedStatus());
    }


    @DisplayName("issueBook: Not issued, borrower has HR, but others have earlier HR")
    @Test
    public void issueBookNotEarliestHR() {
        Borrower newBorrower = new Borrower(-1, "New Borrower", "New Address", 0);
        HoldRequest newHoldRequest = new HoldRequest(newBorrower, bookInTest, new Date());
        String expected = helper.cleanString("\nSorry some other users have requested for this book earlier than you. " +
                "So you have to wait until their hold requests are processed.\n"), actual;

        bookInTest.addHoldRequest(dummyHoldRequest);
        bookInTest.addHoldRequest(newHoldRequest);

        bookInTest.issueBook(newBorrower, dummyStaff);

        actual = helper.cleanString(outStream.toString());
        assertEquals(expected, actual);
        assertEquals(0, newBorrower.getBorrowedBooks().size());
    }

    @ParameterizedTest(name="issueBook: null borrower, require HR: {0}")
    @CsvSource({
            "n",
            "y"
    })
    public void  issBookIssuedNullBorrower(String userInput) {
        Borrower newBorrower = null;

        ByteArrayInputStream inStream = new ByteArrayInputStream((userInput + "\n").getBytes());
        System.setIn(inStream);

        bookInTest.setIssuedStatus(true);

        assertThrows(Exception.class, () -> {
            bookInTest.issueBook(newBorrower, dummyStaff);
        });
    }

    @DisplayName("returnBook: Normal")
    @Test
    public void returnBookNormal() {
        bookInTest.issueBook(dummyBorrower, dummyStaff);
        bookInTest.returnBook(dummyBorrower,dummyBorrower.getBorrowedBooks().get(0), dummyStaff);
        assertEquals(0, dummyBorrower.getBorrowedBooks().size());
    }

    @DisplayName("returnBook: Wrong borrower")
    @Test
    public void returnBookWrongBorrower() {
        Loan existLoan;
        Borrower newBorrower = new Borrower(-1, "Reader", "Home", 0);
        String successPrompt, actualPrompt;

        bookInTest.issueBook(dummyBorrower, dummyStaff);
        existLoan = dummyBorrower.getBorrowedBooks().get(0);
        assertThrows(Exception.class, () -> {
            bookInTest.returnBook(newBorrower, existLoan, dummyStaff);
        });

    }

    @ParameterizedTest(name="returnBook: fine not paid: {0}")
    @CsvSource({
            "y",
            "n"
    })
    public void returnBookWithFine(String userInput){
        Library.getInstance().setFine(0.5); // Should be mocking this but can't

        Loan overDueLoan = new Loan(dummyBorrower,
                bookInTest,
                dummyStaff,
                null,
                new GregorianCalendar(2018, Calendar.OCTOBER, 31).getTime(),
                null,
                false);

        ByteArrayInputStream inStream = new ByteArrayInputStream((userInput + "\n").getBytes());
        System.setIn(inStream);
        boolean expected;

        bookInTest.setIssuedStatus(true);
        dummyBorrower.addBorrowedBook(overDueLoan);
        bookInTest.returnBook(dummyBorrower, overDueLoan, dummyStaff);

        if (userInput.equals("y"))
            expected = true;
        else
            expected = false;

        helper.printInTest(outStream.toString());
        assertEquals(expected, overDueLoan.getFineStatus());
    }

    private Book updateInfoParseResource(String content, Book b) {
        String title = b.getTitle(),
                subject = b.getSubject(),
                author = b.getAuthor();
        String lines[] = content.split("\\r?\\n");
        helper.printInTest("");
        int index = 0;
        if(lines[index].equals("y")) {
            index++;
            author = lines[index];
        }
        index++;
        if(lines[index].equals("y") ) {
            index++;
            subject = lines[index];
        }
        index++;
        if(lines[index].equals("y")) {
            index++;
            title = lines[index];
        }
        Book equivalentBook = new Book(-1, title, subject, author, false);
        return equivalentBook;
    }

}
