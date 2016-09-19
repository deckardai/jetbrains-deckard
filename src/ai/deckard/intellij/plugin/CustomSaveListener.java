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

import java.math.BigDecimal;

public class CustomSaveListener extends FileDocumentManagerAdapter {

    @Override
    public void beforeDocumentSaving(Document document) {
        String currentFile = FileDocumentManager.getInstance().getFile(document).getPath();
        Deckard.log(currentFile)
    }
}