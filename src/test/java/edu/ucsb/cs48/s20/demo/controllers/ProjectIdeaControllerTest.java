package edu.ucsb.cs48.s20.demo.controllers;

import java.util.Optional;

import edu.ucsb.cs48.s20.demo.advice.AuthControllerAdvice;
import edu.ucsb.cs48.s20.demo.advice.StudentFlowAdvice;
import edu.ucsb.cs48.s20.demo.entities.Student;
import edu.ucsb.cs48.s20.demo.entities.ProjectIdea;
import edu.ucsb.cs48.s20.demo.formbeans.Idea;
import edu.ucsb.cs48.s20.demo.repositories.AppUserRepository;
import edu.ucsb.cs48.s20.demo.repositories.ProjectIdeaRepository;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import utils.OAuthUtils;
import java.util.Arrays;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
@WebAppConfiguration
@RunWith(SpringRunner.class)
public class ProjectIdeaControllerTest {
    @MockBean
    private AuthControllerAdvice aca;

    @MockBean
    private ClientRegistrationRepository crr;

    @MockBean
    private MembershipService ms;

    @Autowired
    private ProjectIdeaController projectIdeaController;

    @MockBean
    private GoogleMembershipService gms;

    @MockBean
    private AppUserRepository aur;

    @MockBean
    private ProjectIdeaRepository pir;

    @MockBean
    private StudentRepository sr;

    @MockBean
    private StudentFlowAdvice sfa;

    private Authentication mockAuthentication;

    @Before
    public void setUp() {
        OAuth2User principal = OAuthUtils.createOAuth2User("Chris Gaucho", "cgaucho@ucsb.edu");
        mockAuthentication = OAuthUtils.getOauthAuthenticationFor(principal);
        when(ms.isMember((OAuth2AuthenticationToken) mockAuthentication)).thenReturn(true);
    }

    /*after valid submission, there is a redirect to idea reviews page*/
    /*when a student submits a VALID idea (title/deets are proper lengths), it is added to the idea db and assigned to the student*/
    @Test
    public void addValidIdea_saveToDb() throws Exception {
        Model model = new ExtendedModelMap();
        RedirectAttributes redirAttrs = new RedirectAttributesModelMap();

        Idea idea = new Idea();
        idea.setTitle("TITLE");
        idea.setDetails("This is a valid description because it is more than 30 characters.");

        Student jorbus = new Student();
        jorbus.setId(5);

        when(ms.role((OAuth2AuthenticationToken) mockAuthentication)).thenReturn("Student");
        when(sfa.getStudent((OAuth2AuthenticationToken) mockAuthentication)).thenReturn(jorbus);

        String ret = projectIdeaController.addIdea(idea, model, (OAuth2AuthenticationToken) mockAuthentication, null, redirAttrs);

        assert(ret.equals("redirect:/reviews/perform"));
        assert(model.getAttribute("titleHasErrors").equals(false));
        assert(model.getAttribute("detailHasErrors").equals(false));
        final ArgumentCaptor<ProjectIdea> captor = ArgumentCaptor.forClass(ProjectIdea.class);
        verify(pir, times(1)).save(captor.capture());
        verify(sr, times(1)).save(jorbus);
        assert(captor.getValue().getStudent().equals(jorbus));
        assert(captor.getValue().getTitle().equals("TITLE"));
        assert(captor.getValue().getDetails().equals("This is a valid description because it is more than 30 characters."));
        assert(jorbus.getProjectIdea().equals(captor.getValue()));
    }


    /*submitting an invalid idea sets model attrs titleHasErrors, detailsHasErrors and adds title/detail err strings to model*/
    /*redirect to index page*/
    @Test
    public void addInvalidIdea_noSaveToDb() throws Exception {
        Model model = new ExtendedModelMap();
        RedirectAttributes redirAttrs = new RedirectAttributesModelMap();

        Idea idea = new Idea();
        idea.setTitle("");
        idea.setDetails("");

        Student jorbus = new Student();
        jorbus.setId(5);

        when(ms.role((OAuth2AuthenticationToken) mockAuthentication)).thenReturn("Student");
        when(sfa.getStudent((OAuth2AuthenticationToken) mockAuthentication)).thenReturn(jorbus);

        String ret = projectIdeaController.addIdea(idea, model, (OAuth2AuthenticationToken) mockAuthentication, null, redirAttrs);

        assert(ret.equals("index"));
        assert(model.getAttribute("titleHasErrors").equals(true));
        assert(model.getAttribute("titleErrors").equals("Title is too short (4 characters minimum). Currently: 0 chars."));
        assert(model.getAttribute("detailHasErrors").equals(true));
        assert(model.getAttribute("detailErrors").equals("Please add some more detail (at least 30 characters). Currently: 0 chars."));

        verify(pir, times(0)).save(any());
        verify(sr, times(0)).save(any());
        assert(model.getAttribute("idea").equals(idea));
    }

