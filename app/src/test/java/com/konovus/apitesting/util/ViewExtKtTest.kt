package com.konovus.apitesting.util

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test


class ViewExtKtTest {

    data class Person(val name: String, val age: Int)

    @Before
    fun setup() {

    }

    @Test
    fun `replace, existing value, updated list`() {
        //given
        val list: List<Person> = listOf(
            Person("Vanea", 22),
            Person("Jora", 33),
            Person("Grisa", 44)
        )

        val updatedPerson = Person("Grisa", 25)
        val newList = list.replaceIf(updatedPerson) {
            it.name == "Grisa"
        }

        assertThat(newList).contains(updatedPerson)
    }

}