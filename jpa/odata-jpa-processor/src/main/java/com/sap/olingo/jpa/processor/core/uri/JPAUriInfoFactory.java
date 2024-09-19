package com.sap.olingo.jpa.processor.core.uri;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmStructuredType;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.api.uri.UriResourceSingleton;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.LevelsExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.processor.core.api.JPAODataPage;
import com.sap.olingo.jpa.processor.core.api.JPAODataPageExpandInfo;

public class JPAUriInfoFactory {

  private final JPAODataPage page;

  public JPAUriInfoFactory(@Nonnull final JPAODataPage page) {
    super();
    this.page = page;
  }

  public JPAUriInfoResource build() {
    if (page.expandInfo().isEmpty())
      // Standard pagination not resolving the $levels, as JPA Processor supports ODatas max option,
      // so depth is not known up-front and a late resolution has to be done anyhow. This also prevent the early
      // resolution of *
      return new JPAUriInfoBuilder(page.uriInfo())
          // .withResolvedStars()
          .withTop(page.top())
          .withSkip(page.skip())
          .withSkipToken(page.skipToken())
          .build();
    else
      return new JPAUriInfoBuilder(page.uriInfo())
          .withResolvedStars(page.expandInfo())
          .withExpandInfo(page.expandInfo())
          .withExpandOrderBy()
          .withTop(page.top())
          .withSkip(page.skip())
          .build();
  }

  private static class JPAUriInfoBuilder {
    private final JPAUriInfoResourceImpl uriInfo;

    private JPAUriInfoBuilder(final UriInfoResource original) {
      uriInfo = new JPAUriInfoResourceImpl(original);
    }

    /**
     * Searches for an expand star and resolves that path. This is only done for the depth of expandInfo
     * <p>
     * Examples:<br>
     * ...Es?$expand=* => ...Es?$expand=Navigation1,Navigation2<br>
     * ...Es?$expand=Navigation1($expand=*)
     * => ...Es?$expand=Navigation1($expand=Navigation1_1,Navigation1_2)<br>
     * ...Es?$expand=*($levels=2) =>
     * ...Es?$expand=Navigation1($expand=Navigation1_1,Navigation1_2),Navigation2($expand=Navigation2_1,Navigation2_2)<br>
     * ...Es?$expand=*($levels=max) =>
     * ...Es?$expand=Navigation1($expand=Navigation1_1($levels=max),Navigation1_2($levels=max)),Navigation2($expand=Navigation2_1($levels=max),Navigation2_2($levels=max))<br>
     * @param list
     * @param expandInfo
     * @return
     */
    public JPAUriInfoBuilder withResolvedStars(final List<JPAODataPageExpandInfo> expandInfo) {
      final var noLevels = expandInfo.size();
      if (uriInfo.getExpandOption() != null) {
        uriInfo.setSystemQueryOption(expandStarItems(uriInfo.getExpandOption(), uriInfo.getUriResourceParts(),
            noLevels));
      }
      return this;
    }

    private ExpandOption expandStarItems(@Nonnull final ExpandOption expandOption,
        final List<UriResource> parentResourceParts, final int noLevels) {
      final List<ExpandItem> items = new ArrayList<>(expandOption.getExpandItems());
      if (noLevels <= 0)
        return new JPAExpandOption(expandOption, items);
      for (int i = 0; i < items.size(); i++) {
        final var item = items.get(i);
        if (item.isStar()) {
          final var last = parentResourceParts.get(parentResourceParts.size() - 1);
          final LevelsExpandOption levels = getLevelsExpandOption(item);
          final List<ExpandItem> newItems = expandNavigation(item, (UriResourcePartTyped) last, levels);
          items.remove(i);
          items.addAll(newItems);
          i--; // NOSONAR
        } else if (item.getExpandOption() != null) {
          items.set(i, new JPAExpandItem(item, expandStarItems(item.getExpandOption(), item.getResourcePath()
              .getUriResourceParts(), noLevels - 1)));
        }
      }
      return new JPAExpandOption(expandOption, items);
    }

    private LevelsExpandOption getLevelsExpandOption(final ExpandItem item) {
      final var levels = item.getLevelsOption();
      if (levels != null) {
        return new JPALevelsExpandOption(levels).levelResolved();
      }
      return null;
    }

