package metro.ExoticStamp.modules.rbac.domain;

import java.util.regex.Pattern;

public final class RbacValidationConstants {

    private RbacValidationConstants() {}

    /** Uppercase letters, digits, underscore — typical for role/permission codes. */
    public static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]*$");

    public static final String CODE_PATTERN_MESSAGE =
            "Code must start with a letter and contain only A-Z, 0-9, underscore";
}
