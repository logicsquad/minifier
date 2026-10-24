![](https://github.com/logicsquad/minifier/workflows/build/badge.svg)
[![License](https://img.shields.io/badge/License-BSD-blue.svg)](https://opensource.org/licenses/BSD-2-Clause)

Minifier
========

What is this?
-------------
This project provides Java classes to do one thing: safely "minify"
web resources. The key word is "safely": the aim is _not_ to produce
maximal size reduction, nor introduce any morphological changes, but
to provide _reasonable_ file size reduction in most cases.

At launch, we will provide classes to minify Javascript and CSS.

Getting started
---------------
There are two minifier classes:

* `CSSMinifier` for CSS; and
* `JSMinifier` for Javascript.

They both implement `Minifier`, which takes the text of a resource and
returns it minified:

    String minify(String source) throws MinificationException;

A minifier keeps no state between calls, so a single instance can
minify any number of resources, from any number of threads. For
example:

    Minifier minifier = new CSSMinifier();
    try {
        String minified = minifier.minify(css);
    } catch (MinificationException e) {
        // Handle exception
    }

If you have a `Reader` and a `Writer`, `Minifier` also has a default
method that reads the whole resource from one, and writes it, minified,
to the other:

    void minify(Reader reader, Writer writer) throws IOException, MinificationException;

It writes nothing unless minification succeeds, and it doesn't close
either stream: that's up to you.

A `MinificationException` will usually wrap some _other_
exception, such as `CSSMinifier.UnterminatedCommentException`. If you
need to know why minification failed, you can call `getCause()` on the
exception you catch.

Using Minifier
--------------
You can use Minifier in your projects by including it as a Maven
dependency:

    <dependency>
      <groupId>net.logicsquad</groupId>
      <artifactId>minifier</artifactId>
      <version>1.6</version>
    </dependency>

If you're building a Maven project, and just want to minify project
resources during the build, check out [Minifier Maven Plugin](https://github.com/logicsquad/minifier-maven-plugin),
which uses Minifier to do the work.

Contributing
------------
By all means, open issue tickets and pull requests if you have something
to contribute.

References
----------
Javascript minification is based on Douglas Crockford's [original
C-based JSMin](https://github.com/douglascrockford/JSMin), via a
[translation from C to
Java](https://github.com/galan/packtag/blob/master/packtag-core/src/main/java/net/sf/packtag/implementation/JSMin.java)
by John Reilly. Code from these projects is used here under license.

CSS minification is based on the
[CSSMin](https://github.com/barryvan/CSSMin) project by Barry van
Oudtshoorn. Code from this project is used here under license.
