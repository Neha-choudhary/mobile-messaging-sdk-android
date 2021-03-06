package org.infobip.mobile.messaging.api.registration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cloud registration sync response encapsulation.
 *
 * @author mstipanov
 * @see MobileApiRegistration
 * @see MobileApiRegistration#upsert(String, Boolean)
 * @since 17.03.2016.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponse {

    /**
     * Application instance ID on Mobile Messaging backend.
     */
    private String deviceApplicationInstanceId;

    /**
     * Push registration status (enabled/disabled) on Mobile Messaging backend.
     */
    private Boolean pushRegistrationEnabled;
}
