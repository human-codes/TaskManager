package tmanager.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TaskService {
    public int getDeadlineStatus(LocalDateTime deadline) {
        LocalDateTime now = LocalDateTime.now();

        if (deadline == null) {
            return 3; // Status for null deadlines
        }
        if (deadline.isBefore(now)) {
            return 0; // Deadline passed
        }
        if (deadline.isBefore(now.plusDays(2))) {
            return 1; // Within 2 days
        }
        return 2; // More than 2 days away
    }
}
