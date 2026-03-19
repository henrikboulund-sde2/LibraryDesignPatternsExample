package dk.easv.designpatternsexample.bll.command;

/**
 * Command interface for the Command Pattern.
 * Belongs to the Business Logic Layer (BLL).
 *
 * Command Pattern:
 *   Encapsulates a request as a self-contained object.
 *   This allows:
 *     - Parameterising operations (pass commands around like objects)
 *     - Logging executed commands (command history)
 *     - Undoable operations (each command knows how to reverse itself)
 *     - Queuing or scheduling operations
 */
public interface ICommand {

    /** Perform the operation. */
    void execute();

    /** Reverse the operation, restoring the previous state. */
    void undo();

    /** Human-readable description shown in the command history log. */
    String getDescription();
}
