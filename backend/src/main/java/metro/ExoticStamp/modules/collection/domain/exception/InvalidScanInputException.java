package metro.ExoticStamp.modules.collection.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

/**
 * NFC/QR scan payload is invalid (e.g. XOR violation).
 */
public class InvalidScanInputException extends DomainException {

    public InvalidScanInputException(String message) {
        super(message);
    }
}
