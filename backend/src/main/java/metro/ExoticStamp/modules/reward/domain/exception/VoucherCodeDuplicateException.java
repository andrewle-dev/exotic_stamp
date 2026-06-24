package metro.ExoticStamp.modules.reward.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

public class VoucherCodeDuplicateException extends DomainException {

    public VoucherCodeDuplicateException(String code) {
        super("Voucher code already exists: " + code);
    }
}
