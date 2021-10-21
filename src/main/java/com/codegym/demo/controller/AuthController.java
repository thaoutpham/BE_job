package com.codegym.demo.controller;

import com.codegym.demo.constant.Constant;
import com.codegym.demo.dto.request.*;
import com.codegym.demo.dto.response.JwtResponse;
import com.codegym.demo.dto.response.Response;
import com.codegym.demo.model.Company;
import com.codegym.demo.model.User;
import com.codegym.demo.security.jwt.CompanyJwtService;
import com.codegym.demo.security.jwt.UserJwtService;
import com.codegym.demo.security.principal.CompanyPrinciple;
import com.codegym.demo.security.principal.UserPrinciple;
import com.codegym.demo.service.company.ICompanyService;
import com.codegym.demo.service.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.codegym.demo.dto.response.ResponseBody;

import javax.validation.Valid;
import java.util.Optional;

@RequestMapping("/auth")
@RestController
@CrossOrigin(origins = "*")
public class AuthController {
    @Autowired
    private IUserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserJwtService userJwtService;

    @Autowired
    private ICompanyService companyService;

    @Autowired
    private CompanyJwtService companyJwtService;


    @Autowired
    PasswordEncoder passwordEncoder;

    @PostMapping("/users/register")
    public ResponseEntity<?> register(@Validated @RequestBody UserRegisterForm registerForm, BindingResult bindingResult) {
        try {
            if (bindingResult.hasFieldErrors()) {
                return new ResponseEntity<>(new ResponseBody(Response.OBJECT_INVALID, null), HttpStatus.BAD_REQUEST);
            }
            if (companyService.existsByEmail(registerForm.getEmail()) || userService.existsByEmail(registerForm.getEmail())) {
                return new ResponseEntity<>(new ResponseBody(Response.EMAIL_IS_EXISTS, null), HttpStatus.CONFLICT);
            }
            User user = new User(registerForm.getName(), registerForm.getEmail(), registerForm.getPassword(), registerForm.getPhone());
            user.setType(Constant.TypeName.USER);
            return new ResponseEntity<>(new ResponseBody(Response.SUCCESS, userService.save(user)), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseBody(Response.SYSTEM_ERROR, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
        @PostMapping("/users/login")
    public ResponseEntity<ResponseBody> login(@Validated @RequestBody UserLoginForm loginForm) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginForm.getEmail(), loginForm.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = userJwtService.generateTokenLogin(authentication);
            UserPrinciple userPrinciple = (UserPrinciple) authentication.getPrincipal();
            User currentUser = userService.findByEmail(loginForm.getEmail()).get();
            return new ResponseEntity<>(new ResponseBody(Response.SUCCESS,
                    new JwtResponse(currentUser.getId(), jwt, currentUser.getName(), userPrinciple.getAuthorities())),
                    HttpStatus.OK);
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(new ResponseBody(Response.OBJECT_NOT_FOUND, null), HttpStatus.FORBIDDEN);
        }
    }
    @PostMapping("/companies/register")
    public ResponseEntity<ResponseBody> registerMerchant(@Validated @RequestBody CompanyRegisterForm registerForm, BindingResult bindingResult) {
        try {
            if (bindingResult.hasFieldErrors()) {
                return new ResponseEntity<>(new ResponseBody(Response.OBJECT_INVALID, null), HttpStatus.BAD_REQUEST);
            }
            if (companyService.existsByCompanyName(registerForm.getCompanyName())) {
                return new ResponseEntity<>(new ResponseBody(Response.NAME_IS_EXISTS, null), HttpStatus.CONFLICT);
            }
            if (companyService.existsByEmail(registerForm.getEmail()) || userService.existsByEmail(registerForm.getEmail())) {
                return new ResponseEntity<>(new ResponseBody(Response.EMAIL_IS_EXISTS, null), HttpStatus.CONFLICT);
            }

            String encode = passwordEncoder.encode(registerForm.getPassword());
            Company company = new Company(
                    registerForm.getCompanyName(),
                    registerForm.getShortName(),
                    registerForm.getEmail(),
                    encode,
                    registerForm.getDescription());
            company.setType(Constant.TypeName.COMPANY);
            companyService.save(company);
            String companyCode = company.getShortName().substring(0, 3) + company.getId() + (int) (Math.random() * (9999 - 1000) + 1000);
            company.setCompanyCode(companyCode);
            return new ResponseEntity<>(new ResponseBody(Response.SUCCESS, companyService.save(company)), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseBody(Response.SYSTEM_ERROR, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/companies/login")
    public ResponseEntity<ResponseBody> login(@Validated @RequestBody CompanyLoginForm companyLoginForm) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(companyLoginForm.getEmail(), companyLoginForm.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = companyJwtService.generateTokenLogin(authentication);
            CompanyPrinciple companyPrinciple = (CompanyPrinciple) authentication.getPrincipal();
            Company company = companyService.findByEmail(companyLoginForm.getEmail()).get();
            return new ResponseEntity<>(new ResponseBody(Response.SUCCESS,
                    new JwtResponse(company.getId(), jwt, company.getCompanyName(), companyPrinciple.getAuthorities())),
                    HttpStatus.OK);
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(new ResponseBody(Response.OBJECT_NOT_FOUND, null), HttpStatus.FORBIDDEN);
        }
    }
    @PostMapping("/companies/{id}/change-password")
    public ResponseEntity<?> changeCompanyPassword(@Valid @RequestBody CompanyPasswordForm companyPasswordForm, @PathVariable Long id, BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors()) {
            return new ResponseEntity<>(new ResponseBody(Response.OBJECT_INVALID, null), HttpStatus.BAD_REQUEST);
        }
        Optional<Company> company = companyService.findById(id);
        if (!company.isPresent()) {
            return new ResponseEntity<>(new ResponseBody(Response.SYSTEM_ERROR, null), HttpStatus.NOT_FOUND);
        }
        if (companyPasswordForm.getNewPassword().trim().equals(companyPasswordForm.getCurrentPassword().trim())) {
            return new ResponseEntity<>(new ResponseBody(Response.NEW_PASSWORD_IS_DUPLICATED, null), HttpStatus.CONFLICT);
        }
        boolean matches = passwordEncoder.matches(companyPasswordForm.getCurrentPassword(), company.get().getPassword());
        if (companyPasswordForm.getNewPassword() != null) {
            if (matches) {
                company.get().setPassword(passwordEncoder.encode(companyPasswordForm.getNewPassword().trim()));
                companyService.save(company.get());
            } else {
                return new ResponseEntity<>(new ResponseBody(Response.PASSWORD_IS_NOT_TRUE, null), HttpStatus.CONFLICT);
            }
        }
        return new ResponseEntity<>(new ResponseBody(Response.SUCCESS, company.get()), HttpStatus.OK);
    }
    @PostMapping("/users/{id}/change-password")
    public ResponseEntity<?> changeUserPassword(@Valid @RequestBody UserPasswordForm userPasswordForm, @PathVariable Long id, BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors()) {
            return new ResponseEntity<>(new ResponseBody(Response.OBJECT_INVALID, null), HttpStatus.BAD_REQUEST);
        }
        Optional<User> user = userService.findById(id);
        if (!user.isPresent()) {
            return new ResponseEntity<>(new ResponseBody(Response.SYSTEM_ERROR, null), HttpStatus.NOT_FOUND);
        }
        if (userPasswordForm.getNewPassword().trim().equals(userPasswordForm.getCurrentPassword().trim())) {
            return new ResponseEntity<>(new ResponseBody(Response.NEW_PASSWORD_IS_DUPLICATED, null), HttpStatus.CONFLICT);
        }
        boolean matches = passwordEncoder.matches(userPasswordForm.getCurrentPassword(), user.get().getPassword());
        if (userPasswordForm.getNewPassword() != null) {
            if (matches) {
                user.get().setPassword(passwordEncoder.encode(userPasswordForm.getNewPassword().trim()));
                userService.save(user.get());
            } else {
                return new ResponseEntity<>(new ResponseBody(Response.PASSWORD_IS_NOT_TRUE, null), HttpStatus.CONFLICT);
            }
        }
        return new ResponseEntity<>(new ResponseBody(Response.SUCCESS, user.get()), HttpStatus.OK);
    }
}