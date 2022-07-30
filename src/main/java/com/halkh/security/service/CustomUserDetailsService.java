package com.halkh.security.service;

import com.halkh.security.entity.Privilege;
import com.halkh.security.entity.Role;
import com.halkh.security.entity.User;
import com.halkh.security.exception.UserNotFoundException;
import com.halkh.security.repository.RoleRepository;
import com.halkh.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.function.Supplier;

@Service("userDetailsService")
@Transactional
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {


        User user = userRepository.findByUsername(email).orElseThrow(() -> new UserNotFoundException("Customer with email {%s} does not exist"));

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), true, true, true, true, getAuthorities(user.getRoles()));
    }

    private Collection<? extends GrantedAuthority> getAuthorities(
            Collection<Role> roles) {

        return getGrantedAuthorities(getPrivileges(roles));
    }

    private List<String> getPrivileges(Collection<Role> roles) {

        List<String> privileges = new ArrayList<>();
        List<Privilege> collection = new ArrayList<>();
        roles.forEach((role -> {
            privileges.add(role.getName());
            collection.addAll(role.getPrivileges());
        }));

        collection.forEach((item -> {
            privileges.add(item.getName());
        }));
        return privileges;
    }

    private List<GrantedAuthority> getGrantedAuthorities(List<String> privileges) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        privileges.forEach((privilege) -> {
            authorities.add(new SimpleGrantedAuthority(privilege));
        });
        return authorities;
    }
}
