Unit and integration tests
==========================

> "Every line of code has been reached," which, from the perspective of theory of computation, is pure nonsense in terms of knowing whether the code does what it should.

James O Coplien, in: [Why Most Unit Testing is Waste](http://www.rbcs-us.com/documents/Why-Most-Unit-Testing-is-Waste.pdf)



Maven Surefire and Maven Failsafe
---------------------------------
* Several Maven profiles are configured. With the default profile only unit tests, but no integration tests are run.
* Unit test classes must have the postfix: `Test`
* Integration test classes must have the postfix: `IT`


Further reading:
* [http://martinfowler.com/tags/testing.html](http://martinfowler.com/tags/testing.html)
