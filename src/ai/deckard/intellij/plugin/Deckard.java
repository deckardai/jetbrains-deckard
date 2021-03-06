/* ==========================================================
File:        Deckard.java
Description: Deckard connector for JetBrains IDEs.
Maintainer:  Deckard <support@deckard.ai>
License:     BSD, see LICENSE for more details.
Website:     https://www.deckard.ai/
===========================================================*/

package ai.deckard.intellij.plugin;

import com.intellij.AppTopics;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.util.PlatformUtils;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Level;

public class Deckard implements ApplicationComponent {

    public static final String VERSION = "0.2.12";
    public static final Logger log = Logger.getInstance("Deckard");

    public static String backendUrl = "http://localhost:3325/";

    public static String IDE_NAME;
    public static String IDE_VERSION;
    public static Boolean DEBUG = false;

    public static MessageBusConnection busConnection;
    protected static ExecutorService executor = Executors.newSingleThreadExecutor();


    public static String makeIdeName() {
        if(PlatformUtils.isPyCharm()) {
            return "pycharm";
        }
        if(PlatformUtils.isRubyMine()) {
            return "rubymine";
        }
        return PlatformUtils.getPlatformPrefix().toLowerCase();
    }

    public Deckard() {
    }

    public void initComponent() {
        log.info("Initializing Deckard plugin v" + VERSION + " (https://deckard.ai/)");

        // Set runtime constants
        IDE_NAME = makeIdeName();
        IDE_VERSION = ApplicationInfo.getInstance().getFullVersion();
        log.info("Editor name: " + IDE_NAME);

        setLoggingLevel();

        setupEventListeners();
        log.info("Finished initializing Deckard plugin");
    }

    private void setupEventListeners() {
        ApplicationManager.getApplication().invokeLater(new Runnable(){
            public void run() {
                // Selection
                EditorFactory.getInstance().getEventMulticaster().addSelectionListener(new CustomSelectionListener());

                // File save
                MessageBus bus = ApplicationManager.getApplication().getMessageBus();
                busConnection = bus.connect();
                busConnection.subscribe(AppTopics.FILE_DOCUMENT_SYNC, new CustomSaveListener());
            }

        });
    }

    public void disposeComponent() {
        try {
            busConnection.disconnect();
        } catch(Exception e) { }
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
