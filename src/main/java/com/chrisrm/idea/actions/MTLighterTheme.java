package com.chrisrm.idea.actions;

import com.chrisrm.idea.MTTheme;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.*;


public class MTLighterTheme extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        JOptionPane.showMessageDialog(null, "Keep in mind, the lighter theme is a work in progress and there will be some issues.", "Work in progress", JOptionPane.INFORMATION_MESSAGE);
        MTTheme.LIGHTER.activate();
    }
}