    /**
     * The uri info conversion based on an expand. An expand gets order by the foreign key. This has be rebuild.
     * @return
     */
    public JPAUriInfoBuilder withExpandOrderBy() {
      return this;
    }

    /**
     * Converts an uri info according to the information provided by expandInfo.
     * <p>
     * Examples:<br>
     * ...Es?$expand=Navigation => ...Es(key)/Navigation<br>
     * ...Singleton?$expand=Navigation =>
     * ...Singleton(key)/Navigation<br>
     * ...Es/Navigation1?$expand=Navigation2 => ...Es(key)/Navigation1/Navigation2<br>
     * ...Es?$expand=Navigation1($expand=Navigation2) => ...Es(key)/Navigation1(key)/Navigation2 //option<br>
     * ...Es?$expand=Navigation1,Navigation2) => ...Es(key)/Navigation1 //option<br>
     * ...Es?$expand=Navigation1($levels=2) => ...Es(key)/Navigation1?$expand=Navigation1<br>
     * ...Es?$expand=Navigation1($levels=max) => ...Es(key)/Navigation1?$expand=Navigation1($levels=max)
     * @param expandInfo
     * @return
     */
    public JPAUriInfoBuilder withExpandInfo(final List<JPAODataPageExpandInfo> expandInfo) {

      final List<UriResource> newResourceParts = new ArrayList<>(uriInfo.getUriResourceParts());
      JPALevelsExpandOption levelsOption = null;
      var expandOption = uriInfo.getExpandOption();
      var parts = uriInfo.getUriResourceParts();
      ExpandItem lastItem = null;
      for (final var info : expandInfo) {
        if (lastItem != null) {
          final var lastIndex = newResourceParts.size() - 1;
          newResourceParts.set(lastIndex, new JPAUriResourceNavigationImpl(((UriResourceNavigation) newResourceParts
              .get(lastIndex)).getProperty(), info.keyPath()));
        }
        final var parentParts = parts;
        lastItem = expandOption.getExpandItems().stream()
            .filter(item -> matchesNavigationName(info, item, parentParts))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Provided uri info does not contain an expand of " + info
                .navigationPropertyPath()));
        newResourceParts.addAll(lastItem.getResourcePath().getUriResourceParts());
        if (lastItem.getLevelsOption() == null) {
          parts = expandOption.getExpandItems().get(0).getResourcePath().getUriResourceParts();
          expandOption = expandOption.getExpandItems().get(0).getExpandOption();
        } else {
          levelsOption = new JPALevelsExpandOption(lastItem.getLevelsOption());
          levelsOption.levelResolved();
          expandOption = new JPAExpandOption(expandOption, levelsOption);
        }
      }

      if (levelsOption != null && levelsOption.getValue() == 0 && !levelsOption.isMax())
        expandOption = null;

      if (lastItem != null) {
        setOption(lastItem.getFilterOption(), SystemQueryOptionKind.FILTER);
        setOption(lastItem.getOrderByOption(), SystemQueryOptionKind.ORDERBY);
        setOption(lastItem.getSearchOption(), SystemQueryOptionKind.SEARCH);
        setOption(lastItem.getSelectOption(), SystemQueryOptionKind.SELECT);
        setOption(expandOption, SystemQueryOptionKind.EXPAND);

      }

      if (newResourceParts.get(0).getKind() == UriResourceKind.entitySet)
        newResourceParts.set(0, new JPAUriResourceEntitySetImpl((UriResourceEntitySet) newResourceParts.get(0),
            expandInfo.get(0)));
      uriInfo.setUriResourceParts(newResourceParts);

