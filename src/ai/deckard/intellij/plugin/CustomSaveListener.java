/* ==========================================================
File:        CustomSaveListener.java
Description: Deckard connector for JetBrains IDEs.
Maintainer:  Deckard <support@deckard.ai>
License:     BSD, see LICENSE for more details.
Website:     https://www.deckard.ai/
===========================================================*/

package ai.deckard.intellij.plugin;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerAdapter;


public class CustomSaveListener extends FileDocumentManagerAdapter {

    @Override
    public void beforeDocumentSaving(Document document) {
        final String fullPath = FileDocumentManager.getInstance().getFile(document).getPath();

        String content = document.getText();
        if(content.length() > 1000 * 1000) {
            content = "";
        }
        final String finalContent = content;

        Deckard.executor.execute(new Runnable() {
            @Override
            public void run() {
                Requests.post(
                        "change",
                        "{\"fullPath\":\"" +
                            Requests.jsonEscape(fullPath) +
                        "\",\"content\":\"" +
                            Requests.jsonEscape(finalContent) +
                        "\"}"
                );
            }
        });
    }
}