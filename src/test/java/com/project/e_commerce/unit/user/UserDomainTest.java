package com.project.e_commerce.unit.user;

import com.project.e_commerce.domain.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserDomainTest {

    // --- Email format: ^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$ ---

    @Test
    void validateEmail_acceptsValidFormats() {
        assertTrue(User.validateEmail("user@example.com"));
        assertTrue(User.validateEmail("user.name@example.com"));
        assertTrue(User.validateEmail("user+tag@example.co"));
        assertTrue(User.validateEmail("user_name@sub.example.com"));
        assertTrue(User.validateEmail("USER@EXAMPLE.COM"));
    }

    @Test
    void validateEmail_rejectsInvalidFormats() {
        assertFalse(User.validateEmail(""));
        assertFalse(User.validateEmail("userexample.com"));
        assertFalse(User.validateEmail("user@"));
        assertFalse(User.validateEmail("@example.com"));
        assertFalse(User.validateEmail("user@example"));
        assertFalse(User.validateEmail("user@.com"));
        assertFalse(User.validateEmail("user name@example.com"));
        assertFalse(User.validateEmail("user@exa mple.com"));
        assertFalse(User.validateEmail("user@example.c"));
    }

    // --- Username: trimmed length 3-15, letters/numbers/_/- only, no '@' or similar, not empty ---

    @Test
    void validateUsername_acceptsValidNames() {
        assertTrue(User.validateUsername("abc"));
        assertTrue(User.validateUsername("John_Doe"));
        assertTrue(User.validateUsername("user-name"));
        assertTrue(User.validateUsername("fifteenChars123"));
        assertTrue(User.validateUsername("  trimmedOk  "));
    }

    @Test
    void validateUsername_rejectsInvalidNames() {
        assertFalse(User.validateUsername(""));
        assertFalse(User.validateUsername("   "));
        assertFalse(User.validateUsername("ab"));
        assertFalse(User.validateUsername("  ab  "));
        assertFalse(User.validateUsername("thisNameHasMoreThan15Chars"));
        assertFalse(User.validateUsername("user@name"));
        assertFalse(User.validateUsername("user.name"));
        assertFalse(User.validateUsername("user name"));
    }

    // --- Password: length 8-20, any character allowed, never trimmed, not empty ---

    @Test
    void validatePassword_acceptsValidPasswords() {
        assertTrue(User.validatePassword("abcd1234"));
        assertTrue(User.validatePassword("P@ssw0rd!"));
        assertTrue(User.validatePassword("exactlyTwentyChars12"));
        assertTrue(User.validatePassword("with spaces 123"));
        assertTrue(User.validatePassword("símbolos_y_más1"));
    }

    @Test
    void validatePassword_rejectsInvalidPasswords() {
        assertFalse(User.validatePassword(""));
        assertFalse(User.validatePassword("short1"));
        assertFalse(User.validatePassword("thisPasswordHasMoreThanTwentyCharacters"));
    }

    @Test
    void validatePassword_doesNotTrim_spacesCountAsCharacters() {
        // Surrounding spaces are NOT trimmed; they count as real characters.
        // "  abcd1234  " is 12 chars, within the 8-20 range, so it stays valid as-is.
        String padded = "  abcd1234  ";
        assertTrue(User.validatePassword(padded));
    }
}
