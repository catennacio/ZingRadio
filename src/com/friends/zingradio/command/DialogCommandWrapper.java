package com.friends.zingradio.command;

import android.content.DialogInterface;

public class DialogCommandWrapper implements DialogInterface.OnClickListener
{
    private ICommand command;

    public DialogCommandWrapper(ICommand command)
    {
        this.command = command;
    }

    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        dialog.dismiss();
        command.execute();
    }
}
