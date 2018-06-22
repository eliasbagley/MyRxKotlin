package com.anonyome.rxtechtalk

import com.google.common.truth.Truth.assertThat
import org.junit.Test


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
//    @Test
//    fun testCreation() {
//        val o = Observable.create<Int> { o ->
//            o.onNext(5)
//            o.onComplete()
//        }
//    }

    @Test
    fun testJust() {
        val o = Observable.just(5)

        val values = o.await()

        assertThat(values).isEqualTo(listOf(5))
        assertThat(o.hasTerminalEvents())
    }

    @Test
    fun testFromIterable() {
        val arr = listOf(1, 2, 3)
        val o = Observable.fromIterable(arr)

        val values = o.await()

        assertThat(values).isEqualTo(arr)
        assertThat(o.hasTerminalEvents())
    }

    @Test
    fun testNoOp() {
        val arr = listOf(1, 2, 3)
        val o = Observable.fromIterable(arr)
                .noop()

        val values = o.await()

        assertThat(values).isEqualTo(arr)
        assertThat(o.hasTerminalEvents())
    }

    @Test
    fun testFilter() {
        val arr = listOf(1, 2, 3)
        val o = Observable.fromIterable(arr)
                .filter { it == 2}

        val values = o.await()

        assertThat(values).isEqualTo(listOf(2))
        assertThat(o.hasTerminalEvents())
    }

    @Test
    fun testMap() {
        val o = Observable.fromIterable(listOf(1, 2, 3))
                .map { "$it" }

        val values = o.await()

        assertThat(values).isEqualTo(listOf("1" ,"2", "3"))
        assertThat(o.hasTerminalEvents())
    }

    @Test
    fun testFlatMap() {
        val o = Observable.fromIterable(listOf(1, 2, 3))
                .flatMap {value ->
                    Observable.fromIterable(listOf(value, value))
                }

        val values = o.await()

        assertThat(values).isEqualTo(listOf(1, 1, 2, 2, 3, 3))
        assertThat(o.hasTerminalEvents())
    }

    @Test
    fun testMerge() {
        val o1 = Observable.just(1)
        val o2 = Observable.just(2)

        val o3 = o1.merge(o2)

        val values = o3.await()

        assertThat(values).isEqualTo(listOf(1, 2))
    }

}
