Build, maven plugins, reporting
===============================
This page contains notes about the build process, maven plugins and reporting.
Have a look here before you change the build.


Maven plugins
-------------

### Maven Surefire Plugin

The latest version of the `Maven Surefire Plugin` (maven-surefire-plugin, v2.19) has a bug s.t. single tests can't be run from the IDE/NetBeans (still works fine from the console though). 
That's not much fun, so version 2.18.1 it is in the meantime. Make sure this bug has been fixed before upgrading to a newer version.

See:
* [https://netbeans.org/bugzilla/show_bug.cgi?id=244404](https://netbeans.org/bugzilla/show_bug.cgi?id=244404)
* [https://issues.apache.org/jira/browse/SUREFIRE-1028](https://issues.apache.org/jira/browse/SUREFIRE-1028)


Reporting
---------

### Test coverage (unit and integration tests)

`Cobertura` didn't play nice with Java 8 (as of Oct. 2015, cobertura-maven-plugin, v2.7), hence `Jacoco` is used to produce test coverage reports instead.
`Jacoco` is nice too, and seems to recieve more love from maintainers anyways nowadays. So there's that.

The execution phases had to be tweaked in order to not produce the site folder too early, otherwise no other reports would be generated (there might be some kind of aggregate/merge button, alas I didn't find it...). 
Similarly do not try to change the `Jacoco` files (i.e. the Maven properties `testing.surefire.file` and `testing.failsafe.file`), or the reporting will fail.

See:
* [http://www.eclemma.org/jacoco/](http://www.eclemma.org/jacoco/)
