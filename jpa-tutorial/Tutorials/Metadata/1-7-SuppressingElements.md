# 1.7 Suppressing Elements

There are a number of use case not to put all the database artifacts that are required for an application or web service to the (OData) API. May be there are tables that are needed for technical reasons or some extra fields, which shall not be shown. To achieve that it is possible to annotate the corresponding Java artifact with `@EdmIgnore`. For the time being we want to suppress the _custom..._ attributes at the business partner:

```
...
public abstract class BusinessPartner {
  ...
  @EdmIgnore
  @Column(name = "\"CustomString1\"", length = 250)
  private String customString1;

  @EdmIgnore
  @Column(name = "\"CustomString2\"", length = 250)
  private String customString2;

  @EdmIgnore
  @Column(name = "\"CustomNum1\"", precision = 30, scale = 5)
  private BigDecimal customNum1;

  @EdmIgnore
  @Column(name = "\"CustomNum2\"", precision = 30, scale = 5)
  private BigDecimal customNum2;
  ...
}
```
As already indicated `@EdmIgnore` can also be used at tables or embeddables. Be aware that there are no special consistency checks in place that prevents you from using an ignored embeddable in an not ignored table.

Next step: [Tutorial 1.8 Functions](1-8-Functions.md)
