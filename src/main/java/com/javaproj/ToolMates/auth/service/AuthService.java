package com.javaproj.ToolMates.auth.service;

import com.javaproj.ToolMates.auth.dto.LoginRequest;
import com.javaproj.ToolMates.auth.dto.SignupRequest;
import com.javaproj.ToolMates.auth.exception.DuplicateFieldException;
import com.javaproj.ToolMates.auth.exception.InvalidCredentialsException;
import com.javaproj.ToolMates.auth.model.User;
import com.javaproj.ToolMates.auth.repository.UserDao;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired private UserDao userDao;
    @Autowired private PasswordEncoder passwordEncoder;

    public User register(SignupRequest req) {
        validateCuetStudentEmail(req.getStudentEmail());

        if (userDao.existsByStudentId(req.getStudentId()))
            throw new DuplicateFieldException("studentId", "This Student ID is already registered.");

        if (userDao.existsByStudentEmail(req.getStudentEmail().toLowerCase()))
            throw new DuplicateFieldException("studentEmail", "This email address is already registered.");

        User user = new User();
        user.setFirstName(req.getFirstName().trim());
        user.setLastName(req.getLastName().trim());
        user.setStudentId(req.getStudentId().trim());
        user.setDepartment(req.getDepartment());
        user.setStudentEmail(req.getStudentEmail().toLowerCase().trim());
        user.setPhone(req.getPhone().trim());
        user.setAddress(req.getAddress() != null ? req.getAddress().trim() : null);
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setEmailVerified(true);

        return userDao.save(user);
    }

    public User login(LoginRequest req, HttpSession session) {
        String identifier = req.getUsername().trim();

        Optional<User> optUser = userDao.findByStudentId(identifier);
        if (optUser.isEmpty()) {
            optUser = userDao.findByEmail(identifier.toLowerCase());
        }

        User user = optUser.orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        int maxAge = req.isRememberMe() ? 60 * 60 * 24 * 30 : -1;
        session.setMaxInactiveInterval(maxAge);
        session.setAttribute("userId",    user.getUserId());
        session.setAttribute("studentId", user.getStudentId());
        session.setAttribute("email",     user.getStudentEmail());

        return user;
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    private void validateCuetStudentEmail(String email) {
        if (email == null || !email.trim().toLowerCase().matches("^[a-z0-9._%+-]+@student\\.cuet\\.ac\\.bd$")) {
            throw new IllegalArgumentException("Please enter a valid CUET student email address.");
        }
    }
}
