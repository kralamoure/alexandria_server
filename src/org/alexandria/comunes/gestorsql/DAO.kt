package org.alexandria.comunes.gestorsql

interface DAO<T> {
    fun load(obj: Any?)
    fun update(obj: T): Boolean
}