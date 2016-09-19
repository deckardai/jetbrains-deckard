/* ==========================================================
File:        Deckard.java
Description: Deckard connector for JetBrains IDEs.
Maintainer:  Deckard <support@deckard.ai>
License:     BSD, see LICENSE for more details.
Website:     https://www.deckard.ai/
===========================================================*/

package ai.deckard.intellij.plugin;

import com.intellij.AppTopics;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.PlatformUtils;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Level;

public class Deckard implements ApplicationComponent {

    public static final String VERSION = "7.0.9";
    public static final Logger log = Logger.getInstance("Deckard");

    public static String IDE_NAME;
    public static String IDE_VERSION;
    public static MessageBusConnection connection;
    public static Boolean DEBUG = false;
    public static Boolean READY = false;
    public static String lastFile = null;
    public static BigDecimal lastTime = new BigDecimal(0);
    private static String lastProject = null;

    private final int queueTimeoutSeconds = 10;
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public Deckard() {
    }

    public void initComponent() {
        log.info("Initializing Deckard plugin v" + VERSION + " (https://deckard.ai/)");

        // Set runtime constants
        IDE_NAME = PlatformUtils.getPlatformPrefix();
        IDE_VERSION = ApplicationInfo.getInstance().getFullVersion();

        setLoggingLevel();

        setupEventListeners();
        log.info("Finished initializing Deckard plugin");
    }

    private void setupEventListeners() {
        ApplicationManager.getApplication().invokeLater(new Runnable(){
            public void run() {

                // save file
                MessageBus bus = ApplicationManager.getApplication().getMessageBus();
                connection = bus.connect();
                connection.subscribe(AppTopics.FILE_DOCUMENT_SYNC, new CustomSaveListener());

                // edit document
                EditorFactory.getInstance().getEventMulticaster().addDocumentListener(new CustomDocumentListener());

                // mouse press
                EditorFactory.getInstance().getEventMulticaster().addEditorMouseListener(new CustomEditorMouseListener());
            }
        });
    }

    public void disposeComponent() {
        try {
            connection.disconnect();
        } catch(Exception e) { }
    }

    public static BigDecimal getCurrentTimestamp() {
        return new BigDecimal(String.valueOf(System.currentTimeMillis() / 1000.0)).setScale(4, BigDecimal.ROUND_HALF_UP);
    }

    private static String toJSON() {
        StringBuffer json = new StringBuffer();
        json.append("{}");
        return json.toString();
    }

    private static String jsonEscape(String s) {
        if (s == null)
            return null;
        StringBuffer escaped = new StringBuffer();
        final int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            switch(c) {
                case '\\': escaped.append("\\\\"); break;
                case '"': escaped.append("\\\""); break;
                case '\b': escaped.append("\\b"); break;
                case '\f': escaped.append("\\f"); break;
                case '\n': escaped.append("\\n"); break;
                case '\r': escaped.append("\\r"); break;
                case '\t': escaped.append("\\t"); break;
                default:
                    boolean isUnicode = (c >= '\u0000' && c <= '\u001F') || (c >= '\u007F' && c <= '\u009F') || (c >= '\u2000' && c <= '\u20FF');
                    if (isUnicode){
                        escaped.append("\\u");
                        String hex = Integer.toHexString(c);
                        for (int k = 0; k < 4 - hex.length(); k++) {
                            escaped.append('0');
                        }
                        escaped.append(hex.toUpperCase());
                    } else {
                        escaped.append(c);
                    }
            }
        }
        return escaped.toString();
    }

    public static void setLoggingLevel() {
        if (Deckard.DEBUG) {
            log.setLevel(Level.DEBUG);
            log.debug("Logging level set to DEBUG");
        } else {
            log.setLevel(Level.INFO);
        }
    }

    @NotNull
    public String getComponentName() {
        return "Deckard";
    }
}
