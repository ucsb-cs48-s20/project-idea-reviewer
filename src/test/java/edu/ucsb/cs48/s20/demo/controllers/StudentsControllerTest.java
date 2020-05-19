package edu.ucsb.cs48.s20.demo.controllers;

import edu.ucsb.cs48.s20.demo.advice.AuthControllerAdvice;
import edu.ucsb.cs48.s20.demo.advice.StudentFlowAdvice;
import edu.ucsb.cs48.s20.demo.entities.Student;
import edu.ucsb.cs48.s20.demo.repositories.AppUserRepository;
import edu.ucsb.cs48.s20.demo.repositories.StudentRepository;
import edu.ucsb.cs48.s20.demo.services.GoogleMembershipService;
import edu.ucsb.cs48.s20.demo.services.MembershipService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import utils.OAuthUtils;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WebAppConfiguration
@RunWith(SpringRunner.class)
/**
 * A fairly comprehensive set of Model/Controller unit tests for StudentsController
 * (UI/HTML tests take place in src/test/java/edu/ucsb/cs48/s20/demo/ui/StudentsHtmlTest.class)
 */
public class StudentsControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuthControllerAdvice aca;

    @MockBean
    private ClientRegistrationRepository crr;

    @MockBean
    private MembershipService ms;

    @Autowired
    private StudentsController studentsController;

    @MockBean
    private GoogleMembershipService gms;

    @MockBean
    private AppUserRepository aur;

    @MockBean
    private StudentRepository sr;

    @MockBean
    private StudentFlowAdvice studentFlowAdvice;

    private Authentication mockAuthentication;

    @Before
    public void setUp() {
        OAuth2User principal = OAuthUtils.createOAuth2User("Chris Gaucho", "cgaucho@ucsb.edu");
        mockAuthentication = OAuthUtils.getOauthAuthenticationFor(principal);
        when(ms.isMember((OAuth2AuthenticationToken) mockAuthentication)).thenReturn(true);
    }

    /**
     * This test directly tests the information the /students endpoint injects into the Model
     * The method studentsController.students is called directly vs fully rendering html and using xpath
     *
     * - > This tests the case where no students are enrolled
     */
    @Test
    public void testEmptyStudentsModel() throws Exception {
        // Begin by creating a new Model that we can pass to the controller to populate
        Model model = new ExtendedModelMap();

        // Mock the user role as an Admin
        when(ms.role(any())).thenReturn("Admin");

        // Mock the database response
        List<Student> someStudents = Arrays.asList();
        when(sr.findAll()).thenReturn(someStudents);

        // Call the controller
        studentsController.students(model, (OAuth2AuthenticationToken) mockAuthentication, null);

        // Make sure model has correct attribute
        assert(model.getAttribute("students").equals(someStudents));
    }

    /**
     * This test directly tests the information the /students endpoint injects into the Model
     * The method studentsController.students is called directly vs fully rendering html and using xpath
     *
     * - > This tests the case where some students are enrolled
     */
    @Test
    public void testNonEmptyStudentsModel() throws Exception {
        // Begin by creating a new Model that we can pass to the controller to populate
        Model model = new ExtendedModelMap();

        // Mock the user role as an Admin
        when(ms.role(any())).thenReturn("Admin");

        // Mock the database response
        List<Student> someStudents = Arrays.asList(new Student(), new Student(), new Student());
        when(sr.findAll()).thenReturn(someStudents);

        // Call the controller
        studentsController.students(model, (OAuth2AuthenticationToken) mockAuthentication, null);

        // Make sure model has correct attribute
        assert(model.getAttribute("students").equals(someStudents));
    }

    /**
     * This tests the /students/delete/{id} endpoint by mocking two students, deleting one, and
     * asserting that the controller's model returns only one student in the students list
     * (and that the controller adds a success message)
     *
     * - > Case where a valid student ID is submitted and deleted
     */
    @Test
    public void testDeleteValidStudent() throws Exception {
        // Begin by creating a new Model that we can pass to the controller to populate
        Model model = new ExtendedModelMap();
        RedirectAttributes redirAttrs = new RedirectAttributesModelMap();

        // Mock the user role as an Admin
        when(ms.role(any())).thenReturn("Admin");

        // Create our two students
        Student joe = new Student();
        joe.setId(5);
        Student carl = new Student();
        carl.setId(10);

        // Mock the database response
        when(sr.findById((long)5)).thenReturn(java.util.Optional.of(joe));
        when(sr.findAll()).thenReturn(Arrays.asList(carl));

        // Call the controller
        studentsController.deleteAdmin((long) 5, model, redirAttrs, (OAuth2AuthenticationToken) mockAuthentication);

        // Make sure delete was actually called (once)
        verify(sr, times(1)).delete(joe);

        // Make sure we display a success message
        assert(redirAttrs.getFlashAttributes().containsKey("alertSuccess"));

        // Assert model has correct attribute
        assert(model.getAttribute("students").equals(Arrays.asList(carl)));
    }

    /**
     * This tests the /students/delete/{id} endpoint by mocking two students, deleting an invalid id, and
     * asserting that the controller's model returns returns both students and adds an error message
     *
     * - > Case where a INVALID student ID is submitted, and nothing should be deleted
     */
    @Test
    public void testDeleteInvalidStudent() throws Exception {
        // Begin by creating a new Model that we can pass to the controller to populate
        Model model = new ExtendedModelMap();
        RedirectAttributes redirAttrs = new RedirectAttributesModelMap();

        // Mock the user role as an Admin
        when(ms.role(any())).thenReturn("Admin");

        // Create our two students
        Student joe = new Student();
        joe.setId(5);
        Student carl = new Student();
        carl.setId(10);

        // Mock the database response
        when(sr.findById((long)10)).thenReturn(java.util.Optional.empty());  // Mock no student for ID
        when(sr.findAll()).thenReturn(Arrays.asList(joe, carl));

        // Call the controller
        studentsController.deleteAdmin((long) 5, model, redirAttrs, (OAuth2AuthenticationToken) mockAuthentication);

        // Make sure delete NEVER gets called
        verify(sr, times(0)).delete(any());

        // Make sure we display a error message
        assert(redirAttrs.getFlashAttributes().containsKey("alertDanger"));

        // Assert model has correct attribute
        assert(model.getAttribute("students").equals(Arrays.asList(joe, carl)));
    }

    /**
     * This tests the csv upload functionality at /students/upload
     *
     * - > Case where a valid CSV is uploaded by an admin
     */
    @Test
    public void testValidCSV() throws Exception {
        // Create redirAttrs that the controller can write to (a success/error message)
        RedirectAttributes redirAttrs = new RedirectAttributesModelMap();

        // Mock the user role as an Admin
        when(ms.role(any())).thenReturn("Admin");

        // Prepare the temp csv to upload
        String csvContents =
            "Enrl Cd,Perm #,Grade,Final Units,Student Last,Student First Middle,Quarter,Course ID,Section,Meeting Time(s) / Location(s),Email,ClassLevel,Major1,Major2,Date/Time,Pronoun\n" +
            "\n" +
            "676,12345,,4.0,Gaucho,Joe,S20,CMPSC48,0100,T R   5:00- 6:15             M      2:00- 2:50 PHELP 1530  M      3:00- 3:50 PHELP 1530  ,joegaucho@ucsb.edu,JR,CMPSC,,4/9/2020 10:47:39 AM,";
        MockMultipartFile mockMultipartFile = new MockMultipartFile("students.csv","students.csv",
                "text/plain", csvContents.getBytes());

        // Call the controller
        studentsController.uploadCSV(mockMultipartFile,(OAuth2AuthenticationToken) mockAuthentication, redirAttrs);

        // Capture the argument X that the controller passed to sr.saveAll(X)
        final ArgumentCaptor<List<Student>> captor = ArgumentCaptor.forClass(List.class);
        verify(sr).saveAll(captor.capture());

        // Make sure captured value is correct
        assert(captor.getValue().size() == 1);
        assert(captor.getValue().get(0).getEmail().equals("joegaucho@ucsb.edu"));
        assert(captor.getValue().get(0).getFname().equals("Joe"));
        assert(captor.getValue().get(0).getLname().equals("Gaucho"));
        assert(captor.getValue().get(0).getPerm().equals("12345"));

        // Make sure we have no error
        assert(!redirAttrs.getFlashAttributes().containsKey("alertDanger"));
    }

    /**
     * This tests the csv upload functionality at /students/upload
     *
     * - > Case where an INVALID CSV is uploaded by an admimn
     */
    @Test
    public void testInvalidCSV() throws Exception {
        // Create redirAttrs that the controller can write to (a success/error message)
        RedirectAttributes redirAttrs = new RedirectAttributesModelMap();

        // Mock the user role as an Admin
        when(ms.role(any())).thenReturn("Admin");

        // Prepare the temp csv to upload
        String csvContents =
            "Random garbage that constitutes an invalid csv \n";
        MockMultipartFile mockMultipartFile = new MockMultipartFile("students.csv","students.csv",
                "text/plain", csvContents.getBytes());

        // Call the controller
        studentsController.uploadCSV(mockMultipartFile,(OAuth2AuthenticationToken) mockAuthentication, redirAttrs);

        // Capture the argument X that the controller passed to sr.saveAll(X)
        final ArgumentCaptor<List<Student>> captor = ArgumentCaptor.forClass(List.class);
        verify(sr).saveAll(captor.capture());

        // Make sure no students were imported
        assert(captor.getValue().isEmpty());
    }
}
