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
