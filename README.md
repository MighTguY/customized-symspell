# Customized SymSpell SpellCheck Java 
**This custmoized spell check is is based on the spell correction fuzzy search library [SymSpell](https://github.com/wolfgarbe/symspell) with a few customizations and optimizations**  

## Java Ported v6.6 (Bigrams)
* the optional bigram dictionary in order to use sentence level context information for selecting best spelling correction.


## SymSpell
* The Symmetric Delete spelling correction algorithm reduces the complexity of edit candidate generation and dictionary lookup for a given Damerau-Levenshtein distance. 
* It is six orders of magnitude faster (than the standard approach with deletes + transposes + replaces + inserts) and language independent.
* Opposite to other algorithms only deletes are required, no transposes + replaces + inserts. Transposes + replaces + inserts of the input term are transformed into deletes of the dictionary term.
* The speed comes from the inexpensive delete-only edit candidate generation and the pre-calculation.

## Customizations
* We replaced the **Damerau-Levenshtein** implementation with a **weighted Damerau-Levenshtein** implementation: where each operation (delete, insert, swap, replace) can have different edit weights.
* We added some customizing "hooks" that are used to rerank the top-k results (candidate list). The results are then reordered based on a combined proximity
  * added keyboard-distance to get a dynamic replacement weight (since letters close to each other are more likely to be replaced)
  * do some query normalization before search
  
## Keyboard based  Qwerty Distance

we used the adjancey graph of the keyboard for the weights to the connected nodes.
<img src="qwerty.png" align="center">

  
## Built With

* [Maven]()


## Versioning

We use [SemVer](http://semver.org/) for versioning. 

## Licenese

````
The MIT License (MIT)
Copyright © 2019 Lucky Sharma ( https://github.com/MighTguY/customized-symspell )
Copyright © 2018 Wolf Garbe (Original C# implementation https://github.com/wolfgarbe/SymSpell )

Permission is hereby granted, free of charge, to any person 
obtaining a copy of this software and associated documentation files
(the “Software”), to deal in the Software without restriction, 
including without limitation the rights to use, copy, modify,
merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is 
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall 
be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, 
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES 
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR 
THE USE OR OTHER DEALINGS IN THE SOFTWARE.
````

