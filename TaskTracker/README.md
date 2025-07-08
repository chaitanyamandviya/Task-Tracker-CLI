🚀 Java Task Tracker
A simple, command-line based task management application written in Java. This tool allows you to add, list, update, and delete tasks, as well as track their progress directly from your terminal.

Features
CRUD Operations: Add, list, update, and delete tasks.

Status Tracking: Mark tasks as to-do, in-progress, or done.

CLI Interface: All operations are handled through a simple interactive command-line interface.

Data Persistence: Tasks are saved to a tasks.json file, so your data persists between sessions.

Task Filtering: View tasks filtered by their current status.

Getting Started
Follow these instructions to get the project compiled and running.

Prerequisites
You must have a Java Development Kit (JDK) installed on your system.

Compilation
Navigate to the project directory and compile the source file using javac:

Bash

javac TaskTracker.java
Execution
Run the application with the java command:

Bash

java TaskTracker
Usage
Once running, the application will provide a > prompt for you to enter commands. Descriptions containing spaces must be enclosed in double quotes.

Commands
Here are the available commands:

add "<description>"
Adds a new task to your list.

Example:

Bash

> add "Finish project report"
list [status]
Displays tasks. You can optionally filter by status: todo, in-progress, or done.

Examples:

Bash

> list
> list todo
update <id> "<new_description>"
Updates the description of an existing task by its ID.

Example:

Bash

> update 1 "Buy groceries and milk"
delete <id>
Removes a task from the list by its ID.

Example:

Bash

> delete 3
mark-done <id>
Marks a task as 'done'.

Example:

Bash

> mark-done 2
mark-in-progress <id>
Marks a task as 'in-progress'.

Example:

Bash

> mark-in-progress 1
help
Displays the help message with all commands.

exit
Exits the application.

Data Persistence 💡
All tasks are automatically saved to a tasks.json file in the application's root directory. This file is created and updated as you manage your tasks. If you delete this file, all task data will be lost.