package com.chrisrm.idea;

import com.intellij.openapi.fileEditor.impl.EditorTabColorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class MTEditorTabColor implements EditorTabColorProvider {

    private static final Color COLOR_DARKER = new JBColor(new Color(33, 33, 33, 255), new Color(33, 33, 33, 255));
    private static final Color COLOR_DEFAULT = new JBColor(new Color(31, 31, 27, 255), new Color(31, 31, 27, 255));
    private static final Color COLOR_LIGHTER = new JBColor(new Color(250, 250, 250, 255), new Color(250, 250, 250, 255));

    @Nullable
    @Override
    public Color getEditorTabColor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        String theme = new MTDataLayer().getValue("theme", "default").toLowerCase();

        if (theme.equals("darker")) {
            return COLOR_DARKER;
        } else if (theme.equals("lighter")) {
            return COLOR_LIGHTER;
        } else {
            return COLOR_DEFAULT;
        }
    }
}
