package com.freetime.notifications

import java.math.BigInteger
import java.security.MessageDigest

/** Returns null for tags that cannot safely be ordered as app versions. */
internal fun compareReleaseVersions(candidate: String, installed: String): Int? {
    val pattern = Regex("^[vV]?([0-9]+(?:\\.[0-9]+)*)(?:-([0-9A-Za-z.-]+))?(?:\\+[0-9A-Za-z.-]+)?$")
    val left = pattern.matchEntire(candidate.trim()) ?: return null
    val right = pattern.matchEntire(installed.trim()) ?: return null
    val leftNumbers = left.groupValues[1].split('.').map(::BigInteger)
    val rightNumbers = right.groupValues[1].split('.').map(::BigInteger)
    for (index in 0 until maxOf(leftNumbers.size, rightNumbers.size)) {
        val result = (leftNumbers.getOrNull(index) ?: BigInteger.ZERO)
            .compareTo(rightNumbers.getOrNull(index) ?: BigInteger.ZERO)
        if (result != 0) return result
    }
    val leftPre = left.groupValues[2]
    val rightPre = right.groupValues[2]
    if (leftPre.isEmpty() && rightPre.isEmpty()) return 0
    if (leftPre.isEmpty()) return 1
    if (rightPre.isEmpty()) return -1
    val leftParts = leftPre.split('.')
    val rightParts = rightPre.split('.')
    for (index in 0 until minOf(leftParts.size, rightParts.size)) {
        val a = leftParts[index]
        val b = rightParts[index]
        val aNumeric = a.toBigIntegerOrNull()
        val bNumeric = b.toBigIntegerOrNull()
        val result = when {
            aNumeric != null && bNumeric != null -> aNumeric.compareTo(bNumeric)
            aNumeric != null -> -1
            bNumeric != null -> 1
            else -> a.compareTo(b)
        }
        if (result != 0) return result
    }
    return leftParts.size.compareTo(rightParts.size)
}

internal fun shouldNotifyRelease(entry: NotificationEntry, installedName: String, installedCode: Long): Boolean =
    entry.versionCode?.let { it > installedCode }
        ?: entry.versionName?.let { (compareReleaseVersions(it, installedName) ?: 0) > 0 }
        ?: false

internal fun notificationKey(category: NotificationCategory, sourceId: String, entryId: String): String {
    // Length prefixes prevent ambiguous pairs such as ("a:b", "c") and ("a", "b:c").
    val input = "${sourceId.length}:$sourceId${entryId.length}:$entryId"
    val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
    return "freetime.notifications:${category.name}:" + digest.joinToString("") { "%02x".format(it) }
}
