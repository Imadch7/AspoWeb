package com.web.aspoapp.Service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.web.aspoapp.Model.Command;
import com.web.aspoapp.Model.CommandResult;
import com.web.aspoapp.Util.CommandExecutor;
import com.web.aspoapp.Util.Json;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.naming.directory.InvalidAttributesException;
import java.io.File;
import java.io.IOException;

@Service
public class CommandService {

    private File currentWorkingDirectory;
    private CommandExecutor commandResult;
    private final String containerId = "container_id";

    public CommandService() {
        this.currentWorkingDirectory = new File(System.getProperty("user.home"));
        System.out.println("CommandService initialized. Current working directory on Host: " + currentWorkingDirectory.getAbsolutePath());
    }

    public CommandExecutor getCommandResult() {
        return commandResult;
    }

    public String getContainerId() {
        return containerId;
    }

    /**
     * Executes a command and updates the current working directory if it's a 'cd' command.
     * @param command The command to execute.
     * @return The result of the command execution.
     */
    public CommandResult executeCommand(Command command) {

        CommandResult result = commandResult.executeCommand(command.getCommand(), currentWorkingDirectory);

        if (command.getCommand().trim().startsWith("cd ") && result.getExitCode() == 0) {
            String newPath = result.getStdout().trim();
            if (!newPath.isEmpty()) {
                File newCwd = new File(newPath);
                if (newCwd.exists() && newCwd.isDirectory()) {
                    this.currentWorkingDirectory = newCwd;
                    System.out.println("Updated CWD to: " + this.currentWorkingDirectory.getAbsolutePath());
                    result.setStdout(this.currentWorkingDirectory.getAbsolutePath());
                    result.setStderr("");
                } else {
                    result.setStderr("Error:can not moviing to : " + newPath + " (invalid path or not exist)\n");
                    result.setExitCode(1);
                    result.setStdout("");
                }
            } else {
                result.setStderr("Error: 'cd'.\n");
                result.setExitCode(1);
                result.setStdout("");
            }
        }
        return result;
    }

    /**
     * Gets the current working directory path.
     * @return The absolute path of the current working directory.
     */
    public String getCurrentWorkingDirectoryPath() {
        return this.currentWorkingDirectory.getAbsolutePath();
    }

    public CommandResult cloneRepository(String repoUrl) {
        if (repoUrl == null || repoUrl.trim().isEmpty()) {
            return new CommandResult(new Command(""), "empty URL", "error", 1);
        }
        try{
            Command cmd = new Command("docker exec -it"+containerId+" git clone "+repoUrl);
            if (containerId == null || containerId.trim().isEmpty()) {
                throw new InvalidAttributesException("Container ID is not set.");
            }
            CommandResult result = executeCommand(cmd);
            if (result.getExitCode() == 0) {
                result.setStdout("repo cloned.");
            } else {
                result.setStderr("Error in cloning: " + result.getStderr());
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommandResult(new Command(""), "", "Error :Invalid URL " + e.getMessage(), 1);
        }
    }

    /**
     * Executes a command and updates the current working directory if it's a 'cd' command.
     * @param command The command to execute.
     * @return The result of the command execution.
     */
    public CommandResult executeCmdinsideDocker(Command command) {

        CommandResult result = commandResult.executeCommand("docker exec -it"+command.getCommand(), currentWorkingDirectory);

        if (command.getCommand().trim().startsWith("cd ") && result.getExitCode() == 0) {
            String newPath = result.getStdout().trim();
            if (!newPath.isEmpty()) {
                File newCwd = new File(newPath);
                if (newCwd.exists() && newCwd.isDirectory()) {
                    this.currentWorkingDirectory = newCwd;
                    System.out.println("Updated CWD to: " + this.currentWorkingDirectory.getAbsolutePath());
                    result.setStdout(this.currentWorkingDirectory.getAbsolutePath());
                    result.setStderr("");
                } else {
                    result.setStderr("Error:can not moviing to : " + newPath + " (invalid path or not exist)\n");
                    result.setExitCode(1);
                    result.setStdout("");
                }
            } else {
                result.setStderr("Error: 'cd'.\n");
                result.setExitCode(1);
                result.setStdout("");
            }
        }
        return result;
    }

}