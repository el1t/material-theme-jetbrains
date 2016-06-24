package com.chrisrm.idea.actions;

import com.chrisrm.idea.MTColorSchemeManager;
import com.chrisrm.idea.MTColorSchemeManagerImpl;
import com.chrisrm.idea.MTDataLayer;
import com.chrisrm.idea.MTLaf;
import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.JBColor;

import java.util.Arrays;
import java.util.List;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class MTSwitchTheme {

    private final MTColorSchemeManager colorSchemeManager = new MTColorSchemeManagerImpl();

    public void setTheme(String theme) {

        try {
            UIManager.setLookAndFeel(new MTLaf(theme.toLowerCase()));
            JBColor.setDark(this.useDarkTheme(theme));
            IconLoader.setUseDarkIcons(this.useDarkTheme(theme));

            new MTDataLayer().setValue("theme", theme);
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        List<String> Schemes = Arrays.asList("Spacegray Theme - Darker", "Spacegray Theme - Default", "Spacegray Theme - Lighter");
        String currentScheme = colorSchemeManager.getGlobalColorScheme().getName();

        String makeActiveScheme = (!Schemes.contains(currentScheme)) ? currentScheme : "Spacegray Theme - " + theme;

        final EditorColorsScheme scheme = colorSchemeManager.getScheme(makeActiveScheme);
        if (scheme != null) {
            colorSchemeManager.setGlobalScheme(scheme);
        }

        UISettings.getInstance().fireUISettingsChanged();
        ActionToolbarImpl.updateAllToolbarsImmediately();
    }

    /**
     * TODO Make more dynamic
     *
     * @param theme
     * @return bool
     */
    private boolean useDarkTheme(String theme) {
        return !theme.toLowerCase().equals("lighter");
    }

}
