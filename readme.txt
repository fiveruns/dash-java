FiveRuns Dash Java API
===================

Dash is a system for collecting any type of metrics for your system.  This Java library
provides an API for collecting metric values from your Java process and uploading them 
to the Dash service for aggregation and display.

Please see the Dash service specification for a technical overview of Dash and how it works.

The Dash Java API provides basic metric support for Java applications.  It does not support traces
or exceptions as of this release.


Runtime Dependencies
==================

Java 5 or greater
Apache commons-httpclient 3.1
Apache commons-codec 1.2 (required by httpclient)
Apache commons-logging 1.0.4

The Apache jars can be found in the lib directory, along with the LICENSE file which must legally accompany them.

Installation
==================

Place the Dash jar and the jars for the dependencies above in your classpath.


Configuration
==================

Dash is configured with plain Java code.  See examples/integration.txt for a primer on how to configure and start Dash in your software.


Recipes
=================

Dash groups metrics into recipes.  This API ships with a single "Java VM" recipe which contains metrics which
track free memory, # of loaded classes and garbage collection time.  You can create your own recipes for subsystems 
like Tomcat or Hibernate or pull in 3rd party recipes.  See the Dash FAQ at http://dash.fiveruns.com/help for best practices, conventions and links to the latest recipes.


License
===========

# (The FiveRuns/MIT License)
#
# Copyright (c) 2009 FiveRuns Corporation
# 
# Permission is hereby granted, free of charge, to any person obtaining
# a copy of this software and associated documentation files (the
# 'Software'), to deal in the Software without restriction, including
# without limitation the rights to use, copy, modify, merge, publish,
# distribute, sublicense, and/or sell copies of the Software, and to
# permit persons to whom the Software is furnished to do so, subject to
# the following conditions:
# 
# The above copyright notice and this permission notice shall be
# included in all copies or substantial portions of the Software.
# 
# THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND,
# EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
# MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
# IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
# CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
# TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
# SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
