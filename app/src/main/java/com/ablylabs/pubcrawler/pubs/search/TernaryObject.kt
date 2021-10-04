package com.ablylabs.pubcrawler.pubs.search

/**
Any object implementing this interface can be processed in
TernarySearchTree. The aim is to decouple ternary items from concrete
implementations. A single text must be provided by implementors
 * */
interface TernaryObject {
    val text: String
}