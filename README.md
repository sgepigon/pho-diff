# pho-diff

Visually compare the phonetic inventories of two languages.

# Why?

When learning a new language, you need to know the new sounds---the sounds that aren't a part of your native sound systems. I wanted a tool to quickly compare the different sounds of two languages. `pho-diff` presents two languages in the form of a `diff` of their [IPA charts](https://www.internationalphoneticassociation.org/content/full-ipa-chart). Given two languages `a` and `b`, if a symbol is absent in `b` but present in `a`, it's colored green. Likewise, if a symbol is absent in `a` but present in `b`, it's colored red (see [Examples](#examples)).

# Resources

  * The set of native language phonetic inventories are from the [Speech Accent Archive](http://accent.gmu.edu/browse_native.php). (you can read more about the project [here](http://accent.gmu.edu/about.php).)
  * [Interactive IPA Chart](http://www.ipachart.com/)

## Installation

Download from https://github.com/sgepigon/pho-diff.

## Usage

```
java -jar pho-diff-0.1.0-standalone.jar a b
```

`a` and `b` should be languages from the [Speech Accent Archive](http://accent.gmu.edu/browse_native.php).

## Examples

```
lein run english tagalog
```

```clojure
{:keys [:a "english" :b "tagalog"],
 :charts
 {:cons "resources/output/english-tagalog-cons.gif",
  :vowels "resources/output/english-tagalog-vowels.gif"},
 :other-sounds
 {:a #{"labio-velar voiced central approximant [w]" "5 diphthongs"},
  :b #{"labio-velar central approximant [w]"}},
 :sources
 {:a
  "http://accent.gmu.edu/browse_native.php?function=detail&languageid=18",
  :b
  "http://accent.gmu.edu/browse_native.php?function=detail&languageid=64"}}
```

![Consonant Diff Chart](resources/README/english-tagalog-cons.gif)
![Vowel Diff Chart](resources/README/english-tagalog-vowels.gif)


## License

Copyright © 2017 Santiago Gepigon III

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
