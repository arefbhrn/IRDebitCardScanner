# IR Debit Card Scanner

[![JitPack](https://jitpack.io/v/arefbhrn/IRDebitCardScanner.svg)](https://jitpack.io/#arefbhrn/IRDebitCardScanner)

A lightweight android library to scan Iranian debit cards fast and realtime using Deep Learning and TensorFlow-Lite.
This library scans valid card numbers only.
Keep in mind that split ABIs while releasing your app to reduce its size.

To check stability and scan speed, check [STABILITY.md](./STABILITY.md) file.

## Preview

![Mellat card scan demo](./art/mellat.gif)

## Installation

Gradle:

```groovy
dependencies {
    implementation 'com.github.arefbhrn:IRDebitCardScanner:1.1.0'
}
```

## How To Use

(1) Start scanner activity and wait for result:

```kotlin
ScanActivity.start(this)
```

(2) Retrieve scanned data:

```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (ScanActivity.isScanResult(requestCode) && resultCode == Activity.RESULT_OK && data != null) {
        val scanResult = ScanActivity.debitCardFromResult(data)
        if (scanResult != null)
            Log.d("IRDCS", scanResult.number)
    }
}
```

### Debug Mode

(1) Start scanner activity in debug mode:

```kotlin
ScanActivity.startDebug(this)
```

In this mode you will see a scanned preview while scanning.

### Alternative Texts

(1) Start scanner activity with alternative texts:

```kotlin
ScanActivity.start(this, "IRDC Scanner",
    "Position your card in the frame so the card number is visible")
```

In this mode texts in scanner activity would be set as you prefer.

## Utilities

`DebitCardUtils` provides helper methods for card validation and formatting.

### Validate a card number

Uses a Luhn check combined with a known bank prefix lookup:

```kotlin
val isValid = DebitCardUtils.isCardNumberValid("6104331234567890")
```

### Get bank slug from card number

```kotlin
val bankSlug = DebitCardUtils.getBankSlugFromCardNumber("6104331234567890")
// e.g. "b_mellat"
```

### Validate an Iranian IBAN

```kotlin
val isValid = DebitCardUtils.isIbanValid("IR820540102680020817909002")
```

### Format a card number

Inserts a space every 4 digits:

```kotlin
val formatted = DebitCardUtils.format("6104331234567890")
// "6104 3312 3456 7890"
```

### Add custom card number prefixes

Add your own 6-digit prefixes to detectable bank slugs before scanning:

```kotlin
DebitCardUtils.addCardNumberStarters(mapOf("123456" to "my_bank"))
```

## Contact me

If you have a better idea or way on this project, please let me know. Thanks :) ?:

- [Email](mailto:arefprivate@gmail.com)
- [Website](https://arefdev.com)

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](https://opensource.org/licenses/Apache-2.0) file for details
