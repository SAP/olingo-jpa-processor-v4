# Contributing to the JPA Processor
You want to contribute to the JPA Processor? Welcome! Please read this document to understand what you can do:
 * [Report an Issue](#report-an-issue)
 * [Contribute Code](#contribute-code)

## Report an Issue
For bugs, questions and ideas for enhancement please open an issue here on GitHub.

In case you need to report a bug, please provide the following information:

1. The JPA Processor version you use.
2. The JPA implementation you use. EclipseLink, Hibernate, Open JPA or ...
3. The version of the JPA implementation.
4. A description of the model or a part of the model used when the problem occurred e.g. by providing an snippet of the $matadata document.
5. A description of the problem including the request, that create it.


## Contribute Code

You are welcome to contribute code to the JPA Processor in order to fix bugs or to propose enhancements.

There are three important things to know:

1.  You must be aware of the Apache License (which describes contributions) and **agree to the Developer Certificate of Origin**. This is common practice in all major Open Source projects. To make this process as simple as possible, we are using *[CLA assistant](https://cla-assistant.io/)*. CLA assistant is an open source tool that integrates with GitHub very well and enables a one-click-experience for accepting the DCO. See the respective section below for details.
2.  There are **some requirements regarding code style and quality** which need to be met (we also have to follow them). The respective section below gives more details on the coding guidelines.
3.  **Not all proposed contributions can be accepted**. The code must fit the overall direction of the JPAProcessor and really improve it. For most bug fixes this is a given, but it could haven that design ideas get violated.

### Developer Certificate of Origin (DCO)

Due to legal reasons, contributors will be asked to accept a DCO when they create the first pull request to this project. SAP uses [the standard DCO text of the Linux Foundation](https://developercertificate.org/).

This happens in an automated fashion during the submission process:	 the CLA assistant tool will add a comment to the pull request. Click it to check the DCO, then accept it on the following screen. CLA assistant will save this decision for upcoming contributions.

### Contribution Content Guidelines

Code that is contributed should use:

- [eclipse-codestyle-formatter.xml](https://github.com/SAP/olingo-jpa-processor-v4/blob/main/jpa/eclipse-codestyle-formatter.xml)
- [eclipse-cleanup-profile.xml](https://github.com/SAP/olingo-jpa-processor-v4/blob/main/jpa/eclipse-cleanup-profile.xml)

A contribution should contain tests when ever possible. An approved pull request must not be changed after the approval.
