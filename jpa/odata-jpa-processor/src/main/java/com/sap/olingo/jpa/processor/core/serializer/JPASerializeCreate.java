package com.sap.olingo.jpa.processor.core.serializer;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.queryoption.ApplyOption;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.LevelsExpandOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SearchOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;
import org.apache.olingo.server.api.uri.queryoption.TopOption;

import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPASerializerException;
import com.sap.olingo.jpa.processor.core.query.Utility;

final class JPASerializeCreate implements JPASerializer {
  private final ServiceMetadata serviceMetadata;
  private final UriInfoResource uriInfo;
  private final ODataSerializer serializer;
  private final JPAODataSessionContextAccess serviceContext;

  public JPASerializeCreate(final ServiceMetadata serviceMetadata, final ODataSerializer serializer,
      final UriInfoResource uriInfo, final JPAODataSessionContextAccess serviceContext) {
    this.uriInfo = uriInfo;
    this.serializer = serializer;
    this.serviceMetadata = serviceMetadata;
    this.serviceContext = serviceContext;
  }

  @Override
  public ContentType getContentType() {
    return null;
  }

  @Override
  public SerializerResult serialize(final ODataRequest request, final EntityCollection result)
      throws SerializerException, ODataJPASerializerException {

    final ExpandOption expandOption = new ExpandOptionWrapper(new ExpandItemWrapper());
    final EdmBindingTarget targetEdmBindingTarget = Utility.determineBindingTarget(uriInfo.getUriResourceParts());
    final EdmEntityType entityType = targetEdmBindingTarget.getEntityType();
    try {
      final ContextURL contextUrl = ContextURL.with()
          .serviceRoot(buildServiceRoot(request, serviceContext))
          .entitySetOrSingletonOrType(targetEdmBindingTarget.getName())
          .build();

      final EntitySerializerOptions options = EntitySerializerOptions.with()
          .contextURL(contextUrl)
          .expand(expandOption)
          .build();

      return serializer.entity(serviceMetadata, entityType, result
          .getEntities()
          .get(0),
          options);
    } catch (final URISyntaxException e) {
      throw new ODataJPASerializerException(e, HttpStatusCode.BAD_REQUEST);
    }
  }

  private static class ExpandItemWrapper implements ExpandItem {

    @Override
    public ApplyOption getApplyOption() {
      return null;
    }

    @Override
    public CountOption getCountOption() {
      return null;
    }

    @Override
    public ExpandOption getExpandOption() {
      return null;
    }

    @Override
    public FilterOption getFilterOption() {
      return null;
    }

    @Override
    public LevelsExpandOption getLevelsOption() {
      return null;
    }

    @Override
    public OrderByOption getOrderByOption() {
      return null;
    }

    @Override
    public UriInfoResource getResourcePath() {
      return null;
    }

    @Override
    public SearchOption getSearchOption() {
      return null;
    }

    @Override
    public SelectOption getSelectOption() {
      return null;
    }

    @Override
    public SkipOption getSkipOption() {
      return null;
    }

    @Override
    public EdmType getStartTypeFilter() {
      return null;
    }

    @Override
    public TopOption getTopOption() {
      return null;
    }

    @Override
    public boolean hasCountPath() {
      return false;
    }

    @Override
    public boolean isRef() {
      return false;
    }

    @Override
    public boolean isStar() {
      return true;
    }

  }

  private static class ExpandOptionWrapper implements ExpandOption {

    private final List<ExpandItem> items = new ArrayList<>(1);

    public ExpandOptionWrapper(final ExpandItemWrapper expandItemWrapper) {
      items.add(expandItemWrapper);
    }

    @Override
    public List<ExpandItem> getExpandItems() {
      return items;
    }

    @Override
    public SystemQueryOptionKind getKind() {
      return SystemQueryOptionKind.EXPAND;
    }

    @Override
    public String getName() {
      return null;
    }

    @Override
    public String getText() {
      return null;
    }
  }
}
