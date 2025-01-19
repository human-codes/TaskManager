package tmanager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tmanager.entity.*;
import tmanager.entity.enums.TaskStatus;
import tmanager.repository.*;
import tmanager.service.CardService;
import tmanager.service.TaskService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class BoardPageController {

    private final BoardRepository boardRepository;
    private final CardRepository cardRepository;
    private final CardService cardService;
    private final AttachmentRepository attachmentRepository;
    private final AttachmentContentRepository attachmentContentRepository;
    private final TaskRepository taskRepository;
    private final TaskService taskService;
    private final UserRepository userRepository;

    @GetMapping("/")
    public String boardPage(
            @RequestParam(required = false) UUID boardId,
            Model model
    ) {

        List<Board> boards = boardRepository.findAll();
        List<User> users = userRepository.findAll();

        Board selectedBoard = null;
        List<Card> cardsByBoard = List.of();
        List<Task> tasksByCards = List.of();

        if (boardId != null) {
            selectedBoard = boardRepository.findById(boardId).orElse(null);
            System.out.println(boardId);
            if (selectedBoard != null) {
                cardsByBoard = cardRepository.findByBoard_Id(boardId);
                for (Card card : cardsByBoard) {
                    if (card.isFinisher()){
                        List<Task> tasksByBoardId = taskRepository.findTasksByBoardId(card.getBoard().getId());
                        for (Task tasksByCard : tasksByBoardId) {
                            int deadlineStatus = taskService.getDeadlineStatus(tasksByCard.getDeadline());
                            if (deadlineStatus==0){
                                tasksByCard.setCard(card);
                                taskRepository.save(tasksByCard);
                            }
//                            else {
//                                Card card1 = cardsByBoard.get(0);
//                                tasksByCard.setCard(card1);
//                                taskRepository.save(tasksByCard);
//                            }
                        }
                       cardsByBoard = cardService.sortCards(cardsByBoard);
                       break;
                    }
                }
                tasksByCards = cardRepository.findTasksByCards(cardsByBoard);
            }
        }

        model.addAttribute("tasks", tasksByCards);
        model.addAttribute("cards", cardsByBoard);
        model.addAttribute("selectedBoard", selectedBoard);
        model.addAttribute("boards", boards);
        model.addAttribute("taskService", taskService);
        model.addAttribute("users", users);

        return "boardpage";
    }


    @GetMapping("/addboard")
    public String saveBoardPage(){
        return "addboardpage";
    }
    @PostMapping("/saveBoard")
    public String saveBoard(@ModelAttribute Board board) {
        boardRepository.save(board);
        return "redirect:/";
    }

    @GetMapping("/addCard")
    public String saveCardPage(@RequestParam(required = false) UUID selectedBoardId, Model model) {

        List<Card> selectedCards = cardRepository.findByBoard_Id(selectedBoardId);
        boolean isStarter=false;
        boolean isFinisher=false;
        for (Card selectedCard : selectedCards) {
            if (selectedCard.isStarter()){
                isStarter=true;
            }
            else if (selectedCard.isFinisher()){
                isFinisher=true;
            }
        }
        model.addAttribute("cards", selectedCards);
        model.addAttribute("isStarter", isStarter);
        model.addAttribute("isFinisher", isFinisher);
        model.addAttribute("selectedBoardId", selectedBoardId);
        return "addcardpage";
    }

    @PostMapping("/saveCard")
    public String saveCard(@RequestParam String name,
                           @RequestParam(required = false) Boolean starter,
                           @RequestParam(required = false) Boolean finisher,
                           @RequestParam UUID selectedBoardId, Model model) {

        Card newCard = new Card();
        newCard.setName(name);
        newCard.setBoard(boardRepository.findById(selectedBoardId).orElse(null));

        if (starter != null && starter) {
            newCard.setStarter(true);
        } else if (finisher != null && finisher) {
            newCard.setFinisher(true);
        }


        cardRepository.save(newCard);
        return "redirect:/?boardId=" + selectedBoardId;
    }


    @GetMapping("/addTask")
    public String saveTaskPage(@RequestParam(required = false) UUID selectedCardId,
                               @RequestParam(required = false) UUID selectedBoardId,
                               Model model) {
        model.addAttribute("selectedCardId", selectedCardId);
        model.addAttribute("selectedBoardId", selectedBoardId);
        return "addtaskpage";
    }

    @PostMapping("/saveTask")
    public String saveTask(@RequestParam(required = false) UUID taskId,
                           @RequestParam(required = false) MultipartFile taskImage,
                           @RequestParam(required = false) String title,
                           @RequestParam(required = false) String description,
                           @RequestParam(required = false) LocalDateTime deadline,
                           @RequestParam UUID selectedBoardId,
                           @RequestParam UUID selectedCardId) throws IOException {

        Task task;

        if (taskId != null) {
            // Fetch the existing task for editing
            task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        } else {
            // Create a new task if taskId is null
            task = new Task();
            task.setCard(cardRepository.findById(selectedCardId).orElse(null));
            task.setStarted(false);
        }

        // Update task fields
        task.setTitle(title);
        task.setDescription(description);
        task.setDeadline(deadline);

        if (taskImage != null && !taskImage.isEmpty()) {
            // Handle file attachment (replace existing attachment if necessary)
            Attachment attachment = new Attachment();
            attachment.setFilename(taskImage.getOriginalFilename());
            attachmentRepository.save(attachment);

            AttachmentContent attachmentContent = new AttachmentContent();
            attachmentContent.setAttachment(attachment);
            attachmentContent.setFileData(taskImage.getBytes());
            attachmentContentRepository.save(attachmentContent);

            task.setAttachment(attachment);
        }

        // Save or update the task
        taskRepository.save(task);

        return "redirect:/?boardId=" + selectedBoardId;
    }






    @PostMapping("/tasktoward")
    public String tasktoward(@RequestParam UUID selectedTask,
                             @RequestParam UUID selectedBoardId,
                             @RequestParam UUID selectedCardId
    ){
        Optional<Card> byId = cardRepository.findById(selectedCardId);
        Card currentCard = byId.get();

        Task task=taskRepository.findById(selectedTask).orElse(new Task());
        List<Card> cardsByBoard = List.of();
        cardsByBoard = cardRepository.findByBoard_Id(selectedBoardId);
        for (Card card : cardsByBoard) {
            if (card.isFinisher()){
                cardsByBoard = cardService.sortCards(cardsByBoard);
                break;
            }
        }
        for (int i = 0; i < cardsByBoard.size(); i++) {
            if (cardsByBoard.get(i).getId()==currentCard.getId()){
                task.setCard(cardsByBoard.get(i+1));
                if (cardsByBoard.get(i+1).isFinisher()){
                    task.setCompletedAt(LocalDateTime.now());
                    task.setStatus(TaskStatus.COMPLETED);
                }
                else  task.setStatus(TaskStatus.IN_PROGRESS);
                break;
            }
        }


        taskRepository.save(task);
        return "redirect:/?boardId=" + selectedBoardId;
    }
    @PostMapping("/taskbackward")
    public String taskbackward(@RequestParam UUID selectedTask,
                               @RequestParam UUID selectedBoardId,
                               @RequestParam UUID selectedCardId)
    {
        Optional<Card> byId = cardRepository.findById(selectedCardId);
        Card currentCard = byId.get();

        Task task=taskRepository.findById(selectedTask).orElse(new Task());
        List<Card> cardsByBoard = List.of();
        cardsByBoard = cardRepository.findByBoard_Id(selectedBoardId);
        for (Card card : cardsByBoard) {
            if (card.isFinisher()){
                cardsByBoard = cardService.sortCards(cardsByBoard);
                break;
            }
        }
        for (int i = 0; i < cardsByBoard.size(); i++) {
            if (cardsByBoard.get(i).getId()==currentCard.getId()){
                task.setCard(cardsByBoard.get(i-1));
                if (cardsByBoard.get(i-1).isStarter()){
                    task.setStatus(TaskStatus.NOT_STARTED);
                }
                break;
            }
        }
        taskRepository.save(task);
        return "redirect:/?boardId=" + selectedBoardId;

    }

    @GetMapping("/taskDetail")
    public String taskDetails(Model model,
                              @RequestParam UUID selectedTask,
                              @RequestParam UUID selectedBoardId,
                              @RequestParam UUID selectedCardId) {
        List<User> users = userRepository.findAll();
        Task task = taskRepository.findById(selectedTask).orElse(new Task());

        // Pass the current time to the template
        model.addAttribute("currentTime", LocalDateTime.now());

        // Check the status based on logic (Not Started, In Progress, Completed, Expired)
        if (task.getCard() != null && task.getCard().isStarter()) {
            task.setStatus(TaskStatus.NOT_STARTED); // Default status for starter card
        } else if (task.getCompletedAt() != null) {
            task.setStatus(TaskStatus.COMPLETED);
        } else if (task.getDeadline() != null && task.getDeadline().isBefore(LocalDateTime.now())) {
            task.setStatus(TaskStatus.EXPIRED);
        } else if (!task.getStarted() && task.getStatus() == null) {
            task.setStatus(TaskStatus.IN_PROGRESS); // Default status if task is not started and not completed
        }

        model.addAttribute("allUsers", users);
        model.addAttribute("selectedCardId", selectedCardId);
        model.addAttribute("selectedBoardId", selectedBoardId);
        model.addAttribute("task", task);
        return "taskdetailpage";
    }

    @PostMapping("/deleteTask")
    public String deleteTask(@RequestParam UUID taskId,
                             @RequestParam UUID selectedBoardId){
        taskRepository.deleteById(taskId);
        return "redirect:/?boardId=" + selectedBoardId;
    }


    @GetMapping("/editTask")
    public String editTask(@RequestParam UUID taskId, Model model,@RequestParam UUID selectedBoardId,
                           @RequestParam UUID selectedCardId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        // Fetch all users and remove already assigned ones
        List<User> allUsers = userRepository.findAll();
        List<User> unassignedUsers = allUsers.stream()
                .filter(user -> !task.getUsers().contains(user))
                .collect(Collectors.toList());

        model.addAttribute("selectedCardId", selectedCardId);
        model.addAttribute("selectedBoardId", selectedBoardId);
        model.addAttribute("task", task);
        model.addAttribute("unassignedUsers", unassignedUsers); // List of users not assigned to the task
        return "edittask";
    }


    @PostMapping("/deleteUser")
    public String deleteUserFromTask(@RequestParam UUID taskId,
                                     @RequestParam UUID userId,
                                     @RequestParam UUID selectedBoardId,
                                     @RequestParam UUID selectedCardId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Remove the user from the task
        task.getUsers().remove(user);

        // Save the task with the updated user list
        taskRepository.save(task);

        // Redirect to the edit task page
        return "redirect:/editTask?taskId=" + taskId + "&selectedCardId=" + selectedCardId + "&selectedBoardId=" + selectedBoardId;
    }





    @PostMapping("/assignUser")
    public String assignUserToTask(@RequestParam UUID taskId,
                                   @RequestParam UUID userId,
                                   @RequestParam UUID selectedBoardId,
                                   @RequestParam UUID selectedCardId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Add the user to the task if not already assigned
        if (!task.getUsers().contains(user)) {
            task.getUsers().add(user);
            taskRepository.save(task);
        }

        return "redirect:/editTask?taskId=" + taskId + "&selectedCardId=" + selectedCardId + "&selectedBoardId=" + selectedBoardId;
    }








}
