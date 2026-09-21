package com.taskmanager.service;

import com.taskmanager.entity.Role;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication is required");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));
    }

    public void requireOwner(Task task) {
        User currentUser = getCurrentUser();

        if (task.getOwner() == null
                || !task.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(
                    "Only the task owner may perform this action");
        }
    }

    public void requireOwnerOrAdmin(Task task) {
        User currentUser = getCurrentUser();
        boolean ownsTask = task.getOwner() != null
                && task.getOwner().getId().equals(currentUser.getId());

        if (!ownsTask && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException(
                    "Only the task owner or an administrator may view this task");
        }
    }
}
