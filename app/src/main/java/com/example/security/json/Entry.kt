package com.example.security.json

data class Entry<K, V>(val key: K, val value: V)

infix fun <A, B> A.to(that: B): Entry<A, B> = Entry(this, that)
