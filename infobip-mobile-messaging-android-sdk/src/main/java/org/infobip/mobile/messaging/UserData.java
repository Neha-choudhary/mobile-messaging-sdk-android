package org.infobip.mobile.messaging;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.infobip.mobile.messaging.api.support.http.serialization.JsonSerializer;
import org.infobip.mobile.messaging.util.DateTimeUtil;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.infobip.mobile.messaging.BroadcastParameter.EXTRA_USER_DATA;

/**
 * User data is used to provide user details to the server.
 * </p>
 * User data supports set of predefined fields ({@link PredefinedField}) and custom fields.
 * In order to delete any field, set it to null and report it to server.
 *
 * @author sslavin
 * @since 15/07/16.
 */
public class UserData {

    private static final JsonSerializer serializer = new JsonSerializer();

    private String externalUserId;
    private Map<String, Object> predefinedUserData;
    private Map<String, CustomUserDataValue> customUserData;

    public UserData() {
        this.externalUserId = null;
        this.predefinedUserData = new HashMap<>();
        this.customUserData = new HashMap<>();
    }

    public UserData(String userData) {
        UserData data = serializer.deserialize(userData, UserData.class);
        this.externalUserId = data.externalUserId;
        this.predefinedUserData = data.predefinedUserData;
        this.customUserData = data.customUserData;
    }

    protected UserData(String externalUserId, Map<String, Object> predefinedUserData, Map<String, CustomUserDataValue> customUserData) {
        this.externalUserId = externalUserId;
        this.predefinedUserData = predefinedUserData;
        this.customUserData = customUserData;
    }

    @Nullable
    public static UserData merge(UserData old, UserData latest) {
        if (old == null && latest == null) {
            return null;
        }

        UserData merged = new UserData();
        merged.add(old);
        merged.add(latest);
        return merged;
    }

    public static UserData createFrom(Bundle bundle) {
        return new UserData(bundle.getString(EXTRA_USER_DATA));
    }

    private void add(UserData data) {
        if (data == null) {
            return;
        }

        if (data.predefinedUserData != null) {
            this.predefinedUserData.putAll(data.predefinedUserData);
        }

        if (data.customUserData != null) {
            this.customUserData.putAll(data.customUserData);
        }

        this.externalUserId = data.externalUserId;
    }

    @Override
    public String toString() {
        return serializer.serialize(this);
    }

    public Map<String, Object> getPredefinedUserData() {
        return predefinedUserData;
    }

    public void setPredefinedUserData(Map<String, Object> predefinedUserData) {
        this.predefinedUserData = predefinedUserData;
    }

    public void setCustomUserData(Map<String, CustomUserDataValue> customUserData) {
        this.customUserData = customUserData;
    }

    public Map<String, CustomUserDataValue> getCustomUserData() {
        return customUserData;
    }

    public void setCustomUserDataElement(String key, CustomUserDataValue customUserDataValue) {
        this.customUserData.put(key, customUserDataValue);
    }

    public CustomUserDataValue getCustomUserDataValue(String key) {
        return this.customUserData.get(key);
    }

    public void removeCustomUserDataElement(String key) {
        this.customUserData.put(key, null);
    }

    public String getMsisdn() {
        return getField(PredefinedField.MSISDN);
    }

    public void setMsisdn(String msisdn) {
        setField(PredefinedField.MSISDN, msisdn);
    }

    public String getFirstName() {
        return getField(PredefinedField.FIRST_NAME);
    }

    public void setFirstName(String firstName) {
        setField(PredefinedField.FIRST_NAME, firstName);
    }

    public String getLastName() {
        return getField(PredefinedField.LAST_NAME);
    }

    public void setLastName(String lastName) {
        setField(PredefinedField.LAST_NAME, lastName);
    }

    public String getGender() {
        return getField(PredefinedField.GENDER);
    }

    public void setGender(String gender) {
        setField(PredefinedField.GENDER, gender);
    }

    public Date getBirthdate() {
        try {
            return DateTimeUtil.DateFromYMDString((String)getField(PredefinedField.BIRTHDATE));
        } catch (ParseException e) {
            return null;
        }
    }

    public void setBirthdate(Date birthdate) {
        String ymdString = DateTimeUtil.DateToYMDString(birthdate);
        setField(PredefinedField.BIRTHDATE, ymdString);
    }

    public String getEmail() {
        return getField(PredefinedField.EMAIL);
    }

    public void setEmail(String email) {
        setField(PredefinedField.EMAIL, email);
    }

    public String getMiddleName() {
        return getField(PredefinedField.MIDDLE_NAME);
    }

    public void setMiddleName(String middleName) {
        setField(PredefinedField.MIDDLE_NAME, middleName);
    }

    public void setExternalUserId(String externalUserId) {
        this.externalUserId = externalUserId;
    }

    public String getExternalUserId() {
        return externalUserId;
    }

    private <T> T getField(PredefinedField field) {
        if (predefinedUserData == null) {
            return null;
        }

        Object object = predefinedUserData.get(field.getKey());
        try {
            return (T) object;
        } catch (ClassCastException e) {
            return null;
        }
    }

    private void setField(PredefinedField field, Object value) {
        predefinedUserData.put(field.getKey(), value);
    }

    protected enum PredefinedField {
        MSISDN("msisdn"),
        FIRST_NAME("firstName"),
        LAST_NAME("lastName"),
        MIDDLE_NAME("middleName"),
        GENDER("gender"),
        BIRTHDATE("birthdate"),
        EMAIL("email");

        private final String key;

        PredefinedField(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }
}
