package org.infobip.mobile.messaging.interactive.inapp.rules;

import android.app.Activity;

import org.infobip.mobile.messaging.Message;
import org.infobip.mobile.messaging.interactive.MobileInteractive;
import org.infobip.mobile.messaging.interactive.NotificationAction;
import org.infobip.mobile.messaging.interactive.NotificationCategory;
import org.infobip.mobile.messaging.interactive.PredefinedNotificationAction;
import org.infobip.mobile.messaging.interactive.inapp.foreground.ForegroundState;
import org.infobip.mobile.messaging.interactive.inapp.foreground.ForegroundStateMonitor;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author sslavin
 * @since 18/04/2018.
 */
public class InAppRulesTest {

    private InAppRules inAppRules;

    private MobileInteractive mobileInteractive = mock(MobileInteractive.class);
    private ForegroundStateMonitor foregroundStateMonitor = mock(ForegroundStateMonitor.class);
    private Message message = mock(Message.class);
    private Activity activity = mock(Activity.class);
    private NotificationAction[] defaultActions;

    @Before
    public void before() {
        reset(mobileInteractive, foregroundStateMonitor, message);
        when(message.getInternalData()).thenReturn("{\"inApp\":true}");
        inAppRules = new InAppRules(mobileInteractive, foregroundStateMonitor);

        defaultActions = PredefinedNotificationAction.defaultInAppActions();
    }

    @Test
    public void shouldNotDisplayIfInAppNotConfigured() {
        when(message.getInternalData()).thenReturn("{}");
        ShowOrNot showOrNot = inAppRules.shouldDisplayDialogFor(message);
        assertEquals(false, showOrNot.shouldShowNow());
        assertEquals(false, showOrNot.shouldShowWhenInForeground());
    }

    @Test
    public void shouldNotDisplayIfInAppDisabled() {
        when(message.getInternalData()).thenReturn("{\"inApp\":false}");
        ShowOrNot showOrNot = inAppRules.shouldDisplayDialogFor(message);
        assertEquals(false, showOrNot.shouldShowNow());
        assertEquals(false, showOrNot.shouldShowWhenInForeground());
    }

    @Test
    public void shouldDisplayIfSilent() {
        when(message.isSilent()).thenReturn(true);
        when(foregroundStateMonitor.isInForeground()).thenReturn(ForegroundState.foreground(activity));

        ShowOrNot showOrNot = inAppRules.shouldDisplayDialogFor(message);
        assertEquals(true, showOrNot.shouldShowNow());
        assertEquals(false, showOrNot.shouldShowWhenInForeground());
    }

    @Test
    public void shouldDisplayWithDefaultActionsIfNoCategoryAndForeground() {
        when(foregroundStateMonitor.isInForeground()).thenReturn(ForegroundState.foreground(activity));

        ShowOrNot showOrNot = inAppRules.shouldDisplayDialogFor(message);
        verifyDisplayNowWithDefaultActions(showOrNot);
    }

    @Test
    public void shouldDisplayWithDefaultActionsIfNoCategoryStoredAndForeground() {
        String categoryId = "categoryId";
        when(message.getCategory()).thenReturn(categoryId);
        when(mobileInteractive.getNotificationCategory(eq(categoryId))).thenReturn(null);
        when(foregroundStateMonitor.isInForeground()).thenReturn(ForegroundState.foreground(activity));

        ShowOrNot showOrNot = inAppRules.shouldDisplayDialogFor(message);
        verifyDisplayNowWithDefaultActions(showOrNot);
    }

    @Test
    public void shouldDisplayWithDefaultActionsIfNoActionsStoredForCategoryAndForeground() {
        String categoryId = "categoryId";
        when(message.getCategory()).thenReturn(categoryId);
        when(mobileInteractive.getNotificationCategory(eq(categoryId))).thenReturn(new NotificationCategory(categoryId));
        when(foregroundStateMonitor.isInForeground()).thenReturn(ForegroundState.foreground(activity));

        ShowOrNot showOrNot = inAppRules.shouldDisplayDialogFor(message);
        verifyDisplayNowWithDefaultActions(showOrNot);
    }

