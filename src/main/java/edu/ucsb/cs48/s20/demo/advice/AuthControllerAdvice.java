package edu.ucsb.cs48.s20.demo.advice;

import org.springframework.web.bind.annotation.ModelAttribute;

import edu.ucsb.cs48.s20.demo.controllers.AppUsersController;
import edu.ucsb.cs48.s20.demo.entities.AppUser;
import edu.ucsb.cs48.s20.demo.repositories.AppUserRepository;
import edu.ucsb.cs48.s20.demo.services.MembershipService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class AuthControllerAdvice {

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private AppUserRepository appUserRepository;

    @ModelAttribute("isLoggedIn")
    public boolean getIsLoggedIn(OAuth2AuthenticationToken token) {
        updateLoginTable(token);
        return token != null;
    }

    @ModelAttribute("fname")
    public String getFirstName(OAuth2AuthenticationToken token) {
        return membershipService.firstName(token);
    }

    @ModelAttribute("lname")
    public String getLastName(OAuth2AuthenticationToken token) {
        return membershipService.lastName(token);
    }

    @ModelAttribute("name")
    public String getName(OAuth2AuthenticationToken token) {
        return membershipService.name(token);
    }

    @ModelAttribute("email")
    public String getEmail(OAuth2AuthenticationToken token) {
        return membershipService.email(token);
    }

    @ModelAttribute("picture")
    public String getPicture(OAuth2AuthenticationToken token) {
        if (token == null) return "";
        if (token.getPrincipal().getAttributes().get("picture") == null) return "";
        return token.getPrincipal().getAttributes().get("picture").toString();
    }

    @ModelAttribute("isMember")
    public boolean getIsMember(OAuth2AuthenticationToken token) {
        return membershipService.isMember(token);
    }

    @ModelAttribute("isAdmin")
    public boolean getIsAdmin(OAuth2AuthenticationToken token) {
        return membershipService.isAdmin(token);
    }

    @ModelAttribute("isStudent")
    public boolean getIsStudent(OAuth2AuthenticationToken token) {
        return membershipService.isStudent(token);
    }

    @ModelAttribute("role")
    public String getRole(OAuth2AuthenticationToken token) {
        return membershipService.role(token);
    }

    private void updateLoginTable(OAuth2AuthenticationToken token) {
        if (token==null) return;

        String email = membershipService.email(token);
        if (email == null) return;

        List<AppUser> appUsers = appUserRepository.findByEmail(email);

        if (appUsers.size()==0) {
            // No user with this email is in the AppUsers table yet, so add one
            AppUser u = new AppUser();
            u.setEmail(email);
            u.setName(membershipService.name(token));
            appUserRepository.save(u);
        }
    }
}