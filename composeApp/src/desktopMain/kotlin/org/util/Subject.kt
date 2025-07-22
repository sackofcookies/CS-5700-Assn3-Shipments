package org.util

interface Subject<T> {
    fun registerObserver(observer: T)
    fun unregisterObserver(observer: T)
    fun notifyObservers()
}