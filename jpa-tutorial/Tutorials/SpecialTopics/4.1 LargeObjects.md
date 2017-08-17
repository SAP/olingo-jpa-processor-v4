# 4.1 Reading Large Objects
JPA enable a tagging of large objects with the annotation @Lob. Java provides in addition two interfaces `java.sql.Blob` and `java.sql.Clob` to support the handling of Lobs. The interfaces have to be implemnted individually by each database provider, which makes it difficult to create a generic solution. Nevertheless they are supported in general.
## Metadata
Independent from the used type, if a property is annotated with @Lob `maxlength` is suppressed.

For _Blob_:

If JPA entity property is either of type byte[] or java.sql.Blob and annotated with @Lob the corresponding EDM property will get type Edm.Binary.

For _Clob_:

If JPA entity property is either of type String or java.sql.Clob and annotated with @Lob the corresponding EDM property will get type as Edm.String.
## Reading Large Objects
Olingo itself does not support `java.sql.Blob` or  `java.sql.Clob`. Therefore the Result Converter has to switch the type from Blob to byte[] respectifly Clob to String by calling `getBytes(long pos, int length)` or `getSubString(long pos, int length)`.

## Modifying Large Objects
JPA does not have the option to create Blob or Clob instances. Therefore the JPA Processor provides the data as byte[] or String. Within the JPA CUD Handler implementation the data have to be converted into an DB specific implmentation of `java.sql.Blob` or `java.sql.Clob`.
