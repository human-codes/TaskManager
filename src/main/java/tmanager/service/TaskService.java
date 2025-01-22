package tmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tmanager.entity.Task;
import tmanager.entity.User;
import tmanager.entity.enums.TaskStatus;
import tmanager.repository.TaskRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;


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
        return 2; // More than 2 days away (this will trigger the blue background)
    }


    public void updateTaskStatuses() {
        List<Task> tasks = taskRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        for (Task task : tasks) {
            if (task.getDeadline() != null && task.getDeadline().isBefore(now) && task.getStatus() != TaskStatus.COMPLETED) {
                task.setStatus(TaskStatus.EXPIRED);
                taskRepository.save(task);
            }
        }
    }


}
