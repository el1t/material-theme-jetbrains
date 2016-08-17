package com.chrisrm.idea;

import com.intellij.ide.ui.laf.IdeaLaf;
import com.intellij.ide.ui.laf.LafManagerImpl;
import com.intellij.ide.ui.laf.darcula.DarculaLaf;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.util.containers.hash.HashMap;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import sun.awt.AppContext;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicLookAndFeel;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class MTLaf extends DarculaLaf {

    private BasicLookAndFeel base;
    private String theme;

    public MTLaf(String theme) {
        this.theme = theme;
        base = createBaseLookAndFeel();
    }

    protected BasicLookAndFeel createBaseLookAndFeel() {
        try {
            if (SystemInfo.isWindows || SystemInfo.isLinux) {
                return new IdeaLaf();
            } else {
                final String name = UIManager.getSystemLookAndFeelClassName();
                return (BasicLookAndFeel) Class.forName(name).newInstance();
            }
        } catch (Exception e) {
            log(e);
        }
        return null;
    }

    private static Font findFont(String name) {
        for (Font font : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()) {
            if (font.getName().equals(name)) {
                return font;
            }
        }
        return null;
    }

    @Override
    public UIDefaults getDefaults() {
        try {
            final Method superMethod = BasicLookAndFeel.class.getDeclaredMethod("getDefaults");
            superMethod.setAccessible(true);
            final UIDefaults metalDefaults = (UIDefaults)superMethod.invoke(new MetalLookAndFeel());

            final UIDefaults defaults = (UIDefaults)superMethod.invoke(base);
            if (SystemInfo.isLinux) {
                if (!Registry.is("darcula.use.native.fonts.on.linux")) {
                    Font font = findFont("DejaVu Sans");
                    if (font != null) {
                        for (Object key : defaults.keySet()) {
                            if (key instanceof String && ((String)key).endsWith(".font")) {
                                defaults.put(key, new FontUIResource(font.deriveFont(13f)));
                            }
                        }
                    }
                } else if (Arrays.asList("CN", "JP", "KR", "TW").contains(Locale.getDefault().getCountry())) {
                    for (Object key : defaults.keySet()) {
                        if (key instanceof String && ((String)key).endsWith(".font")) {
                            final Font font = defaults.getFont(key);
                            if (font != null) {
                                defaults.put(key, new FontUIResource("Dialog", font.getStyle(), font.getSize()));
                            }
                        }
                    }
                }
            }

            LafManagerImpl.initInputMapDefaults(defaults);
            initIdeaDefaults(defaults);
            patchStyledEditorKit(defaults);
            patchComboBox(metalDefaults, defaults);
            defaults.remove("Spinner.arrowButtonBorder");
            defaults.put("Spinner.arrowButtonSize", JBUI.size(16, 5).asUIResource());
            MetalLookAndFeel.setCurrentTheme(createMetalTheme());
            if (SystemInfo.isWindows && Registry.is("ide.win.frame.decoration")) {
                JFrame.setDefaultLookAndFeelDecorated(true);
                JDialog.setDefaultLookAndFeelDecorated(true);
            }
            if (SystemInfo.isLinux && JBUI.isHiDPI()) {
                applySystemFonts(defaults);
            }
            defaults.put("EditorPane.font", defaults.getFont("TextField.font"));
            return defaults;
        }
        catch (Exception e) {
            log(e);
        }
        return super.getDefaults();
    }

    private static void applySystemFonts(UIDefaults defaults) {
        try {
            String fqn = javax.swing.UIManager.getSystemLookAndFeelClassName();
            Object systemLookAndFeel = Class.forName(fqn).newInstance();
            final Method superMethod = BasicLookAndFeel.class.getDeclaredMethod("getDefaults");
            superMethod.setAccessible(true);
            final UIDefaults systemDefaults = (UIDefaults) superMethod.invoke(systemLookAndFeel);
            for (Map.Entry<Object, Object> entry : systemDefaults.entrySet()) {
                if (entry.getValue() instanceof Font) {
                    defaults.put(entry.getKey(), entry.getValue());
                }
            }
        } catch (Exception e) {
            log(e);
        }
    }

    private static void patchComboBox(UIDefaults metalDefaults, UIDefaults defaults) {
        defaults.remove("ComboBox.ancestorInputMap");
        defaults.remove("ComboBox.actionMap");
        defaults.put("ComboBox.ancestorInputMap", metalDefaults.get("ComboBox.ancestorInputMap"));
        defaults.put("ComboBox.actionMap", metalDefaults.get("ComboBox.actionMap"));
    }


    @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
    private void patchStyledEditorKit(UIDefaults defaults) {
        URL url = getClass().getResource("/properties/" + this.theme + "/mt-" + this.theme + (JBUI.isHiDPI() ? "@2x.css" : ".css"));
        StyleSheet styleSheet = UIUtil.loadStyleSheet(url);
        defaults.put("StyledEditorKit.JBDefaultStyle", styleSheet);
        try {
            Field keyField = HTMLEditorKit.class.getDeclaredField("DEFAULT_STYLES_KEY");
            keyField.setAccessible(true);
            AppContext.getAppContext().put(keyField.get(null), UIUtil.loadStyleSheet(url));
        } catch (Exception e) {
            log(e);
        }
    }

    @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
    protected void loadDefaults(UIDefaults defaults) {
        final Properties properties = new Properties();
        final String osSuffix = SystemInfo.isMac ? "mac" : SystemInfo.isWindows ? "windows" : "linux";
        try {
            InputStream stream = getClass().getResourceAsStream("/properties/" + this.theme + "/mt-" + this.theme + ".properties");
            properties.load(stream);
            stream.close();

            stream = getClass().getResourceAsStream("/properties/" + this.theme + "/mt-" + this.theme + "_" + osSuffix + ".properties");
            properties.load(stream);
            stream.close();

            HashMap<String, Object> darculaGlobalSettings = new HashMap<String, Object>();
            final String prefix = this.getPrefix() + ".";
            for (String key : properties.stringPropertyNames()) {
                if (key.startsWith(prefix)) {
                    darculaGlobalSettings.put(key.substring(prefix.length()), parseValue(key, properties.getProperty(key)));
                }
            }

            for (Object key : defaults.keySet()) {
                if (key instanceof String && ((String) key).contains("")) {
                    final String s = (String) key;
                    final String darculaKey = s.substring(s.lastIndexOf('.') + 1);
                    if (darculaGlobalSettings.containsKey(darculaKey)) {
                        defaults.put(key, darculaGlobalSettings.get(darculaKey));
                    }
                }
            }

            for (String key : properties.stringPropertyNames()) {
                final String value = properties.getProperty(key);

                if (!key.startsWith("material")) {
                    defaults.put(key, parseValue(key, value));
                }
            }
        } catch (IOException e) {
            log(e);
        }
    }

    @SuppressWarnings("UnusedParameters")
    private static void log(Exception e) {
        e.printStackTrace();
    }


}