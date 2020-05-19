package edu.ucsb.cs48.s20.demo.services;

import java.util.List;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

public interface MembershipService {

    /**
     * @param token OAuth token
     * @return true if current logged-in user is a member but not an admin
     */

    public boolean isMember(OAuth2AuthenticationToken token);

    /**
     * @param token OAuth token
     * @return true if current logged-in user is an Admin
     */

    public boolean isAdmin(OAuth2AuthenticationToken token);

    /**
     * @param token OAuth token
     * @return true if current logged-in user is a Student
     */

    public boolean isStudent(OAuth2AuthenticationToken token);

    /**
     * @param token OAuth token
     * @return true if current logged-in user is an Admin or a Member
     */

    default public boolean isMemberOrAdmin(OAuth2AuthenticationToken token) {
        return isMember(token) || isAdmin(token);
    }

    default public String role(OAuth2AuthenticationToken token) {
        if (token == null)
            return "Guest";
        if (isAdmin(token))
            return "Admin";
        if (isStudent(token))
            return "Student";
        if (isMember(token))
            return "Member";
        return "Guest";
    }

    public List<String> getAdminEmails();

    public String name(OAuth2AuthenticationToken oAuth2AuthenticationToken);

    public String firstName(OAuth2AuthenticationToken oAuth2AuthenticationToken);

    public String lastName(OAuth2AuthenticationToken oAuth2AuthenticationToken);

    public String email(OAuth2AuthenticationToken oAuth2AuthenticationToken);

}