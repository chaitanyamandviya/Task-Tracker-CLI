# Task Tracker

Sample solution for the [task-tracker](https://roadmap.sh/projects/task-tracker) challenge from [roadmap.sh](https://roadmap.sh/).

## How to run

Clone the repository and run the following command:

```bash
git clone https://github.com/chaitanyamandviya/Task-Tracker-CLI
```

Run the following command to build and run the project:

```bash
 ~ javac TaskTracker.java
 ~ java TaskTracker.java # To see the list of available commands

# To add a task
add <description>
add "Buy groceries"

# To delete a task
delete <id>
delete 1

# To mark a task as in progress/done
mark-in-progress 1
mark-done 1

# To list all tasks todo/in-progress/done
list todo
list in-progress
list done

# To exit application
exit