    @Test
    public void shouldDisplayWithDefaultActionsIfOnlyInputActionsInCategoryAndForeground() {
        String categoryId = "categoryId";
        when(message.getCategory()).thenReturn(categoryId);
        when(mobileInteractive.getNotificationCategory(eq(categoryId))).thenReturn(new NotificationCategory(categoryId, inputAction()));
        when(foregroundStateMonitor.isInForeground()).thenReturn(ForegroundState.foreground(activity));

        ShowOrNot showOrNot = inAppRules.shouldDisplayDialogFor(message);
        verifyDisplayNowWithDefaultActions(showOrNot);
    }

    @Test
    public void shouldDisplayActionNowIfOrdinaryActionsExistAndForeground() {
        String categoryId = "categoryId";
        NotificationAction action = ordinaryAction();
        NotificationCategory category = category(categoryId, inputAction(), action);
        when(message.getCategory()).thenReturn(categoryId);
        when(mobileInteractive.getNotificationCategory(eq(categoryId))).thenReturn(category);
        when(foregroundStateMonitor.isInForeground()).thenReturn(ForegroundState.foreground(activity));

        ShowOrNot showOrNot = inAppRules.shouldDisplayDialogFor(message);
        assertEquals(true, showOrNot.shouldShowNow());
        assertEquals(false, showOrNot.shouldShowWhenInForeground());
        assertEquals(category, showOrNot.getCategory());
        assertEquals(action, showOrNot.getActionsToShowFor()[0]);
        assertEquals(activity, showOrNot.getBaseActivityForDialog());
    }

    @Test
    public void shouldDisplayActionLaterIfOrdinaryActionsExistAndBackground() {
        String categoryId = "categoryId";
        NotificationAction action = ordinaryAction();
        NotificationCategory category = category(categoryId, inputAction(), action);
        when(message.getCategory()).thenReturn(categoryId);
        when(mobileInteractive.getNotificationCategory(eq(categoryId))).thenReturn(category);
        when(foregroundStateMonitor.isInForeground()).thenReturn(ForegroundState.background());

        ShowOrNot showOrNot = inAppRules.shouldDisplayDialogFor(message);
        assertEquals(false, showOrNot.shouldShowNow());
        assertEquals(true, showOrNot.shouldShowWhenInForeground());
        assertEquals(0, showOrNot.getActionsToShowFor().length);
        assertNull(showOrNot.getCategory());
        assertNull(showOrNot.getBaseActivityForDialog());
    }

    @Test
    public void shouldDisplayWithDefaultActionsLaterIfInAppConfiguredNoCategoryAndBackground() {
        when(foregroundStateMonitor.isInForeground()).thenReturn(ForegroundState.background());

        ShowOrNot showOrNot = inAppRules.shouldDisplayDialogFor(message);
        assertEquals(false, showOrNot.shouldShowNow());
        assertEquals(true, showOrNot.shouldShowWhenInForeground());
        assertEquals(0, showOrNot.getActionsToShowFor().length);
        assertNull(showOrNot.getCategory());
        assertNull(showOrNot.getBaseActivityForDialog());
    }

    private void verifyDisplayNowWithDefaultActions(ShowOrNot showOrNot) {
        assertEquals(true, showOrNot.shouldShowNow());
        assertEquals(false, showOrNot.shouldShowWhenInForeground());
        assertEquals(defaultActions[0].getId(), showOrNot.getActionsToShowFor()[0].getId());
        assertEquals(defaultActions[1].getId(), showOrNot.getActionsToShowFor()[1].getId());
    }

    private NotificationAction inputAction() {
        return new NotificationAction.Builder()
                .withId("id1")
                .withTitleText("title")
                .withInput()
                .build();
    }

    private NotificationAction ordinaryAction() {
        return new NotificationAction.Builder()
                .withId("id2")
                .withTitleText("title")
                .build();
    }

    private NotificationCategory category(String categoryId, NotificationAction... actions) {
        return new NotificationCategory(categoryId, actions);
    }
}
