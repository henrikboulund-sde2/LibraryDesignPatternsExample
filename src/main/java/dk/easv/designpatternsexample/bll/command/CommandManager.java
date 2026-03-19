package dk.easv.designpatternsexample.bll.command;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Manages the execution and undo history of commands.
 * Belongs to the Business Logic Layer (BLL).
 *
 * Uses a stack (Deque) so the most recently executed command
 * is always the first to be undone — standard "last in, first undone" behaviour.
 */
public class CommandManager {

    private final Deque<ICommand> history = new ArrayDeque<>();

    /**
     * Execute a command and push it onto the history stack.
     * This is the only way commands should be run — never call execute() directly.
     */
    public void execute(ICommand command) {
        command.execute();
        history.push(command);
    }

    /** Returns true if there is at least one command that can be undone. */
    public boolean canUndo() {
        return !history.isEmpty();
    }

    /**
     * Undo the most recently executed command and remove it from the history.
     * Does nothing if the history is empty.
     */
    public void undo() {
        if (canUndo()) {
            ICommand command = history.pop();
            command.undo();
        }
    }

    /**
     * Returns descriptions of all executed commands, most recent first.
     * Used to display the command history log in the UI.
     */
    public List<String> getHistory() {
        List<String> descriptions = new ArrayList<>();
        for (ICommand command : history) {
            descriptions.add(command.getDescription());
        }
        return descriptions;
    }

    /** Returns the number of commands currently in the history. */
    public int historySize() {
        return history.size();
    }
}
