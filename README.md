# Where can I go

Web application showing flight ticket prices for multiple destinatons at the same time. See http://where2fly2.com

The application has limited access to flight data. It uses the following sources:
*  Transavia flight data exposed by open API - updated daily.
* Google QPX Flight data - queries are limited to the free quota of 50 requests peer day, so the shown prices are not always up to date.

Unfortunaltely, this date is not enough for a good search.

## Build and run

    lein bower install
    
From REPL, start `wcig.web.start-dev`.
Or, after build:

   -java -cp [uberjar, config files...] wcig.run :prod 3030 localhost 27017

## Usage

In [environ](https://github.com/weavejester/environ) key :transavia-key must be configured.  

## License

Copyright © 2016

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
