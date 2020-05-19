package edu.ucsb.cs48.s20.demo.ui;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import edu.ucsb.cs48.s20.demo.advice.AuthControllerAdvice;
import edu.ucsb.cs48.s20.demo.advice.StudentFlowAdvice;
import edu.ucsb.cs48.s20.demo.entities.ProjectIdea;
import edu.ucsb.cs48.s20.demo.entities.Student;
import edu.ucsb.cs48.s20.demo.repositories.AppUserRepository;
import edu.ucsb.cs48.s20.demo.repositories.ProjectIdeaRepository;
import edu.ucsb.cs48.s20.demo.repositories.StudentRepository;
import edu.ucsb.cs48.s20.demo.services.MembershipService;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.security.oauth2.core.user.OAuth2User;
import utils.OAuthUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.junit.Before;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import org.springframework.test.context.web.WebAppConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@WebAppConfiguration
@RunWith(SpringRunner.class)
public class HomePageHTMLTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuthControllerAdvice aca;

    @MockBean
    private StudentFlowAdvice sfa;

    @MockBean
    private ClientRegistrationRepository crr;

    @MockBean
    private MembershipService ms;

    @MockBean
    private AppUserRepository aur;

    @MockBean
    private ProjectIdeaRepository pir;

    @MockBean
    private StudentRepository sr;

    private Authentication mockAuthentication;

    @Before
    public void setUp() {
        final OAuth2User principal = OAuthUtils.createOAuth2User("Chris Gaucho", "cgaucho@ucsb.edu");
        mockAuthentication = OAuthUtils.getOauthAuthenticationFor(principal);
        when(ms.isMember((OAuth2AuthenticationToken) mockAuthentication)).thenReturn(true);
    }

    /*generic test to check that any request to home page succeeds*/
    @Test
    public void getHomePage_ContentType() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
        .andExpect(content().contentType("text/html;charset=UTF-8"));
    }

    /*test content of the page- title should be "CS48 demo"*/
    @Test
    public void getHomePage_hasCorrectTitle() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
        .andExpect(xpath("//title").exists())
        .andExpect(xpath("//title").string("CS48 demo"));
    }

    /*for some reason, these blocks aren't in a check that the user is a student- might wanna change*/
    /*test that student with no submitted ideas and should see the text box*/
    @Test
    public void studentWithNoSubmissions_hasTextBox() throws Exception {
        when(sfa.needsToSubmitProjectIdea((OAuth2AuthenticationToken) mockAuthentication)).thenReturn(true);
        // when(aca.isStudent((OAuth2AuthenticationToken) mockAuthentication))).return(true);
        mvc.perform(MockMvcRequestBuilders.get("/").with(authentication(mockAuthentication)).accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk()).andExpect(xpath("/html/body/div/div[1]/p[1]").string("To get started, submit your project idea below."))
        .andExpect(xpath("//*[@id='details']").exists());

    }

    /*a student who has submitted will not see the text box*/
    @Test
    public void studentWithASubmission_hasNoTextBox() throws Exception {
        when(sfa.needsToSubmitProjectIdea((OAuth2AuthenticationToken) mockAuthentication)).thenReturn(false);
        // when(aca.isStudent((OAuth2AuthenticationToken) mockAuthentication))).return(true);
        mvc.perform(MockMvcRequestBuilders.get("/").with(authentication(mockAuthentication)).accept(MediaType.TEXT_HTML))
        .andExpect(xpath("//*[@id='details']").doesNotExist());
    }

    /*when a student has submitted an idea, it is displayed on the page*/
    @Test
    public void studentWithSubmission_displaysIdea() throws Exception {
        when(aca.getIsStudent((OAuth2AuthenticationToken) mockAuthentication)).thenReturn(true);
        when(sfa.needsToSubmitProjectIdea((OAuth2AuthenticationToken) mockAuthentication)).thenReturn(false);

        String expectedTitle = "Idea0";
        String expectedDeets = "THE DEETS";

        ProjectIdea pi = new ProjectIdea();
        pi.setTitle(expectedTitle);
        pi.setDetails(expectedDeets);

        Student person = new Student();
        person.setProjectIdea(pi);

        when(sfa.getStudent((OAuth2AuthenticationToken) mockAuthentication)).thenReturn(person);

        mvc.perform(MockMvcRequestBuilders.get("/").with(authentication(mockAuthentication)).accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
        .andExpect(xpath("/html/body/div/div/div/h2").string("Your Project Idea"))
        .andExpect(xpath("/html/body/div/div/div/table/tbody/tr[1]/th").string("Title"))
        .andExpect(xpath("/html/body/div/div/div/table/tbody/tr[1]/td").string(expectedTitle))
        .andExpect(xpath("/html/body/div/div/div/table/tbody/tr[2]/th").string("Details"))
        .andExpect(xpath("/html/body/div/div/div/table/tbody/tr[2]/td").string(expectedDeets));

    }

    /*when a student has submitted an idea, there is a flash message indicating how many ideas they need to review*/
    @Test
    public void studentWithSubmission_hasReviewFlashMsg() throws Exception {
        ProjectIdea pi = new ProjectIdea();
        pi.setTitle("someTitle");
        pi.setDetails("someDeets");

        Student person = new Student();
        person.setProjectIdea(pi);

        when(aca.getIsStudent((OAuth2AuthenticationToken) mockAuthentication)).thenReturn(true);
        when(sfa.needsToSubmitProjectIdea((OAuth2AuthenticationToken) mockAuthentication)).thenReturn(false);
        when(sfa.getReviewsNeeded((OAuth2AuthenticationToken) mockAuthentication)).thenReturn(5);
        when(sfa.getStudent((OAuth2AuthenticationToken) mockAuthentication)).thenReturn(person);

        mvc.perform(MockMvcRequestBuilders.get("/").with(authentication(mockAuthentication)).accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
        .andExpect(xpath("/html/body/div/div/div/div/div/span").string("You must review 5 more project ideas. "));
    }

}
