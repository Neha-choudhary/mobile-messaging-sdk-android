package org.infobip.mobile.messaging.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.infobip.mobile.messaging.MobileMessagingProperty;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author mstipanov
 * @since 07.04.2016.
 */
public abstract class PreferenceHelper {
    private static final Object LOCK = new Object();
    private static Cryptor cryptor = null;

    protected PreferenceHelper() {
    }

    private static Cryptor getCryptor(Context context) {
        if (cryptor != null) {
            return cryptor;
        }

        cryptor = new Cryptor(DeviceInformation.getDeviceID(context));
        return cryptor;
    }

    public static String findString(Context context, MobileMessagingProperty property) {
        return findString(context, property.getKey(), (String) property.getDefaultValue(), property.isEncrypted());
    }

    public static String findString(Context context, String key, String defaultValue) {
        return findString(context, key, defaultValue, false);
    }

    public static String findString(Context context, String key, String defaultValue, boolean encrypted) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (!encrypted) {
            return sharedPreferences.getString(key, defaultValue);
        }

        String encryptedKey = getCryptor(context).encrypt(key);
        String encryptedValue = sharedPreferences.getString(encryptedKey, defaultValue);
        return getCryptor(context).decrypt(encryptedValue);
    }

    public static void saveString(Context context, MobileMessagingProperty property, String value) {
        saveString(context, property.getKey(), value, property.isEncrypted());
    }

    public static void saveString(Context context, String key, String value, boolean encrypted) {
        if (!encrypted) {
            saveString(context, key, value);
            return;
        }

        String encryptedKey = getCryptor(context).encrypt(key);
        String encryptedValue = getCryptor(context).encrypt(value);
        saveString(context, encryptedKey, encryptedValue);
    }

    public static void saveString(Context context, String key, String value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (null == value) {
            remove(context, key);
            return;
        }
        sharedPreferences.edit().putString(key, value).apply();
    }

    public static long findLong(Context context, MobileMessagingProperty property) {
        Object defaultValue = property.getDefaultValue();
        if (null == defaultValue) {
            defaultValue = 0L;
        }
        return findLong(context, property.getKey(), (Long) defaultValue);
    }

    public static long findLong(Context context, String key, long defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String string = sharedPreferences.getString(key, String.valueOf(defaultValue));
        if (StringUtils.isBlank(string)) {
            return 0;
        }
        return Long.parseLong(string);
    }

    public static void saveLong(Context context, MobileMessagingProperty property, long value) {
        saveLong(context, property.getKey(), value);
    }

    public static void saveLong(Context context, String key, long value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(key, String.valueOf(value)).apply();
    }

    public static <T> Class<T> findClass(Context context, MobileMessagingProperty property) {
        return findClass(context, property.getKey(), (Class<T>) property.getDefaultValue());
    }

    public static <T> Class<T> findClass(Context context, String key, Class<T> defaultValue) {
        String callbackActivityClassName = findString(context, key, null);
        if (StringUtils.isBlank(callbackActivityClassName)) {
            return defaultValue;
        }
        //TODO cache
        try {
            return (Class<T>) Class.forName(callbackActivityClassName);
        } catch (ClassNotFoundException e) {
            //TODO log
            return null;
        }
    }

    public static void saveClass(Context context, MobileMessagingProperty property, Class<?> aClass) {
        saveClass(context, property.getKey(), aClass);
    }

    public static void saveClass(Context context, String key, Class<?> aClass) {
        String value = null != aClass.getName() ? aClass.getName() : null;
        saveString(context, key, value);
    }

    public static boolean findBoolean(Context context, MobileMessagingProperty property) {
        return findBoolean(context, property.getKey(), Boolean.TRUE.equals(property.getDefaultValue()));
    }

    public static boolean findBoolean(Context context, String key, boolean defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public static void saveBoolean(Context context, MobileMessagingProperty property, boolean value) {
        saveBoolean(context, property.getKey(), value);
    }

    public static void saveBoolean(Context context, String key, boolean value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public static int findInt(Context context, MobileMessagingProperty property) {
        Object defaultValue = property.getDefaultValue();
        int defaultInt = 0;
        if (null != defaultValue) {
            defaultInt = (int) defaultValue;
        }
        return findInt(context, property.getKey(), defaultInt);
    }

    public static int findInt(Context context, String key, int defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(key, defaultValue);
    }

    public static void saveInt(Context context, MobileMessagingProperty property, int value) {
        saveInt(context, property.getKey(), value);
    }

    public static void saveInt(Context context, String key, int value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putInt(key, value).apply();
    }

    public static long[] findLongArray(Context context, MobileMessagingProperty property) {
        return findLongArray(context, property.getKey(), (long[]) property.getDefaultValue());
    }

    public static long[] findLongArray(Context context, String key, long[] defaultValue) {
        String vibrate = findString(context, key, null);
        if (null == vibrate) {
            return defaultValue;
        }
        //TODO cache
        try {
            JSONArray jsonArray = new JSONArray(vibrate);
            long[] vibratePattern = new long[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                vibratePattern[i] = jsonArray.getLong(i);
            }
            return vibratePattern;
        } catch (JSONException e) {
            //TODO log
            return defaultValue;
        }
    }

    public static void saveLongArray(Context context, MobileMessagingProperty property, long[] value) {
        saveLongArray(context, property.getKey(), value);
    }

    public static void saveLongArray(Context context, String key, long[] value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (null == value) {
            remove(context, key);
            return;
        }
        JSONArray jsonArray = new JSONArray();
        for (long aValue : value) {
            jsonArray.put(aValue);
        }
        sharedPreferences.edit().putString(key, jsonArray.toString()).apply();
    }

    public static String[] findAndRemoveStringArray(Context context, MobileMessagingProperty property) {
        final List<String> strings = new ArrayList<>();
        editSet(context, property.getKey(), new SetMutator() {
            @Override
            public void mutate(Set<String> set) {
                strings.addAll(set);
                set.clear();
            }
        });
        return strings.toArray(new String[strings.size()]);
    }

    public static String[] findStringArray(Context context, MobileMessagingProperty property) {
        return findStringArray(context, property.getKey(), (String[]) property.getDefaultValue());
    }

    public static String[] findStringArray(Context context, String key, String[] defaultValue) {
        return find(context, key, defaultValue, new SetConverter<String[]>() {
            @Override
            public String[] convert(Set<String> set) {
                return set.toArray(new String[set.size()]);
            }
        });
    }

    public static <T> T find(Context context, String key, T defaultValue, SetConverter<T> converter) {
        synchronized (LOCK) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            Set<String> value = sharedPreferences.getStringSet(key, null);
            if (null == value) {
                return defaultValue;
            }
            return converter.convert(value);
        }
    }

    public static void appendToStringArray(Context context, MobileMessagingProperty property, String... strings) {
        appendToStringArray(context, property.getKey(), strings);
    }

    public static void appendToStringArray(Context context, String key, final String... strings) {
        SetMutator mutator = new SetMutator() {
            @Override
            public void mutate(Set<String> set) {
                set.addAll(Arrays.asList(strings));
            }
        };
        editSet(context, key, mutator);
    }

    public static void deleteFromStringArray(Context context, MobileMessagingProperty property, String... strings) {
        deleteFromStringArray(context, property.getKey(), strings);
    }

    public static void deleteFromStringArray(Context context, String key, final String... strings) {
        SetMutator mutator = new SetMutator() {
            @Override
            public void mutate(Set<String> set) {
                set.removeAll(Arrays.asList(strings));
            }
        };
        editSet(context, key, mutator);
    }

    public static void saveStringArray(Context context, MobileMessagingProperty property, String... strings) {
        saveStringArray(context, property.getKey(), strings);
    }

    public static void saveStringArray(Context context, String key, final String... strings) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final HashSet<String> stringSet = new HashSet<String>() {{
            addAll(Arrays.asList(strings));
        }};
        sharedPreferences.edit().putStringSet(key, stringSet).apply();
    }

    public static Set<String> findStringSet(Context context, MobileMessagingProperty property) {
        return findStringSet(context, property.getKey(), (Set<String>) property.getDefaultValue());
    }

    public static Set<String> findStringSet(Context context, String key, Set<String> defaultValue) {
        return find(context, key, defaultValue, new SetConverter<Set<String>>() {
            @Override
            public Set<String> convert(Set<String> set) {
                return set;
            }
        });
    }

    public static void saveStringSet(Context context, MobileMessagingProperty property, final Set<String> set) {
        saveStringSet(context, property.getKey(), set);
    }

    public static void saveStringSet(Context context, String key, final Set<String> set) {
        SetMutator mutator = new SetMutator() {
            @Override
            public void mutate(Set<String> innerSet) {
                innerSet.clear();
                innerSet.addAll(set);
            }
        };
        editSet(context, key, mutator);
    }

    public static void editSet(Context context, String key, SetMutator mutator) {
        synchronized (LOCK) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            final Set<String> set = new HashSet<>(sharedPreferences.getStringSet(key, new HashSet<String>()));
            mutator.mutate(set);
            if (set.isEmpty()) {
                remove(context, key);
                return;
            }
            sharedPreferences.edit().putStringSet(key, set).apply();
        }
    }

    public static void remove(Context context, MobileMessagingProperty property) {
        String key = property.getKey();
        if (property.isEncrypted()) {
            key = getCryptor(context).encrypt(key);
        }
        remove(context, key);
    }

    public static void remove(Context context, String key) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .remove(key)
                .apply();
    }

    public static boolean contains(Context context, MobileMessagingProperty property) {
        String key = property.getKey();
        if (property.isEncrypted()) {
            key = getCryptor(context).encrypt(key);
        }
        return contains(context, key);
    }

    public static boolean contains(Context context, String key) {
        return PreferenceManager.getDefaultSharedPreferences(context).contains(key);
    }

    public static void registerOnSharedPreferenceChangeListener(Context context, SharedPreferences.OnSharedPreferenceChangeListener listener) {
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(listener);
    }

    public static <Result> Result runTransaction(Transaction<Result> transaction) {
        synchronized (LOCK) {
            return transaction.run();
        }
    }

    public interface SetMutator {
        void mutate(Set<String> set);
    }

    public interface SetConverter<T> {
        T convert(Set<String> set);
    }

    public interface Transaction<Result> {
        Result run();
    }
}
