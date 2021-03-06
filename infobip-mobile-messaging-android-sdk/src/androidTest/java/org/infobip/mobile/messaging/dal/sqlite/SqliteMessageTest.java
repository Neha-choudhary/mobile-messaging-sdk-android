package org.infobip.mobile.messaging.dal.sqlite;

import android.database.sqlite.SQLiteConstraintException;

import org.infobip.mobile.messaging.Message;
import org.infobip.mobile.messaging.tools.MobileMessagingTestCase;
import org.json.JSONObject;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.fail;

/**
 * @author sslavin
 * @since 12/01/2017.
 */

public class SqliteMessageTest extends MobileMessagingTestCase {

    @Test
    public void test_message_toFromSqlite() throws Exception {

        Message message = new Message(
                "SomeMessageId",
                "SomeTitle",
                "SomeBody",
                "SomeSound",
                true,
                "SomeIcon",
                false,
                "SomeCategory",
                "SomeFrom",
                1234L,
                5678L,
                9012L,
                new JSONObject() {{
                    put("stringValue", "StringValue2");
                    put("numberValue", 2);
                    put("booleanValue", true);
                }},
                null,
                "SomeDestination",
                Message.Status.SUCCESS,
                "SomeStatusMessage",
                "http://www.some-content.com.ru.hr"
        );

        databaseHelper.save(new SqliteMessage(message));

        List<SqliteMessage> messages = databaseHelper.findAll(SqliteMessage.class);
        assertEquals(1, messages.size());

        Message m = messages.get(0);
        assertNotSame(message, m);
        assertEquals("SomeMessageId", m.getMessageId());
        assertEquals("SomeTitle", m.getTitle());
        assertEquals("SomeBody", m.getBody());
        assertEquals("SomeSound", m.getSound());
        assertEquals(true, m.isVibrate());
        assertEquals("SomeIcon", m.getIcon());
        assertEquals(1234L, m.getReceivedTimestamp());
        assertEquals(5678L, m.getSeenTimestamp());
        assertEquals(9012L, m.getSentTimestamp());
        JSONAssert.assertEquals("{" +
                "'stringValue': 'StringValue2'," +
                "'numberValue': 2," +
                "'booleanValue':true" +
                "}", m.getCustomPayload(), true);
        assertEquals("SomeDestination", message.getDestination());
        assertEquals(Message.Status.SUCCESS, message.getStatus());
        assertEquals("SomeStatusMessage", message.getStatusMessage());
        assertEquals("http://www.some-content.com.ru.hr", message.getContentUrl());
    }

    @Test
    public void test_message_shouldNotSaveMessageWithNullId() throws Exception {
        Message message = new Message();
        message.setMessageId(null);

        try {
            databaseHelper.save(new SqliteMessage(message));
            fail();
        } catch (SQLiteConstraintException ignored) {
        }
    }
}