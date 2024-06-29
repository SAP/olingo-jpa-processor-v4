# Parallel Processing

__This project is under construction! Feel free to try it.__

This project contains JPA Processor enhancements to process OData requests in parallel. As a first step GET requests that a combined in a $batch request are executed in parallel. The parallel processing is used if the corresponding factory is provided via the service context:

```java
.setBatchProcessorFactory(new JPAODataParallelBatchProcessorFactory())
```

It shall be mentioned that the OData specification would allow a parallel processing only if the clients sends a `continue-on-error` header, see: [Preference continue-on-error (odata.continue-on-error)](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#_Toc31358874)
