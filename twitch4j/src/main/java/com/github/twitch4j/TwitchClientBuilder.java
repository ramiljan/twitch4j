package com.github.twitch4j;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.auth.TwitchAuth;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.TwitchChatBuilder;
import com.github.twitch4j.common.config.Twitch4JGlobal;
import com.github.twitch4j.common.util.ThreadUtils;
import com.github.twitch4j.graphql.TwitchGraphQL;
import com.github.twitch4j.graphql.TwitchGraphQLBuilder;
import com.github.twitch4j.helix.TwitchHelix;
import com.github.twitch4j.helix.TwitchHelixBuilder;
import com.github.twitch4j.kraken.TwitchKraken;
import com.github.twitch4j.kraken.TwitchKrakenBuilder;
import com.github.twitch4j.pubsub.TwitchPubSub;
import com.github.twitch4j.pubsub.TwitchPubSubBuilder;
import com.github.twitch4j.tmi.TwitchMessagingInterface;
import com.github.twitch4j.tmi.TwitchMessagingInterfaceBuilder;
import io.github.bucket4j.Bandwidth;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Builder to get a TwitchClient Instance by provided various options, to provide the user with a lot of customizable options.
 */
@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TwitchClientBuilder {

    /**
     * Client Id
     */
    @With
    private String clientId = Twitch4JGlobal.clientId;

    /**
     * Client Secret
     */
    @With
    private String clientSecret = Twitch4JGlobal.clientSecret;

    /**
     * User Agent
     */
    @With
    private String userAgent = Twitch4JGlobal.userAgent;

    /**
     * HTTP Request Queue Size
     */
    @With
    private Integer requestQueueSize = -1;

    /**
     * Redirect Url
     */
    @With
    private String redirectUrl = "http://localhost";

    /**
     * Default Timeout
     */
    @With
    private Integer timeout = 5000;

    /**
     * Enabled: Helix
     */
    @With
    private Boolean enableHelix = false;

    /**
     * Enabled: Kraken
     */
    @With
    private Boolean enableKraken = false;

    /**
     * Enabled: TMI
     */
    @With
    private Boolean enableTMI = false;

    /**
     * Enabled: Chat
     */
    @With
    private Boolean enableChat = false;

    /**
     * IRC Command Handlers
     */
    protected final Set<String> commandPrefixes = new HashSet<>();

    /**
     * Enabled: PubSub
     */
    @With
    private Boolean enablePubSub = false;

    /**
     * Enabled: GraphQL
     */
    @With
    private Boolean enableGraphQL = false;

    /**
     * Chat Account
     */
    @With
    private OAuth2Credential chatAccount;

    /**
     * EventManager
     */
    @With
    private EventManager eventManager = null;

    /**
     * Size of the ChatQueue
     */
    @With
    protected Integer chatQueueSize = 200;

    /**
     * Custom RateLimit for ChatMessages
     */
    @With
    protected Bandwidth chatRateLimit = Bandwidth.simple(20, Duration.ofSeconds(30));

    /**
     * Wait time for taking items off chat queue in milliseconds. Default recommended
     */
    @With
    private long chatQueueTimeout = 1000L;

    /**
     * CredentialManager
     */
    @With
    private CredentialManager credentialManager = CredentialManagerBuilder.builder().build();

    /**
     * Scheduler Thread Pool Executor
     */
    @With
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = null;

    /**
     * Millisecond Delay for Client Helper Thread
     */
    @With
    private long helperThreadRate = 10000L;

    /**
     * Default Auth Token for API Requests
     */
    @With
    private OAuth2Credential defaultAuthToken = null;

    /**
     * With a CommandTrigger
     *
     * @param commandTrigger Command Trigger (Prefix)
     * @return TwitchClientBuilder
     */
    public TwitchClientBuilder withCommandTrigger(String commandTrigger) {
        this.commandPrefixes.add(commandTrigger);
        return this;
    }

    /**
     * Initialize the builder
     *
     * @return Twitch Client Builder
     */
    public static TwitchClientBuilder builder() {
        return new TwitchClientBuilder();
    }

    /**
     * Initialize
     *
     * @return {@link TwitchClient} initialized class
     */
    public TwitchClient build() {
        log.debug("TwitchClient: Initializing ErrorTracking ...");

        // Module: Auth (registers Twitch Identity Providers)
        TwitchAuth.registerIdentityProvider(credentialManager, getClientId(), getClientSecret(), redirectUrl);

        // Default EventManager
        if (eventManager == null) {
            eventManager = new EventManager();
            eventManager.autoDiscovery();
        }

        // Default ScheduledThreadPoolExecutor
        if(scheduledThreadPoolExecutor == null)
            scheduledThreadPoolExecutor = ThreadUtils.getDefaultScheduledThreadPoolExecutor();

        // Module: Helix
        TwitchHelix helix = null;
        if (this.enableHelix) {
            helix = TwitchHelixBuilder.builder()
                .withClientId(clientId)
                .withClientSecret(clientSecret)
                .withUserAgent(userAgent)
                .withRequestQueueSize(requestQueueSize)
                .withTimeout(timeout)
                .build();
        }

        // Module: Kraken
        TwitchKraken kraken = null;
        if (this.enableKraken) {
            kraken = TwitchKrakenBuilder.builder()
                .withClientId(clientId)
                .withClientSecret(clientSecret)
                .withUserAgent(userAgent)
                .withRequestQueueSize(requestQueueSize)
                .withTimeout(timeout)
                .build();
        }

        // Module: TMI
        TwitchMessagingInterface tmi = null;
        if (this.enableTMI) {
            tmi = TwitchMessagingInterfaceBuilder.builder()
                .withClientId(clientId)
                .withClientSecret(clientSecret)
                .withUserAgent(userAgent)
                .withRequestQueueSize(requestQueueSize)
                .withTimeout(timeout)
                .build();
        }

        // Module: Chat
        TwitchChat chat = null;
        if (this.enableChat) {
            chat = TwitchChatBuilder.builder()
                .withCredentialManager(credentialManager)
                .withChatAccount(chatAccount)
                .withChatQueueSize(chatQueueSize)
                .withChatRateLimit(chatRateLimit)
                .withScheduledThreadPoolExecutor(scheduledThreadPoolExecutor)
                .withChatQueueTimeout(chatQueueTimeout)
                .withCommandTriggers(commandPrefixes)
                .build();
        }

        // Module: PubSub
        TwitchPubSub pubSub = null;
        if (this.enablePubSub) {
            pubSub = TwitchPubSubBuilder.builder()
                .withEventManager(eventManager)
                .withScheduledThreadPoolExecutor(scheduledThreadPoolExecutor)
                .build();
        }

        // Module: GraphQL
        TwitchGraphQL graphql = null;
        if (this.enableGraphQL) {
            graphql = TwitchGraphQLBuilder.builder()
                .withEventManager(eventManager)
                .withClientId(clientId)
                .withClientSecret(clientSecret)
                .build();
        }

        // Module: TwitchClient & ClientHelper
        final TwitchClient client = new TwitchClient(eventManager, helix, kraken, tmi, chat, pubSub, graphql, scheduledThreadPoolExecutor);
        client.getClientHelper().setDefaultAuthToken(defaultAuthToken);
        client.getClientHelper().setThreadRate(helperThreadRate);

        // Return new Client Instance
        return client;
    }

}
