# FalconEngine

A Search Engine written in Java using a JavaFX GUI. This search engine was written in iterations as can be observed in the commits and the files, but the final product accepts a corpus of JSON files to build a searchable on-disk index. Both boolean and ranked retrieval are supported, in addition to the following special features: wildcard queries (using KGram indexing), spelling correction, and NEAR retrieval. Unit testing performed using JUnit.
<br>
# Why is it called the Falcon Engine? 
<br>Because despite being a bucket of bolts, it still makes the Kessler run in 12 parsecs.
<br>
# What are the other iterations?
<br>First iteration utilized in-memory indexes and a separate (but relevant) iteration used the indexing in the engine for classification of the unidentified Federalist Papers with Rocchio Classification and k-Nearest Neighbor Classification.