    /*DELETE*/
    /*only an admin can perform this action*/
    @Test
    public void deleteNotAdmin_redirectWithFlashMsg() throws Exception {
        Model model = new ExtendedModelMap();
        RedirectAttributes redirAttrs = new RedirectAttributesModelMap();

        when(ms.role((OAuth2AuthenticationToken) mockAuthentication)).thenReturn("Student");

        String ret = projectIdeaController.deleteIdea((long) 5, model, redirAttrs, (OAuth2AuthenticationToken) mockAuthentication);

        assert(ret.equals("redirect:/"));
        verify(pir, times(0)).delete(any());
        verify(sr, times(0)).save(any());
        assert(redirAttrs.getFlashAttributes().containsKey("alertDanger"));
        assert(redirAttrs.getFlashAttributes().containsValue("You do not have permission to access that page"));
    }

    /*try to delete an idea that does not exist- no deletion triggered)*/
    @Test
    public void deleteIdeaThatDNE_noDbDeletion() throws Exception {
        Model model = new ExtendedModelMap();
        RedirectAttributes redirAttrs = new RedirectAttributesModelMap();

        when(ms.role((OAuth2AuthenticationToken) mockAuthentication)).thenReturn("Admin");

        String ret = projectIdeaController.deleteIdea((long) 5, model, redirAttrs, (OAuth2AuthenticationToken) mockAuthentication);

        assert(ret.equals("redirect:/ideas"));
        verify(pir, times(1)).findById((long)5);
        verify(sr, times(0)).save(any());
        assert(redirAttrs.getFlashAttributes().containsKey("alertDanger"));
        assert(redirAttrs.getFlashAttributes().containsValue("ProjectIdea with that id does not exist."));
    }

    /*try to delete valid idea- delete on idea repo triggered, save on student- check redirectAttrs for success msg*/
    /*redirect to ideas index page*/
    @Test
    public void deleteIdeaThatExists_dbDeletion() throws Exception {
        Model model = new ExtendedModelMap();
        RedirectAttributes redirAttrs = new RedirectAttributesModelMap();

        when(ms.role((OAuth2AuthenticationToken) mockAuthentication)).thenReturn("Admin");

        Student jorbus = new Student();
        jorbus.setFname("Jorbus");
        jorbus.setLname("The III");
        jorbus.setId(5);

        ProjectIdea pi = new ProjectIdea();
        pi.setTitle("TITLE");
        pi.setDetails("This is a valid description because it is more than 30 characters.");
        pi.setId(5);
        pi.setStudent(jorbus);

        when(pir.findById((long) 5)).thenReturn(Optional.of(pi));

        String ret = projectIdeaController.deleteIdea((long) 5, model, redirAttrs, (OAuth2AuthenticationToken) mockAuthentication);

        assert(ret.equals("redirect:/ideas"));
        verify(pir, times(1)).findById((long)5);
        verify(sr, times(1)).save(jorbus);
        assert(redirAttrs.getFlashAttributes().containsKey("alertSuccess"));
        assert(redirAttrs.getFlashAttributes().containsValue("Idea TITLE for student Jorbus The III successfully deleted."));
    }

    /*INDEX PAGE*/
    /*redirect when not logged in as admin*/
    public void indexNotAdmin_redirectWithFlashMsg() throws Exception {
        Model model = new ExtendedModelMap();
        RedirectAttributes redirAttrs = new RedirectAttributesModelMap();

        when(ms.role((OAuth2AuthenticationToken) mockAuthentication)).thenReturn("Student");

        String ret = projectIdeaController.ideas(model, (OAuth2AuthenticationToken) mockAuthentication, redirAttrs);

        assert(ret.equals("redirect:/"));
        assert(redirAttrs.getFlashAttributes().containsKey("alertDanger"));
        assert(redirAttrs.getFlashAttributes().containsValue("You do not have permission to access that page"));
    }

    /*success when admin*/
    @Test
    public void indexAdmin_showIdeaList() throws Exception {
        Model model = new ExtendedModelMap();
        RedirectAttributes redirAttrs = new RedirectAttributesModelMap();

        ProjectIdea p1 = new ProjectIdea();
        p1.setId(1);
        ProjectIdea p2 = new ProjectIdea();
        p2.setId(2);
        ProjectIdea p3 = new ProjectIdea();
        p3.setId(3);

        Iterable<ProjectIdea> pi_list = Arrays.asList(p1,p2, p3);
        when(ms.role((OAuth2AuthenticationToken) mockAuthentication)).thenReturn("Admin");
        when(pir.findAll()).thenReturn(pi_list);

        String ret = projectIdeaController.ideas(model, (OAuth2AuthenticationToken) mockAuthentication, redirAttrs);

        assert(ret.equals("ideas/index"));
        verify(pir, times(1)).findAll();
        assert(model.getAttribute("ideas").equals(pi_list));


    }
}