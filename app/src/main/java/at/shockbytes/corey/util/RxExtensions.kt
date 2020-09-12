package at.shockbytes.corey.util

import io.reactivex.Completable
import io.reactivex.Observable

fun completableOf(action: () -> Unit): Completable {
    return Completable.fromAction(action)
}

fun completableEmitterOf(
        action: () -> Unit
): Completable {
    return Completable.create { source ->

        try {
            action()
            source.onComplete()
        } catch (e: Throwable) {
            source.onError(e)
        }
    }
}
fun <T> observableEmitterOf(
        action: () -> T
): Observable<T> {
    return Observable.create { source ->
        try {
            source.onNext(action())
        } catch (e: Throwable) {
            source.onError(e)
        }
    }
}