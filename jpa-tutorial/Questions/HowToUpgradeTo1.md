# What needs to be done to come from 0.3.x to 1.x?

During the implementation of release 1.0.x, beside some additional functionality, some incompatible changes where introduced. The most important change is the introduction of the Request Context in analogy to the Session Context.

If we look at a Spring controller class, how it was in 0.3.9:

```Java
@RestController
@RequestMapping("bp/v1/**")
@RequestScope
public class ODataController {
  
  @Autowired
  private JPAODataCRUDContextAccess serviceContext;
  
  @RequestMapping(value = "**", method = { RequestMethod.GET, RequestMethod.PATCH, // NOSONAR
      RequestMethod.POST, RequestMethod.DELETE })
  public void crud(final HttpServletRequest req, final HttpServletResponse resp) throws ODataException {

    final JPAODataCRUDHandler handler = new JPAODataCRUDHandler(serviceContext);
    handler.getJPAODataRequestContext().setCUDRequestHandler(new JPAExampleCUDRequestHandler());
    handler.process(req, resp);
  }
}
```

We can see that an instance of JPAODataCRUDHandler is created. Afterward the request context is requested. The request context is filled with all the necessary information. In opposite to that in 1.x, like below in an example created with 1.0.8, the controller contains only the creation of the handler instance and the request context is injected:

```Java
@RestController
@RequestMapping("bp/v1/")
@RequestScope
public class ODataController {
  
  @Autowired
  private JPAODataSessionContextAccess serviceContext;
  @Autowired
  private JPAODataRequestContext requestContext;
  
  @RequestMapping(value = "**", method = { RequestMethod.GET, RequestMethod.PATCH, // NOSONAR
      RequestMethod.POST, RequestMethod.DELETE })
  public void crud(final HttpServletRequest req, final HttpServletResponse resp) throws ODataException {

    new JPAODataRequestHandler(serviceContext, requestContext).process(req, resp);
  }
}
```

As the session context, the request context is created in a spring configuration:

```Java
  @Bean
  @Scope(scopeName = SCOPE_REQUEST)
  public JPAODataRequestContext requestContext() {

    return JPAODataRequestContext.with()
        .setCUDRequestHandler(new JPAExampleCUDRequestHandler())
        .setDebugSupport(new DefaultDebugSupport())
        .build();
  }
```

In case you have read the code snippets carefully, you have noticed that a class and an interface have been renamed. The former JPAODataCRUDContextAccess became JPAODataSessionContextAccess and the former JPAODataCRUDHandler is now called JPAODataRequestHandler.

To use 1.x you have to adopt your code accordingly. It shall be mentioned, that the annotation `EdmAsEntitySet` has been deprecated and shall be replaced with `EdmEntityType`.
