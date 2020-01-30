# Benchmarking results
We have done benchmarking of the Vannila Symspell on a Mac(System details are mentioned below).
For benchmarking we have done benchmarking with the dataset of `30k` and `80k` words with edit distance of `[1, 2, 3]`.
Prefix length was constant to `7`.
Benchmarking also contain results for the Verbosity levels of **TOP**, **CLOSEST** and **ALL**


## Benchmark Summary
We have done 3 runs each for 30k and 80k data set, which also includes results for each verbosity level.
After the runs the final benchmarking looks like: 
```
Average Precalculation time instance 30843.33 ms
Average Lookup time instance 138141.09296296295 ns ~ 0.03814 ms
Total Lookup results instance 648092
``` 
Benchmark Summary is as follows:

### Run 1
```
DataSize        : 30,000
Queries         : 1000
PrefixLength    : 7
MaxEditDistance : 1

Precalculation Time take : 4.017 s
```

Verbosity       : __TOP__
```
Lookup        : 622 
Response Time : 12684 ns/op
```
Verbosity       : __CLOSEST__
```
Lookup        : 1610 
Response Time : 8646 ns/op
```
Verbosity       : __ALL__
```
Lookup        : 4693 
Response Time : 14304 ns/op
```

### Run 2

```
DataSize        : 82,761
Queries         : 1000
PrefixLength    : 7
MaxEditDistance : 1

Precalculation Time take : 4.653 s
```

Verbosity       : __TOP__
```
Lookup        : 635 
Response Time : 5740 ns/op
```
Verbosity       : __CLOSEST__
```
Lookup        : 2347 
Response Time : 5686 ns/op
```
Verbosity       : __ALL__
```
Lookup        : 6546 
Response Time : 13159 ns/op
```

### Run 3
```
DataSize        : 30,000
Queries         : 1000
PrefixLength    : 7
MaxEditDistance : 2

Precalculation Time take : 4.121 s
```

Verbosity       : __TOP__
```
Lookup        : 850 
Response Time : 17238 ns/op
```
Verbosity       : __CLOSEST__
```
Lookup        : 2876 
Response Time : 21163 ns/op
```
Verbosity       : __ALL__
```
Lookup        : 37058 
Response Time : 88658 ns/op
```

### Run 4

```
DataSize        : 82,761
Queries         : 1000
PrefixLength    : 7
MaxEditDistance : 2

Precalculation Time take : 62.863 s
```

Verbosity       : __TOP__
```
Lookup        : 858 
Response Time : 34335 ns/op
```
Verbosity       : __CLOSEST__
```
Lookup        : 4156 
Response Time : 146604 ns/op
```
Verbosity       : __ALL__
```
Lookup        : 48262 
Response Time : 262154 ns/op
```

### Run 5

```
DataSize        : 30,000
Queries         : 1000
PrefixLength    : 7
MaxEditDistance : 3

Precalculation Time take : 8.416 s
```

Verbosity       : __TOP__
```
Lookup        : 914 
Response Time : 144562 ns/op
```
Verbosity       : __CLOSEST__
```
Lookup        : 3165 
Response Time : 37652 ns/op
```
Verbosity       : __ALL__
```
Lookup        : 193833 
Response Time : 376399 ns/op
```

### Run 6

```
DataSize        : 82,761
Queries         : 1000
PrefixLength    : 7
MaxEditDistance : 1

Precalculation Time take : 59,105 s
```

Verbosity       : __TOP__
```
Lookup        : 920 
Response Time : 40069 ns/op
```
Verbosity       : __CLOSEST__
```
Lookup        : 4443 
Response Time : 40896 ns/op
```
Verbosity       : __ALL__
```
Lookup        : 333931 
Response Time : 1427456 ns/op
```


