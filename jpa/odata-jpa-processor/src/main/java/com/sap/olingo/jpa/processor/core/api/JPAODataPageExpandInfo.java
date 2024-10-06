package com.sap.olingo.jpa.processor.core.api;

/**
 *
 * @param navigationPropertyPath Concatenated path information leading to the next expand
 * @param keyPath Concatenated key information to be used to convert the expand
 *
 * @author Oliver Grande
 * @since 2.2.0
 *
 */
public record JPAODataPageExpandInfo(String navigationPropertyPath, String keyPath) {

}
