/* ==========================================================
File:        CustomSelectionListener.java
Description: Deckard connector for JetBrains IDEs.
Maintainer:  Deckard <support@deckard.ai>
License:     BSD, see LICENSE for more details.
Website:     https://www.deckard.ai/
===========================================================*/

package ai.deckard.intellij.plugin;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
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
        final Editor editor = selectionEvent.getEditor();
        final Document document = editor.getDocument();

        final VirtualFile file = instance.getFile(document);
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

            final LogicalPosition pos = editor.offsetToLogicalPosition(startOffset);
            final LogicalPosition endPos = editor.offsetToLogicalPosition(endOffset);
            final String text = document.getText(range);

            /* The logical position give us access to the column shown in the editor
               which integrate a computation for 'tab' that we need to subtract.
               Therefore, we compute in the following section this 'shift' */
            int shift = 0;
            CharSequence chars = document.getCharsSequence();
            int tabSize = editor.getSettings().getTabSize(editor.getProject());
            for (int i = document.getLineStartOffset(pos.line); i < startOffset; i++) {
                char c = chars.charAt(i);
                if (c == '\t') {
                    shift += tabSize - 1;
                }
            }
            final int tabShift = shift;

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
                            Integer.toString(pos.column-tabShift) +
                        ",\"end\":{\"lineno\":" +
                            Integer.toString(endPos.line) +
                        ",\"charno\":" +
                            Integer.toString(endPos.column-tabShift) +
                        "},\"text\":\"" +
                            Requests.jsonEscape(text) +
                        "\",\"editor\":\"" +
                            Requests.jsonEscape(Deckard.IDE_NAME) +
                        "\"}"
                    );
                }
            });
        }
    }
}
