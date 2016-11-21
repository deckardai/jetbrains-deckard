package ai.deckard.intellij.plugin;

/**
 * Created by fenek on 21/11/2016.
 */


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;

public class PluginMenu extends AnAction {
    public PluginMenu() {
        super("Deckard");
    }
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
    }
}