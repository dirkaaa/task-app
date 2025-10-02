package com.matekoncz.task_manager.service;

import com.matekoncz.task_manager.exceptions.auth.WrongUsernameOrPasswordException;
import com.matekoncz.task_manager.exceptions.user.UserCanNotBeCreatedException;
import com.matekoncz.task_manager.exceptions.user.UserNameIsNotUniqueException;
import com.matekoncz.task_manager.exceptions.user.UserNotFoundException;
import com.matekoncz.task_manager.model.User;
import com.matekoncz.task_manager.repository.UserRepository;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) throws UserCanNotBeCreatedException, UserNameIsNotUniqueException{
        try{
            getUserByUsername(user.getUsername());
            throw new UserNameIsNotUniqueException();
        } catch (UserNotFoundException e) {
            validateUser(user);
            String hashedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(hashedPassword);
            return userRepository.save(user);
        }
    }

    private void validateUser(User user) throws UserCanNotBeCreatedException {
        if (user.getUsername() == null || user.getUsername().isBlank() ||
                user.getPassword() == null || user.getPassword().isBlank()) {
            throw new UserCanNotBeCreatedException();
        }
    }

    public User getUserById(Long id) throws UserNotFoundException {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    public User getUserByUsername(String username) throws UserNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
    }

    public User authenticate(String username, String password) throws WrongUsernameOrPasswordException {
        User user;
        try {
            user = getUserByUsername(username);
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new WrongUsernameOrPasswordException();
            }
        return user;
        } catch (UserNotFoundException e) {
            throw new WrongUsernameOrPasswordException();
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }
}