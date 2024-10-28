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

They both implement `Minifier`, which declares a single method:

    void minify(Writer writer) throws MinificationException;

Further, they both extend `AbstractMinifier`, which provides a
constructor taking a `Reader` object. So you need a `Reader` and a
`Writer` to minify a resource, and that's it. For example:

    Reader input = new FileReader("basic.css");
    Writer output = new FileWriter("basic-min.css");
    Minifier min = new CSSMinifier(input);
    try {
        min.minify(output);
    } catch (MinificationException e) {
        // Handle exception
    }

And that's it. A `MinificationException` will usually wrap some _other_
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
      <version>1.3</version>
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
