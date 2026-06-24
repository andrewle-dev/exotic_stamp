/// Scan input type accepted by the backend.
enum ScanType {
  nfc('NFC'),
  qr('QR_STATIC');

  const ScanType(this.apiValue);

  final String apiValue;
}
