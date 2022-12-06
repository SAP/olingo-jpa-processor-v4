package com.sap.olingo.jpa.processor.core.testmodel;

import java.nio.ByteBuffer;
import java.util.UUID;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

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
    byte[] buffer = new byte[16];
    ByteBuffer bb = ByteBuffer.wrap(buffer);
    bb.putLong(uuid.getMostSignificantBits());
    bb.putLong(uuid.getLeastSignificantBits());
    return buffer;
  }

  @Override
  public UUID convertToEntityAttribute(final byte[] bytes) {
    return bytes == null ? null : convertToUUID(bytes);
  }
  
  private UUID convertToUUID(byte[] bytes) {
    ByteBuffer bb = ByteBuffer.wrap(bytes);
    long high = bb.getLong();
    long low = bb.getLong();
    return new UUID(high, low);
  }
}
