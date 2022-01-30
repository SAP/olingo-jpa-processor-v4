# 4.6: Transient Fields

There are use case, which require that values of properties are not stored on the database, but calculated at runtime. One can think of different solutions depending on the exact requirements. This tutorial shall demonstrate two options. As an example we want to assume that the Person entity shall have a property that contains the full or concatenated name;

## Using Database Views

An elegant way to provide calculate the value of transient properties is to perform the calculation on the database.
This requires to separate to read and write version 
Create a scalar function 
Create a view 
## Using Conversion Hock

Annotation 
    at Properties and Collection Properties
    not at Navigation Properties

      @EdmTransient(requiredAttributes = { "lastName", "firstName" }, calculator = FullNameCalculator.class)
  @Transient
  private String fullName;