import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A simple command-line task tracker application in Java.
 * This application runs in an interactive loop.
 *
 * --- USAGE ---
 * Compile: javac TaskTracker.java
 * Run:     java TaskTracker
 * Then, enter commands at the prompt.
 *
 * --- COMMANDS ---
 * add "<description>"          - Adds a new task.
 * list [status]              - Lists tasks. Status can be 'todo', 'in-progress', or 'done'.
 * update <id> "<new_desc>"   - Updates a task's description.
 * delete <id>                - Deletes a task.
 * mark-done <id>             - Marks a task as done.
 * mark-in-progress <id>      - Marks a task as in-progress.
 * help                       - Shows this help message.
 * exit                       - Exits the application.
 */
public class TaskTracker {

    private static final String FILE_NAME = "tasks.json";

    // Represents the status of a task.
    enum Status {
        TODO,
        IN_PROGRESS,
        DONE
    }

    // Represents a single task.
    static class Task {
        private final int id;
        private String description;
        private Status status;

        Task(int id, String description, Status status) {
            this.id = id;
            this.description = description;
            this.status = status;
        }

        public int getId() { return id; }
        public String getDescription() { return description; }
        public Status getStatus() { return status; }
        public void setDescription(String description) { this.description = description; }
        public void setStatus(Status status) { this.status = status; }

        public String toJsonString() {
            return String.format("  {\n    \"id\": %d,\n    \"description\": \"%s\",\n    \"status\": \"%s\"\n  }",
                id, escapeJson(description), status);
        }

        private String escapeJson(String s) {
            return s.replace("\"", "\\\"");
        }

        @Override
        public String toString() {
            String statusIcon = "";
            switch (status) {
                case TODO:        statusIcon = "[ ]"; break;
                case IN_PROGRESS: statusIcon = "[~]"; break;
                case DONE:        statusIcon = "[✓]"; break;
            }
            return String.format("%s %d: %s (%s)", statusIcon, id, description, status.toString().toLowerCase().replace("_", "-"));
        }
    }

