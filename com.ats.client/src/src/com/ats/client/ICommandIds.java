package com.ats.client;

/**
 * Interface defining the application's command IDs.
 * Key bindings can be defined for specific commands.
 * To associate an action with a command, use IAction.setActionDefinitionId(commandId).
 *
 * @see org.eclipse.jface.action.IAction#setActionDefinitionId(String)
 */
public interface ICommandIds {

    public static final String CMD_OPEN = "com.ats.client.open";
    public static final String CMD_OPEN_MESSAGE = "com.ats.client.openMessage";
    public static final String CMD_OPEN_PREFERENCES = "com.ats.client.actions.openPreferencesAction";
}
