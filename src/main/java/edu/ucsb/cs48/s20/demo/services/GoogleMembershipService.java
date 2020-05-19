package edu.ucsb.cs48.s20.demo.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import edu.ucsb.cs48.s20.demo.repositories.AdminRepository;
import edu.ucsb.cs48.s20.demo.repositories.StudentRepository;

/**
 * Service object that wraps the UCSB Academic Curriculum API
 */
@Service
@Primary
public class GoogleMembershipService implements MembershipService {

    private Logger logger = LoggerFactory.getLogger(GoogleMembershipService.class);

    // The trailing colon sets empty string as the default
    // The DefaultConversionService in Config.java allows the List to be used.

    @Value("${app.admin.emails:}")
    final private List<String> adminEmails = new ArrayList<String>();

    @Value("${app.member.hosted-domain}")
    final private String memberHostedDomain = "";

    @Autowired
    private OAuth2AuthorizedClientService clientService;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private StudentRepository studentRepository;

    /**
     * @param token OAuth token
     * @return true if current logged-in user is a member but not an admin
     */
    public boolean isMember(OAuth2AuthenticationToken token) {
        return hasRole(token, "member");
    }

    /**
     * @param token OAuth token
     * @return true if current logged-in user is an Admin
     */

    public boolean isAdmin(OAuth2AuthenticationToken token) {
        return hasRole(token, "admin");
    }

    /**
     * @param token OAuth token
     * @return true if current logged-in user is an Admin
     */

    public boolean isStudent(OAuth2AuthenticationToken token) {
        return hasRole(token, "student");
    }

    /**
     * does current logged in user have this role
     *
     * @param token      OAuth token
     * @param roleToTest "member" or "admin" (lowercase)
     * @return if the current logged in user has that role
     */

    public boolean hasRole(OAuth2AuthenticationToken token, String roleToTest) {

        logger.info("adminEmails=[" + adminEmails + "]");

        if (token == null) {
            return false;
        }
        if (clientService == null) {
            logger.error(String.format("unable to obtain autowired clientService"));
            return false;
        }

        OAuth2User oAuth2User = token.getPrincipal();

        String email = (String) oAuth2User.getAttributes().get("email");
        // hd is the domain of the email, e.g. ucsb.edu
        String hostedDomain = (String) oAuth2User.getAttributes().get("hd");

        logger.info("email=[" + email + "]");
        logger.info("hostedDomain=" + hostedDomain);

        if (roleToTest.equals("admin") && isAdminEmail(email)) {
            return true;
        }

        if (roleToTest.equals("student") && isStudentEmail(email)) {
            return true;
        }

        if (roleToTest.equals("member") && memberHostedDomain.equals(hostedDomain)) {
            return true;
        }

        return false;
    }

    private boolean isAdminEmail(String email) {
        return !adminRepository.findByEmail(email).isEmpty() || (adminEmails.contains(email));
    }

    private boolean isStudentEmail(String email) {
        return !studentRepository.findByEmail(email).isEmpty();
    }

    public List<String> getAdminEmails() {
        return adminEmails;
    }

    public String name(OAuth2AuthenticationToken token) {
        if (token == null)
            return "";
        return token.getPrincipal().getAttributes().get("name").toString();
    }

    public String firstName(OAuth2AuthenticationToken token) {
        if (token == null)
            return "";
        return token.getPrincipal().getAttributes().get("given_name").toString();
    }

    public String lastName(OAuth2AuthenticationToken token) {
        if (token == null)
            return "";
        try {
            return token.getPrincipal().getAttributes().get("family_name").toString();
        } catch(NullPointerException e) {
            return "";
        }
    }

    public String email(OAuth2AuthenticationToken token) {
        if (token == null)
            return "";
        return token.getPrincipal().getAttributes().get("email").toString();
    }

}
