[![Build](https://github.com/logicsquad/minifier/actions/workflows/build.yml/badge.svg)](https://github.com/logicsquad/minifier/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/License-BSD-blue.svg)](https://opensource.org/licenses/BSD-2-Clause)

Minifier
========

What is this?
-------------
This project provides Java classes to do one thing: safely "minify"
web resources. The key word is "safely": the aim is _not_ to produce
maximal size reduction, nor introduce any morphological changes, but
to provide _reasonable_ file size reduction in most cases.

Getting started
---------------
There are two minifier classes:

* `CSSMinifier` for CSS; and
* `JSMinifier` for JavaScript.

They both implement `Minifier`, which takes the text of a resource and
returns it minified:

    String minify(String source) throws MinificationException;

A minifier keeps no state between calls, so a single instance can
minify any number of resources, from any number of threads. For
example, to minify a stylesheet in `css`:

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

A `MinificationException` says what went wrong, and where: its message
is something like `Unterminated comment at line 12, column 5`, and
`line()` and `column()` return the position, both numbered from 1.

Comments are removed, apart from those that start with `/*!`, which
mark a licence or copyright notice by convention: those are kept, where
they are.

Using Minifier
--------------
You can use Minifier in your projects by including it as a Maven
dependency:

    <dependency>
      <groupId>net.logicsquad</groupId>
      <artifactId>minifier</artifactId>
      <version>2.0</version>
    </dependency>

On the module path, its module is `net.logicsquad.minifier`.

If you're building a Maven project, and just want to minify project
resources during the build, check out [Minifier Maven Plugin](https://github.com/logicsquad/minifier-maven-plugin),
which uses Minifier to do the work.

Benchmarks
----------
[minification-benchmarks](https://github.com/privatenumber/minification-benchmarks)
compares JavaScript minifiers on a dozen widely used libraries. Minifier
isn't one of them, but run over the same files, `JSMinifier` 2.0
minifies all 12, and each result parses to the same program as its
original. Here are the sizes of its results, as a percentage of the
original's, beside those of [terser](https://github.com/terser/terser)
5.51, from the benchmark's own results:

| Library           | Original | gzipped | JSMinifier | gzipped | terser | gzipped |
|:------------------|---------:|--------:|-----------:|--------:|-------:|--------:|
| React 17.0.2      |  72.1 kB | 19.4 kB |        57% |     57% |    32% |     43% |
| Moment.js 2.29.1  |   174 kB | 36.2 kB |        56% |     69% |    34% |     52% |
| jQuery 3.5.1      |   288 kB | 84.5 kB |        50% |     48% |    31% |     37% |
| Vue 2.6.12        |   342 kB | 89.7 kB |        58% |     64% |    34% |     48% |
| Lodash 4.17.21    |   544 kB | 96.7 kB |        27% |     37% |    13% |     26% |
| D3 6.3.1          |   556 kB |  131 kB |        72% |     80% |    48% |     67% |
| terser 5.30.3     |  1.01 MB |  194 kB |        63% |     75% |    45% |     64% |
| three.js 0.124.0  |  1.25 MB |  248 kB |        76% |     78% |    52% |     64% |
| Victory 35.8.4    |  2.13 MB |  310 kB |        68% |     72% |    33% |     51% |
| ECharts 5.1.1     |  3.20 MB |  685 kB |        56% |     64% |    31% |     47% |
| Ant Design 4.16.1 |  6.67 MB |  825 kB |        67% |     76% |    34% |     55% |
| TypeScript 4.9.5  |  10.9 MB | 1.88 MB |        54% |     60% |    31% |     45% |

terser goes further, shortening names and rewriting code, which
`JSMinifier` never does. On an Apple M2, with Java 21, `JSMinifier`
takes about 10 ms to minify React, and 0.2 seconds for TypeScript, even
before the JIT has warmed up.

Contributing
------------
By all means, open issue tickets and pull requests if you have something
to contribute.

References
----------
JavaScript minification is based on Douglas Crockford's [original
C-based JSMin](https://github.com/douglascrockford/JSMin), via a
[translation from C to
Java](https://github.com/galan/packtag/blob/master/packtag-core/src/main/java/net/sf/packtag/implementation/JSMin.java)
by John Reilly. Code from these projects is used here under license.

CSS minification is based on the
[CSSMin](https://github.com/barryvan/CSSMin) project by Barry van
Oudtshoorn. Code from this project is used here under license.
