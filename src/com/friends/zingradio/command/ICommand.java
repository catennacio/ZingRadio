package com.friends.zingradio.command;

public interface ICommand
{
    public void execute();
 
    public static final ICommand NO_OP = new ICommand() { public void execute() {} };
}