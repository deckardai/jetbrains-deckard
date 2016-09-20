/* ==========================================================
File:        CustomSelectionListener.java
Description: Deckard connector for JetBrains IDEs.
Maintainer:  Deckard <support@deckard.ai>
License:     BSD, see LICENSE for more details.
Website:     https://www.deckard.ai/
===========================================================*/

package ai.deckard.intellij.plugin;

import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;

public class CustomSelectionListener implements SelectionListener {

    public int maxLength = 1000;

    public long minPauseMs = 500;
    protected long lastEventMs = 0;

    @Override
    public void selectionChanged(SelectionEvent selectionEvent) {
        final FileDocumentManager instance = FileDocumentManager.getInstance();
        final VirtualFile file = instance.getFile(selectionEvent.getEditor().getDocument());
        if (file != null && !file.getUrl().startsWith("mock://")) {

            final String currentFile = file.getPath();
            TextRange range = selectionEvent.getNewRange();

            if(range.getLength() == 0){
                return;
            }

            long nowMs = System.currentTimeMillis();
            if(nowMs - lastEventMs < minPauseMs){
                return;
            }
            lastEventMs = nowMs;

            int startOffset = range.getStartOffset();
            int endOffset = range.getEndOffset();
            if(endOffset - startOffset > maxLength) {
                endOffset = startOffset + maxLength;
            }

            final LogicalPosition pos = selectionEvent.getEditor().offsetToLogicalPosition(startOffset);
            final LogicalPosition endPos = selectionEvent.getEditor().offsetToLogicalPosition(endOffset);
            final String text = selectionEvent.getEditor().getDocument().getText(range);

            Deckard.executor.execute(new Runnable() {
                @Override
                public void run() {
                    Requests.post(
                        "event",
                        "{\"path\":\"" +
                            Requests.jsonEscape(file.getPath()) +
                        "\",\"lineno\":" +
                            Integer.toString(pos.line) +
                        ",\"charno\":" +
                            Integer.toString(pos.column) +
                        ",\"end\":{\"lineno\":" +
                            Integer.toString(endPos.line) +
                        ",\"charno\":" +
                            Integer.toString(endPos.column) +
                        "},\"text\":\"" +
                            Requests.jsonEscape(text) +
                        "\",\"editor\":\"idea\"}"
                    );
                }
            });
        }
    }
}
