/*
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 *
 */
package org.kaaproject.kaa.server.transports.http.transport;

import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.util.LinkedList;
import java.util.List;

import org.kaaproject.kaa.common.endpoint.gen.ConfigurationSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionCommand;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionCommandType;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequestMetaData;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.TopicState;
import org.kaaproject.kaa.server.transports.http.transport.commands.LongSyncCommand;

/**
 * HTTP Test for LongSync request.
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class HttpTestLongSyncClient extends HttpTestClient<SyncRequest, SyncResponse> {

    /** Defined application token */
    public static final String SDK_TOKEN = "123test";

    /** Hash size, in test we use fake hashes gust generating them as random bytes */
    public static final int HASH_SIZE = 32;

    /** Max subscription command size, actual value get random size */
    public static final int MAX_SUBSCRIPTION_COMMANDS_SIZE  = 100;

    /** Defined topic id length */
    public static final int TOPIC_ID_LENGTH  = 10;

    /** profile hash byte array */
    private byte[] profileHash;

    /** configuration hash byte array */
    private byte[] configurationHash;

    /** subscription commands list */
    private List<SubscriptionCommand> subscriptionCommands;

    /** topic state list */
    private List<TopicState> topicStates;

    /** application seq number, in test it used to pass testId */
    private int appStateSeqNumber;

    /**
     * Constructor
     * @param serverPublicKey - server public key
     * @param activity - interface to notify request finish.
     * @throws MalformedURLException - if URI is incorrect
     * @throws Exception - if request creation failed
     */
    public HttpTestLongSyncClient(
            PublicKey serverPublicKey,
            HttpActivity<SyncResponse> activity)
            throws MalformedURLException, Exception {
        super(serverPublicKey, LongSyncCommand.getCommandName(), activity);
        longSyncInit();
        postInit(getRequest());
    }

    /**
     * Create SyncRequest.
     */
    private void longSyncInit() {
        SyncRequest request = new SyncRequest();
        request.setSyncRequestMetaData(new SyncRequestMetaData());
        setRequest(request);
        getRequest().getSyncRequestMetaData().setTimeout(Long.valueOf(rnd.nextInt(10000000)));
        syncInit();
    }

    /**
     * Create SyncRequest.
     */
    private void syncInit() {

        profileHash = getRandomBytes(HASH_SIZE);
        configurationHash = getRandomBytes(HASH_SIZE);
        appStateSeqNumber = getId();

        SyncRequest request = new SyncRequest();
        request.setSyncRequestMetaData(new SyncRequestMetaData());
        request.getSyncRequestMetaData().setSdkToken(SDK_TOKEN);
        request.getSyncRequestMetaData().setEndpointPublicKeyHash(ByteBuffer.wrap(getClientPublicKeyHash().getData()));
        request.getSyncRequestMetaData().setProfileHash(ByteBuffer.wrap(profileHash));

        ConfigurationSyncRequest csRequest = new ConfigurationSyncRequest();
        csRequest.setConfigurationHash(ByteBuffer.wrap(configurationHash));
        request.setConfigurationSyncRequest(csRequest);

        generateSubscriptionCommandList();
        NotificationSyncRequest nsRequest = new NotificationSyncRequest();
        nsRequest.setSubscriptionCommands(subscriptionCommands);
        nsRequest.setTopicStates(topicStates);
        nsRequest.setTopicListHash(1);
        request.setNotificationSyncRequest(nsRequest);

        setRequest(request);
    }

    /**
     * generate subscription command list
     */
    private void generateSubscriptionCommandList() {
        subscriptionCommands = new LinkedList<>();
        topicStates = new LinkedList<>();
        int sc_size = Math.round(rnd.nextFloat()*MAX_SUBSCRIPTION_COMMANDS_SIZE);
        for(int i=0; i<sc_size;i++) {
            SubscriptionCommand sc = null;
            if (rnd.nextBoolean()) {
                sc = new SubscriptionCommand(rnd.nextLong(), SubscriptionCommandType.ADD);
            } else {
                sc = new SubscriptionCommand(rnd.nextLong(), SubscriptionCommandType.REMOVE);
            }
            topicStates.add(new TopicState(sc.getTopicId(), rnd.nextInt()));
            subscriptionCommands.add(sc);
        }
    }

    /**
     * @return the profileHash
     */
    public byte[] getProfileHash() {
        return profileHash;
    }

    /**
     * @return the configurationHash
     */
    public byte[] getConfigurationHash() {
        return configurationHash;
    }

    /**
     * @return the subscriptionCommands
     */
    public List<SubscriptionCommand> getSubscriptionCommands() {
        return subscriptionCommands;
    }

    /**
     * @return the topicStates
     */
    public List<TopicState> getTopicStates() {
        return topicStates;
    }

    /**
     * @return the appStateSeqNumber
     */
    public int getAppStateSeqNumber() {
        return appStateSeqNumber;
    }

    @Override
    protected Class<SyncRequest> getRequestConverterClass() {
        return SyncRequest.class;
    }

    @Override
    protected Class<SyncResponse> getResponseConverterClass() {
        return SyncResponse.class;
    }

}
