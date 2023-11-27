package com.sap.olingo.jpa.processor.core.testmodel;

import java.nio.ByteBuffer;
import java.util.UUID;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Default converter to convert from {@link java.util.UUID} to a byte array.
 *
 * @author Oliver Grande
 */
@Converter(autoApply = false)
public class UUIDToBinaryConverter implements AttributeConverter<UUID, byte[]> {

  @Override
  public byte[] convertToDatabaseColumn(final UUID uuid) {
    return uuid == null ? null : convertToBytes(uuid);
  }

  private byte[] convertToBytes(final UUID uuid) {
    final byte[] buffer = new byte[16];
    final ByteBuffer bb = ByteBuffer.wrap(buffer);
    bb.putLong(uuid.getMostSignificantBits());
    bb.putLong(uuid.getLeastSignificantBits());
    return buffer;
  }

  @Override
  public UUID convertToEntityAttribute(final byte[] bytes) {
    return bytes == null ? null : convertToUUID(bytes);
  }

  private UUID convertToUUID(final byte[] bytes) {
    final ByteBuffer bb = ByteBuffer.wrap(bytes);
    final long high = bb.getLong();
    final long low = bb.getLong();
    return new UUID(high, low);
  }
}
