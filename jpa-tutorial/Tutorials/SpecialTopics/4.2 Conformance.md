# 4.2 Conformance
The following lists shall give you a rough overview of the topics where the JPA processor gives support. Please note that the decisions for certain functionality where not taken with the goal to fulfill a certain conformance level, but where driven by the need of a project.
## OData Minimal Conformance Level
|No|Requirement|State|
|:--- |:--- |:--- |
|1|MUST publish a service document at the service root ([section 11.1.1](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752280))| X |
|2|MUST return data according to at least one of the OData defined formats ([section 7](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752218))| X |
|3|MUST support server-driven paging when returning partial results ([section 11.2.5.7](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752288))| - |
|4|MUST return the appropriate OData-Version header ([section 8.1.5](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752225))| X |
|5|MUST conform to the semantics the following headers, or fail the request| |
|5.1|Accept ([section 8.2.1](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752227))| X |
|5.2|OData-MaxVersion ([section 8.2.7](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752233))| X |
|6|MUST follow OData guidelines for extensibility ([section 6 and all subsections](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752211))| ? |
|7|MUST successfully parse the request according to [OData-ABNF] for any supported system query string options and either follow the specification or return 501 Not Implemented ([section 9.3.1](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752255)) for any unsupported functionality (section 0)| X |
|8|MUST expose only data types defined in [OData-CSDL]| X |
|9|MUST NOT require clients to understand any metadata or instance annotations ([section 6.4](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752215)), custom headers ([section 6.5](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752216)), or custom content ([section 6.2](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752213)) in the payload in order to correctly consume the service| X |
|10|MUST NOT violate any OData update semantics ([section 11.4 and all subsections](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752297))| X |
|11|MUST NOT violate any other OData-defined semantics| X |
|12|SHOULD support $expand ([section 11.2.4.2](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752287))| X |
|13|MAY publish metadata at $metadata according to [OData-CSDL] ([section 11.1.2](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752281))| X |
|14|MUST include edit links (explicitly or implicitly) for all updatable or deletable resources according to [OData-Atom] and [OData-JSON]| ? |
|15|MUST support POST of new entities to insertable entity sets ([section 11.4.1.5](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752298) and [11.4.2.1](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752299))| X |
|16|MUST support POST of new related entities to updatable navigation properties ([section 11.4.6.1](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752303))| X |
|17|MUST support POST to $ref to add an existing entity to an updatable related collection ([section 11.4.6.1](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752303))| - |
|18|MUST support PUT to $ref to set an existing single updatable related entity ([section 11.4.6.3](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752303))| - |
|19|MUST support PATCH to all edit URLs for updatable resources ([section 11.4.3](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752300))| X |
|20|MUST support DELETE to all edit URLs for deletable resources ([section 11.4.5](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752302))| X |
|21|MUST support DELETE to $ref to remove an entity from an updatable navigation property ([section 11.4.6.2](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752303))| - |
|22|MUST support if-match header in update/delete of any resources returned with an ETag ([section 11.4.1.1](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752298))| - |
|23|MUST return a Location header with the edit URL or read URL of a created resource ([section 11.4.1.5](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752298))| X |
|24|MUST include the OData-EntityId header in response to any create or upsert operation that returns 204 No Content ([Section 8.3.3](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752238))| X |
|25|MUST support Upserts ([section 11.4.4](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752301))| X |
|26|SHOULD support PUT and PATCH to an individual primitive ([section 11.4.9.1](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752306)) or complex ([section 11.4.9.3](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752306)) property (respectively)| X |
|27|SHOULD support DELETE to set an individual property to null ([section 11.4.9.2](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752306))| X |
|28|SHOULD support deep inserts ([section 11.4.2.2](https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752299))| X |

