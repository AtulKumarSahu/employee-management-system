package com.ems.employeemanagement.enums;

/**
 * Gender options for employee profiles.
 *
 * <p>Stored as a string in the database.
 * PREFER_NOT_TO_SAY is included for inclusivity and GDPR compliance.
 */
public enum Gender {
    MALE,
    FEMALE,
    OTHER,
    PREFER_NOT_TO_SAY
}

