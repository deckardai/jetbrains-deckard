/* ==========================================================
File:        ApiKey.java
Description: Prompts user for api key if it does not exist.
Maintainer:  WakaTime <support@wakatime.com>
License:     BSD, see LICENSE for more details.
Website:     https://wakatime.com/
===========================================================*/

package com.wakatime.intellij.plugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.UUID;

public class ApiKey extends DialogWrapper {
    private final JPanel panel;
    private final JTextField input;
    private static String _api_key = "";

    public ApiKey(@Nullable Project project) {
        super(project, true);
        setTitle("WakaTime API Key");
        setOKButtonText("Save");
        panel = new JPanel();
        input = new JTextField(36);
        panel.add(input);

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return panel;
    }

    @Override
    protected ValidationInfo doValidate() {
        String apiKey = input.getText();
        try {
            UUID.fromString(apiKey);
        } catch (Exception e) {
            return new ValidationInfo("Invalid api key.");
        }
        return null;
    }

    @Override
    public void doOKAction() {
        ApiKey.setApiKey(input.getText());
        super.doOKAction();
    }

    public String promptForApiKey() {
        input.setText(ApiKey.getApiKey());
        this.show();
        return input.getText();
    }

    public static String getApiKey() {
        if (!ApiKey._api_key.equals("")) {
            return ApiKey._api_key;
        }

        String apiKey = ConfigFile.get("settings", "api_key");
        if (apiKey == null) apiKey = "";

        ApiKey._api_key = apiKey;
        return apiKey;
    }

    public static void setApiKey(String apiKey) {
        ConfigFile.set("settings", "api_key", apiKey);
        ApiKey._api_key = apiKey;
    }

}