## OData Intermediate Conformance Level
|No|Requirment|State|
|:--- |:--- |:--- |
|1|MUST conform to the OData Minimal Conformance Level| - |
|2|MUST successfully parse the [OData-ABNF] and either follow the specification or return 501 Not Implemented for any unsupported functionality (section 9.3.1)| X |
|3|MUST support $select| X |
|4|MUST support casting to a derived type according to [OData‑URL] if derived types are present in the model| - |
|5|MUST support $top| X |
|6|MUST support /$value on media entities (section 4.10. in [OData‑URL]) and individual properties (section 11.2.3.1)| X |
|7|MUST support $filter (section 11.2.5.1)|  |
|7.1|MUST support eq, ne filter operations on properties of entities in the requested entity set (section 11.2.5.1.1)| X |
|7.2|MUST support aliases in $filter expressions (section 11.2.5.1.3)| - |
|7.3|SHOULD support additional filter operations (section 11.2.5.1.1) and MUST return 501 Not Implemented for any unsupported filter operations (section 9.3.1)| X |
|7.4|SHOULD support the canonical functions (section 11.2.5.1.2) and MUST return 501 Not Implemented for any unsupported canonical functions (section 9.3.1)| ? |
|7.5|SHOULD support $filter on expanded entities (section 11.2.4.2.1)| X |
|8|SHOULD publish metadata at $metadata according to [OData-CSDL] (section 11.1.2)| X |
|9|SHOULD support the [OData-JSON] format| X |
|10|SHOULD consider supporting basic authentication as specified in [RFC2617] over HTTPS for the highest level of interoperability with generic clients| ? |
|11|SHOULD support the $search system query option (section 11.2.5.6)| X |
|12|SHOULD support the $skip system query option (section 11.2.5.4)| X |
|13|SHOULD support the $count system query option (section 11.2.5.5)| X |
|14|SHOULD support $expand (section 11.2.4.2)| X |
|15|SHOULD support the lambda operators any and all on navigation- and collection-valued properties (section 5.1.1.5 in [OData-URL])| ? |
|16|SHOULD support the /$count segment on navigation and collection properties (section 11.2.9)| X |
|17|SHOULD support $orderby asc and desc on individual properties (section 11.2.5.2)| X |

## OData Advanced Conformance Level
|No|Requirment|State|
|:--- |:--- |:--- |
|1|MUST conform to at least the OData Intermediate Conformance Level| - |
|2|MUST publish metadata at $metadata according to [OData-CSDL] (section 11.1.2)| X |
|3|MUST support the [OData-JSON] format| X |
|4|MUST support the /$count segment on navigation and collection properties (section 11.2.9)| X |
|5|MUST support the lambda operators any and all on navigation- and collection-valued properties (section 5.1.1.5 in [OData-URL])| ? |
|6|MUST support the $skip system query option (section 11.2.5.4)| X |
|7|MUST support the $count system query option (section 11.2.5.5)| X |
|8|MUST support $orderby asc and desc on individual properties (section 11.2.5.2)| X |
|9|MUST support $expand (section 11.2.4.2)|   |
|9.1|MUST support returning references for expanded properties (section 11.2.4.2)| ? |
|9.2|MUST support $filter on expanded entities (section 11.2.4.2.1)| X |
|9.3|MUST support cast segment in expand with derived types (section 11.2.4.2.1)| ? |
|9.4|SHOULD support $orderby asc and desc on individual properties (section 11.2.4.2.1)| X |
|9.5|SHOULD support the $count system query option for expanded properties (section 11.2.4.2.1)| X |
|9.6|SHOULD support $top and $skip on expanded properties (section 11.2.4.2.1)| X |
|9.7|SHOULD support $search on expanded properties (section 11.2.4.2.1)| X |
|9.8|SHOULD support $levels for recursive expand (section 11.2.4.2.1.1)| X |
|10|MUST support the $search system query option (section 11.2.5.6)| ? |
|11|MUST support batch requests (section11.7 and all subsections)| ? |
|12|MUST support the resource path conventions defined in [OData-URL]| X |
|13|SHOULD support Asynchronous operations (section 8.2.8.8)| - |
|14|SHOULD support Delta change tracking (section 8.2.8.6)| - |
|15|SHOULD support cross-join queries defined in [OData-URL]| - |
|16|SHOULD support a conforming OData service interface over metadata (section 11.1.3)| - |
