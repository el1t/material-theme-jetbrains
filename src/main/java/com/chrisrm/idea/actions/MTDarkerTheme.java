package com.chrisrm.idea.actions;

import com.chrisrm.idea.MTTheme;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.*;


public class MTDarkerTheme extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        MTTheme.DARKER.activate();
    }
}
