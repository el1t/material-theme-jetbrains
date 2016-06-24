package com.chrisrm.idea;

import com.intellij.openapi.fileEditor.impl.EditorTabColorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class MTEditorTabColor implements EditorTabColorProvider {

    private static final Color COLOR_DARKER = new JBColor(new Color(39, 40, 34, 255), new Color(39, 40, 34, 255));
    private static final Color COLOR_DEFAULT = new JBColor(new Color(43, 48, 59, 255), new Color(43, 48, 59, 255));
    private static final Color COLOR_LIGHTER = new JBColor(new Color(239, 241, 245, 255), new Color(239, 241, 245, 255));

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