    public static void main(String[] args) {
        List<Task> tasks = loadTasks();

        System.out.println("🎉 Welcome to the Interactive Task Tracker! 🎉");
        printHelp();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String line = scanner.nextLine().trim();

                if (line.isEmpty()) {
                    continue;
                }

                if (line.equalsIgnoreCase("exit")) {
                    System.out.println("Goodbye! 👋");
                    break;
                }
                
                // Use regex to handle arguments in quotes
                List<String> argList = new ArrayList<>();
                Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(line);
                while (m.find()) {
                    argList.add(m.group(1).replace("\"", ""));
                }
                String[] inputArgs = argList.toArray(new String[0]);

                processCommand(inputArgs, tasks);
            }
        }
    }

    /**
     * Processes a single command entered by the user.
     * @param args The parsed command and its arguments.
     * @param tasks The current list of tasks.
     */
    private static void processCommand(String[] args, List<Task> tasks) {
        if (args.length == 0) {
            return;
        }
        String command = args[0].toLowerCase();

        try {
            switch (command) {
                case "add":
                    if (args.length < 2) throw new IllegalArgumentException("Task description is missing.");
                    addTask(tasks, args[1]);
                    break;
                case "list":
                    listTasks(tasks, args);
                    break;
                case "update":
                    if (args.length < 3) throw new IllegalArgumentException("Task ID and new description are required.");
                    updateTask(tasks, Integer.parseInt(args[1]), args[2]);
                    break;
                case "delete":
                    if (args.length < 2) throw new IllegalArgumentException("Task ID is required.");
                    deleteTask(tasks, Integer.parseInt(args[1]));
                    break;
                case "mark-done":
                     if (args.length < 2) throw new IllegalArgumentException("Task ID is required.");
                    updateTaskStatus(tasks, Integer.parseInt(args[1]), Status.DONE);
                    break;
                case "mark-in-progress":
                     if (args.length < 2) throw new IllegalArgumentException("Task ID is required.");
                    updateTaskStatus(tasks, Integer.parseInt(args[1]), Status.IN_PROGRESS);
                    break;
                case "help":
                    printHelp();
                    break;
                default:
                    System.out.println("Unknown command: " + command);
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid task ID. Please provide a number.");
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static List<Task> loadTasks() {
        List<Task> tasks = new ArrayList<>();
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return tasks;
        }

        try {
            String content = new String(Files.readAllBytes(Paths.get(FILE_NAME)));
            if (content.trim().isEmpty() || content.trim().equals("[]")) {
                return tasks;
            }

            Pattern taskPattern = Pattern.compile("\\{([^}]+)\\}");
            Matcher taskMatcher = taskPattern.matcher(content);

            while (taskMatcher.find()) {
                String taskContent = taskMatcher.group(1);
                int id = Integer.parseInt(extractValue(taskContent, "id"));
                String description = extractValue(taskContent, "description");
                Status status = Status.valueOf(extractValue(taskContent, "status"));
                tasks.add(new Task(id, description, status));
            }

        } catch (IOException e) {
            System.err.println("Error reading tasks file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error parsing tasks file. It might be corrupted. " + e.getMessage());
        }
        return tasks;
    }

    private static String extractValue(String content, String key) {
        Pattern p = Pattern.compile("\"" + key + "\":\\s*\"?([^\",}]+)\"?");
        Matcher m = p.matcher(content);
        if (m.find()) {
            return m.group(1).trim().replace("\\\"", "\"");
        }
        throw new IllegalArgumentException("Key not found in task content: " + key);
    }

    private static void saveTasks(List<Task> tasks) {
        try (FileWriter writer = new FileWriter(FILE_NAME)) {
            writer.write("[\n");
            for (int i = 0; i < tasks.size(); i++) {
                writer.write(tasks.get(i).toJsonString());
                if (i < tasks.size() - 1) {
                    writer.write(",\n");
                }
            }
            writer.write("\n]\n");
        } catch (IOException e) {
            System.err.println("Error: Could not save tasks to file. " + e.getMessage());
        }
    }

    private static void addTask(List<Task> tasks, String description) {
        int newId = tasks.stream().mapToInt(Task::getId).max().orElse(0) + 1;
        Task newTask = new Task(newId, description, Status.TODO);
        tasks.add(newTask);
        saveTasks(tasks);
        System.out.println("Task added successfully (ID: " + newId + ")");
    }

    private static void listTasks(List<Task> tasks, String[] args) {
        System.out.println("\n--- Your Tasks ---");
        if (tasks.isEmpty()) {
            System.out.println("No tasks yet. Use 'add \"<description>\"' to create one.");
            return;
        }

        Optional<Status> filterStatus = Optional.empty();
        if (args.length > 1) {
            try {
                filterStatus = Optional.of(Status.valueOf(args[1].toUpperCase().replace("-", "_")));
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid status filter: " + args[1] + ". Showing all tasks.");
            }
        }
        
        final Optional<Status> finalFilter = filterStatus;
        List<Task> filteredTasks = tasks.stream()
            .filter(task -> finalFilter.map(s -> task.getStatus() == s).orElse(true))
            .collect(Collectors.toList());

        if (filteredTasks.isEmpty()) {
            System.out.println("No tasks match the specified filter.");
        } else {
            filteredTasks.forEach(System.out::println);
        }
        System.out.println("------------------\n");
    }

    private static void updateTask(List<Task> tasks, int id, String newDescription) {
        Optional<Task> taskToUpdate = tasks.stream().filter(t -> t.getId() == id).findFirst();
        if (taskToUpdate.isPresent()) {
            Task task = taskToUpdate.get();
            task.setDescription(newDescription);
            saveTasks(tasks);
            System.out.println("Task " + id + " updated successfully.");
        } else {
            System.out.println("Task with ID " + id + " not found.");
        }
    }

    private static void deleteTask(List<Task> tasks, int id) {
        boolean removed = tasks.removeIf(t -> t.getId() == id);
        if (removed) {
            saveTasks(tasks);
            System.out.println("Task " + id + " deleted successfully.");
        } else {
            System.out.println("Task with ID " + id + " not found.");
        }
    }

    private static void updateTaskStatus(List<Task> tasks, int id, Status newStatus) {
        Optional<Task> taskToUpdate = tasks.stream().filter(t -> t.getId() == id).findFirst();
        if (taskToUpdate.isPresent()) {
            Task task = taskToUpdate.get();
            task.setStatus(newStatus);
            saveTasks(tasks);
            System.out.println("Task " + id + " marked as " + newStatus.toString().toLowerCase().replace("_","-") + ".");
        } else {
            System.out.println("Task with ID " + id + " not found.");
        }
    }

    private static void printHelp() {
        System.out.println("\n--- Task Tracker CLI ---");
        System.out.println("Manage your tasks from the command line.");
        System.out.println("\nUSAGE:");
        System.out.println("Enter a command at the prompt. Use quotes for descriptions with spaces.");
        System.out.println("\nCOMMANDS:");
        System.out.println("  add \"<description>\"        - Adds a new task.");
        System.out.println("  list [status]              - Lists tasks. Optional status: todo, in-progress, done.");
        System.out.println("  update <id> \"<new_desc>\"   - Updates the description of an existing task.");
        System.out.println("  delete <id>                - Deletes a task.");
        System.out.println("  mark-done <id>             - Marks a task as done.");
        System.out.println("  mark-in-progress <id>      - Marks a task as in-progress.");
        System.out.println("  help                       - Shows this help message.");
        System.out.println("  exit                       - Exits the application.");
        System.out.println("\nEXAMPLES:");
        System.out.println("  > add \"Buy milk\"");
        System.out.println("  > list todo");
        System.out.println("  > mark-done 1\n");
    }
}