package tmanager.controller;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import java.util.*;
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
    private final CommentRepository commentRepository;

    @GetMapping("/")
    public String boardPage(
            @RequestParam(required = false) UUID boardId,
            @RequestParam(required = false) String searchedTasks,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) UUID userId,
            @AuthenticationPrincipal User user,// Added userId parameter
            Model model
    ) {
        List<Board> boards = boardRepository.findAll();
        List<User> users = userRepository.findAll();
        List<Card> cardsByBoard = List.of();
        List<Task> tasksByCards = List.of();

        Board selectedBoard = boardId != null ? boardRepository.findById(boardId).orElse(null) : null;

        if (selectedBoard != null) {
            cardsByBoard = cardRepository.findByBoard_Id(boardId);
            tasksByCards = cardRepository.findTasksByCards(cardsByBoard);

            // Filter tasks by user if userId is provided
            if (userId != null) {
                User selectedUser = userRepository.findById(userId).orElse(null);
                if (selectedUser != null) {
                    tasksByCards = tasksByCards.stream()
                            .filter(task -> task.getUsers().contains(selectedUser))
                            .toList();
                }
            }





            // Search filter
            if (searchedTasks != null && !searchedTasks.trim().isEmpty()) {
                tasksByCards = tasksByCards.stream()
                        .filter(task -> task.getTitle() != null &&
                                task.getTitle().toLowerCase().contains(searchedTasks.toLowerCase()))
                        .toList();
            }

            // Additional filters
            if (filter != null) {
                tasksByCards = switch (filter) {
                    case "withoutDeadline" -> tasksByCards.stream()
                            .filter(task -> task.getDeadline() == null)
                            .toList();
                    case "warningTasks" -> tasksByCards.stream()
                            .filter(task -> taskService.getDeadlineStatus(task.getDeadline()) == 1)
                            .toList();
                    case "completed" -> tasksByCards.stream()
                            .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                            .toList();
                    case "expired" -> tasksByCards.stream()
                            .filter(task -> task.getStatus()==TaskStatus.EXPIRED)
                            .toList();
                    default -> tasksByCards;
                };
            }
        }

        // Update tasks with deadlines for 'finisher' cards
        for (Card card : cardsByBoard) {
            if (card.isFinisher()) {
                List<Task> tasksByBoardId = taskRepository.findTasksByBoardId(card.getBoard().getId());
                tasksByBoardId.stream()
                        .filter(task -> taskService.getDeadlineStatus(task.getDeadline()) == 0)
                        .forEach(task -> {
                            task.setStatus(TaskStatus.EXPIRED);
                            task.setCard(card);
                            taskRepository.save(task);
                        });
                cardsByBoard = cardService.sortAndAssignOrders(cardsByBoard); // Sort cards after updates
                break;
            }
        }



        model.addAttribute("tasks", tasksByCards);
        model.addAttribute("cards", cardsByBoard);
        model.addAttribute("selectedBoard", selectedBoard);
        model.addAttribute("boards", boards);
        model.addAttribute("taskService", taskService);
        model.addAttribute("users", users);
        model.addAttribute("selectedUserId",userId);
         // Added attribute for UI reference
        model.addAttribute("searchedTasks", searchedTasks);
        model.addAttribute("filter", filter);
        return "boardpage";
    }





    @GetMapping("/addboard")
    public String saveBoardPage(@RequestParam(required = false) UUID selectedBoardId, Model model) {
        model.addAttribute("selectedBoardId", selectedBoardId);
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
        if (!cardRepository.findAll().isEmpty()) {
            if (finisher==null){

                if (starter==null){
                    Integer maxOrder = cardRepository.findMaxOrderByBoardId(selectedBoardId);
                    newCard.setOrders(maxOrder == null ? 1 : maxOrder + 1);
                }
            }
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
        Card card = cardRepository.findById(selectedCardId).orElse(null);
        if (taskId != null) {
            // Fetch the existing task for editing
            task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        } else {
            // Create a new task if taskId is null
            task = new Task();
            task.setCard(card);
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
        card.setHasTask(true);
        cardRepository.save(card);
        // Save or update the task
        taskRepository.save(task);

        return "redirect:/?boardId=" + selectedBoardId+"&all=all";
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
                cardsByBoard = cardService.sortAndAssignOrders(cardsByBoard);
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
                cardsByBoard = cardService.sortAndAssignOrders(cardsByBoard);
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
        List<Comment> comments = commentRepository.findByTask(task);
        model.addAttribute("comments", comments);
        model.addAttribute("allUsers", users);
        model.addAttribute("selectedCardId", selectedCardId);
        model.addAttribute("selectedBoardId", selectedBoardId);
        model.addAttribute("task", task);
        return "taskdetailpage";
    }

    @PostMapping("/deleteTask")
    @Transactional
    public String deleteTask(@RequestParam UUID taskId,
                             @RequestParam UUID selectedBoardId){
        Task task = taskRepository.findById(taskId).orElse(new Task());
        Card card = task.getCard();
        List<Task> byCardId = taskRepository.findByCardId(card.getId());
        if (byCardId.size()==1){
            card.setHasTask(false);
            cardRepository.save(card);
        }
        commentRepository.deleteByTaskId(taskId);
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




    @PostMapping("/addComment")
    public String addComment(@RequestParam String text,
                             @RequestParam UUID taskId,
                             @RequestParam UUID selectedBoardId,
                             @AuthenticationPrincipal User user) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        Comment comment = new Comment();
        comment.setText(text);
        comment.setDate(LocalDateTime.now());
        comment.setUser(user); // Add the currently authenticated user
        comment.setTask(task); // Associate comment with the task

        commentRepository.save(comment);

        return "redirect:/taskDetail?selectedTask=" + taskId + "&selectedBoardId=" + selectedBoardId + "&selectedCardId=" + task.getCard().getId();
    }

    @GetMapping("/getDeveloperResults")
    public String getDeveloperResults(){


        return "";
    }

    @GetMapping("/getCriminalsResults")
    public String getCriminalsResults(Model model){
        List<Map<String, Object>> expiredTaskCountByUser = taskRepository.findExpiredTaskCountByUser();
        model.addAttribute("expiredTaskCountByUser", expiredTaskCountByUser);
        return "expiredreportpage";
    }

    @GetMapping("/taskStatistics")
    public String getTaskStatistics(Model model) {
        List<Map<String, Object>> taskStatistics = taskRepository.findTaskStatisticsByUser();
        model.addAttribute("taskStatistics", taskStatistics);
        return "taskStatisticsPage";
    }



    @PostMapping("/cardtoward")
    public String cardtoward(
            @RequestParam UUID selectedBoardId,
            @RequestParam UUID selectedCardId
    ){

        List<Card> cards1 = cardRepository.findByBoardId(selectedBoardId);
        List<Card> cards = cardService.sortAndAssignOrders(cards1);
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getId().equals(selectedCardId)) {
                int a=cards.get(i).getOrders();
                cards.get(i).setOrders(cards.get(i+1).getOrders());
                cards.get(i+1).setOrders(a);
                cardRepository.save(cards.get(i));
                cardRepository.save(cards.get(i+1));
                break;
            }
        }
        return "redirect:/?boardId=" + selectedBoardId;
    }

    @PostMapping("/cardbackward")
    public String cardbackward(
            @RequestParam UUID selectedBoardId,
            @RequestParam UUID selectedCardId
    ){

        List<Card> cards1 = cardRepository.findByBoardId(selectedBoardId);
        List<Card> cards = cardService.sortAndAssignOrders(cards1);
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getId().equals(selectedCardId)) {
                int a=cards.get(i).getOrders();
                cards.get(i).setOrders(cards.get(i-1).getOrders());
                cards.get(i-1).setOrders(a);
                cardRepository.save(cards.get(i));
                cardRepository.save(cards.get(i-1));
                break;
            }
        }
        return "redirect:/?boardId=" + selectedBoardId;
    }


    @PostMapping("/editCard")
    public String editCard(
            Model model,
            @RequestParam UUID selectedCardId

    ){
        Optional<Card> byId = cardRepository.findById(selectedCardId);
        model.addAttribute("currentCard", byId.get());
        return "editcardpage";
    }

    @PostMapping("/deleteCard")
    public String deleteCard(@RequestParam UUID selectedCardId){
        Optional<Card> byId = cardRepository.findById(selectedCardId);
        Board board = byId.get().getBoard();
        cardRepository.deleteById(selectedCardId);
        return "redirect:/?boardId=" + board.getId();
    }
    @PostMapping("/saveEditedCard")
    public String saveEditedCard(@RequestParam UUID selectedCardId,
                                 @RequestParam String name){
        Optional<Card> byId = cardRepository.findById(selectedCardId);
        Board board = byId.get().getBoard();
        byId.get().setName(name);
        cardRepository.save(byId.get());
        return "redirect:/?boardId=" + board.getId();
    }

}
