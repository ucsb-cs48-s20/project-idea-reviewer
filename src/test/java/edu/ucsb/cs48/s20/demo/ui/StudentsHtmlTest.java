package edu.ucsb.cs48.s20.demo.ui;

import edu.ucsb.cs48.s20.demo.advice.AuthControllerAdvice;
import edu.ucsb.cs48.s20.demo.advice.StudentFlowAdvice;
import edu.ucsb.cs48.s20.demo.controllers.StudentsController;
import edu.ucsb.cs48.s20.demo.entities.Student;
import edu.ucsb.cs48.s20.demo.repositories.AppUserRepository;
import edu.ucsb.cs48.s20.demo.repositories.StudentRepository;
import edu.ucsb.cs48.s20.demo.services.GoogleMembershipService;
import edu.ucsb.cs48.s20.demo.services.MembershipService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import utils.OAuthUtils;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

@SpringBootTest
@AutoConfigureMockMvc
@WebAppConfiguration
@RunWith(SpringRunner.class)
public class StudentsHtmlTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuthControllerAdvice aca;

    @MockBean
    private ClientRegistrationRepository crr;

    @MockBean
    private MembershipService ms;

    @MockBean
    private GoogleMembershipService gms;

    @MockBean
    private AppUserRepository aur;

    @MockBean
    private StudentRepository sr;

    @MockBean
    private StudentFlowAdvice studentFlowAdvice;

    @Autowired
    private StudentsController studentsController;

    private Authentication mockAuthentication;

    @Before
    public void setUp() {
        OAuth2User principal = OAuthUtils.createOAuth2User("Chris Gaucho", "cgaucho@ucsb.edu");
        mockAuthentication = OAuthUtils.getOauthAuthenticationFor(principal);
        when(ms.isMember((OAuth2AuthenticationToken) mockAuthentication)).thenReturn(true);
    }

    /**
     * This test makes sure admins can access the /students endpoint
     */
    @Test
    public void testAdminPageAccessForAdmins() throws Exception {
        // Set role to admin
        when(ms.role(any())).thenReturn("Admin");

        // Make sure page loads without redirect
        mvc.perform(MockMvcRequestBuilders.get("/students").with(authentication(mockAuthentication)).accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk());
        // Note: with(authentication(mockAuthentication)) is required to bypass Spring's HttpSecurity that would otherwise redirect guest users to the login page
    }

    /**
     * This test makes sure students can NOT access the /students endpoint
     */
    @Test
    public void testAdminPageAccessForStudents() throws Exception {
        // Set role to student
        when(ms.role(any())).thenReturn("Student");

        // Make sure page redirects (since student should not have access)
        mvc.perform(MockMvcRequestBuilders.get("/students").with(authentication(mockAuthentication)).accept(MediaType.TEXT_HTML))
        .andExpect(status().is3xxRedirection());
        // Note: with(authentication(mockAuthentication)) is required to bypass Spring's HttpSecurity that would otherwise redirect guest users to the login page
    }

    /**
     * This test makes sure the table has no rows when there is no students in the database
     */
    @Test
    public void testEmptyTable() throws Exception {
        // Set role to admin
        when(ms.role(any())).thenReturn("Admin");

        // Check students endpoint. Since DB has not been mocked, there will be no students in the table
        mvc.perform(MockMvcRequestBuilders.get("/students").with(authentication(mockAuthentication)).accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk())
        .andExpect(xpath("/html[@lang=\"en\"]/body/div[@class=\"container\"]/table[@class=\"bootstrap-table\"]/tbody//tr[1]")
                   .doesNotExist());
    }

    /**
     * This test makes sure the table has the correct number of rows when there are students in the database (2 rows)
     */
    @Test
    public void testTableWithStudents() throws Exception {
        // Set role to admin
        when(ms.role(any())).thenReturn("Admin");

        // Create two students to return from db
        Student s1 = mock(Student.class);
        when(s1.getFname()).thenReturn("Tom");
        Student s2 = mock(Student.class);
        when(s1.getFname()).thenReturn("Joe");

        // Mock database return
        when(sr.findAll()).thenReturn(Arrays.asList(s1,s2));

        // Make the call and ensure table rows 1 and 2 exist (not 0 and 1, html is weird)
        mvc.perform(MockMvcRequestBuilders.get("/students").with(authentication(mockAuthentication)).accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk())
        .andExpect(xpath("/html[@lang=\"en\"]/body/div[@class=\"container\"]/table[@class=\"bootstrap-table\"]/tbody//tr[1]")
                   .exists())
        .andExpect(xpath("/html[@lang=\"en\"]/body/div[@class=\"container\"]/table[@class=\"bootstrap-table\"]/tbody//tr[2]")
                   .exists())
        .andExpect(xpath("/html[@lang=\"en\"]/body/div[@class=\"container\"]/table[@class=\"bootstrap-table\"]/tbody//tr[3]")
                   .doesNotExist());

    }


}
