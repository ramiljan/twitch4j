package com.github.twitch4j;

import com.github.philippheuer.events4j.EventManager;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.helix.TwitchHelix;
import com.github.twitch4j.modules.ModuleLoader;
import kraken.TwitchKraken;

public class TwitchClient {

    /**
     * Event Manager
     */
    private final EventManager eventManager;

    /**
     * API: Helix
     */
    private final TwitchHelix helix;

    /**
     * API: Kraken
     */
    private final TwitchKraken kraken;

    /**
     * Chat
     */
    private final TwitchChat chat;

    /**
     * Modules
     */
    private final ModuleLoader moduleLoader;

    /**
     * Constructor
     *
     * @param eventManager EventManager
     * @param helix TwitchHelix
     * @param kraken TwitchKraken
     * @param chat TwitchChat
     */
    public TwitchClient(EventManager eventManager, TwitchHelix helix, TwitchKraken kraken, TwitchChat chat) {
        this.eventManager = eventManager;
        this.helix = helix;
        this.kraken = kraken;
        this.chat = chat;

        // module loader
        this.moduleLoader = new ModuleLoader(this);

        // register with serviceMediator
        this.eventManager.getServiceMediator().addService("twitch4j", this);
    }

    /**
     * Get the event manager
     *
     * @return EventManager
     */
    public EventManager getEventManager() {
        return this.eventManager;
    }

    /**
     * Get Helix
     *
     * @return TwitchHelix
     */
    public TwitchHelix getHelix() {
        if (this.helix == null) {
            throw new RuntimeException("You have not enabled the Helix Module! Please check out the documentation on Twitch4J -> Helix.");
        }

        return this.helix;
    }

    /**
     * Get Helix
     *
     * @return TwitchHelix
     */
    public TwitchKraken getKraken() {
        if (this.kraken == null) {
            throw new RuntimeException("You have not enabled the Kraken Module! Please check out the documentation on Twitch4J -> Kraken.");
        }

        return this.kraken;
    }

    /**
     * Get Chat
     *
     * @return TwitchChat
     */
    public TwitchChat getChat() {
        if (this.chat == null) {
            throw new RuntimeException("You have not enabled the Chat Module! Please check out the documentation on Twitch4J -> Chat.");
        }

        return this.chat;
    }

    /**
     * Get Module Loader
     *
     * @return ModuleLoader
     */
    public ModuleLoader getModuleLoader() {
        return this.moduleLoader;
    }
}
