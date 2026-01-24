package me.shurik.bettersuggestions.client.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfigClient;
import me.shurik.bettersuggestions.config.ConfigStore;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AutoConfigClient.getConfigScreen(ConfigStore.class, parent).get();
    }
}