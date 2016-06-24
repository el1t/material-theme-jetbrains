package com.chrisrm.idea;

import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManagerAdapter;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.tabs.impl.DefaultEditorTabsPainter;
import com.intellij.ui.tabs.impl.JBEditorTabs;
import com.intellij.ui.tabs.impl.JBEditorTabsPainter;
import com.intellij.util.ReflectionUtil;
import com.intellij.util.messages.MessageBus;

import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.awt.Component;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Dennis.Ushakov
 */
public class MTTabsPainterPatcher implements ApplicationComponent {

    final Properties properties = new Properties();
    String theme;

    public MTTabsPainterPatcher() {
        this.theme = new MTDataLayer().getValue("theme", "default").toLowerCase();

        try {
            InputStream stream = getClass().getResourceAsStream("/properties/" + this.theme + "/mt-" + this.theme + ".properties");
            properties.load(stream);
            stream.close();
        } catch (IOException e) {
            ;
        }
    }

    @Override
    public void initComponent() {
        final MessageBus bus = ApplicationManagerEx.getApplicationEx().getMessageBus();
        bus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerAdapter() {
            @Override
            public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                final FileEditor editor = event.getNewEditor();
                if (editor != null) {
                    Component component = editor.getComponent();
                    while (component != null) {
                        if (component instanceof JBEditorTabs) {
                            patchPainter((JBEditorTabs) component);
                            return;
                        }
                        component = component.getParent();
                    }
                }
            }
        });
    }

    private void patchPainter(JBEditorTabs component) {
        final JBEditorTabsPainter painter = ReflectionUtil.getField(JBEditorTabs.class, component,
                JBEditorTabsPainter.class, "myDarkPainter");

        if (painter instanceof MTTabsPainter) return;

        ReflectionUtil.setField(JBEditorTabs.class, component, JBEditorTabsPainter.class, "myDarkPainter", new MTTabsPainter());
    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "MTTabsPainterPatcher";
    }


    public static class MTTabsPainter extends DefaultEditorTabsPainter {

        @Override
        protected Color getDefaultTabColor() {
            if (myDefaultTabColor != null) {
                return myDefaultTabColor;
            }

            Properties properties = getProperties();

            return ColorUtil.fromHex("#" + properties.getProperty("material.tab.background"));
        }

        @Override
        protected Color getInactiveMaskColor() {
            return this.getDefaultTabColor();
        }

        private Properties getProperties() {
            Properties properties = new Properties();
            String theme = new MTDataLayer().getValue("theme", "default").toLowerCase();

            try {
                InputStream stream = MTTabsPainter.class.getResourceAsStream("/properties/" + theme + "/mt-" + theme + ".properties");
                properties.load(stream);
                stream.close();
            } catch (IOException e) {}

            return properties;
        }
    }
}
