package app.hypershell;

import app.hypershell.ICommandOutputListener;

interface IUserService {
    /**
     * Executes a shell command with arguments and returns the result synchronously.
     *
     * @param command an array of strings representing the command and its arguments
     * @return the standard output of the executed command
     */
    String execArr(in String[] command) = 1;

    /**
     * Executes a command and streams its output back via a listener asynchronously.
     *
     * @param command an array of strings representing the command and its arguments
     * @param listener the callback interface to receive output lines
     */
    void execArrWithCallback(in String[] command, ICommandOutputListener listener) = 2;

    oneway void destroy() = 16777114;
}