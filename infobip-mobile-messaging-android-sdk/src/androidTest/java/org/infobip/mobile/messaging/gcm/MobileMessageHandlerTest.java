package org.infobip.mobile.messaging.gcm;

import org.infobip.mobile.messaging.Message;
import org.infobip.mobile.messaging.MobileMessaging;
import org.infobip.mobile.messaging.storage.MessageStore;
import org.infobip.mobile.messaging.tools.MobileMessagingTestCase;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * @author sslavin
 * @since 15/02/2017.
 */

public class MobileMessageHandlerTest extends MobileMessagingTestCase {

    private MobileMessageHandler handler;
    private MessageStore commonStore;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        enableMessageStoreForReceivedMessages();

        handler = new MobileMessageHandler(mobileMessagingCore, broadcaster, notificationHandler, mobileMessagingCore.getMessageStoreWrapper());
        commonStore = MobileMessaging.getInstance(context).getMessageStore();
    }

    @Test
    public void test_shouldSaveNonGeoMessageToUserStore() throws Exception {

        // Given
        Message m = createMessage(context, "SomeMessageId", false);

        // When
        handler.handleMessage(m);

        // Then
        List<Message> messages = commonStore.findAll(context);
        assertEquals(1, messages.size());
        assertEquals("SomeMessageId", messages.get(0).getMessageId());
    }

    @Test
    public void test_shouldSend_messageReceived_forNonGeoMessage() throws Exception {

        // Given
        Message m = createMessage(context, "SomeMessageId", false);

        // When
        handler.handleMessage(m);

        // Then
        Mockito.verify(broadcaster, Mockito.after(1000).atLeastOnce()).messageReceived(Mockito.any(Message.class));
    }
}
