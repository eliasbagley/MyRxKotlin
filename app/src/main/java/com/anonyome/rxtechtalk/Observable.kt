package com.anonyome.rxtechtalk


interface Observer<in T> {
    fun onNext(value: T)
    fun onError(err: Throwable)
    fun onComplete()
}

class Observable<T> : Observer<T> {

    private var err: Throwable? = null
    private var complete = false
    private var buffer: List<T> = mutableListOf()
    private var observers: List<Observer<T>> = mutableListOf()

    companion object {

        fun <T> create(lambda: (Observer<T>) -> Unit): Observable<T> {
            return Observable<T>().apply {
                lambda(this)
            }
        }

        fun <T> just(value: T): Observable<T> {
            return create { s ->
                s.onNext(value)
                s.onComplete()
            }
        }

        fun <T> fromIterable(values: List<T>): Observable<T> {
            return create { s ->
                values.forEach { s.onNext(it) }
                s.onComplete()
            }
        }

    }

    fun subscribe(observer: Observer<T>) {
        observers += listOf(observer)
        drainBuffer()
    }

    private fun drainBuffer() {
        if (observers.isEmpty()) {
            return
        }

        buffer.forEach { value ->
            observers.forEach { it.onNext(value) }
        }

        //TODO handle errors and completion
        if (complete) {
            observers.forEach { it.onComplete() }
            return
        }

        if (err != null) {
            observers.forEach { it.onError(err!!) }
            return
        }

        buffer = mutableListOf()
    }

    // region observer methods
    override fun onNext(value: T) {
        if (hasTerminalEvents()) {
            return
        }

        buffer += listOf(value)

        drainBuffer()
    }

    override fun onError(err: Throwable) {
        this.err = err

        drainBuffer()
    }

    override fun onComplete() {
        complete = true

        drainBuffer()
    }

    // observer methods

    fun hasTerminalEvents() = complete || err != null

    fun await(): List<T> {
        val values = mutableListOf<T>()

        this.subscribe(object : Observer<T> {
            override fun onNext(value: T) {
                values += listOf(value)
            }

            override fun onError(err: Throwable) {
                throw err
            }

            override fun onComplete() {
            }

        })

        return values
    }
}


fun <T> Observable<T>.noop(): Observable<T> {
    val new = Observable<T>()

    this.subscribe(object : Observer<T> {
        override fun onNext(value: T) = new.onNext(value)

        override fun onError(err: Throwable) = new.onError(err)

        override fun onComplete() = new.onComplete()
    })

    return new
}

fun <T> Observable<T>.filter(predicate: (value: T) -> Boolean): Observable<T> {
    val new = Observable<T>()

    this.subscribe(object : Observer<T> {
        override fun onNext(value: T) {
            if (predicate(value)) {
                new.onNext(value)
            }
        }

        override fun onError(err: Throwable) = new.onError(err)

        override fun onComplete() = new.onComplete()
    })

    return new
}

fun <T, V> Observable<T>.map(mapper: (value: T) -> V): Observable<V> {
    val new = Observable<V>()

    this.subscribe(object : Observer<T> {
        override fun onNext(value: T) {
            new.onNext(mapper(value))
        }

        override fun onError(err: Throwable) = new.onError(err)

        override fun onComplete() = new.onComplete()
    })

    return new
}

fun <T, V> Observable<T>.flatMap(flatMapper: (value: T) -> Observable<V>): Observable<V> {
    val new = Observable<V>()

    this.subscribe(object : Observer<T> {
        override fun onNext(value: T) {
            val branch = flatMapper(value)

            branch.subscribe(object: Observer<V> {
                override fun onNext(value: V) {
                    new.onNext(value)
                }

                override fun onError(err: Throwable) = new.onError(err)

                override fun onComplete() {
                    // don't forward
                }

            })
        }

        override fun onError(err: Throwable) = new.onError(err)

        override fun onComplete() = new.onComplete()
    })

    return new
}

fun <T> Observable<T>.take(num: Int): Observable<T> {
    val new = Observable<T>()

    var numValues = 0

    this.subscribe(object : Observer<T> {
        override fun onNext(value: T) {
            numValues++

            if (numValues > num) {
                new.onComplete()
            } else {
                new.onNext(value)
            }
        }

        override fun onError(err: Throwable) = new.onError(err)

        override fun onComplete() = new.onComplete()
    })

    return new
}

fun <T> Observable<T>.merge(observable: Observable<T>): Observable<T> {
    val new = Observable<T>()

    this.subscribe(object : Observer<T> {
        override fun onNext(value: T) = new.onNext(value)

        override fun onError(err: Throwable) = new.onError(err)

        override fun onComplete() {

        }
    })

    observable.subscribe(object : Observer<T> {
        override fun onNext(value: T) = new.onNext(value)

        override fun onError(err: Throwable) = new.onError(err)

        override fun onComplete() {

        }
    })

    return new
}
