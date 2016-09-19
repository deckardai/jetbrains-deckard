/* ==========================================================
File:        CustomEditorMouseListener.java
Description: Deckard connector for JetBrains IDEs.
Maintainer:  Deckard <support@deckard.ai>
License:     BSD, see LICENSE for more details.
Website:     https://www.deckard.ai/
===========================================================*/

package ai.deckard.intellij.plugin;

import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;

import java.math.BigDecimal;

public class CustomEditorMouseListener  implements EditorMouseListener {
    @Override
    public void mousePressed(EditorMouseEvent editorMouseEvent) {
        final FileDocumentManager instance = FileDocumentManager.getInstance();
        final VirtualFile file = instance.getFile(editorMouseEvent.getEditor().getDocument());
        if (file != null && !file.getUrl().startsWith("mock://")) {
            final String currentFile = file.getPath();
            Deckard.log.info(currentFile);
        }
    }

    @Override
    public void mouseClicked(EditorMouseEvent editorMouseEvent) {
    }

    @Override
    public void mouseReleased(EditorMouseEvent editorMouseEvent) {
    }

    @Override
    public void mouseEntered(EditorMouseEvent editorMouseEvent) {
    }

    @Override
    public void mouseExited(EditorMouseEvent editorMouseEvent) {
    }
}