      return this;
    }

    public JPAUriInfoBuilder withSkipToken(@Nullable final Object skipToken) {
      if (skipToken == null)
        uriInfo.removeSystemQueryOption(SystemQueryOptionKind.SKIPTOKEN);
      else
        uriInfo.setSystemQueryOption(new JPASkipTokenOptionImpl(skipToken.toString()));
      return this;
    }

    JPAUriInfoBuilder withTop(final int top) {
      if (top != Integer.MAX_VALUE)
        uriInfo.setSystemQueryOption(new JPATopOptionImpl(top));
      return this;
    }

    JPAUriInfoBuilder withSkip(final int skip) {
      if (skip != 0)
        uriInfo.setSystemQueryOption(new JPASkipOptionImpl(skip));
      return this;
    }

    JPAUriInfoResource build() {
      return uriInfo;
    }

    private void setOption(final SystemQueryOption option, final SystemQueryOptionKind kind) {
      if (option == null)
        uriInfo.removeSystemQueryOption(kind);
      else
        uriInfo.setSystemQueryOption(option);
    }

    private boolean matchesNavigationName(final JPAODataPageExpandInfo info, final ExpandItem item,
        final List<UriResource> parentParts) {

      final var resourceParts = item.getResourcePath().getUriResourceParts();
      final var nameParts = info.navigationPropertyPath().split(JPAPath.PATH_SEPARATOR);
      final List<UriResourceComplexProperty> complexParentParts = parentParts.stream()
          .filter(part -> part.getKind().equals(UriResourceKind.complexProperty))
          .map(UriResourceComplexProperty.class::cast)
          .toList();

      if (resourceParts.size() + complexParentParts.size() != nameParts.length)
        return false;
      // Solve issue with complex from super ordinate part being part of info path
      for (int i = 0; i < complexParentParts.size(); i++) {
        if (!complexParentParts.get(i).getSegmentValue().equals(nameParts[i])) {
          return false;
        }
      }
      final int complex = complexParentParts.size();
      for (int i = complex; i < nameParts.length; i++) {
        if (!resourceParts.get(i - complex).getSegmentValue().equals(nameParts[i]))
          return false;
      }
      return true;
    }

    private List<ExpandItem> expandNavigation(final ExpandItem item, final UriResourcePartTyped last,
        final LevelsExpandOption levels) {

      final List<List<UriResourcePartTyped>> pathList = new ArrayList<>();
      pathList.add(List.of(last));
      getAllStructuredTypes(pathList, pathList.get(0));
      return pathList.stream()
          .map(this::getAllNavigationProperties)
          .flatMap(Collection::stream)
          .map(navigation -> new JPAExpandItem(item, navigation, levels))
          .map(i -> (ExpandItem) i)
          .toList();
    }

    private void getAllStructuredTypes(final List<List<UriResourcePartTyped>> pathList,
        final List<UriResourcePartTyped> parentPath) {

      final var parentType = ((EdmStructuredType) parentPath.get(parentPath.size() - 1).getType());

      parentType.getPropertyNames().stream()
          .map(parentType::getProperty)
          .filter(property -> property.getType() instanceof EdmStructuredType)
          .map(EdmProperty.class::cast)
          .map(JPAUriResourceComplexProperty::new)
          .forEach(type -> {
            final List<UriResourcePartTyped> path = new ArrayList<>(parentPath);
            path.add(type);
            pathList.add(path);
            this.getAllStructuredTypes(pathList, path);
          });
    }

    private List<List<UriResourcePartTyped>> getAllNavigationProperties(final List<UriResourcePartTyped> parentPath) {

      final var last = (EdmStructuredType) parentPath.get(parentPath.size() - 1).getType();
      final List<List<UriResourcePartTyped>> pathList = new ArrayList<>();
      last.getNavigationPropertyNames().stream()
          .map(last::getNavigationProperty)
          .map(JPAUriResourceNavigationImpl::new)
          .forEach(navigation -> {
            final List<UriResourcePartTyped> path = new ArrayList<>(parentPath);
            path.add(navigation);
            pathList.add(path);
          });
      return pathList;
    }

    private EdmStructuredType getEntityType(final UriResource last) {
      return switch (last.getKind()) {
        case entitySet -> ((UriResourceEntitySet) last).getEntityType();
        case singleton -> ((UriResourceSingleton) last).getEntityType();
        case navigationProperty -> ((UriResourceNavigation) last).getProperty().getType();
        case function -> (EdmEntityType) ((UriResourceFunction) last).getFunction().getReturnType().getType();
        case complexProperty -> ((UriResourceComplexProperty) last).getComplexType();
        default -> throw new IllegalAccessError();
      };
    }
  }
}
